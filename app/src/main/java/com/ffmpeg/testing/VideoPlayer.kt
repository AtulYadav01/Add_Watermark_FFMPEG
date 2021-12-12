package com.bunty.viewanddatabinding

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.*
import com.bunty.viewanddatabinding.databinding.ActivityVideoPlayerBinding
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.upstream.*
import java.util.*
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException

import android.widget.Toast
import androidx.core.content.ContextCompat

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler

import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import java.lang.Exception
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException

import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.simform.videooperations.*
import android.media.MediaMetadataRetriever
import android.os.*
import androidx.core.net.toFile
import androidx.core.view.isVisible
import com.blankj.utilcode.util.TimeUtils
import java.io.File
import java.util.logging.Handler


class VideoPlayer : AppCompatActivity(), Player.EventListener {

    private lateinit var simpleExoplayer: SimpleExoPlayer
    private var playbackPosition: Long = 0
    private val mp4Url = "https://html5demos.com/assets/dizzy.mp4"
    private val dashUrl = "https://storage.googleapis.com/wvmedia/clear/vp9/tears/tears_uhd.mpd"
    private val urlList = listOf(mp4Url to "default", dashUrl to "dash")
    lateinit var binding: ActivityVideoPlayerBinding

    var uniqueVideo = ""
    var uniqueTextVideo = ""
    var outputPath = ""
    var inputPath = ""

    private val dataSourceFactory: DataSource.Factory by lazy {
        DefaultDataSourceFactory(this, "exoplayer-sample")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        /* var str2 = arrayOf(
             "-i",
             inputPath,
             "-i",
             image,
             "-filter_complex",
             "[1] scale=70:70 [tmp];[0][tmp] overlay=main_w-overlay_w-10:main_h-overlay_h-10",
             outputPath
         )*/
       // downloadFile()
        binding.downloadBtn.setOnClickListener {
            downloadFile()
         //   addWaterMark()
        }

       /* binding.deleteBtn.setOnClickListener {
            var uri = Uri.fromFile(File(inputPath))
            val file = uri.toFile()
            file.delete()
        }*/

        /* val downloadFolder = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
         Toast.makeText(this, downloadFolder.toString(), Toast.LENGTH_SHORT).show()*/

    }

    fun addWaterMark() {
        binding.progressBar.isVisible = true
        // var font = R.font.little_lord
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource("/storage/emulated/0/Android/data/com.ffmpeg.testing/files/File/myVid.mp4")
        val width = 460
        Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
        val height = 380
        Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
        retriever.release()

        var posx = width - 100f
        var posy = height - 50f

        uniqueTextVideo = "textwaterVid" + TimeUtils.getNowMills() + ".mp4"

        val fontPath =
            "/storage/emulated/0/Android/data/com.ffmpeg.testing/files/File/little_lord.ttf"
        var color = R.color.white
        inputPath =
            "/storage/emulated/0/Android/data/com.ffmpeg.testing/files/File/$uniqueVideo"
        outputPath =
            "/storage/emulated/0/Android/data/com.ffmpeg.testing/files/File/$uniqueTextVideo"

        var random = (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()


        val query =
            FFmpegQueryExtension().addTextOnVideo(
                inputPath,
                random.toString(),
                posx,
                posy,
                fontPath,
                true,
                25,
                "#ffffff",
                outputPath
            )

        CallBackOfQuery().callQuery(this, query, object : FFmpegCallBack {
            override fun statisticsProcess(statistics: Statistics) {
                Log.i("FFMPEG LOG : ", statistics.videoFrameNumber.toString())
            }

            override fun process(logMessage: LogMessage) {
                Log.d("FFMPEG2", logMessage.text + logMessage.toString())
            }

            override fun success() {
                binding.progressBar.isVisible = false
                shareVideo()
                var uri = Uri.fromFile(File(inputPath))
                val file = uri.toFile()
                file.delete()
                Log.d("FFMPEG3", "success")
            }

            override fun cancel() {
                Log.d("FFMPEG4", "cancle")
            }

            override fun failed() {
                Log.d("FFMPEG5", "failed")
            }
        })

    }

    fun downloadFile() {
        uniqueVideo = "textVid" + TimeUtils.getNowMills() + ".mp4"
        Toast.makeText(this, "Download started", Toast.LENGTH_LONG).show()
        val DownloadUrl =
            mp4Url
        val request1 = DownloadManager.Request(Uri.parse(DownloadUrl))
        request1.setDescription("Sample Video File") //appears the same in Notification bar while downloading
        request1.setTitle("Video.mp4")
        request1.setVisibleInDownloadsUi(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request1.allowScanningByMediaScanner()
            request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
        }
        request1.setDestinationInExternalFilesDir(applicationContext, "/File", "$uniqueVideo")
        val manager1 = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        Objects.requireNonNull(manager1).enqueue(request1)
        if (DownloadManager.STATUS_SUCCESSFUL == 8) {
            Toast.makeText(this, "Download Successful", Toast.LENGTH_LONG).show()
          Handler().postDelayed({
              addWaterMark()
          },2000)
        }
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer() {
        simpleExoplayer = SimpleExoPlayer.Builder(this).build()
        val randomUrl = urlList.random()
        preparePlayer(randomUrl.first, randomUrl.second)
        binding.exoplayerView.player = simpleExoplayer
        simpleExoplayer.seekTo(playbackPosition)
        simpleExoplayer.playWhenReady = true
        simpleExoplayer.addListener(this)
    }

    private fun buildMediaSource(uri: Uri, type: String): MediaSource {
        return if (type == "dash") {
            DashMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
        } else {
            ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri)
        }
    }

    private fun preparePlayer(videoUrl: String, type: String) {
        val uri = Uri.parse(videoUrl)
        val mediaSource = buildMediaSource(uri, type)
        simpleExoplayer.prepare(mediaSource)
    }

    private fun releasePlayer() {
        playbackPosition = simpleExoplayer.currentPosition
        simpleExoplayer.release()
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        // handle error
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == Player.STATE_BUFFERING)
            binding.progressBar.visibility = View.VISIBLE
        else if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED)
            binding.progressBar.visibility = View.INVISIBLE
    }

    fun shareVideo() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "video/mp4"
        var uri = Uri.fromFile(File(outputPath))
        val file = uri.toFile()
        Log.d("uri", uri.toString())
        Log.d("file", file.toString())
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        startActivity(Intent.createChooser(shareIntent, "Share video using"))
    }

}
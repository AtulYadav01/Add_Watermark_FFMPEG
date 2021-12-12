package com.bunty.viewanddatabinding

import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import androidx.core.view.isVisible
import com.bunty.viewanddatabinding.databinding.ActivityWaterMarkBinding
import com.simform.videooperations.*
import java.io.File
import java.io.InputStream
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import com.blankj.utilcode.util.TimeUtils


class WaterMark : AppCompatActivity() {

    lateinit var binding: ActivityWaterMarkBinding
    var REQUEST_VIDEO_CODE = 100
    var selectedVideoPath = ""
    var outputPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_water_mark)
        binding = ActivityWaterMarkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        binding.apply {
            selectVidBtn.setOnClickListener {
                selectVid()
            }

            convertWatermarkBtn.setOnClickListener {
                addImageWaterMark()
            }
        }

    }

    fun selectVid() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(
            Intent.createChooser(intent, "Select Video"),
            REQUEST_VIDEO_CODE
        )

    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_VIDEO_CODE) {
                val selectedVideoUri = data?.data
                selectedVideoPath = getPath(selectedVideoUri)
                binding.selectVidText.text = selectedVideoPath
            }
        }
    }*/

    /*fun getPath(uri: Uri?): String {
        var cursor: Cursor? = null
        return try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            cursor = contentResolver.query(uri!!, projection, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }*/

    fun addImageWaterMark() {
        binding.progressBar.isVisible = true
        var waterImage = 1

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource("/storage/emulated/0/Android/data/com.ffmpeg.testing/files/File/myVid.mp4")
        val width = 460
        Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH))
        val height = 380
        Integer.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT))
        retriever.release()

        var posx = width - 50f
        var posy = height - 50f

        val image =
            "/storage/emulated/0/Android/data/com.ffmpeg.testing/files/File/note.png"
        var color = R.color.white
        var inputPath =
            "/storage/emulated/0/Android/data/com.ffmpeg.testing/files/File/myVid.mp4"
        outputPath =
            "/storage/emulated/0/Android/data/com.ffmpeg.testing/files/File/waterVid"+TimeUtils.getNowMills()+".mp4"
        Log.d("color", color.toString())

        val query =
            FFmpegQueryExtension().addVideoWaterMark(
                inputPath,
                image,
                posx,
                posy,
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
                Log.d("FFMPEG3", "success")
            }

            override fun cancel() {
                Log.d("FFMPEG4", "cancle")
            }

            override fun failed() {
                binding.progressBar.isVisible = false
                Log.d("FFMPEG5", "failed")
            }
        })
    }

    /*fun addWaterMark() {
        val fontPath = Common.getFileFromAssets(this, "font/little_lord.ttf").absolutePath

        var input =  "/storage/emulated/0/Android/data/com.ffmpeg.testing/files/File/myVid.mp4"
        outputPath =
            "/storage/emulated/0/Android/data/com.ffmpeg.testing/files/File/new.mp4"

        Log.d("link", binding.selectVidText.text.toString().trim())

        val query =
            FFmpegQueryExtension().addTextOnVideo(
                input,
                "water mark",
                50f,
                50f,
                fontPath,
                true,
                50,
                "#ffffff",
                outputPath
            )

        CallBackOfQuery().callQuery(this, query, object : FFmpegCallBack {
            override fun statisticsProcess(statistics: Statistics) {
                Log.i("FFMPEG1: ", statistics.videoFrameNumber.toString())
            }

            override fun process(logMessage: LogMessage) {
                Log.d("FFMPEG2",logMessage.text + logMessage.toString())
            }

            override fun success() {
                binding.afterVidText.setText(outputPath)
                Log.d("FFMPEG3","success")
            }

            override fun cancel() {
                Log.d("FFMPEG4","cancle")
            }

            override fun failed() {
                Log.d("FFMPEG5","failed")
            }
        })

    }*/

    fun shareVideo() {

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "video/mp4"
        var uri =  Uri.fromFile( File(outputPath))
        val file = uri.toFile()
        Log.d("uri",uri.toString())
        Log.d("file",file.toString())
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file))
        startActivity(Intent.createChooser(shareIntent, "Share video using"))

    }

    /* var uri = Uri.fromFile( File("/storage/emulated/0/Android/data/com.ffmpeg.testing/files/File/waterWithImage2.mp4"))
     var file: File = File(getPath(uri))


     fun getPath(uri: Uri?): String? {
         val projection = arrayOf(MediaStore.Images.Media.DATA)
         val cursor = contentResolver.query(uri!!, projection, null, null, null) ?: return null
         val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
         cursor.moveToFirst()
         val s = cursor.getString(column_index)
         cursor.close()
         return s
     }*/


}
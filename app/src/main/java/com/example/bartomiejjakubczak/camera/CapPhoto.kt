@file:Suppress("DEPRECATION")

package com.example.bartomiejjakubczak.camera

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Environment
import android.os.IBinder
import android.os.StrictMode
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CapPhoto : Service() {

    companion object {
        val DEBUG_TAG = "CameraService"
    }

    private lateinit var camera: Camera

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val callback = Camera.PictureCallback { data, camera ->
            Log.d(DEBUG_TAG, "Photo taken!")

            if(isExternalStorageWritable()){
             val mediaStorageDir = File(
                     Environment.getExternalStoragePublicDirectory
                     (Environment.DIRECTORY_PICTURES),"App4Hab")

                if(!mediaStorageDir.exists()){
                    if(!mediaStorageDir.mkdirs()){
                        Log.d(DEBUG_TAG, "failed to create directory")
                    }
                }

                try {
                    val cal = Calendar.getInstance()
                    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
                    val tar = sdf.format(cal.time)

                    val outputFile = File(mediaStorageDir, "$tar.jpg")
                    var outStream = FileOutputStream(outputFile)
                    with(outStream){
                        outStream.write(data)
                        outStream.close()
                    }

                    Log.d(DEBUG_TAG, "${data.size} byte written to $mediaStorageDir$tar.jpg")
                    camera.stopPreview()
                    camera.release()

                }catch (e: FileNotFoundException){
                    Log.d(DEBUG_TAG, e.message)
                }catch (e: IOException){
                    Log.d(DEBUG_TAG, e.message)
                }

            }
            else{
                Log.d(DEBUG_TAG, "Failed to reach the external storage.")
            }
        }
        val cameraCount = Camera.getNumberOfCameras()
        camera = when (cameraCount) {
            2 -> Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
            1 -> Camera.open()
            else -> throw IllegalStateException()
        }

        val surfaceView = SurfaceView(applicationContext)

        try {
            camera.setPreviewDisplay(surfaceView.holder)
            camera.setPreviewTexture(SurfaceTexture(Context.MODE_PRIVATE))
            val params = camera.parameters
            camera.parameters = params
            camera.startPreview()
            camera.takePicture(null, null, callback)
        } catch (e: IOException) {
            Log.d(DEBUG_TAG, "Camera service failed")
            surfaceView.holder.setType((SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS))
        }
        return Service.START_STICKY_COMPATIBILITY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(DEBUG_TAG, "Camera service created")

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}
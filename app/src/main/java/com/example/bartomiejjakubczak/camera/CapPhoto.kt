@file:Suppress("DEPRECATION")

package com.example.bartomiejjakubczak.camera

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.Parameters.*
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

        val cameraNumber = Camera.getNumberOfCameras()
        openCamera(cameraNumber)
        Log.d(DEBUG_TAG, "Camera opened successfully.")

        try {
            /* taking picture here*/
            camera.setPreviewTexture(SurfaceTexture(Context.MODE_PRIVATE)) /*This is needed
            for the application to work, as we are not using any view or surface holder.*/
            camera.startPreview() //start previewing the picture to be taken
            Log.d(DEBUG_TAG, "Preview started.")
            cameraConfiguration() //will take the best resolutions for pictures and preview
            camera.autoFocus(autoFocusCallback) //autoFocusCallback calls for taking pictures

        } catch (e: IOException) {
            Log.d(DEBUG_TAG, "Camera service failed")
        }
        return Service.START_STICKY_COMPATIBILITY
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(DEBUG_TAG, "Camera service created")

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    override fun onBind(intent: Intent?): IBinder? { /*this was necessary to implement
    as a part of interface */
        return null
    }

    /* ------------------------------------FUNCTIONS--------------------------------------------*/

    private fun isExternalStorageWritable(): Boolean { //check if memory card is available
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private val pictureCallback = Camera.PictureCallback { data, camera ->
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

    private val autoFocusCallback = Camera.AutoFocusCallback{success, camera ->
        Log.d(DEBUG_TAG, "Auto focus successful: $success")
            if(success){
                camera.takePicture(null, null,null, pictureCallback)
                Log.d(DEBUG_TAG, "End of the process.")
            }
        //TODO We have to figure out the situation in which camera didnt focus
    }

    private fun openCamera(cameraCount: Int): Camera{
        camera = when (cameraCount) {
            2 -> Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
            1 -> Camera.open()
            else -> throw IllegalStateException()
        }
        return camera
    }


    private fun cameraConfiguration(){

        val parameters: Camera.Parameters = camera.parameters

        val previewSizes = parameters.supportedPreviewSizes
        val imageSizes = parameters.supportedPictureSizes

        parameters.setPreviewSize(previewSizes[0].width,previewSizes[0].height)
        parameters.setPictureSize(imageSizes[0].width, imageSizes[0].height)
        parameters.pictureFormat = ImageFormat.JPEG
        parameters.autoWhiteBalanceLock = false
        parameters.autoExposureLock = false
        parameters.focusMode = FOCUS_MODE_AUTO //It needs to be auto in order to let focus callback work
        parameters.set("iso-values", 100) //This will help us in taking pictures in sharp light
        parameters.set("burst-capture", 1) //I read in some article that this helps to stabilize photos
        //we could also try modify whitebalance value to for example daylight

        println(parameters.flatten())
        camera.parameters = parameters
        Log.d(DEBUG_TAG, "Configuration set.")
    }
}
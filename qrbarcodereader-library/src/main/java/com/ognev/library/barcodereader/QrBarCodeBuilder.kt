package com.ognev.library.barcodereader;

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewTreeObserver
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalStateException

/**
 * QrBarCodeBuilder Singleton.
 */
class QrBarCodeBuilder

private constructor(builder: Builder) {
  private val PERMISSIONS_REQUEST_CAMERA_ACCESS: Int = 101
  private val LOGTAG = this@QrBarCodeBuilder.toString()
  private var cameraSource: CameraSource? = null
  private var barcodeDetector: BarcodeDetector? = null

  private val width: Int
  private val height: Int
  private val facing: Int
  private val qrBarCodeDataListener: QrBarCodeListener?
  private lateinit var context: Context
  private var surfaceView: SurfaceView? = null
  private var autoFocusEnabled: Boolean = false
  private var activity: Activity? = null
  private var activitySupport: ActivityCompat? = null
  private var fragment: Fragment? = null
  private var fragmentSupport: android.support.v4.app.Fragment? = null

  /**
   * Is camera running boolean.

   * @return the boolean
   */
  var isCameraRunning = false
    private set

  private var surfaceCreated = false

  fun initAndStart(surfaceView: SurfaceView) {

    surfaceView.viewTreeObserver
        .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
          override fun onGlobalLayout() {
            init()
            start()
            removeOnGlobalLayoutListener(surfaceView, this)
          }
        })
  }

  private val surfaceHolderCallback = object : SurfaceHolder.Callback {
    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
      //we can start barcode after after creating
      surfaceCreated = true
      startCameraView(context, cameraSource, surfaceView)
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {}

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
      surfaceCreated = false
      stop()
      surfaceHolder.removeCallback(this)
    }
  }

  init {
    this.autoFocusEnabled = builder.autofocusEnabled
    this.width = builder.width
    this.height = builder.height
    this.facing = builder.facing
    this.qrBarCodeDataListener = builder.qrBarCodeDataListener
    this.context = builder.context
    this.surfaceView = builder.surfaceView
    //for better performance we should use one detector for all Reader, if builder not specify it
    if (builder.barcodeDetector == null) {
      this.barcodeDetector  = BarcodeDetector.Builder(context.applicationContext).setBarcodeFormats(
          Barcode.QR_CODE).build()
    } else {
      this.barcodeDetector = builder.barcodeDetector
    }
  }


  /**
   * Init.
   */
  private fun init() {
    if (!hasAutofocus(context)) {
      Log.e(LOGTAG, "Do not have autofocus feature, disabling autofocus feature in the library!")
      autoFocusEnabled = false
    }

    if (!hasCameraHardware(context)) {
      Log.e(LOGTAG, "Does not have camera hardware!")
      return
    }
    if (!checkCameraPermission(context)) {
      if(context is Activity) {
        activity = context as Activity
        ActivityCompat.requestPermissions(activity!!,
            arrayOf(Manifest.permission.CAMERA),
            PERMISSIONS_REQUEST_CAMERA_ACCESS)
      }

      if(context is Fragment) {
        fragment = context as Fragment
        ActivityCompat.requestPermissions(fragment!!.activity,
            arrayOf(Manifest.permission.CAMERA),
            PERMISSIONS_REQUEST_CAMERA_ACCESS)
      }

      if(context is android.support.v4.app.Fragment) {
        fragmentSupport = context as android.support.v4.app.Fragment
        ActivityCompat.requestPermissions(fragmentSupport!!.activity,
            arrayOf(Manifest.permission.CAMERA),
            PERMISSIONS_REQUEST_CAMERA_ACCESS)
      }

      Log.e(LOGTAG, "Do not have camera permission!")


      return
    }

    if (barcodeDetector!!.isOperational) {
      barcodeDetector!!.setProcessor(object : Detector.Processor<Barcode> {
        override fun release() {
          // Handled via public method
        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>) {
          val barcodes = detections.detectedItems
          if (barcodes.size() != 0 && qrBarCodeDataListener != null) {
            qrBarCodeDataListener!!.onDetected(barcodes.valueAt(0).displayValue)
          }
        }
      })

      cameraSource = CameraSource.Builder(context, barcodeDetector!!)
          .setAutoFocusEnabled(autoFocusEnabled)
          .setFacing(facing)
          .setRequestedPreviewSize(width, height)
          .build()
    } else {
      Log.e(LOGTAG, "Barcode recognition libs are not downloaded and are not operational")
    }
  }

  /**
   * Start scanning qr codes.
   */
  fun start() {
    if (surfaceView != null && surfaceHolderCallback != null) {
      //if surface already created, we can start camera
      if (surfaceCreated) {
        startCameraView(context, cameraSource, surfaceView!!)
      } else {
        //startCameraView will be invoke in void surfaceCreated
        surfaceView!!.holder.addCallback(surfaceHolderCallback)
      }
    }
  }

  private fun startCameraView(context: Context, cameraSource: CameraSource?,
      surfaceView: SurfaceView?) {
    if (isCameraRunning) {
      return
//      throw IllegalStateException("Camera already started!")
    }
    try {
      if (ActivityCompat.checkSelfPermission(context,
          Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        Log.e(LOGTAG, "Permission not granted!")
      } else if (!isCameraRunning && cameraSource != null && surfaceView != null) {
        cameraSource.start(surfaceView.holder)
        isCameraRunning = true
      }
    } catch (ie: IOException) {
      Log.e(LOGTAG, ie.message)
      ie.printStackTrace()
    }

  }

  /**
   * Stop camera
   */
  fun stop() {
    try {
      if (isCameraRunning && cameraSource != null) {
        cameraSource!!.stop()
        isCameraRunning = false
      }
    } catch (ie: Exception) {
      Log.e(LOGTAG, ie.message)
      ie.printStackTrace()
    }

  }

  /**
   * Release and cleanup QREader.
   */
  fun releaseAndCleanup() {
    stop()
    if (cameraSource != null) {
      //release camera and barcode detector(will invoke inside) resources
      cameraSource!!.release()
      cameraSource = null
    }
  }

  private fun checkCameraPermission(context: Context): Boolean {
    val permission = Manifest.permission.CAMERA
    val res = context.checkCallingOrSelfPermission(permission)
    return res == PackageManager.PERMISSION_GRANTED
  }

  private fun hasCameraHardware(context: Context): Boolean {
    return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
  }

  private fun hasAutofocus(context: Context): Boolean {
    return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)
  }

  /**
   * The type Builder.
   */
  class Builder
  /**
   * Instantiates a new Builder.

   * @param context
   * *     the context
   * *
   * @param surfaceView
   * *     the surface view
   * *
   * @param qrBarCodeDataListener
   * *     the qr data listener
   */
  ( val context: Context,  val surfaceView: SurfaceView,
       val qrBarCodeDataListener: QrBarCodeListener) {
     var autofocusEnabled: Boolean = false
     var width: Int = 0
     var height: Int = 0
     var facing: Int = 0
     var barcodeDetector: BarcodeDetector? = null

    init {
      this.autofocusEnabled = true
      this.width = 800
      this.height = 800
      this.facing = BACK_CAM
    }

    /**
     * Enable autofocus builder.

     * @param autofocusEnabled
     * *     the autofocus enabled
     * *
     * @return the builder
     */
    fun enableAutofocus(autofocusEnabled: Boolean): Builder {
      this.autofocusEnabled = autofocusEnabled
      return this
    }

    /**
     * Width builder.

     * @param width
     * *     the width
     * *
     * @return the builder
     */
    fun width(width: Int): Builder {
      if (width != 0) {
        this.width = width
      }
      return this
    }

    /**
     * Height builder.

     * @param height
     * *     the height
     * *
     * @return the builder
     */
    fun height(height: Int): Builder {
      if (height != 0) {
        this.height = height
      }
      return this
    }

    /**
     * Facing builder.

     * @param facing
     * *     the facing
     * *
     * @return the builder
     */
    fun facing(facing: Int): Builder {
      this.facing = facing
      return this
    }

    /**
     * Build QREader

     * @return the QREader
     */
    fun build(): QrBarCodeBuilder {
      return QrBarCodeBuilder(this)
    }

    /**
     * Barcode detector.

     * @param barcodeDetector
     * *     the barcode detector
     */
    fun barcodeDetector(barcodeDetector: BarcodeDetector) {
      this.barcodeDetector = barcodeDetector
    }
  }

  companion object {

    /**
     * The constant FRONT_CAM.
     */
    val FRONT_CAM = CameraSource.CAMERA_FACING_FRONT
    /**
     * The constant BACK_CAM.
     */
    val BACK_CAM = CameraSource.CAMERA_FACING_BACK

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private fun removeOnGlobalLayoutListener(v: View,
        listener: ViewTreeObserver.OnGlobalLayoutListener) {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
        v.viewTreeObserver.removeGlobalOnLayoutListener(listener)
      } else {
        v.viewTreeObserver.removeOnGlobalLayoutListener(listener)
      }
    }
  }

   interface QrBarCodeListener {

     /**
      * On detected.

      * @param data
      * *     the data
      */
     // Called from not main thread. Be careful
     abstract fun onDetected(data: String)
   }
}


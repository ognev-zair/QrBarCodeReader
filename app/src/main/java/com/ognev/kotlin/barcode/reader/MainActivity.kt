package com.ognev.kotlin.barcode.reader

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.ognev.library.barcodereader.QrBarCodeBuilder
import com.ognev.library.barcodereader.QrBarCodeBuilder.QrBarCodeListener
//import kotlinx.android.synthetic.main.activity_main.camera_toggle
import kotlinx.android.synthetic.main.activity_main.qr_view
import kotlinx.android.synthetic.main.activity_main.result
import kotlinx.android.synthetic.main.activity_main.toggle_flash

//import com.sun.javafx.application.ParametersImpl.getParameters



class MainActivity : AppCompatActivity() {

  var qrBarcodeReader: QrBarCodeBuilder? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
//    EasyFlashlight.init(this)

    qrBarcodeReader = QrBarCodeBuilder.Builder(this, qr_view, object : QrBarCodeListener {
      override fun onDetected(data: String) {
        runOnUiThread { result.text = data }
      }
    } ).facing(QrBarCodeBuilder.BACK_CAM)
        .enableAutofocus(true)
        .height(qr_view.height)
        .width(qr_view.width).build()


    toggle_flash.setOnClickListener {
      if(!qrBarcodeReader!!.isFlashOn()) {
        toggle_flash.setColorFilter(Color.argb(255, 255, 255, 255))
      } else{
        toggle_flash.setColorFilter(Color.parseColor("#FFDA44"))
      }
      qrBarcodeReader!!.switchOnOffFlash() }

  }



  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
      grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    qrBarcodeReader!!.initAndStart(qr_view)
  }

  override fun onResume() {
    super.onResume()
    qrBarcodeReader!!.initAndStart(qr_view)
  }

  override fun onPause() {
    super.onPause()
    qrBarcodeReader!!.releaseAndCleanup()
  }
}

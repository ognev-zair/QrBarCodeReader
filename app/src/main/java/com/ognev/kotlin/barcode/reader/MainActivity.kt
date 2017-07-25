package com.ognev.kotlin.barcode.reader

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.ognev.library.barcodereader.QrBarCodeBuilder
import com.ognev.library.barcodereader.QrBarCodeBuilder.QrBarCodeListener
import kotlinx.android.synthetic.main.activity_main.result
import kotlinx.android.synthetic.main.activity_main.surface_view

class MainActivity : AppCompatActivity() {

  var qrBarcodeReader: QrBarCodeBuilder? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    qrBarcodeReader = QrBarCodeBuilder.Builder(this, surface_view, object : QrBarCodeListener {
      override fun onDetected(data: String) {
        runOnUiThread { result.text = data }
      }
    } ).facing(QrBarCodeBuilder.BACK_CAM)
        .enableAutofocus(true)
        .height(surface_view.height)
        .width(surface_view.width).build()

  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
      grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    qrBarcodeReader!!.initAndStart(surface_view)
  }

  override fun onResume() {
    super.onResume()
    qrBarcodeReader!!.initAndStart(surface_view)
  }

  override fun onPause() {
    super.onPause()
    qrBarcodeReader!!.releaseAndCleanup()
  }
}

package com.ognev.library.barcodereader

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.View
import android.view.animation.Animation
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.camera_view.view.line
import kotlinx.android.synthetic.main.camera_view.view.surface_view
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation


/**
 * Created by ognev on 7/25/17.
 */

class QrSurfaceView : FrameLayout {

//  private var cameraSurfaceView: SurfaceView? = null
//  private var line: View? = null

  constructor(context: Context) : super(context) {
    initView(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    initView(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr) {
    initView(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
      context, attrs, defStyleAttr, defStyleRes) {
    initView(context)
  }

  private fun initView(context: Context) {
    View.inflate(context, R.layout.camera_view, this)
    var animation = TranslateAnimation(
        TranslateAnimation.ABSOLUTE, 0f,
        TranslateAnimation.ABSOLUTE, 0f,
        TranslateAnimation.RELATIVE_TO_PARENT, 0f,
        TranslateAnimation.RELATIVE_TO_PARENT, 1.0f)
    animation.duration = 4900
    animation.repeatCount = -1
    animation.repeatMode = Animation.REVERSE
    animation.interpolator = LinearInterpolator()
    line.animation = animation
  }

}

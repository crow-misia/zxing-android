package io.github.crow_misia.zxing

import android.view.MotionEvent
import android.view.View
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfoUnavailableException
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.MeteringPoint
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

interface AutoFocusPreview {
    fun setUp()
}

class AutoFocusIntervalPreview(
    private val cameraControl: CameraControl,
    private val previewView: View,
    private val interval: Duration,
    private val meteringPoint: MeteringPoint,
    private val onError: (CameraInfoUnavailableException) -> Unit,
) : AutoFocusPreview {
    override fun setUp() {
        previewView.afterMeasured {
            try {
                val action = FocusMeteringAction.Builder(meteringPoint)
                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                    .build()
                cameraControl.startFocusAndMetering(action)
            } catch (e: CameraInfoUnavailableException) {
                onError(e)
            }
        }
    }
}

class AutoFocusTouchPreview(
    private val cameraControl: CameraControl,
    private val previewView: View,
    private val onError: (CameraInfoUnavailableException) -> Unit,
) : AutoFocusPreview {
    override fun setUp() {
        previewView.afterMeasured {
            previewView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> true
                    MotionEvent.ACTION_UP -> {
                        try {
                            val factory = SurfaceOrientedMeteringPointFactory(
                                previewView.width.toFloat(), previewView.height.toFloat()
                            )
                            val action = FocusMeteringAction.Builder(factory.createPoint(event.x, event.y))
                                    .disableAutoCancel()
                                    .build()
                            cameraControl.startFocusAndMetering(action)
                        } catch (e: CameraInfoUnavailableException) {
                            onError(e)
                        }
                        true
                    }
                    else -> false
                }
            }
        }
    }
}

fun CameraControl.autoFocusInterval(
    previewView: View,
    interval: Duration,
    meteringPoint: MeteringPoint,
    onError: (CameraInfoUnavailableException) -> Unit = { },
) {
    AutoFocusIntervalPreview(this, previewView, interval, meteringPoint, onError).setUp()
}

fun CameraControl.autoFocusTouch(
    previewView: View,
    onError: (CameraInfoUnavailableException) -> Unit = { },
) {
    AutoFocusTouchPreview(this, previewView, onError).setUp()
}

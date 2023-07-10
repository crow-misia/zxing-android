/**
 * Copyright (C) 2022 Zenichi Amano.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.crow_misia.zxing

import android.annotation.SuppressLint
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
    private val autoCancelDuration: Duration,
    private val meteringPoint: MeteringPoint,
    private val onError: (CameraInfoUnavailableException) -> Unit,
) : AutoFocusPreview {
    override fun setUp() {
        previewView.afterMeasured {
            try {
                val action = FocusMeteringAction.Builder(meteringPoint)
                    .setAutoCancelDuration(autoCancelDuration.inWholeSeconds, TimeUnit.SECONDS)
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
    @SuppressLint("ClickableViewAccessibility")
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
    autoCancelDuration: Duration,
    meteringPoint: MeteringPoint,
    onError: (CameraInfoUnavailableException) -> Unit = { },
) {
    AutoFocusIntervalPreview(this, previewView, autoCancelDuration, meteringPoint, onError).setUp()
}

fun CameraControl.autoFocusTouch(
    previewView: View,
    onError: (CameraInfoUnavailableException) -> Unit = { },
) {
    AutoFocusTouchPreview(this, previewView, onError).setUp()
}

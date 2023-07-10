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

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.Result
import java.nio.BufferUnderflowException
import kotlin.concurrent.getOrSet

typealias BarcodeDetectListener = (image: ImageProxy, results: Array<Result>) -> Unit

class BarcodeAnalyzer(
    private val decoderFactory: DecoderFactory,
    listener: BarcodeDetectListener? = null,
) : ImageAnalysis.Analyzer {
    private val decoder = ThreadLocal<Decoder>()
    private val listeners = linkedSetOf<BarcodeDetectListener>().apply { listener?.let { add(it) } }
    var enabled: Boolean = true

    fun addListener(listener: BarcodeDetectListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: BarcodeDetectListener) {
        listeners.remove(listener)
    }

    override fun analyze(image: ImageProxy) {
        if (!enabled || listeners.isEmpty()) {
            image.close()
            return
        }

        try {
            val plane = image.planes[0]
            val source = PlanarLuminanceSource(
                buffer = plane.buffer,
                width = image.width,
                height = image.height,
                dataWidth = plane.rowStride,
                dataHeight = image.height,
            )
            val decoder = decoder.getOrSet { decoderFactory.createDecoder(emptyMap()) }
            val results = decoder.decode(source)
            listeners.forEach {
                it(image, results)
            }
        } catch (e: BufferUnderflowException) {
            // ignore.
        } finally {
            image.close()
        }
    }
}

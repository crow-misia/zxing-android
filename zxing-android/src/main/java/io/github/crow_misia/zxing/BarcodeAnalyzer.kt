package io.github.crow_misia.zxing

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.Result
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

        image.close()
    }
}

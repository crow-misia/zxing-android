package io.crow.misia.crow_misia.zxing.analysis

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.Reader
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.MultipleBarcodeReader
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import io.github.crow_misia.libyuv.ext.ImageProxyExt.toI400Buffer
import io.github.crow_misia.zxing.analysis.ReaderWrapper
import java.util.*
import kotlin.concurrent.getOrSet

typealias BarcodeDetectListener = (image: ImageProxy, results: Array<Result>) -> Unit

class BarcodeAnalyzer(
    readerProvider: () -> Reader,
    hints: Map<DecodeHintType, Any>? = null,
    listener: BarcodeDetectListener? = null,
) : ImageAnalysis.Analyzer {
    private val wrappedReader = ThreadLocal.withInitial { ReaderWrapper.wrap(readerProvider(), hints) }
    private val listeners = linkedSetOf<BarcodeDetectListener>().apply { listener?.let { add(it) } }

    fun addListener(listener: BarcodeDetectListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: BarcodeDetectListener) {
        listeners.remove(listener)
    }

    override fun analyze(image: ImageProxy) {
        if (listeners.isEmpty()) {
            image.close()
            return
        }

        image.toI400Buffer().use {
            val luminanceSource = PlanarLuminanceSource(buffer = it)
            val bitmap = luminanceSource.toBitmap()
            val results = bitmap.decode()
            listeners.forEach {
                it(image, results)
            }
        }

        image.close()
    }

    private fun LuminanceSource.toBitmap(): BinaryBitmap {
        return BinaryBitmap(HybridBinarizer(this))
    }

    private fun BinaryBitmap.decode(): Array<Result> {
        val reader = wrappedReader.get()!!
        return try {
            reader.decode(this)
        } catch (e: Exception) {
            emptyArray()
        } finally {
            reader.reset()
        }
    }
}

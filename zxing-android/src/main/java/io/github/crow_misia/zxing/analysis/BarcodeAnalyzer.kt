package io.crow.misia.crow_misia.zxing.analysis

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
import java.util.*

typealias BarcodeDetectListener = (image: ImageProxy, results: Array<Result>) -> Unit

class BarcodeAnalyzer(
    private val reader: Reader,
    private val hints: Map<DecodeHintType, Any>? = null,
    listener: BarcodeDetectListener? = null,
) : ImageAnalysis.Analyzer {
    private val listeners = ArrayList<BarcodeDetectListener>().apply { listener?.let { add(it) } }
    private val frameRateWindow = 8
    private val frameTimestamps = ArrayDeque<Long>(5)

    override fun analyze(image: ImageProxy) {
        if (listeners.isEmpty()) {
            image.close()
            return
        }

        image.toI400Buffer().use {
            val luminanceSource = PlanarLuminanceSource(buffer = it)
            val bitmap = luminanceSource.toBitmap()

            bitmap.decode()?.also { results ->
                listeners.forEach {
                    it(image, results)
                }
            }
        }

        image.close()
    }

    private fun LuminanceSource.toBitmap(): BinaryBitmap {
        return BinaryBitmap(HybridBinarizer(this))
    }

    private fun BinaryBitmap.decode(): Array<Result>? {
        return try {
            if (reader is MultipleBarcodeReader) {
                reader.decodeMultiple(this)
            } else {
                val result = if (reader is MultiFormatReader) {
                    reader.decodeWithState(this)
                } else {
                    reader.decode(this)
                }
                result?.let { arrayOf(it) }
            }
        } catch (e: Exception) {
            null
        } finally {
            reader.reset()
        }
    }
}
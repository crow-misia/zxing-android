package io.crow.misia.crow_misia.zxing.analysis

import com.google.zxing.LuminanceSource
import com.google.zxing.PlanarYUVLuminanceSource
import io.github.crow_misia.libyuv.I400Buffer
import kotlin.concurrent.getOrSet

class PlanarLuminanceSource(
    buffer: I400Buffer,
) : LuminanceSource(buffer.width, buffer.height) {
    companion object {
        private val cachedBuffer = ThreadLocal<ByteArray>()
    }

    private val dataWidth = buffer.planes[0].rowStride
    private val bufferSize = dataWidth * height
    private val buffer = cachedBuffer.getOrSet {
        ByteArray(bufferSize).also { newBuffer ->
            cachedBuffer.set(newBuffer)
        }
    }.let {
        if (it.size < bufferSize) {
            ByteArray(bufferSize).also { newBuffer ->
                cachedBuffer.set(newBuffer)
            }
        } else {
            it
        }
    }

    init {
        buffer.asByteArray(this.buffer)
    }

    override fun getRow(y: Int, row: ByteArray?): ByteArray {
        require(!(y < 0 || y >= height)) { "Requested row is outside the image: $y" }
        val width = dataWidth
        val result = if (row == null || row.size < width) {
            ByteArray(width)
        } else {
            row
        }

        val offset = y * width
        System.arraycopy(buffer, offset, result, 0, width)
        return result
    }

    override fun getMatrix(): ByteArray {
        return buffer
    }

    override fun isCropSupported(): Boolean = true

    override fun crop(left: Int, top: Int, width: Int, height: Int): LuminanceSource {
        return PlanarYUVLuminanceSource(buffer, width, height, left, top, width, height, false)
    }
}
package io.github.crow_misia.zxing.analysis

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.Reader
import com.google.zxing.Result
import com.google.zxing.multi.MultipleBarcodeReader

sealed interface ReaderWrapper {
    fun decode(image: BinaryBitmap): Array<Result>
    fun reset()

    companion object {
        fun wrap(reader: Reader, hints: Map<DecodeHintType, Any>?): ReaderWrapper {
            val wrapedReader = when (reader) {
                is MultipleBarcodeReader -> MultipleBarcodeReaderWrapper(reader, hints)
                is MultiFormatReader -> MultiFormatReaderWrapper(reader, hints)
                else -> GeneralReaderWrapper(reader, hints)
            }
            return wrapedReader
        }
    }
}

class MultipleBarcodeReaderWrapper(
    private val reader: MultipleBarcodeReader,
    private val hints: Map<DecodeHintType, Any>?,
) : ReaderWrapper {
    override fun decode(image: BinaryBitmap): Array<Result> {
        return reader.decodeMultiple(image, hints)
    }
    override fun reset() = Unit
}

class MultiFormatReaderWrapper(
    private val reader: MultiFormatReader,
    hints: Map<DecodeHintType, Any>?,
) : ReaderWrapper {
    init {
        reader.setHints(hints)
    }
    override fun decode(image: BinaryBitmap): Array<Result> {
        val result = reader.decodeWithState(image)
        return result?.let { arrayOf(it) } ?: emptyArray()
    }
    override fun reset() {
        reader.reset()
    }
}

class GeneralReaderWrapper(
    private val reader: Reader,
    private val hints: Map<DecodeHintType, Any>?,
) : ReaderWrapper {
    override fun decode(image: BinaryBitmap): Array<Result> {
        val result = reader.decode(image, hints)
        return result?.let { arrayOf(it) } ?: emptyArray()
    }
    override fun reset() {
        reader.reset()
    }
}

package io.github.crow_misia.zxing

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.Reader
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer

open class Decoder(
    reader: Reader,
    hints: Map<DecodeHintType, Any>,
) {
    private val reader = ReaderWrapper.wrap(reader, hints)

    fun decode(source: LuminanceSource): Array<Result> {
        return decode(toBitmap(source))
    }

    protected open fun toBitmap(source: LuminanceSource): BinaryBitmap {
        return BinaryBitmap(HybridBinarizer(source))
    }

    protected open fun decode(bitmap: BinaryBitmap): Array<Result> {
        return try {
            reader.decode(bitmap)
        } catch (e: Throwable) {
            emptyArray()
        } finally {
            reader.reset()
        }
    }
}

class InvertedDecoder(
    reader: Reader,
    hints: Map<DecodeHintType, Any>,
) : Decoder(reader, hints) {
    override fun toBitmap(source: LuminanceSource): BinaryBitmap {
        return super.toBitmap(source.invert())
    }
}

class MixedDecoder(
    reader: Reader,
    hints: Map<DecodeHintType, Any>,
) : Decoder(reader, hints) {
    private var isInverted = true
    override fun toBitmap(source: LuminanceSource): BinaryBitmap {
        return if (isInverted) {
            isInverted = false
            super.toBitmap(source.invert())
        } else {
            isInverted = true
            super.toBitmap(source)
        }
    }
}
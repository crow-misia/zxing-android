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
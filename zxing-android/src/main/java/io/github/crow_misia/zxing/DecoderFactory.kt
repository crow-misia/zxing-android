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

import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.Reader
import java.nio.charset.Charset
import java.util.*

interface DecoderFactory {
    fun createDecoder(baseHints: Map<DecodeHintType, Any>): Decoder
}

open class DefaultDecoderFactory @JvmOverloads constructor(
    private val readerProvider: () -> Reader,
    private val decodeFormats: List<BarcodeFormat> = emptyList(),
    private val hints: Map<DecodeHintType, Any> = emptyMap(),
    private val characterSet: Charset = Charsets.UTF_8,
    private val scanType: ScanType = ScanType.NORMAL,
) : DecoderFactory {
    override fun createDecoder(baseHints: Map<DecodeHintType, Any>): Decoder {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java)

        hints.putAll(baseHints)

        if (this.hints.isNotEmpty()) {
            hints.putAll(this.hints)
        }

        if (this.decodeFormats.isNotEmpty()) {
            hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
        }

        hints[DecodeHintType.CHARACTER_SET] = characterSet.toString()

        val reader = readerProvider()

        return when (scanType) {
            ScanType.INVERTED -> InvertedDecoder(reader, hints)
            ScanType.MIXED -> MixedDecoder(reader, hints)
            else -> Decoder(reader, hints)
        }
    }
}

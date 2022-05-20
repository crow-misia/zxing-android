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
            hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats)
        }

        hints.put(DecodeHintType.CHARACTER_SET, characterSet.toString())

        val reader = readerProvider()

        return when (scanType) {
            ScanType.INVERTED -> InvertedDecoder(reader, hints)
            ScanType.MIXED -> MixedDecoder(reader, hints)
            else -> Decoder(reader, hints)
        }
    }
}
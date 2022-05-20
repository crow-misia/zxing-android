package io.github.crow_misia.zxing

enum class ScanType(val value: Int) {
    NORMAL(0),
    INVERTED(1),
    MIXED(2)
    ;
    companion object {
        private val values = values().associateBy { it.value }

        @JvmStatic
        fun of(value: Int) = values[value] ?: NORMAL
    }
}
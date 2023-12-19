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

/*
 * UniThen
 * Copyright (C) 2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with UniNow GmbH, the provider/developer of the booking system.
 */

package de.bixilon.unithen.storage.sql

import kotlinx.coroutines.runBlocking
import unithen.common.generated.resources.Res

object SqlUtil {

    fun load(path: String): String {
        return runBlocking { Res.readBytes("files/sql/$path.sql").decodeToString() }
    }

    fun split(raw: String): List<String> {
        val statements: MutableList<String> = mutableListOf()
        val builder = StringBuilder()
        var begin = false

        fun append(data: String) {
            if (builder.isNotEmpty()) {
                builder.appendLine()
            }
            builder.append(data)
        }

        fun push(data: String) {
            append(data)
            statements += builder.toString()
            builder.clear()
        }

        for (line in raw.lines()) {
            val trimmed = line.trim()
            if (trimmed.isBlank()) continue
            if (trimmed.startsWith("--")) continue


            if (trimmed.startsWith("BEGIN")) {
                require(!begin) { "Nested BEGIN!" }
                begin = true
                append(trimmed)
                continue
            }

            if (trimmed.startsWith("END")) {
                require(begin) { "END without BEGIN!" }
                begin = false
            }

            if (begin) {
                append(trimmed)
                continue
            }

            if (";" in trimmed) {
                val split = trimmed.split(";").filter { it.isNotBlank() }

                push(split.getOrNull(0)?.trim() ?: "")

                for (middle in 1 until split.size - 1) {
                    push(split[middle].trim())
                }

                if (split.size > 1) {
                    val end = split.last().trim()

                    if (trimmed.endsWith(";")) {
                        push(end)
                    } else {
                        append(end)
                    }
                }
            } else {
                append(trimmed)
            }
        }

        require(!begin) { "BEGIN without END" }
        require(builder.isBlank()) { "Trailing data: $builder" }

        return statements
    }
}

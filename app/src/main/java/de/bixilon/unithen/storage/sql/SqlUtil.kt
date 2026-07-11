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

import de.bixilon.kutil.stream.InputStreamUtil.readAsString

object SqlUtil {

    fun split(path: String): List<String> {
        val raw = SqlUtil::class.java.getResourceAsStream("/sql/$path.sql")!!.readAsString()

        val statements: MutableList<String> = mutableListOf()
        val builder = StringBuilder()
        var begin = false

        raw.lines().forEach { line ->
            val trimmed = line.trim()

            if (trimmed.startsWith("BEGIN")) {
                begin = true
            }

            builder.append(line).append("\n")

            if (begin) {
                if (trimmed.startsWith("END")) {
                    begin = false
                    statements.add(builder.toString().trim())
                    builder.clear()
                }
            } else if (trimmed.endsWith(";")) {
                statements.add(builder.toString().removeSuffix(";"))
                builder.clear()
            }
        }

        return statements
    }
}

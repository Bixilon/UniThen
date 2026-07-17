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

package de.bixilon.unithen.ui.main.checkin.scan

import de.bixilon.unithen.storage.types.User

object Contributors {
    val MAJOR_CONTRIBUTORS = mutableMapOf(
        0x54550CBADB5BC304 to "moritz",
    )

    fun User.isMajorContributor(): Boolean {
        // Well, not the best, but not revealing my user id :)
        // This is just an e*as*ter eg*g, nothing special. Purely visual.
        val hash = uuid.toLongs { a, b -> a xor b } and 0xFB.inv()

        val name = MAJOR_CONTRIBUTORS[hash] ?: return false

        return firstname.lowercase().trim() == name
    }
}

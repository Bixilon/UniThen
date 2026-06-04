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

package de.bixilon.unithen.util

import java.net.URI

object KUtil {

    @Deprecated("kutil 1.31")
    operator fun URI.div(path: String): URI = this.with(path = (this.path?.trimEnd('/') ?: "") + '/' + path.trimStart('/'))

    @Deprecated("kutil 1.31")
    fun URI.with(scheme: String? = this.scheme, userInfo: String? = this.userInfo, host: String? = this.host, port: Int = this.port, path: String? = this.path, query: String? = this.query, fragment: String? = this.fragment): URI {
        return URI(scheme, userInfo, host, port, path, query, fragment)
    }


    @Deprecated("kutil 1.31")
    inline fun <I> I.applyIf(enabled: Boolean, block: I.() -> I) = if (enabled) block.invoke(this) else this
}

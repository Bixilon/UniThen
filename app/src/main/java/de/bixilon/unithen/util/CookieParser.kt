package de.bixilon.unithen.util

import de.bixilon.kutil.string.WhitespaceUtil.removeTrailingWhitespaces

object CookieParser {


    fun parse(cookies: String): Map<String, String> {
        if (cookies.isBlank()) return emptyMap()
        return cookies.split(";").associate { it.removeTrailingWhitespaces().split("=", limit = 2).let { Pair(it[0].lowercase(), it[1]) } }
    }
}

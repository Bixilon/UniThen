package de.bixilon.unithen.util

import junit.framework.TestCase.assertEquals
import org.junit.Test

class CookieParserTest {

    @Test
    fun `no cookies`() {
        val cookies = CookieParser.parse("")
        assertEquals(cookies, emptyMap<String, String>())
    }

    @Test
    fun `single cookie`() {
        val cookies = CookieParser.parse("name=value")
        assertEquals(cookies, mapOf("name" to "value"))
    }

    @Test
    fun `multiple cookie`() {
        val cookies = CookieParser.parse("name=value; second=true")
        assertEquals(cookies, mapOf("name" to "value", "second" to "true"))
    }

    @Test
    fun `case insensitive cookie`() {
        val cookies = CookieParser.parse("nAme=value; secOnd=True")
        assertEquals(cookies, mapOf("name" to "value", "second" to "True"))
    }
}

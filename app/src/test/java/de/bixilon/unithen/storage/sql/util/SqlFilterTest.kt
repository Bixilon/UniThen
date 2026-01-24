package de.bixilon.unithen.storage.sql.util

import junit.framework.TestCase.assertEquals
import org.junit.Test

class SqlFilterTest {

    @Test
    fun `AND empty filter`() {
        val filter = SqlFilter.and()
        val expected = SqlFilter("", listOf())

        assertEquals(filter, expected)
    }

    @Test
    fun `AND single filter`() {
        val filter = SqlFilter.and("abc" to "def")
        val expected = SqlFilter("(abc=?)", listOf("def"))

        assertEquals(filter, expected)
    }

    @Test
    fun `AND multiple filters`() {
        val filter = SqlFilter.and("abc" to "def", "xyz" to null, "ush" to 1)
        val expected = SqlFilter("(abc=? AND ush=?)", listOf("def", "1"))

        assertEquals(filter, expected)
    }

    @Test
    fun `OR multiple filters`() {
        val filter = SqlFilter.or("abc" to "def", "xyz" to null, "ush" to 1)
        val expected = SqlFilter("(abc=? OR ush=?)", listOf("def", "1"))

        assertEquals(filter, expected)
    }
}

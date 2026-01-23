package de.bixilon.unithen.graphql.query

import de.bixilon.unithen.api.graphql.query.QueryLoader
import junit.framework.TestCase.assertTrue
import org.junit.Test

class QueryLoaderTest {

    @Test
    fun `load and cache coruses query`() {
        val name = "courses"
        val query = QueryLoader[name]

        assertTrue(query.query.startsWith("query"))
    }
}

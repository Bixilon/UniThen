package de.bixilon.unithen.api

import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import junit.framework.TestCase.assertEquals


class UniNowUtilTest {

    fun `extract userid from frontpage`() {
        val html = UniNowUtilTest::class.java.getResourceAsStream("/http/front_page.html")!!.readAsString()

        val userId = UniNowUtil.extractUserId(html)
        assertEquals(userId, "10000000-0003-0000-0000-000000000001".toUUID())
    }
}

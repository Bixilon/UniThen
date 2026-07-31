package de.bixilon.unithen.ui.components.qr

import androidx.compose.ui.graphics.ImageBitmap
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QrCodeTest {

    private operator fun ImageBitmap.get(x: Int, y: Int): Boolean {
        val buffer = IntArray(1)
        readPixels(buffer, x, y, 1, 1)

        return buffer[0] != 0x00
    }

    @Test
    fun `create hello world qr code`() {
        val bitmap = encodeQr("hello world")


        assertTrue(bitmap[0, 0])
        assertTrue(bitmap[1, 0])
        assertFalse(bitmap[1, 1])
    }
}

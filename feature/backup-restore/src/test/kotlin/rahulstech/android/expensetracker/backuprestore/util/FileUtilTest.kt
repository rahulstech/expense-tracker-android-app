package rahulstech.android.expensetracker.backuprestore.util

import org.junit.Assert.assertArrayEquals
import org.junit.Test
import java.io.ByteArrayOutputStream

class FileUtilTest {

    @Test
    fun tetCopy() {
        val expected = "expense tracker".toByteArray()
        val src = expected.inputStream()
        val dest = ByteArrayOutputStream()

        FileUtil.copy(src, dest)
        val actual = dest.toByteArray()

        assertArrayEquals(expected, actual)
    }
}
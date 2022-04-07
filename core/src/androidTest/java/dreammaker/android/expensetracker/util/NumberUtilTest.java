package dreammaker.android.expensetracker.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NumberUtilTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void format() {
        String numstr = NumberUtil.format(12456.26,new Locale("bn"));
        assertEquals("১২,৪৫৬.২৬",numstr);
    }
}
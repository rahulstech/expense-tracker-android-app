package dreammaker.android.expensetracker.util;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class NumberToWordTest {

    Context context;

    @Before
    public void setUp() throws Exception {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void getCachedInstance() {

        Locale english = new Locale("en");
        Locale bengali = new Locale("bn");

        context.getResources().getConfiguration().setLocale(english);
        NumberToWord ntw1 = NumberToWord.getCachedInstance(context);
        NumberToWord ntw2 = NumberToWord.getCachedInstance(context);

        assertEquals("locale unchanged",ntw1,ntw2);

        context.getResources().getConfiguration().setLocale(bengali);
        NumberToWord ntw3 = NumberToWord.getCachedInstance(context);

        assertNotEquals("locale changed",ntw1,ntw3);
    }

    @Test
    public void newInstance() {
        Locale target = new Locale("bn");
        Locale actual = context.getResources().getConfiguration().locale;

        NumberToWord ntw = NumberToWord.newInstance(context,target);

        assertEquals("NumberToWord.getLocale",target,ntw.getLocale());

        assertEquals("app locale restore",actual,context.getResources().getConfiguration().locale);
    }

    @Test
    public void numberToWords_Locale_bn() {
        NumberToWord ntw = NumberToWord.newInstance(context, new Locale("bn"));
        String[] words;

        words = ntw.numberToWords(15627894);
        assertEquals("integer only", "পনেরো মিলিয়ন ছয় লক্ষ সাতাশ হাজার আট শত চুরানব্বই", words[0]);

        words = ntw.numberToWords(.56);
        assertEquals("decimal only", "ছাপান্ন", words[1]);
    }


}
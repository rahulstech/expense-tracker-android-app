package dreammaker.android.expensetracker.util;

import android.content.Context;
import android.content.res.Resources;

import java.util.Locale;

import androidx.annotation.NonNull;
import dreammaker.android.expensetracker.R;

public class NumberToWord {
	private static NumberToWord instance = null;
	
	private final Locale locale;

	private final String[] BASE_NUMBER_WORDS;
	private final String HUNDRED;
	private final String THOUSAND;
	private final String LAKH;
	private final String MILLION;
	private final String HUNDREDS;
	private final String THOUSANDS;
	private final String LAKHS;
	private final String MILLIONS;

	
	private NumberToWord(Resources res, Locale target){
		this.locale = target;
		// change app current locale to target locale
		// to get string resources for target locale
		Locale actual = res.getConfiguration().locale;
		res.getConfiguration().locale = target;
		res.updateConfiguration(res.getConfiguration(),res.getDisplayMetrics());
		BASE_NUMBER_WORDS = res.getStringArray(R.array.base_numbers_words);
		HUNDRED = res.getString(R.string.hundred);
		THOUSAND = res.getString(R.string.thousand);
		LAKH = res.getString(R.string.lakh);
		MILLION = res.getString(R.string.million);
		HUNDREDS = res.getString(R.string.hundreds);
		THOUSANDS = res.getString(R.string.thousands);
		LAKHS = res.getString(R.string.lakhs);
		MILLIONS = res.getString(R.string.millions);
		// now restore app locale
		res.getConfiguration().locale = actual;
		res.updateConfiguration(res.getConfiguration(),res.getDisplayMetrics());
	}
	
	public Locale getLocale(){
		return locale;
	}

	/**
	 * The instance returned by this method is cached until app locale is changed
	 *
	 * @param ctx application context
	 * @return a new or cached instance of {@link NumberToWord}
	 */
	public static NumberToWord getCachedInstance(Context ctx){
		Resources res = ctx.getApplicationContext().getResources();
		Locale locale = res.getConfiguration().locale;
		if (null == instance || !locale.equals(instance.locale)){
			instance = new NumberToWord(res,locale);
		}
		return instance;
	}

	/**
	 * Returns a new instance of this class for the target locale. The instance
	 * returned is not cached and also does not replace the cached instance.
	 *
	 * @param context the app context
	 * @param target the target locale
	 * @return a new instance of {@link NumberToWord} for target locale
	 */
	public static NumberToWord newInstance(@NonNull Context context, @NonNull Locale target) {
		return new NumberToWord(context.getApplicationContext().getResources(),target);
	}

	/**
	 * Returns the verbose representation of the absolute value of given number. Input may be integral
	 * or decimal. Returned array contains the integral and decimal parts separately as
	 * index 0 -> verbose integral part
	 * index 1 -> verbose decimal part
	 * For example:
	 * 144.56 = {"one hundred forty four", "fifty six"}
	 * Note: the verbose representation of the decimal part is up to 2 decimal places and rounded off
	 *
	 * @param n input number
	 * @return the verbose representation of input number
	 */
	public String[] numberToWords(double n){
		n = Math.abs(n);
		long[] parts = splitByDecimal(n);
		String[] words = new String[]{
			convertUpToNineDigitNumber(parts[0]),
			convertUpToNineDigitNumber(parts[1])
		};
		return words;
	}

	private String convertUpToNineDigitNumber(long n){
		long x = n/1000000;
		String words = "";
		if (x > 0) words = appendQuantityQualifier( convertUpToNineDigitNumber(x),MILLION,MILLIONS,x==1);
		words = join(words,convertUpToSixDigitNumber(n%1000000));
		return words;
	}
	
	private String convertUpToSixDigitNumber(long n){
		long x = n/100000;
		String words = "";
		if (x > 0) words = appendQuantityQualifier(convertUpToTwoDigitNumber(x),LAKH,LAKHS,x==1);
		words = join(words,convertUpToFiveDigitNumber(n%100000));
		return words;
	}
	
	private String convertUpToFiveDigitNumber(long n){
		long x = n/1000;
		String words = "";
		if (x > 0) words = appendQuantityQualifier(convertUpToTwoDigitNumber(x),THOUSAND,THOUSANDS,x==1);
		words = join(words,convertUpToThreeDigitNumber(n%1000));
		return words;
	}
	
	private String convertUpToThreeDigitNumber(long n){
		long x = n/100;
		String words = "";
		if (x > 0) words = appendQuantityQualifier(convertUpToTwoDigitNumber(x),HUNDRED,HUNDREDS,x==1);
		words = join(words,convertUpToTwoDigitNumber(n%100));
		return words;
	}
	
	private String convertUpToTwoDigitNumber(long n){
		return BASE_NUMBER_WORDS[(int) n];
	}
	
	private long[] splitByDecimal(double n){
		long i = (long) n;
		long d = Math.round(n*100)%100;
		return new long[]{i,d};
	}

	private String appendQuantityQualifier(String quantity, String singularSuffix, String pluralSuffix, boolean isSingular) {
		return quantity+" "+(isSingular ? singularSuffix : pluralSuffix);
	}

	private String join(String prefix, String suffix) {
		if (prefix.isEmpty()) return suffix;
		else if (suffix.isEmpty()) return prefix;
		else return prefix+" "+suffix;
	}
}

package dreammaker.android.expensetracker.database.type;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class Currency {

    public static final Currency ZERO = new Currency(BigDecimal.ZERO);

    BigDecimal mValue;

    Currency(@NonNull BigDecimal value) {
        this.mValue = value.setScale(2, RoundingMode.DOWN);
    }

    @NonNull
    public static Currency valueOf(double value) {
        return valueOf(BigDecimal.valueOf(value));
    }

    @NonNull
    public static Currency valueOf(String value) {
        if (TextUtils.isEmpty(value)) {
            throw new IllegalArgumentException("can not convert an empty string to Currency");
        }
        return valueOf(new BigDecimal(value));
    }

    @NonNull
    public static Currency valueOf(@NonNull BigDecimal value) {
        if (isValueZero(value)) return ZERO;
        return new Currency(value);
    }

    @NonNull
    public BigDecimal getValue() {
        return mValue;
    }

    @NonNull
    public Currency add(@NonNull Currency other) {
        BigDecimal result = mValue.add(other.mValue);
        if (isValueZero(result)) return ZERO;
        return new Currency(result);
    }

    @NonNull
    public Currency subtract(@NonNull Currency other) {
        BigDecimal result = mValue.subtract(other.mValue);
        if (isValueZero(result)) return ZERO;
        return new Currency(result);
    }

    @NonNull
    public Currency abs() {
        if (isValueZero(mValue)) return ZERO;
        return new Currency(mValue.abs());
    }

    @NonNull
    public Currency negate() {
        if (isValueZero(mValue)) return ZERO;
        return new Currency(mValue.negate());
    }

    public boolean isNegative() {
        return compareTo(ZERO) < 0;
    }

    public int compareTo(@NonNull Currency other) {
        return mValue.compareTo(other.mValue);
    }

    @NonNull
    public Currency copy() {
        BigDecimal copy = new BigDecimal(mValue.toString());
        return new Currency(copy);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Currency)) return false;
        Currency currency = (Currency) o;
        return Objects.equals(mValue, currency.mValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mValue);
    }

    @NonNull
    @Override
    public String toString() {
        return mValue.toPlainString();
    }

    static boolean isValueZero(@NonNull BigDecimal value) {
        return BigDecimal.ZERO.compareTo(value) == 0;
    }
}

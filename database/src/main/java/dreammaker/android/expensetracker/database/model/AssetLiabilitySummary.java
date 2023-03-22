package dreammaker.android.expensetracker.database.model;

import java.math.BigDecimal;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.DatabaseView;
import androidx.room.TypeConverters;
import dreammaker.android.expensetracker.database.Converters;

public class AssetLiabilitySummary {

    @TypeConverters(Converters.class)
    private BigDecimal totalPositiveBalance;

    @TypeConverters(Converters.class)
    private BigDecimal totalNegativeBalance;

    @TypeConverters(Converters.class)
    private BigDecimal totalPositiveDue;

    @TypeConverters(Converters.class)
    private BigDecimal totalPositiveBorrow;

    @TypeConverters(Converters.class)
    private BigDecimal totalNegativeDue;

    @TypeConverters(Converters.class)
    private BigDecimal totalNegativeBorrow;


    public AssetLiabilitySummary() {}

    public BigDecimal getTotalPositiveBalance() {
        return totalPositiveBalance;
    }

    public void setTotalPositiveBalance(BigDecimal totalPositiveBalance) {
        this.totalPositiveBalance = totalPositiveBalance;
    }

    public BigDecimal getTotalNegativeBalance() {
        return null == totalNegativeBalance ? BigDecimal.ZERO : totalNegativeBalance;
    }

    public void setTotalNegativeBalance(BigDecimal totalNegativeBalance) {
        this.totalNegativeBalance = totalNegativeBalance;
    }

    public BigDecimal getTotalPositiveDue() {
        return null == totalPositiveDue ? BigDecimal.ZERO : totalPositiveDue;
    }

    public void setTotalPositiveDue(BigDecimal totalPositiveDue) {
        this.totalPositiveDue = totalPositiveDue;
    }

    public BigDecimal getTotalPositiveBorrow() {
        return null == totalPositiveBorrow ? BigDecimal.ZERO : totalPositiveBorrow;
    }

    public void setTotalPositiveBorrow(BigDecimal totalPositiveBorrow) {
        this.totalPositiveBorrow = totalPositiveBorrow;
    }

    public BigDecimal getTotalNegativeDue() {
        return null == totalNegativeDue ? BigDecimal.ZERO : totalNegativeDue;
    }

    public void setTotalNegativeDue(BigDecimal totalNegativeDue) {
        this.totalNegativeDue = totalNegativeDue;
    }

    public BigDecimal getTotalNegativeBorrow() {
        return null == totalNegativeBorrow ? BigDecimal.ZERO : totalNegativeBorrow;
    }

    public void setTotalNegativeBorrow(BigDecimal totalNegativeBorrow) {
        this.totalNegativeBorrow = totalNegativeBorrow;
    }

    @NonNull
    public BigDecimal getTotalDue() {
        return getTotalPositiveDue().add(getTotalNegativeBorrow());
    }

    @NonNull
    public BigDecimal getTotalBorrow() {
        return getTotalPositiveBorrow().add(getTotalNegativeDue());
    }

    @NonNull
    public BigDecimal getTotalAsset() {
        BigDecimal asset = getTotalPositiveBalance().add(getTotalPositiveDue()).add(getTotalNegativeBorrow());
        return asset;
    }

    @NonNull
    public BigDecimal getTotalLiability() {
        BigDecimal liability = getTotalNegativeBalance().add(getTotalNegativeDue()).add(getTotalPositiveBorrow());
        return liability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssetLiabilitySummary)) return false;
        AssetLiabilitySummary that = (AssetLiabilitySummary) o;
        return Objects.equals(totalPositiveBalance, that.totalPositiveBalance) && Objects.equals(totalNegativeBalance, that.totalNegativeBalance) && Objects.equals(totalPositiveDue, that.totalPositiveDue) && Objects.equals(totalPositiveBorrow, that.totalPositiveBorrow) && Objects.equals(totalNegativeDue, that.totalNegativeDue) && Objects.equals(totalNegativeBorrow, that.totalNegativeBorrow);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalPositiveBalance, totalNegativeBalance, totalPositiveDue, totalPositiveBorrow, totalNegativeDue, totalNegativeBorrow);
    }

    @Override
    public String toString() {
        return "AssetLiabilitySummary{" +
                "totalPositiveBalance=" + totalPositiveBalance +
                ", totalNegativeBalance=" + totalNegativeBalance +
                ", totalPositiveDue=" + totalPositiveDue +
                ", totalPositiveBorrow=" + totalPositiveBorrow +
                ", totalNegativeDue=" + totalNegativeDue +
                ", totalNegativeBorrow=" + totalNegativeBorrow +
                '}';
    }
}

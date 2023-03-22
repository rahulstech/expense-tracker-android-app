package dreammaker.android.expensetracker.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import dreammaker.android.expensetracker.database.model.AssetLiabilitySummary;

@Dao
public interface AnalyticsDao {

    @Query("SELECT * FROM (SELECT SUM(CASE WHEN CAST(`balance` AS REAL) >= 0 THEN `balance` ELSE 0 END) AS `totalPositiveBalance`, " +
            "SUM(CASE WHEN CAST(`balance` AS REAL) < 0 THEN ABS(`balance`) ELSE 0 END) AS `totalNegativeBalance` FROM `accounts`), " +
            "(SELECT SUM(CASE WHEN CAST(`amountDue` AS REAL) >= 0 THEN `amountDue` ELSE 0 END) AS `totalPositiveDue`, " +
            " SUM( CASE WHEN CAST(`amountBorrow` AS REAL) < 0 THEN ABS(`amountBorrow`) ELSE 0 END ) AS `totalNegativeBorrow`, " +
            "SUM(CASE WHEN CAST(`amountDue` AS REAL) < 0 THEN ABS(`amountDue`) ELSE 0 END) AS `totalNegativeDue`, " +
            "SUM(CASE WHEN CAST(`amountBorrow` AS REAL) >= 0 THEN `amountBorrow` ELSE 0 END ) AS `totalPositiveBorrow`" +
            " FROM `people`) ")
    LiveData<AssetLiabilitySummary> getTotalAssetLiability();
}

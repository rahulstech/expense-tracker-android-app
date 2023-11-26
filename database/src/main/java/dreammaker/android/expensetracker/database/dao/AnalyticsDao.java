package dreammaker.android.expensetracker.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import dreammaker.android.expensetracker.database.view.AssetLiabilitySummary;

@Dao
@SuppressWarnings("unused")
public interface AnalyticsDao {

    @Query("SELECT * FROM `asset_liability_summary_view`")
    LiveData<AssetLiabilitySummary> getTotalAssetLiability();
}

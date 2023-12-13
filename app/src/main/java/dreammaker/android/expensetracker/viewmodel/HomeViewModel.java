package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.dao.AnalyticsDao;
import dreammaker.android.expensetracker.database.view.AssetLiabilitySummary;

public class HomeViewModel extends DBViewModel {

    private LiveData<AssetLiabilitySummary> assetLiabilitySummaryLiveData;

    public HomeViewModel(@NonNull Application application) {
        super(application);
    }

    @NonNull
    private AnalyticsDao getAnalyticsDao() {
        return getExpenseDatabase().getAnalyticsDao();
    }

    @NonNull
    public LiveData<AssetLiabilitySummary> getAssetLiabilitySummaryLiveData() {
        if (null == assetLiabilitySummaryLiveData) {
            assetLiabilitySummaryLiveData = getAnalyticsDao().getTotalAssetLiability();
        }
        return assetLiabilitySummaryLiveData;
    }
}

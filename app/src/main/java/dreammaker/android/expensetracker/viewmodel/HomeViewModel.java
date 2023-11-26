package dreammaker.android.expensetracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import dreammaker.android.expensetracker.database.ExpensesDatabase;
import dreammaker.android.expensetracker.database.dao.AnalyticsDao;
import dreammaker.android.expensetracker.database.view.AssetLiabilitySummary;

public class HomeViewModel extends AndroidViewModel {

    private AnalyticsDao analyticsDao;

    private LiveData<AssetLiabilitySummary> assetLiabilitySummaryLiveData;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        ExpensesDatabase db = ExpensesDatabase.getInstance(application);
        analyticsDao = db.getAnalyticsDao();
    }

    public LiveData<AssetLiabilitySummary> getAssetLiabilitySummaryLiveData() {
        if (null == assetLiabilitySummaryLiveData) {
            assetLiabilitySummaryLiveData = analyticsDao.getTotalAssetLiability();
        }
        return assetLiabilitySummaryLiveData;
    }
}

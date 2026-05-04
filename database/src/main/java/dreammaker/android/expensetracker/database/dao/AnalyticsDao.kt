package dreammaker.android.expensetracker.database.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {

    @Query("SELECT COALESCE(SUM(`balance`), 0) AS `total_balance` FROM `accounts`")
    fun getTotalAccountBalance(): Flow<Double>
}
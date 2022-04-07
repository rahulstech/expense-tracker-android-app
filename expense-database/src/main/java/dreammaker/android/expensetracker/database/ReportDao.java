package dreammaker.android.expensetracker.database;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Query;
import dreammaker.android.expensetracker.util.Date;

@Dao
public abstract class ReportDao {

    @Query("SELECT sum(CASE WHEN amount < 0 THEN 1 ELSE 0 END) totalCreditEntries," +
            "       sum(CASE WHEN amount < 0 THEN amount ELSE 0 END) totalCreditedAmount," +
            "       sum(CASE WHEN amount > 0 THEN 1 ELSE 0 END) totalDebitEntries," +
            "       sum(CASE WHEN amount > 0 THEN amount ELSE 0 END) totalDebitedAmount," +
            "       date dateStart," +
            "       date dateEnd, " +
            "       :entityType entityType, :entityId entityId" +
            "  FROM transactions" +
            " WHERE date >= :start AND date <= :end AND CASE :entityType WHEN \"PERSONS\" THEN person_id = :entityId WHEN \"ACCOUNTS\" THEN account_id = :entityId END" +
            " GROUP BY dateStart ORDER BY dateStart DESC;")
    public abstract List<Report> getDailyReport(Date start, Date end, long entityId, EntityType entityType);
}

package dreammaker.android.expensetracker.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import dreammaker.android.expensetracker.util.Date;

@Entity(tableName = "budgets")
public class Budget implements Cloneable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private long budgetId;

    @NonNull
    private Date dateStart;

    @NonNull
    private Date dateEnd;

    @ColumnInfo(defaultValue = "0")
    @NonNull
    private float  targetAmount;

    @ColumnInfo(defaultValue = "0")
    @NonNull
    private float achievedAmount;

    @NonNull
    private long entityId;

    @NonNull
    private EntityType entityType;

    private String remark;

    public Budget(long budgetId, @NonNull Date dateStart, @NonNull Date dateEnd, float targetAmount, float achievedAmount, long entityId, @NonNull EntityType entityType, String remark) {
        this.budgetId = budgetId;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.targetAmount = targetAmount;
        this.achievedAmount = achievedAmount;
        this.entityId = entityId;
        this.entityType = entityType;
        this.remark = remark;
    }

    @Ignore
    public Budget() {}

    public long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(long budgetId) {
        this.budgetId = budgetId;
    }

    @NonNull
    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(@NonNull Date dateStart) {
        this.dateStart = dateStart;
    }

    @NonNull
    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(@NonNull Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public float getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(float targetAmount) {
        this.targetAmount = targetAmount;
    }

    public float getAchievedAmount() {
        return achievedAmount;
    }

    public void setAchievedAmount(float achievedAmount) {
        this.achievedAmount = achievedAmount;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    @NonNull
    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(@NonNull EntityType entityType) {
        this.entityType = entityType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @NonNull
    @Override
    protected Budget clone() {
        return new Budget(budgetId,dateStart,dateEnd,targetAmount,achievedAmount,entityId,entityType,remark);
    }
}

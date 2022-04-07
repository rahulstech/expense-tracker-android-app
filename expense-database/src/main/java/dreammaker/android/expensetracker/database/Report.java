package dreammaker.android.expensetracker.database;

import dreammaker.android.expensetracker.util.Date;

public class Report implements Cloneable {

    private Date dateStart;

    private Date dateEnd;

    private int totalCreditEntries;

    private float totalCreditedAmount;

    private int totalDebitEntries;

    private float totalDebitedAmount;

    private long entityId;

    private EntityType entityType;

    public Report(Date dateStart, Date dateEnd, int totalCreditEntries, float totalCreditedAmount, int totalDebitEntries, float totalDebitedAmount, long entityId, EntityType entityType) {
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.totalCreditEntries = totalCreditEntries;
        this.totalCreditedAmount = totalCreditedAmount;
        this.totalDebitEntries = totalDebitEntries;
        this.totalDebitedAmount = totalDebitedAmount;
        this.entityId = entityId;
        this.entityType = entityType;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public int getTotalCreditEntries() {
        return totalCreditEntries;
    }

    public void setTotalCreditEntries(int totalCreditEntries) {
        this.totalCreditEntries = totalCreditEntries;
    }

    public float getTotalCreditedAmount() {
        return totalCreditedAmount;
    }

    public void setTotalCreditedAmount(float totalCreditedAmount) {
        this.totalCreditedAmount = totalCreditedAmount;
    }

    public int getTotalDebitEntries() {
        return totalDebitEntries;
    }

    public void setTotalDebitEntries(int totalDebitEntries) {
        this.totalDebitEntries = totalDebitEntries;
    }

    public float getTotalDebitedAmount() {
        return totalDebitedAmount;
    }

    public void setTotalDebitedAmount(float totalDebitedAmount) {
        this.totalDebitedAmount = totalDebitedAmount;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    @Override
    public String toString() {
        return "Report{" +
                "dateStart=" + dateStart +
                ", dateEnd=" + dateEnd +
                ", totalCreditEntries=" + totalCreditEntries +
                ", totalCreditedAmount=" + totalCreditedAmount +
                ", totalDebitEntries=" + totalDebitEntries +
                ", totalDebitedAmount=" + totalDebitedAmount +
                ", entityId=" + entityId +
                ", entityType=" + entityType +
                '}';
    }


}

package dreammaker.android.expensetracker.viewmodel;

public class OperationCallback {

    private Object extra;

    public void setExtra(Object extra) { this.extra = extra;}

    @SuppressWarnings("unchecked")
    public <R> R getExtra() { return (R) extra; }

    public void onCompleteInsert(boolean success){}

    public void onCompleteUpdate(boolean success){}

    public void onCompleteDelete(boolean success){}
}

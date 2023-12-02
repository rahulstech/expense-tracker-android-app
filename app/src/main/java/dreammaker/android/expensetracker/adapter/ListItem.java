package dreammaker.android.expensetracker.adapter;

@SuppressWarnings("unused")
public class ListItem {

    private Object data;
    private int type;
    private Object extras;

    public ListItem() {
        this(null,0,null);
    }

    public ListItem(Object data, int type) {
        this(data,type,null);
    }

    public ListItem(Object data, int type, Object extras) {
        this.data = data;
        this.type = type;
        this.extras = extras;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return (T) data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getExtras() {
        return extras;
    }

    public void setExtras(Object extras) {
        this.extras = extras;
    }
}

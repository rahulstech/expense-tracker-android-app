package dreammaker.android.expensetracker.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public abstract class BaseSpinnerAdapter<T> extends BaseListAdapter<T, BaseSpinnerAdapter.SpinnerViewHolder> {

    protected BaseSpinnerAdapter(Context context) {
        super(context);
    }

    public int getPositionForId(long itemId) {
        final int count = getCount();
        for (int i = 0; i < count; i++){
            if (getItemId(i) == itemId) return i;
        }
        return NO_POSITION;
    }

    @Override
    protected SpinnerViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        return new SpinnerViewHolder(getLayoutInflater().inflate(android.R.layout.simple_list_item_1, parent, false));
    }

    public static class SpinnerViewHolder extends ViewHolder{

        TextView text1;

        public SpinnerViewHolder(View itemView) {
            super(itemView);
            text1 = findViewById(android.R.id.text1);
        }

        public void setContentText(CharSequence contentText){
            text1.setText(contentText);
        }
    }
}

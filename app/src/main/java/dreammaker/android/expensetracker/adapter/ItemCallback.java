package dreammaker.android.expensetracker.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

@SuppressWarnings("unused")
public abstract class ItemCallback extends DiffUtil.ItemCallback<ListItem> {


    @Override
    public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
        if (oldItem.getType() != newItem.getType()) {
            return false;
        }
        if (null == oldItem.getData() && null != newItem.getData()) {
            return areItemsTheSame(newItem.getType(),oldItem.getData(),newItem.getData());
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
        if (oldItem.getType() != newItem.getType()) {
            return false;
        }
        if (null == oldItem.getData() && null != newItem.getData()) {
            return areContentsTheSame(newItem.getType(),oldItem.getData(),newItem.getData());
        }
        return false;
    }

    protected abstract boolean areItemsTheSame(int type, @NonNull Object oldData, @NonNull Object newData);

    protected abstract boolean areContentsTheSame(int type, @NonNull Object oldData, @NonNull Object newData);
}

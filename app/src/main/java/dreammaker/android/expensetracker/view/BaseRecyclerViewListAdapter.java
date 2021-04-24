package dreammaker.android.expensetracker.view;

import android.content.Context;

import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.util.Check;

public abstract class BaseRecyclerViewListAdapter<T, VH extends RecyclerView.ViewHolder> extends ListAdapter<T,VH> {

    private LayoutInflater inflater;
    private OnItemChildClickListener<BaseRecyclerViewListAdapter<T, VH>, VH> onItemChildClickListener;

    protected BaseRecyclerViewListAdapter(@NonNull Context context, @NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
        inflater = LayoutInflater.from(context);
    }

    protected BaseRecyclerViewListAdapter(@NonNull Context context, @NonNull AsyncDifferConfig<T> config) {
        super(config);
        inflater = LayoutInflater.from(context);
    }

    public LayoutInflater getLayoutInflater(){
        return inflater;
    }

    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        this.onItemChildClickListener = onItemChildClickListener;
    }

    public OnItemChildClickListener<BaseRecyclerViewListAdapter<T, VH>, VH> getOnItemChildClickListener() {
        return onItemChildClickListener;
    }

    public boolean hasOnItemChildClickListener(){
        return Check.isNonNull(onItemChildClickListener);
    }

    @Override
    public T getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        T item = getItem(position);
        return Check.isNonNull(item) ? getItemId(item) : 0L;
    }

    public <D> D onSaveData(){ return null; }

    public void onRestoreData(@Nullable Object data){}

    protected abstract long getItemId(@NonNull T item);
}

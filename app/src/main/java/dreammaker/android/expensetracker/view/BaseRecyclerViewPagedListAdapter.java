package dreammaker.android.expensetracker.view;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.util.Check;

public abstract class BaseRecyclerViewPagedListAdapter<T, VH extends RecyclerView.ViewHolder> extends PagedListAdapter<T,VH> {

    private LayoutInflater inflater;
    private OnItemChildClickListener<BaseRecyclerViewPagedListAdapter<T, VH>, VH> onItemChildClickListener;

    protected BaseRecyclerViewPagedListAdapter(@NonNull Context context, @NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
        inflater = LayoutInflater.from(context);
    }

    protected BaseRecyclerViewPagedListAdapter(@NonNull Context context, @NonNull AsyncDifferConfig<T> config) {
        super(config);
        inflater = LayoutInflater.from(context);
    }

    public LayoutInflater getLayoutInflater(){
        return inflater;
    }

    public void setOnItemChildClickListener(OnItemChildClickListener onItemChildClickListener) {
        this.onItemChildClickListener = onItemChildClickListener;
    }

    public OnItemChildClickListener<BaseRecyclerViewPagedListAdapter<T, VH>, VH> getOnItemChildClickListener() {
        return onItemChildClickListener;
    }

    public boolean hasOnItemChildClickListener(){
        return Check.isNonNull(onItemChildClickListener);
    }

    public boolean isEmpty(){
        return getItemCount() == 0;
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

    protected abstract long getItemId(@NonNull T item);
}

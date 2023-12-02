package dreammaker.android.expensetracker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings({"unused","FieldMayBeFinal"})
public class BaseViewHolder<I> extends RecyclerView.ViewHolder {

    private Context mContext;

    private RecyclerView.Adapter<?> mAdapter;

    @NonNull
    public static <T> BaseViewHolder<T> create(@NonNull View itemView) {
        return new BaseViewHolder<>(itemView);
    }

    @NonNull
    public static <T> BaseViewHolder<T> create(@NonNull Context context, @NonNull ViewGroup parent, @LayoutRes int resId) {
        return create(LayoutInflater.from(context).inflate(resId,parent,false));
    }

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
        mContext = itemView.getContext();
    }

    public <V extends View> V findViewById(@IdRes int id) {
        return itemView.findViewById(id);
    }

    public void setAdapter(@NonNull RecyclerView.Adapter<?> adapter) {
        mAdapter = adapter;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        return mAdapter;
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    public void bind(@Nullable I item) {
        if (null == item) {
            onBindNull();
        }
        else {
            onBindNonNull(item);
        }
    }

    protected void onBindNull() {}

    protected void onBindNonNull(@NonNull I item){}
}

package dreammaker.android.expensetracker.view;

import android.content.Context;
import android.os.Bundle;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Filter;

public abstract class BaseRecyclerViewListAdapterFilterable<T, VH extends RecyclerView.ViewHolder> extends BaseRecyclerViewListAdapter<T, VH> implements Filter.FilterCallback<T> {
    private List<Integer> positions;
    private Filter<T> filter;

    protected BaseRecyclerViewListAdapterFilterable(@NonNull Context context, @NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(context, diffCallback);
        filter = new Filter<>(this);
    }

    protected BaseRecyclerViewListAdapterFilterable(@NonNull Context context, @NonNull AsyncDifferConfig<T> config) {
        super(context, config);
        filter = new Filter<>(this);
    }

    @Override
    public void onCurrentListChanged(@NonNull List<T> previousList, @NonNull List<T> currentList) {
        filter.cancel();
        filter.changeItems(currentList);
    }

    @Override
    public int getItemCount() {
        return Check.isNull(positions) ? super.getItemCount() : positions.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @Override
    public T getItem(int position) {
        return Check.isNull(positions) ? super.getItem(position) : super.getItem(positions.get(position));
    }

    public Filter<T> getFilter() {
        return filter;
    }

    @Override
    public <D> D onSaveData() {
        BaseRecyclerViewListAdapterFilterableSaveData d = new BaseRecyclerViewListAdapterFilterableSaveData();
        d.positions = positions;
        d.filterSavedState = getFilter().onSaveState();
        return (D) d;
    }

    @Override
    public void onRestoreData(@Nullable Object data) {
        super.onRestoreData(data);
        if (Check.isNonNull(data)){
            BaseRecyclerViewListAdapterFilterableSaveData d = (BaseRecyclerViewListAdapterFilterableSaveData) data;
            positions = d.positions;
            getFilter().onRestoreState(d.filterSavedState);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onFilterComplete(@Nullable List<Integer> inserted, @Nullable List<Integer> removed) {
        this.positions = inserted;
        notifyDataSetChanged();
    }

    public static class BaseRecyclerViewListAdapterFilterableSaveData{
        public List<Integer> positions;
        public Bundle filterSavedState;
    }
}

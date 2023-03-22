package dreammaker.android.expensetracker.view;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Filter;

public abstract class AbsSelectionListAdapter<T,VH extends ViewHolder> extends BaseListAdapter<T,VH>
        implements ViewHolder.OnChildClickListener, Filter.FilterCallback<T> {

    private List<T> checkedItems;
    private List<Integer> filteredPositions = null;
    private OnItemSelectionChangeListener listener;

    public AbsSelectionListAdapter(Context context) {
        super(context);
        checkedItems = new ArrayList<>();
        setFilter(new Filter<>(this));
    }

    ///////////////////////////////////////////////////////////////////////////
    ///            Methods For ViewHolder.OnChildClickListener             ///
    /////////////////////////////////////////////////////////////////////////

    @Override
    public void onChildClick(ViewHolder vh, View child) {
        vh.setChecked(toggleChecked(vh.getAdapterPosition()));
    }

    //////////////////////////////////////////////////////////////////////////
    ///                    Methods For FilterCallback                     ///
    ////////////////////////////////////////////////////////////////////////

    @Override
    public void onFilterComplete(@Nullable List<Integer> inserted, @Nullable List<Integer> removed) {
        this.filteredPositions = inserted;
        if (Check.isNonNull(inserted) && !inserted.isEmpty()) notifyDataSetChanged();
        else notifyDataSetInvalidated();
    }

    ///////////////////////////////////////////////////////////////////////
    ///           Methods For AbsSelectionListAdapter<T,VH>            ///
    /////////////////////////////////////////////////////////////////////

    public void setOnItemSelectionChangeListener(OnItemSelectionChangeListener listener) {
        this.listener = listener;
    }

    public void setCheckedItems(List<T> items) {
        this.checkedItems.clear();
        this.checkedItems = new ArrayList<>();
        if (null != items) {
            this.checkedItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    @Override
    public void changeList(List<T> newData) {
        clear();
        super.changeList(newData);
    }

    @Override
    public T getItem(int position) {
        return Check.isNonNull(filteredPositions) ? super.getItem(filteredPositions.get(position))
                : super.getItem(position);
    }

    @Override
    public int getCount() {
        return Check.isNonNull(filteredPositions) ? filteredPositions.size() : super.getCount();
    }

    @Override
    protected final void onBindViewHolder(@NonNull VH vh, int position) {
        final boolean checked = isChecked(position);
        vh.setOnChildClickListener(this);
        vh.setChecked(checked);
        onBindViewHolder(vh,position,checked);
    }

    protected abstract void onBindViewHolder(@NonNull VH vh, int position, boolean checked);

    protected final void setChecked(int position, boolean checked) {
        final T item = getItem(position);
        if (checked) {
            if (!checkedItems.contains(item))
                checkedItems.add(item);
        }
        else
            checkedItems.remove(item);
    }

    protected final boolean isChecked(int position) { return checkedItems.contains(getItem(position)); }

    protected final boolean toggleChecked(int position) {
        final boolean newValue = !isChecked(position);
        setChecked(position,newValue);
        if (null != listener) {
            listener.onItemSelectionChange(position,newValue);
        }
        return newValue;
    }

    private void clear(){
        checkedItems.clear();
        filteredPositions = null;
    }

    public interface OnItemSelectionChangeListener {
        void onItemSelectionChange(int position,boolean checked);
    }
}

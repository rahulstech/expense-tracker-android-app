package dreammaker.android.expensetracker.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Filter;

public abstract class BaseListAdapter<T, VH extends ViewHolder> extends BaseAdapter {

    public static final int NO_POSITION = -1;
    public static final long NO_ID = 0L;

    private LayoutInflater inflater;
    private boolean hasData = false;
    private List<T> mData;
    private Filter<T> filter;
    private OnItemChildClickListener<BaseListAdapter<T, VH>, VH> onItemChildClickListener;

    public BaseListAdapter(Context context){
        inflater = LayoutInflater.from(context);
    }

    public LayoutInflater getLayoutInflater(){
        return inflater;
    }

    public void changeList(List<T> newData){
        if (mData == newData) return;
        hasData = Check.isNonNull(newData) && !newData.isEmpty();
        mData = newData;
        if (hasData) notifyDataSetChanged();
        else notifyDataSetInvalidated();
        if (hasFilter()){
            getFilter().cancel();
            getFilter().changeItems(newData);
        }
    }

    public void setOnItemChildClickListener(OnItemChildClickListener<BaseListAdapter<T,VH>, VH> onItemChildClickListener) {
        this.onItemChildClickListener = onItemChildClickListener;
    }

    public OnItemChildClickListener<BaseListAdapter<T, VH>, VH> getOnItemChildClickListener() {
        return onItemChildClickListener;
    }

    public List<T> getItemsList(){
        return mData;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    public T getItem(int position){
        return isEmpty() ? null : mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        T item = getItem(position);
        return Check.isNonNull(item) ? getItemId(item) : NO_ID;
    }

    @Override
    public int getCount() {
        return hasData ? mData.size() : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VH vh;
        View v;
        if (null == convertView){
            vh = onCreateViewHolder(parent, position);
            v = vh.getRoot();
            v.setTag(vh);
        }
        else {
            v = convertView;
            vh = (VH) v.getTag();
        }
        vh.setAdapterPosition(position);
        onBindViewHolder(vh, position);
        return v;
    }

    public void setFilter(Filter<T> filter) {
        this.filter = filter;
    }

    public boolean hasFilter() { return null != filter; }

    public Filter<T> getFilter() {
        return filter;
    }

    @Nullable
    public <D> D onSaveData(){ return null; }

    public void onRestoreData(@Nullable Object data){}

    protected abstract long getItemId(@NonNull T item);

    protected abstract VH onCreateViewHolder(ViewGroup parent, int position);

    protected abstract void onBindViewHolder(VH vh, int position);
}

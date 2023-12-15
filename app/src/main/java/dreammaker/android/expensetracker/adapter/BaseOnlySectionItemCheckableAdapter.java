package dreammaker.android.expensetracker.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.listener.ChoiceModel;

@SuppressWarnings("unused")
public abstract class BaseOnlySectionItemCheckableAdapter<H,I,HVH extends RecyclerView.ViewHolder, IVH extends RecyclerView.ViewHolder>
        extends SectionedListAdapter<H,I,HVH,IVH> implements ChoiceModel.Callback {


    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private ChoiceModel mChoiceModel;

    private SparseArrayCompat<Object> mChoiceKeyMap;

    protected BaseOnlySectionItemCheckableAdapter(@NonNull Context context, @NonNull ItemCallback callback) {
        super(context, callback);
    }

    public void setChoiceModel(ChoiceModel model) {
        Objects.requireNonNull(model,"null ChoiceModel given");
        mChoiceModel = model;
    }

    public ChoiceModel getChoiceModel() {
        return mChoiceModel;
    }

    @NonNull
    @Override
    public Object getKey(int position) {
        if (null == mChoiceKeyMap) {
            throw new IllegalStateException("no choice key position map found");
        }
        Object key = mChoiceKeyMap.get(position,null);
        if (key == null) {
            throw new NullPointerException("no choice key exists for position="+position);
        }
        return key;
    }

    @Override
    public int getPosition(@NonNull Object key) {
        if (null == mChoiceKeyMap) {
            return RecyclerView.NO_POSITION;
        }
        int index = mChoiceKeyMap.indexOfValue(key);
        if (index < 0) {
            return RecyclerView.NO_POSITION;
        }
        return mChoiceKeyMap.keyAt(index);
    }

    @Override
    public boolean isCheckable(int position) {
        return getItemViewType(position) == SECTION_ITEM_TYPE;
    }

    protected final void postChoiceKeyMap(final SparseArrayCompat<Object> map) {
        mHandler.post(()->{
            if (null == map) {
                if (null != mChoiceKeyMap) {
                    mChoiceKeyMap.clear();
                    mChoiceKeyMap = null;
                    return;
                }
            }
            mChoiceKeyMap = map;
        });
    }

    protected final SparseArrayCompat<Object> prepareChoiceKeyMap(List<ListItem> listItems) {
        if (listItems.isEmpty()) {
            return null;
        }
        SparseArrayCompat<Object> map = new SparseArrayCompat<>();
        int position = 0;
        for (ListItem item : listItems) {
            if (item.getType() == SECTION_ITEM_TYPE) {
                Object key = getChoiceKeyFromData(item.getData());
                map.put(position,key);
            }
            position++;
        }
        return map;
    }

    @NonNull
    protected abstract Object getChoiceKeyFromData(I data);
}

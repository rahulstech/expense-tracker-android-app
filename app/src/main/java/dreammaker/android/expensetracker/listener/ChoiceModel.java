package dreammaker.android.expensetracker.listener;

import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.RecyclerView;

/**
 * <pre>
 *  class SimpleViewHolder extends RecyclerView.ViewHolder {
 *
 *         CheckedTextView text1;
 *
 *         public SimpleViewHolder(&#64NonNull View itemView) {
 *             super(itemView);
 *             text1 = itemView.findViewById(android.R.id.text1);
 *         }
 *
 *         public void bind(String value) {
 *             text1.setText(value);
 *         }
 *
 *         public void setChecked(boolean checked) {
 *             text1.setChecked(checked);
 *         }
 *     }
 *
 *  class MyAdapter extends RecyclerView.Adapter&#60SimpleViewHolder&#62 implements ChoiceModel.Callback {
 *
 *         private List&#60String&#62 mItems = Collections.emptyList();
 *         private ChoiceModel mChoiceModel;
 *
 *         public MyAdapter(List&#60String&#62 items) {
 *              mItems = items;
 *         }
 *
 *         public void setChoiceModel(ChoiceModel model) {
 *              mChoiceModel = model;
 *         }
 *
 *         public int getItemCount() {
 *              return mItems.size();
 *         }
 *
 *         &#64NonNull
 *         public Object getKey(int position) {
 *              return mItems.get(position);
 *         }
 *
 *         public int getPosition(@NonNull Object key) {
 *              return mItems.indexOf(key);
 *         }
 *
 *         public boolean isCheckable(int position) {
 *             return true;
 *         }
 *
 *         &#64NonNull
 *         public SimpleViewHolder onCreateViewHolder(&#64NonNull ViewGroup parent, int itemType) {
 *             View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_checked,parent,false);
 *             SimpleViewHolder holder = new SimpleViewHolder(view);
 *             return holder;
 *         }
 *
 *         public void onBindViewHolder(&#64onNull SimpleViewHolder holder, int position) {
 *             holder.setItem(mItems.get(position));
 *             holder.setChecked(model.isChecked(position));
 *         }
 *     }
 * </pre>
 */
@SuppressWarnings("unused")
public class ChoiceModel implements OnItemClickListener, OnItemLongClickListener {

    private static final String TAG = ChoiceModel.class.getSimpleName();

    /**
     * In this mode item selection is disabled. item clicking and long clicking
     * can be handled via {@link OnItemClickListener} and {@link OnItemLongClickListener} respectively
     */
    public static final int CHOICE_MODE_NONE = 0;

    /**
     * In this mode only one item is selected at a time others
     * are selected
     */
    public static final int CHOICE_MODE_SINGLE = 1;

    /**
     * In this mode multiple items can be selected
     */
    public static final int CHOICE_MODE_MULTIPLE = 2;

    /**
     * In this mode {@link ActionMode} is stated and multiple items can be selected
     */
    public static final int CHOICE_MODE_MULTIPLE_MODAL = 4;

    @NonNull
    private final RecyclerView mRecyclerView;

    @NonNull
    private final Callback mCallback;

    private int mChoiceMode = CHOICE_MODE_NONE;

    private final List<Object> mKeys;
    
    private OnItemClickListener mClickListener;

    private OnItemLongClickListener mLongClickListener;

    private OnItemCheckedListener mItemCheckedListener;

    private ModalChoiceModeListenerWrapper mModalModeListener;

    private ActionMode mChoiceActionMode;

    /** indicates that action mode is stated long pressing an item */
    private boolean mInActionMode = false;

    public ChoiceModel(@NonNull RecyclerView rv, @NonNull Callback callback) {
        Objects.requireNonNull(rv,"RecyclerView == null");
        Objects.requireNonNull(callback,"ChoiceModel.Callback == null");
        mRecyclerView = rv;
        mCallback = callback;
        mKeys = new ArrayList<>();
        RecyclerViewItemClickHelper mClickHelper = new RecyclerViewItemClickHelper(rv);
        mClickHelper.setOnItemClickListener(this);
        mClickHelper.setOnItemLongClickListener(this);
        mRecyclerView.addOnItemTouchListener(mClickHelper);
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    @NonNull
    public RecyclerView.Adapter<?> getAdapter() {
        RecyclerView.Adapter<?> adapter = mRecyclerView.getAdapter();
        Objects.requireNonNull(adapter,"RecyclerView.getAdapter() == null");
        return mRecyclerView.getAdapter();
    }

    // Setter methods for listeners

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    public void setOnItemCheckedListener(OnItemCheckedListener listener) {
        mItemCheckedListener = listener;
    }

    public void setModalChoiceModeListener(ModalChoiceModeListener listener) {
        if (null == mModalModeListener) {
            mModalModeListener = new ModalChoiceModeListenerWrapper();
        }
        mModalModeListener.wrap(listener);
    }

    public void setChoiceMode(int mode) {
        Log.d(TAG,"old choice-mode: "+mChoiceMode+" new choice-mode: "+mode);
        if (mChoiceMode == mode) {
            return;
        }
        mChoiceMode = mode;
        clearAllSelections();
        mItemCheckedListener = null;
        mModalModeListener = null;
    }

    // Methods related to sate

    public void onRestoreInstanceState(@NonNull SavedStateViewModel savedState) {
        Log.d(TAG,"restoreInstanceState");
        final int choiceMode = savedState.mChoiceMode;
        final List<Object> keys = savedState.mKeys;
        final boolean inActionMode = savedState.mInActionMode;

        mChoiceMode = choiceMode;
        if (null != keys) {
            mKeys.addAll(keys);
        }
        if (inActionMode && !(mInActionMode = startActionMode())) {
            return;
        }
        else {
            mKeys.clear();
        }
        notifyAdapterItemChanged(getCheckedPositionsAsArray());
    }

    public void onSaveInstanceState(@NonNull SavedStateViewModel outState) {
        Log.d(TAG,"saveInstanceState");
        outState.mChoiceMode = mChoiceMode;
        outState.mKeys = new ArrayList<>(mKeys);
        outState.mInActionMode = mInActionMode;
    }

    // Methods related to item selection

    public void clearAllSelections() {
        if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL && mInActionMode) {
            mChoiceActionMode.finish();
            // we don't need to clear selections because onDestroyActionMode itself
            // calls this method. so when action mode is destroyed the mInActionMode
            // will be false and the actual clearing of selections will happen
            return;
        }
        int[] positions = getCheckedPositionsAsArray();
        mKeys.clear();
        notifyAdapterItemChanged(positions);
    }

    private int[] getCheckedPositionsAsArray() {
        int count = mKeys.size();
        Object[] keys = mKeys.toArray();
        int[] positions = new int[count];
        int valid = 0;
        for (int i=0; i<count; i++) {
            int position = mCallback.getPosition(keys[i]);
            if (position >= 0) {
                positions[valid++] = position;
            }
        }
        if (valid == count) {
            return positions;
        }
        int[] valid_positions = new int[valid];
        System.arraycopy(positions,0,valid_positions,0,valid);
        return valid_positions;
    }

    public int getChoiceMode() {
        return mChoiceMode;
    }

    public boolean isChecked(int position) {
        Object key = mCallback.getKey(position);
        return mKeys.contains(key);
    }

    public void toggle(int position) {
        setChecked(position,!isChecked(position));
    }

    public void setChecked(int position, boolean check) {
        boolean checkable = mCallback.isCheckable(position);
        Object key = mCallback.getKey(position);
        Objects.requireNonNull(key,"ChoiceModel key at position "+position+" is null");
        Log.d(TAG,"mode="+mChoiceMode+" position="+position+" checkable="+checkable+" check="+check);
        if (!checkable) {
            return;
        }
        if (mChoiceMode == CHOICE_MODE_SINGLE) {
            if (check) {
                Object selectedKey = getSelectedKey();
                if (null != selectedKey) {
                    mKeys.remove(selectedKey);
                    if (!key.equals(selectedKey)){
                        notifyAdapterItemChanged(mCallback.getPosition(selectedKey));
                    }
                }
                if (!key.equals(selectedKey)) {
                    mKeys.add(key);
                }
            }
            else {
                mKeys.remove(key);
            }
        }
        else if (mChoiceMode == CHOICE_MODE_MULTIPLE) {
            if (check) {
                mKeys.add(key);
            }
            else {
                mKeys.remove(key);
            }
        }
        else if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            // check user started the action mode by long pressing item
            if (!mInActionMode) {
                return;
            }
            if (check) {
                mKeys.add(key);
            }
            else {
                mKeys.remove(key);
            }
        }
        if (mChoiceMode != CHOICE_MODE_NONE) {
            notifyAdapterItemChanged(position);
            notifyListeners(position,check);
        }
    }

    @Nullable
    public Object getSelectedKey() {
        if (mKeys.isEmpty()) {
            return null;
        }
        return mKeys.get(0);
    }

    public int getSelectedPosition() {
        Object key = getSelectedKey();
        if (null == key) {
            return RecyclerView.NO_POSITION;
        }
        return getPosition(key);
    }

    @NonNull
    public List<Object> getCheckedKeys() {
        return Collections.unmodifiableList(mKeys);
    }

    @NonNull
    public List<Integer> getCheckedPositions() {
        final Object[] keys = mKeys.toArray();
        ArrayList<Integer> positions = new ArrayList<>(keys.length);
        for (Object key : keys) {
            int position = mCallback.getPosition(key);
            if (position >= 0) {
                positions.add(position);
            }
        }
        return positions;
    }

    public int getCheckedCount() {
        return mKeys.size();
    }

    public boolean hasSelection() {
        return 0!=getCheckedCount();
    }

    public int getPosition(@NonNull Object key) {
        return mCallback.getPosition(key);
    }

    // Item click and long click listener methods

    @Override
    public void onClickItem(@NonNull RecyclerView recyclerView, @NonNull View view, int position) {
        Log.d(TAG,"itemClicked@"+position);
        if (null != mClickListener
                && (mChoiceMode == CHOICE_MODE_NONE
                || (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL && !mInActionMode))) {
            mClickListener.onClickItem(recyclerView,view,position);
        }
        else {
            toggle(position);
        }
    }

    @Override
    public void onLongClickItem(@NonNull RecyclerView rv, @NonNull View view, int position) {
        Log.d(TAG,"itemLongClicked@"+position);
        if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL){
            if (mCallback.isCheckable(position) && !mInActionMode) {
                boolean actionModeStated = startActionMode();
                if (!actionModeStated) {
                    throw new IllegalStateException("unable to start action mode");
                }
                mInActionMode = true;
                setChecked(position, true);
            }
        }
        else if (null != mLongClickListener){
            mLongClickListener.onLongClickItem(rv,view,position);
        }
    }

    // Internal helper methods

    private boolean startActionMode() {
        // first check choice mode is modal or not
        if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            if (!mModalModeListener.hasWrappedListener()) {
                throw new IllegalStateException("no ModalChoiceModeLister is set");
            }
            // if action mode not started then start
            if (null == mChoiceActionMode) {
                mChoiceActionMode = mRecyclerView.startActionMode(mModalModeListener);
                return mChoiceActionMode != null;
            }
        }
        return false;
    }

    private void notifyAdapterItemChanged(int... positions) {
        for (int pos : positions) {
            getAdapter().notifyItemChanged(pos);
        }
    }

    private void notifyListeners(int position, boolean checked) {
        if (mChoiceMode == CHOICE_MODE_MULTIPLE_MODAL) {
            mModalModeListener.onItemChecked(mChoiceActionMode,
                    getItemViewForAdapterPosition(position),position,checked);
            // if no more items selected then finish action mode
            if (mKeys.isEmpty()) {
                mChoiceActionMode.finish();
            }
        }
        else if (null != mItemCheckedListener) {
            mItemCheckedListener.onItemChecked(mRecyclerView,
                    getItemViewForAdapterPosition(position), position, checked);
        }
    }

    @NonNull
    private View getItemViewForAdapterPosition(int position) {
        RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForAdapterPosition(position);
        if (null == holder) {
            throw new IllegalStateException("can not find ViewHolder at position=" + position);
        }
        return holder.itemView;
    }

    private void onDestroyActionMode() {
        mChoiceActionMode = null;
        mInActionMode = false;
        clearAllSelections();
    }

    /**
     *
     */
    public interface Callback {

        @NonNull Object getKey(int position);

        int getPosition(@NonNull Object key);

        boolean isCheckable(int position);
    }

    /**
     * An ViewModel to retain ChoiceModel state across configuration change.
     */
    public static class SavedStateViewModel extends ViewModel {

        int mChoiceMode;

        List<Object> mKeys;

        boolean mInActionMode;

        public SavedStateViewModel() {}
    }

    private class ModalChoiceModeListenerWrapper implements ModalChoiceModeListener {

        private ModalChoiceModeListener wrapped;

        public ModalChoiceModeListenerWrapper() {}

        public boolean hasWrappedListener() {
            return null != wrapped;
        }

        public ModalChoiceModeListener getWrapped() {
            return wrapped;
        }

        public void wrap(ModalChoiceModeListener listener) {
            this.wrapped = listener;
        }

        @Override
        public void onItemChecked(@NonNull ActionMode mode, @NonNull View view, int position, boolean checked) {
            if (null != wrapped) {
                wrapped.onItemChecked(mode,view,position,checked);
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (null != wrapped) {
                wrapped.onCreateActionMode(mode,menu);
                return true;
            }
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if (null != wrapped) {
                return wrapped.onPrepareActionMode(mode,menu);
            }
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (null != wrapped) {
                return wrapped.onActionItemClicked(mode,item);
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (null != wrapped) {
                wrapped.onDestroyActionMode(mode);
            }
            ChoiceModel.this.onDestroyActionMode();
        }
    }
}

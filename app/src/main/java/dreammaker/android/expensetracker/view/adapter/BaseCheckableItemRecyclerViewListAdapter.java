package dreammaker.android.expensetracker.view.adapter;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.AbsSavedState;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public abstract class BaseCheckableItemRecyclerViewListAdapter<T,VH extends BaseCheckableItemRecyclerViewListAdapter.BaseCheckableItemViewHolder<T>>
        extends BaseClickableItemRecyclerViewListAdapter<T,VH> {

    private static final String TAG = "BaseChkbleAdapter";

    public static final int CHOICE_MODE_NONE = 0;

    public static final int CHOICE_MODE_SINGLE = 1;

    public static final int CHOICE_MODE_MULTIPLE = 2;


    private int choiceMode = CHOICE_MODE_NONE;

    private List<Long> mCheckedIds = null;

    private long mCheckedItemId = -RecyclerView.NO_ID;

    private OnRecyclerViewItemCheckChangeListener checkedChangeListener;

    protected BaseCheckableItemRecyclerViewListAdapter(@NonNull Context context, @NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(context, diffCallback);
    }

    protected BaseCheckableItemRecyclerViewListAdapter(@NonNull Context context, @NonNull DiffUtil.ItemCallback<T> diffCallback, boolean enableBlankItem) {
        super(context, diffCallback,enableBlankItem);
    }

    public Parcelable onSaveState() {
        SavedState state = new SavedState();
        state.choiceMode = choiceMode;
        state.mCheckedItemId = mCheckedItemId;
        state.mCheckedIds = mCheckedIds;
        return state;
    }

    public void onRestoreState(@Nullable Parcelable in) {
        if (null != in && in instanceof SavedState) {
            SavedState state = (SavedState) in;
            choiceMode = state.choiceMode;
            mCheckedItemId = state.mCheckedItemId;
            mCheckedIds = state.mCheckedIds;
            notifyDataSetChanged();
        }
    }

    public void setOnRecyclerViewItemCheckedChangeListener(@Nullable OnRecyclerViewItemCheckChangeListener listener) {
        this.checkedChangeListener = listener;
    }

    public void clearSelections() {
        if (null != mCheckedIds) mCheckedIds.clear();
        mCheckedItemId = RecyclerView.NO_ID;
        mCheckedIds = null;
        notifyDataSetChanged();
    }

    public boolean hasSelection() {
        if (choiceMode == CHOICE_MODE_NONE) return false;
        if (choiceMode == CHOICE_MODE_SINGLE) return mCheckedItemId != RecyclerView.NO_ID;
        return null != mCheckedIds && !mCheckedIds.isEmpty();
    }

    public void changeChoiceMode(int choiceMode) {
        if (choiceMode == this.choiceMode) return;
        clearSelections();
        this.choiceMode = choiceMode;
        if (choiceMode == CHOICE_MODE_NONE) return;
        if (!hasStableIds()) {
            throw new IllegalStateException("choice mode other than NONE requires stable item id");
        }
        notifyDataSetChanged();
    }

    public int getChoiceMode() {
        return choiceMode;
    }

    public boolean isChecked(int position) {
        if (choiceMode == CHOICE_MODE_NONE) return false;
        final long itemId = getItemId(position);
        if (choiceMode == CHOICE_MODE_SINGLE) return mCheckedItemId == itemId;
        return mCheckedIds.contains(itemId);
    }

    public List<Long> getCheckedItemIds() {
        if (CHOICE_MODE_MULTIPLE == choiceMode) {
            return new ArrayList<>(this.mCheckedIds);
        }
        return null;
    }

    public int getPositionByItemId(long id) {
        if (!hasStableIds()) {
            throw new IllegalStateException("item position by item id requires stable item ids");
        }
        for (int i = 0; i < getItemCount(); i++) {
            if (id == getItemId(i)) {
                return i;
            }
        }
        return -1;
    }

    public int getCheckedItemPosition() {
        if (choiceMode == CHOICE_MODE_SINGLE) {
            return getPositionByItemId(mCheckedItemId);
        }
        return RecyclerView.NO_POSITION;
    }

    @Nullable
    public T getCheckedItem() {
        if (choiceMode == CHOICE_MODE_SINGLE && mCheckedItemId != RecyclerView.NO_ID) {
            return getItem(getCheckedItemPosition());
        }
        return null;
    }

    public void dispatchItemCheckChanged(@NonNull View which, int position, boolean checked) {
        setItemChecked(position,checked);
        if (null != checkedChangeListener) {
            checkedChangeListener.onRecyclerViewItemCheckChanged(this,which,position,checked);
        }
    }

    private boolean setItemChecked(int position, boolean checked) {
        if (choiceMode == CHOICE_MODE_NONE) return false;

        int choiceMode = this.choiceMode;
        long itemId = getItemId(position);

        if (choiceMode == CHOICE_MODE_MULTIPLE) {
            if (null == mCheckedIds) mCheckedIds = new ArrayList<>();
            boolean isChecked = mCheckedIds.contains(itemId);
            if (isChecked == checked) return false;
            if (checked) mCheckedIds.add(itemId);
            else mCheckedIds.remove(itemId);
            return true;
        }
        else {
            int oldCheckedIndex = getCheckedItemPosition();
            long oldCheckedId = mCheckedItemId;
            if (itemId != oldCheckedId) {
                if (RecyclerView.NO_POSITION != oldCheckedIndex)
                    notifyItemChanged(oldCheckedIndex);
                mCheckedItemId = itemId;
                notifyItemChanged(getCheckedItemPosition());
                return true;
            }
            return false;
        }
    }

    public static abstract class BaseCheckableItemViewHolder<T> extends BaseClickableItemRecyclerViewListAdapter.BaseClickableItemViewHolder<T> {

        public BaseCheckableItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Nullable
        public BaseCheckableItemRecyclerViewListAdapter<?,?> getCheckableItemAdapter() {
            return (BaseCheckableItemRecyclerViewListAdapter<?, ?>) getBindingAdapter();
        }

        public boolean isCheckable() {
            return true;
        }

        public void setChecked(boolean checked) {}

        public boolean isChecked() {
            return getCheckableItemAdapter().isChecked(getAbsoluteAdapterPosition());
        }

        @Override
        public void onClick(View view) {
            dispatchItemCheckChanged(view,!isChecked());
        }

        protected final void dispatchItemCheckChanged(@NonNull View which, boolean checked) {
            if (!isCheckable()) return;
            getCheckableItemAdapter().dispatchItemCheckChanged(which,getAbsoluteAdapterPosition(),checked);
        }
    }

    public static class SavedState extends AbsSavedState {

        int choiceMode;
        long mCheckedItemId;
        List<Long> mCheckedIds;

        public SavedState() {
            super(EMPTY_STATE);
        }

        public SavedState(Parcelable parent) {
            super(parent);
        }

        protected SavedState(Parcel in) {
            super(in);
            choiceMode = in.readInt();
            mCheckedItemId = in.readLong();
            int countCheckedIds = in.readInt();
            if (countCheckedIds > 0) {
                mCheckedIds = new ArrayList<>();
                for (int i = 0; i < countCheckedIds; i++) {
                    mCheckedIds.add(in.readLong());
                }
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            final int choiceMode = this.choiceMode;
            final long mCheckedItemId = this.mCheckedItemId;
            final List<Long> mCheckedIds = this.mCheckedIds;
            final int countCheckedIds = null == mCheckedIds || mCheckedIds.isEmpty() ? 0 : mCheckedIds.size();
            dest.writeInt(choiceMode);
            dest.writeLong(mCheckedItemId);
            dest.writeInt(countCheckedIds);
            if (countCheckedIds > 0) {
                for (long id : mCheckedIds) {
                    dest.writeLong(id);
                }
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}

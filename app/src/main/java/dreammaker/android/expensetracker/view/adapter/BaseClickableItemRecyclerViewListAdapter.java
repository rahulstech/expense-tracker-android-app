package dreammaker.android.expensetracker.view.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.util.Check;

public abstract class BaseClickableItemRecyclerViewListAdapter<T,VH extends BaseClickableItemRecyclerViewListAdapter.BaseClickableItemViewHolder>
        extends ListAdapter<T,VH> {

    private static final String TAG = "BaseClkAdapter";

    public static final int ITEM_TYPE_BLANK = 0;

    public static final int ITEM_TYPE_ITEM = 1;

    @NonNull
    private Context context;

    @NonNull
    private LayoutInflater inflater;

    private List<T> mOriginalItems = null;

    private Filter mFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<T> values = onFilter(constraint);
            int count = null == values ? 0 : values.size();
            FilterResults results = new FilterResults();
            results.count = count;
            results.values = values;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<T> values = (List<T>) results.values;
            submitList(values);
        }
    };

    private OnRecyclerViewItemClickListener clickListener;

    private OnRecyclerViewItemLongClickListener longClickListener;

    private boolean mBlankItemEnabled = false;

    protected BaseClickableItemRecyclerViewListAdapter(@NonNull Context context, @NonNull DiffUtil.ItemCallback<T> diffCallback) {
        this(context,diffCallback,false);
    }

    protected BaseClickableItemRecyclerViewListAdapter(@NonNull Context context, @NonNull DiffUtil.ItemCallback<T> diffCallback, boolean enableBlankItem) {
        super(diffCallback);
        init(context);
        this.mBlankItemEnabled = enableBlankItem;
    }

    private void init(@NonNull Context context) {
        Check.isNonNull(context,"null == context");
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    public Context getContext() {
        return context;
    }

    @NonNull
    public LayoutInflater getInflater() {
        return inflater;
    }

    public void setOnRecyclerViewItemClickListener(@Nullable OnRecyclerViewItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnRecyclerViewItemLongClickListener(@Nullable OnRecyclerViewItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOriginalItems(@Nullable List<T> items) {
        this.mOriginalItems = items;
    }

    @Nullable
    public List<T> getOriginalItems() {
        return mOriginalItems;
    }

    public boolean isBlankItemEnabled() {
        return mBlankItemEnabled;
    }

    @Nullable
    public T getItem(int position) {
        if (getItemViewType(position) == ITEM_TYPE_BLANK) return null;
        return super.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == ITEM_TYPE_BLANK) return RecyclerView.NO_ID;
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        final int count = super.getItemCount();
        if (!isBlankItemEnabled() || 0 == count) return count;
        return count+1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isBlankItemEnabled() && position == getItemCount()-1) {
            return ITEM_TYPE_BLANK;
        }
        return ITEM_TYPE_ITEM;
    }

    public boolean isEmpty() { return getItemCount() == 0; }

    public void dispatchItemClick(@NonNull View which, int position) {
        if (getItemViewType(position) == ITEM_TYPE_BLANK) return;
        if (null != clickListener) {
            clickListener.onRecyclerViewItemClicked(this,which,position);
        }
    }

    public boolean dispatchItemLongClick(@NonNull View which, int position) {
        if (getItemViewType(position) == ITEM_TYPE_BLANK) return false;
        if (null != longClickListener) {
            longClickListener.onRecyclerViewItemLongClicked(this,which,position);
        }
        return false;
    }

    public Filter getFilter() {
        return mFilter;
    }

    @NonNull
    @Override
    public final VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        VH vh = onCreateViewHolder(getInflater(),parent,viewType);
        return vh;
    }

    protected abstract VH onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType);

    protected List<T> onFilter(@Nullable CharSequence constraint) { return null; }

    public abstract static class BaseClickableItemViewHolder<T> extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        @NonNull
        private static View wrapItemView(@NonNull View itemView) {
            LayoutInflater inflater = LayoutInflater.from(itemView.getContext());
            return inflater.inflate(R.layout.list_item_view_and_empty_view_container, null, false);
        }

        private TextView blankItem;
        private FrameLayout itemViewContainer;
        private boolean mLayoutParamSet = false;

        public BaseClickableItemViewHolder(@NonNull View itemView) {
            super(wrapItemView(itemView));
            blankItem = findViewById(R.id.blank_item);
            itemViewContainer = findViewById(R.id.item_view_container);
            itemViewContainer.addView(itemView,new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT));
        }

        @Nullable
        public BaseClickableItemRecyclerViewListAdapter<?,?> getClickableItemAdapter() {
            return (BaseClickableItemRecyclerViewListAdapter<?,?>) getBindingAdapter();
        }

        public <V extends View> V findViewById(@IdRes int id) {
            return itemView.findViewById(id);
        }

        public void bind(@Nullable T item) {
            bindWithBlankItem(item,null);
        }

        public void bindWithBlankItem(@Nullable T item, @Nullable Object payload) {
            if (!mLayoutParamSet) {
                itemView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT));
                mLayoutParamSet = true;
            }
            if (getItemViewType() == ITEM_TYPE_BLANK) {
                itemViewContainer.setVisibility(View.GONE);
                blankItem.setVisibility(View.VISIBLE);
            }
            else {
                blankItem.setVisibility(View.GONE);
                itemViewContainer.setVisibility(View.VISIBLE);
                bind(item,payload);
            }
        }

        public abstract void bind(@Nullable T item, @Nullable Object payload);

        @NonNull
        public Context getContext() {
            return itemView.getContext();
        }

        public String getString(@StringRes int res) {
            return getContext().getString(res);
        }

        public int getColor(@ColorRes int res) {
            return ContextCompat.getColor(getContext(),res);
        }

        @Override
        public void onClick(View view) {
            dispatchItemClick(view);
        }

        @Override
        public boolean onLongClick(View view) {
            return dispatchItemLongClick(view);
        }

        protected final void dispatchItemClick(@NonNull View which) {
            getClickableItemAdapter().dispatchItemClick(which,getAbsoluteAdapterPosition());
        }

        protected final boolean dispatchItemLongClick(@NonNull View which) {
            return getClickableItemAdapter().dispatchItemLongClick(which,getAbsoluteAdapterPosition());
        }
    }
}

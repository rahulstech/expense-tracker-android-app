package dreammaker.android.expensetracker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Simple Sectioned Adapter. This adapter handles only two categories of item types - header and child.
 * Any number of view types can be added; but these view types must be of two categories either header
 * or child. {@link #isHeaderType(int)} checks if the view type is header category, default implementation
 * returns true if the view type is {@link #TYPE_HEADER}. {@link #isChildType(int)} checks if the view type
 * is child category, default implementation returns true if the view type if {@link #TYPE_CHILD}.
 * User of the class submits a list of child items and headers are build upon those child items. Section
 * headers are created asynchronously.
 *
 * @param <H> header type
 * @param <C> child type
 * @param <HVH> header view holder, a subclass of {@link androidx.recyclerview.widget.RecyclerView.ViewHolder}
 * @param <CVH> child view holder, a subclass of {@link  androidx.recyclerview.widget.RecyclerView.ViewHolder}
 */
@SuppressWarnings({"unused","FieldMayBeFinal"})
public abstract class SectionedAdapter<H,C,HVH extends RecyclerView.ViewHolder, CVH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SectionedAdapter";

    public static final int TYPE_HEADER = 100;

    public static final int TYPE_CHILD = 200;

    private Context mContext;
    private LayoutInflater mInflater;
    private ItemCallback<H,C> mCheckCallback;
    private AsyncListDiffer<ListItem> mDiffer;
    private AsyncSectionBuilder mSectionBuilder;
    private List<Integer> mSections;

    public SectionedAdapter(@NonNull Context context, @NonNull ItemCallback<H,C> callback) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mCheckCallback = callback;
        mSections = Collections.emptyList();
        mDiffer = new AsyncListDiffer<>(this, new DiffUtil.ItemCallback<ListItem>() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                if (oldItem.type != newItem.type) return false;
                int type = oldItem.type;
                if (TYPE_HEADER == type) {
                    return mCheckCallback.isSameHeader((H) oldItem.data,(H) newItem.data);
                }
                else {
                    return mCheckCallback.isSameChild((C) oldItem.data, (C) newItem.data);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                if (oldItem.type != newItem.type) return false;
                int type = oldItem.type;
                if (TYPE_HEADER == type) {
                    return mCheckCallback.isHeaderContentSame((H) oldItem.data,(H) newItem.data);
                }
                else {
                    return mCheckCallback.isChildContentSame((C) oldItem.data, (C) newItem.data);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void submitChildren(@Nullable List<C> children) {
        if (null != mSectionBuilder) {
            try {
                mSectionBuilder.cancel(true);
            }
            catch (Exception ex) {
                Log.e(TAG,null,ex);
            }
        }
        if (null == children || children.isEmpty()) {
            onSectionsCreated(null);
        }
        else {
            mSectionBuilder = new AsyncSectionBuilder();
            mSectionBuilder.execute(children);
        }
    }

    @NonNull
    public Context getContext() {
        return mContext;
    }

    @NonNull
    public LayoutInflater getLayoutInflater() {
        return mInflater;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }

    public boolean isHeaderType(int viewType) {
        return viewType == TYPE_HEADER;
    }

    public boolean isChildType(int viewType) {
        return viewType == TYPE_CHILD;
    }

    @Override
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    public int getSectionCount() {
        return mSections.size();
    }

    /**
     * Returns the header data if the item itself a header or if item is a
     * child then the header of the section the child belongs to.
     *
     * @param adapterPosition adapter position
     * @return header data
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public H getHeader(int adapterPosition) {
        int viewType = getItemViewType(adapterPosition);
        ListItem item = getItem(adapterPosition);
        if (viewType == TYPE_HEADER) {
            return (H) item.data;
        }
        int sectionPosition = mSections.get(item.section);
        return (H) getItem(sectionPosition).data;
    }

    /**
     * Returns the data at the position either header or child.
     * This method auto casts the return value. Before using this
     * method before sure about the return type. Otherwise may raise
     * {@link ClassCastException}
     *
     * @param position adapter position
     * @param <T> return type
     * @return the data at the position
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public <T> T getData(int position) {
        return (T) getItem(position).data;
    }

    @NonNull
    private ListItem getItem(int position) {
        List<ListItem> items = mDiffer.getCurrentList();
        return items.get(position);
    }

    /**
     * Create a new view holder for the given view type.
     * This method handles only two categories of view type i.e. header and child. If either of the methods
     * {@link #isHeaderType(int)} or {@link #isChildType(int)} returns true, only that view type is handles,
     * otherwise throws exception
     *
     * @param parent containing view
     * @param viewType type of view
     * @return created view holder
     * @throws  IllegalStateException if view type is unknown
     * @see #isHeaderType(int)
     * @see #isChildType(int)
     */
    @NonNull
    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (isHeaderType(viewType)) {
            return onCreateHeaderViewHolder(getLayoutInflater(),parent,viewType);
        }
        else if (isChildType(viewType)){
            return onCreateChildViewHolder(getLayoutInflater(),parent,viewType);
        }
        throw new IllegalStateException("unknown viewType="+viewType);
    }

    /**
     * Bind the provided view holder at the position
     *
     * @param holder view holder instance to bind
     * @param position binding adapter position
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (isHeaderType(viewType)) {
            onBindHeaderViewHolder((HVH) holder,position);
        }
        else {
            onBindChildViewHolder((CVH) holder,position);
        }
    }

    /**
     * Bind the provided view holder at the position with payloads
     *
     * @param holder view holder instance to bind
     * @param position binding adapter position
     * @param payloads payloads
     */
    @SuppressWarnings("unchecked")
    @Override
    public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        int viewType = getItemViewType(position);
        if (isHeaderType(viewType)) {
            onBindHeaderViewHolder((HVH) holder,position,payloads);
        }
        else {
            onBindChildViewHolder((CVH) holder,position,payloads);
        }
    }

    /**
     * Create a new view holder for the view types that returns true from {@link #isHeaderType(int)}
     *
     * @param parent containing view
     * @param viewType type of view
     * @return created view holder
     * @see #isHeaderType(int)
     */
    @NonNull
    protected abstract HVH onCreateHeaderViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType);

    /**
     * Create a new view holder for the view types that returns true from {@link #isChildType(int)} (int)}
     *
     * @param parent containing view
     * @param viewType type of view
     * @return created view holder
     * @see #isChildType(int)
     */
    @NonNull
    protected abstract CVH onCreateChildViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType);

    /**
     * Bind the provided view holder, returned by {@link #onCreateHeaderViewHolder(LayoutInflater, ViewGroup, int)},
     * at the position
     *
     * @param holder view holder instance to bind
     * @param adapterPosition binding adapter position
     */
    protected abstract void onBindHeaderViewHolder(@NonNull HVH holder, int adapterPosition);

    /**
     * Bind the provided view holder, returned by {@link #onCreateChildViewHolder(LayoutInflater, ViewGroup, int)},
     * at the position
     *
     * @param holder view holder instance to bind
     * @param adapterPosition binding adapter position
     */
    protected abstract void onBindChildViewHolder(@NonNull CVH holder, int adapterPosition);

    /**
     * Bind the provided view holder, returned by {@link #onCreateHeaderViewHolder(LayoutInflater, ViewGroup, int)},
     * at the position with payloads
     *
     * @param holder view holder instance to bind
     * @param adapterPosition binding adapter position
     * @param payloads payloads
     */
    protected void onBindHeaderViewHolder(@NonNull HVH holder, int adapterPosition, @NonNull List<Object> payloads) {
        onBindHeaderViewHolder(holder,adapterPosition);
    }

    /**
     * Bind the provided view holder, returned by {@link #onCreateChildViewHolder(LayoutInflater, ViewGroup, int)},
     * at the position with payloads
     *
     * @param holder view holder instance to bind
     * @param adapterPosition binding adapter position
     * @param payloads payloads
     */
    protected void onBindChildViewHolder(@NonNull CVH holder, int adapterPosition, @NonNull List<Object> payloads) {
        onBindChildViewHolder(holder,adapterPosition);
    }

    /**
     * Create header item from the given child item
     *
     * @param child the child item to create header item
     * @return the created header item for child
     */
    @NonNull
    protected abstract H onCreateHeaderFromChild(@NonNull C child);

    /**
     * Do anything before start creating the section headers like sorting, filtering etc.
     * This method is called from background. Default implementation returns the given list.
     *
     * @param list submitted list of child items
     * @return handled list
     */
    @Nullable
    protected List<C> onBeforeCreateSections(@NonNull List<C> list) {
        return list;
    }

    private boolean isSameHeader(@NonNull H left, @NonNull H right) {
        return mCheckCallback.isSameHeader(left,right);
    }

    private void onSectionsCreated(@Nullable AsyncSectionBuilderResult result) {
        if (null != result) {
            mDiffer.submitList(result.items);
            mSections = result.sections;
        }
        else {
            mDiffer.submitList(null);
            mSections = Collections.emptyList();
        }
    }

    private static class ListItem {
        Object data;
        int section;
        int type;
    }

    private static class AsyncSectionBuilderResult {
        List<ListItem> items;
        // this list contains the start positions of sections
        // a section start from header and ends to the last child of the section
        List<Integer> sections;
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncSectionBuilder extends AsyncTask<List<C>,Void,AsyncSectionBuilderResult> {

        AsyncSectionBuilder() {super();}

        @Override
        protected AsyncSectionBuilderResult doInBackground(List<C>[] lists) {
            AsyncSectionBuilderResult result = new AsyncSectionBuilderResult();
            List<C> list = beforeCreateSection(lists[0]);
            if (null == list || list.isEmpty()) {
                return result;
            }
            List<ListItem> items = onCreateSections(list);
            List<Integer> sections = mapSectionPosition(items);
            result.items = items;
            result.sections = sections;
            return result;
        }

        @Override
        protected void onPostExecute(AsyncSectionBuilderResult result) {
            onSectionsCreated(result);
        }

        @Nullable
        private List<C> beforeCreateSection(@NonNull List<C> children) {
            return onBeforeCreateSections(children);
        }

        @NonNull
        private List<ListItem> onCreateSections(@NonNull List<C> list) {
            ArrayList<ListItem> items = new ArrayList<>();
            H old = null;
            int section = -1;
            for (C child : list) {
                H header = onCreateHeaderFromChild(child);
                if (old == null || !isSameHeader(old,header)) {
                    ListItem item = new ListItem();
                    item.data = header;
                    item.section = ++section;
                    item.type = TYPE_HEADER;
                    items.add(item);
                    old = header;
                }
                ListItem item = new ListItem();
                item.data = child;
                item.section = section;
                item.type = TYPE_CHILD;
                items.add(item);
            }
            return items;
        }

        @NonNull
        private List<Integer> mapSectionPosition(@NonNull List<ListItem> items) {
            ArrayList<Integer> sections = new ArrayList<>();
            int position = -1;
            for (ListItem item : items) {
                ++position;
                if (item.type == TYPE_HEADER) {
                    sections.add(position);
                }
            }
            return sections;
        }
    }
}

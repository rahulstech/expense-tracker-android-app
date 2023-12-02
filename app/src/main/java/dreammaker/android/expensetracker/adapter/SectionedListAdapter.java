package dreammaker.android.expensetracker.adapter;

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
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("unused")
public abstract class SectionedListAdapter<H,I,HVH extends RecyclerView.ViewHolder, IVH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements IHeaderFooterAdapter {

    private static final String TAG = "SectionedListAdapter";

    public static final int SECTION_HEADER_TYPE = 100;

    public static final int SECTION_ITEM_TYPE = 200;

    private final AsyncSectionBuilderCallback<H,I> BUILDER_CALLBACK = this::onResult;

    @NonNull
    @SuppressWarnings("FieldMayBeFinal")
    private Context mContext;

    @NonNull
    @SuppressWarnings("FieldMayBeFinal")
    private LayoutInflater mInflater;

    @SuppressWarnings("FieldMayBeFinal")
    private AsyncListDiffer<ListItem> mDiffer;

    private AsyncSectionBuilder<H,I> mSectionBuilder;

    private AsyncSectionBuilderResult<H,I> mLastResult = new AsyncSectionBuilderResult<>();

    private boolean mHasListHeader = false;

    private boolean mHasListFooter = false;

    protected SectionedListAdapter(@NonNull Context context, @NonNull ItemCallback callback) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDiffer = new AsyncListDiffer<>(this, callback);
    }

    public void submitList(@Nullable List<I> list) {
        if (null != mSectionBuilder) {
            mSectionBuilder.cancel(true);
            mSectionBuilder.removeAllAsyncSectionBuilderCallback();
        }
        mSectionBuilder = onCreateSectionBuilder(list);
        mSectionBuilder.addAsyncSectionBuilderCallback(BUILDER_CALLBACK);
        mSectionBuilder.execute();
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
    public int getItemCount() {
        return mDiffer.getCurrentList().size();
    }

    @Override
    public int getItemViewType(int position) {
        return mDiffer.getCurrentList().get(position).getType();
    }

    @NonNull
    public List<H> getHeaders() {
        return Collections.unmodifiableList(mLastResult.getHeaders());
    }

    public int getHeaderPositionFor(int adapterPosition) {
        ListItem item = mDiffer.getCurrentList().get(adapterPosition);
        return (int) item.getExtras();
    }

    @NonNull
    public List<I> getSubmittedList() {
        return Collections.unmodifiableList(mLastResult.getItems());
    }

    @NonNull
    public <T> T getData(int position) {
        return mDiffer.getCurrentList().get(position).getData();
    }

    @Nullable
    @Override
    public Object getHeaderData() {return null;}

    @Nullable
    @Override
    public Object getFooterData() {return null;}

    @Override
    public void setHasListHeader(boolean hasHeader) {
        mHasListHeader = hasHeader;
        onResult(mLastResult);
    }

    @Override
    public boolean hasListHeader() {
        return mHasListHeader;
    }

    @Override
    public void setHasListFooter(boolean hasFooter) {
        mHasListFooter = hasFooter;
        onResult(mLastResult);
    }

    @Override
    public boolean hasListFooter() {
        return mHasListFooter;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (SECTION_HEADER_TYPE == viewType) {
            return onCreateSectionHeaderViewHolder(parent,viewType);
        }
        else if (SECTION_ITEM_TYPE == viewType){
            return onCreateSectionItemViewHolder(parent,viewType);
        }
        else if (hasListHeader() && LIST_HEADER_TYPE == viewType) {
            return onCreateListHeaderViewHolder(parent);
        }
        else if (hasListFooter() && LIST_FOOTER_TYPE == viewType){
            return onCreateListFooterViewHolder(parent);
        }
        throw new IllegalArgumentException("unknown viewType="+viewType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (SECTION_HEADER_TYPE == type) {
            onBindSectionHeaderViewHolder((HVH) holder,position);
        }
        else if (SECTION_ITEM_TYPE == type){
            onBindSectionItemViewHolder((IVH) holder,position);
        }
        else if (LIST_HEADER_TYPE == type) {
            onBindListFooterViewHolder(holder,getHeaderData());
        }
        else {
            onBindListFooterViewHolder(holder,getFooterData());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        int type = getItemViewType(position);
        if (SECTION_HEADER_TYPE == type) {
            onBindSectionHeaderViewHolder((HVH) holder,position,payloads);
        }
        else if (SECTION_ITEM_TYPE == type){
            onBindSectionItemViewHolder((IVH) holder,position,payloads);
        }
        else {
            onBindViewHolder(holder,position);
        }
    }

    @NonNull
    protected abstract AsyncSectionBuilder<H,I> onCreateSectionBuilder(@Nullable List<I> list);

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateListHeaderViewHolder(@NonNull ViewGroup parent) {
        throw new RuntimeException("onCreateListHeaderViewHolder(ViewGroup) not implemented");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateListFooterViewHolder(@NonNull ViewGroup parent) {
        throw new RuntimeException("onCreateListFooterViewHolder(ViewGroup) not implemented");
    }

    @Override
    public void onBindListHeaderViewHolder(@NonNull RecyclerView.ViewHolder holder, @Nullable Object data) {
        throw new RuntimeException("onBindListHeaderViewHolder(RecyclerView.ViewHolder,Object) not implemented");
    }

    @Override
    public void onBindListFooterViewHolder(@NonNull RecyclerView.ViewHolder holder, @Nullable Object data) {
        throw new RuntimeException("onBindListFooterViewHolder(RecyclerView.ViewHolder,Object) not implemented");
    }

    @NonNull
    protected abstract HVH onCreateSectionHeaderViewHolder(@NonNull ViewGroup parent, int type);

    @NonNull
    protected abstract IVH onCreateSectionItemViewHolder(@NonNull ViewGroup parent, int type);

    protected abstract void onBindSectionHeaderViewHolder(@NonNull HVH holder, int adapterPosition);

    protected abstract void onBindSectionItemViewHolder(@NonNull IVH holder, int adapterPosition);

    protected void onBindSectionHeaderViewHolder(@NonNull HVH holder, int adapterPosition, @NonNull List<Object> payloads) {
        onBindSectionHeaderViewHolder(holder,adapterPosition);
    }

    protected void onBindSectionItemViewHolder(@NonNull IVH holder, int adapterPosition, @NonNull List<Object> payloads) {
        onBindSectionItemViewHolder(holder,adapterPosition);
    }

    private void onResult(AsyncSectionBuilderResult<H,I> result) {
        mLastResult = result;
        final List<ListItem> actualItems = result.getListItems();
        List<ListItem> items = new ArrayList<>();
        addListHeaderItem(items);
        items.addAll(actualItems);
        addListFooterItem(items);
        mDiffer.submitList(items);
    }

    private void addListHeaderItem(@NonNull List<ListItem> list) {
        if (!hasListHeader()) {
            return;
        }
        ListItem item = new ListItem(getHeaderData(),LIST_HEADER_TYPE);
        list.add(0,item);
        Log.d(TAG,"list header item added");
    }

    private void addListFooterItem(@NonNull List<ListItem> list) {
        if (!hasListFooter()) {
            return;
        }
        ListItem item = new ListItem(getFooterData(),LIST_FOOTER_TYPE);
        list.add(item);
        Log.d(TAG,"list footer item added");
    }

    public static class AsyncSectionBuilderResult<H,I> {

        @NonNull
        private final List<ListItem> mListItems;
        @NonNull
        private final List<I> mItems;
        @NonNull
        private final List<H> mHeaders;

        @Nullable
        private Throwable mError;

        public AsyncSectionBuilderResult() {
            this(Collections.emptyList(),Collections.emptyList(),Collections.emptyList());
        }

        public AsyncSectionBuilderResult(@NonNull List<H> headers, @NonNull List<I> items, @NonNull List<ListItem> listItems) {
            mListItems = listItems;
            mHeaders = headers;
            mItems = items;
        }

        public AsyncSectionBuilderResult(@NonNull Throwable error) {
            this();
            mError = error;
        }

        @NonNull
        public List<H> getHeaders() {
            return mHeaders;
        }

        @NonNull
        public List<ListItem> getListItems() {
            return mListItems;
        }

        @NonNull
        public List<I> getItems() {
            return mItems;
        }

        @Nullable
        public Throwable getError() {
            return mError;
        }
    }

    public interface AsyncSectionBuilderCallback<H,I> {

        void onResult(AsyncSectionBuilderResult<H,I> result);
    }

    public abstract static class AsyncSectionBuilder<H,I> extends AsyncTask<Void,Void,AsyncSectionBuilderResult<H,I>> {

        private static final String TAG = "AsyncSectionBuilder";

        @Nullable
        private final List<I> mItems;

        @Nullable
        private final List<H> mHeaders;

        private final ArrayList<AsyncSectionBuilderCallback<H,I>> mCallbacks = new ArrayList<>();

        public AsyncSectionBuilder(@Nullable List<I> items) {
            this(items,null);
        }

        public AsyncSectionBuilder(@Nullable List<I> items, @Nullable List<H> headers) {
            super();
            this.mItems = items;
            this.mHeaders = headers;
        }

        public void addAsyncSectionBuilderCallback(@NonNull AsyncSectionBuilderCallback<H,I> callback) {
            mCallbacks.add(callback);
        }

        public void removeAsyncSectionBuilderCallback(@NonNull AsyncSectionBuilderCallback<H,I> callback) {
            mCallbacks.remove(callback);
        }

        public void  removeAllAsyncSectionBuilderCallback() {
            mCallbacks.clear();
        }

        @NonNull
        public List<I> getItems() {
            if (null == mItems) {
                return Collections.emptyList();
            }
            return mItems;
        }

        @Nullable
        public List<H> getHeaders() {
            return mHeaders;
        }

        @Override
        protected AsyncSectionBuilderResult<H,I> doInBackground(Void... voids) {
            List<I> given_items = this.mItems;
            List<H> given_headers = this.mHeaders;
            try {
                if (null != given_items) {
                    List<I> items = onBeforeBuildSections(given_items);
                    List<H> headers;
                    if (null == given_headers || given_headers.isEmpty()) {
                        headers = createSectionHeaders(items);
                    } else {
                        headers = given_headers;
                    }
                    List<ListItem> listItems = createListItems(items, headers);
                    return new AsyncSectionBuilderResult<>(headers, items, listItems);
                }
            }
            catch (Throwable error) {
                Log.e(TAG,"doInBackground",error);
                return new AsyncSectionBuilderResult<>(error);
            }
            return new AsyncSectionBuilderResult<>();
        }

        @NonNull
        protected List<I> onBeforeBuildSections(@NonNull List<I> items) {
            return items;
        }

        @NonNull
        protected abstract H onCreateSectionHeader(@NonNull I item);

        protected abstract boolean belongsToSection(@NonNull I item, @NonNull H header);

        @NonNull
        private List<H> createSectionHeaders(@NonNull List<I> items) {
            ArrayList<H> headers = new ArrayList<>();
            for (I item : items) {
                H header = onCreateSectionHeader(item);
                headers.add(header);
            }
            return headers;
        }

        @NonNull
        private List<ListItem> createListItems(@NonNull List<I> items, @NonNull List<H> headers) {
            ArrayList<ListItem> listItems = new ArrayList<>();
            H lastHeader = null;
            int position = 0;
            int lastHeaderPosition = 0;
            for (I item : items) {
                H currentHeader = null;
                for (H header : headers) {
                    if (belongsToSection(item,header)) {
                        currentHeader = header;
                        break;
                    }
                }
                if (null == currentHeader) {
                    continue;
                }
                if (lastHeader != currentHeader) {
                    ListItem listItem = new ListItem(currentHeader,SECTION_HEADER_TYPE,position);
                    listItems.add(listItem);
                    lastHeader = currentHeader;
                    lastHeaderPosition = position;
                }
                ListItem listItem = new ListItem(item,SECTION_ITEM_TYPE,lastHeaderPosition);
                listItems.add(listItem);
                position++;
            }
            return listItems;
        }

        @Override
        protected final void onPostExecute(AsyncSectionBuilderResult<H,I> result) {
            for (AsyncSectionBuilderCallback<H,I> callback : mCallbacks) {
                callback.onResult(result);
            }
        }
    }
}

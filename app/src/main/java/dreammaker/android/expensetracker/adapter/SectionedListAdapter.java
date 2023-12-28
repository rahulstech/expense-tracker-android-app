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
import dreammaker.android.expensetracker.BuildConfig;

/**
 * An {@link androidx.recyclerview.widget.RecyclerView.Adapter} subclass that organizes items in sections.
 *
 * @param <H> section header data type
 * @param <I> section item data type
 * @param <HVH> a subclass of {@link androidx.recyclerview.widget.RecyclerView.ViewHolder} for section header
 * @param <IVH> a subclass of {@link androidx.recyclerview.widget.RecyclerView.ViewHolder} for section item
 */
@SuppressWarnings("unused")
public abstract class SectionedListAdapter<H,I,HVH extends RecyclerView.ViewHolder, IVH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = SectionedListAdapter.class.getSimpleName();

    private static final boolean DEBUG = BuildConfig.DEBUG;

    public static final int SECTION_HEADER_TYPE = 100;

    public static final int SECTION_ITEM_TYPE = 200;

    private final AsyncSectionBuilderCallback BUILDER_CALLBACK = this::submitResult;

    @NonNull
    @SuppressWarnings("FieldMayBeFinal")
    private Context mContext;

    @NonNull
    @SuppressWarnings("FieldMayBeFinal")
    private LayoutInflater mInflater;

    @SuppressWarnings("FieldMayBeFinal")
    private AsyncListDiffer<ListItem> mDiffer;

    private AsyncSectionBuilder<H,I> mSectionBuilder;

    private List<ListItem> mListItems = Collections.emptyList();

    private List<H> mHeaders = Collections.emptyList();

    private List<I> mItems = Collections.emptyList();

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
    public List<I> getItems() {
        return Collections.unmodifiableList(mItems);
    }

    @NonNull
    public List<H> getHeaders() {
        return Collections.unmodifiableList(mHeaders);
    }

    public List<ListItem> getListItems() {
        return Collections.unmodifiableList(mListItems);
    }

    /**
     * <strong>Note:</strong> If you change the list of {@link ListItem}s in {@link #onCompleteSectionBuild(List, List, List)}
     * then you must properly offset the adapterPosition, otherwise behaviour may be abnormal
     *
     * @param adapterPosition the position for which to find the header position
     * @return if {@link #getItemViewType(int)} returns either {@link #SECTION_HEADER_TYPE} or {@link #SECTION_ITEM_TYPE}
     *          then the position of header; otherwise {@link RecyclerView#NO_POSITION}
     * @see #getItemViewType(int)
     * @see #onCompleteSectionBuild(List, List, List)
     */
    public int getHeaderPositionFor(int adapterPosition) {
        ListItem item = mListItems.get(adapterPosition);
        if (!(item instanceof SectionedAdapterListItem)) {
            return RecyclerView.NO_POSITION;
        }
        return ((SectionedAdapterListItem) item).getSectionStartPosition();
    }

    public <T> T getData(int position) {
        return mDiffer.getCurrentList().get(position).getData();
    }

    @NonNull
    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (SECTION_HEADER_TYPE == viewType) {
            return onCreateSectionHeaderViewHolder(parent,viewType);
        }
        else if (SECTION_ITEM_TYPE == viewType){
            return onCreateSectionItemViewHolder(parent,viewType);
        }
        else {
            return onCreateOtherViewHolder(parent,viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (SECTION_HEADER_TYPE == type) {
            onBindSectionHeaderViewHolder((HVH) holder,position);
        }
        else if (SECTION_ITEM_TYPE == type){
            onBindSectionItemViewHolder((IVH) holder,position);
        }
        else {
            onBindOtherViewHolder(holder, position);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        int type = getItemViewType(position);
        if (SECTION_HEADER_TYPE == type) {
            onBindSectionHeaderViewHolder((HVH) holder,position,payloads);
        }
        else if (SECTION_ITEM_TYPE == type){
            onBindSectionItemViewHolder((IVH) holder,position,payloads);
        }
        else {
            onBindOtherViewHolder(holder,position,payloads);
        }
    }

    /**
     * Handles {@link androidx.recyclerview.widget.RecyclerView.ViewHolder} creating of viewType other than
     * {@link #SECTION_HEADER_TYPE} or {@link #SECTION_ITEM_TYPE}.
     *
     * @throws UnsupportedOperationException if this method not implemented for unknown viewType
     */
    @NonNull
    protected RecyclerView.ViewHolder onCreateOtherViewHolder(@NonNull ViewGroup parent, int viewType) {
        throw new UnsupportedOperationException("no ViewHolder created for viewType="+viewType);
    }

    /**
     * Handles binding {@link androidx.recyclerview.widget.RecyclerView.ViewHolder} if viewType other than
     * {@link #SECTION_HEADER_TYPE} or {@link #SECTION_ITEM_TYPE}.
     */
    protected void onBindOtherViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {}

    /**
     * Handles binding {@link androidx.recyclerview.widget.RecyclerView.ViewHolder} with payloads if viewType other than
     * {@link #SECTION_HEADER_TYPE} or {@link #SECTION_ITEM_TYPE}.
     */
    protected void onBindOtherViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        onBindOtherViewHolder(holder,position);
    }

    @NonNull
    protected abstract AsyncSectionBuilder<H,I> onCreateSectionBuilder(@Nullable List<I> list);

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

    private void submitResult(AsyncSectionBuilderResult result) {
        final List<ListItem> listItems = getEmptyOrUnmodifiableList(result.getListItems());
        final List<H> headers = getEmptyOrUnmodifiableList(result.getHeaders());
        final List<I> items = getEmptyOrUnmodifiableList(result.getItems());
        onCompleteSectionBuild(listItems,headers,items);
        performSubmit(result.getListItems(),result.getHeaders(),result.getItems());
    }

    private <T> List<T> getEmptyOrUnmodifiableList(List<T> list) {
        if (null == list || list.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }

    protected void onCompleteSectionBuild(@NonNull List<ListItem> listItems, @NonNull List<H> headers, @NonNull List<I> items){}

    protected final void performSubmit(List<ListItem> listItems, List<H> headers, List<I> items) {
        mListItems = null == listItems || listItems.isEmpty() ? Collections.emptyList() : listItems;
        mHeaders = null == headers || headers.isEmpty() ? Collections.emptyList() : headers;
        mItems = null == items || items.isEmpty() ? Collections.emptyList() : items;
        mDiffer.submitList(listItems);
    }

    private static class SectionedAdapterListItem extends ListItem {

        private int sectionStartPosition;

        public SectionedAdapterListItem() { super(); }

        public SectionedAdapterListItem(Object data, int type, int sectionStartPosition) {
            super(data,type, sectionStartPosition);
        }

        public void setSectionStartPosition(int sectionStartPosition) {
            this.sectionStartPosition = sectionStartPosition;
        }

        public int getSectionStartPosition() {
            return sectionStartPosition;
        }
    }

    public static class AsyncSectionBuilderResult {

        @Nullable
        private List<ListItem> mListItems;

        @Nullable
        private List<?> mItems;

        @Nullable
        private List<?> mHeaders;

        @Nullable
        private Throwable mError;

        public AsyncSectionBuilderResult() {}

        public AsyncSectionBuilderResult(@Nullable List<?> items, @Nullable List<?> headers, @Nullable List<ListItem> listItems) {
            mListItems = listItems;
            mHeaders = headers;
            mItems = items;
        }

        public AsyncSectionBuilderResult(@NonNull Throwable error) {
            mError = error;
        }

        @SuppressWarnings("unchecked")
        public <T> List<T> getHeaders() {
            return (List<T>) mHeaders;
        }

        public List<ListItem> getListItems() {
            return mListItems;
        }

        @SuppressWarnings("unchecked")
        public <T> List<T> getItems() {
            return (List<T>) mItems;
        }

        public Throwable getError() {
            return mError;
        }
    }

    public interface AsyncSectionBuilderCallback {

        void onResult(AsyncSectionBuilderResult result);
    }

    public abstract static class AsyncSectionBuilder<H,I> extends AsyncTask<Void,Void,AsyncSectionBuilderResult> {

        private static final String TAG = AsyncSectionBuilder.class.getSimpleName();

        @Nullable
        private final List<I> mItems;

        @Nullable
        private final List<H> mHeaders;

        /** if set to {@literal true} then headers will be added to list of {@link ListItem} */
        private boolean mAddHeadersAsListItem = true;

        private final ArrayList<AsyncSectionBuilderCallback> mCallbacks = new ArrayList<>();

        public AsyncSectionBuilder(@Nullable List<I> items) {
            this(items,null);
        }

        public AsyncSectionBuilder(@Nullable List<I> items, @Nullable List<H> headers) {
            super();
            this.mItems = items;
            this.mHeaders = headers;
        }

        public void setAddHeaderAsListItem(boolean shouldAdd) {
            mAddHeadersAsListItem = shouldAdd;
        }

        public boolean getAddHeaderAsListItem() {
            return mAddHeadersAsListItem;
        }

        public void addAsyncSectionBuilderCallback(@NonNull AsyncSectionBuilderCallback callback) {
            mCallbacks.add(callback);
        }

        public void removeAsyncSectionBuilderCallback(@NonNull AsyncSectionBuilderCallback callback) {
            mCallbacks.remove(callback);
        }

        void  removeAllAsyncSectionBuilderCallback() {
            mCallbacks.clear();
        }

        @Nullable
        public List<I> getItems() {
            return mItems;
        }

        @Nullable
        public List<H> getHeaders() {
            return mHeaders;
        }

        @Override
        protected AsyncSectionBuilderResult doInBackground(Void... voids) {
            List<I> given_items = this.mItems;
            List<H> given_headers = this.mHeaders;
            try {
                if (null != given_items) {
                    List<I> items = onBeforeBuildSections(given_items);
                    //noinspection ConstantConditions
                    if (items == null) {
                        throw new NullPointerException("onBeforeBuildSections(List) must return non null list");
                    }
                    List<H> headers;
                    if (null == given_headers || given_headers.isEmpty()) {
                        headers = createSectionHeaders(items);
                    } else {
                        headers = given_headers;
                    }
                    List<ListItem> listItems = createListItems(items, headers);
                    onAfterBuildSections(items,headers,listItems);
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,"items: "+items.size()+" headers="+headers.size()+" addHeadersAsListItem="+mAddHeadersAsListItem+
                                " listItems="+listItems.size()+" ");
                    }
                    return new AsyncSectionBuilderResult(items, headers, listItems);
                }
            }
            catch (Throwable error) {
                Log.e(TAG,"doInBackground",error);
                return new AsyncSectionBuilderResult(error);
            }
            return new AsyncSectionBuilderResult();
        }

        /**
         * Called from worker thread before start creating sections. This method is useful
         * when you want to perform some sorting or filtering. The returned list will be used
         * to create headers.
         *
         * @param items given list of items
         */
        @NonNull
        protected List<I> onBeforeBuildSections(@NonNull List<I> items) {
            return items;
        }

        /**
         * Called for each items to create header from the item when no headers supplied in the
         * constructor.
         */
        @NonNull
        protected abstract H onCreateSectionHeader(@NonNull I item);

        /**
         * Checks weather the item comes under the headers or not
         *
         * @return true means item comes under the header, false otherwise
         */
        protected abstract boolean belongsToSection(@NonNull I item, @NonNull H header);

        /**
         * Called for each newly created {@link ListItem} to add extras
         */
        protected void onSetListItemExtras(@NonNull ListItem item) {}

        /**
         * Called from worker thread when sections building is completed and yet to submit the result
         *
         * @param items items returned by {@link #onBeforeBuildSections(List)}
         * @param headers the list of header eiter created or supplied
         * @param listItems the prepared {@link ListItem}s
         * @see #onBeforeBuildSections(List)
         */
        protected void onAfterBuildSections(@NonNull List<I> items, @NonNull List<H> headers, @NonNull List<ListItem> listItems) {}

        private List<H> createSectionHeaders(List<I> items) {
            ArrayList<H> headers = new ArrayList<>();
            for (I item : items) {
                H header = onCreateSectionHeader(item);
                //noinspection ConstantConditions
                if (null == header) {
                    throw new NullPointerException("null section header returned for item="+item);
                }
                headers.add(header);
            }
            return headers;
        }

        private List<ListItem> createListItems(List<I> items, List<H> headers) {
            ArrayList<ListItem> listItems = new ArrayList<>();
            H lastHeader = null;
            int position = 0;
            int sectionStartPosition = 0;
            boolean headerFound;
            for (I item : items) {
                headerFound = false;
                for (H header : headers) {
                    if (belongsToSection(item, header)) {
                        if (!header.equals(lastHeader)) {
                            if (mAddHeadersAsListItem) {
                                ListItem listItem = new SectionedAdapterListItem(header, SECTION_HEADER_TYPE, position);
                                onSetListItemExtras(listItem);
                                listItems.add(listItem);
                                sectionStartPosition = position++;
                            }
                            else {
                                sectionStartPosition = position;
                            }
                            lastHeader = header;
                        }
                        headerFound = true;
                        break;
                    }
                }
                if (!headerFound) {
                    continue;
                }
                ListItem listItem = new SectionedAdapterListItem(item,SECTION_ITEM_TYPE,sectionStartPosition);
                onSetListItemExtras(listItem);
                listItems.add(listItem);
                position++;
            }
            return listItems;
        }

        @Override
        protected final void onPostExecute(AsyncSectionBuilderResult result) {
            for (AsyncSectionBuilderCallback callback : mCallbacks) {
                callback.onResult(result);
            }
        }
    }
}

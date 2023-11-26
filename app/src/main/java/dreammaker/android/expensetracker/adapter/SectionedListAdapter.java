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
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("unused")
public abstract class SectionedListAdapter<H,I,HVH extends RecyclerView.ViewHolder, IVH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SectionedListAdapter";

    public static final int SECTION_HEADER_TYPE = 100;

    public static final int SECTION_ITEM_TYPE = 200;

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

    protected SectionedListAdapter(@NonNull Context context, @NonNull ItemCallback<H,I> callback) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDiffer = new AsyncListDiffer<>(this, new DiffUtil.ItemCallback<ListItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                if (oldItem.getType() == newItem.getType()) {
                    if (SECTION_HEADER_TYPE == oldItem.getType()) {
                        return callback.isSameHeader(oldItem.getData(), newItem.getData());
                    }
                    else {
                        return callback.isSameChild(oldItem.getData(),newItem.getData());
                    }
                }
                return false;
            }

            @Override
            public boolean areContentsTheSame(@NonNull ListItem oldItem, @NonNull ListItem newItem) {
                if (oldItem.getType() == newItem.getType()) {
                    if (SECTION_HEADER_TYPE == oldItem.getType()) {
                        return callback.isHeaderContentSame(oldItem.getData(), newItem.getData());
                    }
                    else {
                        return callback.isChildContentSame(oldItem.getData(),newItem.getData());
                    }
                }
                return false;
            }
        });
    }

    public void submitList(@Nullable List<I> list) {
        if (null != mSectionBuilder) {
            mSectionBuilder.cancel(true);
            mSectionBuilder.setAsyncSectionBuilderCallback(null);
        }
        mSectionBuilder = onCreateSectionBuilder(list);
        mSectionBuilder.setAsyncSectionBuilderCallback(this::onSectionBuilt);
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
        return mLastResult.getHeaders();
    }

    @NonNull
    public <T> T getData(int position) {
        return mDiffer.getCurrentList().get(position).getData();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (SECTION_HEADER_TYPE == viewType) {
            return onCreateSectionHeaderViewHolder(parent,viewType);
        }
        else  {
            return onCreateSectionItemViewHolder(parent,viewType);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (SECTION_HEADER_TYPE == type) {
            onBindSectionHeaderViewHolder((HVH) holder,position);
        }
        else {
            onBindSectionItemViewHolder((IVH) holder,position);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        int type = getItemViewType(position);
        if (SECTION_HEADER_TYPE == type) {
            onBindSectionHeaderViewHolder((HVH) holder,position,payloads);
        }
        else {
            onBindSectionItemViewHolder((IVH) holder,position,payloads);
        }
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

    private void onSectionBuilt(AsyncSectionBuilderResult<H,I> result) {
        Log.d(TAG,"onSectionBuild: listItems="+result.getListItems().size());
        mLastResult = result;
        mDiffer.submitList(result.getListItems());
    }

    public static class ListItem {

        @NonNull
        private final Object data;
        private final int type;

        public ListItem(@NonNull Object data, int type) {
            this.data = data;
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        public <T> T getData() {
            return (T) data;
        }

        public int getType() {
            return type;
        }
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

        private AsyncSectionBuilderCallback<H,I> mCallback;

        public AsyncSectionBuilder(@Nullable List<I> items) {
            this(items,null);
        }

        public AsyncSectionBuilder(@Nullable List<I> items, @Nullable List<H> headers) {
            super();
            this.mItems = items;
            this.mHeaders = headers;
        }

        public void setAsyncSectionBuilderCallback(AsyncSectionBuilderCallback<H,I> callback) {
            mCallback = callback;
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
                    ListItem listItem = new ListItem(currentHeader,SECTION_HEADER_TYPE);
                    listItems.add(listItem);
                    lastHeader = currentHeader;
                }
                ListItem listItem = new ListItem(item,SECTION_ITEM_TYPE);
                listItems.add(listItem);
            }
            return listItems;
        }

        @Override
        protected final void onPostExecute(AsyncSectionBuilderResult<H,I> result) {
            if (null != mCallback) {
                mCallback.onResult(result);
            }
        }
    }
}

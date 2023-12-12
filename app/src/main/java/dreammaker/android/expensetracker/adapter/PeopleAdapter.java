package dreammaker.android.expensetracker.adapter;

import android.content.Context;
import android.util.SparseLongArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.listener.ChoiceModel;
import dreammaker.android.expensetracker.text.TextUtil;

@SuppressWarnings("unused")
public class PeopleAdapter
        extends SectionedListAdapter<String, PersonModel, PeopleAdapter.HeaderViewHolder, PeopleAdapter.ChildViewHolder>
        implements ChoiceModel.Callback {

    private static final String TAG = PeopleAdapter.class.getSimpleName();

    private static final ItemCallback CALLBACK = new ItemCallback() {
        @Override
        protected boolean areItemsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            if (type == SECTION_ITEM_TYPE) {
                return Objects.equals(((PersonModel) oldData).getId(), ((PersonModel) newData).getId());
            }
            return Objects.equals(oldData,newData);
        }

        @Override
        protected boolean areContentsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            return Objects.equals(oldData,newData);
        }
    };

    @Nullable
    private String mQuery;

    private ChoiceModel mChoiceModel;

    private SparseLongArray mChoiceKeyPositionMap;

    public PeopleAdapter(@NonNull Context context) {
        super(context, CALLBACK);
    }

    @Nullable
    public String getQuery() {
        return mQuery;
    }

    @Override
    public void submitList(@Nullable List<PersonModel> list) {
        throw new RuntimeException("use filter(List,String) instead");
    }

    public void filter(@Nullable List<PersonModel> list, @Nullable String query) {
        mQuery = query;
        if (null == list || list.isEmpty()) {
            performSubmit(Collections.emptyList(),Collections.emptyList(),Collections.emptyList());
        }
        else {
            super.submitList(list);
        }
    }

    public void setChoiceModel(ChoiceModel model) {
        mChoiceModel = model;
    }

    public ChoiceModel getChoiceModel() {
        return mChoiceModel;
    }

    @NonNull
    @Override
    public Object getKey(int position) {
        if (null == mChoiceKeyPositionMap) {
            throw new IllegalStateException("no choice key position map found");
        }
        long key = mChoiceKeyPositionMap.get(position,Long.MIN_VALUE);
        if (key == Long.MIN_VALUE) {
            throw new NullPointerException("no choice key exists for position="+position);
        }
        return key;
    }

    @Override
    public int getPosition(@NonNull Object key) {
        long value = (Long) key;
        int index = mChoiceKeyPositionMap.indexOfValue(value);
        return mChoiceKeyPositionMap.keyAt(index);
    }

    @Override
    public boolean isCheckable(int position) {
        return getItemViewType(position) == SECTION_ITEM_TYPE;
    }

    @NonNull
    @Override
    protected AsyncSectionBuilder<String, PersonModel> onCreateSectionBuilder(@Nullable List<PersonModel> list) {
        return null;
    }

    @NonNull
    @Override
    protected HeaderViewHolder onCreateSectionHeaderViewHolder(@NonNull ViewGroup parent, int type) {
        return null;
    }

    @NonNull
    @Override
    protected ChildViewHolder onCreateSectionItemViewHolder(@NonNull ViewGroup parent, int type) {
        return null;
    }

    @Override
    protected void onBindSectionHeaderViewHolder(@NonNull HeaderViewHolder holder, int adapterPosition) {

    }

    @Override
    protected void onBindSectionItemViewHolder(@NonNull ChildViewHolder holder, int adapterPosition) {

    }

    @Override
    protected void onCompleteSectionBuild(@NonNull List<ListItem> listItems, @NonNull List<String> headers, @NonNull List<PersonModel> items) {
        SparseLongArray map = new SparseLongArray(items.size());
        int position = 0;
        for (ListItem item : listItems) {
            if (item.getType() == SECTION_ITEM_TYPE) {
                PersonModel person = item.getData();
                map.put(position,person.getId());
            }
            position++;
        }
        if (map.size() == 0) {
            mChoiceKeyPositionMap = null;
        }
        else {
            mChoiceKeyPositionMap = map;
        }
    }

    public static class HeaderViewHolder extends BaseViewHolder<String> {

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class ChildViewHolder extends BaseViewHolder<PersonModel> {

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void setChecked(boolean checked) {}
    }

    private static class AsyncItemBuild extends AsyncSectionBuilder<String,PersonModel> {

        private final String mQuery;

        private boolean mFirstNameFirst = true;

        public AsyncItemBuild(@Nullable List<PersonModel> items, String query) {
            super(items);
            mQuery = query;
        }

        public void setDisplayFirstNameFirst(boolean firstNameFirst) {
            mFirstNameFirst = firstNameFirst;
        }

        @NonNull
        @Override
        protected List<PersonModel> onBeforeBuildSections(@NonNull List<PersonModel> items) {
            return items;
        }

        @NonNull
        @Override
        protected String onCreateSectionHeader(@NonNull PersonModel item) {
            return TextUtil.getDisplayLabelForPerson(item.getFirstName(),item.getLastName(),mFirstNameFirst,"#");
        }

        @Override
        protected boolean belongsToSection(@NonNull PersonModel item, @NonNull String header) {
            final String expected = onCreateSectionHeader(item);
            return header.equals(expected);
        }
    }
}

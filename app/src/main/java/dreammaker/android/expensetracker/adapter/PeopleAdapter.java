package dreammaker.android.expensetracker.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseLongArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.databinding.LayoutPersonListItemBinding;
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
        AsyncItemBuild build = new AsyncItemBuild(list);
        build.setQuery(mQuery);
        build.setDisplayFirstNameFirst(true);
        return build;
    }

    @NonNull
    @Override
    protected HeaderViewHolder onCreateSectionHeaderViewHolder(@NonNull ViewGroup parent, int type) {
        View view = getLayoutInflater().inflate(R.layout.layout_simple_list_item_1,parent,false);
        HeaderViewHolder holder = new HeaderViewHolder(view);
        holder.setAdapter(this);
        return holder;
    }

    @NonNull
    @Override
    protected ChildViewHolder onCreateSectionItemViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutPersonListItemBinding binding = LayoutPersonListItemBinding.inflate(getLayoutInflater(),parent,false);
        ChildViewHolder holder = new ChildViewHolder(binding);
        holder.setAdapter(this);
        return holder;
    }

    @Override
    protected void onBindSectionHeaderViewHolder(@NonNull HeaderViewHolder holder, int adapterPosition) {
        holder.bind(getData(adapterPosition));
    }

    @Override
    protected void onBindSectionItemViewHolder(@NonNull ChildViewHolder holder, int adapterPosition) {
        holder.bind(getData(adapterPosition));
        holder.setChecked(mChoiceModel.isChecked(adapterPosition));
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

        private final TextView text1;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = findViewById(R.id.text1);
        }

        @Override
        protected void onBindNonNull(@NonNull String item) {
            text1.setText(item);
        }
    }

    public static class ChildViewHolder extends BaseViewHolder<PersonModel> {

        private final LayoutPersonListItemBinding mBinding;

        public ChildViewHolder(LayoutPersonListItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        @Override
        protected void onBindNonNull(@NonNull PersonModel item) {
            String displayName = TextUtil.getDisplayNameForPerson(item.getFirstName(),item.getLastName(),true,getContext().getString(R.string.label_unknown));
            Drawable placeholder = getDefaultDrawable(displayName,item,true);
            mBinding.name.setText(displayName);
            mBinding.photo.setImageDrawable(placeholder);
            mBinding.due.setText(item.getDue().toString());
            mBinding.borrow.setText(item.getBorrow().toString());
        }

        private Drawable getDefaultDrawable(String displayName, PersonModel person, boolean firstNameFirst) {
            int color = ColorGenerator.MATERIAL.getColor(displayName);
            String text = TextUtil.getDisplayLabelForPerson(person.getFirstName(),person.getLastName(),firstNameFirst,"");
            return TextDrawable.builder()
                    .beginConfig().toUpperCase().endConfig()
                    .buildRound(text,color);
        }

        public void setChecked(boolean checked) {}
    }

    private static class AsyncItemBuild extends AsyncSectionBuilder<String,PersonModel> {

        private static final String HEADER_OTHERS = "#";

        private String mQuery;

        private boolean mFirstNameFirst = true;

        public AsyncItemBuild(@Nullable List<PersonModel> items) {
            super(items);
        }

        public void setQuery(String mQuery) {
            this.mQuery = mQuery;
        }

        public void setDisplayFirstNameFirst(boolean firstNameFirst) {
            mFirstNameFirst = firstNameFirst;
        }

        @NonNull
        @Override
        protected List<PersonModel> onBeforeBuildSections(@NonNull List<PersonModel> items) {
            List<PersonModel> people;
            if (TextUtils.isEmpty(mQuery)) {
                people = items;
            }
            else {
                people = filter(items,mQuery);
            }
            sort(people);
            return people;
        }

        private List<PersonModel> filter(List<PersonModel> people, String query) {
            ArrayList<PersonModel> filtered = new ArrayList<>();
            for (PersonModel person : people) {
                String firstName = person.getFirstName();
                String lastName = person.getLastName();
                if (TextUtil.containsIgnoreCase(firstName,query) || TextUtil.containsIgnoreCase(lastName,query)) {
                    filtered.add(person);
                }
            }
            return filtered;
        }

        private void sort(List<PersonModel> people) {
            people.sort((left,right)->{
                String displayNameLeft = TextUtil.getDisplayNameForPerson(left.getFirstName(),left.getLastName(),mFirstNameFirst,null);
                String displayNameRight = TextUtil.getDisplayNameForPerson(right.getFirstName(),right.getLastName(),mFirstNameFirst,null);
                if (TextUtils.isEmpty(displayNameLeft) && TextUtils.isEmpty(displayNameRight)) {
                    return 0;
                }
                return displayNameLeft.compareToIgnoreCase(displayNameRight);
            });
        }

        @NonNull
        @Override
        protected String onCreateSectionHeader(@NonNull PersonModel item) {
            final String label = TextUtil.getDisplayLabelForPerson(item.getFirstName(),item.getLastName(),mFirstNameFirst, HEADER_OTHERS);
            String header;
            if (label.length() > 1) {
                header = label.substring(0,1);
            }
            else {
                header = label;
            }
            return header;
        }

        @Override
        protected boolean belongsToSection(@NonNull PersonModel item, @NonNull String header) {
            final String expected = onCreateSectionHeader(item);
            return header.equals(expected);
        }
    }
}

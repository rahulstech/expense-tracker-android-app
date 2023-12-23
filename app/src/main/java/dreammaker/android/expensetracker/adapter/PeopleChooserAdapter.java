package dreammaker.android.expensetracker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.databinding.LayoutPersonListItemBinding;
import dreammaker.android.expensetracker.drawable.CheckableDrawableWrapper;
import dreammaker.android.expensetracker.drawable.DrawableUtil;
import dreammaker.android.expensetracker.text.SpannableStringUtil;
import dreammaker.android.expensetracker.text.Spans;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ResourceUtil;

public class PeopleChooserAdapter
        extends BaseOnlySectionItemCheckableAdapter<Integer,PersonModel, PeopleChooserAdapter.HeaderViewHolder, PeopleChooserAdapter.ChildViewHolder> {

    private static final ItemCallback CALLBACK = new ItemCallback() {
        @Override
        protected boolean areItemsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            if (type == SECTION_ITEM_TYPE) {
                return Objects.equals(((PersonModel) oldData).getId(),((PersonModel) newData).getId());
            }
            return Objects.equals(oldData,newData);
        }

        @Override
        protected boolean areContentsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            return Objects.equals(oldData,newData);
        }
    };

    public static final int HEADER_FREQUENTLY_USED = 1;

    public static final int HEADER_OTHERS = 2;

    private final List<Integer> mHeaders = Arrays.asList(HEADER_FREQUENTLY_USED,HEADER_OTHERS);

    private final int mHighlightColor;

    private String mQuery;

    public PeopleChooserAdapter(@NonNull Context context) {
        super(context, CALLBACK);
        mHighlightColor = ResourceUtil.getThemeColor(context,R.attr.colorSecondary);
    }

    @Override
    public void submitList(@Nullable List<PersonModel> list) {
        throw new RuntimeException("use filter(List,String) instead");
    }

    public void filter(@Nullable List<PersonModel> list,String query) {
        mQuery = query;
        if (null == list || list.isEmpty()) {
            performSubmit(Collections.emptyList(),Collections.emptyList(),Collections.emptyList());
        }
        else{
            super.submitList(list);
        }
    }

    public String getQuery() {
        return mQuery;
    }

    private int getHighlightColor() {
        return mHighlightColor;
    }

    private boolean isFirstNameFirst() {
        return false;
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == SECTION_ITEM_TYPE) {
            return ((PersonModel) getData(position)).getId();
        }
        return RecyclerView.NO_ID;
    }

    @NonNull
    @Override
    protected Object getChoiceKeyFromData(PersonModel data) {
        return data.getId();
    }

    @NonNull
    @Override
    protected AsyncSectionBuilder<Integer, PersonModel> onCreateSectionBuilder(@Nullable List<PersonModel> list) {
        AsyncItemBuild build = new AsyncItemBuild(list,mHeaders);
        build.setQuery(getQuery());
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
        holder.setChecked(getChoiceModel().isChecked(adapterPosition));
    }

    public static class HeaderViewHolder extends BaseViewHolder<Integer> {

        private final TextView text1;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = findViewById(R.id.text1);
        }

        @Override
        protected void onBindNonNull(@NonNull Integer item) {
            if (HEADER_FREQUENTLY_USED == item) {
                text1.setText(R.string.label_frequently_used);
            }
            else {
                text1.setText(R.string.label_others);
            }
        }
    }

    public class ChildViewHolder extends BaseViewHolder<PersonModel> {

        private final LayoutPersonListItemBinding mBinding;

        private CheckableDrawableWrapper mWrapper;

        public ChildViewHolder(LayoutPersonListItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        @Override
        protected void onBindNonNull(@NonNull PersonModel item) {
            String displayName = TextUtil.getDisplayNameForPerson(item.getFirstName(),item.getLastName(),isFirstNameFirst(),getContext().getString(R.string.label_unknown));
            Drawable placeholder = DrawableUtil.getPersonDefaultPhoto(item.getFirstName(),item.getLastName(),isFirstNameFirst());
            mWrapper = new CheckableDrawableWrapper(getContext(),placeholder);
            mBinding.name.setText(highlight(displayName,getQuery()));
            mBinding.photo.setImageDrawable(mWrapper);
            mBinding.due.setText(item.getDue().toString());
            mBinding.borrow.setText(item.getBorrow().toString());
        }

        public void setChecked(boolean checked) {
            mWrapper.setChecked(checked);
        }

        private CharSequence highlight(CharSequence text, CharSequence phrase) {
            if (TextUtils.isEmpty(phrase)) {
                return text;
            }
            int start = TextUtil.indexOfIgnoreCase(text,phrase);
            int end = start+phrase.length();
            int color = getHighlightColor();
            Object span = Spans.textColor(color);
            return new SpannableStringUtil()
                    .append(text,span,start,end)
                    .toSpannableString();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncItemBuild extends AsyncSectionBuilder<Integer,PersonModel> {

        private List<PersonModel> mFrequentlyUsed;

        private String mQuery;

        public AsyncItemBuild(@Nullable List<PersonModel> items, @Nullable List<Integer> headers) {
            super(items,headers);
        }

        public void setQuery(String query) {
            this.mQuery = query;
        }

        @NonNull
        @Override
        protected List<PersonModel> onBeforeBuildSections(@NonNull List<PersonModel> items) {
            String query = this.mQuery;
            List<PersonModel> people;
            if (TextUtils.isEmpty(query)) {
                people = items;
            }
            else {
                people = filter(items,query);
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

        private void sort(List<PersonModel> items) {
            if (items.isEmpty()) {
                return;
            }
            items.sort(Comparator.comparingInt(PersonModel::getUsageCount).reversed());
            if (items.size() > Constants.FREQUENTLY_USED_DISPLAY_COUNT) {
                ArrayList<PersonModel> frequent = new ArrayList<>();
                for (int i=0; i<Constants.FREQUENTLY_USED_DISPLAY_COUNT; i++) {
                    PersonModel person = items.get(i);
                    if (person.getUsageCount() > 0) {
                        frequent.add(items.get(i));
                    }
                }
                if (!frequent.isEmpty()) {
                    mFrequentlyUsed = frequent;
                }
            }
        }

        @NonNull
        @Override
        protected Integer onCreateSectionHeader(@NonNull PersonModel item) {
            if (null != mFrequentlyUsed && mFrequentlyUsed.contains(item)) {
                return HEADER_FREQUENTLY_USED;
            }
            return HEADER_OTHERS;
        }

        @Override
        protected boolean belongsToSection(@NonNull PersonModel item, @NonNull Integer header) {
            return header.equals(onCreateSectionHeader(item));
        }

        @Override
        protected void onAfterBuildSections(@NonNull List<PersonModel> items, @NonNull List<Integer> headers, @NonNull List<ListItem> listItems) {
            SparseArrayCompat<Object> map = prepareChoiceKeyMap(listItems);
            if (!isCancelled()) {
                postChoiceKeyMap(map);
            }
        }
    }
}

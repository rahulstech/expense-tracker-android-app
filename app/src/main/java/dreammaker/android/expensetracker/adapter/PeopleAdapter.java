package dreammaker.android.expensetracker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.databinding.LayoutPersonListItemBinding;
import dreammaker.android.expensetracker.drawable.CheckableDrawableWrapper;
import dreammaker.android.expensetracker.drawable.DrawableUtil;
import dreammaker.android.expensetracker.text.SpannableStringUtil;
import dreammaker.android.expensetracker.text.Spans;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.ResourceUtil;

@SuppressWarnings("unused")
public class PeopleAdapter
        extends BaseOnlySectionItemCheckableAdapter<String, PersonModel, PeopleAdapter.HeaderViewHolder, PeopleAdapter.ChildViewHolder> {

    private static final String TAG = PeopleAdapter.class.getSimpleName();

    private static final ItemCallback CALLBACK = new ItemCallback() {
        @Override
        protected boolean areItemsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            if (type == SECTION_ITEM_TYPE) {
                return Objects.equals(((PersonModel) oldData).getId(), ((PersonModel) newData).getId());
            }
            return oldData.hashCode() == newData.hashCode();
        }

        @Override
        protected boolean areContentsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            return Objects.equals(oldData,newData);
        }
    };

    private final int mHighlightColor;

    private String mQuery;

    public PeopleAdapter(@NonNull Context context) {
        super(context, CALLBACK);
        mHighlightColor = ResourceUtil.getThemeColor(context,R.attr.colorSecondary);
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

    @NonNull
    @Override
    protected Object getChoiceKeyFromData(PersonModel data) {
        return data.getId();
    }

    @NonNull
    @Override
    protected AsyncSectionBuilder<String, PersonModel> onCreateSectionBuilder(@Nullable List<PersonModel> list) {
        AsyncItemBuild build = new AsyncItemBuild(list);
        build.setQuery(getQuery());
        build.setFirstNameFirst(isFirstNameFirst());
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
        Log.d(TAG, "onBindSectionItemViewHolder: position="+adapterPosition+" checked="+getChoiceModel().isChecked(adapterPosition));
        holder.setChecked(getChoiceModel().isChecked(adapterPosition));
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
            mBinding.due.setText(TextUtil.prettyFormatCurrency(getRealDue(item)));
            mBinding.borrow.setText(TextUtil.prettyFormatCurrency(getRealBorrow(item)));
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

        private Currency getRealDue(PersonModel person) {
            Currency due = person.getDue();
            Currency borrow = person.getBorrow();
            Currency realDue = Currency.ZERO;
            if (!due.isNegative()) {
                realDue = realDue.add(due);
            }
            if (borrow.isNegative()) {
                realDue = realDue.add(borrow.negate());
            }
            return realDue;
        }

        private Currency getRealBorrow(PersonModel person) {
            Currency due = person.getDue();
            Currency borrow = person.getBorrow();
            Currency realBorrow = Currency.ZERO;
            if (due.isNegative()){
                realBorrow = realBorrow.add(due.negate());
            }
            if (!borrow.isNegative()) {
                realBorrow = realBorrow.add(borrow);
            }
            return realBorrow;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AsyncItemBuild extends AsyncSectionBuilder<String,PersonModel> {

        private String mQuery;

        private boolean mFirstNameFirst = true;

        public AsyncItemBuild(@Nullable List<PersonModel> items) {
            super(items);
        }

        public void setQuery(String query) {
            this.mQuery = query;
        }

        public void setFirstNameFirst(boolean isFirst) {
            this.mFirstNameFirst = isFirst;
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

        private void sort(List<PersonModel> people) {
            ArrayList<PersonModel> sublist = new ArrayList<>();
            for (PersonModel person : people) {
                String displayName = TextUtil.getDisplayNameForPerson(person.getFirstName(),person.getLastName(),mFirstNameFirst,null);
                if (null == displayName) {
                    sublist.add(person);
                    continue;
                }
                int code0 = displayName.codePointAt(0);
                if (!TextUtil.isLetter(code0)) {
                    sublist.add(person);
                }
            }

            people.removeAll(sublist);

            people.sort((left,right)->{
                String displayNameLeft = TextUtil.getDisplayNameForPerson(left.getFirstName(),left.getLastName(),mFirstNameFirst,null);
                String displayNameRight = TextUtil.getDisplayNameForPerson(right.getFirstName(),right.getLastName(),mFirstNameFirst,null);
                return displayNameLeft.compareToIgnoreCase(displayNameRight);
            });

            sublist.sort((left,right)->{
                String displayNameLeft = TextUtil.getDisplayNameForPerson(left.getFirstName(),left.getLastName(),mFirstNameFirst,null);
                String displayNameRight = TextUtil.getDisplayNameForPerson(right.getFirstName(),right.getLastName(),mFirstNameFirst,null);
                boolean emptyLeft = TextUtils.isEmpty(displayNameLeft);
                boolean emptyRight = TextUtils.isEmpty(displayNameRight);
                if (emptyLeft && emptyRight) {
                    return 0;
                }
                else if (emptyLeft || emptyRight) {
                    return -1;
                }
                else {
                    return displayNameLeft.compareToIgnoreCase(displayNameRight);
                }
            });

            people.addAll(sublist);
        }

        @NonNull
        @Override
        protected String onCreateSectionHeader(@NonNull PersonModel item) {
            final String label = TextUtil.getDisplayLabelForPerson(item.getFirstName(),item.getLastName(),mFirstNameFirst);
            int code0 = label.codePointAt(0);
            if (!TextUtil.isLetter(code0)) {
                return TextUtil.DEFAULT_DISPLAY_LABEL_NON_LETTER;
            }
            return label.substring(0,1);
        }

        @Override
        protected boolean belongsToSection(@NonNull PersonModel item, @NonNull String header) {
            final String expected = onCreateSectionHeader(item);
            return header.equals(expected);
        }

        @Override
        protected void onAfterBuildSections(@NonNull List<PersonModel> items, @NonNull List<String> headers, @NonNull List<ListItem> listItems) {
            SparseArrayCompat<Object> map = prepareChoiceKeyMap(listItems);
            if (!isCancelled()) {
                postChoiceKeyMap(map);
            }
        }
    }
}

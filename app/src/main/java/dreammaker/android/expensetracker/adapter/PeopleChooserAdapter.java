package dreammaker.android.expensetracker.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.listener.ChoiceModel;

public class PeopleChooserAdapter
        extends SectionedListAdapter<String,PersonModel, PeopleChooserAdapter.SectionHeaderViewHolder, PeopleChooserAdapter.SectionItemViewHolder>
        implements ChoiceModel.Callback  {

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

    private String mQuery;

    private ChoiceModel mChoiceModel;

    public PeopleChooserAdapter(@NonNull Context context) {
        super(context, CALLBACK);
    }

    public void setQuery(String query) {
        mQuery = query;
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

    public void setChoiceModel(ChoiceModel model) {
        mChoiceModel = model;
    }

    public ChoiceModel getChoiceModel() {
        return mChoiceModel;
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
    protected AsyncSectionBuilder<String, PersonModel> onCreateSectionBuilder(@Nullable List<PersonModel> list) {
        return null;
    }

    @NonNull
    @Override
    protected SectionHeaderViewHolder onCreateSectionHeaderViewHolder(@NonNull ViewGroup parent, int type) {
        return null;
    }

    @NonNull
    @Override
    protected SectionItemViewHolder onCreateSectionItemViewHolder(@NonNull ViewGroup parent, int type) {
        return null;
    }

    @Override
    protected void onBindSectionHeaderViewHolder(@NonNull SectionHeaderViewHolder holder, int adapterPosition) {
        holder.bind(getData(adapterPosition));
    }

    @Override
    protected void onBindSectionItemViewHolder(@NonNull SectionItemViewHolder holder, int adapterPosition) {
        holder.bind(getData(adapterPosition));
        holder.setChecked(mChoiceModel.isChecked(adapterPosition));
    }

    @NonNull
    @Override
    public Object getKey(int position) {
        return null;
    }

    @Override
    public int getPosition(@NonNull Object key) {
        return 0;
    }

    @Override
    public boolean isCheckable(int position) {
        return false;
    }


    public static class SectionHeaderViewHolder extends BaseViewHolder<String> {

        public SectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected void onBindNonNull(@NonNull String item) {

        }
    }

    public static class SectionItemViewHolder extends BaseViewHolder<PersonModel> {

        public SectionItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected void onBindNonNull(@NonNull PersonModel item) {

        }

        public void setChecked(boolean checked) {

        }
    }

    private class AsyncItemBuilder extends SectionedListAdapter.AsyncSectionBuilder<String,PersonModel> {

        public AsyncItemBuilder(@Nullable List<PersonModel> items, @Nullable List<String> headers) {
            super(items, headers);
        }

        @NonNull
        @Override
        protected List<PersonModel> onBeforeBuildSections(@NonNull List<PersonModel> items) {
            String query = mQuery;
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

        private List<PersonModel> filter(List<PersonModel> items, String query) {
            return items;
        }

        private void sort(List<PersonModel> list) {

        }

        @NonNull
        @Override
        protected String onCreateSectionHeader(@NonNull PersonModel item) {
            return null;
        }

        @Override
        protected boolean belongsToSection(@NonNull PersonModel item, @NonNull String header) {
            return false;
        }
    }
}

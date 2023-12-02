package dreammaker.android.expensetracker.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.database.model.PersonModel;

@SuppressWarnings("unused")
public class PeopleAdapter extends SectionedListAdapter<String, PersonModel, PeopleAdapter.HeaderViewHolder, PeopleAdapter.ChildViewHolder> {

    private static final String TAG = PeopleAdapter.class.getSimpleName();

    private static final ItemCallback CALLBACK = new ItemCallback() {
        @Override
        protected boolean areItemsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            return false;
        }

        @Override
        protected boolean areContentsTheSame(int type, @NonNull Object oldData, @NonNull Object newData) {
            return false;
        }
    };

    @Nullable
    private String mQuery;

    public PeopleAdapter(@NonNull Context context) {
        super(context, CALLBACK);
        setHasListFooter(true);
    }

    @Nullable
    public String getQuery() {
        return mQuery;
    }

    public void filter(@Nullable List<PersonModel> list, @Nullable String query) {
        mQuery = query;
        submitList(list);
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

    public static class HeaderViewHolder extends BaseViewHolder<String> {

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class ChildViewHolder extends BaseViewHolder<PersonModel> {

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

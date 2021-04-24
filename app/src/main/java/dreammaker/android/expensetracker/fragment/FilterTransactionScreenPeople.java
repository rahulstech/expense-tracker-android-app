package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.Person;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.view.AbsSelectionListAdapter;
import dreammaker.android.expensetracker.view.PeopleSelectionAdapter;
import dreammaker.android.expensetracker.viewmodel.TransactionsViewModel;

public class FilterTransactionScreenPeople extends BaseFragment<FilterTransactionScreenPeople.FilterTransactionsScreenPeopleViewHolder>
        implements AbsSelectionListAdapter.OnItemSelectionChangeListener {

    private static final String TAG = "FilterTransactionsScreenPeople";

    private TransactionsViewModel viewModel;
    private PeopleSelectionAdapter adapter;

    @Override
    public void onItemSelectionChange(int position, boolean checked) {
        Person item = adapter.getItem(position);
        if (checked) {
            viewModel.getWorkingFilterParams().addSelectedPerson(item);
        }
        else {
            viewModel.getWorkingFilterParams().removeSelectedPerson(item);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setFragmentTitle(R.string.label_filter_transaction);
        if (Check.isNonNull(getActivity())) {
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(TransactionsViewModel.class);
        }
    }

    @NonNull
    @Override
    protected FilterTransactionsScreenPeopleViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return new FilterTransactionsScreenPeopleViewHolder(
                inflater.inflate(R.layout.screen_searchable_list_content, container, false));
    }

    @Override
    protected void onFragmentViewHolderCreated(@NonNull FilterTransactionsScreenPeopleViewHolder vh) {
        adapter = new PeopleSelectionAdapter(getContext());
        adapter.setOnItemSelectionChangeListener(this);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() { vh.configEmptyList(adapter.isEmpty()); }

            @Override
            public void onInvalidated() { vh.configEmptyList(adapter.isEmpty()); }
        });
        vh.search.setQueryHint(getString(R.string.search_person));
        vh.list.setAdapter(adapter);
        vh.empty.setText(R.string.no_person);

        vh.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (Check.isEmptyString(newText)){
                    adapter.getFilter().filter(null);
                    return true;
                }
                return false;
            }
        });
        viewModel.getAllPersonNamAndId().observe(this, this::onPeopleLoaded);
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull FilterTransactionsScreenPeopleViewHolder vh) {}

    private void onPeopleLoaded(List<Person> people) {
        adapter.changeList(people);
        adapter.setCheckedItems(viewModel.getWorkingFilterParams().getSelectedPeople());
    }

    static class FilterTransactionsScreenPeopleViewHolder extends BaseFragment.FragmentViewHolder {
        SearchView search;
        ListView list;
        TextView empty;

        FilterTransactionsScreenPeopleViewHolder(@NonNull View root) {
            super(root);

            search = findViewById(R.id.search);
            list = findViewById(R.id.list);
            empty = findViewById(R.id.empty);
        }

        void configEmptyList( boolean isEmpty){
            if (isEmpty){
                list.setVisibility(View.GONE);
                empty.setVisibility(View.VISIBLE);
            }
            else {
                empty.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
            }
        }
    }
}

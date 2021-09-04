package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.MainActivity;
import dreammaker.android.expensetracker.database.AboutPerson;
import dreammaker.android.expensetracker.database.Person;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Helper;
import dreammaker.android.expensetracker.view.BaseRecyclerViewListAdapterFilterable;
import dreammaker.android.expensetracker.view.OnItemChildClickListener;
import dreammaker.android.expensetracker.view.PersonsAdapter;
import dreammaker.android.expensetracker.viewmodel.PersonsViewModel;
import dreammaker.android.expensetracker.viewmodel.TransactionsViewModel;

import static dreammaker.android.expensetracker.BuildConfig.DEBUG;

public class PersonsList extends BaseListFragment<BaseListFragment.ListFragmentViewHolder> implements OnItemChildClickListener<PersonsAdapter, PersonsAdapter.PersonViewHolder> {

    private static final String TAG = "PersonsList";

    private static final int ID_SAVED_DATE = 957;

    private PersonsViewModel viewModel;
    private TransactionsViewModel transactionsViewModel;
    private PersonsAdapter adapter;
    private PersonListSaveData saveData;
    private NavController navController;

    public PersonsList() { super(); }

    public NavController getNavController() { return navController; }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setFragmentTitle(R.string.label_persons_list);
        if (Check.isNonNull(getActivity())){
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(PersonsViewModel.class);
            transactionsViewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(TransactionsViewModel.class);
            saveData = viewModel.getSavedData(ID_SAVED_DATE, new PersonListSaveData());
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (isViewHolderCreated()) {
            saveData.adapterSaveData = adapter.onSaveData();
            saveData.queryPerson = getQuery();
            viewModel.putSavedData(ID_SAVED_DATE, saveData);
        }
    }

    @NonNull
    @Override
    protected ListFragmentViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        View v = inflater.inflate(R.layout.list_content, container, false);
        ListFragmentViewHolder vh = new ListFragmentViewHolder(v);
        adapter = new PersonsAdapter(getContext());
        adapter.setOnItemChildClickListener(this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() { configureEmptyContent(adapter.isEmpty()); }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) { configureEmptyContent(adapter.isEmpty()); }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) { configureEmptyContent(adapter.isEmpty()); }
        });
        vh.setEmptyText(R.string.no_person);
        vh.list.setAdapter(adapter);
        vh.setOnAddListener(v1 -> onAddPerson());
        viewModel.getPersons().observe(this, this::onAboutPersonFetched);
        return vh;
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull ListFragmentViewHolder vh) {
        navController = Navigation.findNavController(vh.getRoot());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.person_list_menu, menu);
        onPrepareSearchMenu(menu.findItem(R.id.search_person), R.string.search_person, saveData.queryPerson);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        super.onQueryTextChange(newText);
        if (Check.isEmptyString(newText)){
            onFilter(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        onFilter(query);
        return true;
    }

    private void onFilter(String key){
        adapter.getFilter().filter(key); }

    @Override
    public void onItemChildClicked(PersonsAdapter personsAdapter, PersonsAdapter.PersonViewHolder vh, View v) {
        if (null == getContext()) return;
        final AboutPerson person = adapter.getItem(vh.getAbsoluteAdapterPosition());
        Log.d(TAG,"selected person "+person);
        if (vh.options == v){
            PopupMenu menu = new PopupMenu(getContext(), v);
            menu.inflate(R.menu.person_list_item_options_menu);
            menu.setOnMenuItemClickListener(item -> {
                viewModel.setSelectedPerson(person);
                final int itemId = item.getItemId();
                if (R.id.edit == itemId) {
                    onEditPerson(person);
                    return true;
                }
                else if (R.id.delete == itemId) {
                    onDeletePerson(person);
                    return true;
                }
                return false;
            });
            menu.show();
        }
        else if (vh.getRoot() == v) {
            onViewTransactions(person);
        }
    }

    private void onAboutPersonFetched(List<AboutPerson> people){
        adapter.submitList(people);
        adapter.onRestoreData(saveData.adapterSaveData);
    }

    private void onAddPerson(){
        viewModel.setSelectedPerson(null);
        getNavController().navigate(R.id.action_personsList_to_inputPerson);
    }

    private void onEditPerson(AboutPerson person){
        viewModel.setSelectedPerson(person);
        getNavController().navigate(R.id.action_personsList_to_inputPerson);
    }

    private void onDeletePerson(final Person person){
        if (null == getContext()) return;
        new AlertDialog.Builder(getContext())
                .setMessage(getResources().getQuantityString(R.plurals.warning_delete_persons, 1))
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(android.R.string.ok, (di, which) ->{
                    viewModel.setSelectedPerson(null);
                    viewModel.deletePerson(person);
                })
                .show();
    }

    private void onViewTransactions(Person person) {
        transactionsViewModel.loadTransactionsForPerson(person);
        getNavController().navigate(R.id.action_personsList_to_transactionsList);
    }

    private static class PersonListSaveData {
        String queryPerson;
        BaseRecyclerViewListAdapterFilterable.BaseRecyclerViewListAdapterFilterableSaveData adapterSaveData;
    }
}

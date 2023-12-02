package dreammaker.android.expensetracker.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.PersonDisplayModel;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.view.adapter.PeopleListAdapter;
import dreammaker.android.expensetracker.viewmodel.PersonsViewModel;
import dreammaker.android.expensetracker.viewmodel.SavedStateViewModel;

@Deprecated
public class PersonsList extends Fragment {

    private static final String TAG = "PersonsList";

    private static final String KEY_QUERY_STRING = "query_string";

    private static final int SEARCH_KEY_LENGTH_THRESHOLD = 2;

    private PersonsViewModel viewModel;
    private SavedStateViewModel mSavedState;
    private NavController navController;

    private RecyclerView list;
    private TextView emptyView;
    private FloatingActionButton btnAdd;
    private PeopleListAdapter peopleAdapter;
    private String mQueryString = null;

    public PersonsList() { super(); }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        requireActivity().setTitle(R.string.label_people);
        return inflater.inflate(R.layout.list_content,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        viewModel = new ViewModelProvider(requireActivity(),new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(PersonsViewModel.class);
        mSavedState = new ViewModelProvider(this).get(SavedStateViewModel.class);
        list = view.findViewById(R.id.list);
        emptyView = view.findViewById(R.id.empty);
        btnAdd = view.findViewById(R.id.add);
        emptyView.setText(R.string.no_person);
        btnAdd.setOnClickListener(v -> onAddPerson());
        peopleAdapter = new PeopleListAdapter(requireContext());
        peopleAdapter.setOnRecyclerViewItemClickListener((adapter,child,position)->onClickPersonItem(child,position));
        peopleAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                onDataSetChanged();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                onDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                onDataSetChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                onDataSetChanged();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                onDataSetChanged();
            }
        });
        list.setAdapter(peopleAdapter);
        mQueryString = mSavedState.getString(KEY_QUERY_STRING);
        viewModel.getAllPeopleForDisplay().observe(getViewLifecycleOwner(),this::onPeopleFetched);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSavedState.put(KEY_QUERY_STRING,mQueryString);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // TODO: need to find better search ui
        inflater.inflate(R.menu.person_list_menu, menu);
        SearchView search = (SearchView) menu.findItem(R.id.search_person).getActionView();
        search.setQuery(mQueryString,false);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {return true;}

            @Override
            public boolean onQueryTextChange(String newText) {
                onFilter(newText);
                return true;
            }
        });
    }

    private void onFilter(String key){
        mQueryString = key;
        if (TextUtils.isEmpty(key)) {
            peopleAdapter.getFilter().filter(null);
        }
        else if (key.length() > SEARCH_KEY_LENGTH_THRESHOLD) {
            peopleAdapter.getFilter().filter(key);
        }
    }

    private void onDataSetChanged() {
        if (peopleAdapter.isEmpty()) {
            list.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else {
            emptyView.setVisibility(View.INVISIBLE);
            list.setVisibility(View.VISIBLE);
        }
    }

    private void onClickPersonItem(@NonNull View child, int position) {
        PersonDisplayModel person = peopleAdapter.getItem(position);
        int childId = child.getId();
        if (R.id.options == childId){
            PopupMenu menu = new PopupMenu(requireContext(),child);
            menu.inflate(R.menu.person_list_item_options_menu);
            menu.setOnMenuItemClickListener(item -> {
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
        else {
            onViewTransactions(person);
        }
    }

    private void onPeopleFetched(@Nullable List<PersonDisplayModel> people) {
        peopleAdapter.setOriginalItems(people);
        peopleAdapter.submitList(people);
        onFilter(mQueryString);
    }

    private void onAddPerson(){
        navController.navigate(R.id.action_personsList_to_inputPerson);
    }

    @Deprecated
    private void onEditPerson(@NonNull PersonDisplayModel person){
        Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_PERSON,person.getId());
        navController.navigate(R.id.action_personsList_to_inputPerson,args);
    }

    @Deprecated
    private void onDeletePerson(@NonNull PersonDisplayModel person){
        new AlertDialog.Builder(requireContext())
                .setMessage(getResources().getQuantityString(R.plurals.warning_delete_persons, 1))
                .setPositiveButton(android.R.string.cancel, null)
                .setNegativeButton(android.R.string.ok, (di, which) ->{
                    viewModel.removeMultiplePerson("delete_person", Arrays.asList(person.getId()),result -> {});
                })
                .show();
    }

    @Deprecated
    private void onViewTransactions(@NonNull PersonDisplayModel person) {
        Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_PERSON,person.getId());
        navController.navigate(R.id.action_personsList_to_transactionsList,args);
    }
}

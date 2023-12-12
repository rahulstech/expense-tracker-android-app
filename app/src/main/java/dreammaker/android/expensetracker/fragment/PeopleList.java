package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.adapter.PeopleAdapter;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.databinding.LayoutBrowseSearchAddBinding;
import dreammaker.android.expensetracker.listener.ChoiceModel;
import dreammaker.android.expensetracker.listener.ModalChoiceModeListener;
import dreammaker.android.expensetracker.listener.OnItemClickListener;
import dreammaker.android.expensetracker.viewmodel.PeopleListViewModel;

@SuppressWarnings("unused")
public class PeopleList extends Fragment implements OnItemClickListener, ModalChoiceModeListener {

    private static final String TAG = "PersonsList";

    private static final String KEY_QUERY_STRING = "query_string";

    private NavController navController;

    @SuppressWarnings("FieldCanBeLocal")
    private PeopleListViewModel mViewModel;

    private ChoiceModel.SavedStateViewModel mChoiceModelSavedState;

    private LayoutBrowseSearchAddBinding mBinding;

    private PeopleAdapter mAdapter;

    private ChoiceModel mChoiceModel;

    private List<PersonModel> mPeopleList;

    private String mQueryString = null;

    public PeopleList() { super(); }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(PeopleListViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = LayoutBrowseSearchAddBinding.inflate(inflater,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        navController = Navigation.findNavController(view);
        mViewModel.getAllPeople().observe(getViewLifecycleOwner(),this::onPeopleLoadingComplete);
        mChoiceModelSavedState = new ViewModelProvider(this).get(ChoiceModel.SavedStateViewModel.class);
        mBinding.search.setHint(R.string.search_person);
        mBinding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                submitQuery(s.toString());
            }
        });
        mBinding.add.setContentDescription(getText(R.string.description_add_person));
        mBinding.add.setOnClickListener(v -> onClickAddPerson());
        mAdapter = new PeopleAdapter(requireContext());
        mBinding.list.setAdapter(mAdapter);
        mChoiceModel = new ChoiceModel(mBinding.list,mAdapter);
        mChoiceModel.setModalChoiceModeListener(this);
        mChoiceModel.setChoiceMode(ChoiceModel.CHOICE_MODE_MULTIPLE_MODAL);
        mChoiceModel.setOnItemClickListener(this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mQueryString = savedInstanceState.getString(KEY_QUERY_STRING,null);
        }
        mChoiceModel.onRestoreInstanceState(mChoiceModelSavedState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_QUERY_STRING,mQueryString);
        mChoiceModel.onSaveInstanceState(mChoiceModelSavedState);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        updateActionMode(mode);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {}

    @Override
    public void onItemChecked(@NonNull ActionMode mode, @NonNull View view, int position, boolean checked) {
        updateActionMode(mode);
    }

    private void setTitle() {

    }

    private void updateActionMode(ActionMode mode) {
        mode.setTitle(getString(R.string.message_selection_count,mChoiceModel.getCheckedCount()));
    }

    private void onPeopleLoadingComplete(@NonNull List<PersonModel> people) {
        mPeopleList = people;
        submitPeople();
    }

    private void onClickPerson(@NonNull PersonModel person) {
        // TODO: handle person click
    }

    private void submitQuery(String key) {
        mQueryString = key;
        submitPeople();
    }

    private void submitPeople() {
        mAdapter.filter(mPeopleList,mQueryString);
    }

    private void onClickAddPerson(){
        navController.navigate(R.id.action_personsList_to_inputPerson);
    }

    @Override
    public void onClickItem(@NonNull RecyclerView recyclerView, @NonNull View view, int position) {
        if (PeopleAdapter.SECTION_ITEM_TYPE == mAdapter.getItemViewType(position)) {
            onClickPerson(mAdapter.getData(position));
        }
    }
}

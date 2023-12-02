package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
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
import dreammaker.android.expensetracker.databinding.LayoutPeopleListFragmentBinding;
import dreammaker.android.expensetracker.listener.OnItemClickListener;
import dreammaker.android.expensetracker.listener.RecyclerViewItemClickHelper;
import dreammaker.android.expensetracker.viewmodel.PeopleListViewModel;

@SuppressWarnings("all")
public class PeopleList extends Fragment implements OnItemClickListener {

    private static final String TAG = "PersonsList";

    private static final String KEY_QUERY_STRING = "query_string";

    private NavController navController;

    private PeopleListViewModel mViewModel;

    private LayoutPeopleListFragmentBinding mBinding;

    private PeopleAdapter mAdapter;

    private RecyclerViewItemClickHelper mClickHelper;

    private List<PersonModel> mPeopleList;

    private String mQueryString = null;

    public PeopleList() { super(); }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this,(ViewModelProvider.Factory) new ViewModelProvider.AndroidViewModelFactory())
                .get(PeopleListViewModel.class);
        mViewModel.getAllPeople().observe(this,this::onPeopleLoadingComplete);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = LayoutPeopleListFragmentBinding.inflate(inflater,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        mBinding.searchPeople.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                onSearchKeyChanged(s.toString());
            }
        });
        mBinding.addPerson.setOnClickListener(v -> onClickAddPerson());
        mAdapter = new PeopleAdapter(requireContext());
        mBinding.list.setAdapter(mAdapter);
        mClickHelper = new RecyclerViewItemClickHelper(mBinding.list);
        mClickHelper.setOnItemClickListener(this);
        if (null != savedInstanceState) {
            mQueryString = savedInstanceState.getString(KEY_QUERY_STRING,null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: set title
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_QUERY_STRING,mQueryString);
    }

    private void onPeopleLoadingComplete(@NonNull List<PersonModel> people) {
        mPeopleList = people;
        searchPeople();
    }

    private void onClickPerson(@NonNull PersonModel person) {
        // TODO: handle person click
    }

    private void onSearchKeyChanged(String key) {
        mQueryString = key;
        searchPeople();
    }

    private void searchPeople() {
        if (TextUtils.isEmpty(mQueryString)) {
            mAdapter.submitList(mPeopleList);
        }
        else {
            mAdapter.filter(mPeopleList,mQueryString);
        }
    }

    private void onClickAddPerson(){
        navController.navigate(R.id.action_personsList_to_inputPerson);
    }


    @Override
    public void onClickItem(@NonNull RecyclerView recyclerView, @NonNull View view, int positon) {
        if (PeopleAdapter.SECTION_ITEM_TYPE == mAdapter.getItemViewType(positon)) {
            onClickPerson(mAdapter.getData(positon));
        }
    }

    private void onClickDeletePeople() {}

}

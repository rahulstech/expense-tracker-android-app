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
import dreammaker.android.expensetracker.adapter.AccountsAdapter;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.databinding.LayoutAccountsListFragmentBinding;
import dreammaker.android.expensetracker.listener.OnItemClickListener;
import dreammaker.android.expensetracker.listener.RecyclerViewItemClickHelper;
import dreammaker.android.expensetracker.viewmodel.AccountsListViewModel;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;
import static androidx.lifecycle.ViewModelProvider.Factory;

@SuppressWarnings("all")
public class AccountsList extends Fragment implements OnItemClickListener {

    private static final String TAG = AccountsList.class.getSimpleName();

    private static final String KEY_QUERY_STRING = "query_string";

    private static final int SEARCH_KEY_LENGTH_THRESHOLD = 2;

    private NavController navController;

    private AccountsListViewModel mViewModel;

    private LayoutAccountsListFragmentBinding mBinding;

    private AccountsAdapter mAdapter;

    private RecyclerViewItemClickHelper mClickHelper;

    private String mQueryString = null;

    private List<AccountModel> mLastLoadedAccounts = null;

    public AccountsList() { super(); }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(requireActivity(), (Factory) new AndroidViewModelFactory())
                .get(AccountsListViewModel.class);
        mViewModel.getAllAccounts().observe(this,this::onFinishAccountsLoading);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = LayoutAccountsListFragmentBinding.inflate(inflater,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        mBinding.searchAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                onSearchKeyChanged(s.toString());
            }
        });
        mBinding.addAccount.setOnClickListener(v->onClickAddAccount());
        mAdapter = new AccountsAdapter(requireContext());
        mBinding.list.setAdapter(mAdapter);
        mClickHelper = new RecyclerViewItemClickHelper(mBinding.list);
        mClickHelper.setOnItemClickListener(this);
        // restore saved state if available
        if (null != savedInstanceState) {
            mQueryString = savedInstanceState.getString(KEY_QUERY_STRING,null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: change title setting
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_QUERY_STRING,mQueryString);
    }

    private void onSearchKeyChanged(String key) {
        mQueryString = key;
        searchAccounts();
    }

    private void searchAccounts() {
        if (TextUtils.isEmpty(mQueryString)) {
            mAdapter.submitList(mLastLoadedAccounts);
        }
        else {
            mAdapter.filter(mLastLoadedAccounts,mQueryString);
        }
    }

    private void onDataSetChanged() {
        // TODO: implement method
    }

    private void onFinishAccountsLoading(@Nullable List<AccountModel> accounts) {
        mLastLoadedAccounts = accounts;
        searchAccounts();
    }

    private void onClickAddAccount() {
        navController.navigate(R.id.action_accountsList_to_inputAccount);
    }

    private void onClickDeleteAccounts() {
        // TODO: warn before delete
    }

    @Override
    public void onClickItem(@NonNull RecyclerView recyclerView, @NonNull View view, int positon) {
        if (AccountsAdapter.SECTION_ITEM_TYPE == mAdapter.getItemViewType(positon)) {
            onClickAccount(mAdapter.getData(positon));
        }
    }

    private void onClickAccount(@NonNull AccountModel account) {
        // TODO: handle account click
    }
}

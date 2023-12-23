package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
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
import dreammaker.android.expensetracker.databinding.LayoutBrowseSearchAddBinding;
import dreammaker.android.expensetracker.itemdecoration.SimpleEmptyRecyclerViewDecoration;
import dreammaker.android.expensetracker.listener.OnItemClickListener;
import dreammaker.android.expensetracker.listener.RecyclerViewItemClickHelper;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ResourceUtil;
import dreammaker.android.expensetracker.viewmodel.AccountViewModel;

import static androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;
import static androidx.lifecycle.ViewModelProvider.Factory;

@SuppressWarnings("unused")
public class AccountsList extends Fragment implements OnItemClickListener {

    private static final String TAG = AccountsList.class.getSimpleName();

    private static final String KEY_QUERY_STRING = "query_string";

    private NavController navController;

    AccountViewModel mViewModel;

    private LayoutBrowseSearchAddBinding mBinding;

    private AccountsAdapter mAdapter;

    private String mQueryString = null;

    private List<AccountModel> mLastLoadedAccounts = null;

    public AccountsList() { super(); }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this, (Factory) AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(AccountViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = LayoutBrowseSearchAddBinding.inflate(inflater,container,false);
        mViewModel.getAllAccounts().observe(getViewLifecycleOwner(),this::onFinishAccountsLoading);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        mBinding.search.setHint(R.string.search_account);
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
        mBinding.add.setContentDescription(getText(R.string.description_add_account));
        mBinding.add.setImageDrawable(ResourceUtil.getDrawable(requireContext(),R.drawable.ic_add_account));
        mBinding.add.setOnClickListener(v->onClickAddAccount());
        mAdapter = new AccountsAdapter(requireContext());
        mBinding.list.setAdapter(mAdapter);
        mBinding.list.addItemDecoration(new SimpleEmptyRecyclerViewDecoration(getText(R.string.label_no_account),
                ResourceUtil.getDrawable(requireContext(),R.drawable.ic_account_72)));
        RecyclerViewItemClickHelper mClickHelper = new RecyclerViewItemClickHelper(mBinding.list);
        mClickHelper.setOnItemClickListener(this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mQueryString = savedInstanceState.getString(KEY_QUERY_STRING,null);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_QUERY_STRING,mQueryString);
    }

    private void submitQuery(String key) {
        mQueryString = key;
        submitAccounts();
    }

    private void submitAccounts() {
        mAdapter.filter(mLastLoadedAccounts,mQueryString);
    }

    private void onFinishAccountsLoading(@Nullable List<AccountModel> accounts) {
        mLastLoadedAccounts = accounts;
        submitAccounts();
    }

    private void onClickAddAccount() {
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_ACTION,Constants.ACTION_INSERT);
        navController.navigate(R.id.action_accounts_to_input_account,args);
    }

    @Override
    public void onClickItem(@NonNull RecyclerView recyclerView, @NonNull View view, int position) {
        if (AccountsAdapter.SECTION_ITEM_TYPE == mAdapter.getItemViewType(position)) {
            onClickAccount(mAdapter.getData(position));
        }
    }

    private void onClickAccount(@NonNull AccountModel account) {
        Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_ID,account.getId());
        navController.navigate(R.id.action_accounts_to_account_details,args);
    }
}

package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.adapter.AccountsChooserAdapter;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.LayoutPayeePayerChooserBinding;
import dreammaker.android.expensetracker.itemdecoration.SimpleEmptyRecyclerViewDecoration;
import dreammaker.android.expensetracker.listener.ChoiceModel;
import dreammaker.android.expensetracker.util.ResourceUtil;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.AccountViewModel;

@SuppressWarnings("unused")
public class AccountChooserFragment extends Fragment {

    private static final String TAG = AccountChooserFragment.class.getSimpleName();

    private static final String KEY_QUERY = "query";

    private NavController navController;

    @SuppressWarnings("FieldCanBeLocal")
    private AccountViewModel mViewModel;

    private ChoiceModel.SavedStateViewModel mChoiceModelSavedState;

    private AccountsChooserAdapter mAdapter;

    private LayoutPayeePayerChooserBinding mBinding;

    private TransactionHistoryParcelable mHistory;

    private String mQuery;

    private List<AccountModel> mLoadedAccounts;

    private ChoiceModel mChoiceModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHistory = getExtraTransactionHistory();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(AccountViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = LayoutPayeePayerChooserBinding.inflate(inflater,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        navController = Navigation.findNavController(view);
        mViewModel.getAllAccountsWithUsageCount().observe(getViewLifecycleOwner(),this::onAccountsLoaded);
        mChoiceModelSavedState = new ViewModelProvider(this).get(ChoiceModel.SavedStateViewModel.class);
        mBinding.search.setHint(R.string.search_account);
        mBinding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                submitSearch(s.toString());
            }
        });
        mBinding.btnPrevious.setOnClickListener(v->onClickPrevious());
        mBinding.btnNext.setOnClickListener(v-> onClickNext());
        mAdapter = new AccountsChooserAdapter(requireContext());
        mBinding.list.setAdapter(mAdapter);
        mChoiceModel = new ChoiceModel(mBinding.list,mAdapter);
        mChoiceModel.setChoiceMode(ChoiceModel.CHOICE_MODE_SINGLE);
        mAdapter.setChoiceModel(mChoiceModel);
        mBinding.list.addItemDecoration(new SimpleEmptyRecyclerViewDecoration(getText(R.string.label_no_account),
                ResourceUtil.getDrawable(requireContext(),R.drawable.ic_account_black_72)));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mQuery = savedInstanceState.getString(KEY_QUERY,null);
            mChoiceModel.onRestoreInstanceState(mChoiceModelSavedState);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_QUERY,mQuery);
        mChoiceModel.onSaveInstanceState(mChoiceModelSavedState);
    }

    private void setTitle() {
        requireActivity().setTitle(getTitle());
    }

    private CharSequence getTitle() {
        TransactionType type = mHistory.getType();
        switch (type) {
            case INCOME:
            case BORROW: {
                return getText(R.string.label_receive_in);
            }
            case EXPENSE:
            case DUE: {
                return getText(R.string.label_pay_from);
            }
            case MONEY_TRANSFER: {
                if (null == mHistory.getPayeeAccountId()) {
                    return getText(R.string.label_send_to);
                }
                else {
                    return getText(R.string.label_send_from);
                }
            }
        }
        return null;
    }

    private void onClickPrevious() {
        exit();
    }

    private void onClickNext() {
        if (!validateAccount()) {
            return;
        }
        long id = mAdapter.getItemId(mChoiceModel.getSelectedPosition());
        TransactionType type = mHistory.getType();
        switch (type) {
            case INCOME:
            case BORROW:
            case PAY_DUE:{
                mHistory.setPayeeAccountId(id);
            }
            break;
            case EXPENSE:
            case DUE:
            case PAY_BORROW: {
                mHistory.setPayerAccountId(id);
            }
            break;
            case MONEY_TRANSFER: {
                if (mHistory.getPayeeAccountId() == null) {
                    mHistory.setPayeeAccountId(id);
                }
                else {
                    mHistory.setPayerAccountId(id);
                }
            }
        }
        gotoNextDestination(mHistory);
    }

    private void onAccountsLoaded(@Nullable List<AccountModel> accounts) {
        if (null == accounts) {
            // TODO: notify and exit
            ToastUtil.showErrorShort(requireContext(),"");
            exit();
        }
        else {
            mLoadedAccounts = accounts;
            submitAccounts();
        }
    }

    private void submitSearch(String query) {
        mQuery = query;
        submitAccounts();
    }

    private void submitAccounts() {
        mAdapter.filter(mLoadedAccounts,mQuery);
    }

    private TransactionHistoryParcelable getExtraTransactionHistory() {
        return requireArguments().getParcelable(TransactionBasicDetailsInputFragment.EXTRA_TRANSACTION_HISTORY);
    }

    private boolean validateAccount() {
        // TODO: hasSelection always returns false
        if (BuildConfig.DEBUG) {
            Log.d(TAG,"checked-count="+mChoiceModel.getCheckedCount());
        }
        if (!mChoiceModel.hasSelection()) {
            ToastUtil.showErrorShort(requireContext(), R.string.error_no_account_selected);
            return false;
        }
        return true;
    }

    private void gotoNextDestination(TransactionHistoryParcelable history) {
        Bundle args = new Bundle(requireArguments());
        args.putParcelable(TransactionBasicDetailsInputFragment.EXTRA_TRANSACTION_HISTORY,history);
        TransactionType type = history.getType();
        switch (type) {
            case DUE:
            case BORROW: {
                navController.navigate(R.id.action_account_chooser_to_person_chooser,args);
            }
            break;
            case MONEY_TRANSFER: {
                if (history.getPayeeAccountId() != null && history.getPayerAccountId() != null) {
                    navController.navigate(R.id.action_account_chooser_to_save_history,args);
                }
                else {
                    navController.navigate(R.id.action_account_chooser_to_account_chooser,args);
                }
            }
        }
    }

    private void exit() {
        navController.popBackStack();
    }
}

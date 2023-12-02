package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.entity.Account;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.databinding.InputAccountBinding;
import dreammaker.android.expensetracker.dialog.DialogUtil;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.viewmodel.DBViewModel;
import dreammaker.android.expensetracker.viewmodel.InputAccountViewModel;

@SuppressWarnings("all")
public class InputAccount extends Fragment {

    private static final String TAG = InputAccount.class.getSimpleName();

    private static final String KEY_ACCOUNT_VALUE_SET = "account_value_set";

    private NavController navController;

    private InputAccountViewModel mViewModel;

    private InputAccountBinding mBinding;

    private AccountModel nAccount;
    private boolean mAccountSet = false;

    public InputAccount(){
        super();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(requireActivity(), (ViewModelProvider.Factory) new ViewModelProvider.AndroidViewModelFactory())
                .get(InputAccountViewModel.class);
        if (isEditOperation()) {
            long id = getAccountId();
            mViewModel.getAccountById(id).observe(getViewLifecycleOwner(),this::onAccountFetched);
        }
        LiveData<DBViewModel.AsyncQueryResult> result = mViewModel.getLiveResult(InputAccountViewModel.OPERATION_SAVE_ACCOUNT);
        if (result != null) {
            result.observe(getViewLifecycleOwner(),this::onAccountSaveComplete);
        }
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressed();
            }
        });
    }

    private boolean isEditOperation() {
        return Constants.ACTION_UPDATE.equals(requireArguments().getString(Constants.EXTRA_ACTION));
    }

    private long getAccountId() {
        return requireArguments().getLong(Constants.EXTRA_ID,0L);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = InputAccountBinding.inflate(inflater,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        mBinding.containerBalance.setEndIconOnClickListener(v->onToggleCalculator());
        mBinding.buttonSave.setOnClickListener(v->onClickSave());
        if (null != savedInstanceState) {
            mAccountSet = savedInstanceState.getBoolean(KEY_ACCOUNT_VALUE_SET,false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_ACCOUNT_VALUE_SET, mAccountSet);
    }

    private void onBackPressed() {
        if (hasAnyValueChanged()) {
            DialogUtil.createMessageDialog(requireContext(),R.string.warning_not_saved,
                    R.string.label_discard, null,
                    R.string.label_exit, (di,which)->exit(),
                    false);
            return;
        }
        exit();
    }

    private void onAccountFetched(@Nullable AccountModel account) {
        if (null == account) {
            Toast.makeText(requireContext(),R.string.error_account_not_found,Toast.LENGTH_SHORT).show();
            exit();
            return;
        }
        nAccount = account;
        if (mAccountSet) {
            return;
        }
        mBinding.name.setText(account.getName());
        mBinding.balance.setText(account.getBalance().toString());
        mBinding.containerBalance.setHelperText(TextUtil.currencyToText(requireContext(),account.getBalance()));
        mAccountSet = true;
    }

    private void onClickCancel() {
        if (hasAnyValueChanged()) {

            return;
        }
        exit();
    }

    private void onClickSave() {
        CharSequence name = mBinding.name.getText();
        CharSequence txtBalance = mBinding.balance.getText();
        Currency balance = null;

        mBinding.containerName.setErrorEnabled(false);
        mBinding.containerBalance.setErrorEnabled(false);

        // validate inputs
        boolean valid = true;
        if (TextUtils.isEmpty(name)) {
            mBinding.containerName.setError(getString(R.string.error_empty_input));
            valid = false;
        }
        if (TextUtils.isEmpty(txtBalance)) {
            mBinding.containerBalance.setError(getString(R.string.error_empty_input));
            valid = false;
        }
        else {
            try {
                balance = Currency.valueOf(txtBalance.toString());
            }
            catch (Throwable ignore) {

                valid = false;
            }

            balance = TextUtil.tryConvertToCurrencyOrNull(txtBalance);
            valid = null != balance;
            if (!valid) {
                mBinding.containerBalance.setError(getString(R.string.error_invalid_numeric_value));
            }

        }
        // at least one invalid input exists, don't go further
        if (!valid) return;
        // all inputs are valid proceed to save
        Account account = new Account();
        if (isEditOperation()) {
            if (!hasAnyValueChanged()) {
                Toast.makeText(requireContext(),R.string.message_no_change_no_save,Toast.LENGTH_SHORT).show();
                exit();
                return;
            }
            account.setId(getAccountId());
        }
        account.setName(name.toString());
        account.setBalance(balance);
        mViewModel.saveAccount(account).observe(getViewLifecycleOwner(),this::onAccountSaveComplete);
    }

    private void onAccountSaveComplete(@NonNull DBViewModel.AsyncQueryResult result) {
        Account account = (Account) result.getResult();
        if (null == account) {
            Toast.makeText(requireContext(),R.string.error_save,Toast.LENGTH_SHORT).show();
            Log.e(TAG,"fail to save account",result.getError());
            return;
        }
        Toast.makeText(requireContext(),R.string.message_save,Toast.LENGTH_SHORT).show();
        if (isEditOperation()) {
            exit();
        }
        else {
            // TODO: show account details
        }
    }

    private boolean hasAnyValueChanged() {
        CharSequence name = mBinding.name.getText();
        CharSequence txtBalance = mBinding.balance.getText();
        if (isEditOperation()) {
            return mAccountSet && (TextUtils.equals(name, nAccount.getName())
                    || !nAccount.getBalance().equals(TextUtil.tryConvertToCurrencyOrNull(txtBalance)));
        }
        else {
            return !TextUtils.isEmpty(name) || !TextUtils.isEmpty(txtBalance);
        }
    }

    private void onToggleCalculator() {
        // TODO: show to hide calculator
    }

    private void exit() {
        // TODO: exit current screen
        navController.popBackStack();
    }
}

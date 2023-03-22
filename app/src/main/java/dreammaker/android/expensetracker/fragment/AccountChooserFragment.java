package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.AccountDisplayModel;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.view.adapter.AccountChooserRecyclerAdapter;
import dreammaker.android.expensetracker.viewmodel.SavedStateViewModel;
import dreammaker.android.expensetracker.viewmodel.TransactionInputViewModel;

public class AccountChooserFragment extends Fragment {

    private static final String TAG = "AccChooserFrag";

    private static final String KEY_ACCOUNT_ADAPTER_STATE = "account_adapter_state";

    private NavController navController;
    private TransactionInputViewModel viewModel;
    private SavedStateViewModel mSavedState;
    private Button btn2;
    private Button btn1;
    private RecyclerView accountChooser;
    private AccountChooserRecyclerAdapter accountAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(requireActivity(),
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(TransactionInputViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.payee_payer_chooser_layout,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        mSavedState = new ViewModelProvider(this).get(SavedStateViewModel.class);
        TextView title = view.findViewById(R.id.title);
        btn1 = view.findViewById(R.id.btn1);
        btn2 = view.findViewById(R.id.btn2);
        view.findViewById(R.id.container_two_buttons).setVisibility(View.VISIBLE);
        accountChooser = view.findViewById(R.id.list);
        accountAdapter = new AccountChooserRecyclerAdapter(requireContext());
        accountChooser.setAdapter(accountAdapter);
        accountAdapter.changeChoiceMode(AccountChooserRecyclerAdapter.CHOICE_MODE_SINGLE);
        viewModel.getAccountsDisplayLiveData().observe(getViewLifecycleOwner(),items -> accountAdapter.submitList(items));
        btn1.setOnClickListener(v -> onClickBtn1());
        btn2.setOnClickListener(v -> onClickBtn2());

        String action = requireActivity().getIntent().getAction();
        if (Constants.ACTION_INCOME_EXPENSE.equals(action)) {
            title.setText(R.string.pay_from);
            btn1.setText(R.string.add_income);
            btn2.setText(R.string.add_expense);
        }
        else if (Constants.ACTION_PAYMENT_DUE.equals(action)) {
            title.setText(R.string.pay_from);
            btn1.setText(R.string.pay_due);
            btn2.setText(R.string.add_due);
        }
        else if (!isPayeeAccountChooser()) {
            title.setText(R.string.pay_from);
            btn1.setText(R.string.back);
            btn2.setText(R.string.next);
        }
        else {
            title.setText(R.string.pay_to);
            btn1.setText(R.string.back);
            btn2.setText(R.string.send_money);
        }

        // restore adapter state
        accountAdapter.onRestoreState(mSavedState.getParcelable(KEY_ACCOUNT_ADAPTER_STATE));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        mSavedState.put(KEY_ACCOUNT_ADAPTER_STATE, accountAdapter.onSaveState());
    }

    void onClickBtn2() {
        String action = requireActivity().getIntent().getAction();
        if (Constants.ACTION_PAYMENT_DUE.equals(action)) {
            onMoveToNextDestination(R.id.accounts_to_save_transaction,TransactionType.DUE);
        }
        else if (Constants.ACTION_INCOME_EXPENSE.equals(action)) {
            onMoveToNextDestination(R.id.accounts_to_save_transaction,TransactionType.EXPENSE);
        }
        else if (isPayeeAccountChooser()) {
            onMoveToNextDestination(R.id.accounts_to_accounts,TransactionType.MONEY_TRANSFER);
        }
        else {
            onMoveToNextDestination(R.id.accounts_to_accounts,TransactionType.MONEY_TRANSFER);
        }
    }

    void onClickBtn1() {
        String action = requireActivity().getIntent().getAction();
        if (Constants.ACTION_PAYMENT_DUE.equals(action)) {
            onMoveToNextDestination(R.id.accounts_to_save_transaction,TransactionType.PAY_DUE);
        }
        else if (Constants.ACTION_INCOME_EXPENSE.equals(action)) {
            onMoveToNextDestination(R.id.accounts_to_save_transaction,TransactionType.INCOME);
        }
        else {
            navController.popBackStack();
        }
    }

    private boolean validateAccount() {
        if (!accountAdapter.hasSelection()) {
            Toast.makeText(requireContext(), R.string.no_account_selected, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void onMoveToNextDestination(@IdRes int destinationId, @NonNull TransactionType type) {
        if (!validateAccount()) return;

        AccountDisplayModel account = accountAdapter.getCheckedItem();
        String action = requireActivity().getIntent().getAction();
        Bundle args = new Bundle(getArguments());
        args.putString(Constants.EXTRA_TRANSACTION_TYPE,type.name());
        if (Constants.ACTION_MONEY_TRANSFER.equals(action)) {
            if (isPayeeAccountChooser()){
                args.putLong(Constants.EXTRA_PAYEE_ACCOUNT,account.getId());
            }
            else {
                args.putLong(Constants.EXTRA_PAYER_ACCOUNT,account.getId());
            }
        }
        else {
            args.putLong(Constants.EXTRA_ACCOUNT,account.getId());
        }
        navController.navigate(destinationId,args);
    }

    private boolean isPayeeAccountChooser() {
        String action = requireActivity().getIntent().getAction();
        boolean hasPayeeAccount = getArguments().containsKey(Constants.EXTRA_PAYEE_ACCOUNT);
        return Constants.ACTION_MONEY_TRANSFER.equals(action) && hasPayeeAccount;
    }
}

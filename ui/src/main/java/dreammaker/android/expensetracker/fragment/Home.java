package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.MainActivity;
import dreammaker.android.expensetracker.database.BalanceAndDueSummary;
import dreammaker.android.expensetracker.database.Transaction;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Helper;
import dreammaker.android.expensetracker.viewmodel.TransactionsViewModel;

import static dreammaker.android.expensetracker.BuildConfig.DEBUG;

public class Home extends Fragment implements View.OnClickListener {

    private static final String TAG = "Home";

    private TextView totalBalance;
    private TextView totalDue;
    private TextView countAccounts;
    private TextView countPeople;
    private Button accounts;
    private Button persons;
    private FloatingActionButton addTransaction;
    private Button transactions;
    private Button moneyTransfer;

    private TransactionsViewModel viewModel;
    private NavController navController;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (Check.isNonNull(getActivity())) {
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(TransactionsViewModel.class);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        navController = Navigation.findNavController(v);
        totalBalance = v.findViewById(R.id.total_balance);
        totalDue = v.findViewById(R.id.total_due);
        countAccounts = v.findViewById(R.id.count_accounts);
        countPeople = v.findViewById(R.id.count_people);
        accounts = v.findViewById(R.id.accounts);
        persons = v.findViewById(R.id.persons);
        addTransaction = v.findViewById(R.id.add_transaction);
        transactions = v.findViewById(R.id.transactions);
        moneyTransfer = v.findViewById(R.id.money_transfer);
        accounts.setOnClickListener(this);
        persons.setOnClickListener(this);
        addTransaction.setOnClickListener(this);
        transactions.setOnClickListener(this);
        moneyTransfer.setOnClickListener(this);
        Helper.setTitle(getActivity(), R.string.app_name);
        viewModel.getBalanceAndDueSummaryLiveData().observe(getViewLifecycleOwner(), this::onUpdateBalanceAndDueSummary);
    }

    @Override
    public void onClick(View v) {
        if(v == accounts){
            navController.navigate(R.id.action_home_to_accountsList);
        }
        else if(v == persons){
            navController.navigate(R.id.action_home_to_personsList);
        }
        else if(v == transactions){
            viewModel.loadAllTransactions();
            navController.navigate(R.id.action_home_to_transactionsList);
        }
        else if (v == addTransaction){
            viewModel.hasAnyAccount(r -> {
                if (r) {
                    viewModel.setWorkingTransaction(null);
                    navController.navigate(R.id.action_home_to_inputTransaction);
                }
                else
                    showQuickMessage(R.string.message_no_account_for_transaction);
            });
        }
        else if (v == moneyTransfer) {
            navController.navigate(R.id.action_home_to_moneyTransferHistory);
        }
    }

    private void onUpdateBalanceAndDueSummary(BalanceAndDueSummary summary) {
        float balance = 0;
        float due = 0;
        int cAccount = 0;
        int cPeople = 0;
        if (Check.isNonNull(summary)) {
            balance = summary.getTotalBalance();
            cAccount = summary.getCountTotalBalanceAccount();
            due = summary.getTotalDue();
            cPeople = summary.getCountTotalDuePerson();
        }
        totalBalance.setText(Helper.floatToString(balance));
        countAccounts.setText(getString(R.string.label_count_balance_accounts, cAccount));
        totalDue.setText(Helper.floatToString(due));
        countPeople.setText(getString(R.string.label_count_due_people, cPeople));
    }

    private void showQuickMessage(@StringRes int id) {
        MainActivity.showQuickMessage(getActivity(), id, android.R.string.ok);
    }
}

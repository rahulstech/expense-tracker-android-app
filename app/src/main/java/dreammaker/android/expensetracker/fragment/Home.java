package dreammaker.android.expensetracker.fragment;

import android.content.Intent;
import android.os.Bundle;
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
import dreammaker.android.expensetracker.activity.ActivityTransactionInput;
import dreammaker.android.expensetracker.activity.MainActivity;
import dreammaker.android.expensetracker.database.view.AssetLiabilitySummary;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.Helper;
import dreammaker.android.expensetracker.viewmodel.HomeViewModel;

public class Home extends Fragment {

    private static final String TAG = "Home";

    private TextView totalBalance;
    private TextView totalDue;
    private TextView countAccounts;
    private TextView countPeople;
    private Button accounts;
    private Button persons;
    private FloatingActionButton addTransaction;

    private HomeViewModel homeViewModel;
    private NavController navController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        navController = Navigation.findNavController(v);
        homeViewModel = new ViewModelProvider(requireActivity(),
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(HomeViewModel.class);
        totalBalance = v.findViewById(R.id.total_balance);
        totalDue = v.findViewById(R.id.total_due);
        countAccounts = v.findViewById(R.id.count_accounts);
        countPeople = v.findViewById(R.id.count_people);
        accounts = v.findViewById(R.id.accounts);
        persons = v.findViewById(R.id.persons);
        addTransaction = v.findViewById(R.id.add_transaction);
        accounts.setOnClickListener(w -> onShowAccount());
        persons.setOnClickListener(w -> onShowPeople());
        addTransaction.setOnClickListener(w -> onNewTransaction());

        homeViewModel.getAssetLiabilitySummaryLiveData().observe(getViewLifecycleOwner(),this::onAssetLiabilitySummeryFetched);

        Helper.setTitle(requireActivity(), R.string.app_name);
    }

    private void onShowAccount() {
        navController.navigate(R.id.home_to_activity_account);
    }

    private void onShowPeople() {
        navController.navigate(R.id.home_to_activity_people);
    }

    private void onNewTransaction() {
        Intent intent = new Intent(requireActivity(),ActivityTransactionInput.class);
        intent.setAction(Constants.ACTION_PAYMENT_DUE);
        startActivity(intent);
    }

    private void onAssetLiabilitySummeryFetched(@Nullable AssetLiabilitySummary summary) {
        if (null == summary) {
            totalBalance.setText("0");
            totalDue.setText("0");
        }
        else {
            totalBalance.setText(summary.getTotalAsset().toPlainString());
            totalDue.setText(summary.getTotalLiability().toPlainString());
        }
    }

    private void showQuickMessage(@StringRes int id) {
        MainActivity.showQuickMessage(getActivity(), id, android.R.string.ok);
    }
}

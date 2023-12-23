package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.ActivityModel;
import dreammaker.android.expensetracker.activity.ActivityModelProvider;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.database.view.AssetLiabilitySummary;
import dreammaker.android.expensetracker.databinding.FragmentHomeBinding;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.viewmodel.HomeViewModel;

@SuppressWarnings("unused")
public class HomeFragment extends Fragment {

    private static final String TAG = "Home";

    private HomeViewModel mViewModel;

    private NavController navController;

    private FragmentHomeBinding mBinding;

    @SuppressWarnings("FieldCanBeLocal")
    private ActivityModel mActivityModel;

    public HomeFragment() {super();}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(HomeViewModel.class);
        mActivityModel = ((ActivityModelProvider) requireActivity()).getActivityModel();
        mActivityModel.addOnBackPressedCallback(this,this::onBackPressed);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentHomeBinding.inflate(inflater,container,false);
        mViewModel.getAssetLiabilitySummaryLiveData().observe(getViewLifecycleOwner(),this::onAssetLiabilitySummeryFetched);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        navController = Navigation.findNavController(view);
        mBinding.navAccounts.setOnClickListener(v -> onClickAccounts());
        mBinding.navPeople.setOnClickListener(v -> onClickPeople());
        mBinding.navHistories.setOnClickListener(v -> onClickHistories());
        mBinding.addIncome.setOnClickListener(v->onClickAddIncome());
        mBinding.addExpense.setOnClickListener(v->onClickAddExpense());
        mBinding.addMoneyTransfer.setOnClickListener(v-> onClickMoneyTransfer());
        mBinding.addDue.setOnClickListener(v->onClickAddDue());
        mBinding.addPayDue.setOnClickListener(v->onClickAddPayDue());
        mBinding.addBorrow.setOnClickListener(v->onClickAddBorrow());
    }

    private void setTitle() {
        requireActivity().setTitle(R.string.app_name);
    }

    private boolean onBackPressed() {
        return false;
    }

    private void onClickAccounts() {
        navController.navigate(R.id.action_home_to_accounts);
    }

    private void onClickPeople() {
        navController.navigate(R.id.action_home_to_people);
    }

    private void onClickHistories() {
        navController.navigate(R.id.action_home_to_histories);
    }

    private void onClickMoneyTransfer() {
        navigateToInputTransaction(TransactionType.MONEY_TRANSFER);
    }

    private void onClickAddIncome() {
        navigateToInputTransaction(TransactionType.INCOME);
    }

    private void onClickAddExpense() {
        navigateToInputTransaction(TransactionType.EXPENSE);
    }

    private void onClickAddDue() {
        navigateToInputTransaction(TransactionType.DUE);
    }

    private void onClickAddPayDue() {
        navigateToInputTransaction(TransactionType.PAY_DUE);
    }

    private void onClickAddBorrow() {
        navigateToInputTransaction(TransactionType.BORROW);
    }

    private void onAssetLiabilitySummeryFetched(@Nullable AssetLiabilitySummary summary) {
        if (null == summary) {
            showAsset(Currency.ZERO);
            showLiability(Currency.ZERO);
        }
        else {
            showAsset(summary.getTotalAsset());
            showLiability(summary.getTotalLiability());
        }
    }

    private void showAsset(Currency asset) {
        mBinding.totalAsset.setText(TextUtil.prettyFormatCurrency(asset));
    }

    private void showLiability(Currency liability) {
        mBinding.totalLiability.setText(TextUtil.prettyFormatCurrency(liability));
    }

    private void navigateToInputTransaction(TransactionType type) {
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_ACTION,Constants.ACTION_INSERT);
        args.putString(Constants.EXTRA_TRANSACTION_TYPE,type.name());
        navController.navigate(R.id.action_home_to_add_history,args);
    }
}

package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.MainActivity;
import dreammaker.android.expensetracker.util.CalculatorKeyboard;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.viewmodel.TransactionsViewModel;

public class FilterTransactionScreenMiscellaneous extends BaseFragment<FilterTransactionScreenMiscellaneous.FilterTransactionMiscellaneousViewHolder> {

    private TransactionsViewModel viewModel;
    private CalculatorKeyboard minCalcKB, maxCalcKB;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setFragmentTitle(R.string.label_filter_transaction);
        viewModel = new ViewModelProvider(getActivity(),
                new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                .get(TransactionsViewModel.class);
    }

    @NonNull
    @Override
    protected FilterTransactionMiscellaneousViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return new FilterTransactionMiscellaneousViewHolder(inflater.inflate(R.layout.screen_miscellaneous,
                container,false));
    }

    @Override
    protected void onFragmentViewHolderCreated(@NonNull final FilterTransactionMiscellaneousViewHolder vh) {
        minCalcKB = new CalculatorKeyboard(getActivity(),vh.minAmount);
        maxCalcKB = new CalculatorKeyboard(getActivity(),vh.maxAmount);
        vh.credit.setOnCheckedChangeListener((cb,checked) -> viewModel.getWorkingFilterParams().setCredit(checked));
        vh.debit.setOnCheckedChangeListener((cb,checked) -> viewModel.getWorkingFilterParams().setDebit(checked));
        viewModel.getWorkingFilterParamsLiveData().observe(this, param -> populateWithInitialValues(vh,param));
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull FilterTransactionMiscellaneousViewHolder vh) {}

    @Override
    public void onPause() {
        maxCalcKB.hideCalculatorKeyboard();
        minCalcKB.hideCalculatorKeyboard();
        super.onPause();
    }

    @Override
    public boolean onBackPressed() {
        if (minCalcKB.onBackPressed() || maxCalcKB.onBackPressed()) return true;
        return super.onBackPressed();
    }

    private void populateWithInitialValues(@NonNull FilterTransactionMiscellaneousViewHolder vh, TransactionsViewModel.FilterTransactionParams params) {
        vh.minAmount.setText(params.getMinAmountText());
        vh.maxAmount.setText(params.getMaxAmountText());
        vh.credit.setChecked(params.isCredit());
        vh.debit.setChecked(params.isDebit());
    }

    public static class FilterTransactionMiscellaneousViewHolder extends BaseFragment.FragmentViewHolder {
        EditText minAmount;
        EditText maxAmount;
        CheckBox credit;
        CheckBox debit;

        FilterTransactionMiscellaneousViewHolder(@NonNull View root) {
            super(root);
            minAmount = findViewById(R.id.min_amount);
            maxAmount = findViewById(R.id.max_amount);
            credit = findViewById(R.id.credit);
            debit = findViewById(R.id.debit);
        }
    }
}

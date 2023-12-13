package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.concurrent.TaskMaster;
import dreammaker.android.expensetracker.concurrent.TaskResult;
import dreammaker.android.expensetracker.database.model.TransactionHistory;
import dreammaker.android.expensetracker.database.type.Date;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.viewmodel.TransactionInputViewModel;

public class SaveTransactionFragment extends DialogFragment {

    private static final String TAG = "SaveTranFrag";

    private TransactionInputViewModel viewModel;
    private NavController navController;

    private View containerProgress;
    private View containerMessage;

    private TextView txtProgressLabel;
    private TextView txtMessage;
    private Button btnPositive;
    private Button btnNegative;

    public SaveTransactionFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(TransactionInputViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_save_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment_container);

        containerProgress = view.findViewById(R.id.container_progress);
        containerMessage = view.findViewById(R.id.container_message);
        txtProgressLabel = view.findViewById(R.id.progress_label);
        txtMessage = view.findViewById(R.id.message);
        btnPositive = view.findViewById(R.id.button_positive);
        btnNegative = view.findViewById(R.id.button_negative);

        btnPositive.setOnClickListener(v->onClickPositiveButton());
        btnNegative.setOnClickListener(v->onClickNegativeButton());

        setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        onSaveTransaction();
    }

    void onClickPositiveButton() {
        removeResult();
        //navController.popBackStack(R.id.basic_details,true);
    }

    void onClickNegativeButton() {
        removeResult();
        requireActivity().finish();
    }

    private void removeResult() {
        String opId = getArguments().getString(Constants.EXTRA_OPERATION_ID);
        viewModel.getTaskMaster().removeResult(opId);
    }

    private void onSaveTransaction() {
        TaskMaster taskMaster = viewModel.getTaskMaster();
        Bundle args = getArguments();
        String opId = args.getString(Constants.EXTRA_OPERATION_ID);

        TaskResult oldResult = taskMaster.getResult(opId);
        if (null != oldResult) {
            onTransactionSaved((TransactionHistory) oldResult.parameter,oldResult);
        }
        else {
            try {
                taskMaster.addTaskCallback(opId,result -> onTransactionSaved((TransactionHistory) result.parameter,result));
            }
            catch (Exception ex) {
                BigDecimal amount = new BigDecimal(args.getString(Constants.EXTRA_AMOUNT));
                Date date = Date.valueOf(args.getString(Constants.EXTRA_DATE));
                String description = args.getString(Constants.EXTRA_DESCRIPTION);
                TransactionType type = TransactionType.valueOf(args.getString(Constants.EXTRA_TRANSACTION_TYPE));
                Long payeePersonId = null, payerPersonId = null, payeeAccountId = null, payerAccountId = null;
                switch (type) {
                    case INCOME: {
                        payeeAccountId = args.getLong(Constants.EXTRA_ACCOUNT);
                    }
                    break;
                    case EXPENSE: {
                        payerAccountId = args.getLong(Constants.EXTRA_ACCOUNT);
                    }
                    break;
                    case MONEY_TRANSFER: {
                        payeeAccountId = args.getLong(Constants.EXTRA_PAYEE_ACCOUNT);
                        payerAccountId = args.getLong(Constants.EXTRA_PAYER_ACCOUNT);
                    }
                    break;
                    case DUE:
                    case PAY_BORROW:{
                        payeePersonId = args.getLong(Constants.EXTRA_PERSON);
                        payerAccountId = args.getLong(Constants.EXTRA_ACCOUNT);
                    }
                    break;
                    case PAY_DUE:
                    case BORROW: {
                        payerPersonId = args.getLong(Constants.EXTRA_PERSON);
                        payeeAccountId = args.getLong(Constants.EXTRA_ACCOUNT);
                    }
                }

                TransactionHistory transaction = new TransactionHistory();
                transaction.setPayeePersonId(payeePersonId);
                transaction.setPayerPersonId(payerPersonId);
                transaction.setPayeeAccountId(payeeAccountId);
                transaction.setPayerAccountId(payerAccountId);
                transaction.setAmount(amount);
                transaction.setType(type);
                transaction.setDate(date);
                transaction.setDescription(description);

                viewModel.saveTransactionHistory(opId,transaction,result -> onTransactionSaved(transaction,result));
            }
        }
    }

    private void onTransactionSaved(@NonNull TransactionHistory transaction, @NonNull TaskResult result) {
        containerProgress.setVisibility(View.GONE);
        containerMessage.setVisibility(View.VISIBLE);

        if (result.successful) {
            txtMessage.setText(R.string.transaction_save_successful);
        }
        else {
            txtMessage.setText(R.string.account_save_unsuccessful);
        }
    }
}
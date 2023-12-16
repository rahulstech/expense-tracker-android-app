package dreammaker.android.expensetracker.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.entity.TransactionHistory;
import dreammaker.android.expensetracker.databinding.FragmentSaveTransactionHistoryBinding;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.DBViewModel;
import dreammaker.android.expensetracker.viewmodel.TransactionHistoryViewModel;

@SuppressWarnings("unused")
public class SaveTransactionHistoryFragment extends DialogFragment {

    private static final String TAG = SaveTransactionHistoryFragment.class.getSimpleName();

    private TransactionHistoryViewModel viewModel;

    private NavController navController;

    private FragmentSaveTransactionHistoryBinding mBinding;

    public SaveTransactionHistoryFragment() {}

    private boolean isEditOperation() {
        return Constants.ACTION_UPDATE.equals(requireArguments().getString(Constants.EXTRA_ACTION));
    }

    private TransactionHistoryParcelable getTransactionHistoryParcelable() {
        return requireArguments().getParcelable(TransactionBasicDetailsInputFragment.EXTRA_TRANSACTION_HISTORY);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(TransactionHistoryViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setPositiveButton(R.string.save,(di,which)->onClickPositiveButton())
                .create();
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentSaveTransactionHistoryBinding.inflate(inflater,container,false);
        viewModel.setCallbackIfTaskExists(TransactionHistoryViewModel.SAVE_HISTORY,getViewLifecycleOwner(),this::onHistorySaved);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(requireActivity(),R.id.nav_host_fragment_container);

        mBinding.buttonPositive.setOnClickListener(v->onClickPositiveButton());
        mBinding.buttonNegative.setOnClickListener(v->onClickNegativeButton());

        setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        //onSaveTransaction();
    }

    void onClickPositiveButton() {
        saveTransaction();
    }

    void onClickNegativeButton() {

    }

    private void saveTransaction() {
        TransactionHistory history = getTransactionHistoryParcelable();
        viewModel.saveTransactionHistory(history).observe(getViewLifecycleOwner(),this::onHistorySaved);
    }

    private void onHistorySaved(DBViewModel.AsyncQueryResult result) {
        final TransactionHistory history = (TransactionHistory) result.getResult();
        if (null == history) {
            ToastUtil.showErrorShort(requireContext(),R.string.error_save);
            exit();
            return;
        }
        ToastUtil.showSuccessShort(requireContext(),R.string.transaction_history_save_successful);
        if (isEditOperation()) {
            // TODO: properly not exiting
            exitHistoryInput(false);
        }
        else {
            showHistoryDetails(history);
        }
    }

    private void showHistoryDetails(TransactionHistory history) {
        Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_ID,history.getId());
        navController.navigate(R.id.action_save_history_to_history_details,args);
    }

    private void exit() {
        dismiss();
        navController.popBackStack();
    }

    private void exitHistoryInput(boolean reset) {
        dismiss();
        navController.popBackStack(R.id.input_history,true);
    }
}
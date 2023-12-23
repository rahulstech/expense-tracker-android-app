package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.FragmentTransactionHistoryDetailsBinding;
import dreammaker.android.expensetracker.dialog.DialogUtil;
import dreammaker.android.expensetracker.drawable.DrawableUtil;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.DBViewModel;
import dreammaker.android.expensetracker.viewmodel.TransactionHistoryViewModel;
import rahulstech.android.backend.settings.AppSettings;

@SuppressWarnings("unused")
public class TransactionHistoryDetailsFragment extends Fragment {

    private static final String TAG = TransactionHistoryDetailsFragment.class.getSimpleName();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEEE, dd-MMMM-yyyy");

    private TransactionHistoryViewModel mHistoryVM;

    private FragmentTransactionHistoryDetailsBinding mBinding;

    private NavController navController;

    private TransactionHistoryModel mHistory;

    private AppSettings mSettings;

    public TransactionHistoryDetailsFragment() {}

    private long getExtraId() {
        return requireArguments().getLong(Constants.EXTRA_ID,0);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSettings = AppSettings.get(context);
        mHistoryVM = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(TransactionHistoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTransactionHistoryDetailsBinding.inflate(inflater,container,false);
        mHistoryVM.getTransactionById(getExtraId()).observe(getViewLifecycleOwner(),this::onHistoryFetched);
        mHistoryVM.setCallbackIfTaskExists(TransactionHistoryViewModel.DELETE_HISTORIES,getViewLifecycleOwner(),this::onHistoryDeleted);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        navController = Navigation.findNavController(view);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_history_details,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.edit) {
            onClickEditHistory();
            return true;
        }
        else if (id == R.id.delete) {
            onClickDeleteHistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTitle() {
        requireActivity().setTitle(R.string.label_transaction_history_details);
    }

    private boolean isFirstNameFirst() {
        return AppSettings.FIRST_NAME_FIRST == mSettings.getPreferredPersonNameOrientation();
    }

    private void onHistoryFetched(@Nullable TransactionHistoryModel history) {
        if (null == history) {
            ToastUtil.showErrorShort(requireContext(),R.string.error_transaction_history_not_found);
            exit();
            return;
        }
        mHistory = history;
        Currency amount = history.getAmount();
        LocalDate when = history.getWhen();
        TransactionType type = history.getType();
        String description = history.getDescription();
        String payee, payer;
        Drawable payeePhoto, payerPhoto;
        if (history.getPayeeAccount() != null) {
            payee = history.getPayeeAccount().getName();
            payeePhoto = DrawableUtil.getAccountDefaultLogo(payee);
        }
        else if (history.getPayeePerson() != null) {
            PersonModel person = history.getPayeePerson();
            payee = TextUtil.getDisplayNameForPerson(person.getFirstName(),person.getLastName(),true,
                    getContext().getString(R.string.label_unknown));
            payeePhoto = DrawableUtil.getPersonDefaultPhoto(person.getFirstName(),person.getLastName(),isFirstNameFirst());
        }
        else {
            payee = getString(R.string.label_unknown);
            payeePhoto = DrawableUtil.getDrawableUnknown();
        }
        if (history.getPayerAccount() != null) {
            payer = history.getPayerAccount().getName();
            payerPhoto = DrawableUtil.getAccountDefaultLogo(payer);
        }
        else if (history.getPayerPerson() != null) {
            PersonModel person = history.getPayerPerson();
            payer = TextUtil.getDisplayNameForPerson(person.getFirstName(),person.getLastName(),isFirstNameFirst(),
                    getContext().getString(R.string.label_unknown));
            payerPhoto = DrawableUtil.getPersonDefaultPhoto(person.getFirstName(),person.getLastName(),isFirstNameFirst());
        }
        else {
            payer = getString(R.string.label_unknown);
            payerPhoto = DrawableUtil.getDrawableUnknown();
        }
        mBinding.amount.setText(TextUtil.prettyFormatCurrency(amount));
        mBinding.amountText.setText(TextUtil.currencyToText(requireContext(),amount));
        mBinding.description.setText(TextUtil.getTransactionHistoryDescription(getResources(),type,payer,payee,description));
        mBinding.timestamp.setText(getString(R.string.label_transaction_history_when,when.format(FORMATTER)));
        switch (type) {
            case INCOME: {
                hidePayerSection();
                updatePayee(R.string.label_credited_to,payee,payeePhoto);
            }
            break;
            case EXPENSE: {
                hidePayeeSection();
                updatePayer(R.string.label_debited_to,payer,payerPhoto);
            }
            break;
            case DUE:
            case PAY_BORROW: {
                updatePayee(R.string.label_paid_for,payee,payeePhoto);
                updatePayer(R.string.label_paid_from,payer,payerPhoto);
            }
            break;
            case BORROW:
            case PAY_DUE: {
                updatePayee(R.string.label_received_in,payee,payeePhoto);
                updatePayer(R.string.label_paid_by,payer,payerPhoto);
            }
            break;
            case MONEY_TRANSFER: {
                updatePayee(R.string.label_received_in,payee,payeePhoto);
                updatePayer(R.string.label_sent_from,payer,payerPhoto);
            }
            break;
            case DUE_TRANSFER:
            case BORROW_TO_DUE_TRANSFER:
            case BORROW_TRANSFER: {
                updatePayee(R.string.label_sent_to,payee,payeePhoto);
                updatePayer(R.string.label_sent_from,payer,payerPhoto);
            }
        }
    }

    private void hidePayerSection() {
        mBinding.payerPhoto.setVisibility(View.GONE);
        mBinding.payerDetails.setVisibility(View.GONE);
        mBinding.labelPayer.setVisibility(View.GONE);
        mBinding.divider3.setVisibility(View.GONE);
    }

    private void hidePayeeSection() {
        mBinding.payeePhoto.setVisibility(View.GONE);
        mBinding.payeeDetails.setVisibility(View.GONE);
        mBinding.labelPayee.setVisibility(View.GONE);
        mBinding.divider2.setVisibility(View.GONE);
    }

    private void updatePayer(@StringRes int label, CharSequence details, Drawable photo) {
        mBinding.labelPayer.setText(label);
        mBinding.payerDetails.setText(details);
        mBinding.payerPhoto.setImageDrawable(photo);
    }

    private void updatePayee(@StringRes int label, CharSequence details, Drawable photo) {
        mBinding.labelPayee.setText(label);
        mBinding.payeeDetails.setText(details);
        mBinding.payeePhoto.setImageDrawable(photo);
    }

    private void onClickEditHistory() {
        if (isStillLoading()) {
            return;
        }
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_ACTION,Constants.ACTION_UPDATE);
        args.putLong(Constants.EXTRA_ID,getExtraId());
        navController.navigate(R.id.action_history_details_to_input_history,args);
    }

    private void onClickDeleteHistory() {
        if (isStillLoading()) {
            return;
        }
        CharSequence message = getResources().getQuantityString(R.plurals.warning_delete_transaction_histories,1);
        DialogUtil.createMessageDialog(requireContext(),message,getText(R.string.no),null,
                getText(R.string.yes),(di,which)->deleteHistory(mHistory),true);
    }

    private void deleteHistory(TransactionHistoryModel history) {
        mHistoryVM.removeTransactionHistories(new long[]{history.getId()}).observe(getViewLifecycleOwner(),this::onHistoryDeleted);
    }

    private void onHistoryDeleted(DBViewModel.AsyncQueryResult result) {
        Boolean success = (Boolean) result.getResult();
        if (null == success || !success) {
            ToastUtil.showErrorShort(requireContext(),getResources().getQuantityString(R.plurals.error_delete_transaction_histories,1));
            if (BuildConfig.DEBUG) {
                Log.e(TAG,"remove transaction history failed for id="+getExtraId(),result.getError());
            }
        }
    }

    private CharSequence getDescription() {
        TransactionType type = mHistory.getType();
        String original = mHistory.getDescription();
        String payee, payer;
        if (null != mHistory.getPayeeAccount()) {
            payee = mHistory.getPayeeAccount().getName();
        }
        else if (null != mHistory.getPayeePerson()) {
            PersonModel person = mHistory.getPayeePerson();
            payee = TextUtil.getDisplayNameForPerson(person.getFirstName(),person.getLastName(),isFirstNameFirst(),getString(R.string.label_unknown));
        }
        else {
            payee = null;
        }
        if (null != mHistory.getPayerAccount()) {
            payer = mHistory.getPayerAccount().getName();
        }
        else if (null != mHistory.getPayerPerson()) {
            PersonModel person = mHistory.getPayerPerson();
            payer = TextUtil.getDisplayNameForPerson(person.getFirstName(),person.getLastName(),isFirstNameFirst(),getString(R.string.label_unknown));
        }
        else {
            payer = null;
        }
        return TextUtil.getTransactionHistoryDescription(getResources(),type,payer,payee,original);
    }

    private void exit() {
        navController.popBackStack();
    }

    private boolean isStillLoading() {
        return null == mHistory;
    }
}

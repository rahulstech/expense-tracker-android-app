package dreammaker.android.expensetracker.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.ActivityModel;
import dreammaker.android.expensetracker.activity.ActivityModelProvider;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.FragmentTransactionBasicDetailsInputBinding;
import dreammaker.android.expensetracker.dialog.DialogUtil;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.TransactionHistoryViewModel;

@SuppressWarnings("unused")
public class TransactionBasicDetailsInputFragment extends Fragment {

    private static final String TAG = TransactionBasicDetailsInputFragment.class.getSimpleName();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String KEY_PICKED_DATE = "picked_date";

    private static final String KEY_HISTORY_SET = "history_set";

    public static final String EXTRA_TRANSACTION_HISTORY = "extra_transaction_history";

    @SuppressWarnings("FieldCanBeLocal")
    private NavController navController;

    @SuppressWarnings("FieldCanBeLocal")
    private TransactionHistoryViewModel mViewModel;

    private LocalDate pickedDate = LocalDate.now();

    private FragmentTransactionBasicDetailsInputBinding mBinding;

    private TransactionHistoryModel mHistory;

    private boolean mHistorySet = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isEditOperation() && !hasExtraTransactionType()) {
            throw new IllegalArgumentException("invalid TransactionType given as argument");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(TransactionHistoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTransactionBasicDetailsInputBinding.inflate(inflater,container,false);
        if (isEditOperation()) {
            mViewModel.getTransactionById(getExtraTransactionId()).observe(getViewLifecycleOwner(),this::onTransactionHistoryFetched);
        }
        if (requireActivity() instanceof ActivityModelProvider) {
            ActivityModel model = ((ActivityModelProvider) requireActivity()).getActivityModel();
            model.addOnBackPressedCallback(getViewLifecycleOwner(),this::onBackPressed);
        }
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        navController = Navigation.findNavController(view);
        mBinding.date.setOnClickListener(v->onClickDate());
        mBinding.btnNext.setOnClickListener(v->onClickNext());
        mBinding.containerAmount.setEndIconOnClickListener(v->onToggleCalculator());
        LocalDate when = getExtraWhen();
        changeTransactionWhen(pickedDate);
        Currency amount = getExtraAmount();
        setAmount(amount);
        mBinding.description.setText(getExtraDescription());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            String pickedDateValue = savedInstanceState.getString(KEY_PICKED_DATE);
            pickedDate = LocalDate.parse(pickedDateValue,DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.ENGLISH));
            mHistorySet = savedInstanceState.getBoolean(KEY_HISTORY_SET,false);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_PICKED_DATE,pickedDate.format(DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.ENGLISH)));
        outState.putBoolean(KEY_HISTORY_SET,mHistorySet);
    }

    private void setTitle() {
        requireActivity().setTitle(R.string.label_transaction_history_amount_and_details);
    }

    private void onTransactionHistoryFetched(@Nullable TransactionHistoryModel history) {
        if (null == history) {
            ToastUtil.showErrorShort(requireContext(),R.string.error_transaction_history_not_found);
            exit();
        }
        mHistory = history;
        if (!mHistorySet) {
            //noinspection ConstantConditions
            changeTransactionWhen(history.getWhen());
            setAmount(history.getAmount());
            mBinding.description.setText(history.getDescription());
            mHistorySet = true;
        }
    }

    private boolean validateAmount() {
        String txtAmount = mBinding.amount.getEditableText().toString();
        if (TextUtils.isEmpty(txtAmount)) {
            mBinding.containerAmount.setError(getText(R.string.error_empty_input));
            return false;
        }
        Currency amount = TextUtil.tryConvertToCurrencyOrNull(txtAmount);
        if (null == amount) {
            mBinding.containerAmount.setError(getText(R.string.error_invalid_amount));
            return false;
        }
        if (Currency.ZERO.equals(amount)) {
            mBinding.containerAmount.setError(getText(R.string.error_amount_zero));
            return false;
        }
        mBinding.containerAmount.setError(null);
        return true;
    }

    private void onClickDate() {
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),(view, year, month, dayOfMonth) -> {
            LocalDate date = LocalDate.of(year, month+1,dayOfMonth);
            changeTransactionWhen(date);
        }, pickedDate.getYear(),pickedDate.getMonthValue()-1,pickedDate.getDayOfMonth());
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void changeTransactionWhen(LocalDate newDate) {
        pickedDate = newDate;
        mBinding.date.setText(newDate.format(FORMATTER));
    }

    private void setAmount(Currency amount) {
        mBinding.amount.setText(amount.toString());
    }

    private void onToggleCalculator() {
        // TODO: implement toggle calculator
    }

    private void onClickNext() {
        if (!validateAmount()) {
            return;
        }
        if (isEditOperation() && !hasAnyValueChanged()) {
            ToastUtil.showMessageShort(requireContext(),R.string.message_no_change_no_save);
            exit();
            return;
        }
        Currency amount = TextUtil.tryConvertToCurrencyOrNull(mBinding.amount.getEditableText());
        String description = mBinding.description.getEditableText().toString();
        TransactionHistoryParcelable history = new TransactionHistoryParcelable();
        history.setId(getExtraTransactionId());
        history.setWhen(pickedDate);
        //noinspection ConstantConditions
        history.setAmount(amount);
        history.setDescription(description);
        if (isEditOperation()) {
            history.setType(mHistory.getType());
            history.setPayerAccountId(mHistory.getPayerAccountId());
            history.setPayeeAccountId(mHistory.getPayeeAccountId());
            history.setPayeePersonId(mHistory.getPayeePersonId());
            history.setPayerPersonId(mHistory.getPayerPersonId());
        }
        else {
            //noinspection ConstantConditions
            history.setType(getExtraTransactionType());
            history.setPayerAccountId(getExtraPayerAccountId());
            history.setPayeeAccountId(getExtraPayeeAccountId());
            history.setPayeePersonId(getExtraPayeePersonId());
            history.setPayerPersonId(getExtraPayerPersonId());
        }
        gotoNextDestination(history);
    }

    private boolean onBackPressed() {
        if (hasAnyValueChanged()) {
            DialogUtil.createMessageDialog(requireContext(),getText(R.string.warning_not_saved),
                    getText(R.string.label_discard),null,
                    null,null,
                    getText(R.string.label_exit),(di,which)->exit(),
                    false).show();
            return true;
        }
        return false;
    }

    private boolean hasAnyValueChanged() {
        final LocalDate inWhen = pickedDate;
        final Currency inAmount = TextUtil.tryConvertToCurrencyOrNull(mBinding.amount.getEditableText());
        final String inDescription = mBinding.description.getEditableText().toString();
        LocalDate when;
        Currency amount;
        String description;
        if (isEditOperation()) {
            if (!mHistorySet) {
                return false;
            }
            when = mHistory.getWhen();
            amount = mHistory.getAmount();
            description = mHistory.getDescription();
        }
        else {
            when = getExtraWhen();
            amount = getExtraAmount();
            description = getExtraDescription();
        }
        return !when.isEqual(inWhen) || !amount.equals(inAmount)
                || !Objects.equals(description,inDescription);
    }

    private void gotoNextDestination(TransactionHistoryParcelable history) {
        Bundle args = new Bundle(requireArguments());
        args.putParcelable(EXTRA_TRANSACTION_HISTORY,history);
        if (isEditOperation()) {
            navController.navigate(R.id.action_input_history_to_edit_history,args);
            return;
        }
        TransactionType type = history.getType();
        switch (type) {
            case INCOME: {
                if (!hasExtraPayeeAccountId()) {
                    navController.navigate(R.id.action_input_history_to_account_chooser,args);
                }
                else {
                    navController.navigate(R.id.action_input_history_to_save_history,args);
                }
            }
            break;
            case EXPENSE: {
                if (!hasExtraPayerAccountId()) {
                    navController.navigate(R.id.action_input_history_to_account_chooser,args);
                }
                else {
                    navController.navigate(R.id.action_input_history_to_save_history,args);
                }
            }
            break;
            case DUE:
            case BORROW:
            case PAY_DUE:
            case PAY_BORROW:
            case MONEY_TRANSFER: {
                navController.navigate(R.id.action_input_history_to_account_chooser,args);
            }
            break;
            case DUE_TRANSFER:
            case BORROW_TO_DUE_TRANSFER:
            case BORROW_TRANSFER:{
                navController.navigate(R.id.action_input_history_to_person_chooser,args);
            }
        }
    }

    private void exit() {
        navController.popBackStack();
    }

    ////////////////////////////////////////////////////////////////
    //             Check and Get Argument Extras                 //
    //////////////////////////////////////////////////////////////

    private boolean isEditOperation() {
        return Constants.ACTION_UPDATE.equals(requireArguments().getString(Constants.EXTRA_ACTION));
    }

    private long getExtraTransactionId() {
        return requireArguments().getLong(Constants.EXTRA_ID,0);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasExtraTransactionType() {
        return requireArguments().containsKey(Constants.EXTRA_TRANSACTION_TYPE);
    }

    private TransactionType getExtraTransactionType() {
        if (!hasExtraTransactionType()) {
            return null;
        }
        String value = requireArguments().getString(Constants.EXTRA_TRANSACTION_TYPE);
        return TransactionType.valueOf(value);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasExtraPayerAccountId() {
        return requireArguments().containsKey(Constants.EXTRA_PAYER_ACCOUNT);
    }

    private Long getExtraPayerAccountId() {
        if (!hasExtraPayerAccountId()) {
            return null;
        }
        return requireArguments().getLong(Constants.EXTRA_PAYER_ACCOUNT);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasExtraPayeeAccountId() {
        return requireArguments().containsKey(Constants.EXTRA_PAYEE_ACCOUNT);
    }

    private Long getExtraPayeeAccountId() {
        if (!hasExtraPayeeAccountId()) {
            return null;
        }
        return requireArguments().getLong(Constants.EXTRA_PAYEE_ACCOUNT);
    }

    private boolean hasExtraPayerPersonId() {
        return requireArguments().containsKey(Constants.EXTRA_PAYER_PERSON);
    }

    private Long getExtraPayerPersonId() {
        if (!hasExtraPayerPersonId()) {
            return null;
        }
        return requireArguments().getLong(Constants.EXTRA_PAYER_PERSON);
    }

    private boolean hasExtraPayeePersonId() {
        return requireArguments().containsKey(Constants.EXTRA_PAYEE_PERSON);
    }

    private Long getExtraPayeePersonId() {
        if (!hasExtraPayeePersonId()) {
            return null;
        }
        return requireArguments().getLong(Constants.EXTRA_PAYEE_PERSON);
    }

    private boolean hasExtraAmount() {
        return requireArguments().containsKey(Constants.EXTRA_AMOUNT);
    }

    private Currency getExtraAmount() {
        if (!hasExtraAmount()) {
            return Currency.ZERO;
        }
        return Currency.valueOf(requireArguments().getString(Constants.EXTRA_AMOUNT,"0"));
    }

    private boolean hasExtraWhen() {
        return requireArguments().containsKey(Constants.EXTRA_WHEN);
    }

    private LocalDate getExtraWhen() {
        if (!hasExtraWhen()) {
            return LocalDate.now();
        }
        String value = requireArguments().getString(Constants.EXTRA_WHEN);
        return LocalDate.parse(value,DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.ENGLISH));
    }

    private boolean hasExtraDescription() {
        return requireArguments().containsKey(Constants.EXTRA_DESCRIPTION);
    }

    private String getExtraDescription() {
        return requireArguments().getString(Constants.EXTRA_DESCRIPTION,null);
    }
}

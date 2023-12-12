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

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.FragmentTransactionBasicDetailsInputBinding;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.viewmodel.TransactionHistoryInputViewModel;

@SuppressWarnings("unused")
public class TransactionBasicDetailsInputFragment extends Fragment {

    private static final String TAG = "TranBasicDetails";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String KEY_PICKED_DATE = "picked_date";

    public static final String EXTRA_TRANSACTION_HISTORY = "extra_transaction_history";

    private NavController navController;

    @SuppressWarnings("FieldCanBeLocal")
    private TransactionHistoryInputViewModel mViewModel;

    private LocalDate pickedDate = LocalDate.now();

    private FragmentTransactionBasicDetailsInputBinding mBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkHasTransactionTypeOrThrow();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(TransactionHistoryInputViewModel.class);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBeforeExit();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTransactionBasicDetailsInputBinding.inflate(inflater,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        mBinding.date.setOnClickListener(v->onClickDate());
        mBinding.btnNext.setOnClickListener(v->onClickNext());
        if (hasExtraWhen()) {
            LocalDate when = getExtraWhen();
            changeTransactionWhen(when);
        }
        if (hasExtraAmount()) {
            Currency amount = getExtraAmount();
            mBinding.amount.setText(amount.toString());
        }
        if (hasExtraDescription()) {
            mBinding.description.setText(getExtraDescription());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            String pickedDateValue = savedInstanceState.getString(KEY_PICKED_DATE);
            pickedDate = LocalDate.parse(pickedDateValue,DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.ENGLISH));
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_PICKED_DATE,pickedDate.format(DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.ENGLISH)));
    }

    private boolean validateAmount() {
        String txtAmount = mBinding.amount.getEditableText().toString();
        if (TextUtils.isEmpty(txtAmount)) {
            mBinding.containerAmount.setError(getString(R.string.error_no_amount));
            return false;
        }
        if (!TextUtil.isNumber(txtAmount)) {
            mBinding.containerAmount.setError(getString(R.string.error_invalid_amount));
            return false;
        }
        mBinding.containerAmount.setError(null);
        return true;
    }

    private void onClickDate() {
        DatePickerDialog dialog = new DatePickerDialog(requireContext(),(view, year, month, dayOfMonth) -> {
            LocalDate date = LocalDate.of(year,month,dayOfMonth);
            changeTransactionWhen(date);
        }, pickedDate.getYear(),pickedDate.getMonthValue(),pickedDate.getDayOfMonth());
        dialog.show();
    }

    private void changeTransactionWhen(LocalDate newDate) {
        pickedDate = newDate;
        mBinding.date.setText(newDate.format(FORMATTER));
    }

    private void onClickNext() {
        if (!validateAmount()) {
            return;
        }
        String txtAmount = mBinding.amount.getEditableText().toString();
        Currency amount = Currency.valueOf(txtAmount);
        String description = mBinding.description.getEditableText().toString();
        TransactionHistoryParcelable history = new TransactionHistoryParcelable();
        history.setId(getExtraTransactionId());
        history.setType(getExtraTransactionType());
        history.setPayerAccountId(getExtraPayerAccountId());
        history.setPayeeAccountId(getExtraPayeeAccountId());
        history.setPayeePersonId(getExtraPayeePersonId());
        history.setPayerPersonId(getExtraPayerPersonId());
        history.setWhen(pickedDate);
        history.setAmount(amount);
        history.setDescription(description);
        gotoNextDestination(history);
    }

    private void onBeforeExit() {
        if (!hasExtraDescription()) {

        }
    }

    private boolean hasAnyValueChanged() {
        return false;
    }

    private void gotoNextDestination(TransactionHistoryParcelable history) {
        TransactionType type = history.getType();
        switch (type) {
            case INCOME: {
                if (!hasExtraPayerAccountId()) {

                }
                else {

                }
            }
            break;
            case EXPENSE: {
                if (!hasExtraPayeeAccountId()) {

                }
                else {

                }
            }
            break;
            case DUE: {

            }
            break;
            case BORROW: {

            }
            break;
            case PAY_DUE: {

            }
            break;
            case PAY_BORROW: {

            }
            break;
            case MONEY_TRANSFER: {

            }
            break;
            case DUE_TRANSFER: {

            }
            break;
            case BORROW_TO_DUE_TRANSFER: {

            }
        }
    }

    ////////////////////////////////////////////////////////////////
    //             Check and Get Argument Extras                 //
    //////////////////////////////////////////////////////////////

    private long getExtraTransactionId() {
        return requireArguments().getLong(Constants.EXTRA_ID,0);
    }

    private void checkHasTransactionTypeOrThrow() {
        String value = requireArguments().getString(Constants.EXTRA_TRANSACTION_TYPE,null);
        try {
            TransactionType.valueOf(value);
        }
        catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("invalid TransactionType given as argument");
        }
    }

    private TransactionType getExtraTransactionType() {
        String value = requireArguments().getString(Constants.EXTRA_TRANSACTION_TYPE,null);
        return TransactionType.valueOf(value);
    }

    private boolean hasExtraPayerAccountId() {
        return requireArguments().containsKey(Constants.EXTRA_PAYER_ACCOUNT);
    }

    private Long getExtraPayerAccountId() {
        if (!hasExtraPayerAccountId()) {
            return null;
        }
        return requireArguments().getLong(Constants.EXTRA_PAYER_ACCOUNT);
    }

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

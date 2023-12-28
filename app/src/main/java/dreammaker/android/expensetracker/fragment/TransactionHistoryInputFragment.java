package dreammaker.android.expensetracker.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.ActivityModel;
import dreammaker.android.expensetracker.activity.ActivityModelProvider;
import dreammaker.android.expensetracker.animation.AnimatorUtil;
import dreammaker.android.expensetracker.database.entity.TransactionHistory;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.FragmentTransactionBasicDetailsInputBinding;
import dreammaker.android.expensetracker.dialog.DialogUtil;
import dreammaker.android.expensetracker.drawable.DrawableUtil;
import dreammaker.android.expensetracker.fragment.parcelable.AccountParcelable;
import dreammaker.android.expensetracker.fragment.parcelable.PersonParcelable;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ResourceUtil;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.util.ViewUtil;
import dreammaker.android.expensetracker.viewmodel.AccountViewModel;
import dreammaker.android.expensetracker.viewmodel.DBViewModel;
import dreammaker.android.expensetracker.viewmodel.PersonViewModel;
import dreammaker.android.expensetracker.viewmodel.TransactionHistoryViewModel;
import rahulstech.android.backend.settings.AppSettings;

@SuppressWarnings("unused")
public class TransactionHistoryInputFragment extends Fragment {

    private static final String TAG = TransactionHistoryInputFragment.class.getSimpleName();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String KEY_PICKED_DATE = "picked_date";

    private static final String KEY_HISTORY_SET = "history_set";

    private static final String KEY_SELECTED_PAYEE = "key_selected_payee";

    private static final String KEY_SELECTED_PAYER = "key_selected_payer";

    private static final int REQUEST_CODE_PAYEE = 1;

    private static final int REQUEST_CODE_PAYER = 2;

    @SuppressWarnings("FieldCanBeLocal")
    private NavController navController;

    @SuppressWarnings("FieldCanBeLocal")
    private TransactionHistoryViewModel mViewModel;

    private AccountViewModel mAccountVM;

    private PersonViewModel mPersonVM;

    private LocalDate pickedDate = LocalDate.now();

    private FragmentTransactionBasicDetailsInputBinding mBinding;

    private TransactionHistoryModel mHistory;

    private boolean mHistorySet = false;

    private AppSettings mSettings;

    private Parcelable mSelectedPayee;

    private Parcelable mSelectedPayer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isEditOperation() && !hasExtraTransactionType()) {
            throw new IllegalArgumentException("TransactionType not given as argument");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSettings = AppSettings.get(context);
        mViewModel = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(TransactionHistoryViewModel.class);
        mAccountVM = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(AccountViewModel.class);
        mPersonVM = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(PersonViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentTransactionBasicDetailsInputBinding.inflate(inflater,container,false);
        if (isEditOperation()) {
            mViewModel.getTransactionById(getExtraTransactionId()).observe(getViewLifecycleOwner(),this::onTransactionHistoryFetched);
        }
        else {
            Long accountId;
            Long personId;
            if (hasExtraPayeeAccountId()) {
                accountId = getExtraPayeeAccountId();
            }
            else if (hasExtraPayerAccountId()) {
                accountId = getExtraPayerAccountId();
            }
            else {
                accountId = null;
            }
            if (hasExtraPayeePersonId()) {
                personId = getExtraPayeePersonId();
            }
            else if (hasExtraPayerPersonId()) {
                personId = getExtraPayerPersonId();
            }
            else {
                personId = null;
            }
            if (accountId != null) {
                mAccountVM.getAccountById(accountId).observe(getViewLifecycleOwner(),this::onAccountFetched);
            }
            if (personId != null) {
                mPersonVM.getPersonById(personId).observe(getViewLifecycleOwner(),this::onPersonFetched);
            }
        }
        ActivityModel model = ((ActivityModelProvider) requireActivity()).getActivityModel();
        model.addOnBackPressedCallback(getViewLifecycleOwner(),this::onBackPressed);
        mViewModel.setCallbackIfTaskExists(TransactionHistoryViewModel.SAVE_HISTORY,getViewLifecycleOwner(),this::onHistorySaved);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        navController = Navigation.findNavController(view);
        //noinspection ConstantConditions
        navController.getCurrentBackStackEntry().getSavedStateHandle()
                .<Bundle>getLiveData(Constants.KEY_RESULT)
                .observe(getViewLifecycleOwner(),this::onFragmentResult);
        mBinding.date.setOnClickListener(v->onClickDate());
        mBinding.choosePayee.setOnClickListener(v->onClickChoosePayee());
        mBinding.choosePayer.setOnClickListener(v->onClickChoosePayer());
        mBinding.btnSave.setOnClickListener(v-> onClickSave());
        mBinding.containerAmount.setEndIconOnClickListener(v->onToggleCalculator());
        //mBinding.amount.setOnClickListener(v->mBinding.containerAmount.setError(null));
        LocalDate when = getExtraWhen();
        Currency amount = getExtraAmount();
        String description = getExtraDescription();
        changeTransactionWhen(pickedDate);
        setAmount(amount);
        mBinding.description.setText(description);
        if (!isEditOperation()) {
            TransactionType type = getExtraTransactionType();
            switch (type) {
                case INCOME: {
                    hidePayerSection();
                    if (null == getExtraPayeeAccountId()) {
                        preparePayeeChooser(R.string.label_choose_account,R.drawable.ic_account_24);
                    }
                }
                break;
                case EXPENSE: {
                    hidePayeeSection();
                    if (null == getExtraPayerAccountId()) {
                        preparePayerChooser(R.string.label_choose_account,R.drawable.ic_account_24);
                    }
                }
                break;
                case DUE:
                case PAY_BORROW: {
                    preparePayerChooser(R.string.label_choose_account,R.drawable.ic_account_24);
                    if (null == getExtraPayeePersonId()) {
                        preparePayeeChooser(R.string.label_choose_person,R.drawable.ic_person_24);
                    }
                }
                break;
                case DUE_TRANSFER:
                case BORROW_TRANSFER:
                case BORROW_TO_DUE_TRANSFER: {
                    preparePayeeChooser(R.string.label_choose_person,R.drawable.ic_person_24);
                }
                break;
                case BORROW:
                case PAY_DUE:{
                    preparePayeeChooser(R.string.label_choose_account,R.drawable.ic_account_24);
                    if (null == getExtraPayerPersonId()) {
                        preparePayerChooser(R.string.label_choose_person,R.drawable.ic_person_24);
                    }
                }
                break;
                case MONEY_TRANSFER: {
                    preparePayeeChooser(R.string.label_choose_account,R.drawable.ic_account_24);
                    if (null == getExtraPayerPersonId()) {
                        preparePayerChooser(R.string.label_choose_account,R.drawable.ic_account_24);
                    }
                }
            }
            setPayeePayerLabels(type);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            String pickedDateValue = savedInstanceState.getString(KEY_PICKED_DATE);
            pickedDate = LocalDate.parse(pickedDateValue,DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.ENGLISH));
            mHistorySet = savedInstanceState.getBoolean(KEY_HISTORY_SET);
            mSelectedPayee = savedInstanceState.getParcelable(KEY_SELECTED_PAYEE);
            mSelectedPayer = savedInstanceState.getParcelable(KEY_SELECTED_PAYER);
        }
        if (!isEditOperation()) {
            if (null != mSelectedPayee) {
                setPayee(mSelectedPayee);
            }
            if (null != mSelectedPayer) {
                setPayer(mSelectedPayer);
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_PICKED_DATE,pickedDate.format(DateTimeFormatter.ISO_LOCAL_DATE.withLocale(Locale.ENGLISH)));
        outState.putBoolean(KEY_HISTORY_SET,mHistorySet);
        outState.putParcelable(KEY_SELECTED_PAYEE,mSelectedPayee);
        outState.putParcelable(KEY_SELECTED_PAYER,mSelectedPayer);
    }

    private void setTitle() {
        CharSequence title;
        if (isEditOperation()) {
            title = getText(R.string.label_edit_transaction_history);
        }
        else {
            TransactionType type = getExtraTransactionType();
            switch (type) {
                case INCOME: {
                    title = getText(R.string.label_add_income);
                }
                break;
                case EXPENSE: {
                    title = getText(R.string.label_add_expense);
                }
                break;
                case DUE: {
                    title = getText(R.string.label_add_due);
                }
                break;
                case PAY_DUE: {
                    title = getText(R.string.label_add_pay_due);
                }
                break;
                case BORROW: {
                    title = getText(R.string.label_add_borrow);
                }
                break;
                case PAY_BORROW: {
                    title = getText(R.string.label_add_pay_borrow);
                }
                break;
                case DUE_TRANSFER: {
                    title = getText(R.string.label_add_due_transfer);
                }
                break;
                case BORROW_TRANSFER: {
                    title = getText(R.string.label_add_borrow_transfer);
                }
                break;
                case MONEY_TRANSFER: {
                    title = getText(R.string.label_money_transfer);
                }
                break;
                case BORROW_TO_DUE_TRANSFER: {
                    title = getText(R.string.label_add_borrow_to_due_transfer);
                }
                break;
                default: {
                    title = getText(R.string.label_add_transaction_history);
                }
            }
        }
        requireActivity().setTitle(title);
    }

    private void hidePayerSection() {
        mBinding.choosePayer.setVisibility(View.GONE);
        mBinding.labelPayer.setVisibility(View.GONE);
    }

    private void hidePayeeSection() {
        mBinding.choosePayee.setVisibility(View.GONE);
        mBinding.labelPayee.setVisibility(View.GONE);
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
            setPayeePayerLabels(history.getType());
            if (null != history.getPayeeAccount()) {
                setPayee(history.getPayeeAccount());
            }
            else if (null != history.getPayeePerson()) {
                setPayee(history.getPayeePerson());
            }
            else {
                hidePayeeSection();
            }
            if (null != history.getPayerAccount()) {
                setPayer(history.getPayerAccount());
            }
            else if (null != history.getPayerPerson()) {
                setPayer(history.getPayerPerson());
            }
            else {
                hidePayerSection();
            }
            mHistorySet = true;
        }
    }

    private void onAccountFetched(@Nullable AccountModel account) {
        TransactionType type = getExtraTransactionType();
        switch (type) {
            case INCOME:
            case PAY_DUE:
            case BORROW: {
                setPayee(account);
            }
            break;
            case EXPENSE:
            case DUE:
            case PAY_BORROW:
            case MONEY_TRANSFER: {
                setPayer(account);
            }
        }
    }

    private void onPersonFetched(@Nullable PersonModel person) {
        TransactionType type = getExtraTransactionType();
        switch (type) {
            case PAY_DUE:
            case BORROW:
            case BORROW_TRANSFER:
            case DUE_TRANSFER:
            case BORROW_TO_DUE_TRANSFER:{
                setPayer(person);
            }
            break;
            case DUE:
            case PAY_BORROW: {
                setPayee(person);
            }
        }
    }

    private void onFragmentResult(Bundle result) {
        int code = result.getInt(Constants.KEY_REQUEST_CODE);
        Parcelable selection = result.getParcelable(Constants.KEY_RESULT);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onFragmentResult: code="+code+" selection="+selection);
        }
        if (null == selection) {
            return;
        }
        if (code == REQUEST_CODE_PAYEE) {
            setPayee(selection);
        }
        else {
            setPayer(selection);
        }
    }

    private void setPayeePayerLabels(TransactionType type) {
        switch (type) {
            case INCOME: {
                mBinding.labelPayee.setText(R.string.label_credit_to);
            }
            break;
            case EXPENSE: {
                mBinding.labelPayer.setText(R.string.label_debit_to);
            }
            break;
            case DUE:
            case PAY_BORROW: {
                mBinding.labelPayee.setText(R.string.label_pay_for);
                mBinding.labelPayer.setText(R.string.label_pay_from);
            }
            break;
            case BORROW:
            case PAY_DUE: {
                mBinding.labelPayee.setText(R.string.label_receive_in);
                mBinding.labelPayer.setText(R.string.label_paid_by);
            }
            break;
            case MONEY_TRANSFER: {
                mBinding.labelPayee.setText(R.string.label_receive_in);
                mBinding.labelPayer.setText(R.string.label_send_from);
            }
            break;
            case DUE_TRANSFER:
            case BORROW_TO_DUE_TRANSFER:
            case BORROW_TRANSFER: {
                mBinding.labelPayee.setText(R.string.label_send_to);
                mBinding.labelPayer.setText(R.string.label_send_from);
            }
        }
    }

    private void setPayee(Object payee) {
        Button view = mBinding.choosePayee;
        if (payee instanceof AccountModel) {
            AccountModel account = (AccountModel) payee;
            mSelectedPayee = new AccountParcelable(account);
            setAccount(account,view);
        }
        else if (payee instanceof PersonModel) {
            PersonModel person = (PersonModel) payee;
            mSelectedPayee = new PersonParcelable(person);
            setPerson(person,view);
        }
        else if (payee instanceof AccountParcelable) {
            AccountParcelable parcelable = (AccountParcelable) payee;
            mSelectedPayee = parcelable;
            setAccount(parcelable.asAccountModel(),view);
        }
        else if (payee instanceof PersonParcelable) {
            PersonParcelable parcelable = (PersonParcelable) payee;
            mSelectedPayee = parcelable;
            setPerson(parcelable.asPersonModel(),view);
        }
        else {
            mSelectedPayee = null;
            prepareChooser(view,getText(R.string.label_unknown),DrawableUtil.getDrawableUnknown(),false);
        }
    }

    private void setPayer(Object payer) {
        Button view = mBinding.choosePayer;
        if (payer instanceof AccountModel) {
            AccountModel model = (AccountModel) payer;
            mSelectedPayer = new AccountParcelable(model);
            setAccount(model,view);
        }
        else if (payer instanceof PersonModel) {
            PersonModel model = (PersonModel) payer;
            mSelectedPayer = new PersonParcelable(model);
            setPerson(model,view);
        }
        else if (payer instanceof AccountParcelable) {
            AccountParcelable parcelable = (AccountParcelable) payer;
            mSelectedPayer = parcelable;
            setAccount(parcelable.asAccountModel(),view);
        }
        else if (payer instanceof PersonParcelable) {
            PersonParcelable parcelable = (PersonParcelable) payer;
            mSelectedPayer = parcelable;
            setPerson(parcelable.asPersonModel(),view);
        }
        else {
            mSelectedPayer = null;
            prepareChooser(view,getText(R.string.label_unknown),DrawableUtil.getDrawableUnknown(),false);
        }
    }

    private Long getSelectedPayeeId() {
        if (mSelectedPayee instanceof AccountParcelable) {
            return ((AccountParcelable) mSelectedPayee).getId();
        }
        else if (mSelectedPayee instanceof PersonParcelable) {
            return ((PersonParcelable) mSelectedPayee).getId();
        }
        return null;
    }

    private Long getSelectedPayerId() {
        if (mSelectedPayer instanceof AccountParcelable) {
            return ((AccountParcelable) mSelectedPayer).getId();
        }
        else if (mSelectedPayer instanceof PersonParcelable) {
            return ((PersonParcelable) mSelectedPayer).getId();
        }
        return null;
    }

    private void setAccount(AccountModel account, Button view) {
        CharSequence name = account.getName();
        Drawable logo = DrawableUtil.getAccountDefaultLogo(account.getName());
        boolean clickable;
        if (isEditOperation()) {
            clickable = false;
        }
        else {
            Long accountId;
            if (hasExtraPayeeAccountId()) {
                accountId = getExtraPayeeAccountId();
            }
            else if (hasExtraPayerAccountId()) {
                accountId = getExtraPayerAccountId();
            }
            else {
                accountId = null;
            }
            clickable = !account.getId().equals(accountId);
        }
        prepareChooser(view,name,logo,clickable);
    }

    private void setPerson(PersonModel person, Button view) {
        CharSequence name = TextUtil.getDisplayNameForPerson(person.getFirstName(),person.getLastName(),
                isFirstNameFirst(), getString(R.string.label_unknown));
        Drawable photo = DrawableUtil.getPersonDefaultPhoto(person.getFirstName(),person.getLastName(),isFirstNameFirst());
        boolean clickable;
        if (isEditOperation()) {
            clickable = false;
        }
        else {
            Long personId;
            if (hasExtraPayeePersonId()) {
                personId = getExtraPayeePersonId();
            }
            else if (hasExtraPayerPersonId()) {
                personId = getExtraPayerPersonId();
            }
            else {
                personId = null;
            }
            clickable = !person.getId().equals(personId);
        }
        prepareChooser(view,name,photo,clickable);
    }

    private void preparePayeeChooser(@StringRes int nameRes, @DrawableRes int drawableRes) {
        prepareChooser(mBinding.choosePayee,nameRes,drawableRes);
    }

    private void preparePayerChooser(@StringRes int nameRes, @DrawableRes int drawableRes) {
        prepareChooser(mBinding.choosePayer,nameRes,drawableRes);
    }

    private void prepareChooser(Button view, @StringRes int nameRes, @DrawableRes int drawableRes) {
        CharSequence name = getText(nameRes);
        Drawable drawable = ResourceUtil.getDrawable(requireContext(), drawableRes);
        view.setText(name);
        ViewUtil.setTextViewLeftDrawable(view,drawable, ResourceUtil.dpToPixed(getResources(),36));
        view.setClickable(true);
    }

    private void prepareChooser(Button view, CharSequence name, Drawable drawable, boolean clickable) {
        view.setText(name);
        ViewUtil.setTextViewLeftDrawableNoTint(view,drawable,ResourceUtil.dpToPixed(getResources(),36));
        view.setClickable(clickable);
    }

    private boolean validate() {
        if (!validateAmount()) {
            return false;
        }
        if (isEditOperation()) {
            return true;
        }
        TransactionType type = getExtraTransactionType();
        StringBuilder message = new StringBuilder();
        boolean payeeSelected = true, payerSelected = true;
        switch (type) {
            case INCOME: {
                if (null == mSelectedPayee) {
                    payeeSelected = false;
                    message.append(
                            getString(R.string.error_field_not_set,getString(R.string.label_credit_to)));
                }
            }
            break;
            case EXPENSE: {
                if (null == mSelectedPayer) {
                    payerSelected = false;
                    message.append(
                            getString(R.string.error_field_not_set,getString(R.string.label_debit_to)));
                }
            }
            break;
            case DUE:
            case PAY_BORROW: {
                if (null == mSelectedPayee) {
                    payeeSelected = false;
                    message.append(getString(R.string.error_field_not_set,getString(R.string.label_pay_for)))
                            .append("\n");
                }
                if (null == mSelectedPayer) {
                    payerSelected = false;
                    message.append(
                            getString(R.string.error_field_not_set,getString(R.string.label_pay_from)));
                }
            }
            break;
            case BORROW:
            case PAY_DUE: {
                if (null == mSelectedPayee) {
                    payeeSelected = false;
                    message.append(getString(R.string.error_field_not_set,getString(R.string.label_receive_in)))
                            .append("\n");
                }
                if (null == mSelectedPayer) {
                    payerSelected = false;
                    message.append(
                            getString(R.string.error_field_not_set,getString(R.string.label_paid_by)));
                }
            }
            break;
            case MONEY_TRANSFER: {
                if (null == mSelectedPayee) {
                    payeeSelected = false;
                    message.append(getString(R.string.error_field_not_set,getString(R.string.label_receive_in)))
                            .append("\n");
                }
                if (null == mSelectedPayer) {
                    payerSelected = false;
                    message.append(
                            getString(R.string.error_field_not_set,getString(R.string.label_send_from)));
                }
            }
            break;
            case DUE_TRANSFER:
            case BORROW_TO_DUE_TRANSFER:
            case BORROW_TRANSFER: {
                if (null == mSelectedPayee) {
                    payeeSelected = false;
                    message.append(getString(R.string.error_field_not_set,getString(R.string.label_send_to)))
                            .append("\n");
                }
                if (null == mSelectedPayer) {
                    payeeSelected = false;
                    message.append(
                            getString(R.string.error_field_not_set,getString(R.string.label_send_from)));
                }
            }
        }
        boolean valid = payeeSelected && payerSelected;
        if (!valid) {
            ToastUtil.showErrorShort(requireContext(),message);
        }
        if (!payeeSelected) {
            AnimatorUtil.shakeX(mBinding.choosePayee,12,5)
                    .setDuration(AnimatorUtil.SHORT_ANIM_DURATION).start();
        }
        if (!payerSelected) {
            AnimatorUtil.shakeX(mBinding.choosePayer,12,5)
                    .setDuration(AnimatorUtil.SHORT_ANIM_DURATION).start();
        }
        return valid;
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

    private void onClickChoosePayee() {
        TransactionType type = getExtraTransactionType();
        Bundle args = new Bundle();
        args.putInt(Constants.KEY_REQUEST_CODE,REQUEST_CODE_PAYEE);
        args.putString(Constants.EXTRA_ACTION,Constants.ACTION_PICK);
        if (null != mSelectedPayee) {
            args.putParcelable(Constants.EXTRA_INITIALS,mSelectedPayee);
        }
        switch (type) {
            case INCOME:
            case MONEY_TRANSFER:
            case BORROW:
            case PAY_DUE:{
                navController.navigate(R.id.action_input_history_to_account_chooser,args);
            }
            break;
            case DUE:
            case PAY_BORROW:
            case DUE_TRANSFER:
            case BORROW_TRANSFER:
            case BORROW_TO_DUE_TRANSFER: {
                navController.navigate(R.id.action_input_history_to_person_chooser,args);
            }
        }
    }

    private void onClickChoosePayer() {
        TransactionType type = getExtraTransactionType();
        Bundle args = new Bundle();
        args.putInt(Constants.KEY_REQUEST_CODE,REQUEST_CODE_PAYER);
        args.putString(Constants.EXTRA_ACTION,Constants.ACTION_PICK);
        if (null != mSelectedPayer) {
            args.putParcelable(Constants.EXTRA_INITIALS,mSelectedPayer);
        }
        switch (type) {
            case EXPENSE:
            case MONEY_TRANSFER:
            case DUE:
            case PAY_BORROW:{
                navController.navigate(R.id.action_input_history_to_account_chooser,args);
            }
            break;
            case BORROW:
            case PAY_DUE:
            case DUE_TRANSFER:
            case BORROW_TRANSFER:
            case BORROW_TO_DUE_TRANSFER: {
                navController.navigate(R.id.action_input_history_to_person_chooser,args);
            }
        }
    }

    private void onClickSave() {
        if (isEditOperation() && !mHistorySet) {
            return;
        }
        if (!validate()) {
            return;
        }
        if (isEditOperation() && !hasAnyValueChanged()) {
            ToastUtil.showMessageShort(requireContext(),R.string.message_no_change_no_save);
            exit();
            return;
        }
        Currency amount = TextUtil.tryConvertToCurrencyOrNull(mBinding.amount.getEditableText());
        String description = mBinding.description.getEditableText().toString();
        TransactionHistory history = new TransactionHistory();
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
            history.setType(getExtraTransactionType());
            if (mSelectedPayee instanceof AccountParcelable) {
                history.setPayeeAccountId(((AccountParcelable) mSelectedPayee).getId());
            }
            else if (mSelectedPayee instanceof PersonParcelable) {
                history.setPayeePersonId(((PersonParcelable) mSelectedPayee).getId());
            }
            if (mSelectedPayer instanceof AccountParcelable) {
                history.setPayerAccountId(((AccountParcelable) mSelectedPayer).getId());
            }
            else if (mSelectedPayer instanceof PersonParcelable) {
                history.setPayerPersonId(((PersonParcelable) mSelectedPayer).getId());
            }
        }
        mViewModel.saveTransactionHistory(history).observe(getViewLifecycleOwner(),this::onHistorySaved);
    }

    private void onHistorySaved(DBViewModel.AsyncQueryResult result) {
        final TransactionHistory history = (TransactionHistory) result.getResult();
        if (null == history) {
            ToastUtil.showErrorShort(requireContext(),R.string.error_save);
            Log.e(TAG, "onHistorySaved: ", result.getError());
            return;
        }
        ToastUtil.showSuccessShort(requireContext(),R.string.transaction_history_save_successful);
        if (isEditOperation()) {
            exit();
        }
        else {
            showHistoryDetails(history);
        }
    }

    private void showHistoryDetails(TransactionHistory history) {
        Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_ID,history.getId());
        navController.navigate(R.id.action_input_history_to_history_details,args);
    }

    private boolean onBackPressed() {
        if (isEditOperation() && !mHistorySet) {
            return false;
        }
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

    /**
     * Checks weather input values are changed. In case of edit operation
     * i.e. action is {@link Constants#ACTION_UPDATE} it
     * matches against the fetched TransactionHistoryModel; but it does
     * not checks if the TransactionHistoryModel is fetched or not. Caller need
     * to ensure this first otherwise it will throws {@link NullPointerException}.
     * In case of insert operation i.e. action is {@link Constants#ACTION_INSERT}
     * it checks against the fragment arguments i.e. extras and the picked values.
     *
     * @return true if at least any field has changed, false otherwise
     */
    private boolean hasAnyValueChanged() {
        final LocalDate inWhen = pickedDate;
        final Currency inAmount = TextUtil.tryConvertToCurrencyOrNull(mBinding.amount.getEditableText());
        final CharSequence inDescription = mBinding.description.getEditableText();
        LocalDate when;
        Currency amount;
        String description;
        if (isEditOperation()) {
            when = mHistory.getWhen();
            amount = mHistory.getAmount();
            description = mHistory.getDescription();
            return !when.isEqual(inWhen) || !amount.equals(inAmount)
                    || !TextUtil.equals(description,inDescription);
        }
        else {
            when = getExtraWhen();
            amount = getExtraAmount();
            description = getExtraDescription();
            Long payeeId, payerId;
            Long inPayeeId, inPayerId;
            payerId = getExtraPayerId();
            payeeId = getExtraPayeeId();
            inPayeeId = getSelectedPayeeId();
            inPayerId = getSelectedPayerId();
            return !when.isEqual(inWhen) || !amount.equals(inAmount)
                    || !TextUtil.equals(description,inDescription)
                    || !Objects.equals(payeeId,inPayeeId) || !Objects.equals(payerId,inPayerId);
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

    @NonNull
    private TransactionType getExtraTransactionType() {
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

    private boolean hasExtraPayeeId() {
        return hasExtraPayeeAccountId() || hasExtraPayeePersonId();
    }

    private Long getExtraPayeeId() {
        if (hasExtraPayeeAccountId()) {
            return getExtraPayeeAccountId();
        }
        else if (hasExtraPayeePersonId()) {
            return getExtraPayeePersonId();
        }
        return null;
    }

    private boolean hasExtraPayerId() {
        return hasExtraPayerAccountId() || hasExtraPayerPersonId();
    }

    private Long getExtraPayerId() {
        if (hasExtraPayerAccountId()) {
            return getExtraPayerAccountId();
        }
        else if (hasExtraPayerPersonId()) {
            return getExtraPayerPersonId();
        }
        return null;
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

    private boolean isFirstNameFirst() {
        return AppSettings.FIRST_NAME_FIRST == mSettings.getPreferredPersonNameOrientation();
    }
}

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
import android.widget.Checkable;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SimpleArrayMap;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.FragmentFilterHistoryBinding;
import dreammaker.android.expensetracker.drawable.DrawableUtil;
import dreammaker.android.expensetracker.fragment.parcelable.AccountParcelable;
import dreammaker.android.expensetracker.fragment.parcelable.HistoryFilterData;
import dreammaker.android.expensetracker.fragment.parcelable.PersonParcelable;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.DateTimeUtil;
import dreammaker.android.expensetracker.widget.ChipWithImage;
import rahulstech.android.backend.settings.AppSettings;

@SuppressWarnings("unused")
public class FilterHistoryFragment extends Fragment {

    private static final String TAG = FilterHistoryFragment.class.getSimpleName();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private static final EnumSet<TransactionType> ALL_TRANSACTION_TYPE = EnumSet.allOf(TransactionType.class);

    private static final String KEY_SELECTED_ACCOUNTS = "key_selected_accounts";

    private static final String KEY_SELECTED_PEOPLE = "key_selected_people";

    private static final String KEY_FILTER_DATA = "key_filter_data";

    private static final String TAG_DATE_RANGE_PICKER = "tag_date_range_picker";

    public static final String KEY_CAN_CHOOSE_ACCOUNTS = "key_can_choose_accounts";

    public static final String KEY_CAN_CHOOSE_PEOPLE = "key_can_choose_people";

    private static final int REQUEST_ACCOUNTS = 1;

    private static final int REQUEST_PEOPLE = 2;

    private final View.OnClickListener onClickCloseAccountChip = v ->{
        Chip chip = (Chip) v;
        AccountParcelable account = (AccountParcelable) chip.getTag();
        onRemoveAccountChip(chip,account);
    };

    private final View.OnClickListener onClickClosePersonChip = v ->{
        Chip chip = (Chip) v;
        PersonParcelable person = (PersonParcelable) chip.getTag();
        onRemovePersonChip(chip,person);
    };

    private NavController navController;

    private FragmentFilterHistoryBinding mBinding;

    private LocalDate[] mPreferredRange;

    private LocalDate mRangeStart;

    private LocalDate mRangeEnd;

    private SimpleArrayMap<TransactionType,Checkable> mTTypeViewMap;

    private ArrayList<AccountParcelable> mSelectedAccounts;

    private ArrayList<PersonParcelable> mSelectedPeople;

    private AppSettings mSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSettings = AppSettings.get(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentFilterHistoryBinding.inflate(inflater,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        mBinding.dateRange.setOnClickListener(v->onClickPickDateRange());
        mBinding.addAccounts.setOnClickListener(v->onClickAddAccounts());
        mBinding.addPeople.setOnClickListener(v->onClickAddPeople());
        mBinding.applyFilter.setOnClickListener(v->onClickFilter());
        prepareTransactionTypeChips();
        showSectionAccountsIfNeeded();
        showSectionPeopleIfNeeded();

        //noinspection ConstantConditions
        navController.getCurrentBackStackEntry().getSavedStateHandle()
                .<Bundle>getLiveData(Constants.KEY_RESULT)
                .observe(getViewLifecycleOwner(),this::onFragmentResult);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            HistoryFilterData data = savedInstanceState.getParcelable(KEY_FILTER_DATA);
            mRangeStart = data.getRangeStart();
            mRangeEnd = data.getRangeEnd();
            mSelectedAccounts = data.getAccounts();
            mSelectedPeople = data.getPeople();
        }
        else if (hasExtraInitials()){
            HistoryFilterData data = requireArguments().getParcelable(Constants.EXTRA_INITIALS);
            EnumSet<TransactionType> types = data.getTypes();
            mRangeStart = data.getRangeStart();
            mRangeEnd = data.getRangeEnd();
            mSelectedAccounts = data.getAccounts();
            mSelectedPeople = data.getPeople();
            setSelectedTypes(types);
        }
        setDateRange(mRangeStart,mRangeEnd);
        setSelectedAccounts(mSelectedAccounts);
        setSelectedPeople(mSelectedPeople);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        HistoryFilterData data = prepareHistoryFilterData();
        outState.putParcelable(KEY_FILTER_DATA,data);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_filter_history,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.reset) {
            onClickResetFilter();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean hasExtraInitials() {
        return null != requireArguments().getParcelable(Constants.EXTRA_INITIALS);
    }

    private boolean isFirstNameFirst() {
        return mSettings.getPreferredPersonNameOrientation() == AppSettings.FIRST_NAME_FIRST;
    }

    private void showSectionAccountsIfNeeded() {
        boolean canChoose = requireArguments().getBoolean(KEY_CAN_CHOOSE_ACCOUNTS,true);
        if (canChoose) {
            mBinding.divider2.setVisibility(View.VISIBLE);
            mBinding.addAccounts.setVisibility(View.VISIBLE);
            mBinding.containerAccounts.setVisibility(View.VISIBLE);
        }
    }

    private void showSectionPeopleIfNeeded() {
        boolean canChoose = requireArguments().getBoolean(KEY_CAN_CHOOSE_PEOPLE,true);
        if (canChoose) {
            mBinding.divider3.setVisibility(View.VISIBLE);
            mBinding.addPeople.setVisibility(View.VISIBLE);
            mBinding.containerPeople.setVisibility(View.VISIBLE);
        }
    }

    private void prepareTransactionTypeChips() {
        final ChipGroup container = mBinding.containerTypes;
        final int count = container.getChildCount();
        mTTypeViewMap = new SimpleArrayMap<>(count);
        Chip child;
        for (int i=0; i<count; i++) {
            child = (Chip) container.getChildAt(i);
            int id = child.getId();
            if (id == R.id.type_income) {
                mTTypeViewMap.put(TransactionType.INCOME,child);
            }
            else if (id == R.id.type_expense) {
                mTTypeViewMap.put(TransactionType.EXPENSE,child);
            }
            else if (id == R.id.type_due) {
                mTTypeViewMap.put(TransactionType.DUE,child);
            }
            else if (id == R.id.type_borrow) {
                mTTypeViewMap.put(TransactionType.BORROW,child);
            }
            else if (id == R.id.type_pay_due) {
                mTTypeViewMap.put(TransactionType.PAY_DUE,child);
            }
            else if (id == R.id.type_pay_borrow) {
                mTTypeViewMap.put(TransactionType.PAY_BORROW,child);
            }
            else if (id == R.id.type_money_transfer) {
                mTTypeViewMap.put(TransactionType.MONEY_TRANSFER,child);
            }
            else if (id == R.id.type_due_transfer) {
                mTTypeViewMap.put(TransactionType.DUE_TRANSFER,child);
            }
            else if (id == R.id.type_borrow_transfer) {
                mTTypeViewMap.put(TransactionType.BORROW_TRANSFER,child);
            }
            else if (id == R.id.type_borrow_to_due_transfer) {
                mTTypeViewMap.put(TransactionType.BORROW_TO_DUE_TRANSFER,child);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ///                       Setter Methods                            ///
    //////////////////////////////////////////////////////////////////////

    private void setDateRange(LocalDate start, LocalDate end) {
        LocalDate[] rage = getShowHistoryDateRange();
        mRangeStart = start;
        mRangeEnd = end;
        LocalDate min, max;
        if (null == start) {
            min = rage[0];
        }
        else {
            min = start;
        }
        if (null == end) {
            max = rage[1];
        }
        else {
            max = end;
        }
        String text = min.format(FORMATTER)+" - "+max.format(FORMATTER);
        mBinding.dateRange.setText(text);
    }

    private void setSelectedTypes(final EnumSet<TransactionType> types) {
        if (null == types || types.isEmpty()) {
            return;
        }
        EnumSet<TransactionType> unchecked = EnumSet.copyOf(ALL_TRANSACTION_TYPE);
        unchecked.removeAll(types);
        for (TransactionType type : unchecked) {
            Checkable view = mTTypeViewMap.get(type);
            if (null != view) {
                view.setChecked(false);
            }
        }
    }

    private void setSelectedAccounts(ArrayList<AccountParcelable> accounts) {
        mBinding.containerAccounts.removeAllViews();
        if (null != mSelectedAccounts){
            mSelectedAccounts.clear();
            mSelectedAccounts = null;
        }
        if (null == accounts) {
            return;
        }
        mSelectedAccounts = accounts;
        for (AccountParcelable account : accounts) {
            setAccount(account);
        }
    }

    private void setSelectedPeople(ArrayList<PersonParcelable> people) {
        mBinding.containerPeople.removeAllViews();
        if (null != mSelectedPeople) {
            mSelectedPeople.clear();
            mSelectedPeople = null;
        }
        if (null == people) {
            return;
        }
        mSelectedPeople = people;
        for (PersonParcelable person : people) {
            setPerson(person);
        }
    }

    private void setAccount(AccountParcelable account) {
        ChipWithImage chip = new ChipWithImage(requireContext());
        String text = account.getName();
        Drawable icon = DrawableUtil.getAccountDefaultLogo(account.getName());
        chip.setText(text);
        chip.setImage(null,icon);
        chip.setTag(account);
        chip.setOnCloseIconClickListener(onClickCloseAccountChip);
        mBinding.containerAccounts.addView(chip);
    }

    private void setPerson(PersonParcelable person) {
        ChipWithImage chip = new ChipWithImage(requireContext());
        String text = TextUtil.getDisplayNameForPerson(person.getFirstName(),person.getLastName(),isFirstNameFirst(),null);
        Drawable icon = DrawableUtil.getPersonDefaultPhoto(person.getFirstName(),person.getLastName(),isFirstNameFirst());
        chip.setText(text);
        chip.setImage(null,icon);
        chip.setTag(person);
        chip.setOnCloseIconClickListener(onClickClosePersonChip);
        mBinding.containerPeople.addView(chip);
    }

    @SuppressWarnings("ConstantConditions")
    private void setResult(HistoryFilterData result) {
        navController.getPreviousBackStackEntry().getSavedStateHandle()
                .set(Constants.KEY_RESULT,result);
    }

    ////////////////////////////////////////////////////////////////////////
    ///                       Getter Methods                            ///
    //////////////////////////////////////////////////////////////////////

    private HistoryFilterData prepareHistoryFilterData() {
        EnumSet<TransactionType> types = getSelectedTypes();
        HistoryFilterData data = new HistoryFilterData();
        data.setRangeStart(mRangeStart);
        data.setRangeEnd(mRangeEnd);
        data.setTypes(types);
        data.setAccounts(mSelectedAccounts);
        data.setPeople(mSelectedPeople);
        return data;
    }

    private EnumSet<TransactionType> getSelectedTypes() {
        EnumSet<TransactionType> set = EnumSet.noneOf(TransactionType.class);
        for (TransactionType type : ALL_TRANSACTION_TYPE) {
            Checkable view = mTTypeViewMap.get(type);
            if (null != view && view.isChecked()) {
                set.add(type);
            }
        }
        return set;
    }

    /**
     * index 0 -> date start
     * index 1 -> date end
     */
    protected LocalDate[] getShowHistoryDateRange() {
        if (null == mPreferredRange) {
            mPreferredRange = mSettings.getShowHistoryDateRange(LocalDate.now());
        }
        return mPreferredRange;
    }

    ////////////////////////////////////////////////////////////////////////
    ///                       Event Handler                             ///
    //////////////////////////////////////////////////////////////////////

    private void onRemoveAccountChip(View chip, AccountParcelable account) {
        mBinding.containerAccounts.removeView(chip);
        mSelectedAccounts.remove(account);
    }

    private void onRemovePersonChip(View chip, PersonParcelable person) {
        mBinding.containerPeople.removeView(chip);
        mSelectedPeople.remove(person);
    }

    private void onFragmentResult(@NonNull Bundle result) {
        final int code = result.getInt(Constants.KEY_REQUEST_CODE);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onFragmentResult: code="+code+" hasResult="+result.containsKey(Constants.KEY_RESULT));
        }
        if (code == REQUEST_ACCOUNTS) {
            ArrayList<AccountParcelable> accounts = result.getParcelableArrayList(Constants.KEY_RESULT);
            setSelectedAccounts(accounts);
        }
        else if (code == REQUEST_PEOPLE) {
            ArrayList<PersonParcelable> people = result.getParcelableArrayList(Constants.KEY_RESULT);
            setSelectedPeople(people);
        }
    }

    private void onClickPickDateRange() {
        if (null != getChildFragmentManager().findFragmentByTag(TAG_DATE_RANGE_PICKER)) {
            return;
        }
        LocalDate[] range = getShowHistoryDateRange();
        LocalDate start = range[0];
        LocalDate end = range[1];
        long startMillis = DateTimeUtil.toUTCMillis(start);
        long endMillis = DateTimeUtil.toUTCMillis(end);
        long selectionStartMillis = null == mRangeStart ? startMillis : DateTimeUtil.toUTCMillis(mRangeStart);
        long selectionEndMillis = null == mRangeEnd ? endMillis : DateTimeUtil.toUTCMillis(mRangeEnd);
        MaterialDatePicker<Pair<Long,Long>> picker = MaterialDatePicker.Builder.dateRangePicker()
                .setSelection(new Pair<>(selectionStartMillis,selectionEndMillis))
                .setCalendarConstraints(new CalendarConstraints.Builder().setStart(startMillis).setEnd(endMillis).build())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            LocalDate rangeStart = DateTimeUtil.utcMillisToLocalDateTime(selection.first).toLocalDate();
            LocalDate rangeEnd = DateTimeUtil.utcMillisToLocalDateTime(selection.second).toLocalDate();
            setDateRange(rangeStart,rangeEnd);
        });

        picker.showNow(getChildFragmentManager(),TAG_DATE_RANGE_PICKER);
    }

    private void onClickAddAccounts() {
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_ACTION,Constants.ACTION_PICK_MULTIPLE);
        args.putInt(Constants.KEY_REQUEST_CODE,REQUEST_ACCOUNTS);
        if (null != mSelectedAccounts) {
            args.putParcelableArrayList(Constants.EXTRA_INITIALS,new ArrayList<>(mSelectedAccounts));
        }
        navController.navigate(R.id.action_filter_history_to_account_chooser,args);
    }

    private void onClickAddPeople() {
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_ACTION,Constants.ACTION_PICK_MULTIPLE);
        args.putInt(Constants.KEY_REQUEST_CODE,REQUEST_PEOPLE);
        if (null != mSelectedPeople) {
            args.putParcelableArrayList(Constants.EXTRA_INITIALS,new ArrayList<>(mSelectedPeople));
        }
        navController.navigate(R.id.action_filter_history_to_person_chooser,args);
    }

    private void onClickFilter() {
        HistoryFilterData data = prepareHistoryFilterData();
        setResult(data);
        exit();
    }

    private void onClickResetFilter() {
        setResult(null);
        exit();
    }

    private void exit() {
        navController.popBackStack();
    }
}

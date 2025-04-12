package dreammaker.android.expensetracker.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;
import dreammaker.android.expensetracker.util.ResultCallback;
import dreammaker.android.expensetracker.viewmodel.TransactionsViewModel;

import static dreammaker.android.expensetracker.viewmodel.TransactionsViewModel.FilterTransactionParams.DATE_ALL;
import static dreammaker.android.expensetracker.viewmodel.TransactionsViewModel.FilterTransactionParams.DATE_CUSTOM_RANGE;
import static dreammaker.android.expensetracker.viewmodel.TransactionsViewModel.FilterTransactionParams.DATE_LAST_MONTH;
import static dreammaker.android.expensetracker.viewmodel.TransactionsViewModel.FilterTransactionParams.DATE_LAST_WEEK;
import static dreammaker.android.expensetracker.viewmodel.TransactionsViewModel.FilterTransactionParams.DATE_SPECIFIC;
import static dreammaker.android.expensetracker.viewmodel.TransactionsViewModel.FilterTransactionParams.DATE_THIS_MONTH;
import static dreammaker.android.expensetracker.viewmodel.TransactionsViewModel.FilterTransactionParams.DATE_THIS_WEEK;
import static dreammaker.android.expensetracker.viewmodel.TransactionsViewModel.FilterTransactionParams.DATE_TODAY;
import static dreammaker.android.expensetracker.viewmodel.TransactionsViewModel.FilterTransactionParams.DATE_YESTERDAY;

public class FilterTransactionScreenDates extends BaseFragment<FilterTransactionScreenDates.FilterTransactionScreenDatesViewHolder>{

    private static final boolean DEBUG = true;

    private static final String TAG = "FTScreenDates";

    private static final String DATE_PATTERN = "dd-MMMM-yyyy";

    private TransactionsViewModel viewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setFragmentTitle(R.string.label_filter_transaction);
        if (null != context) {
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(TransactionsViewModel.class);
        }
    }

    @NonNull
    @Override
    protected FilterTransactionScreenDatesViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return new FilterTransactionScreenDatesViewHolder(inflater.inflate(R.layout.scree_filter_dates,
                container,
                false));
    }

    @Override
    protected void onFragmentViewHolderCreated(@NonNull FilterTransactionScreenDatesViewHolder vh) {
        viewModel.getWorkingFilterParamsLiveData().observe(this,
                params -> populateWithInitialValues(vh,params) );
        vh.filterDateGroup.setOnCheckedChangeListener((group,checked) ->
                onFilterDateOptionChange(group.getCheckedRadioButtonId()));
        vh.date.setOnClickListener(v -> onClickDatePickerButton((Button)v));
        vh.minDate.setOnClickListener(v -> onClickDatePickerButton((Button) v));
        vh.maxDate.setOnClickListener(v -> onClickDatePickerButton((Button) v));
    }

    private void onFilterDateOptionChange(@IdRes int selected) {
        final FilterTransactionScreenDatesViewHolder vh = getViewHolder();
        final TransactionsViewModel.FilterTransactionParams params = viewModel.getWorkingFilterParams();
        if (DEBUG) {
            Log.d(TAG,"oldDateType="+params.getDateType()+" | oldMinDate= "+params.getMinDate()+
                    " | oldMaxDate="+params.getMaxDate());
        }
        if (vh.rbSpecific.getId() == selected) {
            if (DEBUG) Log.d(TAG,"specific date radio is selected");
            vh.pickDateRange.setVisibility(View.GONE);
            vh.pickDate.setVisibility(View.VISIBLE);
        }
        else if (vh.rbCustomRange.getId() == selected) {
            if (DEBUG) Log.d(TAG,"date-range radio is selected");
            vh.pickDate.setVisibility(View.GONE);
            vh.pickDateRange.setVisibility(View.VISIBLE);
        }
        else {
            if (DEBUG) Log.d(TAG,"neither specific not date-range radio is selected");
            vh.pickDate.setVisibility(View.GONE);
            vh.pickDateRange.setVisibility(View.GONE);
            if (selected == R.id.rb_all) {
                params.setDateRange(DATE_ALL, null, null);
            } else if (selected == R.id.rb_today) {
                params.setDateRange(DATE_TODAY, new Date(), new Date());
            } else if (selected == R.id.rb_yesterday) {
                params.setDateRange(DATE_YESTERDAY, new Date().yesterday(), new Date().yesterday());
            } else if (selected == R.id.rb_this_week) {
                params.setDateRange(DATE_THIS_WEEK, new Date().firstDateOfThisWeek(), new Date().lastDateOfThisWeek());
            } else if (selected == R.id.rb_last_week) {
                params.setDateRange(DATE_LAST_WEEK, new Date().firstDateOfLastWeek(), new Date().lastDateOfLastWeek());
            } else if (selected == R.id.rb_this_month) {
                params.setDateRange(DATE_THIS_MONTH, new Date().firstDateOfThisMonth(), new Date().lastDateOfThisMonth());
            } else if (selected == R.id.rb_last_month) {
                params.setDateRange(DATE_LAST_MONTH, new Date().firstDateOfLastMonth(), new Date().lastDateOfLastMonth());
            }

        }
        if (DEBUG) {
            Log.d(TAG,"newDateType="+params.getDateType()+" | newMinDate= "+params.getMinDate()+
                    " | newMaxDate="+params.getMaxDate());
        }
    }

    private void onClickDatePickerButton(Button b) {
        final FilterTransactionScreenDatesViewHolder vh = getViewHolder();
        final TransactionsViewModel.FilterTransactionParams params = viewModel.getWorkingFilterParams();
        if (vh.date == b) {
            Date initial = null;
            if (Check.isEquals(params.getMaxDate(),params.getMinDate())) {
                if (null != params.getMaxDate()) {
                    initial = params.getMaxDate().clone();
                }
                else if (null != params.getMinDate()) {
                    initial = params.getMinDate().clone();
                }
            }
            if (null == initial) {
                initial = new Date();
            }
            onChooseDate(b,initial,date -> params.setDateRange(DATE_SPECIFIC,date,date));
        }
        else if (vh.minDate == b) {
            final Date initial = null != params.getMinDate() ? params.getMinDate() : new Date();
            onChooseDate(b,initial,date -> params.setDateType(DATE_CUSTOM_RANGE).setMinDate(date));
        }
        else if (vh.maxDate == b) {
            final Date initial = null != params.getMaxDate() ? params.getMaxDate() : new Date();
            onChooseDate(b,initial,date -> params.setDateType(DATE_CUSTOM_RANGE).setMaxDate(date));
        }
    }

    private void onChooseDate(@NonNull Button which,@NonNull Date date,@Nullable ResultCallback<Date> listener){
        new DatePickerDialog(getContext(), (view, year, month, day) -> {
            final Date newDate = new Date(year,month,day);
            if (null != listener) listener.onResult(newDate);
            which.setText(newDate.format(DATE_PATTERN));
        }, date.getYear(),
                date.getMonth(),
                date.getDayOfMonth())
                .show();
    }

    private void populateWithInitialValues(@NonNull FilterTransactionScreenDatesViewHolder vh, TransactionsViewModel.FilterTransactionParams params) {
        switch (params.getDateType()) {
            case DATE_ALL: {
                vh.rbAll.setChecked(true);
            }
            break;
            case DATE_TODAY: {
                vh.rbToday.setChecked(true);
            }
            break;
            case DATE_SPECIFIC: {
                vh.rbSpecific.setChecked(true);
                vh.date.setText(params.getMaxDate().format(DATE_PATTERN));
            }
            break;
            case DATE_THIS_WEEK: {
                vh.rbThisWeek.setChecked(true);
            }
            break;
            case DATE_LAST_WEEK: {
                vh.rbLastWeek.setChecked(true);
            }
            break;
            case DATE_THIS_MONTH: {
                vh.rbThisMonth.setChecked(true);
            }
            break;
            case DATE_LAST_MONTH: {
                vh.rbLastMonth.setChecked(true);
            }
            break;
            case DATE_CUSTOM_RANGE: {
                vh.rbCustomRange.setChecked(true);
                vh.maxDate.setText(null == params.getMaxDate() ? null
                        : params.getMaxDate().format(DATE_PATTERN));
                vh.minDate.setText(null == params.getMinDate() ? null
                        : params.getMinDate().format(DATE_PATTERN));
            }
        }
    }

    public static class FilterTransactionScreenDatesViewHolder extends BaseFragment.FragmentViewHolder {
        RadioGroup filterDateGroup;
        RadioButton rbAll;
        RadioButton rbToday;
        RadioButton rbYesterday;
        RadioButton rbSpecific;
        RadioButton rbThisWeek;
        RadioButton rbLastWeek;
        RadioButton rbThisMonth;
        RadioButton rbLastMonth;
        RadioButton rbCustomRange;
        View pickDate;
        View pickDateRange;
        Button date;
        Button maxDate;
        Button minDate;

        FilterTransactionScreenDatesViewHolder(@NonNull View root) {
            super(root);
            filterDateGroup = findViewById(R.id.filter_date_group);
            rbAll = findViewById(R.id.rb_all);
            rbToday = findViewById(R.id.rb_today);
            rbYesterday = findViewById(R.id.rb_yesterday);
            rbSpecific = findViewById(R.id.rb_specific);
            rbThisWeek = findViewById(R.id.rb_this_week);
            rbLastWeek = findViewById(R.id.rb_last_week);
            rbThisMonth = findViewById(R.id.rb_this_month);
            rbLastMonth = findViewById(R.id.rb_last_month);
            rbCustomRange = findViewById(R.id.rb_custom_range);
            pickDate = findViewById(R.id.pick_date);
            pickDateRange = findViewById(R.id.pick_date_range);
            date = findViewById(R.id.when);
            maxDate = findViewById(R.id.max_date);
            minDate = findViewById(R.id.min_date);
        }
    }
}

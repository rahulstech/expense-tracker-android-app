package dreammaker.android.expensetracker.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.textfield.TextInputLayout;

import java.util.UUID;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.database.type.Date;
import dreammaker.android.expensetracker.viewmodel.SavedStateViewModel;
import dreammaker.android.expensetracker.viewmodel.TransactionInputViewModel;

public class TransactionBasicDetailsInputFragment extends Fragment {

    private static final String TAG = "TranBasicDetails";

    private static final String KEY_PICKED_DATE = "picked_date";

    private Spinner dateOptions;
    private Button btn2;
    private Button btn1;

    private TextInputLayout containerAmount;
    private EditText inpAmount;
    private EditText inpDescription;

    private NavController navController;
    private SavedStateViewModel mSavedState;
    private Date pickedDate = Date.today();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.transaction_basic_details_input_layout,container,false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        mSavedState = new ViewModelProvider(this).get(SavedStateViewModel.class);
        dateOptions = view.findViewById(R.id.date_options);
        btn1 = view.findViewById(R.id.btn1);
        btn2 = view.findViewById(R.id.btn2);
        containerAmount = view.findViewById(R.id.container_amount);
        inpAmount = view.findViewById(R.id.line2);
        inpDescription = view.findViewById(R.id.description);
        view.findViewById(R.id.container_two_buttons).setVisibility(View.VISIBLE);

        ArrayAdapter<CharSequence> dateOptionAdapter = ArrayAdapter.createFromResource(requireContext(),R.array.transaction_date_option_values,
                android.R.layout.simple_dropdown_item_1line);
        dateOptions.setAdapter(dateOptionAdapter);
        dateOptions.setSelection(1);
        dateOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onChangeDateOption(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        String action = requireActivity().getIntent().getAction();
        boolean hasAccount = requireActivity().getIntent().hasExtra(Constants.EXTRA_ACCOUNT);
        if (Constants.ACTION_INCOME_EXPENSE.equals(action) && hasAccount) {
            btn1.setText(R.string.add_income);
            btn2.setText(R.string.add_expense);
        }
        else {
            btn1.setText(R.string.cancel);
            btn2.setText(R.string.next);
        }
        btn1.setOnClickListener(v -> onClickBtn1());
        btn2.setOnClickListener(v -> onClickBtn2());

        String txtDate = mSavedState.getString(KEY_PICKED_DATE);
        if (null != txtDate) {
            pickedDate = Date.valueOf(txtDate);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSavedState.put(KEY_PICKED_DATE,pickedDate.toString());
    }

    private boolean validateAmount() {
        String txtAmount = inpAmount.getText().toString();
        if (TextUtils.isEmpty(txtAmount)) {
            containerAmount.setError(getString(R.string.error_no_amount));
            return false;
        }
        if (!Check.isNumeric(txtAmount)) {
            containerAmount.setError(getString(R.string.error_invalid_amount));
            return false;
        }
        return true;
    }

    void onClickBtn2() {
        if (!validateAmount()) return;

        Intent intent = requireActivity().getIntent();
        String action = intent.getAction();
        Bundle extras = intent.getExtras();
        boolean hasAccount = intent.hasExtra(Constants.EXTRA_ACCOUNT);
        boolean hasPerson = intent.hasExtra(Constants.EXTRA_PERSON);

        Bundle args = new Bundle();
        if (Constants.ACTION_PAYMENT_DUE.equals(action)) {
            if (hasPerson) {
                args.putLong(Constants.EXTRA_PERSON,extras.getLong(Constants.EXTRA_PERSON));
                onMoveToNextDestination(R.id.basic_details_to_account,args);
            }
            else {
                onMoveToNextDestination(R.id.basic_details_to_people,args);
            }
        }
        else if (Constants.ACTION_INCOME_EXPENSE.equals(action)) {
            args.putString(Constants.EXTRA_TRANSACTION_TYPE, TransactionType.EXPENSE.name());
            if (hasAccount) {
                args.putLong(Constants.EXTRA_ACCOUNT,extras.getLong(Constants.EXTRA_ACCOUNT));
                onMoveToNextDestination(R.id.basic_details_to_save_transaction,args);
            }
            else {
                onMoveToNextDestination(R.id.basic_details_to_account,args);
            }
        }
        else {
            if (hasAccount) {
                args.putLong(Constants.EXTRA_PAYER_ACCOUNT,extras.getLong(Constants.EXTRA_ACCOUNT));
            }
            navController.navigate(R.id.basic_details_to_account,args);
        }
    }

    void onClickBtn1() {
        String action = requireActivity().getIntent().getAction();
        boolean hasAccount = requireActivity().getIntent().hasExtra(Constants.EXTRA_ACCOUNT);
        if (Constants.ACTION_INCOME_EXPENSE.equals(action)) {
            Bundle args = new Bundle();
            args.putString(Constants.EXTRA_TRANSACTION_TYPE,TransactionType.INCOME.name());
            if (hasAccount) {
                args.putLong(Constants.EXTRA_ACCOUNT,requireActivity().getIntent().getLongExtra(Constants.EXTRA_ACCOUNT,0));
                onMoveToNextDestination(R.id.basic_details_to_save_transaction,args);
            }
            else {
                onMoveToNextDestination(R.id.basic_details_to_account,args);
            }
        }
        else {
            requireActivity().finish();
        }
    }

    private void onChangeDateOption(int which) {
        if (0 == which) {
            pickedDate = Date.yesterday();
        }
        else if (1 == which) {
            pickedDate = Date.today();
        }
        else {
            onShowDatePicker();
        }
    }

    private void onShowDatePicker() {
        DatePickerDialog picker = new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            pickedDate = new Date(year,month,day);
        },pickedDate.getYear(),pickedDate.getMonth(),pickedDate.getDayOfMonth());
        picker.show();
    }

    private void onMoveToNextDestination(@IdRes int destinationId, @Nullable Bundle parent) {
        String date = pickedDate.toString();
        String amount = inpAmount.getText().toString();
        String description = inpDescription.getText().toString();

        Bundle args = new Bundle(parent);
        args.putString(Constants.EXTRA_OPERATION_ID, UUID.randomUUID().toString());
        args.putString(Constants.EXTRA_DATE,date);
        args.putString(Constants.EXTRA_AMOUNT,amount);
        args.putString(Constants.EXTRA_DESCRIPTION,description);
        navController.navigate(destinationId,args);
    }
}

package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.adapter.PeopleChooserAdapter;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.LayoutPayeePayerChooserBinding;
import dreammaker.android.expensetracker.itemdecoration.SimpleEmptyRecyclerViewDecoration;
import dreammaker.android.expensetracker.listener.ChoiceModel;
import dreammaker.android.expensetracker.util.ResourceUtil;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.PersonViewModel;

@SuppressWarnings("unused")
public class PersonChooserFragment extends Fragment {

    private static final String TAG = PersonChooserFragment.class.getSimpleName();

    private static final String KEY_QUERY = "query";

    private NavController navController;

    @SuppressWarnings("FieldCanBeLocal")
    private PersonViewModel mViewModel;

    private ChoiceModel.SavedStateViewModel mChoiceModelSavedState;

    private LayoutPayeePayerChooserBinding mBinding;

    private String mQuery;

    private ChoiceModel mChoiceModel;

    private PeopleChooserAdapter mAdapter;

    private TransactionHistoryParcelable mHistory;

    private List<PersonModel> mLoadedPeople;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHistory = getExtraTransactionHistory();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(PersonViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = LayoutPayeePayerChooserBinding.inflate(inflater,container,false);
        mViewModel.getAllPeopleWithUsageCount().observe(getViewLifecycleOwner(),this::onPeopleLoaded);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        navController = Navigation.findNavController(view);
        mChoiceModelSavedState = new ViewModelProvider(this).get(ChoiceModel.SavedStateViewModel.class);
        mBinding.search.setHint(R.string.search_person);
        mBinding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                submitQuery(s.toString());
            }
        });
        mAdapter = new PeopleChooserAdapter(requireContext());
        mBinding.list.setAdapter(mAdapter);
        mChoiceModel = new ChoiceModel(mBinding.list,mAdapter);
        mChoiceModel.setChoiceMode(ChoiceModel.CHOICE_MODE_SINGLE);
        mAdapter.setChoiceModel(mChoiceModel);
        mBinding.list.addItemDecoration(new SimpleEmptyRecyclerViewDecoration(getText(R.string.label_no_person),
                ResourceUtil.getDrawable(requireContext(),R.drawable.ic_person_black_72)));
        mBinding.btnNext.setOnClickListener(v -> onClickNext());
        mBinding.btnPrevious.setOnClickListener(v -> onClickPrevious());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mQuery = savedInstanceState.getString(KEY_QUERY,null);
            mChoiceModel.onRestoreInstanceState(mChoiceModelSavedState);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_QUERY,mQuery);
        mChoiceModel.onSaveInstanceState(mChoiceModelSavedState);
    }

    private void setTitle() {
        requireActivity().setTitle(getTitle());
    }

    private CharSequence getTitle() {
        TransactionType type = mHistory.getType();
        switch (type) {
            case DUE: {
                return getText(R.string.label_pay_for);
            }
            case PAY_BORROW: {
                return getText(R.string.label_pay_to);
            }
            case BORROW:
            case PAY_DUE:{
                return getText(R.string.label_receive_from);
            }
            case DUE_TRANSFER:
            case BORROW_TRANSFER:
            case BORROW_TO_DUE_TRANSFER: {
                if (null == mHistory.getPayeePersonId()) {
                    return getText(R.string.label_send_to);
                }
                else {
                    return getText(R.string.label_send_from);
                }
            }
        }
        return null;
    }

    private void onPeopleLoaded(@Nullable List<PersonModel> people) {
        if (null == people) {
            String message = getString(R.string.no_person).toLowerCase();
            ToastUtil.showErrorShort(requireContext(),message);
            onClickPrevious();
        }
        else {
            mLoadedPeople = people;
            submitPeople();
        }
    }

    private void submitQuery(String query) {
        mQuery = query;
        submitPeople();
    }

    private void submitPeople() {
        mAdapter.filter(mLoadedPeople,mQuery);
    }

    private void onClickNext() {
        if (!validatePerson()) {
            return;
        }
        long id = mAdapter.getItemId(mChoiceModel.getSelectedPosition());
        TransactionType type = mHistory.getType();
        switch (type) {
            case DUE:
            case PAY_BORROW: {
                mHistory.setPayeePersonId(id);
            }
            break;
            case BORROW:
            case PAY_DUE: {
                mHistory.setPayerPersonId(id);
            }
            break;
            case DUE_TRANSFER:
            case BORROW_TRANSFER:
            case BORROW_TO_DUE_TRANSFER: {
                if (null == mHistory.getPayeePersonId()) {
                    mHistory.setPayeePersonId(id);
                }
                else {
                    mHistory.setPayerPersonId(id);
                }
            }
        }
        gotoNextDestination(mHistory);
    }

    private boolean validatePerson() {
        if (!mChoiceModel.hasSelection()) {
            Toast.makeText(requireContext(), R.string.error_no_person_selected, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void gotoNextDestination(TransactionHistoryParcelable history) {
        Bundle args = new Bundle(requireArguments());
        args.putParcelable(TransactionBasicDetailsInputFragment.EXTRA_TRANSACTION_HISTORY,history);
        TransactionType type = history.getType();
        switch (type) {
            case DUE:
            case PAY_BORROW: {
                if (mHistory.getPayerAccountId() == null) {
                    navController.navigate(R.id.action_person_chooser_to_account_chooser,args);
                }
                else {
                    navController.navigate(R.id.action_person_chooser_to_save_history,args);
                }
            }
            break;
            case BORROW:
            case PAY_DUE: {
                if (mHistory.getPayeeAccountId() == null) {
                    navController.navigate(R.id.action_person_chooser_to_account_chooser,args);
                }
                else {
                    navController.navigate(R.id.action_person_chooser_to_save_history,args);
                }
            }
            break;
            case DUE_TRANSFER:
            case BORROW_TRANSFER:
            case BORROW_TO_DUE_TRANSFER: {
                if (history.getPayeePersonId() != null && history.getPayerPersonId() != null) {
                    navController.navigate(R.id.action_person_chooser_to_save_history,args);
                }
                else {
                    navController.navigate(R.id.action_person_chooser_to_person_chooser,args);
                }
            }
        }
    }

    private void onClickPrevious() {
        navController.popBackStack();
    }

    private TransactionHistoryParcelable getExtraTransactionHistory() {
        return requireArguments().getParcelable(TransactionBasicDetailsInputFragment.EXTRA_TRANSACTION_HISTORY);
    }
}

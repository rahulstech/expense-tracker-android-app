package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.FragmentPersonDetailsBinding;
import dreammaker.android.expensetracker.dialog.DialogUtil;
import dreammaker.android.expensetracker.drawable.DrawableUtil;
import dreammaker.android.expensetracker.text.SpannableStringUtil;
import dreammaker.android.expensetracker.text.Spans;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.DBViewModel;
import dreammaker.android.expensetracker.viewmodel.PersonViewModel;
import rahulstech.android.backend.settings.AppSettings;

@SuppressWarnings("unused")
public class PersonDetailsFragment extends BaseEntityWithTransactionHistoriesFragment {

    private static final String TAG = PersonDetailsFragment.class.getSimpleName();

    private PersonViewModel mPersonVM;

    private FragmentPersonDetailsBinding mBinding;

    private NavController navController;

    private PersonModel mPerson;

    private AppSettings mSettings;

    public PersonDetailsFragment() {super();}

    private long getExtraPersonId() {
        return requireArguments().getLong(Constants.EXTRA_ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSettings = AppSettings.get(context);
        mPersonVM = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(PersonViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentPersonDetailsBinding.inflate(inflater,container,false);
        mPersonVM.getPersonById(getExtraPersonId()).observe(getViewLifecycleOwner(),this::onPersonFetched);
        loadHistories(ENTITY_PEOPLE,getExtraPersonId());
        mPersonVM.setCallbackIfTaskExists(PersonViewModel.DELETE_PEOPLE,this,this::onPersonDeleted);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        navController = Navigation.findNavController(view);
        mBinding.addDue.setOnClickListener(v->onClickAddDue());
        mBinding.addBorrow.setOnClickListener(v->onClickAddBorrow());
        mBinding.addPayDue.setOnClickListener(v->onClickAddPayDue());
        mBinding.addPayBorrow.setOnClickListener(v->onClickAddPayBorrow());
        mBinding.addSendDue.setOnClickListener(v->onClickSendDue());
        mBinding.addSendBorrow.setOnClickListener(v->onClickSendBorrow());
    }

    @Override
    protected RecyclerView getHistoryList() {
        return mBinding.list;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_person_details,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            onClickDeletePerson();
            return true;
        }
        else if (id == R.id.edit) {
            onClickEditPerson();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode,menu);
        updateActionTitle(mode);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        super.onDestroyActionMode(mode);
        updateActionTitle(mode);
    }

    @Override
    public void onItemChecked(@NonNull ActionMode mode, @NonNull View view, int position, boolean checked) {
        super.onItemChecked(mode,view,position,checked);
        updateActionTitle(mode);
    }

    private void updateActionTitle(ActionMode mode) {
        mode.setTitle(getString(R.string.message_selection_count,getHistoryChoiceModel().getCheckedCount()));
    }

    private void setTitle() {
        requireActivity().setTitle(R.string.label_person_details);
    }

    private void onPersonFetched(@Nullable PersonModel person) {
        if (null == person) {
            ToastUtil.showErrorShort(requireContext(),R.string.error_person_not_found);
            exit();
            return;
        }
        mPerson = person;
        String displayName = TextUtil.getDisplayNameForPerson(person.getFirstName(),person.getLastName(),isFirstNameFirst(),getString(R.string.label_unknown));
        Drawable photo = DrawableUtil.getPersonDefaultPhoto(person.getFirstName(),person.getLastName(),isFirstNameFirst());
        mBinding.name.setText(displayName);
        mBinding.photo.setImageDrawable(photo);
        setPersonRealDueAndRealBorrow();
    }

    private Currency getRealDue() {
        Currency due = mPerson.getDue();
        Currency borrow = mPerson.getBorrow();
        Currency realDue = Currency.ZERO;
        if (!due.isNegative()) {
            realDue = realDue.add(due);
        }
        if (borrow.isNegative()) {
            realDue = realDue.add(borrow.negate());
        }
        return realDue;
    }

    private Currency getRealBorrow() {
        Currency due = mPerson.getDue();
        Currency borrow = mPerson.getBorrow();
        Currency realBorrow = Currency.ZERO;
        if (due.isNegative()){
            realBorrow = realBorrow.add(due.negate());
        }
        if (!borrow.isNegative()) {
            realBorrow = realBorrow.add(borrow);
        }
        return realBorrow;
    }

    private void setPersonRealDueAndRealBorrow() {
        Currency realDue = getRealDue();
        Currency realBorrow = getRealBorrow();
        mBinding.due.setText(TextUtil.prettyFormatCurrency(realDue));
        mBinding.dueText.setText(TextUtil.currencyToText(requireContext(),realDue));
        mBinding.borrow.setText(TextUtil.prettyFormatCurrency(realBorrow));
        mBinding.borrowText.setText(TextUtil.currencyToText(requireContext(),realBorrow));
    }

    private boolean isFirstNameFirst() {
        return AppSettings.FIRST_NAME_FIRST == mSettings.getPreferredPersonNameOrientation();
    }

    @Override
    protected void onClickHistory(@NonNull TransactionHistoryModel history) {
        Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_ID,history.getId());
        navController.navigate(R.id.action_person_details_to_history_details,args);
    }

    private void onClickDeletePerson() {
        final long id = mPerson.getId();
        final String displayName = TextUtil.getDisplayNameForPerson(mPerson.getFirstName(),mPerson.getLastName(),
                isFirstNameFirst(),getString(R.string.label_unknown));
        CharSequence message = getResources().getQuantityString(R.plurals.warning_delete_persons,1,displayName);
        CharSequence highlighted = new SpannableStringUtil(message).append("\n\n")
                .append(displayName,new Object[]{Spans.bold(),Spans.relativeSize(2)})
                .toSpannableString();
        DialogUtil.createMessageDialog(requireContext(),highlighted,
                getText(R.string.no),null,
                getText(R.string.yes),(di,which)->deletePerson(id),
                false).show();
    }

    private void onClickEditPerson() {
        long id = mPerson.getId();
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_ACTION,Constants.ACTION_UPDATE);
        args.putLong(Constants.EXTRA_ID,id);
        navController.navigate(R.id.action_person_details_to_input_person,args);
    }

    private void deletePerson(final long id) {
        mPersonVM.removePeople(new long[]{id}).observe(this,this::onPersonDeleted);
    }

    private void onPersonDeleted(DBViewModel.AsyncQueryResult result) {
        Boolean success = (Boolean) result.getResult();
        if (null == success || !success) {
            ToastUtil.showErrorShort(requireContext(),getResources().getQuantityString(R.plurals.error_delete_people,1));
            if (BuildConfig.DEBUG) {
                Log.e(TAG,"fail to delete person with id="+getExtraPersonId(),result.getError());
            }
        }
    }

    private void onClickAddDue() {
        navigateToInputTransaction(TransactionType.DUE,false);
    }

    private void onClickAddBorrow() {
        navigateToInputTransaction(TransactionType.BORROW,true);
    }

    private void onClickAddPayDue() {
        navigateToInputTransaction(TransactionType.PAY_DUE,true);
    }

    private void onClickAddPayBorrow() {
        navigateToInputTransaction(TransactionType.PAY_BORROW,false);
    }

    private void onClickSendDue() {
        Currency due = getRealDue();
        if (due.equals(Currency.ZERO)) {
            ToastUtil.showMessageShort(requireContext(),R.string.error_due_send_zero);
            return;
        }
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_AMOUNT,due.toString());
        navigateToInputTransaction(TransactionType.DUE_TRANSFER,true,extras);
    }

    private void onClickSendBorrow() {
        Currency borrow = getRealBorrow();
        if (borrow.equals(Currency.ZERO)) {
            ToastUtil.showMessageShort(requireContext(),R.string.error_borrow_send_zero);
            return;
        }
        Bundle extras = new Bundle();
        extras.putString(Constants.EXTRA_AMOUNT,borrow.toString());
        navigateToInputTransaction(TransactionType.BORROW_TRANSFER,true,extras);
    }

    private void navigateToInputTransaction(TransactionType type, boolean payer) {
        navigateToInputTransaction(type,payer,null);
    }

    private void navigateToInputTransaction(TransactionType type, boolean payer, Bundle extras) {
        final long id = mPerson.getId();
        Bundle args = new Bundle();
        if (null != extras) {
            args.putAll(extras);
        }
        args.putString(Constants.EXTRA_ACTION,Constants.ACTION_INSERT);
        args.putString(Constants.EXTRA_TRANSACTION_TYPE,type.name());
        if (payer) {
            args.putLong(Constants.EXTRA_PAYER_PERSON,id);
        }
        else {
            args.putLong(Constants.EXTRA_PAYEE_PERSON,id);
        }
        navController.navigate(R.id.action_person_details_to_input_history,args);
    }

    private boolean isStillLoading() {
        return null == mPerson;
    }

    private void exit() {
        navController.popBackStack();
    }
}
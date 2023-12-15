package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.FragmentPersonDetailsBinding;
import dreammaker.android.expensetracker.dialog.DialogUtil;
import dreammaker.android.expensetracker.drawable.DrawableUtil;
import dreammaker.android.expensetracker.itemdecoration.SimpleEmptyRecyclerViewDecoration;
import dreammaker.android.expensetracker.text.TextUtil;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ResourceUtil;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.DBViewModel;
import dreammaker.android.expensetracker.viewmodel.PersonViewModel;
import dreammaker.android.expensetracker.viewmodel.TransactionHistoryViewModel;
import rahulstech.android.backend.settings.AppSettings;

@SuppressWarnings("unused")
public class PersonDetailsFragment extends Fragment {

    private static final String TAG = PersonDetailsFragment.class.getSimpleName();

    private PersonViewModel mPersonVM;

    private TransactionHistoryViewModel mHistoryVM;

    private FragmentPersonDetailsBinding mBinding;

    private NavController navController;

    private PersonModel mPerson;

    private AppSettings mSettings;

    public PersonDetailsFragment() {}

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
        mHistoryVM = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(TransactionHistoryViewModel.class);
        mPersonVM.setCallbackIfTaskExists(PersonViewModel.DELETE_PEOPLE,this,this::onPersonDeleted);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentPersonDetailsBinding.inflate(inflater,container,false);
        mPersonVM.getPersonById(getExtraPersonId()).observe(getViewLifecycleOwner(),this::onPersonFetched);
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
        mBinding.list.addItemDecoration(new SimpleEmptyRecyclerViewDecoration(getText(R.string.label_no_history),
                ResourceUtil.getDrawable(requireContext(),R.drawable.ic_baseline_history_72)));
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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
        setPersonDue(person.getDue());
        setPersonBorrow(person.getBorrow());
    }

    private void setPersonDue(Currency due) {}

    private void setPersonBorrow(Currency borrow) {}

    private boolean isFirstNameFirst() {
        return AppSettings.FIRST_NAME_FIRST == mSettings.getPreferredPersonNameOrientation();
    }

    private void onClickDeletePerson() {
        // TODO: highlight the person display name
        final long id = mPerson.getId();
        final String displayName = TextUtil.getDisplayNameForPerson(mPerson.getFirstName(),mPerson.getLastName(),
                isFirstNameFirst(),getString(R.string.label_unknown));
        String message = getResources().getQuantityString(R.plurals.warning_delete_persons,1,displayName);
        DialogUtil.createMessageDialog(requireContext(),message,
                getText(R.string.yes),(di,which)->deletePerson(id),
                getText(R.string.no),null,false).show();
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
        Integer count = (Integer) result.getResult();
        if (null == count || count != 0) {
            // TODO: show not deleted error
            ToastUtil.showErrorShort(requireContext(),"");
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

    private void navigateToInputTransaction(TransactionType type, boolean payer) {
        final long id = mPerson.getId();
        Bundle args = new Bundle();
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

    private void exit() {
        navController.popBackStack();
    }
}
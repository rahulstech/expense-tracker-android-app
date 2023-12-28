package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.adapter.AccountsChooserAdapter;
import dreammaker.android.expensetracker.database.model.AccountModel;
import dreammaker.android.expensetracker.fragment.parcelable.AccountParcelable;
import dreammaker.android.expensetracker.itemdecoration.SimpleEmptyRecyclerViewDecoration;
import dreammaker.android.expensetracker.listener.ChoiceModel;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ResourceUtil;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.AccountViewModel;

@SuppressWarnings("unused")
public class AccountChooserFragment extends BaseChooserWithSearchFragment {

    private static final String TAG = AccountChooserFragment.class.getSimpleName();

    @SuppressWarnings("FieldCanBeLocal")
    private AccountViewModel mViewModel;

    @SuppressWarnings("FieldCanBeLocal")
    private NavController navController;

    private AccountsChooserAdapter mAdapter;

    private List<AccountModel> mLoadedAccounts;

    private ArrayList<AccountParcelable> mSelectedAccounts;

    private String mQuery;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(AccountViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);
        mViewModel.getAllAccountsWithUsageCount().observe(getViewLifecycleOwner(),this::onAccountsLoaded);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        getSearchQueryInput().setHint(R.string.search_account);
        mAdapter = new AccountsChooserAdapter(requireContext());
        setAdapter(mAdapter);
        getList().addItemDecoration(new SimpleEmptyRecyclerViewDecoration(getText(R.string.label_no_account),
                ResourceUtil.getDrawable(requireContext(),R.drawable.ic_account_72)));
        super.onViewCreated(view, savedInstanceState);
        mAdapter.setChoiceModel(getChoiceModel());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mSelectedAccounts = savedInstanceState.getParcelableArrayList(KEY_SELECTIONS);
        }
        if (null == mSelectedAccounts) {
            mSelectedAccounts = new ArrayList<>();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_SELECTIONS,mSelectedAccounts);
    }

    @NonNull
    @Override
    public ChoiceModel.Callback getChoiceModelCallback() {
        return mAdapter;
    }

    @Override
    protected void onChangeSearchQuery(CharSequence query) {
        submitAccounts();
    }

    @Override
    public void onItemChecked(@NonNull RecyclerView recyclerView, @NonNull View view, int position, boolean checked) {
        super.onItemChecked(recyclerView,view,position,checked);
        if (mAdapter.getItemViewType(position) == AccountsChooserAdapter.SECTION_ITEM_TYPE) {
            AccountModel account = mAdapter.getData(position);
            AccountParcelable parcelable = new AccountParcelable(account);
            if (checked) {
                mSelectedAccounts.add(parcelable);
            }
            else {
                mSelectedAccounts.remove(parcelable);
            }
        }
    }

    @NonNull
    @Override
    protected Bundle onPrepareResult() {
        Bundle result = new Bundle();
        final ArrayList<AccountParcelable> selections = mSelectedAccounts;
        final int count = selections.size();
        final String action = getAction();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onPrepareResult: action="+action+" selections-size="+count);
        }
        if (count > 0) {
            if (Constants.ACTION_PICK_MULTIPLE.equals(action)) {
                result.putParcelableArrayList(Constants.KEY_RESULT, selections);
            } else if (Constants.ACTION_PICK.equals(action)) {
                AccountParcelable account = selections.get(count-1); // last added
                result.putParcelable(Constants.KEY_RESULT, account);
            }
        }
        return result;
    }

    private void onAccountsLoaded(@Nullable List<AccountModel> accounts) {
        if (null == accounts) {
            String message = getString(R.string.no_account).toLowerCase();
            ToastUtil.showErrorShort(requireContext(),message);
            exit();
        }
        else {
            mLoadedAccounts = accounts;
            submitAccounts();
            if (!accounts.isEmpty() && hasExtraInitial()) {
                if (Constants.ACTION_PICK_MULTIPLE.equals(getAction())) {
                    ArrayList<AccountParcelable> initials = getExtraInitial();
                    if (null != initials && !initials.isEmpty()) {
                        ArrayList<Object> keys = new ArrayList<>();
                        for (AccountParcelable account : initials) {
                            keys.add(account.getId());
                        }
                        getChoiceModel().setChecked(keys,true);
                        mSelectedAccounts.addAll(initials);
                    }
                }
                else {
                    AccountParcelable initial = getExtraInitial();
                    if (null != initial) {
                        Object key = initial.getId();
                        getChoiceModel().setChecked(key, true);
                        mSelectedAccounts.add(initial);
                    }
                }
            }
        }
    }

    private void submitAccounts() {
        CharSequence text = getSearchQueryText();
        String query = null == text ? null : text.toString();
        mAdapter.filter(mLoadedAccounts,query);
    }
}

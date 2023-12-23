package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.time.LocalDate;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.adapter.SectionedListAdapter;
import dreammaker.android.expensetracker.adapter.SectionedTransactionHistoryAdapter;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.dialog.DialogUtil;
import dreammaker.android.expensetracker.itemdecoration.SimpleEmptyRecyclerViewDecoration;
import dreammaker.android.expensetracker.listener.ChoiceModel;
import dreammaker.android.expensetracker.listener.ModalChoiceModeListener;
import dreammaker.android.expensetracker.listener.OnItemClickListener;
import dreammaker.android.expensetracker.util.ResourceUtil;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.DBViewModel;
import dreammaker.android.expensetracker.viewmodel.TransactionHistoryViewModel;
import rahulstech.android.backend.settings.AppSettings;

@SuppressWarnings("unused")
public abstract class BaseEntityWithTransactionHistoriesFragment extends Fragment implements ModalChoiceModeListener, OnItemClickListener {

    private static final String TAG = BaseEntityWithTransactionHistoriesFragment.class.getSimpleName();

    /** show histories for all people and accounts */
    public static final int ENTITY_ALL = 0;

    /** show histories for account with given id */
    public static final int ENTITY_ACCOUNTS = 1;

    /** show histories for person with given id */
    public static final int ENTITY_PEOPLE = 2;

    private TransactionHistoryViewModel mHistoryVM;

    private ChoiceModel mChoiceModel;

    private ChoiceModel.SavedStateViewModel mChoiceModelSavedStated;

    private SectionedTransactionHistoryAdapter mAdapter;

    private List<TransactionHistoryModel> mHistories;

    private AppSettings mSettings;

    protected BaseEntityWithTransactionHistoriesFragment() {super();}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mSettings = AppSettings.get(context);
        mHistoryVM = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(TransactionHistoryViewModel.class);
        mChoiceModelSavedStated = new ViewModelProvider(this).get(ChoiceModel.SavedStateViewModel.class);
    }

    @Nullable
    @Override
    public abstract View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

    protected abstract RecyclerView getHistoryList();

    protected SectionedTransactionHistoryAdapter getHistoryAdapter() {
        return mAdapter;
    }

    protected ChoiceModel getHistoryChoiceModel() {
        return mChoiceModel;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView list = getHistoryList();
        list.addItemDecoration(new SimpleEmptyRecyclerViewDecoration(getText(R.string.label_no_history),
                ResourceUtil.getDrawable(requireContext(),R.drawable.ic_baseline_history_72)));
        mAdapter = new SectionedTransactionHistoryAdapter(requireContext());
        list.setAdapter(mAdapter);
        mChoiceModel = new ChoiceModel(list,mAdapter);
        mChoiceModel.setChoiceMode(ChoiceModel.CHOICE_MODE_MULTIPLE_MODAL);
        mChoiceModel.setModalChoiceModeListener(this);
        mChoiceModel.setOnItemClickListener(this);
        mAdapter.setChoiceModel(mChoiceModel);
        changeHistoryAdapterHeaderTypeForHistoryGrouping(mSettings.getHistoryGrouping());
        mHistoryVM.setCallbackIfTaskExists(TransactionHistoryViewModel.DELETE_HISTORIES,getViewLifecycleOwner(),this::onHistoriesDeleted);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mChoiceModel.onRestoreInstanceState(mChoiceModelSavedStated);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(null != mChoiceModel) {
            mChoiceModel.onSaveInstanceState(mChoiceModelSavedStated);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_history_groupping,menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        int groupBy = mSettings.getHistoryGrouping();
        if (groupBy == AppSettings.GROUP_MONTHLY) {
            MenuItem item = menu.findItem(R.id.group_monthly);
            item.setChecked(true);
        }
        else {
            MenuItem item = menu.findItem(R.id.group_daily);
            item.setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.group_daily) {
            changeHistoryGrouping(AppSettings.GROUP_DAILY);
            return true;
        }
        else if (id == R.id.group_monthly) {
            changeHistoryGrouping(AppSettings.GROUP_MONTHLY);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_action_mode_histories_list,menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {return false;}

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete) {
            onClickDeleteHistories();
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {}

    @Override
    public void onItemChecked(@NonNull ActionMode mode, @NonNull View view, int position, boolean checked) {}

    @Override
    public final void onClickItem(@NonNull RecyclerView recyclerView, @NonNull View view, int position) {
        if (mAdapter.getItemViewType(position) == SectionedListAdapter.SECTION_ITEM_TYPE) {
            onClickHistory(mAdapter.getData(position));
        }
    }

    protected void onClickHistory(@NonNull TransactionHistoryModel history) {}

    protected void onClickDeleteHistories(){
        if (!mChoiceModel.hasSelection()) {
            return;
        }
        int count = mChoiceModel.getCheckedCount();
        CharSequence message = getResources().getQuantityString(R.plurals.warning_delete_transaction_histories,0,count);
        DialogUtil.createMessageDialog(requireContext(),message,
                getText(R.string.no),null,
                getText(R.string.yes),(di,which)->deleteHistories(),true)
                .show();
    }

    protected void onHistoriesDeleted(DBViewModel.AsyncQueryResult result) {
        final int selections = mChoiceModel.getCheckedCount();
        mChoiceModel.finishActionMode();
        Boolean success = (Boolean) result.getResult();
        if (null == success || !success) {
            ToastUtil.showErrorShort(requireContext(),getResources().getQuantityString(R.plurals.error_delete_transaction_histories,0));
            if (BuildConfig.DEBUG) {
                Log.e(TAG,"fail to remove multiple transaction histories",result.getError());
            }
        }
    }

    private void deleteHistories() {
        List<Object> keys = mChoiceModel.getCheckedKeys();
        long[] ids = new long[keys.size()];
        int position = 0;
        for (Object key : keys) {
            ids[position++] = (long) key;
        }
        Log.d(TAG,"ids="+ids.length);
        mHistoryVM.removeTransactionHistories(ids).observe(getViewLifecycleOwner(),this::onHistoriesDeleted);
    }

    protected void onHistoriesFetched(@Nullable List<TransactionHistoryModel> histories) {
        mHistories = histories;
        submitHistories();
    }

    /**
     * index 0 -> date start
     * index 1 -> date end
     */
    protected LocalDate[] getShowHistoryDateRange(final int months) {
        LocalDate start = LocalDate.now();
        LocalDate end;
        if (months == AppSettings.HISTORY_MONTH_3) {
            end = start.minusMonths(3);
        }
        else if (months == AppSettings.HISTORY_MONTH_6) {
            end = start.minusMonths(6);
        }
        else {
            end = start.minusMonths(12);
        }
        return new LocalDate[]{start,end};
    }

    protected final void loadAllHistories() {
        loadHistories(ENTITY_ALL,0);
    }

    protected final void loadHistories(int entity, long id) {
        LocalDate[] range = getShowHistoryDateRange(mSettings.getShowHistoriesOfMonths());
        LocalDate start = range[1];
        LocalDate end = range[0];
        LiveData<List<TransactionHistoryModel>> histories;
        if (entity == ENTITY_ACCOUNTS) {
            histories = mHistoryVM.getTransactionsForAccountBetweenDates(id,start,end);
        }
        else if (entity == ENTITY_PEOPLE) {
            histories = mHistoryVM.getTransactionsForPersonBetweenDates(id,start,end);
        }
        else {
            histories = mHistoryVM.getTransactionsBetweenDates(start,end);
        }
        histories.observe(getViewLifecycleOwner(),this::onHistoriesFetched);
    }

    protected void changeHistoryGrouping(int groupBy) {
        final int oldGroupBy = mSettings.getHistoryGrouping();
        if (oldGroupBy == groupBy) {
            return;
        }
        mSettings.setHistoryGrouping(groupBy);
        changeHistoryAdapterHeaderTypeForHistoryGrouping(groupBy);
    }

    protected void changeHistoryAdapterHeaderTypeForHistoryGrouping(int groupBy) {
        int headerType;
        if (groupBy == AppSettings.GROUP_MONTHLY) {
            headerType = SectionedTransactionHistoryAdapter.HEADER_MONTH;
        }
        else {
            headerType = SectionedTransactionHistoryAdapter.HEADER_DATE;
        }
        getHistoryAdapter().setHeaderType(headerType);
        submitHistories();
    }

    protected final void submitHistories() {
        mAdapter.submitList(mHistories);
    }
}

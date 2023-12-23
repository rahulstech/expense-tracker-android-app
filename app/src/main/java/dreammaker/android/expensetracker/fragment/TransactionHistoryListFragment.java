package dreammaker.android.expensetracker.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.activity.ActivityModel;
import dreammaker.android.expensetracker.activity.ActivityModelProvider;
import dreammaker.android.expensetracker.adapter.SectionedTransactionHistoryAdapter;
import dreammaker.android.expensetracker.animation.AnimatorUtil;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.database.type.TransactionType;
import dreammaker.android.expensetracker.databinding.FragmentTransactionHistoryListBinding;
import dreammaker.android.expensetracker.util.Constants;

@SuppressWarnings("unused")
public class TransactionHistoryListFragment extends BaseEntityWithTransactionHistoriesFragment {

    private static final String TAG = TransactionHistoryListFragment.class.getSimpleName();

    private static final String KEY_QUERY = "key_query";

    private SectionedTransactionHistoryAdapter mTransactionsAdapter;

    private NavController navController;

    private FragmentTransactionHistoryListBinding mBinding;

    private SectionedTransactionHistoryAdapter.HistoryFilterData mQuery;

    private boolean mRevealMenuOpened = false;

    @SuppressWarnings("FieldCanBeLocal")
    private ActivityModel mActivityModel;

    public TransactionHistoryListFragment() {super();}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivityModel = ((ActivityModelProvider) requireActivity()).getActivityModel();
        mActivityModel.addOnBackPressedCallback(this,this::onBackPressed);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTransactionHistoryListBinding.inflate(inflater,container,false);
        loadAllHistories();
        return mBinding.getRoot();
    }

    @Override
    protected RecyclerView getHistoryList() {
        return mBinding.list;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        navController = Navigation.findNavController(view);
        mBinding.btnAddHistory.setOnClickListener(v-> onClickAddHistory());
        mBinding.addIncome.setOnClickListener(v->onClickAddIncome());
        mBinding.addExpense.setOnClickListener(v->onClickAddExpense());
        mBinding.addMoneyTransfer.setOnClickListener(v-> onClickMoneyTransfer());
        mBinding.addDue.setOnClickListener(v->onClickAddDue());
        mBinding.addPayDue.setOnClickListener(v->onClickAddPayDue());
        mBinding.addBorrow.setOnClickListener(v->onClickAddBorrow());
    }

    @Override
    public void onPause() {
        super.onPause();
        hideRevealMenu(null);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null != mQuery) {
            outState.putParcelable(KEY_QUERY,mQuery);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mQuery = savedInstanceState.getParcelable(KEY_QUERY);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_history_list,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.filter) {
            onClickFilterHistory();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mBinding.btnAddHistory.hide();
        updateActionTitle(mode);
        return true;
    }

    @Override
    public void onItemChecked(@NonNull ActionMode mode, @NonNull View view, int position, boolean checked) {
        updateActionTitle(mode);
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        updateActionTitle(mode);
        mBinding.btnAddHistory.show();
    }

    private void updateActionTitle(ActionMode mode) {
        mode.setTitle(getString(R.string.message_selection_count,getHistoryChoiceModel().getCheckedCount()));
    }

    @Override
    protected void onClickHistory(@NonNull TransactionHistoryModel history) {
        if (mRevealMenuOpened) {
            // since the overlapping reveal menu is over the recycler view, the item touch listener
            // will response to the unhandled clicked of the reveal menu. so when reveal menu is open
            // don't handle this click event
            return;
        }
        Bundle args = new Bundle();
        args.putLong(Constants.EXTRA_ID,history.getId());
        navController.navigate(R.id.action_histories_to_history_details,args);
    }

    private boolean onBackPressed() {
       return hideRevealMenu(null);
    }

    private void onClickAddHistory() {
        toggleRevealMenu(null);
    }

    private void onClickFilterHistory() {
        Bundle args = new Bundle();
        args.putParcelable(FilterHistoryBottomSheet.EXTRA_HISTORY_FILTER_DATA,mQuery);
        navController.navigate(R.id.action_histories_to_filter_history,args);
    }

    private void toggleRevealMenu(@Nullable Animator.AnimatorListener listener) {
        float rotationFrom, rotationTo;
        if (mRevealMenuOpened) {
            rotationFrom = 45;
            rotationTo = 0;
        }
        else {
            rotationFrom = 0;
            rotationTo = 45;
        }
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(mBinding.btnAddHistory,"rotation",rotationFrom,rotationTo);
        rotationAnimator.setDuration(AnimatorUtil.LONG_ANIM_DURATION);
        Animator revealAnimator = AnimatorUtil.circularReveal(mBinding.btnAddHistory,mBinding.revealMenu,!mRevealMenuOpened);
        revealAnimator.setDuration(AnimatorUtil.SHORT_ANIM_DURATION);
        revealAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (!mRevealMenuOpened) {
                    mBinding.revealMenu.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mRevealMenuOpened) {
                    mBinding.revealMenu.setVisibility(View.GONE);
                }
                mRevealMenuOpened = !mRevealMenuOpened;
            }
        });
        AnimatorSet set = new AnimatorSet();
        Toolbar toolbar = mActivityModel.getSupportToolbar();
        if (null != toolbar) {
            int height = toolbar.getMeasuredHeight();
            int fromY, toY;
            if (mRevealMenuOpened) {
                fromY = -height;
                toY = 0;
            }
            else {
                fromY = 0;
                toY = -height;
            }
            ObjectAnimator translateAnimator = ObjectAnimator.ofFloat(toolbar,"translationY",fromY,toY);
            translateAnimator.setDuration(AnimatorUtil.SHORT_ANIM_DURATION);
            set.playTogether(revealAnimator,rotationAnimator,translateAnimator);
        }
        else {
            set.playTogether(revealAnimator,rotationAnimator);
        }
        if (null != listener) {
            set.addListener(listener);
        }
        set.start();
    }

    /**
     * hide the reveal menu if it is currently open
     *
     * @return true if reveal menu is closed by this method, false if already hidden
     */
    private boolean hideRevealMenu(@Nullable Animator.AnimatorListener listener) {
        if (mRevealMenuOpened) {
            toggleRevealMenu(listener);
            return true;
        }
        return false;
    }

    private void onClickMoneyTransfer() {
        navigateToInputTransaction(TransactionType.MONEY_TRANSFER);
    }

    private void onClickAddIncome() {
        navigateToInputTransaction(TransactionType.INCOME);
    }

    private void onClickAddExpense() {
        navigateToInputTransaction(TransactionType.EXPENSE);
    }

    private void onClickAddDue() {
        navigateToInputTransaction(TransactionType.DUE);
    }

    private void onClickAddPayDue() {
        navigateToInputTransaction(TransactionType.PAY_DUE);
    }

    private void onClickAddBorrow() {
        navigateToInputTransaction(TransactionType.BORROW);
    }

    private void navigateToInputTransaction(TransactionType type) {
        hideRevealMenu(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Bundle args = new Bundle();
                args.putString(Constants.EXTRA_ACTION,Constants.ACTION_INSERT);
                args.putString(Constants.EXTRA_TRANSACTION_TYPE,type.name());
                navController.navigate(R.id.action_histories_to_input_history,args);
            }
        });
    }
}
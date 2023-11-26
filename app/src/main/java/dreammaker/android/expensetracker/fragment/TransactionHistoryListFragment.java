package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.adapter.SectionedTransactionHistoryAdapter;
import dreammaker.android.expensetracker.database.model.TransactionHistoryModel;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.viewmodel.TransactionHistoryListFragmentViewModel;

@SuppressWarnings("unused")
public class TransactionHistoryListFragment extends Fragment {

    private static final String TAG = TransactionHistoryListFragment.class.getSimpleName();

    private RecyclerView mList;
    private FloatingActionButton btnAddHistory;
    private SectionedTransactionHistoryAdapter mTransactionsAdapter;

    private TransactionHistoryListFragmentViewModel mViewModel;
    private NavController mNavController;

    public TransactionHistoryListFragment() {}

    public long getEntityId() {
        return requireArguments().getLong(Constants.EXTRA_ID);
    }

    public int getEntityType() {
        return requireArguments().getInt(Constants.EXTRA_ENTITY);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mViewModel = new ViewModelProvider(requireActivity(),new ViewModelProvider.AndroidViewModelFactory())
                .get(TransactionHistoryListFragmentViewModel.class);

        long id = getEntityId();
        int entity = getEntityType();
        LocalDate start = LocalDate.MIN; // TODO: change date start
        LocalDate end = LocalDate.MAX; // TODO: change date end
        LiveData<List<TransactionHistoryModel>> liveData;
        if (entity == Constants.ENTITY_ACCOUNTS) {
            liveData = mViewModel.getTransactionsForAccountBetweenDates(id, start,end);
        }
        else {
            liveData = mViewModel.getTransactionsForPeopleBetweenDates(id,start,end);
        }
        liveData.observe(this, this::onTransactionHistoryListFetched);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_transaction_history_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mNavController = Navigation.findNavController(view);
        mList = view.findViewById(R.id.list);
        btnAddHistory = view.findViewById(R.id.btnAddHistory);
        mTransactionsAdapter = new SectionedTransactionHistoryAdapter(requireContext());
        mList.setAdapter(mTransactionsAdapter);
        btnAddHistory.setOnClickListener(v->onClickAddTransaction());
    }

    private void onTransactionHistoryListFetched(List<TransactionHistoryModel> histories) {
        mTransactionsAdapter.submitChildren(histories);
    }

    private void onClickAddTransaction() {
        // TODO: implement onClickAddTransaction
        long id = getEntityId();
        int entity = getEntityType();

        Bundle args = new Bundle();

    }
}
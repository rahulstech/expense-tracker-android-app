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
import dreammaker.android.expensetracker.databinding.FragmentTransactionHistoryListBinding;
import dreammaker.android.expensetracker.viewmodel.TransactionHistoryViewModel;

@SuppressWarnings("unused")
public class TransactionHistoryListFragment extends Fragment {

    private static final String TAG = TransactionHistoryListFragment.class.getSimpleName();

    private SectionedTransactionHistoryAdapter mTransactionsAdapter;

    private TransactionHistoryViewModel mViewModel;

    private NavController mNavController;

    private FragmentTransactionHistoryListBinding mBinding;

    public TransactionHistoryListFragment() {super();}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(TransactionHistoryViewModel.class);
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
        mTransactionsAdapter = new SectionedTransactionHistoryAdapter(requireContext());
    }

    private void onTransactionHistoryListFetched(List<TransactionHistoryModel> histories) {
        mTransactionsAdapter.submitList(histories);
    }

    private void onClickAddTransaction() {
        // TODO: implement onClickAddTransaction
        //long id = getEntityId();
        //int entity = getEntityType();

        Bundle args = new Bundle();

    }
}
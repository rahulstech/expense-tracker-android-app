package dreammaker.android.expensetracker.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import dreammaker.android.expensetracker.database.model.TransactionHistoryDisplayModel;

public class TransactionHistoriesListAdapter extends BaseCheckableItemRecyclerViewListAdapter<TransactionHistoryDisplayModel, TransactionHistoriesListAdapter.TransactionHistoryViewMode> {

    private static DiffUtil.ItemCallback<TransactionHistoryDisplayModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<TransactionHistoryDisplayModel>() {
        @Override
        public boolean areItemsTheSame(@NonNull TransactionHistoryDisplayModel oldItem, @NonNull TransactionHistoryDisplayModel newItem) {
            return oldItem.getTransactionId() == newItem.getTransactionId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TransactionHistoryDisplayModel oldItem, @NonNull TransactionHistoryDisplayModel newItem) {
            return oldItem.equals(newItem);
        }
    };

    public TransactionHistoriesListAdapter(@NonNull Context context) {
        super(context, DIFF_CALLBACK);
    }

    @Override
    protected TransactionHistoryViewMode onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionHistoryViewMode holder, int position) {

    }

    public static class TransactionHistoryViewMode extends BaseCheckableItemRecyclerViewListAdapter.BaseCheckableItemViewHolder<TransactionHistoryDisplayModel> {


        public TransactionHistoryViewMode(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bind(@Nullable TransactionHistoryDisplayModel item, @Nullable Object payload) {

        }
    }
}

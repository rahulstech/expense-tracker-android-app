package dreammaker.android.expensetracker.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("unused")
public interface IHeaderFooterAdapter {

    int LIST_HEADER_TYPE = -1000;

    int LIST_FOOTER_TYPE = -2000;

    @Nullable
    Object getHeaderData();

    @Nullable
    Object getFooterData();

    void setHasListHeader(boolean hasHeader);

    boolean hasListHeader();

    void setHasListFooter(boolean hasFooter);

    boolean hasListFooter();

    @NonNull
    RecyclerView.ViewHolder onCreateListHeaderViewHolder(@NonNull ViewGroup parent);

    @NonNull
    RecyclerView.ViewHolder onCreateListFooterViewHolder(@NonNull ViewGroup parent);

    void onBindListHeaderViewHolder(@NonNull RecyclerView.ViewHolder holder, @Nullable Object data);
    void onBindListFooterViewHolder(@NonNull RecyclerView.ViewHolder holder, @Nullable Object data);
}

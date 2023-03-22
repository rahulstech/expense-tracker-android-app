package dreammaker.android.expensetracker.view.adapter;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public interface OnRecyclerViewItemCheckChangeListener {

    void onRecyclerViewItemCheckChanged(RecyclerView.Adapter<?> adapter, View which, int position, boolean checked);
}

package dreammaker.android.expensetracker.view;
import android.view.*;

public interface OnItemChildClickListener<ADAPTER, VIEWHOLDER> {
	void onItemChildClicked(ADAPTER adapter, VIEWHOLDER viewholder, View childView);
}

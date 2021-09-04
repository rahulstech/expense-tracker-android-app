package dreammaker.android.expensetracker.view;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.util.Check;

public class RVAViewHolder extends RecyclerView.ViewHolder {

    private OnChildClickListener onChildClickListener;
    private View.OnClickListener clickListener = v -> {
        if (hasOnChildClickListener())
            getOnChildClickListener().onChildClick(this,v);
    };

    public RVAViewHolder(View itemView){
		super(itemView);
    }

    public void bindChildForClick(View child) {
        child.setOnClickListener(clickListener);
    }

    public View getRoot(){
        return itemView;
    }

    public void setOnChildClickListener(OnChildClickListener onChildClickListener) {
        this.onChildClickListener = onChildClickListener;
    }

    public OnChildClickListener getOnChildClickListener() {
        return onChildClickListener;
    }

    public boolean hasOnChildClickListener(){
        return Check.isNonNull(onChildClickListener);
    }

    @SuppressWarnings("unchecked")
    public <V extends View> V findViewById(int id){
        return (V) itemView.findViewById(id);
    }

    public interface OnChildClickListener{
        void onChildClick(RecyclerView.ViewHolder vh, View child);
    }
}

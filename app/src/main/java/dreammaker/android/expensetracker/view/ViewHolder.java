package dreammaker.android.expensetracker.view;

import android.view.View;
import android.widget.Checkable;

import dreammaker.android.expensetracker.util.Check;

public abstract class ViewHolder {
    private int adapterPosition;
    private View itemView;
    private OnChildClickListener onChildClickListener;

    private View.OnClickListener rootClickListener = v -> {
        if (null != getOnChildClickListener())
            getOnChildClickListener().onChildClick(ViewHolder.this,v);
    };
    
    public ViewHolder(View itemView){
        this.itemView = itemView;
    }

    public void setOnChildClickListener(OnChildClickListener onChildClickListener) {
        this.onChildClickListener = onChildClickListener;
        if (null != onChildClickListener) {
            getRoot().setOnClickListener(rootClickListener);
        }
        else {
            getRoot().setOnClickListener(null);
        }
    }

    public OnChildClickListener getOnChildClickListener() {
        return onChildClickListener;
    }

    public View getRoot(){
        return itemView;
    }
    
    public int getAdapterPosition(){
        return adapterPosition;
    }
    
    public void setAdapterPosition(int adapterPosition){
        this.adapterPosition = adapterPosition;
    }

    public void setChecked(boolean checked){
        if (itemView instanceof Checkable) ((Checkable) itemView).setChecked(checked);
        else itemView.setSelected(checked);
    }
    
    @SuppressWarnings("unchecked")
    public <V extends View> V findViewById(int id){ return (V) itemView.findViewById(id);}

    public interface OnChildClickListener{
        void onChildClick(ViewHolder vh, View child);
    }
}

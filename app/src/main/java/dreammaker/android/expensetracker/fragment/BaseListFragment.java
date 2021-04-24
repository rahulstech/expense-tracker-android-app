package dreammaker.android.expensetracker.fragment;

import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Helper;

public abstract class BaseListFragment<VH extends BaseListFragment.ListFragmentViewHolder> extends BaseFragment<VH> {

    protected BaseListFragment(){
        super();
    }

    public void configureEmptyContent(boolean isEmpty){
        getViewHolder().configureEmptyContent(isEmpty);
    }

    public static class ListFragmentViewHolder extends FragmentViewHolder{
        FloatingActionButton add;
        RecyclerView list;
        TextView empty;

        public ListFragmentViewHolder(@NonNull View root) {
            super(root);
            add = findViewById(R.id.add);
            list = findViewById(android.R.id.list);
            empty = findViewById(R.id.empty);
            Helper.setUpHideFABOnRecyclerViewScroll(list, add);
        }

        public void setOnAddListener(View.OnClickListener listener){
            add.setOnClickListener(listener);
        }

        public void setEmptyText(CharSequence text){
            empty.setText(text);
        }

        public void setEmptyText(@StringRes int resId){
            empty.setText(resId);
        }

        public void configureEmptyContent(boolean isEmpty){
            if(isEmpty){
                list.setVisibility(View.GONE);
                empty.setVisibility(View.VISIBLE);
            }
            else{
                empty.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
            }
        }
    }
}

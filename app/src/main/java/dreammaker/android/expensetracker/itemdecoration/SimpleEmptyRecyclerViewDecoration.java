package dreammaker.android.expensetracker.itemdecoration;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import dreammaker.android.expensetracker.R;

@SuppressWarnings("unused")
public class SimpleEmptyRecyclerViewDecoration extends AbsEmptyRecyclerViewDecoration {

    private final CharSequence mText;

    private final Drawable mIcon;

    public SimpleEmptyRecyclerViewDecoration(CharSequence text, Drawable icon) {
        super();
        mText = text;
        mIcon = icon;
    }

    @NonNull
    @Override
    protected View onCreateEmptyView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        View view = inflater.inflate(R.layout.empty_placeholder,parent,false);
        TextView text1 = view.findViewById(R.id.text1);
        text1.setText(mText);
        text1.setCompoundDrawablesWithIntrinsicBounds(null,mIcon,null,null);
        return view;
    }
}

package dreammaker.android.expensetracker.itemdecoration;

import android.graphics.Canvas;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.BuildConfig;

@SuppressWarnings("unused")
public abstract class AbsEmptyRecyclerViewDecoration extends RecyclerView.ItemDecoration {

    private static final boolean DEBUG = BuildConfig.DEBUG;

    private static final String TAG = AbsEmptyRecyclerViewDecoration.class.getSimpleName();

    private View mEmpty;

    protected AbsEmptyRecyclerViewDecoration() {}

    @NonNull
    protected abstract View onCreateEmptyView(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent);

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        boolean empty = parent.getChildCount() == 0;
        if (!empty) {
            return;
        }
        if (null == mEmpty) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            mEmpty = onCreateEmptyView(inflater,parent);
            //noinspection ConstantConditions
            if (null == mEmpty) {
                throw new NullPointerException("onCreateEmptyView must return non null");
            }
        }
        int drawWidth = parent.getWidth();
        int drawHeight = parent.getHeight();
        int widthSpec = View.MeasureSpec.makeMeasureSpec(drawWidth, View.MeasureSpec.AT_MOST);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(drawHeight, View.MeasureSpec.AT_MOST);
        mEmpty.measure(widthSpec,heightSpec);
        mEmpty.layout(0,0, mEmpty.getMeasuredWidth(), mEmpty.getMeasuredHeight());
        int translateX = drawWidth/2 - mEmpty.getMeasuredWidth()/2;
        int translateY = drawHeight/2 - mEmpty.getMeasuredHeight()/2;
        if (DEBUG) {
            Log.d(TAG,"parent="+drawWidth+"x"+drawHeight+" emptyView="+mEmpty.getMeasuredWidth()+"x"+mEmpty.getMeasuredHeight()+
                    " show@("+translateX+","+translateY+")");
        }

        int count = c.save();
        c.translate(translateX,translateY);
        mEmpty.draw(c);
        c.restoreToCount(count);
    }
}

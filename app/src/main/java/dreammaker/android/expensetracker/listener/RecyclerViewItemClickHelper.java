package dreammaker.android.expensetracker.listener;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings({"unused"})
public class RecyclerViewItemClickHelper implements RecyclerView.OnItemTouchListener {

    @SuppressWarnings("FieldCanBeLocal")
    private final GestureDetector.SimpleOnGestureListener mGestureListener
            = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (canHandleEvent()) {
                handleClickEvent(e);
                return true;
            }
            return false;
        }
    };

    private final GestureDetector mGestureDetector;

    private final RecyclerView mRecyclerView;

    private boolean mShouldHandleEvent = true;

    private boolean mInterceptAllowed = true;

    @Nullable
    private OnItemClickListener mClickListener;

    public RecyclerViewItemClickHelper(@NonNull RecyclerView view) {
        mRecyclerView = view;
        mGestureDetector = new GestureDetector(view.getContext(),mGestureListener);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        mShouldHandleEvent = false;
        boolean accepted = mGestureDetector.onTouchEvent(e);
        mShouldHandleEvent = true;
        return accepted;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        mInterceptAllowed = disallowIntercept;
    }

    private boolean canHandleEvent() {
        return mInterceptAllowed && mShouldHandleEvent;
    }

    private void handleClickEvent(@NonNull MotionEvent e) {
        final float x = e.getX();
        final float y = e.getY();
        View itemView = mRecyclerView.findChildViewUnder(x,y);
        if (null == itemView) {
            return;
        }
        int position = mRecyclerView.getChildAdapterPosition(itemView);
        if (null != mClickListener) {
            mClickListener.onClickItem(mRecyclerView,itemView,position);
        }
    }
}

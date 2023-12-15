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

        @Override
        public void onLongPress(MotionEvent e) {
            if (canHandleEvent()) {
                handleLongClickEvent(e);
            }
        }
    };

    private final GestureDetector mGestureDetector;

    @NonNull
    private final RecyclerView mRecyclerView;

    private boolean mShouldHandleEvent = true;

    private boolean mInterceptAllowed = true;

    @Nullable
    private OnItemClickListener mClickListener;

    @Nullable
    private OnItemLongClickListener mLongClickListener;

    public RecyclerViewItemClickHelper(@NonNull RecyclerView view) {
        mRecyclerView = view;
        mGestureDetector = new GestureDetector(view.getContext(),mGestureListener);
        view.addOnItemTouchListener(this);
    }

    @NonNull
    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongClickListener = listener;
        mGestureDetector.setIsLongpressEnabled(null != listener);
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
        View itemView = findChildViewUnder(e);
        if (null == itemView) {
            return;
        }
        int position = mRecyclerView.getChildAdapterPosition(itemView);
        if (null != mClickListener) {
            mClickListener.onClickItem(mRecyclerView,itemView,position);
        }
    }

    private void handleLongClickEvent(@NonNull MotionEvent e) {
        View itemView = findChildViewUnder(e);
        if (null == itemView) {
            return;
        }
        int position = mRecyclerView.getChildAdapterPosition(itemView);
        if (null != mLongClickListener) {
            mLongClickListener.onLongClickItem(mRecyclerView,itemView,position);
        }
    }

    @Nullable
    private View findChildViewUnder(@NonNull MotionEvent e) {
        final float x = e.getX();
        final float y = e.getY();
        return mRecyclerView.findChildViewUnder(x,y);
    }
}

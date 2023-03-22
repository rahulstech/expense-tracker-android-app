package dreammaker.android.expensetracker.util;

import android.app.Activity;
import android.content.Context;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

public class Helper {
  public static final int ACTION_INSERT = 10;
  public static final int ACTION_EDIT = 11;
  public static final int ACTION_DELETE = 12;

  public static final int CATEGORY_TRANSACTION = 22;
  public static final int CATEGORY_SEND_MONEY = 23;
  public static final int CATEGORY_RECEIVE_MONEY = 24;

  public static final String EXTRA_ID = "dreammaker.android.expensetracker.extra.ID";

  public static final String CATEGORY = "category";

  public static void setTitle(@Nullable Activity activity, @StringRes int title){
    if (Check.isNonNull(activity)){
      setTitleAndSubTitle(activity, activity.getString(title), null);
    }
  }

  public static void setTitleAndSubTitle(@Nullable Activity activity, CharSequence title, CharSequence subtitle){
    if (Check.isNonNull(activity)){
      if (activity instanceof AppCompatActivity){
        AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
        appCompatActivity.getSupportActionBar().setTitle(title);
        appCompatActivity.getSupportActionBar().setSubtitle(subtitle);
      }
      else {
        activity.getActionBar().setTitle(title);
        activity.getActionBar().setSubtitle(title);
      }
    }
  }

  public static void setUpHideFABOnRecyclerViewScroll(@NonNull RecyclerView rv, @NonNull final FloatingActionButton btn){
    rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {}

      @Override
      public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        if (dy > 0){
          btn.hide();
        }
        else {
          btn.show();
        }
      }
    });
  }

  public static String floatToString(float fValue){
    if (fValue == (int) fValue){
      return String.format("%d", (int) fValue);
    }
    if (fValue*10 == (int) (fValue*10)){
      return String.format("%.1f", fValue);
    }
    return String.format("%.2f", fValue);
  }

  public static String getResourceString(@NonNull Context context, @StringRes int resId) {
    return getResourceString(context,resId);
  }

  public static String getResourceString(@NonNull Context context, @StringRes int resId, @Nullable Object... args) {
    if (null == context) {
      throw new NullPointerException("context == null");
    }
    return context.getString(resId, args);
  }
}

package dreammaker.android.expensetracker.util;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An utility class with returns the positions of items
 * in the main list of item which matches against a key.
 * This class automatically cancels the previous filter result
 * if a new filter request arrives during an ongoin filtering request.
 * The user of the class can also cancel an filter manually by using
 * cancel method.
 */
public class Filter<I> {

    private static final String TAG = "Filter";
    private static final String KEY_LAST_FILTER_KEY = "last_filter_key";
    private static final String KEY_TASK_CANCELED = "task_canceled";
    private static final String KEY_FILTER_GENERATION = "filter_generation";

    // the most recent filter key
    private String lastFilterKey = null;

    // the total number of filter request handled by this class
    private int filterGeneration = 0;

    // weather the ongoing task is canceled or not
    private boolean taskCanceled = false;

    // the complete list of items
    private List<I> items;

    private FilterCallback<I> callback;

    public Filter(FilterCallback<I> callback){
        if(null == callback) throw new NullPointerException("non null instance of FilterCallback is required in constructor");
        this.callback = callback;
    }

    public void changeItems(List<I> newItems){
        this.items = newItems;
        cancel();
    }

    /**
     * Called by the user of this class to start a new filter
     *
     * @param newKey the filter key for the new filter request
     */
    public final void filter(String newKey){
        if(Check.isEqualString(lastFilterKey, newKey)) return;
        int currentGeneration = ++filterGeneration;
        lastFilterKey = newKey;
        taskCanceled = false;
        commit(items, newKey, currentGeneration);
    }

    /**
     * cancels an ongoing filter task
     */
    public void cancel(){
        taskCanceled = true;
    }

    /**
     * @returns the filter key for the last filter
     */
    @Nullable
    public String getKey(){
        return lastFilterKey;
    }

    public Bundle onSaveState(){
        Bundle outState = new Bundle();
        outState.putInt(KEY_FILTER_GENERATION, filterGeneration);
        outState.putBoolean(KEY_TASK_CANCELED, taskCanceled);
        outState.putString(KEY_LAST_FILTER_KEY, lastFilterKey);
        return outState;
    }

    public void onRestoreState(Bundle savedState){
        filterGeneration = savedState.getInt(KEY_FILTER_GENERATION, 0);
        taskCanceled = savedState.getBoolean(KEY_TASK_CANCELED, false);
        lastFilterKey = savedState.getString(KEY_LAST_FILTER_KEY, null);
    }

    private void commit(final List<I> list, final String newKey, final int generation){
        if(null == newKey){
            callback.onFilterComplete(null, null);
            return;
        }
        AppExecutor.getDiskOperationsExecutor().execute(new Runnable(){
            @Override
            public void run(){
                final int myGeneration = generation;
                final List<I> items = list;
                if(null != items){
                    final ArrayList<Integer> removed = new ArrayList<>();
                    final ArrayList<Integer> inserted = new ArrayList<>();
                    int pos = 0;
                    for(I item : items){
                        if(taskCanceled || Check.isNull(callback)) break;
                        if(callback.onMatch(item, newKey)){
                            inserted.add(pos);
                        }
                        else {
                            removed.add(pos);
                        }
                        pos++;
                    }
                    if(myGeneration == filterGeneration && !taskCanceled){
                        AppExecutor.getMainThreadExecutor().execute(new Runnable(){
                            @Override
                            public void run(){
                                if (Check.isNonNull(callback)) callback.onFilterComplete(inserted, removed);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Callback methods used by Filter class for various purpose
     */
    public interface FilterCallback<I>{
        /**
         * Matches an single item against the filter key.
         * This method contains the actual filtering item by item logic.
         * Note: this method is run on a background thread
         *
         * @param item a single item to match
         * @param key the filter key to match with
         * @return true if the item and the key matches,
         *			false otherwise
         */
        boolean onMatch(@Nullable I item, @NonNull String key);

        /**
         * Called when the filtering is complete.
         *
         * @param inserted positions of the items in the
         *                 list of items which returned true
         *                 in onMatch(I,K) method call
         * @param removed positions of the items in the
         *                list of items for which
         *                onMath(I,K) returns false
         */
        void onFilterComplete(@Nullable List<Integer> inserted, @Nullable List<Integer> removed);
    }
}

package dreammaker.android.expensetracker.adapter;

import androidx.annotation.NonNull;

/**
 * Callback method to calculate difference between two non-null items in a list
 *
 * @param <H> header item type
 * @param <C> child item type
 */
public interface ItemCallback<H,C> {

    boolean isSameHeader(@NonNull H oldHeader, @NonNull H newHeader);

    boolean isSameChild(@NonNull C oldChild, @NonNull C newChild);

    boolean isHeaderContentSame(@NonNull H oldHeader, @NonNull H newHeader);

    boolean isChildContentSame(@NonNull C oldChild, @NonNull C newChild);
}

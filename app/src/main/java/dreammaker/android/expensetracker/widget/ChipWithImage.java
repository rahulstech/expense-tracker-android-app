package dreammaker.android.expensetracker.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;

import com.google.android.material.chip.Chip;

import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.R;

public class ChipWithImage extends Chip {
    public ChipWithImage(Context context) {
        this(context,null);
    }

    public ChipWithImage(Context context, AttributeSet attrs) {
        super(context, attrs,R.attr.chipWithImageStyle);

    }

    public void setImage(@Nullable Uri src, @Nullable Drawable placeholder) {
        setChipIcon(placeholder);
    }
}

package dreammaker.android.expensetracker.core.ui.helper

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview


@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
@Preview(name = "Phone - FontScale 85%",
    device = "spec:width=411dp,height=891dp,dpi=420",
    fontScale = .85f,
    showSystemUi = true
)
@Preview(name = "Phone - Landscape - FontScale 150%",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    fontScale = 1.5f,
    showSystemUi = true
)
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240", showSystemUi = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES, showSystemUi = true)
annotation class PreviewScreen
package dreammaker.android.expensetracker.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.window.DialogProperties
import dreammaker.android.expensetracker.R

@Composable
fun YesNoDialog(
    header: String,
    onYes: ()-> Unit,
    onNo: ()-> Unit,
    body: String = "",
    yesText: String = stringResource(R.string.label_yes),
    noText: String = stringResource(R.string.label_no),
    dismissOnBackPressed: Boolean = true,
    dismissOnClickOutside: Boolean = true,
) {
    AlertDialog(
        shape = MaterialTheme.shapes.medium,
        onDismissRequest = {},
        title = {
            if (header.isNotBlank()) {
                Text(
                    text = header,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            if (body.isNotBlank()) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onYes) {
                Text(yesText)
            }
        },
        dismissButton = {
            TextButton(onClick = onNo) {
                Text(noText)
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPressed,
            dismissOnClickOutside = dismissOnClickOutside
        )
    )
}

@PreviewScreenSizes
@Composable
fun YesNoDialogPreview() {
    YesNoDialog(
        header = "Header",
        body = "This is long body for this dialog",
        yesText = "I Want",
        noText = "No Thanks",
        onYes = {},
        onNo = {}
    )
}
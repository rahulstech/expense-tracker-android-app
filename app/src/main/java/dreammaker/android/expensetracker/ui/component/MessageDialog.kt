package dreammaker.android.expensetracker.ui.component

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.util.fastJoinToString
import androidx.compose.ui.window.DialogProperties
import dreammaker.android.expensetracker.core.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme

@Composable
fun MessageDialog(
    body: String,
    onDismiss: () -> Unit,
    header: String = "",
    dismissText: String = stringResource(R.string.label_ok),
    dismissOnBackPressed: Boolean = true,
    dismissOnClickOutside: Boolean = true,
) {
    AlertDialog(
        shape = MaterialTheme.shapes.medium,
        onDismissRequest = onDismiss,
        title = {
            if (header.isNotBlank()) {
                Text(
                    text = header,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPressed,
            dismissOnClickOutside = dismissOnClickOutside
        )
    )
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
fun MessageDialogPreview() {
    ExpenseTrackerTheme {
        MessageDialog(
            header = "This is a very long header to test that if the header fits the area properly",
            body = (1..2000).map { "$it This is long body for this dialog" }.fastJoinToString("\n"),
            onDismiss = {},
            dismissText = "Ok"
        )
    }
}
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
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.window.DialogProperties
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme

@Composable
fun YesNoDialog(
    onYes: () -> Unit,
    onNo: () -> Unit,
    modifier: Modifier = Modifier,
    header: String = "",
    body: String = "",
    yesText: String = stringResource(R.string.label_yes),
    noText: String = stringResource(R.string.label_no),
    onDismissRequest: ()-> Unit = {},
    dismissOnBackPressed: Boolean = true,
    dismissOnClickOutside: Boolean = true,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        shape = MaterialTheme.shapes.medium,
        title = header.takeIf { it.isNotBlank() }?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        text = body.takeIf { it.isNotBlank() }?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.verticalScroll(state = rememberScrollState()),
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
            dismissOnClickOutside = dismissOnClickOutside,
        ),
    )
}

@PreviewScreenSizes
@Composable
fun YesNoDialogPreview() {
    ExpenseTrackerTheme {
        YesNoDialog(
            header = "Header",
            body = "This is a long body text for this dialog",
            yesText = "I Want",
            noText = "No Thanks",
            onYes = {},
            onNo = {},
        )
    }
}

package dreammaker.android.expensetracker.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dreammaker.android.expensetracker.core.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YesNoDialog(
    onYes: () -> Unit,
    onNo: () -> Unit,
    modifier: Modifier = Modifier,
    header: String = stringResource(R.string.label_choose_one),
    body: String = "",
    yesText: String = stringResource(R.string.label_yes),
    noText: String = stringResource(R.string.label_no),
    dismissOnBackPressed: Boolean = true,
    dismissOnClickOutside: Boolean = true,
) {
    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPressed,
            dismissOnClickOutside = dismissOnClickOutside,
        ),
    ) {
        Card(
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                header.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                body.takeIf { body.isNotBlank() }?.let {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.verticalScroll(state = rememberScrollState()),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {

                    TextButton(onClick = onNo) {
                        Text(noText, maxLines = 1, overflow = TextOverflow.Clip, style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    TextButton(onClick = onYes) {
                        Text(yesText, maxLines = 1, overflow = TextOverflow.Clip, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
fun YesNoDialogPreview() {
    ExpenseTrackerTheme {
        YesNoDialog(
            header = "Header",
            body = "This is a long body text for this dialog",
            yesText = "I Want",
            onYes = {},
            noText = "No Thanks",
            onNo = {},
        )
    }
}

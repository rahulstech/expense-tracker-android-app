package dreammaker.android.expensetracker.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressDialog(
    text: String,
    onAction: (()-> Unit)? = null,
    actionText: String = stringResource(android.R.string.cancel)
) {
    BasicAlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )
    ) {
        Card(
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )

                Spacer(modifier = Modifier.height(24.dp))

                onAction?.let { action ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = action) {
                            Text(actionText, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}


@PreviewLightDark
@Composable
fun ProgressDialogPreview() {
    ExpenseTrackerTheme {
        Scaffold { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
            ) {
                ProgressDialog(
                    text = "Work in progress",
                    actionText = "Cancel",
                    onAction = {}
                )
            }
        }
    }
}
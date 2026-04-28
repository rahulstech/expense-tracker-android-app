package dreammaker.android.expensetracker.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ButtonWithProgressBar(
    buttonText: String,
    onClick: ()-> Unit,
    modifier: Modifier = Modifier,
    progressText: String = "",
    showProgressBar: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled && !showProgressBar
    ) {
        if (showProgressBar) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ){
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.Transparent
                )

                if (progressText.isNotBlank()) {
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = progressText,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        else {
            Text(buttonText)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ButtonWithProgressBarPreview() {

    var progressText by remember { mutableStateOf("") }
    var showProgressBar by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    ButtonWithProgressBar(
        buttonText = "Click Me",
        onClick = {
            coroutineScope.launch {
                progressText = "initializing"
                delay(700)
                progressText = "done 40%"
                delay(700)
                progressText = "done 98%"
                delay(700)
                progressText = "done"
            }
            coroutineScope.launch {
                showProgressBar = true
                delay(3000)
                showProgressBar = false
            }
        },
        progressText = progressText,
        showProgressBar = showProgressBar
    )
}

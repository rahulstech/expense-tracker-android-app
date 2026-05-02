package dreammaker.android.expensetracker.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ButtonWithProgressBar(
    buttonText: String,
    onClick: ()-> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showProgressBar: Boolean = false,
    progressText: String = "",
    shape: Shape = ButtonDefaults.shape,
) = ButtonWithProgressBar(
        buttonText = {
            Text(buttonText)
        },
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        showProgressBar = showProgressBar,
        progressText = {
            Text(
                text = progressText,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        shape = shape
    )

@Composable
fun ButtonWithProgressBar(
    buttonText: @Composable ()-> Unit,
    onClick: ()-> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showProgressBar: Boolean = false,
    progressText: (@Composable ()-> Unit)? = null,
    shape: Shape = ButtonDefaults.shape,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled && !showProgressBar,
        shape = shape
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

                progressText?.let {
                    Spacer(modifier = Modifier.width(12.dp))
                    it.invoke()
                }
            }
        }
        else {
            buttonText()
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
        buttonText = {
            Text("Click Me")
        },
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
        progressText = {
            Text(progressText)
        },
        showProgressBar = showProgressBar
    )
}

package dreammaker.android.expensetracker.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme

@Immutable
class AmountTextFiledColors private constructor(
    val valueColor: Color = Color.Unspecified,
    val borderFocusedColor: Color = Color.Unspecified,
    val borderUnfocusedColor: Color = Color.Unspecified,
    val borderErrorColor: Color = Color.Unspecified
) {
    companion object {

        @Composable
        fun defaultColors(
            valueColor: Color = LocalTextStyle.current.color,
            borderFocusedColor: Color = MaterialTheme.colorScheme.primary,
            borderUnfocusedColor: Color = MaterialTheme.colorScheme.outline,
            borderErrorColor: Color = MaterialTheme.colorScheme.error
        ): AmountTextFiledColors {
            return AmountTextFiledColors(
                valueColor = valueColor,
                borderFocusedColor = borderFocusedColor,
                borderUnfocusedColor = borderUnfocusedColor,
                borderErrorColor = borderErrorColor
            )
        }
    }
}

@Composable
fun AmountTextField(
    value: String,
    onValueChange: (String)-> Unit,
    modifier: Modifier = Modifier,
    colors: AmountTextFiledColors = AmountTextFiledColors.defaultColors(),
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    labelText: String? = null,
    errorText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = {
            Text(
                labelText ?: "0.00",
                style = textStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f)
            )
        },
        textStyle = textStyle,
        prefix = {
            Text(
                text = stringResource(R.string.symbol_rupee),
                style = textStyle,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
        },
        trailingIcon = {
            if (value.isNotBlank()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "clear amount",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            autoCorrectEnabled = false,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.valueColor,
            unfocusedTextColor = colors.valueColor,
            errorTextColor = colors.valueColor,
            focusedBorderColor = colors.borderFocusedColor,
            unfocusedBorderColor = colors.borderUnfocusedColor,
            errorBorderColor = colors.borderErrorColor,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
        ),
        isError = errorText != null,
        supportingText = errorText?.let { text -> {
                Text(
                    text,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall
                )
        }},
        singleLine = true
    )
}


@PreviewLightDark
@Composable
fun AmountTextFieldPreview() {
    var value by remember { mutableStateOf("") }

    ExpenseTrackerTheme {
        Surface {
            AmountTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
    }
}
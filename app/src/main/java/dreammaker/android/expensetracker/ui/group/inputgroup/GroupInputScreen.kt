package dreammaker.android.expensetracker.ui.group.inputgroup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import rahulstech.android.expensetracker.domain.model.Group

@Composable
fun GroupInputScreen(
    uiState: GroupInputUIState,
    onCancel: () -> Unit,
    onSave: (Group) -> Unit = {}
) {
    var name by rememberSaveable { mutableStateOf("") }
    var balance by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var balanceError by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.group) {
        uiState.group?.let {
            name = it.name
            balance = it.balance.toString()
        }
    }

    LaunchedEffect(uiState.isSaving, uiState.isSaveSuccessful) {
        if (!uiState.isSaving && uiState.isSaveSuccessful) {
            onCancel()
        }
    }

    val context = LocalContext.current

    fun validateName() {
        nameError = when {
            name.isBlank() -> context.getString(R.string.error_empty_group_name)
            else -> null
        }
    }

    fun validateBalance() {
        val amount = balance.toFloatOrNull()
        balanceError = when {
            amount == null -> context.getString(R.string.error_invalid_balance_input) // Reuse account error if specific one not available
            else -> null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = null
            },
            label = { Text(stringResource(R.string.hint_group_name)) },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } },
            trailingIcon = {
                if (name.isNotEmpty()) {
                    IconButton(onClick = { name = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = balance,
            onValueChange = {
                balance = it
                balanceError = null
            },
            label = { Text(stringResource(R.string.hint_group_balance)) },
            modifier = Modifier.fillMaxWidth(),
            isError = balanceError != null,
            supportingText = balanceError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                validateName()
                validateBalance()
                if (nameError == null && balanceError == null) {
                    val amount = balance.toFloatOrNull() ?: 0f
                    val group = Group(
                        id = uiState.group?.id ?: 0L,
                        name = name,
                        balance = amount
                    )
                    onSave(group)
                }
            },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.save))
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.cancel))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupInputScreenPreview() {
    ExpenseTrackerTheme {
        GroupInputScreen(
            uiState = GroupInputUIState(),
            onCancel = {},
            onSave = {},
        )
    }
}

package dreammaker.android.expensetracker.ui.group.inputgroup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.util.QuickMessages
import dreammaker.android.expensetracker.ui.component.SaveCancelActionButtons
import dreammaker.android.expensetracker.ui.component.YesNoDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.domain.model.Group

@Composable
fun GroupInputScreen(
    isEdit: Boolean,
    viewModel: GroupInputViewModel,
    exit: () -> Unit,
) {
    val context = LocalContext.current
    var showAddMoreDialog by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var balance by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var balanceError by rememberSaveable { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    // validation methods
    fun validateName(): Boolean {
        nameError = when {
            name.isBlank() -> context.getString(R.string.error_empty_group_name)
            else -> null
        }
        return null == nameError
    }

    fun validateBalance(): Boolean {
        val amount = balance.toFloatOrNull()
        balanceError = when {
            balance.isBlank() -> context.getString(R.string.error_empty_balance_input)
            amount == null -> context.getString(R.string.error_invalid_balance_input)
            else -> null
        }
        return null == balanceError
    }

    // handle ui events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is GroupInputUIEvent.SaveError -> {
                    QuickMessages.toastError(
                        context,
                        context.getString(R.string.group_save_unsuccessful),
                        true
                    )
                }
                is GroupInputUIEvent.SaveSuccessful -> {
                    showAddMoreDialog = true
                }
                is GroupInputUIEvent.LoadingError -> {
                    QuickMessages.toastError(
                        context,
                        context.getString(R.string.message_group_not_found),
                        true
                    )
                    exit()
                }
            }
        }
    }

    // handle group loading
    LaunchedEffect(uiState.group?.id) {
        if (isEdit) {
            if (!uiState.isLoadingGroup) {
                // on loading complete if group found set
                // the initial name and balance to the loaded group name and balance
                // if not found show toast and exit
                uiState.group?.let {
                    name = it.name
                    balance = it.balance.toString()
                }
            }
        }
    }

    if (uiState.isLoadingGroup) {
        FullScreenLoading()
    } else {
        GroupInputForm(
            isSaving = uiState.isSaving,
            onCancel = exit,
            onSave = {
                if (validateName() && validateBalance()) {
                    val amount = balance.toFloat()
                    val group = if (isEdit) {
                        Group(
                            id = uiState.group?.id ?: 0,
                            name = name,
                            balance = amount
                        )
                    } else {
                        Group(
                            name = name,
                            balance = amount
                        )
                    }
                    viewModel.saveGroup(group, isEdit)
                }
            },
            name = name,
            onNameChange = {
                name = it
                nameError = null
            },
            balance = balance,
            onBalanceChange = {
                balance = it
                balanceError = null
            },
            nameError = nameError,
            balanceError = balanceError
        )
    }

    if (showAddMoreDialog) {
        YesNoDialog(
            header = stringResource(R.string.add_more_group),
            body = "",
            yesText = stringResource(R.string.label_yes),
            noText = stringResource(R.string.label_no),
            onYes = {
                showAddMoreDialog = false
                name = ""
                balance = ""
            },
            onNo = {
                showAddMoreDialog = false
                exit()
            },
            dismissOnBackPressed = false,
            dismissOnClickOutside = false
        )
    }
}

//-------- Full Screen Group Loading ---------
@Composable
fun FullScreenLoading() {

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(.85f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.loading_group),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                trackColor = MaterialTheme.colorScheme.primaryContainer
            )
        }
    }
}

//-------- Group Input Form -----------

@Composable
fun GroupInputForm(
    name: String,
    onNameChange: (String) -> Unit,
    balance: String,
    onBalanceChange: (String) -> Unit,
    nameError: String?,
    balanceError: String?,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    isSaving: Boolean = false
) {
    val nameLabel = stringResource(R.string.hint_group_name)
    val balanceLabel = stringResource(R.string.hint_group_balance)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(nameLabel) },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it) } },
            trailingIcon = {
                if (name.isNotEmpty()) {
                    IconButton(onClick = { onNameChange("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = balance,
            onValueChange = onBalanceChange,
            label = { Text(balanceLabel) },
            modifier = Modifier.fillMaxWidth(),
            isError = balanceError != null,
            supportingText = balanceError?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        SaveCancelActionButtons(
            isSaving = isSaving,
            onCancel = onCancel,
            onSave = onSave
        )
    }
}

//--- Preview -----------------

@PreviewLightDark
@Composable
fun FullScreenLoadingPreview() {
    ExpenseTrackerTheme {
        FullScreenLoading()
    }
}

@PreviewScreenSizes
@Composable
fun GroupInputFormPreview() {
    var isSaving by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    ExpenseTrackerTheme {
        GroupInputForm(
            name = "Food",
            onNameChange = {},
            balance = "100.0",
            onBalanceChange = {},
            nameError = null,
            balanceError = null,
            onSave = {
                coroutineScope.launch {
                    isSaving = true
                    delay(3000)
                    isSaving = false
                }
            },
            onCancel = {},
            isSaving = isSaving
        )
    }
}

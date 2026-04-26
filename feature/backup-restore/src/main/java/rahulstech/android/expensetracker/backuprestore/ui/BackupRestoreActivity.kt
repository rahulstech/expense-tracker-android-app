package rahulstech.android.expensetracker.backuprestore.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.core.util.QuickMessages
import rahulstech.android.expensetracker.backuprestore.Constants.BACKUP_FILE_MIME_TYPES
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.settings.AgentSettingsProvider
import rahulstech.android.expensetracker.backuprestore.settings.BackupFrequency
import rahulstech.android.expensetracker.backuprestore.util.FileEntry
import rahulstech.android.expensetracker.backuprestore.util.FileUtil
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

typealias PendingTask = () -> Unit

private val DATETIME_FORMAT = DateTimeFormatter.ofPattern("EEE, dd-MMM-yyyy hh:mm a")

@AndroidEntryPoint
class BackupRestoreActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BackupRestoreActivity"

    }

    @Inject
    lateinit var settings: AgentSettingsProvider

    private val viewModel: BackupRestoreViewModel by viewModels()

    private lateinit var permissionRequestLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>

    private var pendingTask: PendingTask? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionRequestLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionResult
        )

        openDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument(), this::onPickBackupFile
        )

        setContent {
            ExpenseTrackerTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.activity_title_backup_restore)) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        Icons.AutoMirrored.Default.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                scrolledContainerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                ) { paddingValues ->

                    Box(
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        BackupRestoreRoute(
                            viewModel = viewModel,
                            onStartBackup = { onClickStartBackup() },
                            onRestoreLocal = { onClickPickBackupFile() },
                        )
                    }
                }
            }
        }
    }

    // event handlers

    private fun onClickStartBackup() {
        val permissions: Array<String> = when {
            Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            Build.VERSION.SDK_INT <= 28 -> arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            else -> emptyArray()
        }
        if (permissions.isNotEmpty()) {
            runIfPermissionGranted(permissions) { startBackup() }
        } else {
            startBackup()
        }
    }

    private fun onClickPickBackupFile() {
        val permissions: Array<String> = when {
            Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            Build.VERSION.SDK_INT <= 28 -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            else -> emptyArray()
        }
        if (permissions.isNotEmpty()) {
            runIfPermissionGranted(permissions) { showPickBackupFileInstruction() }
        } else {
            showPickBackupFileInstruction()
        }
    }

    private fun showPickBackupFileInstruction() {
        QuickMessages.alertInformation(this,
            getString(R.string.message_pick_backup_file_instruction),
            QuickMessages.AlertButton(getString(R.string.label_choose)) {
                openDocumentLauncher.launch(arrayOf("application/*", "text/*"))
            },
            QuickMessages.AlertButton(getString(R.string.label_cancel))
        )
    }

    private fun onPickBackupFile(uri: Uri?) {
        if (null == uri) {
            Log.i(TAG, "no backup file picked; can not restore")
            showCancellableAlert(getString(R.string.message_no_backup_file_picked))
            return
        }
        val entry = FileUtil.getBackupFileDetails(this, uri)
        Log.i(TAG, "picked backup file for restore $entry")
        if (!entry.isOfType(BACKUP_FILE_MIME_TYPES)) {
            showCancellableAlert(getString(R.string.message_invalid_backup_file_type, entry.displayName))
            return
        }
        startRestore(entry)
    }

    private fun showCancellableAlert(message: String) {
        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setPositiveButton(R.string.label_cancel, null)
            .show()
    }

    // backup and restore

    private fun startBackup() {
        BackupRestoreHelper.startBackup(applicationContext, settings.getBackupFrequency())
    }

    private fun startRestore(entry: FileEntry) {
        BackupRestoreHelper.startRestore(applicationContext, entry)
    }

    // runtime permission related methods

    private fun runIfPermissionGranted(permissions: Array<String>, task: PendingTask) {
        if (hasPermission(permissions)) {
            task()
        } else {
            pendingTask = task
            permissionRequestLauncher.launch(permissions)
        }
    }

    private fun hasPermission(permissions: Array<String>): Boolean =
        permissions.all { permission ->
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, permission)
        }

    private fun onPermissionResult(grants: Map<String, Boolean>) {
        pendingTask?.let { task ->
            val notGranted = grants.entries
                .filter { !it.value }
                .map { it.key }
            if (notGranted.isEmpty()) {
                task.invoke()
                pendingTask = null
            } else {
                // If some permissions are not granted, we might want to inform the user or just not run the task.
                // The original logic calls runIfPermissionGranted again, which might cause an infinite loop if user keeps denying.
                // For now, let's just clear the pending task if any not granted.
                pendingTask = null
            }
        }
    }
}

@Composable
fun BackupRestoreRoute(
    viewModel: BackupRestoreViewModel,
    onStartBackup: () -> Unit,
    onRestoreLocal: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    BackupRestoreScreen(
        uiState = uiState,
        onStartBackup = onStartBackup,
        onRestoreLocal = onRestoreLocal,
        onFrequencyChange = { viewModel.changeBackupFrequency(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    uiState: BackupRestoreActivityUIState,
    onStartBackup: () -> Unit,
    onRestoreLocal: () -> Unit,
    onFrequencyChange: (BackupFrequency) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Backup Section
        SectionBackup(
            lastLocalBackupTime = uiState.lastLocalBackupTime,
            currentFrequency = uiState.backupFrequency,
            onStartBackup = onStartBackup,
            onFrequencyChange = onFrequencyChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Restore Section
        SectionRestore(
            onRestoreLocal = onRestoreLocal
        )
    }
}


@Composable
fun SectionBackup(
    lastLocalBackupTime: LocalDateTime?,
    currentFrequency: BackupFrequency,
    onStartBackup: () -> Unit,
    onFrequencyChange: (BackupFrequency) -> Unit
) {
    val lastLocalBackupTimeText by remember(lastLocalBackupTime) {
        derivedStateOf {
            lastLocalBackupTime?.format(DATETIME_FORMAT)
        }
    }

    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.description_backup_settings),
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = stringResource(
                    R.string.label_last_local_backup_time,
                    lastLocalBackupTimeText ?: stringResource(R.string.label_last_local_backup_time_never)
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(onClick = onStartBackup) {
                Text(stringResource(R.string.label_start_backup))
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.label_auto_backup),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(12.dp))

                FrequencyDropdown(
                    currentFrequency = currentFrequency,
                    onFrequencyChange = onFrequencyChange
                )
            }
        }
    }
}

@Composable
fun SectionRestore(
    onRestoreLocal: ()-> Unit
) {
    SectionCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.description_restore_settings),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(onClick = onRestoreLocal) {
                Text(stringResource(R.string.label_open_restore_local))
            }
        }
    }
}


@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable ()-> Unit
) {
    OutlinedCard (
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FrequencyDropdown(
    currentFrequency: BackupFrequency,
    onFrequencyChange: (BackupFrequency) -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = currentFrequency.getLabel(context),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            BackupFrequency.entries.forEach { frequency ->
                DropdownMenuItem(
                    text = { Text(frequency.getLabel(context)) },
                    onClick = {
                        onFrequencyChange(frequency)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BackupRestoreScreenPreview() {
    ExpenseTrackerTheme {
        BackupRestoreScreen(
            uiState = BackupRestoreActivityUIState(
                lastLocalBackupTime = LocalDateTime.of(2026,2,3,13,55,0),
                backupFrequency = BackupFrequency.DAILY
            ),
            onStartBackup = {},
            onRestoreLocal = {},
            onFrequencyChange = {}
        )
    }
}

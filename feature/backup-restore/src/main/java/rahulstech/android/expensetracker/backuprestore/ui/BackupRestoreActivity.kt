package rahulstech.android.expensetracker.backuprestore.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.backuprestore.Constants.BACKUP_FILE_MIME_TYPES
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.databinding.ActivityBackupRestoreBinding
import rahulstech.android.expensetracker.backuprestore.settings.AgentSettingsProvider
import rahulstech.android.expensetracker.backuprestore.settings.BackupFrequency
import rahulstech.android.expensetracker.backuprestore.util.DateTimeUtil
import rahulstech.android.expensetracker.backuprestore.util.FileEntry
import rahulstech.android.expensetracker.backuprestore.util.FileUtil
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper
import rahulstech.android.expensetracker.backuprestore.worker.ProgressData

typealias PendingTask = () -> Unit

class BackupRestoreActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BackupRestoreActivity"
    }

    private lateinit var binding: ActivityBackupRestoreBinding

    private val viewModel: BackupRestoreViewModel by viewModels()

    private lateinit var settings: AgentSettingsProvider

    private lateinit var permissionRequestLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>

    private lateinit var adapterBackupFrequency: BackupFrequencyAdapter

    private var pendingTask: PendingTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupRestoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        settings = AgentSettingsProvider.get(this)

        permissionRequestLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),this::onPermissionResult)

        openDocumentLauncher = registerForActivityResult(
            ActivityResultContracts.OpenDocument(),this::onPickBackupFile)

        updateLastLocalBackupTime()

        adapterBackupFrequency = BackupFrequencyAdapter()
        binding.autoBackupFrequency.apply {
            adapter = adapterBackupFrequency

            setSelection(adapterBackupFrequency.getPosition(settings.getBackupFrequency()))

            onItemSelectedListener = object: OnItemSelectedListener {
                override fun onItemSelected(view: AdapterView<*>?, itemView: View?, position: Int, id: Long) {
                    onBackupFrequencyUpdated(adapterBackupFrequency.getItem(position))
                }

                override fun onNothingSelected(view: AdapterView<*>?) {}
            }
        }

        binding.btnStartBackup.setOnClickListener { onClickStartBackup() }
        binding.btnCancelBackup.setOnClickListener { onClickCancelBackup() }
        binding.btnOpenRestoreLocal.setOnClickListener { onClickPickBackupFile() }

        viewModel.getLastLocalBackupTime().observe(this) { updateLastLocalBackupTime() }
        lifecycleScope.launch {
            viewModel.getBackupProgressFlow().collectLatest { updateBackupProgress(it) }
        }
        lifecycleScope.launch {
            viewModel.getRestoreProgressFlow().collectLatest { updateRestoreProgress(it) }
        }
    }




    // event handlers

    private fun onBackupFrequencyUpdated(newFrequency: BackupFrequency) {
        BackupRestoreHelper.rescheduleBackup(this, newFrequency)
        settings.setBackupFrequency(newFrequency)
    }

    private fun onClickStartBackup() {
        val permissions: Array<String> = when {
            Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            Build.VERSION.SDK_INT <= 28 -> arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            else -> emptyArray()
        }
        if (permissions.isNotEmpty()) {
            runIfPermissionGranted(permissions){startBackup()}
        }
        else {
            startBackup()
        }
    }

    private fun onClickCancelBackup() { cancelBackup() }

    private fun onClickPickBackupFile() {
        val permissions: Array<String> = when {
            Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            Build.VERSION.SDK_INT <= 28 -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            else -> emptyArray()
        }
        if (permissions.isNotEmpty()) {
            runIfPermissionGranted(permissions){showPickBackupFileInstruction()}
        }
        else {
            showPickBackupFileInstruction()
        }
    }

    private fun showPickBackupFileInstruction() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.message_pick_backup_file_instruction)
            .setPositiveButton(R.string.label_choose) { _,_ -> openDocumentLauncher.launch(arrayOf("application/*","text/*")) }
            .setNegativeButton(R.string.label_cancel, null)
            .create().show()
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
            .setPositiveButton(R.string.label_cancel,null)
            .show()
    }

    // backup and restore

    private fun startBackup() {
        BackupRestoreHelper.startBackup(applicationContext, settings.getBackupFrequency())
    }

    private fun cancelBackup() {
        BackupRestoreHelper.cancelBackup(applicationContext)
    }

    private fun startRestore(entry: FileEntry) {
        BackupRestoreHelper.startRestore(applicationContext, entry)
    }

    private fun updateLastLocalBackupTime() {
        val milli = settings.getLastLocalBackupMillis()
        val time = if (milli < 0) {
            getString(R.string.label_last_local_backup_time_never)
        }
        else {
            DateTimeUtil.formatLastLocalBackup(milli)
        }
        binding.labelLastLocalBackup.text = buildString {
            append(getString(R.string.label_last_local_backup_time))
            append(": ")
            append(time)
        }
    }

    private fun updateBackupProgress(data: ProgressData?) {
        if (data == null) {
            binding.groupBackupProgress.visibility = View.INVISIBLE
            binding.btnStartBackup.visibility = View.VISIBLE
        }
        else {
            binding.groupBackupProgress.visibility = View.VISIBLE
            binding.btnStartBackup.visibility = View.INVISIBLE
            binding.backupProgressMessage.text = data.message
            binding.backupProgressbar.apply {
                isIndeterminate = data.max < 0 || data.current < 0
                max = data.max
                progress = data.current
            }
        }
    }

    private fun updateRestoreProgress(data: ProgressData?) {
        if (data == null) {
            binding.groupRestoreProgress.visibility = View.INVISIBLE
            binding.btnOpenRestoreLocal.visibility = View.VISIBLE
        }
        else {
            binding.groupRestoreProgress.visibility = View.VISIBLE
            binding.btnOpenRestoreLocal.visibility = View.INVISIBLE
            binding.restoreProgressMessage.text = data.message
            binding.restoreProgressbar.apply {
                isIndeterminate = data.max < 0 || data.current < 0
                max = data.max
                progress = data.current
            }
        }
    }


    // runtime permission related methods

    private fun runIfPermissionGranted(permissions: Array<String>, task: PendingTask) {
        if (hasPermission(permissions)) {
            task()
        }
        else {
            pendingTask = task
            permissionRequestLauncher.launch(permissions)
        }
    }

    private fun hasPermission(permissions: Array<String>): Boolean =
        permissions.all{ permission ->
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, permission)
        }

    private fun onPermissionResult(grants: Map<String, Boolean>) {
        pendingTask?.let { task ->
            val notGranted = grants.entries
                .filter { !it.value }
                .map { it.key }
            if (notGranted.isEmpty()) {
                task.invoke()
            }
            else {
                runIfPermissionGranted(notGranted.toTypedArray()) { task.invoke() }
            }
        }
    }
}
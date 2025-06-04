package rahulstech.android.expensetracker.backuprestore.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.databinding.FragmentBackupRestoreLayoutBinding
import rahulstech.android.expensetracker.backuprestore.settings.AgentSettingsProvider
import rahulstech.android.expensetracker.backuprestore.settings.BackupFrequency
import rahulstech.android.expensetracker.backuprestore.util.DateTimeUtil
import rahulstech.android.expensetracker.backuprestore.util.FileEntry
import rahulstech.android.expensetracker.backuprestore.util.FileUtil
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper

class BackupRestoreFragment: Fragment() {
    companion object {
        private val TAG = BackupRestoreFragment::class.simpleName

        private val BACKUP_FILE_MIME_TYPES = arrayOf("application/json","application/gzip")
    }

    private lateinit var permissionRequestLauncher: ActivityResultLauncher<Array<String>>

    private var pendingPermissionResultCallback: PermissionResultCallback? = null

    private lateinit var binding: FragmentBackupRestoreLayoutBinding

    private lateinit var viewModel: BackupRestoreViewModel

    private lateinit var adapterBackupFrequency: BackupFrequencyAdapter

    private lateinit var settings: AgentSettingsProvider

    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>


    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[BackupRestoreViewModel::class.java]
        settings = AgentSettingsProvider.get(context)
        permissionRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result -> onPermissionResult(result) }
        openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { result -> onPickBackupFile(result) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBackupRestoreLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateLastLocalBackupTime()
        adapterBackupFrequency = BackupFrequencyAdapter()
        binding.autoBackupFrequency.apply {
            adapter = adapterBackupFrequency
            setSelection(adapterBackupFrequency.getPosition(
                settings.getBackupFrequency()
            ))
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

        viewModel.getLastLocalBackupTime().observe(viewLifecycleOwner) { updateLastLocalBackupTime() }
        lifecycleScope.launch {
            viewModel.getBackupProgressFlow().collectLatest { updateBackupProgress(it) }
        }
        lifecycleScope.launch {
            viewModel.getRestoreProgressFlow().collectLatest { updateRestoreProgress(it) }
        }
    }

    private fun onBackupFrequencyUpdated(newFrequency: BackupFrequency) {
        settings.setBackupFrequency(newFrequency)
        BackupRestoreHelper.rescheduleBackup(requireContext(), newFrequency)
    }

    private fun onClickStartBackup() {
        val callback = object: PermissionResultCallback {
            override fun onPermissionsGranted() { startBackup() }
            override fun onPermissionsNotGranted(permissions: Collection<String>) {
                // TODO: show permission rationale
            }
        }

        if (Build.VERSION.SDK_INT >= 33) {
            runIfPermissionGranted(Manifest.permission.POST_NOTIFICATIONS, callback)
        }
        else if (Build.VERSION.SDK_INT <= 28) {
            runIfPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE, callback)
        }
        else {
            callback.onPermissionsGranted()
        }
    }

    private fun onClickCancelBackup() {
        cancelBackup()
    }

    private fun onClickPickBackupFile() {
        val callback = object: PermissionResultCallback {
            override fun onPermissionsGranted() { showPickBackupFileInstruction() }
            override fun onPermissionsNotGranted(permissions: Collection<String>) {}
        }
        if (Build.VERSION.SDK_INT >= 33) {
            runIfPermissionGranted(Manifest.permission.POST_NOTIFICATIONS, callback)
        }
        else if (Build.VERSION.SDK_INT <= 28) {
            runIfPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE, callback)
        }
        else {
            callback.onPermissionsGranted()
        }
    }

    private fun onPickBackupFile(uri: Uri?) {
        if (null == uri) {
            Log.i(TAG, "no backup file picked; can not restore")
            showCancellableAlert(getString(R.string.message_no_backup_file_picked))
            return
        }
        val entry = FileUtil.getBackupFileDetails(requireContext(), uri)
        Log.i(TAG, "picked backup file for restore $entry")
        if (!entry.isOfType(BACKUP_FILE_MIME_TYPES)) {
            showCancellableAlert(getString(R.string.message_invalid_backup_file_type, entry.displayName))
            return
        }
        startRestore(entry)
    }

    private fun onPermissionResult(result: Map<String,Boolean>) {
        pendingPermissionResultCallback?.let { callback ->
            val granted = result.filter { e -> e.value }
            val notGranted = result.filter { e -> !e.value }
            if (granted.size == result.size) {
                callback.onPermissionsGranted()
            }
            else {
                callback.onPermissionsNotGranted(notGranted.keys)
            }
        }
        pendingPermissionResultCallback = null
    }

    private fun showPickBackupFileInstruction() {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(R.string.message_pick_backup_file_instruction)
            .setPositiveButton(R.string.label_choose) { _,_ -> openDocumentLauncher.launch(arrayOf("application/*","text/*")) }
            .setNegativeButton(R.string.label_cancel, null)
            .create().show()
    }

    private fun startBackup() {
        BackupRestoreHelper.startBackup(requireContext(), settings.getBackupFrequency())
    }

    private fun cancelBackup() {
        BackupRestoreHelper.cancelBackup(requireContext())
    }

    private fun startRestore(entry: FileEntry) {
        BackupRestoreHelper.startRestore(requireContext(), entry.uri, entry.mimeType, entry.displayName)
    }

    private fun updateBackupProgress(data: BackupRestoreHelper.ProgressData?) {
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

    private fun updateRestoreProgress(data: BackupRestoreHelper.ProgressData?) {
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

    private fun runIfPermissionGranted(permission: String, callback: PermissionResultCallback) {
        if (hasPermission(permission)) {
            callback.onPermissionsGranted()
        }
        else {
            requestPermission(permission, callback)
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(requireContext(), permission)
    }

    private fun requestPermission(permission: String, callback: PermissionResultCallback) {
        pendingPermissionResultCallback = callback
        permissionRequestLauncher.launch(arrayOf(permission))
    }

    private fun showCancellableAlert(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.label_cancel,null)
            .create().show()
    }

    private interface PermissionResultCallback {

        fun onPermissionsGranted()

        fun onPermissionsNotGranted(permissions: Collection<String>)
    }

    private class BackupFrequencyAdapter: BaseAdapter() {

        private val frequencies = BackupFrequency.entries.toTypedArray()

        override fun getCount(): Int = frequencies.size

        fun getPosition(frequency: BackupFrequency): Int = frequency.ordinal

        override fun getItem(position: Int): BackupFrequency = frequencies[position]

        override fun getItemId(position: Int): Long = frequencies[position].hashCode().toLong()

        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            val itemView: View
            val text1: TextView
            if (null == view) {
                itemView = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
                text1 = itemView.findViewById(android.R.id.text1)
                itemView.tag = text1
            }
            else {
                itemView = view
                text1 = view.tag as TextView
            }
            text1.text = getItem(position).getLabel(parent.context)
            return itemView
        }

        override fun getDropDownView(position: Int, view: View?, parent: ViewGroup): View {
            val itemView: View
            val text1: TextView
            if (null == view) {
                itemView = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_single_choice, parent, false)
                text1 = itemView.findViewById(android.R.id.text1)
                itemView.tag = text1
            }
            else {
                itemView = view
                text1 = view.tag as TextView
            }
            text1.text = getItem(position).getLabel(parent.context)
            return itemView
        }
    }
}
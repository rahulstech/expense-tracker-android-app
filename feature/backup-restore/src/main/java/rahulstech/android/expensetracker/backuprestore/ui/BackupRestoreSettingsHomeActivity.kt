package rahulstech.android.expensetracker.backuprestore.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.databinding.ActivitySettingsHomeLayoutBinding
import rahulstech.android.expensetracker.backuprestore.settings.AgentSettingsProvider
import rahulstech.android.expensetracker.backuprestore.settings.BackupFrequency
import rahulstech.android.expensetracker.backuprestore.util.DateTimeUtil
import rahulstech.android.expensetracker.backuprestore.util.FileEntry
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper

class BackupRestoreSettingsHomeActivity: AppCompatActivity() {

    private val TAG = BackupRestoreSettingsHomeActivity::class.simpleName

    private lateinit var viewModel: BackupRestoreSettingsViewModel
    private lateinit var binding: ActivitySettingsHomeLayoutBinding
    private lateinit var adapterBackupFrequency: BackupFrequencyAdapter
    private lateinit var agentSettings: AgentSettingsProvider

    private lateinit var requestLauncher: ActivityResultContract<String,Boolean>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsHomeLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        agentSettings = AgentSettingsProvider.get(this)

        setSupportActionBar(binding.actionBar.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        updateLastLocalBackupTime()
        adapterBackupFrequency = BackupFrequencyAdapter()
        binding.autoBackupFrequency.apply {
            adapter = adapterBackupFrequency
            setSelection(adapterBackupFrequency.getPosition(getCurrentAutoBackupFrequency()))
            onItemSelectedListener = object: OnItemSelectedListener {
                override fun onItemSelected(view: AdapterView<*>?, itemView: View?, position: Int, id: Long) {
                    updateAutoBackupFrequencyChange(adapterBackupFrequency.getItem(position))
                }

                override fun onNothingSelected(view: AdapterView<*>?) {}
            }
        }
        binding.btnStartBackup.setOnClickListener { startBackup() }
        binding.btnCancelBackup.setOnClickListener { cancelBackup() }
        binding.btnOpenRestoreLocal.setOnClickListener { openRestoreLocal() }

        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(application))[BackupRestoreSettingsViewModel::class.java]

        lifecycleScope.launch {
            BackupRestoreHelper.getBackupProgress(this@BackupRestoreSettingsHomeActivity).collectLatest { updateBackupProgress(it) }
        }
    }

    private fun checkPermissionsGrantedOrRequest(onGranted: ()->Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            // first check permissions granted
            if (PackageManager.PERMISSION_GRANTED
                != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // TODO: show permission rationale
                val requestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) { onGranted.invoke() }
                }
                requestLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            else {
                onGranted.invoke()
            }
        }
        else {
            onGranted.invoke()
        }
    }

    private fun startBackup() {
        checkPermissionsGrantedOrRequest {
            BackupRestoreHelper.startBackup(this, agentSettings.getBackupFrequency())
        }
    }

    private fun cancelBackup() {
        BackupRestoreHelper.cancelBackup(this)
    }

    private fun openRestoreLocal() {
//        lifecycleScope.launch {
//            viewModel.getBackupFiles().collectLatest { entries ->
//                var selectedEntry: FileEntry? = null
//                val adapter = RestoreFileChooserAdapter(entries)
//                val dialog = MaterialAlertDialogBuilder(requireContext())
//                    .setTitle(R.string.label_choose_restore_file)
//                    .setSingleChoiceItems(adapter, 0) { _, selection ->
//                        selectedEntry = adapter.getItem(selection)
//                        Log.i(TAG, "selected backup file $selectedEntry")
//                    }
//                    .setPositiveButton(R.string.label_start_restore) { _, _ ->
//                        selectedEntry?.let { entry ->
//                            BackupRestoreHelper.startRestore(this, entry.uri, entry.mimeType)
//                        }
//                    }
//                    .setNeutralButton(R.string.label_cancel, null)
//                    .create()
//                dialog.show()
//            }
//        }
    }

    private fun updateBackupProgress(data: BackupRestoreHelper.ProgressData?) {
        if (data == null) {
            binding.layoutBackupProgress.visibility = View.GONE
            binding.btnStartBackup.visibility = View.VISIBLE
        }
        else {
            binding.layoutBackupProgress.visibility = View.VISIBLE
            binding.btnStartBackup.visibility = View.GONE
            binding.backupProgressMessage.text = data.message
            binding.backupProgressbar.apply {
                isIndeterminate = data.max < 0 || data.current < 0
                max = data.max
                progress = data.current
            }
        }
    }

    private fun updateLastLocalBackupTime() {
        val millis = agentSettings.getLastLocalBackupMillis()
        val datetime = if (millis < 0) getString(R.string.label_last_local_backup_time_never)
        else DateTimeUtil.formatLastLocalBackup(millis)
        val label = getString(R.string.label_last_local_backup_time)
        val text = "$label: $datetime"
        binding.labelLastLocalBackup.text = text
    }

    private fun updateAutoBackupFrequencyChange(frequency: BackupFrequency) {
        agentSettings.setBackupFrequency(frequency)
        BackupRestoreHelper.rescheduleBackup(this, frequency)
    }

    private fun getCurrentAutoBackupFrequency(): BackupFrequency = agentSettings.getBackupFrequency()

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

    private class RestoreFileChooserAdapter(val entries: List<FileEntry>): BaseAdapter() {

        override fun getCount(): Int = entries.size

        override fun getItem(position: Int): FileEntry = entries[position]

        override fun getItemId(position: Int): Long = entries[position].hashCode().toLong()

        override fun getView(position: Int, view: View?, parent: ViewGroup): View {
            val entry = getItem(position)
            val itemView: View
            val text1: TextView
            val text2: TextView
            if (null == view) {
                val inflater = LayoutInflater.from(parent.context)
                itemView = inflater.inflate(android.R.layout.simple_list_item_activated_2,parent,false)
                text1 = itemView.findViewById(android.R.id.text1)
                text2 = itemView.findViewById(android.R.id.text2)
                itemView.tag = arrayOf(text1,text2)
            }
            else {
                itemView = view
                val tag = view.tag as Array<*>
                text1 = tag[0] as TextView
                text2 = tag[1] as TextView
            }
            text1.text = entry.displayName
            text2.text = DateTimeUtil.formatLastModified(entry.lastModifiedMillis)
            return itemView
        }
    }
}
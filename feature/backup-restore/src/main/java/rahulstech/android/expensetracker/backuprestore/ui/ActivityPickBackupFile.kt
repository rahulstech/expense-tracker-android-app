package rahulstech.android.expensetracker.backuprestore.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.databinding.LayoutPickBackupFileBinding
import rahulstech.android.expensetracker.backuprestore.util.DateTimeUtil
import rahulstech.android.expensetracker.backuprestore.util.FileEntry
import rahulstech.android.expensetracker.backuprestore.worker.BackupRestoreHelper

class ActivityPickBackupFile: AppCompatActivity() {

    companion object {
        private val TAG = ActivityPickBackupFile::class.simpleName
    }

    private lateinit var binding: LayoutPickBackupFileBinding
    private lateinit var viewModel: BackupRestoreSettingsViewModel

    private var pickedFile: FileEntry? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutPickBackupFileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.actionBar.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnRestore.setOnClickListener { pickedFile?.let { onBackupFileSelected(it) } }
        binding.btnCancel.setOnClickListener { finish() }

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[BackupRestoreSettingsViewModel::class]
        lifecycleScope.launch {
            viewModel.getBackupFiles().collectLatest { onBackupFilesLoaded(it) }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (!super.onSupportNavigateUp()) {
            super.onBackPressedDispatcher.onBackPressed()
            return true
        }
        return false
    }

    private fun onBackupFilesLoaded(entries: List<FileEntry>) {
        binding.progressbar.visibility = View.GONE

        if (entries.isEmpty()) {
            binding.message.text = getString(R.string.label_no_local_backup_file)
            return
        }

        val entry = entries.first()
        binding.message.setText(R.string.message_picked_backup_file)
        binding.messageResult.text = buildSpannedString {
            bold { append(entry.displayName) }
            appendLine()
            append(DateTimeUtil.formatLastModified(entry.lastModifiedMillis))
        }
        pickedFile = entry
        binding.groupResult.visibility = View.VISIBLE

    }

    private fun onBackupFileSelected(entry: FileEntry) {
        BackupRestoreHelper.startRestore(this, entry.uri, entry.mimeType, entry.displayName)
        finish()
    }
}
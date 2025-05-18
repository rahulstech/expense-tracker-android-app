package rahulstech.android.expensetracker.backuprestore.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import rahulstech.android.expensetracker.backuprestore.databinding.FragmentBackupRestoreBinding
import rahulstech.android.expensetracker.backuprestore.worker.Constants
import rahulstech.android.expensetracker.backuprestore.worker.backup.JsonBackupWorker

class BackupRestoreSettingsFragment: Fragment() {

    private var _binding: FragmentBackupRestoreBinding? = null
    private val binding: FragmentBackupRestoreBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackupRestoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.backup.setOnClickListener { startBackup() }
    }

    private fun startBackup() {
        val workManager = WorkManager.getInstance(requireContext().applicationContext)

        val jsonBackupWork = OneTimeWorkRequestBuilder<JsonBackupWorker>()
            .addTag(Constants.TAG_JSON_BACKUP_WORK)
            .build()

        workManager
            .beginUniqueWork(Constants.TAG_BACKUP_WORK, ExistingWorkPolicy.REPLACE, jsonBackupWork)
            .enqueue()
    }
}
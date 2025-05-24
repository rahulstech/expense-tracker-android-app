package rahulstech.android.expensetracker.backuprestore.util

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.receiver.WorkBroadcastReceiver
import rahulstech.android.expensetracker.backuprestore.worker.Constants

object NotificationConstants {
    const val BACKUP_NOTIFICATION_ID = 55
    const val RESTORE_NOTIFICATION_ID = 56
    const val NOTIFICATION_CHANNEL_ID_BACKUP_RESTORE = "rahulstech.android.expensetracker.notificationchannel.BACKUP_RESTORE"
    const val NOTIFICATION_CHANNEL_NAME_BACKUP_RESTORE = "Backup and Restore"
    const val NOTIFICATION_CHANNEL_DESCRIPTION_BACKUP_RESTORE = ""
}

class NotificationBuilder {
    private var _title: CharSequence? = null
    private var _titleRes: Int? = null
    private var _message: CharSequence? = null
    private var _messageRes: Int? = null
    private var _actionContent: PendingIntent? = null
    private var _actionPosition: NotificationCompat.Action? = null
    private var _actionNegative: NotificationCompat.Action? = null
    private var _actionNeutral: NotificationCompat.Action? = null
    private var _showProgress: Boolean = false
    private var _progressMax: Int = -1;
    private var _progressCurrent: Int = -1
    private var _smallIconRes: Int? = null

    fun setTitle(title: CharSequence) {
        _title = title
    }

    fun setTitleResource(@StringRes resId: Int) {
        _titleRes = resId
    }

    fun setMessage(message: CharSequence) {
        _message = message
    }

    fun setMessageResource(@StringRes resId: Int) {
        _messageRes = resId
    }

    fun setPositionAction(action: NotificationCompat.Action) {
        _actionPosition = action
    }

    fun setNegativeAction(action: NotificationCompat.Action) {
        _actionNegative = action
    }

    fun setNeutralAction(action: NotificationCompat.Action) {
        _actionNeutral = action
    }

    fun setProgress(current: Int, max: Int) {
        _progressCurrent = current
        _progressMax = max
        _showProgress = true
    }

    fun setSmallIconResource(@DrawableRes resId: Int) {
        _smallIconRes = resId
    }

    fun setContentAction(action: PendingIntent) {
        _actionContent = action
    }

    fun create(context: Context, channelId: String): Notification {
        val titleText = _title ?: _titleRes?.let { context.getText(it) } ?: ""
        val messageText = _message ?: _messageRes?.let { context.getText(it) } ?: ""
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(titleText)
            .setContentText(messageText)
            .setSmallIcon(IconCompat.createWithResource(context.applicationContext, _smallIconRes ?: R.drawable.database) )
        if (_showProgress) {
            val indeterminate = _progressCurrent !in 0.._progressMax
            builder.setProgress(_progressMax, _progressCurrent, indeterminate)
        }
        _actionContent?.let { action -> builder.setContentIntent(action) }
        _actionNeutral?.let { action -> builder.addAction(action) }
        _actionNegative?.let { action -> builder.addAction(action) }
        _actionPosition?.let { action -> builder.addAction(action) }
        return builder.build()
    }
}

class NotificationActionBuilder(val action: PendingIntent, val actionLabel: String) {
    fun create(context: Context): NotificationCompat.Action {
        val builder = NotificationCompat.Action.Builder(null, actionLabel, action)
        return builder.build()
    }
}

fun createNotificationChannel(context: Context, channelId: String) {
    val manager = NotificationManagerCompat.from(context.applicationContext)
    var channel = manager.getNotificationChannelCompat(channelId)
    if (null == channel) {
        channel = NotificationChannelCompat.Builder(channelId,NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setDescription(NotificationConstants.NOTIFICATION_CHANNEL_DESCRIPTION_BACKUP_RESTORE)
            .setName(NotificationConstants.NOTIFICATION_CHANNEL_NAME_BACKUP_RESTORE)
            .setShowBadge(false)
            .build()
        manager.createNotificationChannel(channel)
    }
}

fun createBackupNotification(context: Context, builder: NotificationBuilder): Notification {
    val appContext = context.applicationContext
    builder.apply {
        setTitleResource(R.string.notification_title_backup)
        setSmallIconResource(R.drawable.sd_card)

        val flags = PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        val intentCancelWork = Intent(appContext, WorkBroadcastReceiver::class.java).apply {
            setAction(WorkBroadcastReceiver.ACTION_CANCEL_WORK)
            putExtra(WorkBroadcastReceiver.EXTRA_WORK_NAME, Constants.TAG_BACKUP_WORK)
        }
        val actionCancelBackup = PendingIntent.getBroadcast(
            context.applicationContext,
            Constants.REQUEST_CANCEL_BACKUP,
            intentCancelWork,
            flags)
        setNeutralAction(NotificationActionBuilder(actionCancelBackup,appContext.getString(R.string.label_cancel)).create(appContext))
    }
    createNotificationChannel(context, NotificationConstants.NOTIFICATION_CHANNEL_ID_BACKUP_RESTORE)
    return builder.create(appContext, NotificationConstants.NOTIFICATION_CHANNEL_ID_BACKUP_RESTORE)
}

fun createRestoreNotification(context: Context, builder: NotificationBuilder): Notification {
    val appContext = context.applicationContext
    builder.apply {
        setTitleResource(R.string.notification_title_restore)
        setSmallIconResource(R.drawable.arrow_circle_down)
//        val actionContent = PendingIntent.getActivity(appContext,
//            Constants.REQUEST_SHOW_RESTORE_ACTIVITY,
//            Intent(appContext, BackupRestoreSettingsHomeActivity::class.java),
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
//        setContentAction(actionContent)
    }
    createNotificationChannel(appContext,NotificationConstants.NOTIFICATION_CHANNEL_ID_BACKUP_RESTORE)
    return builder.create(appContext, NotificationConstants.NOTIFICATION_CHANNEL_ID_BACKUP_RESTORE)
}

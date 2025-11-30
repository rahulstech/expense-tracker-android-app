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
import androidx.work.Data
import androidx.work.workDataOf
import rahulstech.android.expensetracker.backuprestore.Constants
import rahulstech.android.expensetracker.backuprestore.R
import rahulstech.android.expensetracker.backuprestore.receiver.WorkBroadcastReceiver


data class Progress(
    val current: Int,
    val max: Int,
    val message: String?,
) {
    companion object {

        fun unbounded(current: Int, message: String? = null): Progress = Progress(current,-1,message)

        fun infinite( message: String? = null): Progress = Progress(-1,-1,message)
    }

    val isInFinite: Boolean get() = current < 0 && max < 0

    val isUnbounded: Boolean get() = current >= 0 && max < 0

    fun toWorkData(): Data = workDataOf(
        Constants.DATA_PROGRESS_MESSAGE to message,
        Constants.DATA_PROGRESS_CURRENT to current,
        Constants.DATA_PROGRESS_MAX to max
    )
}

object NotificationConstants {
    const val BACKUP_NOTIFICATION_ID = 55
    const val RESTORE_NOTIFICATION_ID = 56
    const val NOTIFICATION_CHANNEL_ID_BACKUP_RESTORE = "rahulstech.android.expensetracker.notificationchannel.BACKUP_RESTORE"
    const val NOTIFICATION_CHANNEL_NAME_BACKUP_RESTORE = "Backup and Restore"
    const val NOTIFICATION_CHANNEL_DESCRIPTION_BACKUP_RESTORE = "App backup and restore notifications"
}

class NotificationBuilder(private val context: Context) {
    var title: CharSequence? = null
    var message: CharSequence? = null
    var actionPosition: NotificationCompat.Action? = null
    var actionNegative: NotificationCompat.Action? = null
    var actionNeutral: NotificationCompat.Action? = null
    var progress: Progress? = null
    private val showProgress: Boolean get() = null != progress
    private var _smallIconRes: Int? = null

    fun setTitleResource(@StringRes resId: Int) {
        title = context.getText(resId)
    }

    fun setMessageResource(@StringRes resId: Int) {
        message = context.getText(resId)
    }

    fun setProgress(current: Int, max: Int, message: String? = null) {
        progress = Progress(current,max,message)
    }

    fun setSmallIconResource(@DrawableRes resId: Int) {
        _smallIconRes = resId
    }

    fun create(channelId: String): Notification {
        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(IconCompat.createWithResource(context.applicationContext, _smallIconRes ?: R.drawable.database) )
        if (showProgress && null != progress) {
            val progress = this.progress!!
            builder.setProgress(progress.max, progress.current, progress.isInFinite)
        }
        actionNeutral?.let { action -> builder.addAction(action) }
        actionNegative?.let { action -> builder.addAction(action) }
        actionPosition?.let { action -> builder.addAction(action) }
        return builder.build()
    }
}

class NotificationActionBuilder(
    private val context: Context,
    val action: PendingIntent,
    val actionLabel: String) {

    fun create(): NotificationCompat.Action {
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
        actionNeutral = NotificationActionBuilder(context,actionCancelBackup,appContext.getString(R.string.label_cancel)).create()
    }
    createNotificationChannel(context, NotificationConstants.NOTIFICATION_CHANNEL_ID_BACKUP_RESTORE)
    return builder.create(NotificationConstants.NOTIFICATION_CHANNEL_ID_BACKUP_RESTORE)
}

fun createRestoreNotification(context: Context, builder: NotificationBuilder): Notification {
    val appContext = context.applicationContext
    builder.apply {
        setTitleResource(R.string.notification_title_restore)
        setSmallIconResource(R.drawable.arrow_circle_down)
    }
    createNotificationChannel(appContext,NotificationConstants.NOTIFICATION_CHANNEL_ID_BACKUP_RESTORE)
    return builder.create(NotificationConstants.NOTIFICATION_CHANNEL_ID_BACKUP_RESTORE)
}

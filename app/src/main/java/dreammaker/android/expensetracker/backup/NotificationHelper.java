package dreammaker.android.expensetracker.backup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import dreammaker.android.expensetracker.R;

public class NotificationHelper implements Handler.Callback {

    private static final String BACKUP_RESTORE_CHANNEL_ID = "Backup And Restore";
    private static final String BACKUP_RESTORE_CHANNEL_DESCRIPTION = "Notifications related to backup and restore service";

    public static class NotificationArgs {
        public static final int NOTIFICATION_TYPE_SIMPLE = 1;
        public static final int NOTIFICATION_TYPE_DETERMINATE_PROGRESS = 2;
        public static final int NOTIFICATION_TYPE_INDETERMINATE_PROGRESS = 3;

        private int notificationId;
        private int notificationType;
        private int progressMax;
        private int progressCurrent;
        private String notificationTitle;
        private String notificationMessage;
        private NotificationCompat.Action action;
        private PendingIntent contentIntent;
        private boolean autoCancel = false;

        @Deprecated
        public NotificationArgs(int notificationType, int notificationId) {
            this.notificationType = notificationType;
            this.notificationId = notificationId;
        }

        public NotificationArgs(int notificationType) {
            this.notificationType = notificationType;
        }

        public NotificationArgs setProgressMax(int max) {
            this.progressMax = max;
            return this;
        }

        public NotificationArgs setProgressCurrent(int current) {
            this.progressCurrent = current;
            return this;
        }

        public NotificationArgs setNotificationTitle(String title) {
            this.notificationTitle = title;
            return this;
        }

        public NotificationArgs setNotificationMessage(String message) {
            this.notificationMessage = message;
            return this;
        }

        public NotificationArgs addAction(@NonNull NotificationCompat.Action action) {
            this.action = action;
            return this;
        }

        public NotificationArgs setContentIntent(@NonNull PendingIntent pi) {
            this.contentIntent = pi;
            return this;
        }

        public NotificationArgs setAutoCancel(boolean autoCancel) {
            this.autoCancel = autoCancel;
            return this;
        }
    }

    private Context appContext;
    private NotificationManagerCompat notificationManager;
    private Handler handler;

    public NotificationHelper(@NonNull Context context) {
        if (null == context) {
            throw new NullPointerException("content == null");
        }
        this.appContext = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(context);
        this.handler = new Handler(Looper.getMainLooper(),this);
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        if (1 == msg.what) {
            NotificationManagerCompat notificationManager = this.notificationManager;
            NotificationArgs args = (NotificationArgs) msg.obj;
            int notificationId = args.notificationId;
            Notification notification = newNotification(args);
            notificationManager.notify(notificationId,notification);
            return true;
        }
        return false;
    }

    public NotificationCompat.Action createAction(@StringRes int resId, @Nullable PendingIntent pi) {
        return new NotificationCompat.Action(null, getResourceString(resId),pi);
    }

    public Context getAppContext() { return appContext; }

    public void showNotification(@NonNull NotificationArgs args) {
        handler.obtainMessage(1,args)
                .sendToTarget();
    }

    public void cancelNotification() {
        handler.removeMessages(1);
    }

    @NonNull
    public Notification newNotification(@NonNull NotificationArgs args) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) setupNotificationChannel();
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(appContext, BACKUP_RESTORE_CHANNEL_ID);
        nBuilder.setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setSmallIcon(R.drawable.backup_restore)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(args.notificationTitle)
                .setContentText(args.notificationMessage);
        if (null != args.action) nBuilder.addAction(args.action);
        if (null != args.contentIntent) nBuilder.setContentIntent(args.contentIntent);
        int notificationType = args.notificationType;
        if (NotificationArgs.NOTIFICATION_TYPE_DETERMINATE_PROGRESS == notificationType) {
            nBuilder.setProgress(args.progressMax,args.progressCurrent,false);
        }
        else if (NotificationArgs.NOTIFICATION_TYPE_INDETERMINATE_PROGRESS == notificationType) {
            nBuilder.setProgress(0,0,true);
        }
        return nBuilder.build();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void setupNotificationChannel() {
        if (null == notificationManager.getNotificationChannel(BACKUP_RESTORE_CHANNEL_ID)) {
            NotificationChannel channel = new NotificationChannel(BACKUP_RESTORE_CHANNEL_ID,
                    BACKUP_RESTORE_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(BACKUP_RESTORE_CHANNEL_DESCRIPTION);
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String getResourceString(@StringRes int resId) {
        return appContext.getString(resId);
    }
}

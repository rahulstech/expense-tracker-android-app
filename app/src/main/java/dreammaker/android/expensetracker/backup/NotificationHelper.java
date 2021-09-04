package dreammaker.android.expensetracker.backup;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.R;

public class NotificationHelper  {

    public static final String MISCELLANEOUS_CHANNEL_ID = "Others";
    public static final String MISCELLANEOUS_CHANNEL_DESCRIPTION = "Notifications for general purpose";
    public static final String BACKUP_RESTORE_CHANNEL_ID = "Backup And Restore";
    public static final String BACKUP_RESTORE_CHANNEL_DESCRIPTION = "Notifications related to backup and restore service";

    public static class NotificationArgs {
        public static final int NOTIFICATION_TYPE_SIMPLE = 1;
        public static final int NOTIFICATION_TYPE_DETERMINATE_PROGRESS = 2;
        public static final int NOTIFICATION_TYPE_INDETERMINATE_PROGRESS = 3;

        private int notificationType;
        private int progressMax;
        private int progressCurrent;
        private int smallIconRes = R.drawable.ic_launcher;
        private String notificationTitle;
        private String notificationMessage;
        private NotificationCompat.Action action;
        private PendingIntent contentIntent;
        private boolean autoCancel = false;
        private long autoCancelDuration = 60000; // default 1 minute

        public NotificationArgs(int notificationType) {
            this.notificationType = notificationType;
        }

        public NotificationArgs setSmallIcon(@DrawableRes int smallIconRes) {
            this.smallIconRes = smallIconRes;
            return this;
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

        public NotificationArgs setAutoCancel(boolean autoCancel, long autoCancelDuration) {
            this.autoCancel = autoCancel;
            this.autoCancelDuration = Math.max(60000, autoCancelDuration); // auto cancel notification minimum after 1 minute
            return this;
        }
    }

    private Context appContext;
    private NotificationManagerCompat notificationManager;

    public NotificationHelper(@NonNull Context context) {
        if (null == context) {
            throw new NullPointerException("content == null");
        }
        this.appContext = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(context);
    }

    public NotificationCompat.Action createAction(@StringRes int resId, @Nullable PendingIntent pi) {
        return new NotificationCompat.Action(null, appContext.getString(resId),pi);
    }

    public Context getAppContext() { return appContext; }

    @NonNull
    public Notification newNotification(String channelID,  @NonNull NotificationArgs args) {
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(appContext,channelID);
        nBuilder.setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setSmallIcon(args.smallIconRes)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(args.notificationTitle)
                .setContentText(args.notificationMessage)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setBigContentTitle(args.notificationTitle)
                        .bigText(args.notificationMessage));
        if (args.autoCancel) {
            nBuilder.setAutoCancel(args.autoCancel)
                    .setTimeoutAfter(args.autoCancelDuration);
        }
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

    public void createNotificationChannel(String id, String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (null == notificationManager.getNotificationChannel(id)) {
                NotificationChannel channel = new NotificationChannel(id,
                        id, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription(description);
                channel.setShowBadge(false);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public void updateNotification(int nid, @NonNull Notification updated) {
        notificationManager.notify(nid,updated);
    }

    public void removeNotification(int nid) {
        notificationManager.cancel(nid);
    }
}

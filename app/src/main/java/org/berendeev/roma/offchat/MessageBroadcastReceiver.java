package org.berendeev.roma.offchat;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.berendeev.roma.offchat.presentation.App;
import org.berendeev.roma.offchat.service.presentation.MainService;

import static android.content.Context.NOTIFICATION_SERVICE;


public class MessageBroadcastReceiver extends BroadcastReceiver {

    private static int id = 0;

    private static final int SERVICE_NOTIFICATION_ID = 43;

    private NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

    private int pendingPushes = 0;

    public MessageBroadcastReceiver() {

    }

    @Override public void onReceive(Context context, Intent intent) {
        if (!App.isMainActivityStarted()){
            Log.d("myTag", "on foreground");
            String message = intent.getStringExtra(MainService.MESSAGE);
            showNotification(message, context);
        }
    }

    private void showNotification(String message, Context context) {

        pendingPushes++;
        style.setBigContentTitle(pendingPushes +" new pushes");
        style.addLine(message + "1");
        style.addLine(message + "2");
        style.setSummaryText("summary");
//        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//        inboxStyle.setBigContentTitle("Enter Content Text");
//        inboxStyle.addLine("hi events " + message);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("Message")
                .setContentText(message)
                .setGroupSummary(true)
                .setNumber(pendingPushes)
                .setStyle(style)
                .setGroup("myPushes")
//                .setStyle(inboxStyle)
                .setSmallIcon(R.mipmap.ic_launcher);
//        notification.flags |= Notification.FLAG_NO_CLEAR;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);
    }
}

package org.berendeev.roma.offchat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.berendeev.roma.offchat.domain.ChatRepository;
import org.berendeev.roma.offchat.domain.model.Message;
import org.berendeev.roma.offchat.presentation.App;
import org.berendeev.roma.offchat.presentation.MainActivity;
import org.berendeev.roma.offchat.presentation.R;
import org.berendeev.roma.offchat.service.presentation.MainService;

import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;


public class MessageBroadcastReceiver extends BroadcastReceiver {


    private static final int SERVICE_NOTIFICATION_ID = 43;

    private NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

    private final ChatRepository chatRepository;

    public MessageBroadcastReceiver() {
        chatRepository = App.getChatComponent().chatRepository();
    }

    @Override public void onReceive(Context context, Intent intent) {
        String text = intent.getStringExtra(MainService.MESSAGE);

        chatRepository
                .newIncomeMessage(text)
                .subscribe();
        if (!App.isMainActivityStarted()){
            Log.d("myTag", "on foreground");
//            Message message = Message.create(-1, System.currentTimeMillis(), not)
            chatRepository
                    .getUnreadMessages()
                    .subscribe((messages, throwable) -> {
                        if (throwable == null){
                            showNotification(messages, context);
                        }
                    });
//            showNotification(chatRepository.getUnreadMessages(), context);
        }
    }

    private void showNotification(List<Message> messages, Context context) {

        int pendingPushes = messages.size();
        style.setBigContentTitle(pendingPushes +" new pushes");



        for (Message message : messages) {
            style.addLine(message.text());
        }
//        style.setSummaryText("summary");

//        context.getString(R.string.new_messages_notification_title);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.new_messages_notification_title))
                .setContentText("content text")
                .setGroupSummary(true)
                .setNumber(pendingPushes)
                .setStyle(style)
                .setContentIntent(pendingIntent)
//                .addAction(R.drawable.ic_reply, context.getString(R.string.app_name), pendingIntent)
                .setGroup("myPushes")
                .setAutoCancel(true)
//                .setStyle(inboxStyle)
                .setSmallIcon(R.mipmap.ic_launcher);

//        notification.flags |= Notification.FLAG_NO_CLEAR;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);
    }
}

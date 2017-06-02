package org.berendeev.roma.offchat.data;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.berendeev.roma.offchat.domain.ChatRepository;
import org.berendeev.roma.offchat.domain.SettingsRepository;
import org.berendeev.roma.offchat.domain.model.Message;
import org.berendeev.roma.offchat.presentation.App;
import org.berendeev.roma.offchat.presentation.MainActivity;
import org.berendeev.roma.offchat.presentation.R;
import org.berendeev.roma.offchat.service.presentation.MainService;

import java.util.List;

import javax.inject.Inject;

import static android.content.Context.NOTIFICATION_SERVICE;


public class MessageBroadcastReceiver extends BroadcastReceiver {

    private SettingsRepository settingsRepository;

    private static final int SERVICE_NOTIFICATION_ID = 43;

    private NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

    private final ChatRepository chatRepository;

    public MessageBroadcastReceiver() {
        chatRepository = App.getChatComponent().chatRepository();
        settingsRepository = App.getChatComponent().settingsRepository();
    }

    @Override public void onReceive(Context context, Intent intent) {
        String text = intent.getStringExtra(MainService.MESSAGE);

        chatRepository
                .newIncomeMessage(text)
                .subscribe();
        if (!App.isMainActivityStarted() && settingsRepository.isShowNotifications()){
            Log.d("myTag", "on foreground");
            chatRepository
                    .getUnreadMessages()
                    .subscribe((messages, throwable) -> {
                        if (throwable == null){
                            showNotification(messages, context);
                        }
                    });
        }
    }

    private void showNotification(List<Message> messages, Context context) {

        int pendingPushes = messages.size();
        style.setBigContentTitle(context.getString(R.string.new_messages, pendingPushes));

        for (Message message : messages) {
            style.addLine(message.text());
        }

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.new_messages_notification_title))
                .setContentText(messages.get(0).text())
                .setGroupSummary(true)
                .setNumber(pendingPushes)
                .setStyle(style)
                .setContentIntent(pendingIntent)
                .setGroup("OffChat")
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);
    }
}

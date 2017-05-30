package org.berendeev.roma.offchat.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import org.berendeev.roma.offchat.service.BotService;
import org.berendeev.roma.offchat.service.presentation.MainService;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.berendeev.roma.offchat.service.BotService.START;
import static org.berendeev.roma.offchat.service.BotService.STOP;

public class MainActivity extends AppCompatActivity {

    private static final int SERVICE_NOTIFICATION_ID = 42;
    @BindView(R.id.start) Button start;
    @BindView(R.id.stop) Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        start.setOnClickListener(v -> {
            startService(getStartIntent());
        });
        stop.setOnClickListener(v -> {
            startService(getStopIntent());
        });
        showNotification("hello " + System.currentTimeMillis());
    }

    @Override protected void onStop() {
        super.onStop();
//        Toast.makeText(this, "Oops", Toast.LENGTH_LONG).show();
    }

    private Intent getStartIntent(){
        Intent intent = new Intent(this, MainService.class);
        intent.putExtra(MainService.COMMAND, START);
        return intent;
    }

    private Intent getStopIntent(){
        Intent intent = new Intent(this, MainService.class);
        intent.putExtra(MainService.COMMAND, STOP);
        return intent;
    }

    private void showNotification(String message) {

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        style.setBigContentTitle(2 +" new pushes");
        style.addLine(message + "1");
        style.addLine(message + "2");
        style.setSummaryText("summary");
//        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//        inboxStyle.setBigContentTitle("Enter Content Text");
//        inboxStyle.addLine("hi events " + message);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Message")
                .setContentText(message)
                .setGroupSummary(true)
                .setNumber(2)
                .setStyle(style)
                .addAction(R.drawable.ic_reply, "Reply", pIntent)
                .setGroup("myPushes")
//                .setStyle(inboxStyle)
                .setSmallIcon(org.berendeev.roma.offchat.R.mipmap.ic_launcher);

//        notification.flags |= Notification.FLAG_NO_CLEAR;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);
    }
}

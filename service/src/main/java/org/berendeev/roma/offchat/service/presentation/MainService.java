package org.berendeev.roma.offchat.service.presentation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.berendeev.roma.offchat.R;


public class MainService extends Service {

//    public static final String BOT_BROADCAST = "org.berendeev.roma.offchat.service.BOT_BROADCAST";

    private static final String LOG_TAG = "myTag";
    public static final String COMMAND = "command";
    public static final int START = 1;
    public static final int STOP = 2;
    private static final int SERVICE_NOTIFICATION_ID = 42;
    public static final String MESSAGE = "data";
    private ToastThread thread;
    private NotificationManager notificationManager;

    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra(COMMAND, 0);

        if (command == START) {
            if (thread == null) {
                thread = new ToastThread();
                thread.start();
                startForeground(SERVICE_NOTIFICATION_ID, getNotification());
            } else {
//                showToast("already started");
            }
        } else if (command == STOP) {
            if (thread != null) {
                thread.finish();
            }
            clearNotification();
        }

        return START_STICKY;
    }

    private void showToast(@Nullable String message) {
//        Toast.makeText(getApplicationContext(), "service works", Toast.LENGTH_SHORT).show();
        Handler h = new Handler(getApplicationContext().getMainLooper());

        h.post(new Runnable() {
            @Override
            public void run() {
                if (message == null) {
                    Toast.makeText(getApplicationContext(), "service works", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void sendBroadcast() {
        Intent intent = new Intent();
        intent.setAction(getResources().getString(R.string.message_action));
        intent.putExtra(MESSAGE, "Notice me senpai! " + System.currentTimeMillis());
        sendBroadcast(intent);
    }

    @Nullable @Override public IBinder onBind(Intent intent) {
        return null;
    }

    private class ToastThread extends Thread {

        private boolean finish = false;

        public void finish() {
            finish = true;
        }

        @Override public void run() {
            Looper.prepare();
            while (!finish) {
//                showToast(null);
                sendBroadcast();
//                showToast(null);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            showToast("stopped");
            stopSelf();
        }
    }

    @Override public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    private Notification getNotification() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Bot started")
                .setContentText("Message bot works")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();

//        notification.flags |= Notification.FLAG_NO_CLEAR;
//        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);
        return notification;
    }

    private void clearNotification() {
        notificationManager.cancel(SERVICE_NOTIFICATION_ID);
    }
}

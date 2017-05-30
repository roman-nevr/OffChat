package org.berendeev.roma.offchat.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.berendeev.roma.offchat.R;
import org.berendeev.roma.offchat.app.MainActivity;


public class BotService extends Service {

    private static final String LOG_TAG = "myTag";
    public static final String COMMAND = "command";
    public static final int START = 1;
    public static final int STOP = 2;
    private static final int SERVICE_NOTIFICATION_ID = 42;
    private ToastThread thread;
    private NotificationManager notificationManager;

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "MyService onCreate");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onCommand");
        int command = intent.getIntExtra(COMMAND, 0);

        if (command == START){
            if (thread == null){
                thread = new ToastThread();
                thread.start();
                startForeground(SERVICE_NOTIFICATION_ID, getNotification());
            }else {
                showToast("already started");
            }
        }else if (command == STOP){
            if (thread != null){
                thread.finish();
            }
            clearNotification();
        }

        return START_STICKY;
    }

    private void showToast(@Nullable String message){
//        Toast.makeText(getApplicationContext(), "service works", Toast.LENGTH_SHORT).show();
        Handler h = new Handler(getApplicationContext().getMainLooper());

        h.post(new Runnable() {
            @Override
            public void run() {
                if (message == null){
                    Toast.makeText(getApplicationContext(), "service works", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Nullable @Override public IBinder onBind(Intent intent) {
        return null;
    }

    private class ToastThread extends Thread{

        private boolean finish = false;

        public void finish(){
            finish = true;
        }

        @Override public void run() {
            Looper.prepare();
            while (!finish) {
                showToast(null);
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
        Log.d(LOG_TAG, "destroyed");
        super.onDestroy();
    }

    private Notification getNotification() {
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Service started")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(getPendingIntent())
                .build();

//        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
//        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);
    }

    private PendingIntent getPendingIntent() {
        Intent intent = getActivityIntent();
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void clearNotification(){
        notificationManager.cancel(SERVICE_NOTIFICATION_ID);
    }

    private Intent getActivityIntent(){
        return new Intent(this, MainActivity.class);
    }

    private void startMyActivity(){
//        Intent intent = new Intent(TimerService.this, AlarmListActivity.class);
//        intent.setAction(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_LAUNCHER);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
    }
}

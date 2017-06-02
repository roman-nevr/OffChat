package org.berendeev.roma.offchat.service.presentation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static org.berendeev.roma.offchat.service.presentation.MainService.START;


public class StartReceiver extends BroadcastReceiver {

    private Context context;
    private final String BOOT_ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override public void onReceive(Context context, Intent intent) {
        this.context = context;
        String action = intent.getAction();
        if (action.equalsIgnoreCase(BOOT_ACTION)) {
            Intent serviceIntent = new Intent(context, MainService.class);
            serviceIntent.putExtra(MainService.COMMAND, START);
            context.startService(serviceIntent);
        }
    }
}

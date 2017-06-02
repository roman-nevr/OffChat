package org.berendeev.roma.offchat.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.berendeev.roma.offchat.domain.SettingsRepository;


public class SettingsRepositoryImpl implements SettingsRepository {

    public static final String SETTINGS = "settings";
    public static final String SHOW_NOTIFICATIONS = "show_notifications";

    private Context context;
    private SharedPreferences settingsPreferences;

    public SettingsRepositoryImpl(Context context) {
        this.context = context;
        settingsPreferences = context.getSharedPreferences(SETTINGS, Context.MODE_PRIVATE);
    }


    @Override public void saveShowNotifications(boolean show) {
        settingsPreferences.edit()
                .putBoolean(SHOW_NOTIFICATIONS, show)
                .apply();
    }

    @Override public boolean isShowNotifications() {
        return settingsPreferences.getBoolean(SHOW_NOTIFICATIONS, true);
    }
}

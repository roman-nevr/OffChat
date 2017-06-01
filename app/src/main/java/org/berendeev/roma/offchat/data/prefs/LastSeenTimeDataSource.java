package org.berendeev.roma.offchat.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class LastSeenTimeDataSource {
    public static final String TIME_STAMP = "time_stamp";
    public static final String TIME = "time";
    private Context context;
    private SharedPreferences timePreferences;

    public LastSeenTimeDataSource(Context context) {
        this.context = context;
        timePreferences = context.getSharedPreferences(TIME_STAMP, Context.MODE_PRIVATE);
    }

    public void saveLastSeenTime(long time){
        timePreferences.edit()
                .putLong(TIME, time)
                .apply();
    }
    public long getLastSeenTime(){
        return timePreferences.getLong(TIME, 0);
    }
}

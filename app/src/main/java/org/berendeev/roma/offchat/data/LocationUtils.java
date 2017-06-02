package org.berendeev.roma.offchat.data;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import static org.berendeev.roma.offchat.domain.LocationRepository.LOCATION_PERMISSION_REQUEST_ID;
import static org.berendeev.roma.offchat.domain.LocationRepository.RESOLUTION_REQUEST_ID;

public class LocationUtils {
    public static void requestResolution(Activity activity, PendingIntent pendingIntent) throws IntentSender.SendIntentException {
        if(pendingIntent != null) {
            ActivityCompat.startIntentSenderForResult(activity, pendingIntent.getIntentSender(), RESOLUTION_REQUEST_ID, null, 0, 0, 0, new Bundle());
        }
    }

    public static void requestLocationPermissions(Fragment fragment) {
        fragment.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_ID);
    }

    public static void requestLocationPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_ID);
    }

    public static void requestResolution(Fragment fragment, PendingIntent pendingIntent) throws IntentSender.SendIntentException {
        if(pendingIntent != null) {
            fragment.startIntentSenderForResult(pendingIntent.getIntentSender(), RESOLUTION_REQUEST_ID, null, 0, 0, 0, new Bundle());
        }
    }

    public static boolean isPermissionsGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}

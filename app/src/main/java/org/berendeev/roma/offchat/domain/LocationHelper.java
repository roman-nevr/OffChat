package org.berendeev.roma.offchat.domain;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;

import org.berendeev.roma.offchat.domain.model.LocationState;

import io.reactivex.Observable;

public abstract class LocationHelper {
    String PENDING_INTENT = "pending_intent";

    public static final int REQUEST_CHECK_SETTINGS = 42;
    public static final int LOCATION_PERMISSION_REQUEST_ID = 43;
    public static final int RESOLUTION_REQUEST_ID = 44;

    public abstract void disconnect();

//    void onRequestResult(int requestCode, int resultCode);

//    Observable<Location> getLocationObservable();

    public abstract Observable<LocationState> getLocationStateObservable();

    public abstract void requestLocation(LocationCallbacks callbacks);

    public abstract void onActivityResult(int requestCode, int resultCode);

    public abstract void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);

    public static void requestResolution(Activity activity, PendingIntent pendingIntent) throws IntentSender.SendIntentException {
        if(pendingIntent != null) {
            ActivityCompat.startIntentSenderForResult(activity, pendingIntent.getIntentSender(), RESOLUTION_REQUEST_ID, null, 0, 0, 0, new Bundle());
        }
    }

    public static void requestLocationPermissions(Fragment fragment, String[] permissions) {
        fragment.requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_ID);
    }

    public static void requestLocationPermissions(Activity activity, String[] permissions) {
        ActivityCompat.requestPermissions(activity, permissions,
                LOCATION_PERMISSION_REQUEST_ID);
    }

    public static void requestResolution(Fragment fragment, PendingIntent pendingIntent) throws IntentSender.SendIntentException {
        if(pendingIntent != null) {
            fragment.startIntentSenderForResult(pendingIntent.getIntentSender(), RESOLUTION_REQUEST_ID, null, 0, 0, 0, new Bundle());
        }
    }

    public static boolean isPermissionsGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public interface LocationCallbacks{
        void connectionFailed(ConnectionResult connectionResult);

        void executePermissionsRequest(String[] permissions);

        void executeResolutionRequest(PendingIntent pendingIntent);

        void onLocation(Location location);
    }
}

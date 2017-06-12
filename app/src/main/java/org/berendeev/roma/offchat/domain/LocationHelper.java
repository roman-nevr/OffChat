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
import com.google.android.gms.location.LocationRequest;

import org.berendeev.roma.offchat.domain.model.LocationState;

import io.reactivex.Observable;

public abstract class LocationHelper {
    String PENDING_INTENT = "pending_intent";

    public static final int REQUEST_CHECK_SETTINGS = 42;
    public static final int LOCATION_PERMISSION_REQUEST_ID = 43;
    public static final int RESOLUTION_REQUEST_ID = 44;

    public abstract void requestLocation(LocationCallbacks callbacks, Priority priority, int expirationDuration);

    public abstract void requestLocationUpdates(LocationCallbacks callbacks, int interval, int fastestInterval, Priority priority);

    public abstract void stopUpdates();

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

    protected static void requestResolution(Fragment fragment, PendingIntent pendingIntent) throws IntentSender.SendIntentException {
        if(pendingIntent != null) {
            fragment.startIntentSenderForResult(pendingIntent.getIntentSender(), RESOLUTION_REQUEST_ID, null, 0, 0, 0, new Bundle());
        }
    }

    protected static boolean isAllPermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    public enum Priority{
        NO_POWER {
            private String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
            @Override public int getValue(){return LocationRequest.PRIORITY_NO_POWER;}

            @Override public String[] getPermissions() {return permissions;}
        },
        LOW_POWER {
            private String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
            @Override public int getValue(){return LocationRequest.PRIORITY_LOW_POWER;}

            @Override public String[] getPermissions() {
                return new String[0];
            }
        },
        BALANCED_POWER_ACCURACY {
            private String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION};
            @Override public int getValue(){return LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;}

            @Override public String[] getPermissions() {return permissions;}
        },
        HIGH_ACCURACY {
            private String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            @Override public int getValue(){return LocationRequest.PRIORITY_HIGH_ACCURACY;}

            @Override public String[] getPermissions() {return permissions;}
        };

        public abstract int getValue();

        public abstract String[] getPermissions();
    }

    public interface LocationCallbacks{
        void connectionFailed(ConnectionResult connectionResult);

        void executePermissionsRequest(String[] permissions);

        void executeResolutionRequest(PendingIntent pendingIntent);

        void onLocation(Location location);

        void onLocationNotAvailable();

        void onPermissionsRejected();
    }
}

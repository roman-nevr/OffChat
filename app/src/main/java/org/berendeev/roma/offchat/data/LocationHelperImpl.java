package org.berendeev.roma.offchat.data;

import android.app.PendingIntent;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.appcompat.BuildConfig;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.berendeev.roma.offchat.domain.LocationHelper;

import static android.app.Activity.RESULT_OK;
import static android.support.v4.content.PermissionChecker.PERMISSION_DENIED;

public class LocationHelperImpl extends LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient
        .OnConnectionFailedListener, LocationListener {

    private final GoogleApiClient googleApiClient;
    private Context context;
    private LocationCallbacks callbacks;
    private Behavior behavior;

    //todo persist value?
    private boolean permissionRejected;

    public LocationHelperImpl(Context context) {
        this.context = context;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override public void onConnected(@Nullable Bundle bundle) {
        try {
            Location lastLocation = getLastLocation();
            if (lastLocation == null) {
                LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
                if (locationAvailability == null || !locationAvailability.isLocationAvailable()) {
                    if (isAllPermissionsGranted(context, behavior.getPermissions())) {
                        checkSettings();
                    } else {
                        //may be you forgot declare some permissions in manifest
                        //or may be user turn off some permissions
                        requestLocationPermissions();
                    }
                }
            } else {
                behavior.onLocationReceived(lastLocation);
            }
        } catch (SecurityException e) {
        }
    }

    @Override public void onConnectionSuspended(int i) {
        int a = 0;
    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        callbacks.connectionFailed(connectionResult);
    }

    @Override public void onLocationChanged(Location location) {
        behavior.onLocationReceived(location);
    }

    @Override public void stopUpdates() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }catch (SecurityException e){
            if (BuildConfig.DEBUG){
                e.printStackTrace();
            }
        }
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override public void requestLocation(LocationCallbacks callbacks, Priority priority, int expirationDuration) {
        setLocationCallbacks(callbacks);
        behavior = new SingleLocationRequestBehavior(priority, expirationDuration);
        requestLocation();
    }

    private void requestLocation(){
        if (googleApiClient != null && googleApiClient.isConnected()){
            //todo
            if (!isAllPermissionsGranted(context, behavior.getPermissions()) && !permissionRejected) {
                    callbacks.executePermissionsRequest(behavior.getPermissions());
            }
            Location lastLocation = getLastLocation();
            if (lastLocation != null){
                behavior.onLocationReceived(lastLocation);
            }
        }else {
            if (googleApiClient != null){
                googleApiClient.connect();
            }
        }
    }

    @Override public void requestLocationUpdates(LocationCallbacks callbacks, int interval, int fastestInterval, Priority priority) {
//        createLocationRequest(interval, fastestInterval, priority.getValue());
        setLocationCallbacks(callbacks);
        behavior = new LocationUpdatesBehavior(interval, fastestInterval, priority);
        requestLocation();
    }

    @Override public void onActivityResult(int requestCode, int resultCode) {
        if (requestCode == RESOLUTION_REQUEST_ID){
            if (resultCode == RESULT_OK){
                behavior.onPermissionsGranted();
            }else {
                callbacks.onLocationNotAvailable();
            }
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_ID){
                if (isPermissionsGranted(permissions, grantResults)){
                    behavior.onPermissionsGranted();
                }else {
//                    permissionRejected = true;
                    callbacks.onPermissionsRejected();
                }
            }
    }

    private Location getLastLocation() {
        try {
            return LocationServices.FusedLocationApi
                    .getLastLocation(googleApiClient);
        } catch (SecurityException e) {
            return null;
        }
    }

   /**
    * check settings before call this method
    */

    private void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, behavior.getRequest(),this);
        }catch (SecurityException e){
            if (BuildConfig.DEBUG){
                e.printStackTrace();
            }
            callbacks.onLocationNotAvailable();
        }
    }

    private void requestLocationPermissions() {
        callbacks.executePermissionsRequest(behavior.getPermissions());
    }

    private void checkSettings() {
        LocationSettingsRequest.Builder locationSettingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(behavior.getRequest());
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        locationSettingsBuilder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        behavior.onSettingsSuccess();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        if(status.hasResolution() && !permissionRejected){
                            PendingIntent pendingIntent = status.getResolution();
                            callbacks.executeResolutionRequest(pendingIntent);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        permissionRejected = true;
                        callbacks.onLocationNotAvailable();
                        break;
                }
            }
        });
    }

    public boolean isPermissionsGranted(String[] permissions, int[] grantResults) {
        for (int grantResult : grantResults) {
            //todo check is it right
            if (grantResult == PERMISSION_DENIED){
                if (BuildConfig.DEBUG){
                    Log.d("location", "permission denied");
                }
                return false;
            }
        }
        return true;
    }

    private void setLocationCallbacks(LocationCallbacks callbacks){
        if (callbacks == null) {
            throw new IllegalArgumentException("callback must be defined");
        }
        this.callbacks = callbacks;
    }

    private abstract class Behavior{
        abstract LocationRequest getRequest();

        abstract String[] getPermissions();

        abstract void onSettingsSuccess();

        abstract void onLocationReceived(Location location);

        void onPermissionsGranted() {
            checkSettings();
        }
    }

    private class SingleLocationRequestBehavior extends Behavior{

        private LocationRequest request;
        private Priority priority;
        private int expirationDuration;

        SingleLocationRequestBehavior(Priority priority, int expirationDuration) {
            request = new LocationRequest();
            request.setPriority(priority.getValue());
            this.priority = priority;
            this.expirationDuration = expirationDuration;
        }

        LocationRequest getRequest() {
            return request;
        }

        @Override String[] getPermissions() {
            return priority.getPermissions();
        }

        @Override public void onSettingsSuccess() {
            Location lastLocation = getLastLocation();
            if (lastLocation == null){
                waitForUpdate();
            }else {
                callbacks.onLocation(lastLocation);
            }
        }

        private void waitForUpdate() {
            request.setNumUpdates(1).setExpirationDuration(expirationDuration);
            startLocationUpdates();
        }

        @Override void onLocationReceived(Location location) {
            googleApiClient.disconnect();
            callbacks.onLocation(location);
        }
    }

    private class LocationUpdatesBehavior extends Behavior{

        private LocationRequest request;
        private Priority priority;

        LocationUpdatesBehavior(int interval, int fastestInterval, Priority priority) {
            request = new LocationRequest();
            request.setInterval(interval);
            request.setFastestInterval(fastestInterval);
            request.setPriority(priority.getValue());
            this.priority = priority;
        }

        @Override LocationRequest getRequest() {
            return request;
        }

        @Override String[] getPermissions() {
            return priority.getPermissions();
        }

        @Override public void onSettingsSuccess() {
            startLocationUpdates();
        }

        @Override void onLocationReceived(Location location) {
            callbacks.onLocation(location);
        }
    }
}
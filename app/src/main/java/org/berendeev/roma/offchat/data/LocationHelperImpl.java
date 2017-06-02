package org.berendeev.roma.offchat.data;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

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
import org.berendeev.roma.offchat.domain.model.LocationState;

import io.reactivex.Observable;

public class LocationHelperImpl extends LocationHelper implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient
        .OnConnectionFailedListener, LocationListener {

    private final GoogleApiClient googleApiClient;
    private Context context;
    private LocationCallbacks callbacks;
    private LocationRequest locationRequest;

    //todo persist value
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
                    //not available
                }
            } else {
                callbacks.onLocation(lastLocation);
            }
        } catch (SecurityException e) {
        }
    }

    @Override public void onConnectionSuspended(int i) {

    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        callbacks.connectionFailed(connectionResult);
    }

    @Override public void onLocationChanged(Location location) {
        callbacks.onLocation(location);
    }

    @Override public void disconnect() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override public Observable<LocationState> getLocationStateObservable() {
        return null;
    }

    @Override public void requestLocation(LocationCallbacks callbacks) {

        if (callbacks == null) {
            throw new IllegalArgumentException("callback must be defined");
        }
        this.callbacks = callbacks;

        if (googleApiClient != null && googleApiClient.isConnected()){
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (!permissionRejected){
                    callbacks.executePermissionsRequest(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION});
                }
            }
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            callbacks.onLocation(lastLocation);
        }else {
            if (googleApiClient != null){
                googleApiClient.connect();
            }
        }

    }

    private void requestUpdates() throws SecurityException {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    @Override public void onActivityResult(int requestCode, int resultCode) {

    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_ID){
                if (isPermissionsGranted()){
                    requestLocation(callbacks);
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

    private void startLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            if (isPermissionsGranted(context)) {
                checkSettings();
            } else {
                requestLocationPermissions();
            }
        }
    }

    private void requestLocationPermissions() {
        callbacks.executePermissionsRequest(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    private void checkSettings() {
        createLocationRequest();
        LocationSettingsRequest.Builder locationSettingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        locationSettingsBuilder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        requestLocation(callbacks);
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        if(status.hasResolution() && !permissionRejected){
                            PendingIntent pendingIntent = status.getResolution();
                            callbacks.executeResolutionRequest(pendingIntent);
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        permissionRejected = true;
                        break;
                }
            }
        });
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(60 * 60 * 1000);
        locationRequest.setFastestInterval(60 * 60 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    public boolean isPermissionsGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}

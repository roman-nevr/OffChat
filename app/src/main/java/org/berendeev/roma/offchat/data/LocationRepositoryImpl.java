package org.berendeev.roma.offchat.data;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
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

import org.berendeev.roma.offchat.domain.LocationRepository;
import org.berendeev.roma.offchat.domain.model.LocationState;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static org.berendeev.roma.offchat.domain.model.LocationState.DEFAULT;
import static org.berendeev.roma.offchat.domain.model.LocationState.State.connectionFailed;
import static org.berendeev.roma.offchat.domain.model.LocationState.State.disconnected;
import static org.berendeev.roma.offchat.domain.model.LocationState.State.ok;
import static org.berendeev.roma.offchat.domain.model.LocationState.State.permissionsRejected;
import static org.berendeev.roma.offchat.domain.model.LocationState.State.requestPermissions;
import static org.berendeev.roma.offchat.domain.model.LocationState.State.requestResolution;

public class LocationRepositoryImpl implements LocationRepository, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {



    private Context context;

    private GoogleApiClient googleApiClient;
    private BehaviorSubject<Location> locationSubject;
    private BehaviorSubject<LocationState> locationStateSubject;

    private LocationRequest locationRequest;
    private LocationSettingsRequest.Builder locationSettingsBuilder;

    private PendingIntent pendingIntent;

    public LocationRepositoryImpl(Context context) {
        this.context = context;
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        locationSubject = BehaviorSubject.create();
        locationStateSubject = BehaviorSubject.createDefault(DEFAULT);
        createLocationRequest();
    }

    @Override public void onConnected(@Nullable Bundle bundle) {
        try {
            Location lastLocation = getLastLocation();
            if (lastLocation == null) {
                LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
                if (!locationAvailability.isLocationAvailable()) {
                    locationStateSubject.onNext(DEFAULT);
                }
            }
            startLocationUpdates();
        } catch (SecurityException e) {
            locationStateSubject.onNext(DEFAULT.toBuilder().state(disconnected).build());
        }
    }

    private void requestLocationPermissions() {
        locationStateSubject.onNext(DEFAULT.toBuilder().state(requestPermissions).build());
    }

    private Location getLastLocation() {
        try {
            return LocationServices.FusedLocationApi
                    .getLastLocation(googleApiClient);
        } catch (SecurityException e) {
            return null;
        }

    }

    @Override
    public void connect() {
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect();
        }
    }

    @Override
    public void disconnect() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
//            stateSubject.onNext(disconnected);
            locationStateSubject.onNext(DEFAULT.toBuilder().state(disconnected).build());
        }
    }

    private void startLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            if (LocationUtils.isPermissionsGranted(context)) {
                checkSettings();
            } else {
                requestLocationPermissions();
            }
        }
    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    googleApiClient, this);
        }

    }

    private boolean isAvailable() throws SecurityException {
        LocationAvailability locationAvailability = LocationServices.FusedLocationApi.getLocationAvailability(googleApiClient);
        return locationAvailability.isLocationAvailable();
    }

    private void checkSettings() {
        locationSettingsBuilder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        locationSettingsBuilder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
//                final LocationSettingsStates states = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        createLocationRequest();
                        requestUpdates();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        if(status.hasResolution() && locationStateSubject.getValue().state() != permissionsRejected){
                            pendingIntent = status.getResolution();
                            Intent intent = new Intent();
                            intent.putExtra(PENDING_INTENT, pendingIntent);
                            locationStateSubject.onNext(DEFAULT.toBuilder().state(requestResolution).data(intent).build());
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        locationStateSubject.onNext(DEFAULT.toBuilder().state(permissionsRejected).build());
                        break;
                }
            }
        });
    }

    private void requestUpdates() throws SecurityException {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(60 * 60 * 1000);
        locationRequest.setFastestInterval(60 * 60 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    @Override public void onConnectionSuspended(int i) {
    }

    @Override public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        locationStateSubject.onNext(DEFAULT.toBuilder().state(connectionFailed).build());
    }

    @Override public void onLocationChanged(Location location) {
        locationSubject.onNext(location);
        locationStateSubject.onNext(DEFAULT.toBuilder().state(ok).build());
    }

    @Override
    public Observable<Location> getLocationObservable() {
        return locationSubject;
    }

    @Override
    public Observable<LocationState> getLocationStateObservable() {
        return locationStateSubject;
    }

    @Override public Location getLastKnownLocation() {
        return getLastLocation();
    }


    @Override
    public void onRequestResult(int requestCode, int resultCode) {
        if(requestCode == RESOLUTION_REQUEST_ID){
            if (resultCode == 0){
                locationStateSubject.onNext(DEFAULT.toBuilder().state(permissionsRejected).build());
            }else {
                startLocationUpdates();
            }
        }
    }



    public void requestResolution(Fragment fragment) throws IntentSender.SendIntentException {
        if(pendingIntent != null) {
            fragment.startIntentSenderForResult(pendingIntent.getIntentSender(), RESOLUTION_REQUEST_ID, null, 0, 0, 0, new Bundle());
        }
    }



    public void openLocationSettings(Fragment fragment){
        fragment.startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    public boolean isConnected(){
        if(googleApiClient != null){
            return googleApiClient.isConnected();
        }else {
            return false;
        }
    }

}

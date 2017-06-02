package org.berendeev.roma.offchat.domain;

import android.location.Location;

import org.berendeev.roma.offchat.domain.model.LocationState;

import io.reactivex.Observable;

public interface LocationRepository {
    String PENDING_INTENT = "pending_intent";

    public static final int REQUEST_CHECK_SETTINGS = 42;
    public static final int LOCATION_PERMISSION_REQUEST_ID = 43;
    public static final int RESOLUTION_REQUEST_ID = 44;

    void connect();

    void disconnect();

    void onRequestResult(int requestCode, int resultCode);

    Observable<Location> getLocationObservable();

    Observable<LocationState> getLocationStateObservable();

    Location getLastKnownLocation();
}

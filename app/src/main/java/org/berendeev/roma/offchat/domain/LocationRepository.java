package org.berendeev.roma.offchat.domain;

import android.location.Location;

import org.berendeev.roma.offchat.domain.model.LocationState;

import io.reactivex.Observable;

public interface LocationRepository {
    String PENDING_INTENT = "pending_intent";

    void connect();

    void disconnect();

    void onRequestResult(int requestCode, int resultCode);

    Observable<Location> getLocationObservable();

    Observable<LocationState> getLocationStateObservable();
}

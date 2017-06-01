package org.berendeev.roma.offchat.domain.model;

import android.content.Intent;

import com.google.auto.value.AutoValue;

import static org.berendeev.roma.offchat.domain.model.LocationState.State.notAvailable;

@AutoValue
public abstract class LocationState {

    public static LocationState DEFAULT = create(notAvailable, new Intent());

    public abstract State state();

    public abstract Intent data();

    public enum State {
        ok, notAvailable, requestPermissions, requestResolution, permissionsRejected, disconnected, connectionFailed
    }

    public static LocationState create(State state, Intent data) {
        return builder()
                .state(state)
                .data(data)
                .build();
    }

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_LocationState.Builder();
    }


    @AutoValue.Builder public abstract static class Builder {
        public abstract Builder state(State state);

        public abstract Builder data(Intent data);

        public abstract LocationState build();
    }
}

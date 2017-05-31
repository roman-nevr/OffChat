package org.berendeev.roma.offchat.domain.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Image {

    public static Image EMPTY = create("");

    public abstract String source();

    public static Image create(String source) {
        return new AutoValue_Image(source);
    }


}

package org.berendeev.roma.offchat.domain.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Message {

    public abstract long id();

    public abstract Owner owner();

    public abstract String text();

    public abstract Image image();

    public static Message create(long id, Owner owner, String text, Image image) {
        return new AutoValue_Message(id, owner, text, image);
    }

    public enum Owner{
        me, notMe
    }


}

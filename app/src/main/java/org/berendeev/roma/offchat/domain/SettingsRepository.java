package org.berendeev.roma.offchat.domain;

public interface SettingsRepository {
    void saveShowNotifications(boolean show);
    boolean isShowNotifications();
}

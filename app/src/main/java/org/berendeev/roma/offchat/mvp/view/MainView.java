package org.berendeev.roma.offchat.mvp.view;

import org.berendeev.roma.offchat.domain.model.Message;

import java.util.List;

public interface MainView {

    void setMessages(List<Message> messages);

    void setText(String text);
}

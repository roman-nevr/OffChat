package org.berendeev.roma.offchat.domain;

import org.berendeev.roma.offchat.domain.model.Message;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.annotations.Nullable;

public interface ChatRepository {
    Observable<List<Message>> getMessagesObservable();

    Completable sendMessage(String message);

    Completable sendMessageWithImage(String message, @Nullable String imagePath);

    Completable newIncomeMessage(Message message);
}
package org.berendeev.roma.offchat.data;

import android.content.Context;
import android.location.Location;

import org.berendeev.roma.offchat.data.prefs.LastSeenTimeDataSource;
import org.berendeev.roma.offchat.data.sqlite.MessageSqlDataSource;
import org.berendeev.roma.offchat.domain.ChatRepository;
import org.berendeev.roma.offchat.domain.model.Image;
import org.berendeev.roma.offchat.domain.model.Message;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.BehaviorSubject;

import static org.berendeev.roma.offchat.domain.model.Message.Owner.me;
import static org.berendeev.roma.offchat.domain.model.Message.Owner.notMe;


public class ChatRepositoryImpl implements ChatRepository {

    private List<Message> messages = new ArrayList<>();
    private BehaviorSubject<List<Message>> messagesSubject;
    private long lastSeenTime;

    private int number = 0;

    private MessageSqlDataSource sqlDataSource;
    private LastSeenTimeDataSource lastSeenTimeDataSource;

    public ChatRepositoryImpl(MessageSqlDataSource sqlDataSource, LastSeenTimeDataSource lastSeenTimeDataSource) {
        this.sqlDataSource = sqlDataSource;
        this.lastSeenTimeDataSource = lastSeenTimeDataSource;

//        sqlDataSource.removeAll();

        messagesSubject = BehaviorSubject.createDefault(sqlDataSource.getAllMessages());
        lastSeenTime = lastSeenTimeDataSource.getLastSeenTime();
//        startFakeLoop();
    }

    @Override public Observable<List<Message>> getMessagesObservable() {
        lastSeenTime = getCurrentTime();
        return messagesSubject.doOnDispose(() -> {
            lastSeenTime = getCurrentTime();
            lastSeenTimeDataSource.saveLastSeenTime(lastSeenTime);
        });
    }

    @Override public Completable sendMessage(String message) {
        return Completable.fromAction(() -> {
            sendNewMessage(message, null);
        });
    }

    @Override public Completable sendMessageWithImage(String text, String imagePath) {
        return Completable.fromAction(() -> {
            sendNewMessage(text, imagePath);
        });
    }

    @Override public Completable newIncomeMessage(String text) {
        return Completable.fromAction(() -> {
            Message message = Message.create(-1, getCurrentTime(), notMe, text, Image.EMPTY);
            saveMessage(message);
        });
    }

    private void saveMessage(Message message){
        sqlDataSource.saveMessage(message);
        List<Message> allMessages = sqlDataSource.getAllMessages();
        messagesSubject.onNext(allMessages);
    }

    @Override public Single<List<Message>> getUnreadMessages() {
        return Single.fromCallable(() -> {
            lastSeenTime = lastSeenTimeDataSource.getLastSeenTime();
            return sqlDataSource.getAllAfterTime(lastSeenTime);
        });
    }

    @Override public Completable sendLocation(Location location) {
        return Completable.fromAction(() -> {
            if (location != null){
                sendNewMessage(location.toString(), null);
            }
        });
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    private void sendNewMessage(String text, String imagePath){
        Message newMessage;
        if (imagePath != null && !imagePath.isEmpty()){
            newMessage = createMessage(text, imagePath);
        }else {
            newMessage = createMessage(text);
        }
        saveMessage(newMessage);
    }

    private Message createMessage(String message, String imagePath){
        return Message.create(-1, getCurrentTime(), me, message, Image.create(imagePath));
    }

    private Message createMessage(String message){
        return Message.create(-1, getCurrentTime(), me, message, Image.EMPTY);
    }

    private void startFakeLoop(){
        new Thread(new Runnable() {
            @Override public void run() {
                boolean finish = false;
                while (!finish){
                    Message message = Message.create(-1, getCurrentTime(), notMe, "message " + ++number, Image.EMPTY);
                    saveMessage(message);
                    try {
                        Thread.sleep(3000);
                    }catch (InterruptedException e){
                        finish = true;
                    }
                }
            }
        }).start();
    }

}

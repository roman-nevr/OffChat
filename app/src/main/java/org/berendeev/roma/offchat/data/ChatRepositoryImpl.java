package org.berendeev.roma.offchat.data;

import org.berendeev.roma.offchat.domain.ChatRepository;
import org.berendeev.roma.offchat.domain.model.Image;
import org.berendeev.roma.offchat.domain.model.Message;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.annotations.Nullable;
import io.reactivex.subjects.BehaviorSubject;

import static org.berendeev.roma.offchat.domain.model.Message.Owner.me;
import static org.berendeev.roma.offchat.domain.model.Message.Owner.notMe;


public class ChatRepositoryImpl implements ChatRepository {

    private List<Message> messages = new ArrayList<>();
    private long id = 0;
    private BehaviorSubject<List<Message>> messagesSubject;

    public ChatRepositoryImpl() {
        messagesSubject = BehaviorSubject.create();
    }

    @Override public Observable<List<Message>> getMessagesObservable() {
        startFakeLoop();
        return messagesSubject;
//        return Observable.create(emitter -> {
//            while (!emitter.isDisposed()){
//                Message message = Message.create(id++, notMe, "message " + id, Image.EMPTY);
//                messages.add(message);
//                emitter.onNext(messages);
//                try {
//                    Thread.sleep(3000);
//                }catch (InterruptedException e){
//                    emitter.onError(e);
//                }
//            }
//        });
    }

    void startFakeLoop(){
        new Thread(new Runnable() {
            @Override public void run() {
                boolean finish = false;
                while (!finish){
                    Message message = Message.create(id++, notMe, "message " + id, Image.EMPTY);
                    addMessage(message);
                    try {
                        Thread.sleep(3000);
                    }catch (InterruptedException e){
                        finish = true;
                    }
                }
            }
        }).start();

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

    private void sendNewMessage(String text, String imagePath){
        Message newMessage;
        if (imagePath != null){
            newMessage = createMessage(text, imagePath);
        }else {
            newMessage = createMessage(text);
        }
        addMessage(newMessage);
    }

    @Override public Completable newIncomeMessage(Message message) {
        return null;
    }

    private void addMessage(Message message){
        messages.add(message);
        messagesSubject.onNext(messages);
    }

    private Message createMessage(String message, String imagePath){
        return Message.create(id++, me, message, Image.create(imagePath));
    }

    private Message createMessage(String message){
        return Message.create(id++, me, message, Image.EMPTY);
    }

}

package org.berendeev.roma.offchat.mvp.presenter;

import android.location.Location;

import org.berendeev.roma.offchat.domain.ChatRepository;
import org.berendeev.roma.offchat.domain.LocationRepository;
import org.berendeev.roma.offchat.mvp.view.MainView;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainViewPresenter {

    private MainView view;
    private CompositeDisposable compositeDisposable;
    @Inject ChatRepository repository;
    @Inject LocationRepository locationRepository;


    @Inject
    public MainViewPresenter() {
        compositeDisposable = new CompositeDisposable();
    }

    public void setView(MainView view) {
        this.view = view;
    }

    public void start(){
        subscribeOnMessages();
        initLocation();
        onLocationClick();
    }

    public void stop(){
        compositeDisposable.clear();
    }

    public void onLocationClick(){
        Location lastKnownLocation = locationRepository.getLastKnownLocation();
        repository.sendLocation(lastKnownLocation);
    }

    public void sendMessage(String text){
        if (!text.isEmpty()) {
            repository.sendMessage(text).subscribe();
            view.setText("");
        }
    }

    public void sendMessage(String text, String path){
        repository.sendMessageWithImage(text, path).subscribe();
        view.setText("");
    }

    private void subscribeOnMessages() {
        compositeDisposable.add(repository
                .getMessagesObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(messages -> {
                    view.setMessages(messages);
                }));
    }

    private void initLocation() {
        compositeDisposable.add(locationRepository
                .getLocationStateObservable()
                .subscribe(locationState -> {
                    switch (locationState.state()){
                        case ok:{

                            break;
                        }
                        case connectionFailed:{

                            break;
                        }
                    }
                }));
        locationRepository.connect();
    }
}

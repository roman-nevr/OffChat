package org.berendeev.roma.offchat.presentation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.berendeev.roma.offchat.domain.ChatRepository;
import org.berendeev.roma.offchat.domain.model.Message;
import org.berendeev.roma.offchat.presentation.adapter.ChatAdapter;
import org.berendeev.roma.offchat.service.presentation.MainService;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static org.berendeev.roma.offchat.service.BotService.START;
import static org.berendeev.roma.offchat.service.BotService.STOP;

public class MainActivity extends AppCompatActivity {

    private static final int SERVICE_NOTIFICATION_ID = 42;
    /*@BindView(R.id.start)*/ Button start;
    /*@BindView(R.id.stop)*/ Button stop;

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.et_message) EditText etMessage;
    @BindView(R.id.button_send) ImageButton sendButton;

    private ChatRepository repository;
    private ChatAdapter adapter;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initDi();
    }

    @Override protected void onStart() {
        super.onStart();
        subscribeOnMessages();
    }

    private void subscribeOnMessages() {
        compositeDisposable.add(repository
                .getMessagesObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(messages -> {
                    setMessages(messages);
                }));
    }

    private void initDi() {
        repository = App.getChatComponent().chatRepository();
    }

    private void initUi() {
        setContentView(R.layout.chat_main);
        ButterKnife.bind(this);
        initRecyclerView();
        initEditText();
    }

    private void initEditText() {
        sendButton.setOnClickListener(v -> {
            if (!etMessage.getText().toString().isEmpty()){
                repository.sendMessage(etMessage.getText().toString())
                        .subscribe();
                etMessage.setText("");
            }
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void setMessages(List<Message> messages) {
        if (adapter == null){
            adapter = new ChatAdapter(messages, getApplicationContext());
            recyclerView.setAdapter(adapter);
        }else{
            adapter.update(messages);
        }
    }

    private void noti(){
        start.setOnClickListener(v -> {
            startService(getStartIntent());
        });
        stop.setOnClickListener(v -> {
            startService(getStopIntent());
        });
        showNotification("hello " + System.currentTimeMillis());
    }

    @Override protected void onStop() {
        super.onStop();
        compositeDisposable.clear();
//        Toast.makeText(this, "Oops", Toast.LENGTH_LONG).show();
    }

    private Intent getStartIntent(){
        Intent intent = new Intent(this, MainService.class);
        intent.putExtra(MainService.COMMAND, START);
        return intent;
    }

    private Intent getStopIntent(){
        Intent intent = new Intent(this, MainService.class);
        intent.putExtra(MainService.COMMAND, STOP);
        return intent;
    }

    private void showNotification(String message) {

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();

        style.setBigContentTitle(2 +" new pushes");
        style.addLine(message + "1");
        style.addLine(message + "2");
        style.setSummaryText("summary");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Message")
                .setContentText(message)
                .setGroupSummary(true)
                .setNumber(2)
                .setStyle(style)
                .addAction(R.drawable.ic_reply, "Reply", pIntent)
                .setGroup("myPushes")
                .setSmallIcon(org.berendeev.roma.offchat.R.mipmap.ic_launcher);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = builder.build();
        notificationManager.notify(SERVICE_NOTIFICATION_ID, notification);
    }
}

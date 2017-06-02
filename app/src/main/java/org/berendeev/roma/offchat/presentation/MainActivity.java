package org.berendeev.roma.offchat.presentation;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;

import org.berendeev.roma.offchat.domain.LocationHelper;
import org.berendeev.roma.offchat.domain.LocationHelper.LocationCallbacks;
import org.berendeev.roma.offchat.domain.model.Message;
import org.berendeev.roma.offchat.mvp.presenter.MainViewPresenter;
import org.berendeev.roma.offchat.mvp.view.MainView;
import org.berendeev.roma.offchat.presentation.adapter.ChatAdapter;
import org.berendeev.roma.offchat.service.presentation.MainService;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.berendeev.roma.offchat.service.presentation.MainService.START;

public class MainActivity extends AppCompatActivity implements MainView, LocationCallbacks{

    private static final String PATH = "path";



    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.et_message) EditText etMessage;
    @BindView(R.id.button_send) ImageButton sendButton;

    @BindView(R.id.location_button) ImageButton locationButton;
    @BindView(R.id.photo_button) ImageButton photoButton;
    @BindView(R.id.gallery_button) ImageButton galleryButton;

    private ChatAdapter adapter;

    @Inject MainViewPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initDi();
        if (savedInstanceState == null){
            presenter.onLocationClick();
        }
    }

    @Override protected void onStart() {
        super.onStart();
        presenter.start();
        startBot();

    }

    private void startBot() {
        startService(getStartIntent());
    }

    private Intent getStartIntent(){
        Intent intent = new Intent(this, MainService.class);
        intent.putExtra(MainService.COMMAND, START);
        return intent;
    }

    private void initDi() {
        App.getChatComponent().inject(this);
        presenter.setView(this);
        presenter.setLocationCallbacks(this);
        presenter.setActivity(this);
    }

    private void initUi() {
        setContentView(R.layout.chat_main);
        ButterKnife.bind(this);
        initRecyclerView();
        initEditText();

        initPhoto();
        initGallery();
        initLocation();
    }

    private void initLocation() {
        locationButton.setOnClickListener(v -> {
            presenter.onLocationClick();
        });
    }

    private void initGallery() {
        galleryButton.setOnClickListener(v -> {
            presenter.dispatchOpenGallery();
        });
    }

    private void initPhoto() {
        photoButton.setOnClickListener(v -> {
            presenter.dispatchTakePictureIntent();
        });
    }

    private void initEditText() {
        sendButton.setOnClickListener(v -> {
            hideKeyboard(etMessage);
            presenter.sendMessage(etMessage.getText().toString());
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void setMessages(List<Message> messages) {
        if (adapter == null){
            adapter = new ChatAdapter(messages);
            recyclerView.setAdapter(adapter);
            recyclerView.scrollToPosition(0);
        }else{
            adapter.update(messages);
        }
    }

    @Override public void setText(String text) {
        etMessage.setText(text);
    }

    @Override protected void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.onActivityResult(requestCode, resultCode, data);

    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        presenter.onPermissionsGranted(requestCode, permissions, grantResults);
    }



    @Override public void onSaveInstanceState(Bundle outState) {
        outState.putString(PATH, presenter.getCurrentPhotoPath());
        super.onSaveInstanceState(outState);
    }

    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        presenter.setCurrentPhotoPath(savedInstanceState.getString(PATH));
    }

    @Override public void connectionFailed(ConnectionResult connectionResult) {
        if (BuildConfig.DEBUG){
            Toast.makeText(this, "connectionFailed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override public void executePermissionsRequest(String[] permissions) {
        LocationHelper.requestLocationPermissions(this, permissions);
    }

    @Override public void executeResolutionRequest( PendingIntent pendingIntent) {
        try {
            LocationHelper.requestResolution(this, pendingIntent);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @Override public void onLocation(Location location) {
        presenter.onLocation(location);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        for (int index = 0; index < menu.size(); index++) {
            MenuItem item = menu.getItem(index);
            presenter.setUpItem(item);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.notifications){
            presenter.setShowNotification(item);
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}

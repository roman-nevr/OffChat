package org.berendeev.roma.offchat.presentation;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.berendeev.roma.offchat.domain.ChatRepository;
import org.berendeev.roma.offchat.domain.model.Message;
import org.berendeev.roma.offchat.mvp.presenter.MainViewPresenter;
import org.berendeev.roma.offchat.mvp.view.MainView;
import org.berendeev.roma.offchat.presentation.adapter.ChatAdapter;
import org.berendeev.roma.offchat.service.presentation.MainService;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static org.berendeev.roma.offchat.service.BotService.START;
import static org.berendeev.roma.offchat.service.BotService.STOP;

public class MainActivity extends AppCompatActivity implements MainView{

    private static final String PATH = "path";
    private static final int ACTION_OPEN_GALLERY = 2;
    private static final int ACTION_TAKE_PHOTO = 1;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String CAMERA_DIR = "/dcim/";
    private String currentPhotoPath;

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
    }

    @Override protected void onStart() {
        super.onStart();
        presenter.start();
        if (BuildConfig.DEBUG){
//            startBot();
        }
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
//        repository = App.getChatComponent().chatRepository();
        App.getChatComponent().inject(this);
        presenter.setView(this);
    }

    private void initUi() {
        setContentView(R.layout.chat_main);
        ButterKnife.bind(this);
        initRecyclerView();
        initEditText();

        initPhoto();
        initGallery();
    }

    private void initGallery() {
        galleryButton.setOnClickListener(v -> {
            dispatchOpenGallery();
        });
    }

    private void initPhoto() {
        photoButton.setOnClickListener(v -> {
            dispatchTakePictureIntent();
        });
    }

    private void initEditText() {
        sendButton.setOnClickListener(v -> {
            presenter.sendMessage(etMessage.getText().toString());
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void setMessages(List<Message> messages) {
        if (adapter == null){
            adapter = new ChatAdapter(messages, getApplicationContext());
            recyclerView.setAdapter(adapter);
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
//        Toast.makeText(this, "Oops", Toast.LENGTH_LONG).show();
    }

    private void dispatchOpenGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, ACTION_OPEN_GALLERY);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = null;

        try {
            file = setUpPhotoFile();
            currentPhotoPath = file.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        } catch (IOException e) {
            e.printStackTrace();
            file = null;
            currentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
    }

    private File setUpPhotoFile() throws IOException {

        File imageFile = createImageFile();

        return imageFile;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumDir = getAlbumDir();
        File imageFile = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumDir);
        return imageFile;
    }

    private File getAlbumDir() {
        File storageDir = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(
                    Environment.getExternalStorageDirectory()
                            + CAMERA_DIR
                            + getAlbumName()
            );

            if (storageDir != null && !storageDir.mkdirs() && !storageDir.exists()) {
                Log.d("CameraSample", "failed to create directory");
                return null;
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    handleBigCameraPhoto();
                }
                if (resultCode == RESULT_CANCELED) {
                    currentPhotoPath = null;
                }
                break;
            } // ACTION_TAKE_PHOTO
            case ACTION_OPEN_GALLERY:{
                if (resultCode == RESULT_OK) {
                    Uri targetUri = data.getData();
                    currentPhotoPath = getRealPathFromURI(targetUri);
                    presenter.sendMessage(etMessage.getText().toString(), currentPhotoPath);
                    currentPhotoPath = null;
                }
            }
        } // switch
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(this, contentUri, proj,
                null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }

        return result;
    }

    private void handleBigCameraPhoto() {

        if (currentPhotoPath != null) {
            presenter.sendMessage(etMessage.getText().toString(), currentPhotoPath);
            notifyGalleryAboutNewPicture();
            currentPhotoPath = null;
        }
    }

    private String getAlbumName() {
        return getString(R.string.album_name);
    }

    private void notifyGalleryAboutNewPicture() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File file = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        outState.putString(PATH, currentPhotoPath);
        super.onSaveInstanceState(outState);
    }

    @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPhotoPath = savedInstanceState.getString(PATH);
    }
}

package org.berendeev.roma.offchat.mvp.presenter;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;

import org.berendeev.roma.offchat.domain.ChatRepository;
import org.berendeev.roma.offchat.domain.LocationHelper;
import org.berendeev.roma.offchat.domain.SettingsRepository;
import org.berendeev.roma.offchat.mvp.view.MainView;
import org.berendeev.roma.offchat.presentation.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class MainViewPresenter {

    private static final int ACTION_OPEN_GALLERY = 2;
    private static final int ACTION_TAKE_PHOTO = 1;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String CAMERA_DIR = "/dcim/";

    private MainView view;
    private CompositeDisposable compositeDisposable;
    @Inject ChatRepository repository;
    @Inject LocationHelper locationHelper;
    @Inject SettingsRepository settingsRepository;
    private LocationHelper.LocationCallbacks locationCallbacks;
    private Activity activity;

    private String currentPhotoPath;

    @Inject
    public MainViewPresenter() {
        compositeDisposable = new CompositeDisposable();
    }

    public void setView(MainView view) {
        this.view = view;
    }

    public void setLocationCallbacks(LocationHelper.LocationCallbacks callbacks){
        this.locationCallbacks = callbacks;
    }

    public void start(){
        subscribeOnMessages();
    }

    public void stop(){
        compositeDisposable.clear();
    }

    public void onLocationClick(){
        locationHelper.requestLocation(locationCallbacks, LocationHelper.Priority.LOW_POWER, 10000);
//        locationHelper.requestLocationUpdates(locationCallbacks, 1000, 1000, LocationHelper.Priority.HIGH_ACCURACY);
    }

    public void sendMessage(String text){
        if (!text.isEmpty()) {
            repository
                    .sendMessage(text)
                    .subscribeOn(Schedulers.computation())
                    .subscribe();
            view.setText("");
        }
    }

    public void sendMessage(String text, String path){
        repository
                .sendMessageWithImage(text, path)
                .subscribeOn(Schedulers.computation())
                .subscribe();
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
//auth gXtbDI0I8FHOHjhu
//    geo fix 54,706058 38,843262

    public void onLocation(Location location) {
//        locationHelper.stopUpdates();
        repository
                .sendLocation(location)
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }

    public void onPermissionsGranted(int requestCode, String[] permissions, int[] grantResults) {
        locationHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void setShowNotification(MenuItem item) {
        if (item.isChecked()){
            item.setChecked(false);
        }else {
            item.setChecked(true);
        }
        settingsRepository.saveShowNotifications(item.isChecked());
    }

    public void setUpItem(MenuItem item) {
        if (item.getItemId() == R.id.notifications){
            item.setChecked(settingsRepository.isShowNotifications());
        }
    }

    public void dispatchOpenGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, ACTION_OPEN_GALLERY);
    }

    public void dispatchTakePictureIntent() {
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

        activity.startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
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
            Log.v(activity.getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(activity, contentUri, proj,
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
            sendMessage("", currentPhotoPath);
            notifyGalleryAboutNewPicture();
            currentPhotoPath = null;
        }
    }

    private String getAlbumName() {
        return activity.getString(R.string.album_name);
    }

    private void notifyGalleryAboutNewPicture() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File file = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        activity.sendBroadcast(mediaScanIntent);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        locationHelper.onActivityResult(requestCode, resultCode);
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
                    sendMessage("", currentPhotoPath);
                    currentPhotoPath = null;
                }
                break;
            }
        } // switch
    }

    public String getCurrentPhotoPath() {
        return currentPhotoPath;
    }

    public void setCurrentPhotoPath(String currentPhotoPath) {
        this.currentPhotoPath = currentPhotoPath;
    }
}

package org.berendeev.roma.offchat.presentation;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PhotoActivity extends AppCompatActivity {


//    @BindView(R.id.camera) Button cameraButton;
//    @BindView(R.id.gallery) Button galleryButton;
//    @BindView(R.id.imageView) ImageView imageView;

    @BindView(R.id.editText) EditText editText;
    @BindView(R.id.text2) TextView textView;

    private static final String PATH = "path";
    private static final int ACTION_OPEN_GALLERY = 2;
    private static final int ACTION_TAKE_PHOTO = 1;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String CAMERA_DIR = "/dcim/";
    private String currentPhotoPath;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo);
        ButterKnife.bind(this);

//        cameraButton.setOnClickListener(v -> {
//            dispatchTakePictureIntent();
//        });
//        galleryButton.setOnClickListener(v -> {
//            dispatchOpenGallery();
//        });


//        Uri uri = Uri.parse("/storage/sdcard/DCIM/offchat/IMG_20170531_060929_-1720877472.jpg");
//        Uri uri = Uri.fromFile(new File("/storage/sdcard/DCIM/offchat/IMG_20170531_060929_-1720877472.jpg"));
//        Picasso.with(this)
//                .load(uri)
//                .into(imageView);
    }

    private void initExp() {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                textView.setText(s);
            }

            @Override public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        initExp();

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

    private String getAlbumName() {
        return getString(R.string.album_name);
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
                    setPicture();
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
            setPicture();
            notifyGalleryAboutNewPicture();
//            currentPhotoPath = null;
        }
    }

    private void setPicture() {

//		/* There isn't enough memory to open up more than a couple camera photos */
//        /* So pre-scale the target bitmap into which the file is decoded */
//
//		/* Get the size of the ImageView */
//        int targetW = imageView.getWidth();
//        int targetH = imageView.getHeight();
//
//		/* Get the size of the image */
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
//        int photoW = bmOptions.outWidth;
//        int photoH = bmOptions.outHeight;
//
//		/* Figure out which way needs to be reduced less */
//        int scaleFactor = 1;
//        if ((targetW > 0) || (targetH > 0)) {
//            scaleFactor = Math.min(photoW / targetW, photoH / targetH);
//        }
//
//		/* Set bitmap options to scale the image decode target */
//        bmOptions.inJustDecodeBounds = false;
//        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;
//
//		/* Decode the JPEG file into a Bitmap */
//        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
//
//		/* Associate the Bitmap to the ImageView */
//        imageView.setImageBitmap(bitmap);
//        imageView.setVisibility(View.VISIBLE);
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
        if (currentPhotoPath != null) {
            //todo restore image
            setPicture();
        }
    }
}


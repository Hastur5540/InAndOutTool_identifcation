package com.example.inandouttool_identification;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {
    private ImageView imageView;
    private Uri photoURI;
    private String photoPath;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.imageView);
        Button takePhotoButton = findViewById(R.id.captureButton_1);
        Button backButton = findViewById(R.id.backButton);

        // 请求相机权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        }

        takePhotoButton.setOnClickListener(v -> dispatchTakePictureIntent());
        backButton.setOnClickListener(v -> finish());
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.inandouttool_identification.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() {
        try {
            File storageDir = getExternalFilesDir(null);
            File image = File.createTempFile("JPEG_" + System.currentTimeMillis() + "_", ".jpg", storageDir);
            photoPath = image.getAbsolutePath();
            return image;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageView.setImageURI(photoURI);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("photoPath", photoPath);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}
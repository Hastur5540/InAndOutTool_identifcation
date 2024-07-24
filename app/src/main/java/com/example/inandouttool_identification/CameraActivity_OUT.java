package com.example.inandouttool_identification;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity_OUT extends AppCompatActivity implements SurfaceHolder.Callback {
    private SurfaceView cameraPreview;
    private Camera camera;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private String photoPath;
    private String w_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPreview = findViewById(R.id.cameraPreview);
        Button takePhotoButton = findViewById(R.id.captureButton_1);
        Button backButton = findViewById(R.id.backButton);

        String workerId = getIntent().getStringExtra("workerId");
        if (workerId != null) {
            w_id = workerId;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            setupCamera();
        }

        takePhotoButton.setOnClickListener(v -> captureImage());
        backButton.setOnClickListener(v -> finish());
    }

    private void setupCamera() {
        SurfaceHolder holder = cameraPreview.getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        try {
            camera = Camera.open();
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) return;
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // Ignored
        }

        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        releaseCamera();
    }

    private void captureImage() {
        if (camera != null) {
            camera.takePicture(null, null, (data, camera) -> {
                Bitmap capturedBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                showPreview(capturedBitmap);
                saveImage(data);
            });
        }
    }

    private void showPreview(Bitmap bitmap) {
        setContentView(R.layout.preview_layout);
        ImageView previewImageView = findViewById(R.id.previewImageView);
        previewImageView.setImageBitmap(bitmap);

        Button confirmButton = findViewById(R.id.confirmButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        confirmButton.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("photoPath", photoPath);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        cancelButton.setOnClickListener(v -> {
            // 返回拍照界面
            releaseCamera(); // 释放相机资源
            setContentView(R.layout.activity_camera);
            cameraPreview = findViewById(R.id.cameraPreview);
            setupCamera(); // 重新设置相机

            // 重新绑定按钮事件
            Button takePhotoButton = findViewById(R.id.captureButton_1);
            Button backButton = findViewById(R.id.backButton);
            takePhotoButton.setOnClickListener(v1 -> captureImage());
            backButton.setOnClickListener(v1 -> finish());
        });
    }

    private void saveImage(byte[] data) {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            return;
        }
        String fileName = "OUT_" + w_id + ".jpg";
        File imageFile = new File(storageDir, fileName);
        photoPath = imageFile.getAbsolutePath();

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupCamera();
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();
    }
}
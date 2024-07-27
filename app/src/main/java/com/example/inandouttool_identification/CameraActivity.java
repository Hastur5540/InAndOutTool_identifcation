package com.example.inandouttool_identification;

import android.os.Environment;
import android.widget.RelativeLayout;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.ListenableFuture;


public class CameraActivity extends AppCompatActivity {
    private PreviewView cameraPreview;
    private Button captureButton;
    private Button backButton;
    private ImageView imageView;

    private String photoPath;
    private String w_id = null;

    private ImageCapture capturedImg;
    private View captureFrame;
    private Bitmap croppedImg;
    private HashMap<String, Integer> screenHW = new HashMap<>();


    private float CAMERA_PREVIEW_HW_RATIO = -1;
    private float FRAME_CAMERAPREVIEW_RATIO = (float) 0.8;
    private float CAPTURE_FRAME_HW_RATIO = (float) 1.4;
    private float MAX_HEIGHT_RATIO = (float) 3.5/4;

    private ProcessCameraProvider processCameraProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPreview = findViewById(R.id.cameraPreview);

        captureFrame = findViewById(R.id.captureFrame);
        captureButton = findViewById(R.id.captureButton_1);
        backButton = findViewById(R.id.backButton);

        // 得当当前activity的h 和 w
        getScreenHW(this);

        String workerId = getIntent().getStringExtra("workerId");
        if (workerId != null) {
            w_id = workerId;
        }


        // 查看当前摄像头权限并获取
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            startCamera();
        }

        startCamera();


        captureButton.setOnClickListener(v -> capturePhoto());
        backButton.setOnClickListener(v -> finish());
    }






    // 根据预定比例常量重塑View组件
    private void reshapeView(View view, int height_limit, int width, float hwRatio){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        params.width = width;
        params.height = Math.min((int)(params.width*hwRatio), height_limit);
        view.setLayoutParams(params);
        view.requestLayout();
    }


    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        if (CAMERA_PREVIEW_HW_RATIO != -1){
            reshapeView(cameraPreview, (int) (screenHW.get("height") * MAX_HEIGHT_RATIO), screenHW.get("width"), CAMERA_PREVIEW_HW_RATIO);
            reshapeView(captureFrame, (int) (screenHW.get("height") * MAX_HEIGHT_RATIO * 0.8), (int) (screenHW.get("width") * FRAME_CAMERAPREVIEW_RATIO), CAPTURE_FRAME_HW_RATIO);
        }




        cameraProviderFuture.addListener(() -> {
            try {
                processCameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                capturedImg = new ImageCapture.Builder().build();

                processCameraProvider.unbindAll();

                if (CAMERA_PREVIEW_HW_RATIO == -1) {
                    processCameraProvider.bindToLifecycle(this, cameraSelector, capturedImg);
                    capturedImg.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
                        @Override
                        public void onCaptureSuccess(@NonNull ImageProxy image) {

                            int rotationDegree = image.getImageInfo().getRotationDegrees();
                            if (rotationDegree == 0 || rotationDegree == 180)
                                CAMERA_PREVIEW_HW_RATIO = (float) image.getHeight() / image.getWidth();
                            if (rotationDegree == 90 || rotationDegree == 270)
                                CAMERA_PREVIEW_HW_RATIO = (float) image.getWidth() / image.getHeight();

                            reshapeView(cameraPreview, (int) (screenHW.get("height") * MAX_HEIGHT_RATIO), screenHW.get("width"), CAMERA_PREVIEW_HW_RATIO);
                            reshapeView(captureFrame, (int) (screenHW.get("height") * MAX_HEIGHT_RATIO * 0.8), (int) (screenHW.get("width") * FRAME_CAMERAPREVIEW_RATIO), CAPTURE_FRAME_HW_RATIO);

                            image.close();


                            processCameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, preview);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            exception.printStackTrace();
                        }
                    });
                }else{
                    processCameraProvider.bindToLifecycle(this, cameraSelector, preview, capturedImg);
                }

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));


    }


    private void capturePhoto() {
        if (capturedImg == null) {
            return;
        }
        capturedImg.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {

                // 图片处理流程： 旋转，放缩，截取。
                Bitmap bitmap = imageProxyToBitmap(image);

                croppedImg = cropBitmapToFrame(bitmap, captureFrame);

                runOnUiThread(() -> toCheckView(croppedImg));

                image.close();

//                Toast.makeText(CameraActivity.this, "Captured and cropped image obtained", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });


    }


    private Bitmap imageProxyToBitmap(ImageProxy image) {
        int rotationDegrees = image.getImageInfo().getRotationDegrees();

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        return rotateBitmap(bitmap, rotationDegrees);
    }


    // 旋转
    private Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
        if (rotationDegrees == 0) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private Bitmap cropBitmapToFrame(Bitmap original, View frame) {
        int cameraPreviewHeight = cameraPreview.getHeight();
        int cameraPreviewWidth = cameraPreview.getWidth();

        // 需要先将图片缩放到CameraPreview上的大小
        Bitmap scalaredImg = Bitmap.createScaledBitmap(original, cameraPreviewWidth, cameraPreviewHeight, true);

        // Get frame position and size
        int[] location = new int[2];
        frame.getLocationOnScreen(location);
        int frameX = location[0];
        int frameY = location[1];
        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();


        int[] previewLoc = new int[2];
        cameraPreview.getLocationOnScreen(previewLoc);
        int preFrameX = previewLoc[0];
        int preFrameY = previewLoc[1];


        // 返回截取结果
//        return original;
        return Bitmap.createBitmap(scalaredImg, frameX-preFrameX, frameY-preFrameY, frameWidth, frameHeight);
    }


    // 获取屏幕宽高（pixel）
    public void getScreenHW(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        screenHW.put("height", displayMetrics.heightPixels);
        screenHW.put("width", displayMetrics.widthPixels);
    }


    private void toCheckView(Bitmap pic){
        setContentView(R.layout.captured_photo_check);
        imageView = findViewById(R.id.previewImageView);


        Bitmap picForDisplay = Bitmap.createScaledBitmap(pic, screenHW.get("width"), (int)(screenHW.get("width") * CAMERA_PREVIEW_HW_RATIO), true);
        imageView.setImageBitmap(picForDisplay);


        Button confirmButton = findViewById(R.id.confirmButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        confirmButton.setOnClickListener(v -> {
            saveImage(bitmapToByteArray(pic));
            Intent resultIntent = new Intent();
            resultIntent.putExtra("photoPath", photoPath);
            setResult(RESULT_OK, resultIntent);
            finish();
        });


        cancelButton.setOnClickListener(v -> {
            setContentView(R.layout.activity_camera);

            // 重新获取视图对象
            cameraPreview = findViewById(R.id.cameraPreview);
            captureFrame = findViewById(R.id.captureFrame);
            captureButton = findViewById(R.id.captureButton_1);
            backButton = findViewById(R.id.backButton);

            // 重新启动相机
            startCamera();

            // 设置按钮点击事件
            captureButton.setOnClickListener(v1 -> capturePhoto());
            backButton.setOnClickListener(v2 -> finish());
        });
    }



    private void saveImage(byte[] data) {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            return;
        }
        String inOutFlag = getIntent().getStringExtra("inOutFlag") + "_";
        String fileName = inOutFlag + w_id + ".jpg";
        File imageFile = new File(storageDir, fileName);
        photoPath = imageFile.getAbsolutePath();

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 将 Bitmap 压缩为 PNG 格式，质量为 100（无损压缩）
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

}
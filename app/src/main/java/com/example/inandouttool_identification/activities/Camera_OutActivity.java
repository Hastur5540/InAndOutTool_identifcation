package com.example.inandouttool_identification.activities;

import static com.example.inandouttool_identification.utils.Constants.*;
import static com.example.inandouttool_identification.utils.Constants.SAVED_IMG_RESOLUTION_HEIGHT;
import static com.example.inandouttool_identification.utils.Constants.SAVED_IMG_RESOLUTION_WIDTH;
import static java.lang.Math.max;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.inandouttool_identification.R;
import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Camera_OutActivity extends AppCompatActivity {
    private PreviewView cameraPreview;
    private Button captureButton;
    private Button backButton;
    private ImageView imageView;
    private ImageView overlayImageView;
    private View captureFrame;
    private Bitmap croppedImg;
    private Button selectPhotoButton;
    private SeekBar seekBar;
    // Add these constants for request codes
    private static final int PICK_IMAGE_REQUEST = 102;

    private String photoPath="";
    private String d_id = null;

    private ImageCapture capturedImg;
//    private View captureFrame;
    private HashMap<String, Integer> screenHW = new HashMap<>();

    private final float CAMERA_PREVIEW_HW_RATIO = (float)SAVED_IMG_RESOLUTION_HEIGHT/SAVED_IMG_RESOLUTION_WIDTH;
    private int cameraPreviewHeight;
    private int cameraPreviewWidth;

    private ProcessCameraProvider processCameraProvider;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private TextView photoCountTextView; // Reference for the TextView
    private  String inOutFlag = "";


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // 得当前activity的h 和 w
        getScreenHW(this);
        super.onConfigurationChanged(newConfig);
        startCamera();
        overlayImageView.setVisibility(View.GONE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_out);
        cameraPreview = findViewById(R.id.cameraPreview);
//        captureFrame = findViewById(R.id.captureFrame);
        captureButton = findViewById(R.id.captureButton_1);
        backButton = findViewById(R.id.backButton);
        overlayImageView = findViewById(R.id.overlayImageView);
        selectPhotoButton = findViewById(R.id.selectPhotoButton);
        seekBar = findViewById(R.id.seekBar);
        String deviceId = getIntent().getStringExtra("WorkerId");
        if (deviceId != null) {
            d_id = deviceId;
        }

        inOutFlag = getIntent().getStringExtra("inOutFlag");

        getScreenHW(this);
        // 查看当前摄像头权限并获取
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            startCamera();
        }

        startCamera();
    }

    private void choosePhoto() {
        Intent intent = new Intent(this, Camera_AlbumActivity.class);
        intent.putExtra("WorkerId", d_id); // 传递设备ID
        intent.putExtra("inOutFlag", "in"); // 传入 in/out 标志
        startActivityForResult(intent, 1); // 启动Camera_AlbumActivity并等待返回
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 判断从选择照片页面返回的结果
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            // 获取照片的路径
            String selectedImagePath = data.getStringExtra("SelectedImagePath");
            if (selectedImagePath != null) {
                // 根据路径读取图片并设置到 overlayImageView
                Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, cameraPreviewWidth, cameraPreviewHeight, true);

                // 获取 cameraPreview 的位置和尺寸
                int left = cameraPreview.getLeft();
                int top = cameraPreview.getTop();

                // 设置 overlayImageView 的布局参数
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) overlayImageView.getLayoutParams();
                params.width = cameraPreviewWidth;
                params.height = cameraPreviewHeight;
                params.leftMargin = left;  // 对齐左边
                params.topMargin = top;    // 对齐顶部

                overlayImageView.setLayoutParams(params);
                overlayImageView.requestLayout();

                overlayImageView.setImageBitmap(scaledBitmap);

                // 确保 overlayImageView 显示
                overlayImageView.setVisibility(View.VISIBLE);
            }

        }
    }

    // 根据预定比例常量重塑View组件
    private void reshapeView(View view, int height_limit, int width, float hwRatio, int flag){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        if(flag==0){
            params.width = width;
            params.height = Math.min((int)(params.width*hwRatio), height_limit);

            cameraPreviewWidth = params.width;
            cameraPreviewHeight = params.height;

        }else{
            // 此时获取action_Bar的height
            int actionBarHeight = -1;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }

            params.height = width - actionBarHeight;
            params.width = Math.min((int)(params.height*hwRatio), height_limit);

            cameraPreviewHeight = params.height;
            cameraPreviewWidth = params.width;
        }
        view.setLayoutParams(params);
        view.requestLayout();

//        System.out.println("123");
    }


    private void startCamera() {
        int rotationDegrees = getWindowManager().getDefaultDisplay().getRotation();

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {

            getScreenHW(this);
            if(rotationDegrees%2==1){  //横着的
                reshapeView(cameraPreview, 100000, screenHW.get("height"), CAMERA_PREVIEW_HW_RATIO, 1);
            }else{ //竖着的
                reshapeView(cameraPreview, 100000, screenHW.get("width"), CAMERA_PREVIEW_HW_RATIO, 0);
            }
            try {
                processCameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // 修改ImageCapture的设置以支持1280x720
                ImageCapture.Builder builder = new ImageCapture.Builder();
//                builder.setTargetResolution(new Size(1280, 120)); // 设置目标分辨率

                capturedImg = builder.build();

                processCameraProvider.unbindAll();
                processCameraProvider.bindToLifecycle(this, cameraSelector, preview, capturedImg);
                overlayImageView.setOnClickListener(v -> {
                    if (overlayImageView.getVisibility() == View.VISIBLE) {
                        overlayImageView.setVisibility(View.GONE); // 点击后隐藏
                    }
                });

                captureButton.setOnClickListener(v -> capturePhoto());

                backButton.setOnClickListener(v -> {

                    finish(); // 结束当前活动
                });

                // Check "inOutFlag" and show button/seekBar for "out"

                selectPhotoButton.setVisibility(View.VISIBLE);
                seekBar.setVisibility(View.VISIBLE);

                // Select photo button click listener
                selectPhotoButton.setOnClickListener(v -> choosePhoto());

                // SeekBar listener to adjust transparency
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float alpha = progress / 100f; // Convert to a fraction
                        overlayImageView.setAlpha(alpha); // Set transparency
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {}
                });
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

//                croppedImg = cropBitmapToFrame(bitmap, captureFrame);

                runOnUiThread(() -> toCheckView(bitmap));

                image.close();

//                Toast.makeText(CameraActivity.this, "Captured and cropped image obtained", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });
    }

    private void toCheckView(Bitmap pic){
        setContentView(R.layout.captured_photo_check);
        imageView = findViewById(R.id.previewImageView);
        int rotationDegrees = getWindowManager().getDefaultDisplay().getRotation();
        Bitmap picForDisplay = null;
        if(rotationDegrees==0){
            picForDisplay = Bitmap.createScaledBitmap(pic, screenHW.get("width"), (int)(screenHW.get("width") * CAMERA_PREVIEW_HW_RATIO), true);
        }else{
            picForDisplay = Bitmap.createScaledBitmap(pic, (int)(screenHW.get("height")* CAMERA_PREVIEW_HW_RATIO), screenHW.get("height"), true);
        }

        imageView.setImageBitmap(picForDisplay);


        Button confirmButton = findViewById(R.id.confirmButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        confirmButton.setOnClickListener(v -> {
            saveImage(bitmapToByteArray(pic));
        });


        cancelButton.setOnClickListener(v -> {
            setContentView(R.layout.activity_camera_out);

            // 重新获取视图对象
            cameraPreview = findViewById(R.id.cameraPreview);
            captureButton = findViewById(R.id.captureButton_1);
            backButton = findViewById(R.id.backButton);
            overlayImageView = findViewById(R.id.overlayImageView);
            selectPhotoButton = findViewById(R.id.selectPhotoButton);
            seekBar = findViewById(R.id.seekBar);
            // 重新启动相机
            startCamera();

            // 设置按钮点击事件
            captureButton.setOnClickListener(v1 -> capturePhoto());
            backButton.setOnClickListener(v2 -> finish());
        });
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
//        int rotationDegrees = image.getImageInfo().getRotationDegrees();

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
//        return rotateBitmap(bitmap, rotationDegrees);
    }

    // 获取屏幕宽高（pixel）
    public void getScreenHW(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        screenHW.put("height", displayMetrics.heightPixels);
        screenHW.put("width", displayMetrics.widthPixels);
    }

    private void saveImage(byte[] data) {
        String deviceFolderName = "Device_temp";
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);

        if (!storageDir.exists() && !storageDir.mkdirs()) {
            return;
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File imageFile = new File(storageDir, fileName);
        photoPath = imageFile.getAbsolutePath();
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("photoPath", photoPath);
        setResult(1, resultIntent);
        finish();
    }

    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 将 Bitmap 压缩为 PNG 格式，质量为 100（无损压缩）
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

}
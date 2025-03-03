package com.example.inandouttool_identification.activities;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
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
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.example.inandouttool_identification.R;
import com.example.inandouttool_identification.utils.Constants;
import com.google.common.util.concurrent.ListenableFuture;
import com.example.inandouttool_identification.utils.AutoRecog;

import org.json.JSONException;
import org.json.JSONObject;


public class CameraActivity extends AppCompatActivity {
    private PreviewView cameraPreview;
    private Button captureButton;
    private Button backButton;
    private ImageView imageView;
    private RelativeLayout loadingView; // 加载动画视图

    private String photoPath;
    private Map<String,String> autoRecogRes;
    private String w_id = null;

    private ImageCapture capturedImg;
    private View captureFrame;
    private Bitmap croppedImg;
    private HashMap<String, Integer> screenHW = new HashMap<>();


    private float CAMERA_PREVIEW_HW_RATIO = (float)1.33;
    private float FRAME_CAMERAPREVIEW_RATIO = (float) 0.8;
    private float CAPTURE_FRAME_HW_RATIO = (float) 1.4;
    private float MAX_HEIGHT_RATIO = (float) 3.5/4;
    private AutoRecog autoRecog = new AutoRecog("/face_recog");

    private int CAMERA_PREVIEW_HEIGHT = -1;
    private int CAMERA_PREVIEW_WIDTH = -1;
    private ProcessCameraProvider processCameraProvider;

    private String name;
    private String wid;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPreview = findViewById(R.id.cameraPreview);

        captureFrame = findViewById(R.id.captureFrame);
        captureButton = findViewById(R.id.captureButton_1);
        backButton = findViewById(R.id.backButton);
        loadingView = findViewById(R.id.loadingView_1);

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

        captureButton.setOnClickListener(v -> capturePhoto());
        backButton.setOnClickListener(v -> finish());
    }


    // 根据预定比例常量重塑View组件
    private void reshapeView(View view, int height, int width, float hwRatio, int rotationDegree){
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        if (rotationDegree == 0) {
            params.width = width;
            params.height = (int) (width * hwRatio);
        }else{
            params.height = height;
            params.width = (int) (height * hwRatio);
        }
        view.setLayoutParams(params);
        view.requestLayout();

        System.out.println("123");
    }


    private void startCamera() {
        captureFrame.setVisibility(View.GONE);
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        int rotationDegrees = getWindowManager().getDefaultDisplay().getRotation();
        rotationDegrees = rotationDegrees % 2;
        if (CAMERA_PREVIEW_HW_RATIO != -1){
            getScreenHW(this);
            reshapeView(cameraPreview, screenHW.get("height") , screenHW.get("width"), CAMERA_PREVIEW_HW_RATIO, rotationDegrees);
            reshapeView(captureFrame, (int)(screenHW.get("height") * FRAME_CAMERAPREVIEW_RATIO) , (int)(screenHW.get("width")* FRAME_CAMERAPREVIEW_RATIO), CAPTURE_FRAME_HW_RATIO, rotationDegrees);
            captureFrame.setVisibility(View.VISIBLE);
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
                            CAMERA_PREVIEW_HW_RATIO = (float) image.getHeight() / image.getWidth();
                            if(CAMERA_PREVIEW_HW_RATIO < 1.){
                                CAMERA_PREVIEW_HW_RATIO = 1 / CAMERA_PREVIEW_HW_RATIO;
                            }

                            int rotationDegrees = getWindowManager().getDefaultDisplay().getRotation() % 2;
                            reshapeView(cameraPreview, screenHW.get("height") , screenHW.get("width"), CAMERA_PREVIEW_HW_RATIO, rotationDegrees);
                            reshapeView(captureFrame, (int)(screenHW.get("height") * FRAME_CAMERAPREVIEW_RATIO) , (int)(screenHW.get("width")* FRAME_CAMERAPREVIEW_RATIO), CAPTURE_FRAME_HW_RATIO, rotationDegrees);
                            captureFrame.setVisibility(View.VISIBLE);
                            image.close();


                            processCameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, preview);
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            exception.printStackTrace();
                        }
                    });
                } else {
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

        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        return bitmap;
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
        int rotationDegrees = getWindowManager().getDefaultDisplay().getRotation() % 2;

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

        int startX = frameX - preFrameX;
        int startY = frameY - preFrameY;
        // 返回截取结果
//        return original;
        if(rotationDegrees==0){
            return Bitmap.createBitmap(scalaredImg, startX, startY, frameWidth, frameHeight);
        }else{
            return Bitmap.createBitmap(scalaredImg, startX, startY, frameWidth, frameHeight);
        }
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
            showLoading();
            saveImage(bitmapToByteArray(pic));
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
    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE); // 显示加载动画
    }
    private void hideLoading() {
        loadingView.setVisibility(View.GONE); // 隐藏加载动画
    }


    private void saveImage(byte[] data) {
        String imageType = getIntent().getStringExtra("imageType");
        String imagePre = imageType + "_";

        assert imageType != null;
        if (imageType.equals("autoRecog")){
            new Thread(() -> {
                try {
                    JSONObject jsonObject = autoRecog.getRecognitionResult(data);
                    name = (String) jsonObject.get("name");
                    wid = (String) jsonObject.get("workid");
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                runOnUiThread(() -> {
                    Intent resultIntent = new Intent();
                    name = Constants.aa.get(name);
                    resultIntent.putExtra("autoRecogResName", name);
                    resultIntent.putExtra("autoRecogResId", wid);
                    setResult(2, resultIntent);
                    finish();
                });
            }).start();
        }else{
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (!storageDir.exists() && !storageDir.mkdirs()) {
                return;
            }
            String fileName = imagePre + w_id + ".jpg";
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
    }


    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 将 Bitmap 压缩为 PNG 格式，质量为 100（无损压缩）
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

}
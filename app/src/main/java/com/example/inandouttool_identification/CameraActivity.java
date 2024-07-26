package com.example.inandouttool_identification;
import android.os.Environment;
import android.widget.RelativeLayout;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLOutput;
import java.util.Arrays;

import android.hardware.camera2.*;
import android.widget.Toast;

public class CameraActivity extends AppCompatActivity {
    private SurfaceView cameraPreview;
    private CameraDevice cameraDevice;
    private static final int REQUEST_CAMERA_PERMISSION = 200;

    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader imageReader;
    private Button captureButton;
    private Button backButton;
    private ImageView imageView;
    private CameraCaptureSession captureSession;
    private Size highestResolution;
    private Bitmap capturedImageTobeCheck;
    private String photoPath;
    private String w_id = null;
    private CameraHelper cameraHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraPreview = findViewById(R.id.cameraPreview);
        captureButton = findViewById(R.id.captureButton_1);
        backButton = findViewById(R.id.backButton);


        String workerId = getIntent().getStringExtra("workerId");
        if (workerId != null) {
            w_id = workerId;
        }


        SurfaceHolder holder = cameraPreview.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    setupCamera();
                    } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                // 更改该surface的layout
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                if (cameraCaptureSession != null) {
                    cameraCaptureSession.close();
                    cameraCaptureSession = null;
                }
                if (cameraDevice != null) {
                    cameraDevice.close();
                    cameraDevice = null;
                }
            }
        });

        captureButton.setOnClickListener(v -> takePicture());
        backButton.setOnClickListener(v -> finish());
    }

    public void setupCamera() throws CameraAccessException {

        cameraHelper = new CameraHelper();
        cameraHelper.adjustCameraPreview(getScreenWidth(this));


        // 权限校验以及控制
        boolean banCapturing = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;

        if (banCapturing) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            finish();
        }


        CameraManager cameraManager = cameraHelper.getCameraManager();
        cameraManager.openCamera(cameraHelper.getCameraIdFacingBackId(), new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                // 相机已打开，保存相机设备实例
                cameraDevice = camera;
                createCameraSession();

                Toast.makeText(CameraActivity.this, String.valueOf(getCameraSensorOrientation()), Toast.LENGTH_SHORT).show();

//                Toast.makeText(CameraActivity.this, "viewW: " + cameraPreview.getWidth() + ", viewH" + cameraPreview.getHeight(), Toast.LENGTH_SHORT).show();
//                Toast.makeText(CameraActivity.this, "cameraW" + cameraHelper.getResolution().getHeight() + ", cameraH: " + cameraHelper.getResolution().getWidth(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                // 断开连接时关闭相机
                camera.close();
                cameraDevice = null;
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                // 发生错误时关闭相机
                camera.close();
                cameraDevice = null;
            }
        }, null); // 使用默认的 Handler

    }


    private void createCameraSession() {
        // 获取 SurfaceHolder 的 Surface, 预览
        SurfaceHolder holder = cameraPreview.getHolder();
        Surface previewSurface = holder.getSurface();

        holder.setSizeFromLayout();
        // 创建ImageReader， 拍照
        imageReader = ImageReader.newInstance(cameraPreview.getWidth(), cameraPreview.getHeight(), ImageFormat.JPEG, 1);
        imageReader.setOnImageAvailableListener(reader -> {
            Image image = null;
            try {
                image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Matrix matrix = new Matrix();
                matrix.postRotate(calRotateDegree());

                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                Bitmap stretchedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, reader.getWidth(), reader.getHeight(), true);


                View captureFrame = findViewById(R.id.captureFrame);
                int[] location = new int[2];
                captureFrame.getLocationOnScreen(location);
                int frameX = location[0];
                int frameY = location[1];
                int frameWidth = captureFrame.getWidth();
                int frameHeight = captureFrame.getHeight();

                int[] previewLoc = new int[2];
                cameraPreview.getLocationOnScreen(previewLoc);
                int preFrameX = previewLoc[0];
                int preFrameY = previewLoc[1];


                Bitmap croppedBitmap = Bitmap.createBitmap(stretchedBitmap, frameX-preFrameX, frameY-preFrameY, frameWidth, frameHeight);

                runOnUiThread(() -> toCheckView(croppedBitmap));
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        }, null);


        // 创建捕捉会话
        try{
            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session){
                    captureSession = session;

                    CaptureRequest.Builder previewRequestBuilder = null;
                    try {
                        previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        previewRequestBuilder.addTarget(previewSurface);

                        captureSession.setRepeatingRequest(previewRequestBuilder.build(), null, null);
                    }catch(CameraAccessException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session){
                    // 会话创建失败
                }

            }, null);
        }catch(CameraAccessException e){
            e.printStackTrace();
        }
    }


    private void takePicture(){
        if (cameraDevice == null) return;

        try {
            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());

            CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
//                    Toast.makeText(CameraActivity.this, "Image Captured", Toast.LENGTH_SHORT).show();
                }
            };

            captureSession.capture(captureBuilder.build(), captureListener, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    public int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }


    private void toCheckView(Bitmap pic){
        setContentView(R.layout.captured_photo_check);
        imageView = findViewById(R.id.previewImageView);


        float radio = (float) pic.getHeight() / pic.getWidth();
        ViewGroup.LayoutParams params = imageView.getLayoutParams();

        // 更新布局参数并设置图像
        params.width = getScreenWidth(this);
        params.height = (int) (params.width * radio);

        imageView.setLayoutParams(params);
        imageView.setImageBitmap(pic);


        // 确保 ImageView 垂直居中
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        imageView.setLayoutParams(layoutParams);


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
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        });
    }


    private int getCameraSensorOrientation() {
        try {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
            return characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }


    private int rotationToDegrees(int rotation) {
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                return 0;
        }
    }


    private int calRotateDegree(){
        int sensorOrientation = getCameraSensorOrientation();

        int rotation = getWindowManager().getDefaultDisplay().getRotation();


        return sensorOrientation - rotationToDegrees(rotation);
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


    // 初始化相机信息
    private class CameraHelper {

        private final CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        private String cameraIdFacingBackId;

        private Size resolution;


        // 设置摄像头朝向
        public void setCameraToFaceBack() throws CameraAccessException {
            String[] cameraIds = cameraManager.getCameraIdList();

            for (String cameraId : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    this.cameraIdFacingBackId = cameraId;
                    break; // 找到后置摄像头后，停止搜索
                }
            }

            if (this.cameraIdFacingBackId == null) {
                throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "后置摄像头未找到");
            }
        }


        private Size getHighestResolution(Size[] outputSizes) {
            Size maxSize = outputSizes[0];
            for (Size size : outputSizes) {
                if (size.getWidth() * size.getHeight() > maxSize.getWidth() * maxSize.getHeight()) {
                    maxSize = size;
                }
            }
            return maxSize;
        }


        // 获取摄像头的分辨率
        public void setCameraResolution() throws CameraAccessException {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(this.cameraIdFacingBackId);

            this.resolution = getHighestResolution(characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG));
        }


        public void adjustCameraPreview(int screenWidth){
            int width = resolution.getWidth();

            int height = resolution.getHeight();

            float radio = (float)height/width;

            int viewHeight = (int)(screenWidth * radio);


            // 确保 cameraPreview 垂直居中
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) cameraPreview.getLayoutParams();
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            cameraPreview.setLayoutParams(layoutParams);


            // 确保设置正确的 LayoutParams 类型
            ViewGroup.LayoutParams params =(RelativeLayout.LayoutParams) cameraPreview.getLayoutParams();
            params.width = screenWidth;
            params.height = viewHeight;
            cameraPreview.setLayoutParams(params);

        }




        // 初始化相机信息
        public CameraHelper() throws CameraAccessException {
            // 设置摄像头为后置摄像头
            setCameraToFaceBack();

            // 获取摄像设备的分辨率
            setCameraResolution();

        }

        public Size getResolution() {
            return resolution;
        }

        public String getCameraIdFacingBackId(){
            return this.cameraIdFacingBackId;
        }

        public CameraManager getCameraManager(){
            return this.cameraManager;
        }
    }
}
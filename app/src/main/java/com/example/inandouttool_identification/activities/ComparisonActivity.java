package com.example.inandouttool_identification.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.inandouttool_identification.httpConnetection.HttpRequest;
import com.example.inandouttool_identification.R;
import com.example.inandouttool_identification.entity.Worker;
import com.example.inandouttool_identification.database.DatabaseHelper;
import com.example.inandouttool_identification.utils.ImageProcess;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ComparisonActivity extends AppCompatActivity {
    private Button compareButton, captureButton,backButton;
    private ImageView workerImageView_IN;
    private ImageView workerImageView_OUT;
    private Worker worker;
    private static List<Worker> workerList = new ArrayList<>();
    String photoPath_OUT = "";
    Boolean Consistent_flags;
    private RelativeLayout loadingView; // 加载动画视图
    private DatabaseHelper databaseHelper;

    private final ImageProcess imageProcessor = new ImageProcess();
    // 得到的的处理后的图片
    String image1Base64 = null;
    String image2Base64 = null;

    String imageInCheckedPath = null;
    String imageOutCheckedPath = null;
    Bundle bundle_In = new Bundle();
    Bundle bundle_Out = new Bundle();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);
        compareButton = findViewById(R.id.compareButton);
        captureButton = findViewById(R.id.captureButton);
        workerImageView_IN = findViewById(R.id.workerImageView_IN);
        workerImageView_OUT = findViewById(R.id.workerImageView_OUT);
        worker = (Worker) getIntent().getSerializableExtra("worker"); // Keep using Serializable
        workerList = (List<Worker>) getIntent().getSerializableExtra("workerList");
        backButton = findViewById(R.id.backButton);
        databaseHelper = new DatabaseHelper(this);
        //返回按钮
        backButton.setOnClickListener(v -> {
            finish();
        });
        // 获取加载视图
        loadingView = findViewById(R.id.loadingView);
        String photoPath_IN = worker.getPhotoPath_IN();
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath_IN);
        workerImageView_IN.post(() -> {
            Bitmap adjustedBitmap = imageProcessor.adjustImageSize(bitmap, workerImageView_IN);
            workerImageView_IN.setImageBitmap(adjustedBitmap);
        });

        // Capture button functionality
        captureButton.setOnClickListener(v -> {
            Intent intent = new Intent(ComparisonActivity.this, CameraActivity.class);
            intent.putExtra("workerId", worker.getId());
            intent.putExtra("imageType", "out");
            startActivityForResult(intent, 1);
        });

        // Compare button functionality
        compareButton.setOnClickListener(v -> {
            if (photoPath_OUT.isEmpty()) {
                Toast.makeText(this, "请先拍摄照片", Toast.LENGTH_SHORT).show();
            } else {
                // 显示加载动画
                showLoading();
                new Thread(() -> {


                    HttpRequest httpRequest = new HttpRequest("/process_image");
                    String photoPathIn = worker.getPhotoPath_IN();
                    String photoPathOut = worker.getPhotoPath_OUT();

                    ArrayList<Map<String, Object>> result1 = null;
                    ArrayList<Map<String, Object>> result2 = null;

                    try {
                        String response = httpRequest.getCompareResult(photoPathIn, photoPathOut);
                        ObjectMapper objectMapper = new ObjectMapper();
                        Map<String, Object> responseJson = objectMapper.readValue(response, new TypeReference<Map<String, Object>>(){});

                        image1Base64 = Objects.requireNonNull(responseJson.get("image_base64_1")).toString();
                        image2Base64 = Objects.requireNonNull(responseJson.get("image_base64_2")).toString();
                        String image1CheckedPath = worker.getId() + "_IN_Checked.jpg";
                        String image2CheckedPath = worker.getId() + "_OUT_Checked.jpg";
                        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        imageInCheckedPath = new File(storageDir, image1CheckedPath).getAbsolutePath();
                        imageOutCheckedPath = new File(storageDir, image2CheckedPath).getAbsolutePath();
                        imageProcessor.saveBase64ToFile(image1Base64, imageInCheckedPath);
                        imageProcessor.saveBase64ToFile(image2Base64, imageOutCheckedPath);

                        result1 = (ArrayList<Map<String, Object>>) responseJson.get("result1");
                        result2 = (ArrayList<Map<String, Object>>) responseJson.get("result2");

                        if (result1 != null){
                            for (Map<String, Object> result: result1) {
                                String className = result.get("class_name").toString();
                                int num = Integer.parseInt(result.get("count").toString());
                                bundle_In.putInt(className, num);
                            }
                        }
                        if (result2!=null){
                            for (Map<String, Object> result:
                                    result2) {
                                String className = result.get("class_name").toString();
                                int num = Integer.parseInt(result.get("count").toString());
                                bundle_Out.putInt(className, num);
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    // 处理结束后，更新UI
                    runOnUiThread(() -> {
                        hideLoading();
                        // 准备跳转到新页面
                        Intent intent = new Intent(ComparisonActivity.this, ToolCheckActivity.class);
                        if(worker.getId().equals("000")){
                            intent.putExtra("tools_IN", bundle_In);
                            intent.putExtra("tools_OUT", bundle_Out);
                        }else{
                            intent.putExtra("tools_IN", bundle_In);
                            intent.putExtra("tools_OUT", bundle_Out);
                            intent.putExtra("in_checked_path", imageInCheckedPath);
                            intent.putExtra("out_checked_path", imageOutCheckedPath);
                        }
                        startActivityForResult(intent, 2);
                        // 这里处理工具比较的逻辑
//                        boolean toolsMatch = true; // 这应为您的实际比较结果
//                        if (toolsMatch) {
//                            Toast.makeText(this, "工具一致", Toast.LENGTH_SHORT).show();
//                            Intent resultIntent = new Intent();
//                            resultIntent.putExtra("worker", worker);
//                            setResult(RESULT_OK, resultIntent);
//                        } else {
//                            Toast.makeText(this, "工具不一致", Toast.LENGTH_SHORT).show();
//                        }
//                        finish(); // 结束当前活动
                    });
                }).start();
            }
        });
    }

    private void showLoading() {
        loadingView.setVisibility(View.VISIBLE); // 显示加载动画
    }

    private void hideLoading() {
        loadingView.setVisibility(View.GONE); // 隐藏加载动画
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            photoPath_OUT = data.getStringExtra("photoPath");
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath_OUT);
            workerImageView_OUT.setImageBitmap(bitmap);
            worker.setPhotoPath_OUT(photoPath_OUT);
            databaseHelper.updateWorker(worker.getId(), worker.getName(), worker.getPhotoPath_IN(), worker.getPhotoPath_OUT());
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Consistent_flags = data.getBooleanExtra("Consistent_flags", false);
            bundle_In.clear();
            bundle_Out.clear();
            if(Consistent_flags){
                Intent resultIntent = new Intent();
                resultIntent.putExtra("worker", worker);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }
    }


}
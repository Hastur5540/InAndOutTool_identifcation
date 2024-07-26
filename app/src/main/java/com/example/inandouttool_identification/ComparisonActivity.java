package com.example.inandouttool_identification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComparisonActivity extends AppCompatActivity {
    private EditText toolsInput;
    private Button compareButton, captureButton,backButton;
    private ImageView workerImageView_IN;
    private ImageView workerImageView_OUT;
    private Worker worker;
    private static List<Worker> workerList = new ArrayList<>();
    String photoPath_OUT = "";
    Boolean Consistent_flags;
    private RelativeLayout loadingView; // 加载动画视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);

        toolsInput = findViewById(R.id.toolsInput);
        compareButton = findViewById(R.id.compareButton);
        captureButton = findViewById(R.id.captureButton);
        workerImageView_IN = findViewById(R.id.workerImageView_IN);
        workerImageView_OUT = findViewById(R.id.workerImageView_OUT);
        worker = (Worker) getIntent().getSerializableExtra("worker"); // Keep using Serializable
        workerList = (List<Worker>) getIntent().getSerializableExtra("workerList");
        //返回按钮
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            worker.setPhotoPath_OUT(photoPath_OUT);
            Intent intent = new Intent(ComparisonActivity.this, CameraActivity.class);
            intent.putExtra("workerList", new ArrayList<>(workerList));
            finish();
        });
        // 获取加载视图
        loadingView = findViewById(R.id.loadingView);
        toolsInput.setText(worker.getId());
        String photoPath_IN = worker.getPhotoPath_IN();
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath_IN);
        workerImageView_IN.setImageBitmap(bitmap);

        // Capture button functionality
        captureButton.setOnClickListener(v -> {
            Intent intent = new Intent(ComparisonActivity.this, CameraActivity.class);
            intent.putExtra("workerId", worker.getId());
            intent.putExtra("inOutFlag", "out");
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
                    // 模拟处理延时
                    try {
                        Thread.sleep(2000); // 实际比较逻辑将放置在这里
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //后端返回的数据
                    Bundle bundle_IN = new Bundle();
                    bundle_IN.putInt("卡簧", 5);
                    bundle_IN.putInt("一字螺丝刀", 3);
                    bundle_IN.putInt("斜口钳", 7);
                    bundle_IN.putInt("插排", 2);
                    bundle_IN.putInt("锉刀", 4);
                    bundle_IN.putInt("橡皮锤", 6);
                    bundle_IN.putInt("活动扳手", 1);
                    bundle_IN.putInt("记号笔", 8);
                    bundle_IN.putInt("多功能万用剪刀", 9);

                    Bundle bundle_OUT = new Bundle();
                    bundle_OUT.putInt("卡簧", 5);
                    bundle_OUT.putInt("一字螺丝刀", 3);
                    bundle_OUT.putInt("斜口钳", 6);
                    bundle_OUT.putInt("插排", 2);
                    bundle_OUT.putInt("锉刀", 4);
                    bundle_OUT.putInt("橡皮锤", 6);
                    bundle_OUT.putInt("活动扳手", 1);
                    bundle_OUT.putInt("记号笔", 8);
                    bundle_OUT.putInt("多功能万用剪刀", 8);

                    // 处理结束后，更新UI
                    runOnUiThread(() -> {
                        hideLoading();
                        // 准备跳转到新页面
                        Intent intent = new Intent(ComparisonActivity.this, ToolCheckActivity.class);
                        if(worker.getId().equals("000")){
                            intent.putExtra("tools_IN", bundle_IN);
                            intent.putExtra("tools_OUT", bundle_OUT);
                        }else{
                            intent.putExtra("tools_IN", bundle_IN);
                            intent.putExtra("tools_OUT", bundle_IN);
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
        if (requestCode == 1 && resultCode == RESULT_OK) {
            photoPath_OUT = data.getStringExtra("photoPath");
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath_OUT);
            workerImageView_OUT.setImageBitmap(bitmap);
            worker.setPhotoPath_OUT(photoPath_OUT);
            for (int i = 0; i < workerList.size(); i++) {
                Worker existingWorker = workerList.get(i);
                if (existingWorker.getId().equals(worker.getId())) { // 根据 ID 匹配
                    workerList.set(i, worker); // 更新列表中的对象
                    break; // 找到后跳出循环
                }
            }
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
            Consistent_flags = data.getBooleanExtra("Consistent_flags", false);
            if(Consistent_flags){
                Intent resultIntent = new Intent();
                resultIntent.putExtra("worker", worker);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        }
    }
}
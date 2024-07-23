package com.example.inandouttool_identification;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ComparisonActivity extends AppCompatActivity {
    private EditText toolsInput;
    private Button compareButton, captureButton;
    private ImageView workerImageView;
    private Worker worker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison);

        toolsInput = findViewById(R.id.toolsInput);
        compareButton = findViewById(R.id.compareButton);
        captureButton = findViewById(R.id.captureButton);

        workerImageView = findViewById(R.id.workerImageView);
        worker = getIntent().getParcelableExtra("worker");

        // 显示工人信息和照片
        toolsInput.setText(worker.id);
        // 加载图片到ImageView，这里需要实现图片加载

        captureButton.setOnClickListener(v -> {
            Intent intent = new Intent(ComparisonActivity.this, CameraActivity.class);
            startActivityForResult(intent, 1);
        });

        compareButton.setOnClickListener(v -> {
            String currentTools = toolsInput.getText().toString();
            //if (currentTools.equals(worker.tools)) {
            if(true){
                Toast.makeText(this, "工具一致", Toast.LENGTH_SHORT).show();
                WorkerListActivity.workerList.remove(worker);
                finish();
            } else {
                Toast.makeText(this, "工具不一致", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // 获取拍照结果，更新照片
        }
    }
}
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
        worker = (Worker) getIntent().getSerializableExtra("worker"); // Keep using Serializable
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        // 显示工人信息和照片
        toolsInput.setText(worker.getId()); // 这里可以更改为工具信息

        // Capture button functionality
        captureButton.setOnClickListener(v -> {
            Intent intent = new Intent(ComparisonActivity.this, CameraActivity.class);
            startActivityForResult(intent, 1);
        });

        // Compare button functionality
        compareButton.setOnClickListener(v -> {
            //String currentTools = toolsInput.getText().toString();
            // 使用 WorkerList 类的静态方法
            if (true) {
                Toast.makeText(this, "工具一致", Toast.LENGTH_SHORT).show();
                WorkerListActivity.removeWorker(worker); // 使用类名调用静态方法
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
            // 获取拍照结果，更新照片路径的逻辑
        }
    }
}
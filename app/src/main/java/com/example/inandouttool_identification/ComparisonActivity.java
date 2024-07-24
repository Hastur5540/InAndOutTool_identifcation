package com.example.inandouttool_identification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ComparisonActivity extends AppCompatActivity {
    private EditText toolsInput;
    private Button compareButton, captureButton;
    private ImageView workerImageView_IN;
    private ImageView workerImageView_OUT;
    private Worker worker;
    String photoPath_OUT = "";

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
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
        // 显示工人信息和进入时拍的照片
        toolsInput.setText(worker.getId()); // 这里可以更改为工具信息
        String photoPath_IN = worker.getPhotoPath_IN();
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath_IN);
        workerImageView_IN.setImageBitmap(bitmap);

        // Capture button functionality
        captureButton.setOnClickListener(v -> {
            Intent intent = new Intent(ComparisonActivity.this, CameraActivity_OUT.class);
            intent.putExtra("workerId", worker.getId());
            startActivityForResult(intent, 1);
        });

        // Compare button functionality
        compareButton.setOnClickListener(v -> {
            //String currentTools = toolsInput.getText().toString();
            if (photoPath_OUT.isEmpty()) {
                Toast.makeText(this, "请先拍摄照片", Toast.LENGTH_SHORT).show();
            } else {
                worker.setPhotoPath_OUT(photoPath_OUT);
                if (true) {
                    Toast.makeText(this, "工具一致", Toast.LENGTH_SHORT).show();
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("worker", worker);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(this, "工具不一致", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            photoPath_OUT = data.getStringExtra("photoPath");
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath_OUT);
            workerImageView_OUT.setImageBitmap(bitmap);
        }
    }
}
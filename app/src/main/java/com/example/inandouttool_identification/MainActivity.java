package com.example.inandouttool_identification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText nameInput, idInput, toolsInput;
    private Button captureButton, submitButton;
    private String photoPath = "";
    private List<Worker> workerList = new ArrayList<>();
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameInput = findViewById(R.id.nameInput);
        idInput = findViewById(R.id.idInput);
        captureButton = findViewById(R.id.buttonCamera);
        submitButton = findViewById(R.id.submitButton);
        imageView = findViewById(R.id.imageView);

        captureButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivityForResult(intent, 1);
        });

        submitButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String id = idInput.getText().toString();
            workerList.add(new Worker(name, id, photoPath));

            // 保存并更新工人列表
            WorkerListActivity.updateWorkerList(workerList);
            Toast.makeText(this, "工人信息已保存！", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            intent.putExtra("workerId", id);
            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            photoPath = data.getStringExtra("photoPath");
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            imageView.setImageBitmap(bitmap);
        }
    }
}
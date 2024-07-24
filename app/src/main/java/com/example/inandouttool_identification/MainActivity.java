package com.example.inandouttool_identification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText nameInput, idInput;
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
            if (validateInputs()) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("workerId", idInput.getText().toString());
                startActivityForResult(intent, 1);
            }
        });

        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                if (photoPath.isEmpty()) {
                    Toast.makeText(this, "请先拍摄照片", Toast.LENGTH_SHORT).show();
                } else {
                    String name = nameInput.getText().toString();
                    String id = idInput.getText().toString();
                    workerList.add(new Worker(name, id, photoPath));

                    // 保存并更新工人列表
                    WorkerListActivity.updateWorkerList(workerList);
                    Toast.makeText(this, "工人信息已保存！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateInputs() {
        if (nameInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "请先输入姓名", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (idInput.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "请先输入工号", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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
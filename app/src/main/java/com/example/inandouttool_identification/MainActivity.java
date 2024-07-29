package com.example.inandouttool_identification;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
    private Button listButton, captureButton, submitButton;
    private String photoPath = "";
    private ImageView imageView;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listButton = findViewById(R.id.listButton);
        nameInput = findViewById(R.id.nameInput);
        idInput = findViewById(R.id.idInput);
        captureButton = findViewById(R.id.buttonCamera);
        submitButton = findViewById(R.id.submitButton);
        imageView = findViewById(R.id.imageView);
        databaseHelper = new DatabaseHelper(this);

        listButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, WorkerListActivity.class);
            startActivity(intent);
        });

        captureButton.setOnClickListener(v -> {
            if (validateInputs()) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("workerId", idInput.getText().toString());
                intent.putExtra("inOutFlag", "in");
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

                    // 检查工号是否已存在
                    if (isWorkerIdExists(id)) {
                        Toast.makeText(this, "工号 " + id + " 已存在", Toast.LENGTH_SHORT).show();
                    } else {
                        databaseHelper.addWorker(name, id, photoPath, null);
                        Toast.makeText(this, "工人信息已保存！", Toast.LENGTH_SHORT).show();
                        clearInputs();
                    }
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

    private void clearInputs() {
        nameInput.setText("");
        idInput.setText("");
        imageView.setImageDrawable(null); // 清空图片显示
        photoPath = ""; // 清空照片路径
    }

    public boolean isWorkerIdExists(String workerId) {
        Worker worker = databaseHelper.getWorkerById(workerId);
        boolean exists = false;
        if(worker!=null){
            exists=true;
        }
        return exists;
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

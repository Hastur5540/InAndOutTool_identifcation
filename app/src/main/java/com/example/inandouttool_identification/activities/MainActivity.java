package com.example.inandouttool_identification.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.widget.ArrayAdapter;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inandouttool_identification.R;
import com.example.inandouttool_identification.entity.Worker;
import com.example.inandouttool_identification.database.DatabaseHelper;
import com.example.inandouttool_identification.utils.ImageProcess;
import com.example.inandouttool_identification.utils.AutoRecog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText nameInput, idInput;
    private Button listButton, captureButton, submitButton;
    private String photoPath = "";
    private ImageView imageView;
    private DatabaseHelper databaseHelper;
    private Spinner entryMethodSpinner;
    private ImageProcess imageProcessor = new ImageProcess();
    private TextView autoEntryName;
    private TextView autoEntryId;
    private String name;
    private String workId;

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

        entryMethodSpinner = findViewById(R.id.entryMethodSpinner);
        autoEntryName = findViewById(R.id.autoEntryName);
        autoEntryId = findViewById(R.id.autoEntryId);

        // 配置下拉框的适配器
        String[] entryMethods = {"手工录入", "自动识别"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, entryMethods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        entryMethodSpinner.setAdapter(adapter);
        entryMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) { // 手工录入
                    nameInput.setVisibility(View.VISIBLE);
                    idInput.setVisibility(View.VISIBLE);
                    autoEntryName.setVisibility(View.GONE);
                    autoEntryId.setVisibility(View.GONE);
                } else if (position == 1) { // 自动识别
                    Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                    intent.putExtra("workerId", idInput.getText().toString());
                    intent.putExtra("imageType", "autoRecog");
                    startActivityForResult(intent, 1);

                    nameInput.setVisibility(View.GONE);
                    idInput.setVisibility(View.GONE);

                    String displayName = "识别结果: \n" + "姓名: " + name;
                    String displayId = "工号: " + workId;
                    autoEntryName.setText(displayName);
                    autoEntryName.setTextSize(20);
                    autoEntryName.setVisibility(View.VISIBLE);

                    autoEntryId.setText(displayId);
                    autoEntryId.setTextSize(20);
                    autoEntryId.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        listButton.setOnClickListener(v -> {
            deleteTempFiles();
            Intent intent = new Intent(MainActivity.this, WorkerListActivity.class);
            startActivity(intent);
        });
        captureButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            intent.putExtra("workerId", idInput.getText().toString());
            intent.putExtra("imageType", "in");
            startActivityForResult(intent, 1);
        });

        submitButton.setOnClickListener(v -> {
            // 检查当前选中的录入方式
            int selectedPosition = entryMethodSpinner.getSelectedItemPosition();

            if (photoPath.isEmpty()) {
                Toast.makeText(this, "请先拍摄照片", Toast.LENGTH_SHORT).show();
            } else if((selectedPosition==0 && validateInputs())){

                name = nameInput.getText().toString();
                workId = idInput.getText().toString();
                // 检查工号是否已存在
                if (isWorkerIdExists(workId)) {
                    saveImage();
                    Toast.makeText(this, "工号 " + workId + " 已存在，添加图片", Toast.LENGTH_SHORT).show();
                } else {
                    databaseHelper.addWorker(name, workId, photoPath, null);
                    saveImage();
                    Toast.makeText(this, "工人信息已保存！", Toast.LENGTH_SHORT).show();
                    clearInputs();
                }
            }else if(selectedPosition==1){
                if (isWorkerIdExists(workId)) {
                    Toast.makeText(this, "工号 " + workId + " 已存在", Toast.LENGTH_SHORT).show();
                } else {
                    databaseHelper.addWorker(name, workId, photoPath, null);
                    Toast.makeText(this, "工人信息已保存！", Toast.LENGTH_SHORT).show();
                    clearInputs();
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

    private void saveImage() {
        // 获取设备ID
        String id = idInput.getText().toString();
        String deviceFolderName = "WorkerID_" + id; // 使用设备ID命名文件夹
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);

        // 创建文件夹，如果不存在则创建
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            return;
        }

        /*
        发送图片到算法端
         */

        /*
        把算法端传回的图片存入“deviceFolderName”目录
         */


        // 复制 Device_temp 下的所有文件到新创建的文件夹
        File tempDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Device_temp");

        if (tempDir.exists() && tempDir.isDirectory()) {
            File[] tempFiles = tempDir.listFiles();
            if (tempFiles != null) {
                for (File tempFile : tempFiles) {
                    // 复制文件到新目录
                    File newFile = new File(storageDir, tempFile.getName());
                    try {
                        copyFile(tempFile, newFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // 调用方法删除 Device_temp 中的所有文件
                deleteTempFiles();
            }
        }
    }

    private void deleteTempFiles() {
        File tempDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Device_temp");
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            return;
        }
        File[] tempFiles = tempDir.listFiles();
        for (File tempFile : tempFiles) {
            if (tempFile.delete()) {
                // 成功删除文件
                System.out.println("Deleted: " + tempFile.getName());
            } else {
                // 删除失败
                System.out.println("Failed to delete: " + tempFile.getName());
            }
        }
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        try (InputStream in = new FileInputStream(sourceFile);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            photoPath = data.getStringExtra("photoPath");
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            Bitmap adjusted_image = imageProcessor.adjustImageSize(bitmap, imageView);
            imageView.setImageBitmap(adjusted_image);
        } else if (resultCode == 2) {
            name = data.getStringExtra("autoRecogResName");
            workId = data.getStringExtra("autoRecogResId");

            String displayName = "识别结果: \n" + "姓名: " + name;
            String displayId = "工号: " + workId;
            autoEntryName.setText(displayName);
            autoEntryName.setTextSize(20);
            autoEntryName.setVisibility(View.VISIBLE);

            autoEntryId.setText(displayId);
            autoEntryId.setTextSize(20);
            autoEntryId.setVisibility(View.VISIBLE);
        }
    }
}

package com.example.inandouttool_identification;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorkerListActivity extends AppCompatActivity {
    private static List<Worker> workerList = new ArrayList<>();
    private ArrayAdapter<Worker> adapter;
    private Button backButton, searchButton;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_list);

        ListView workerListView = findViewById(R.id.workerListView);
        searchInput = findViewById(R.id.searchInput);
        backButton = findViewById(R.id.backButton);
        searchButton = findViewById(R.id.searchButton); // 新增搜索按钮

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, workerList);
        workerListView.setAdapter(adapter);

        // 从 Intent 获取工人列表
        if (getIntent().hasExtra("workerList")) {
            List<Worker> receivedWorkers = (List<Worker>) getIntent().getSerializableExtra("workerList");
            updateWorkerList(receivedWorkers);
        }

        // 返回按钮点击事件
        backButton.setOnClickListener(v -> finish());

        // 搜索按钮点击事件
        searchButton.setOnClickListener(v -> performSearch());

        // 列表项点击事件
        workerListView.setOnItemClickListener((parent, view, position, id) -> {
            Worker selectedWorker = workerList.get(position);
            startComparisonActivity(selectedWorker);
        });

        // 搜索框文本变化监听
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 实时搜索，这里可以保留
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // 执行搜索
    private void performSearch() {
        String searchQuery = searchInput.getText().toString().trim();
        for (Worker worker : workerList) {
            if (worker.getId().equals(searchQuery)) {
                startComparisonActivity(worker);
                return;
            }
        }
        // 若未找到
        Toast.makeText(this, "无此人", Toast.LENGTH_SHORT).show();
    }

    // 跳转到比较活动
    private void startComparisonActivity(Worker worker) {
        Intent intent = new Intent(WorkerListActivity.this, ComparisonActivity.class);
        intent.putExtra("worker", worker);
        startActivityForResult(intent,1);
    }

    // 更新工人列表
    public void updateWorkerList(List<Worker> workers) {
        workerList.clear();
        if (workers != null) {
            workerList.addAll(workers);
        }
        adapter.notifyDataSetChanged();
    }

    private void deletePhoto(String photoPath) {
        File photoFile = new File(photoPath);
        if (photoFile.exists()) {
            boolean deleted = photoFile.delete();
            if (deleted) {
                Log.d("WorkerList", "Deleted photo: " + photoPath);
            } else {
                Log.d("WorkerList", "Failed to delete photo: " + photoPath);
            }
        }
    }

    //移除工人并刷新列表
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.d("WorkerList", "Before removal: " + workerList.toString());
            Worker worker = (Worker) data.getSerializableExtra("worker");
            if (worker != null) {
                String photoPath_IN = worker.getPhotoPath_IN();
                String photoPath_OUT = worker.getPhotoPath_OUT();
                deletePhoto(photoPath_IN);
                if (photoPath_OUT != null && !photoPath_OUT.isEmpty()) {
                    deletePhoto(photoPath_OUT);
                }
            }
            workerList.remove(worker);
            Log.d("WorkerList", "After removal: " + workerList.toString());
            adapter.notifyDataSetChanged();
        }
    }

}
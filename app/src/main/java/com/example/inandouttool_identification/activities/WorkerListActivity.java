package com.example.inandouttool_identification.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.inandouttool_identification.R;
import com.example.inandouttool_identification.database.DatabaseHelper;
import com.example.inandouttool_identification.entity.Worker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorkerListActivity extends AppCompatActivity {
    private static List<Worker> workerList = new ArrayList<>();
    private ArrayAdapter<Worker> adapter;
    private Button backButton, searchButton;
    private EditText searchInput;
    private DatabaseHelper databaseHelper;
    private ListView workerListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_list);

        workerListView = findViewById(R.id.workerListView);
        searchInput = findViewById(R.id.searchInput);
        backButton = findViewById(R.id.backButton);
        searchButton = findViewById(R.id.searchButton);

        databaseHelper = new DatabaseHelper(this);
        workerList = databaseHelper.getAllWorkers(); // 从数据库获取工人信息
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, workerList);
        workerListView.setAdapter(adapter);

        // 返回按钮点击事件
        backButton.setOnClickListener(v -> {
            finish();
        });

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
        Worker worker = databaseHelper.getWorkerById(searchQuery); // 根据需查询的工人ID传入参数
        if (worker != null) {
            startComparisonActivity(worker);
            return;
        }
        Toast.makeText(this, "无此人", Toast.LENGTH_SHORT).show();
    }

    // 跳转到比较活动
    private void startComparisonActivity(Worker worker) {
        Intent intent = new Intent(WorkerListActivity.this, ComparisonActivity.class);
        intent.putExtra("worker", worker);
        intent.putExtra("workerList", new ArrayList<>(workerList));
        startActivityForResult(intent,1);
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
            Worker worker = (Worker) data.getSerializableExtra("worker");
            if (worker != null) {
                String photoPath_IN = worker.getPhotoPath_IN();
                String photoPath_OUT = worker.getPhotoPath_OUT();
                deletePhoto(photoPath_IN);
                deletePhoto(photoPath_OUT);
            }
            databaseHelper.deleteWorker(worker.getId());
            workerList.remove(worker);
            adapter.notifyDataSetChanged();
        }
    }
}
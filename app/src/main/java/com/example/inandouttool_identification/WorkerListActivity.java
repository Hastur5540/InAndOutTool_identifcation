package com.example.inandouttool_identification;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
        startActivity(intent);
    }

    // 更新工人列表
    public void updateWorkerList(List<Worker> workers) {
        workerList.clear();
        if (workers != null) {
            workerList.addAll(workers);
        }
        adapter.notifyDataSetChanged();
    }

    // 静态方法，用于移除工人
    public static void removeWorker(Worker worker) {
        workerList.remove(worker);
    }
}
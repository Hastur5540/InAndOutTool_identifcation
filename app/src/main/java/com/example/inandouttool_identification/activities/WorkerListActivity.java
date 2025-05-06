package com.example.inandouttool_identification.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
    private ListView WorkerListView;
    private Button currentVisibleDeleteButton = null;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_list);

        WorkerListView = findViewById(R.id.workerListView);
        searchInput = findViewById(R.id.searchInput);
        backButton = findViewById(R.id.backButton);
        searchButton = findViewById(R.id.searchButton);

        databaseHelper = new DatabaseHelper(this);
        workerList = databaseHelper.getAllWorkers();
        adapter = new ArrayAdapter<Worker>(this, R.layout.list_item, R.id.text_item, workerList) {
            @NonNull
            @Override
            public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.text_item);
                Button deleteButton = view.findViewById(R.id.deleteButton_1);

                // 设置设备信息显示
                Worker worker = getItem(position);
                if (worker != null) {
                    textView.setText("工号："+worker.getId());
                }

                // 初始化隐藏删除按钮
                deleteButton.setVisibility(View.INVISIBLE);
                return view;
            }
        };
        WorkerListView.setAdapter(adapter);

        // 返回按钮点击事件
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(WorkerListActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 搜索按钮点击事件
        searchButton.setOnClickListener(v -> performSearch());

        // 列表项点击事件
        WorkerListView.setOnItemClickListener((parent, view, position, id) -> {
            Worker selectedWorker = workerList.get(position);
            startComparisonActivity(selectedWorker);
        });
        // 处理长按事件
        WorkerListView.setOnItemLongClickListener((parent, view, position, id) -> {
            // 隐藏之前显示的删除按钮
            if (currentVisibleDeleteButton != null) {
                currentVisibleDeleteButton.setVisibility(View.INVISIBLE);
            }

            // 获取当前列表项的删除按钮
            currentVisibleDeleteButton = view.findViewById(R.id.deleteButton_1);
            currentVisibleDeleteButton.setVisibility(View.VISIBLE);

            // 设置全局触摸监听
            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            rootView.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // 转换按钮位置到屏幕坐标
                    int[] location = new int[2];
                    currentVisibleDeleteButton.getLocationOnScreen(location);

                    // 判断点击位置是否在按钮范围内
                    float x = event.getRawX();
                    float y = event.getRawY();
                    boolean isInside = x >= location[0] &&
                            x <= (location[0] + currentVisibleDeleteButton.getWidth()) &&
                            y >= location[1] &&
                            y <= (location[1] + currentVisibleDeleteButton.getHeight());

                    if (!isInside) {
                        currentVisibleDeleteButton.setVisibility(View.INVISIBLE);
                        currentVisibleDeleteButton = null;
                        rootView.setOnTouchListener(null); // 移除全局监听
                    }
                }
                return false;
            });

            // 删除按钮点击监听
            currentVisibleDeleteButton.setOnClickListener(v -> {
                deleteWorker(position);
                currentVisibleDeleteButton.setVisibility(View.INVISIBLE);
                currentVisibleDeleteButton = null;
                rootView.setOnTouchListener(null); // 移除全局监听
            });

            return true;
        });

// 修改列表项点击监听（添加隐藏按钮逻辑）
        WorkerListView.setOnItemClickListener((parent, view, position, id) -> {
            if (currentVisibleDeleteButton != null) {
                currentVisibleDeleteButton.setVisibility(View.INVISIBLE);
                currentVisibleDeleteButton = null;
                getWindow().getDecorView().findViewById(android.R.id.content)
                        .setOnTouchListener(null);
            }
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
        Worker Worker = databaseHelper.getWorkerById(searchQuery); // 根据需查询的工人ID传入参数
        if (Worker != null) {
            startComparisonActivity(Worker);
            return;
        }
        Toast.makeText(this, "无此人", Toast.LENGTH_SHORT).show();
    }

    // 跳转到比较活动
    private void startComparisonActivity(Worker Worker) {
        Intent intent = new Intent(WorkerListActivity.this, ComparisonActivity.class);
        intent.putExtra("worker", Worker);
        intent.putExtra("workerList", new ArrayList<>(workerList));
        startActivityForResult(intent,1);
    }

    private void deleteWorker(int position) {
        Worker Worker = workerList.get(position);
        String id = Worker.getId();
        File tempDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "WorkerID_"+id);
        File[] tempFiles = tempDir.listFiles();
        for (File tempFile : tempFiles) {
            if (tempFile.delete()) {
                // 成功删除文件
                Log.d("WorkerList", "Deleted photo: "+ tempFile.getName());
            } else {
                // 删除失败
                Log.d("WorkerList", "Failed to delete: " + tempFile.getName());
            }
        }
        databaseHelper.deleteWorker(id); // 从数据库中删除
        workerList.remove(position); // 从列表中删除
        adapter.notifyDataSetChanged(); // 更新适配器
        Toast.makeText(this, "工号"+id+"已删除", Toast.LENGTH_SHORT).show(); // 提示用户
    }
}
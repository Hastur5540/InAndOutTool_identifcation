package com.example.inandouttool_identification;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class WorkerListActivity extends AppCompatActivity {
    private ListView workerListView;
    private static ArrayAdapter<Worker> adapter;
    static List<Worker> workerList = new ArrayList<>();
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_list);

        workerListView = findViewById(R.id.workerListView);
        searchInput = findViewById(R.id.searchInput);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, workerList);
        workerListView.setAdapter(adapter);

        // 搜索功能
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        workerListView.setOnItemClickListener((parent, view, position, id) -> {
            Worker selectedWorker = workerList.get(position);
            Intent intent = new Intent(WorkerListActivity.this, ComparisonActivity.class);
            intent.putExtra("worker", selectedWorker);
            startActivity(intent);
        });
    }

    public static void updateWorkerList(List<Worker> workers) {
        workerList.clear();
        if (workers != null) {
            workerList.addAll(workers);
        }
        adapter.notifyDataSetChanged();
    }
}

package com.example.inandouttool_identification;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ToolCheckActivity extends AppCompatActivity {

    private Button btnBack;
    private ListView listEnterTools;
    private ListView listExitTools;
    private TextView tvToolConsistency;
    Boolean Consistent_flags = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_check);

        btnBack = findViewById(R.id.btn_back);
        listEnterTools = findViewById(R.id.list_enter_tools);
        listExitTools = findViewById(R.id.list_exit_tools);
        tvToolConsistency = findViewById(R.id.tv_tool_consistency);

        // 设置返回按钮的点击事件
        btnBack.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("Consistent_flags", Consistent_flags);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // 获取 Bundle 并填充数据
        Bundle bundle = getIntent().getBundleExtra("tools");

        if (bundle != null) {
            List<String> enterToolsList = new ArrayList<>();
            List<String> exitToolsList = new ArrayList<>();

            // 提取进入和离开的工具数据
            for (String key : bundle.keySet()) {
                int quantity = bundle.getInt(key);
                enterToolsList.add(key + " x" + quantity);
                exitToolsList.add(key + " x" + quantity);//假数据
            }

            // 使用 ArrayAdapter 填充 ListView
            ArrayAdapter<String> enterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, enterToolsList);
            listEnterTools.setAdapter(enterAdapter);

            // 工具显示在离开工具列表中
            ArrayAdapter<String> exitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, exitToolsList);
            listExitTools.setAdapter(exitAdapter);

            // 检查工具一致性逻辑
            if (enterToolsList.equals(exitToolsList)) {
                tvToolConsistency.setText("工具一致");
                tvToolConsistency.setTextColor(Color.GREEN); // 设置为绿色
                Consistent_flags = true;
            } else {
                tvToolConsistency.setText("工具不一致");
                tvToolConsistency.setTextColor(Color.RED); // 设置为红色
                Consistent_flags = false;
            }
        }
    }
}

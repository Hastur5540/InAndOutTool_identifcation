package com.example.inandouttool_identification;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        btnBack.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("Consistent_flags", Consistent_flags);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        Bundle bundle_IN = getIntent().getBundleExtra("tools_IN");
        Bundle bundle_OUT = getIntent().getBundleExtra("tools_OUT");

        Log.d("ToolCheckActivity", "Bundle IN: " + bundle_IN);
        Log.d("ToolCheckActivity", "Bundle OUT: " + bundle_OUT);

        if (bundle_IN != null && bundle_OUT != null) {
            Map<String, Integer> enterToolsMap = new HashMap<>();
            Map<String, Integer> exitToolsMap = new HashMap<>();

            // Extracting tools
            for (String key : bundle_IN.keySet()) {
                int quantity = bundle_IN.getInt(key);
                enterToolsMap.put(key, quantity);
            }

            for (String key : bundle_OUT.keySet()) {
                int quantity = bundle_OUT.getInt(key);
                exitToolsMap.put(key, quantity);
            }

            List<String> enterToolsList = new ArrayList<>();
            List<String> exitToolsList = new ArrayList<>();

            // Prepare lists
            for (Map.Entry<String, Integer> entry : enterToolsMap.entrySet()) {
                enterToolsList.add(entry.getKey() + " x" + entry.getValue());
            }

            for (Map.Entry<String, Integer> entry : exitToolsMap.entrySet()) {
                exitToolsList.add(entry.getKey() + " x" + entry.getValue());
            }

            // Set adapters
            listEnterTools.setAdapter(new ToolAdapter(enterToolsList, exitToolsMap));
            listExitTools.setAdapter(new ToolAdapter(exitToolsList, enterToolsMap));

            // Consistency check
            Consistent_flags = enterToolsMap.equals(exitToolsMap);
            tvToolConsistency.setText(Consistent_flags ? "工具一致" : "工具不一致");
            tvToolConsistency.setTextColor(Consistent_flags ? Color.GREEN : Color.RED);
        }
    }

    private static class ToolAdapter extends BaseAdapter {
        private final List<String> toolsList;
        private final Map<String, Integer> compareMap;

        ToolAdapter(List<String> toolsList, Map<String, Integer> compareMap) {
            this.toolsList = toolsList;
            this.compareMap = compareMap;
        }

        @Override
        public int getCount() {
            return toolsList.size();
        }

        @Override
        public Object getItem(int position) {
            return toolsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                textView = new TextView(parent.getContext());
                textView.setPadding(16, 16, 16, 16);
            } else {
                textView = (TextView) convertView;
            }
            textView.setText(toolsList.get(position));

            String toolName = toolsList.get(position).split(" x")[0];
            int toolCount = Integer.parseInt(toolsList.get(position).split(" x")[1]);

            // Highlighting logic
            if (compareMap.containsKey(toolName)) {
                if (compareMap.get(toolName) != toolCount) {
                    textView.setBackgroundColor(Color.YELLOW); // Highlight inconsistent tools
                } else {
                    textView.setBackgroundColor(Color.TRANSPARENT); // Default background
                }
            } else {
                textView.setBackgroundColor(Color.TRANSPARENT); // Default background
            }

            return textView;
        }
    }
}

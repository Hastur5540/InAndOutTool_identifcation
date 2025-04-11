package com.example.inandouttool_identification.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.inandouttool_identification.R;
import com.example.inandouttool_identification.database.DatabaseHelper;
import com.example.inandouttool_identification.entity.Worker;
import com.example.inandouttool_identification.utils.ImageProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolCheckActivity extends AppCompatActivity {

    private Button btnBack;
    private ListView listEnterTools;
    private ListView listExitTools;
    private TextView tvToolConsistency;
    private ImageView workerImageView_IN;
    private ImageView workerImageView_OUT;
    private Boolean Consistent_flags = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_check);

        btnBack = findViewById(R.id.btn_back);
        listEnterTools = findViewById(R.id.list_enter_tools);
        listExitTools = findViewById(R.id.list_exit_tools);
        workerImageView_IN = findViewById(R.id.img_enter_tools);
        workerImageView_OUT = findViewById(R.id.img_exit_tools);
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

            // 提取工具
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

            // 准备工具列表
            for (Map.Entry<String, Integer> entry : enterToolsMap.entrySet()) {
                enterToolsList.add(entry.getKey() + " x" + entry.getValue());
            }

            for (Map.Entry<String, Integer> entry : exitToolsMap.entrySet()) {
                exitToolsList.add(entry.getKey() + " x" + entry.getValue());
            }

            // 设置适配器
            listEnterTools.setAdapter(new ToolAdapter(enterToolsList, exitToolsMap));
            listExitTools.setAdapter(new ToolAdapter(exitToolsList, enterToolsMap));

            // 一致性检查
            Consistent_flags = enterToolsMap.equals(exitToolsMap);
            tvToolConsistency.setText(Consistent_flags ? "工具一致" : "工具不一致");
            tvToolConsistency.setTextColor(Consistent_flags ? Color.GREEN : Color.RED);


            ImageProcess imageProcessor = new ImageProcess();
            String imageInCheckedPath = getIntent().getStringExtra("in_checked_path");
            String imageOutCheckedPath = getIntent().getStringExtra("out_checked_path");
            Bitmap imageInCheckedBitmap = imageProcessor.loadImageFromFile(imageInCheckedPath);
            Bitmap imageOutCheckedBitmap = imageProcessor.loadImageFromFile(imageOutCheckedPath);


            if (Consistent_flags) {
                workerImageView_IN.setImageBitmap(imageInCheckedBitmap);
                workerImageView_OUT.setImageBitmap(imageOutCheckedBitmap);
            } else {
                workerImageView_IN.setImageBitmap(imageInCheckedBitmap);
                workerImageView_OUT.setImageBitmap(imageOutCheckedBitmap);

                // 点击一致性文本查看不一致情况
                tvToolConsistency.setOnClickListener(v -> showDiscrepancyDialog(enterToolsMap, exitToolsMap));
            }
            workerImageView_IN.setOnClickListener(v -> ImageProcess.showImageDialog(this, workerImageView_IN.getDrawable()));
            workerImageView_OUT.setOnClickListener(v -> ImageProcess.showImageDialog(this, workerImageView_OUT.getDrawable()));

        }
    }

    private void showDiscrepancyDialog(Map<String, Integer> enterToolsMap, Map<String, Integer> exitToolsMap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("工具不一致");

        // 计算不一致工具
        StringBuilder discrepancy = new StringBuilder("以下工具不一致：\n");

        // 查找多出来的工具
        for (Map.Entry<String, Integer> entry : exitToolsMap.entrySet()) {
            String toolName = entry.getKey();
            int exitCount = entry.getValue();
            int enterCount = enterToolsMap.getOrDefault(toolName, 0);
            if (exitCount > enterCount) {
                discrepancy.append(toolName).append("多了").append(exitCount - enterCount).append("个\n");
            }
        }

        // 查找少了的工具
        for (Map.Entry<String, Integer> entry : enterToolsMap.entrySet()) {
            String toolName = entry.getKey();
            int enterCount = entry.getValue();
            int exitCount = exitToolsMap.getOrDefault(toolName, 0);
            if (enterCount > exitCount) {
                discrepancy.append(toolName).append("少了").append(enterCount - exitCount).append("个\n");
            }
        }

        builder.setMessage(discrepancy.toString());
        builder.setPositiveButton("确认", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
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
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                holder = new ViewHolder();
                holder.textView = convertView.findViewById(R.id.text_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            String currentItem = toolsList.get(position);
            holder.textView.setText(currentItem);

            String toolName = currentItem.split(" x")[0];
            int toolCount = Integer.parseInt(currentItem.split(" x")[1]);

            // 高亮逻辑
            if (compareMap.containsKey(toolName)) {
                if (compareMap.get(toolName) != toolCount) {
                    convertView.setBackgroundColor(Color.YELLOW); // 高亮不一致的工具
                } else {
                    convertView.setBackgroundColor(Color.TRANSPARENT); // 默认背景
                }
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT); // 默认背景
            }

            return convertView;
        }

        private static class ViewHolder {
            TextView textView;
        }
    }

    public static Bitmap decodeBase64ToBitmap(String base64String) {
        // 解码 Base64 字符串为字节数组
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

        // 将字节数组转换为 Bitmap
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}

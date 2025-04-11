package com.example.inandouttool_identification.entity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inandouttool_identification.R;
import com.example.inandouttool_identification.activities.ComparisonActivity;
import com.example.inandouttool_identification.database.DatabaseHelper;
import com.example.inandouttool_identification.entity.Worker;

import java.io.File;
import java.util.List;

public class WorkerAdapter extends BaseAdapter {
    private Context context;
    private List<Worker> workerList;
    private DatabaseHelper databaseHelper;

    public WorkerAdapter(Context context, List<Worker> workerList, DatabaseHelper databaseHelper) {
        this.context = context;
        this.workerList = workerList;
        this.databaseHelper = databaseHelper;
    }

    @Override
    public int getCount() {
        return workerList.size();
    }

    @Override
    public Object getItem(int position) {
        return workerList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_worker, parent, false);
        }

        TextView workerName = convertView.findViewById(R.id.workerName);
        TextView workerId = convertView.findViewById(R.id.workerID);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);
        Button checkButton = convertView.findViewById(R.id.checkButton);

        Worker worker = workerList.get(position);
        workerName.setText(worker.getName());
        workerId.setText(worker.getId());
        // 删除按钮点击事件
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("删除确认")
                    .setMessage("确定要删除该工人吗？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        deleteWorker(worker, position); // 用户确认删除
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        dialog.dismiss(); // 取消操作
                    })
                    .show();
        });


        // **查看按钮点击事件**
        checkButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, ComparisonActivity.class);
            intent.putExtra("worker", worker);
            context.startActivity(intent);
        });

        return convertView;
    }

    // 删除工人
    private void deleteWorker(Worker worker, int position) {
        databaseHelper.deleteWorker(worker.getId());
        deletePhoto(worker.getPhotoPath_IN());
        deletePhoto(worker.getPhotoPath_OUT());

        workerList.remove(position);
        notifyDataSetChanged();

        Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
    }

    // 删除照片
    private void deletePhoto(String photoPath) {
        if(photoPath==null){
            return;
        }
        File photoFile = new File(photoPath);
        if (photoFile.exists()) {
            boolean deleted = photoFile.delete();
            Log.d("WorkerAdapter", deleted ? "Deleted photo: " + photoPath : "Failed to delete photo: " + photoPath);
        }
    }
}

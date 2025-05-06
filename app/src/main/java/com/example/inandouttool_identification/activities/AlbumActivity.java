package com.example.inandouttool_identification.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inandouttool_identification.R;

import java.io.File;
import java.util.ArrayList;

public class AlbumActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private String d_id = null;
    private ArrayList<String> photoList; // 使用字符串列表保存图片路径
    private boolean isEditing = false;
    private Button backButton;
    private Button editButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        recyclerView = findViewById(R.id.recyclerView);
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        photoList = new ArrayList<>();

        // 从 Intent 获取设备 ID
        String deviceId = getIntent().getStringExtra("DeviceId");
        if (deviceId != null) {
            d_id = deviceId;
        }

        // 读取保存在设备文件夹中的所有图片
        loadImagesFromDevice();

        // 使用 GridLayoutManager 设置列数为 2
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        photoAdapter = new PhotoAdapter(this, photoList);
        recyclerView.setAdapter(photoAdapter);
        editButton.setOnClickListener(v -> {
            isEditing = !isEditing; // 切换编辑模式
            photoAdapter.setEditing(isEditing);
        });
        // 返回按钮点击事件
        backButton.setOnClickListener(v -> finish());
    }

    private void loadImagesFromDevice() {
        Log.d("AlbumActivity", "loadImagesFromDevice");
        String deviceFolderName = "";
        if(getIntent().getStringExtra("temp").equals("temp")){
            deviceFolderName = "Device_temp";
        }else{
            deviceFolderName = "WorkerID_" + d_id;
        }
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);

        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String inOutFlag = getIntent().getStringExtra("inOutFlag") + "_";
                    String fileName = d_id;
                    if (file.isFile() && file.getName().startsWith(fileName)) {
                        photoList.add(file.getAbsolutePath());
                        Log.d("AlbumActivity", "Added image: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

        private Context context;
        private ArrayList<String> photos; // 保存图片路径
        private boolean isEditing = false;

        PhotoAdapter(Context context, ArrayList<String> photos) {
            this.context = context;
            this.photos = photos;
        }

        @Override
        public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
            return new PhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoViewHolder holder, int position) {
            String imagePath = photos.get(position);
            Log.d("PhotoAdapter", "Loading image: " + imagePath);
            // 使用 Glide 加载图片
            Glide.with(context)
                    .load(imagePath)
                    .fitCenter() // 添加等比缩放
                    .into(holder.photoImageView);
            holder.photoImageView.setOnClickListener(v -> showZoomedImage(imagePath));
            holder.deleteButton.setVisibility(isEditing ? View.VISIBLE : View.GONE);

            holder.deleteButton.setOnClickListener(v -> {
                // 获取要删除的文件路径
                String filePath = photos.get(position);
                File fileToDelete = new File(filePath);
                // 删除文件
                if (fileToDelete.exists()) {
                    boolean deleted = fileToDelete.delete();
                    if (deleted) {
                        Log.d("PhotoAdapter", "Deleted file: " + filePath);
                        // 从照片列表中移除文件路径
                        photos.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, photos.size());
                    } else {
                        Log.e("PhotoAdapter", "Failed to delete file: " + filePath);
                    }
                } else {
                    Log.e("PhotoAdapter", "File does not exist: " + filePath);
                }
            });
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        void setEditing(boolean isEditing) {
            this.isEditing = isEditing;
            notifyDataSetChanged();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView photoImageView;
            Button deleteButton;

            PhotoViewHolder(View itemView) {
                super(itemView);
                photoImageView = itemView.findViewById(R.id.photoImageView);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }

        private void showZoomedImage(String imagePath) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.dialog_image_zoom, null);
            ImageView zoomedImageView = dialogView.findViewById(R.id.zoomedImageView);

            // 使用 Glide 加载放大图像
            Glide.with(context).load(imagePath).into(zoomedImageView);

            builder.setView(dialogView);
            AlertDialog dialog = builder.create();

            // 设置点击对话框外部或图像关闭对话框
            zoomedImageView.setOnClickListener(v -> dialog.dismiss());

            dialog.setOnCancelListener(dialogInterface -> dialog.dismiss()); // 额外处理
            dialog.show();
        }
    }
}

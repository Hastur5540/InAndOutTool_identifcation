package com.example.inandouttool_identification.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.inandouttool_identification.R;

import java.io.File;
import java.util.ArrayList;

public class Camera_AlbumActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;
    private String d_id = null;
    private ArrayList<String> photoList; // 使用字符串列表保存图片路径
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_album);

        recyclerView = findViewById(R.id.recyclerView);
        backButton = findViewById(R.id.backButton);
        photoList = new ArrayList<>();

        // 从 Intent 获取 ID
        String deviceId = getIntent().getStringExtra("WorkerId");
        if (deviceId != null) {
            d_id = deviceId;
        }

        // 读取保存在设备文件夹中的所有图片
        loadImagesFromDevice();

        // 使用 GridLayoutManager 设置列数为 2
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        photoAdapter = new PhotoAdapter(this, photoList);
        recyclerView.setAdapter(photoAdapter);

        // 返回按钮点击事件
        backButton.setOnClickListener(v -> finish());
    }

    private void loadImagesFromDevice() {
        // 构建设备图片文件夹路径
        String deviceFolderName = "WorkerID_" + d_id; // 使用设备 ID 命名文件夹
        File storageDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), deviceFolderName);

        if (storageDir.exists()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = d_id;
                    if (file.isFile() && file.getName().startsWith(fileName)) {
                        photoList.add(file.getAbsolutePath());
                        Log.d("Camera_AlbumActivity", "Added image: " + file.getAbsolutePath());
                    }
                }
            } else {
                Log.d("Camera_AlbumActivity", "No files found in directory: " + storageDir.getAbsolutePath());
            }
        } else {
            Log.d("Camera_AlbumActivity", "Directory does not exist: " + storageDir.getAbsolutePath());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

        private Context context;
        private ArrayList<String> photos; // 存储文件路径

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

            // 配置点击事件，返回图片路径
            holder.photoImageView.setOnClickListener(v -> {
                // 点击图片后返回图片路径
                Intent resultIntent = new Intent();
                resultIntent.putExtra("SelectedImagePath", imagePath);
                ((Camera_AlbumActivity) context).setResult(RESULT_OK, resultIntent);
                ((Camera_AlbumActivity) context).finish(); // 关闭相册页面
            });
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView photoImageView;

            PhotoViewHolder(View itemView) {
                super(itemView);
                photoImageView = itemView.findViewById(R.id.photoImageView);
            }
        }
    }
}
package com.example.inandouttool_identification.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageProcess {


    // 将base64文件存储到指定文件路径
    public void saveBase64ToFile(String base64String, String filePath) {
        // 解码 Base64 字符串为字节数组
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

        // 将字节数组写入文件
        File file = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decodedBytes);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 从指定文件路径中读取图片并将其转化为bitmap类型
    public Bitmap loadImageFromFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return BitmapFactory.decodeFile(filePath);
        } else {
            return null; // 文件不存在，返回 null
        }
    }

    public Bitmap adjustImageSize(Bitmap originalBitmap, ImageView imageView) {
        int imageViewHeight = imageView.getHeight();
        // 获取原始 Bitmap 的宽度和高度
        int originalWidth = originalBitmap.getWidth();
        int originalHeight = originalBitmap.getHeight();

        // 根据 imageView 的高度计算缩放后的宽度，保持宽高比
        float ratio = (float) imageViewHeight / originalHeight;
        int scaledWidth = (int) (originalWidth * ratio);
        int scaledHeight = imageViewHeight;

        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        layoutParams.width = scaledWidth; // 设置宽度
        imageView.setLayoutParams(layoutParams);
        // 创建调整后的 Bitmap
        return Bitmap.createScaledBitmap(originalBitmap, scaledWidth, scaledHeight, true);
    }

}

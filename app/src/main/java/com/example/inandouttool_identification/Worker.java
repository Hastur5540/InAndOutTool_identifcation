package com.example.inandouttool_identification;

import java.io.Serializable;

public class Worker implements Serializable {
    private String name;          // 姓名
    private String id;            // 工号
    private String photoPath_IN;  // 进厂照片路径
    private String photoPath_OUT; // 出厂照片路径

    // 构造函数
    public Worker(String name, String id, String photoPath_IN, String photoPath_OUT) {
        this.name = name;
        this.id = id;
        this.photoPath_IN = photoPath_IN;
        this.photoPath_OUT = photoPath_OUT;
    }

    // Getter 和 Setter 方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotoPath_IN() {
        return photoPath_IN;
    }

    public void setPhotoPath_IN(String photoPath_IN) {
        this.photoPath_IN = photoPath_IN;
    }

    public String getPhotoPath_OUT() {
        return photoPath_OUT;
    }

    public void setPhotoPath_OUT(String photoPath_OUT) {
        this.photoPath_OUT = photoPath_OUT;
    }

    @Override
    public String toString() {
        return name + " (工号: " + id + ")";
    }
}
package com.example.inandouttool_identification;

import android.os.Parcel;
import android.os.Parcelable;

public class Worker implements Parcelable {
    String name;
    String id; // 工号
    String photoPath;

    public Worker(String name, String id, String photoPath) {
        this.name = name;
        this.id = id;
        this.photoPath = photoPath;
    }

    protected Worker(Parcel in) {
        name = in.readString();
        id = in.readString();
        photoPath = in.readString();
    }

    public static final Creator<Worker> CREATOR = new Creator<Worker>() {
        @Override
        public Worker createFromParcel(Parcel in) {
            return new Worker(in);
        }

        @Override
        public Worker[] newArray(int size) {
            return new Worker[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(id);
        dest.writeString(photoPath);
    }

    @Override
    public String toString() {
        return id + ": " + name; // 用于展示
    }
}

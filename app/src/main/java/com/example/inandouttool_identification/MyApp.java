package com.example.inandouttool_identification;

import android.app.Application;
import android.app.Activity;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MyApp extends Application implements Application.ActivityLifecycleCallbacks {
    private int activityReferences = 0;
    private List<Worker> workerList;
    private static final String FILE_NAME = "workers.dat";
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        loadWorkers(); // 加载 workerList
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activityReferences == 0) {
            // 应用进入前台
        }
        activityReferences++;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        activityReferences--;
        if (activityReferences == 0) {
            // 最后一个 Activity 被停止时，保存 workerList
            saveWorkers();
            //cleanUpResources();
        }
    }

    private void loadWorkers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(getFilesDir() + "/" + FILE_NAME))) {
            workerList = (List<Worker>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            workerList = new ArrayList<>(); // 初始化为空列表
        }
    }

    private void saveWorkers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(getFilesDir() + "/" + FILE_NAME))) {
            oos.writeObject(workerList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanUpResources() {
        // 清理资源逻辑
        //deleteImages();
    }

    private void deleteImages() {
        File directory = getFilesDir();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                if (fileName.startsWith("IN_") || fileName.startsWith("OUT_")) {
                    file.delete();
                }
            }
        }
    }

    // 其余生命周期方法可留空
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
    @Override
    public void onActivityResumed(Activity activity) {}
    @Override
    public void onActivityPaused(Activity activity) {}
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override
    public void onActivityDestroyed(Activity activity) {}

    public List<Worker> getWorkerList() {
        return workerList;
    }
}
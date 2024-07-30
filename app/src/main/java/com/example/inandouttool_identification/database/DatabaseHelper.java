
package com.example.inandouttool_identification.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.inandouttool_identification.entity.Worker;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "worker_database.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE workers (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, workerId TEXT, photoPath_IN TEXT, photoPath_OUT TEXT, photoPath_IN_Checked TEXT, photoPath_OUT_Checked TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS workers");
        onCreate(db);
    }

    public List<Worker> getAllWorkers() {
        List<Worker> workers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM workers", null);

        if (cursor.moveToFirst()) {
            do {
                int nameIndex = cursor.getColumnIndex("name");
                int workerIdIndex = cursor.getColumnIndex("workerId");
                int photoPathInIndex = cursor.getColumnIndex("photoPath_IN");
                int photoPathOutIndex = cursor.getColumnIndex("photoPath_OUT");

                if (nameIndex != -1 && workerIdIndex != -1 && photoPathInIndex != -1 && photoPathOutIndex != -1) {
                    String name = cursor.getString(nameIndex);
                    String workerId = cursor.getString(workerIdIndex);
                    String photoPath_IN = cursor.getString(photoPathInIndex);
                    String photoPath_OUT = cursor.getString(photoPathOutIndex);
                    workers.add(new Worker(name, workerId, photoPath_IN, photoPath_OUT));
                } else {
                    Log.e("DatabaseError", "Column not found in workers table");
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return workers;
    }

    public void addWorker(String name, String workerId, String photoPath_IN, String photoPath_OUT) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("workerId", workerId);
            values.put("photoPath_IN", photoPath_IN);
            values.put("photoPath_OUT", photoPath_OUT);

            long newRowId = db.insert("workers", null, values);
            if (newRowId != -1) {
                Log.d("DatabaseHelper", "Added worker with ID: " + newRowId);
            } else {
                Log.e("DatabaseError", "Error adding worker");
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error adding worker: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    public void deleteWorker(String workerId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete("workers", "workerId = ?", new String[]{workerId});
            Log.d("DatabaseHelper", "Deleted worker with ID: " + workerId);
        } catch (Exception e) {
            Log.e("DatabaseError", "Error deleting worker: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    public Worker getWorkerById(String workerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Worker worker = null;
        Cursor cursor = null;

        try {
            cursor = db.query("workers", null, "workerId = ?", new String[]{String.valueOf(workerId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex("name");
                int photoPathInIndex = cursor.getColumnIndex("photoPath_IN");
                int photoPathOutIndex = cursor.getColumnIndex("photoPath_OUT");

                if (nameIndex != -1 && photoPathInIndex != -1 && photoPathOutIndex != -1) {
                    String name = cursor.getString(nameIndex);
                    String photoPath_IN = cursor.getString(photoPathInIndex);
                    String photoPath_OUT = cursor.getString(photoPathOutIndex);
                    worker = new Worker(name, workerId, photoPath_IN, photoPath_OUT);
                } else {
                    Log.e("DatabaseError", "Column not found in workers table");
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error fetching worker by ID: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return worker;
    }

    public void updateWorker(String workerId, String name, String photoPath_IN, String photoPath_OUT) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("photoPath_IN", photoPath_IN);
            values.put("photoPath_OUT", photoPath_OUT);

            int updatedRows = db.update("workers", values, "workerId = ?", new String[]{workerId});
            if (updatedRows > 0) {
                Log.d("DatabaseHelper", "Updated worker with ID: " + workerId);
            } else {
                Log.e("DatabaseError", "No worker found with ID: " + workerId);
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error updating worker: " + e.getMessage());
        } finally {
            db.close();
        }
    }


    public void updateCheckedImagePath(String workerId, String checkedImage1Path, String checkedImage2Path) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put("photoPath_IN_Checked", checkedImage1Path);
            values.put("photoPath_OUT_Checked", checkedImage2Path);

            // 更新指定 workerId 的记录
            int updatedRows = db.update("workers", values, "workerId = ?", new String[]{workerId});
            if (updatedRows > 0) {
                Log.d("DatabaseHelper", "Updated checked image paths for worker with ID: " + workerId);
            } else {
                Log.e("DatabaseError", "No worker found with ID: " + workerId);
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error updating checked image paths: " + e.getMessage());
        } finally {
            db.close();
        }
    }

}
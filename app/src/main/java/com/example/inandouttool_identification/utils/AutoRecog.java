package com.example.inandouttool_identification.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AutoRecog {
    private String name;
    private String id;
    private String url;

    public AutoRecog(String url){
        String root_path = Constants.TARGET_IP_ADDRESS + ":" + Constants.PORT;
        this.name = "123";
        this.id = "456";
        this.url = root_path + url;
    }

    public JSONObject getRecognitionResult(byte[] data) throws JSONException {
        String boundary = "Boundary-" + System.currentTimeMillis();
        String LINE_FEED = "\r\n";
        InputStream inputStream = null;
        String jsonString = null;

        try {
            URL url = new URL(this.url);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream outputStream = httpConn.getOutputStream();
            // 写入表单数据头
            StringBuilder formData = new StringBuilder();
            formData.append("--").append(boundary).append(LINE_FEED);
            formData.append("Content-Disposition: form-data; name=\"image\"; filename=\"yjh-0006.jpg\"").append(LINE_FEED);
            formData.append("Content-Type: image/jpeg").append(LINE_FEED);
            formData.append(LINE_FEED);
            outputStream.write(formData.toString().getBytes());

            // 直接写入二进制数据
            outputStream.write(data);
            outputStream.write(LINE_FEED.getBytes());

            // 结束 multipart/form-data 请求体
            outputStream.write(("--" + boundary + "--" + LINE_FEED).getBytes());
            outputStream.flush();


            // 读取服务器响应
            int responseCode = httpConn.getResponseCode();

            StringBuilder response = new StringBuilder();
            inputStream = httpConn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            jsonString = response.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        JSONObject jsonResponse = new JSONObject(jsonString);
        JSONObject recogRes = (JSONObject) jsonResponse.get("msg");
        return recogRes;
    }


    private static void addFilePart(OutputStream outputStream, File uploadFile, String fieldName, String boundary, String LINE_FEED) throws Exception {
        StringBuilder formData = new StringBuilder();
        formData.append("--").append(boundary).append(LINE_FEED);
        formData.append("Content-Disposition: form-data; name=\"").append(fieldName)
                .append("\"; filename=\"").append(uploadFile.getName()).append("\"").append(LINE_FEED);
        formData.append("Content-Type: ").append("image/jpeg").append(LINE_FEED);
        formData.append(LINE_FEED);
        outputStream.write(formData.toString().getBytes());

        try (FileInputStream inputStream = new FileInputStream(uploadFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        outputStream.write(LINE_FEED.getBytes());
    }


    public Map<String, String> getRecognitionResult_mock() {
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("name", name);
        resultMap.put("id", id);
        return resultMap;
    }

}

package com.example.inandouttool_identification.httpConnetection;

import com.example.inandouttool_identification.utils.Constants;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest {

    private final String url;

    public HttpRequest(String url){
        String root_path = Constants.TARGET_IP_ADDRESS + ":" + Constants.PORT;

        this.url = root_path + url;
    }


    public String getCompareResult(String file1Path, String file2Path) throws IOException {
//        System.out.println(this.url);

        String boundary = "Boundary-" + System.currentTimeMillis();
        String LINE_FEED = "\r\n";
        InputStream intputStream = null;
        String jsonResponse = null;
        try {
            File uploadFile1 = new File(file1Path);
            File uploadFile2 = new File(file2Path);
            URL url = new URL(this.url);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream outputStream = httpConn.getOutputStream()) {

                addFilePart(outputStream, uploadFile1, "image1", boundary, LINE_FEED);
                addFilePart(outputStream, uploadFile2, "image2", boundary, LINE_FEED);

                outputStream.write((LINE_FEED + "--" + boundary + "--" + LINE_FEED).getBytes());
                outputStream.flush();
            }

            intputStream = httpConn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(intputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            jsonResponse = response.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        BufferedReader reader = new BufferedReader(new InputStreamReader(intputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        return jsonResponse;
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

    public static void main(String[] args) throws IOException {
        HttpRequest httpRequest = new HttpRequest("/process_image");

        String image1Path = "C:\\Users\\yujia\\Desktop\\江浩\\easy\\IMG20240727101344.jpg";
        String image2Path = "C:\\Users\\yujia\\Desktop\\江浩\\easy\\IMG20240727101350.jpg";

        System.out.println(httpRequest.getCompareResult(image1Path, image2Path));


//        try {
//            File imageFile = new File(image1Path);
//            FileInputStream fileInputStream = new FileInputStream(imageFile);
//            byte[] imageBytes = new byte[(int) imageFile.length()];
//            fileInputStream.read(imageBytes);
//            fileInputStream.close();
//
//            // 将字节数组转换为Base64字符串
//            String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
//            System.out.println("Base64 String: " + imageBase64);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}

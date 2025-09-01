package com.monstertechno.webview.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.monstertechno.webview.managers.FileManager;
import com.monstertechno.webview.managers.NotificationManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadWorker extends Worker {
    
    private static final int BUFFER_SIZE = 8192;
    private FileManager fileManager;
    private NotificationManager notificationManager;
    
    public DownloadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.fileManager = new FileManager(context);
        this.notificationManager = new NotificationManager(context);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        String url = getInputData().getString("url");
        String filename = getInputData().getString("filename");
        
        if (url == null || filename == null) {
            return Result.failure();
        }
        
        try {
            return downloadFile(url, filename);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure(createErrorData(e.getMessage()));
        }
    }
    
    private Result downloadFile(String urlString, String filename) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        
        // Set user agent to avoid blocking
        connection.setRequestProperty("User-Agent", 
            "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36");
        
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            return Result.failure(createErrorData("HTTP Error: " + responseCode));
        }
        
        int fileLength = connection.getContentLength();
        
        // Create download file
        String downloadPath = fileManager.getDownloadPath(filename);
        File outputFile = new File(downloadPath);
        
        try (InputStream input = new BufferedInputStream(connection.getInputStream());
             FileOutputStream output = new FileOutputStream(outputFile)) {
            
            byte[] buffer = new byte[BUFFER_SIZE];
            long totalBytesRead = 0;
            int bytesRead;
            int lastProgress = 0;
            
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                
                // Update progress notification
                if (fileLength > 0) {
                    int progress = (int) (totalBytesRead * 100 / fileLength);
                    if (progress > lastProgress + 5) { // Update every 5%
                        notificationManager.showDownloadNotification(filename, progress, false);
                        lastProgress = progress;
                    }
                }
                
                // Check if work is cancelled
                if (isStopped()) {
                    return Result.failure(createErrorData("Download cancelled"));
                }
            }
            
            // Download completed successfully
            notificationManager.showDownloadNotification(filename, 100, true);
            
            return Result.success(createSuccessData(downloadPath, totalBytesRead));
            
        } finally {
            connection.disconnect();
        }
    }
    
    private Data createSuccessData(String filePath, long fileSize) {
        return new Data.Builder()
            .putString("status", "success")
            .putString("file_path", filePath)
            .putLong("file_size", fileSize)
            .build();
    }
    
    private Data createErrorData(String error) {
        return new Data.Builder()
            .putString("status", "error")
            .putString("error", error)
            .build();
    }
}
package com.monstertechno.webview.managers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.monstertechno.webview.workers.DownloadWorker;

import java.io.File;

public class FileManager {
    
    private Context context;
    private NotificationManager notificationManager;
    
    public FileManager(Context context) {
        this.context = context;
        this.notificationManager = new NotificationManager(context);
    }
    
    public void downloadFile(String url, String filename) {
        // Use WorkManager for background download
        Data inputData = new Data.Builder()
            .putString("url", url)
            .putString("filename", filename)
            .build();
        
        OneTimeWorkRequest downloadWork = new OneTimeWorkRequest.Builder(DownloadWorker.class)
            .setInputData(inputData)
            .build();
        
        WorkManager.getInstance(context).enqueue(downloadWork);
        
        // Show initial download notification
        notificationManager.showDownloadNotification(filename, 0, false);
    }
    
    public void shareFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
            );
        } else {
            fileUri = Uri.fromFile(file);
        }
        
        String mimeType = getMimeType(filePath);
        
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        context.startActivity(Intent.createChooser(shareIntent, "Share file"));
    }
    
    public void openFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        
        Uri fileUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                file
            );
        } else {
            fileUri = Uri.fromFile(file);
        }
        
        String mimeType = getMimeType(filePath);
        
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(fileUri, mimeType);
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        
        if (openIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(openIntent);
        }
    }
    
    public File getDownloadsDir() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        } else {
            return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        }
    }
    
    public String getDownloadPath(String filename) {
        File downloadsDir = getDownloadsDir();
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        return new File(downloadsDir, filename).getAbsolutePath();
    }
    
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.delete();
    }
    
    public long getFileSize(String filePath) {
        File file = new File(filePath);
        return file.exists() ? file.length() : -1;
    }
    
    public boolean fileExists(String filePath) {
        return new File(filePath).exists();
    }
    
    public String getMimeType(String filePath) {
        String extension = getFileExtension(filePath);
        if (extension != null) {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            return mimeType != null ? mimeType : "application/octet-stream";
        }
        return "application/octet-stream";
    }
    
    public String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filePath.length() - 1) {
            return filePath.substring(lastDotIndex + 1).toLowerCase();
        }
        return null;
    }
    
    public String getFileName(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        if (fileName.isEmpty() || !fileName.contains(".")) {
            fileName = "download_" + System.currentTimeMillis();
        }
        return fileName;
    }
    
    public File createTempFile(String prefix, String suffix) {
        try {
            return File.createTempFile(prefix, suffix, context.getCacheDir());
        } catch (Exception e) {
            return null;
        }
    }
    
    public void clearCache() {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null && cacheDir.isDirectory()) {
            deleteRecursive(cacheDir);
        }
    }
    
    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }
}
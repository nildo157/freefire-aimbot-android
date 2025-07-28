package com.example.freefireaimbot;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ModelDownloader {
    private static final String TAG = "ModelDownloader";
    private static final String MODEL_URL = "https://github.com/tensorflow/models/raw/master/research/object_detection/test_data/ssd_mobilenet_v1_coco_2018_01_28.tflite";
    private static final String MODEL_FILENAME = "object_detection_model.tflite";
    
    public interface DownloadCallback {
        void onDownloadStart();
        void onDownloadProgress(int progress);
        void onDownloadComplete(String modelPath);
        void onDownloadError(String error);
    }
    
    public static void downloadModel(Context context, DownloadCallback callback) {
        new DownloadModelTask(context, callback).execute(MODEL_URL);
    }
    
    public static boolean isModelDownloaded(Context context) {
        File modelFile = new File(context.getFilesDir(), MODEL_FILENAME);
        return modelFile.exists() && modelFile.length() > 0;
    }
    
    public static String getModelPath(Context context) {
        return new File(context.getFilesDir(), MODEL_FILENAME).getAbsolutePath();
    }
    
    private static class DownloadModelTask extends AsyncTask<String, Integer, String> {
        private Context context;
        private DownloadCallback callback;
        private String errorMessage;
        
        public DownloadModelTask(Context context, DownloadCallback callback) {
            this.context = context;
            this.callback = callback;
        }
        
        @Override
        protected void onPreExecute() {
            if (callback != null) {
                callback.onDownloadStart();
            }
        }
        
        @Override
        protected String doInBackground(String... urls) {
            String modelUrl = urls[0];
            
            try {
                URL url = new URL(modelUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    errorMessage = "Server returned HTTP " + connection.getResponseCode() 
                                 + " " + connection.getResponseMessage();
                    return null;
                }
                
                int fileLength = connection.getContentLength();
                
                InputStream input = connection.getInputStream();
                File outputFile = new File(context.getFilesDir(), MODEL_FILENAME);
                FileOutputStream output = new FileOutputStream(outputFile);
                
                byte[] buffer = new byte[4096];
                long total = 0;
                int count;
                
                while ((count = input.read(buffer)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        output.close();
                        outputFile.delete();
                        return null;
                    }
                    
                    total += count;
                    
                    if (fileLength > 0) {
                        publishProgress((int) (total * 100 / fileLength));
                    }
                    
                    output.write(buffer, 0, count);
                }
                
                output.close();
                input.close();
                connection.disconnect();
                
                Log.d(TAG, "Model downloaded successfully: " + outputFile.getAbsolutePath());
                return outputFile.getAbsolutePath();
                
            } catch (IOException e) {
                Log.e(TAG, "Error downloading model", e);
                errorMessage = e.getMessage();
                return null;
            }
        }
        
        @Override
        protected void onProgressUpdate(Integer... progress) {
            if (callback != null) {
                callback.onDownloadProgress(progress[0]);
            }
        }
        
        @Override
        protected void onPostExecute(String result) {
            if (callback != null) {
                if (result != null) {
                    callback.onDownloadComplete(result);
                } else {
                    callback.onDownloadError(errorMessage != null ? errorMessage : "Unknown error");
                }
            }
        }
    }
    
    public static void createDummyModel(Context context) {
        // Criar um modelo dummy para testes (quando não há conexão com internet)
        try {
            File modelFile = new File(context.getFilesDir(), MODEL_FILENAME);
            FileOutputStream fos = new FileOutputStream(modelFile);
            
            // Escrever alguns bytes dummy (não é um modelo real)
            byte[] dummyData = new byte[1024];
            for (int i = 0; i < dummyData.length; i++) {
                dummyData[i] = (byte) (i % 256);
            }
            fos.write(dummyData);
            fos.close();
            
            Log.d(TAG, "Dummy model created for testing");
        } catch (IOException e) {
            Log.e(TAG, "Error creating dummy model", e);
        }
    }
}


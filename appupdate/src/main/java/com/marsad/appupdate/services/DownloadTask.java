package com.marsad.appupdate.services;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTask extends Thread {

    private static final String TAG = "DownLoadTask";

    private String mFilePath;
    private String mDownLoadUrl;
    private ProgressListener mProgressListener;
    private InputStream in = null;
    private FileOutputStream fileOutputStream = null;

    public DownloadTask(String filePath, String downLoadUrl, ProgressListener progressListener) {
        mDownLoadUrl = downLoadUrl;
        mFilePath = filePath;
        mProgressListener = progressListener;
    }

    @Override
    public void run() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(mDownLoadUrl);
            if (mDownLoadUrl.startsWith("https://")) {
                TrustAllCertificates.install();
            }
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Keep-Alive", "header");

            in = new BufferedInputStream(connection.getInputStream());
            int count = connection.getContentLength();
            if (count <= 0) {
                Log.e(TAG, "file length must > 0");
                return;
            }

            if (in == null) {
                Log.e(TAG, "InputStream not be null");
                return;
            }

            writeToFile(count, mFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            if (mProgressListener != null) {
                mProgressListener.onError();
            }
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void writeToFile(int count, String filePath) throws IOException {
        int len;
        byte[] buf = new byte[2048];
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        fileOutputStream = new FileOutputStream(file);
        int bytesRead = 0;
        while ((len = in.read(buf)) != -1) {
            fileOutputStream.write(buf, 0, len);
            bytesRead += len;
            mProgressListener.update(bytesRead, count);
        }

        mProgressListener.done();
    }

    public interface ProgressListener {
        void done();

        void update(long bytesRead, long contentLength);

        void onError();
    }
}
package com.marsad.appupdate.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.marsad.appupdate.R;
import com.marsad.appupdate.utils.FileUtils;

import java.io.File;

public class DownLoadService extends Service {

    private static final int NOTIFICATION_ID = 0;
    private final DownLoadBinder mDownLoadBinder = new DownLoadBinder();
    private int notificationIcon;
    private String filePath;
    private boolean isBackground = false;
    private DownloadTask mDownLoadTask;
    private DownloadTask.ProgressListener mProgressListener;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            System.out.println("Notification init");
            NotificationChannel channel = new NotificationChannel("update_channel", "update_channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notification channel for app update alerts");
            mNotificationManager.createNotificationChannel(channel);
        }

    }

    public void startDownLoad(String url) {
        filePath = FileUtils.getApkFilePath(getApplicationContext(), url);
        mDownLoadTask = new DownloadTask(filePath, url, new DownloadTask.ProgressListener() {
            @Override
            public void done() {
                mNotificationManager.cancel(NOTIFICATION_ID);
                if (isBackground) {
                    //download finish . start to install app
                    startActivity(FileUtils.openApkFile(getApplicationContext(), new File(filePath)));
                } else {
                    if (mProgressListener != null) {
                        mProgressListener.done();
                    }
                }
            }

            @Override
            public void update(long bytesRead, long contentLength) {
                if (isBackground) {
                    int currentProgress = (int) (bytesRead * 100 / contentLength);
                    if (currentProgress < 1) {
                        currentProgress = 1;
                    }
                    notification(currentProgress);
                    return;
                }

                if (mProgressListener != null) {
                    mProgressListener.update(bytesRead, contentLength);
                }
            }

            @Override
            public void onError() {
                if (mProgressListener != null) {
                    mProgressListener.onError();
                }
                cancelNotification();
            }
        });
        mDownLoadTask.start();
    }

    public void setBackground(boolean background) {
        isBackground = background;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mDownLoadBinder;
    }

    public void cancel() {
        if (mDownLoadTask != null) {
            mDownLoadTask.interrupt();
            mDownLoadTask = null;
        }
    }

    public void setNotificationIcon(int notificationIcon) {
        this.notificationIcon = notificationIcon;
    }

    public void registerProgressListener(DownloadTask.ProgressListener progressListener) {
        mProgressListener = progressListener;
    }

    public void showNotification(int current) {
        mBuilder = new NotificationCompat.Builder(this, "update_channel"); //Added the Channel ID
        mBuilder.setContentTitle(getResources().getString(R.string.update_lib_file_download))
                .setContentText(getResources().getString(R.string.update_lib_file_downloading))
                .setSmallIcon(notificationIcon == 0 ? R.drawable.ic_launcher : notificationIcon);
        mBuilder.setProgress(100, current, false);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void notification(int current) {
        if (mBuilder == null) {
            showNotification(current);
            return;
        }
        mBuilder.setProgress(100, current, false);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private void cancelNotification() {
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    public class DownLoadBinder extends Binder {
        public DownLoadService getService() {
            return DownLoadService.this;
        }
    }
}
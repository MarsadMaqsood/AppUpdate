package com.marsad.appupdate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.abc).setOnClickListener(view -> checkUpdate());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_DENIED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            } else {
                checkUpdate();
            }
        }

    }

    private void checkUpdate() {

        UpdateWrapper updateWrapper = new UpdateWrapper.Builder(MainActivity.this)

                //set update check delay in milliseconds
                .setTime(3000)

                //set notification icon
                .setNotificationIcon(R.mipmap.ic_launcher)

                //set custom download dialog title
//              .setDownloadDialogTitle("Download Started")

                //set update dialog title
                .setUpdateTitle("Stay with time")

                //set update dialog content text
                .setUpdateContentText("Update to ensure that you are enjoying the latest features of the app.")

                //set update file url
                .setUrl("https://marsad.dev/update.json")

                //set customs activity
//              .setCustomsActivity(cls)

                //set showToast. default is true
                .setIsShowToast(false)

                //add callback ,return new version info
                .setCallback((model, hasNewVersion) -> {
                    Log.d("Latest Version", hasNewVersion + "");
                    Log.d("Version Name", model.getVersionName());
                    Log.d("Version Code", model.getVersionCode() + "");
                    Log.d("Version Description", model.getContentText());
                    Log.d("Min Support", model.getMinSupport() + "");
                    Log.d("Download URL", model.getUrl() + "");
                })
                .build();

        updateWrapper.start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkUpdate();
        }

    }

}
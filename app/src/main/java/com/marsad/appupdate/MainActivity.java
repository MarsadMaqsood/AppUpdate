package com.marsad.appupdate;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UpdateWrapper updateWrapper = new UpdateWrapper.Builder(this)

                .setTime(3000)
                //set notification icon
                .setNotificationIcon(R.mipmap.ic_launcher)
                //set update file url
                .setUrl("https://marsad.ml/update.json")
                //set customs activity
                .setCustomsActivity(cls)
                //set showToast. default is true
                .setIsShowToast(false)
                //add callback ,return new version info
                .setCallback((model, hasNewVersion) -> {
                    Log.d("Latest Version", hasNewVersion + "");
                    Log.d("Version Name", model.getVersionName());
                    Log.d("Version Code", model.getVersionCode() + "");
                    Log.d("Version Description", model.getContent());
                    Log.d("Min Support", model.getMinSupport() + "");
                    Log.d("Download URL", model.getUrl() + "");
                })
                .build();

        updateWrapper.start();

    }
}
package com.marsad.appupdate.services;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class VersionModel implements Serializable {

    /**
     * versionCode : 1
     * versionName : 1.0.0
     * content : Update Description
     * minSupport : 1
     * url : App Download Url
     */

    private int versionCode;
    private String versionName;
    private String contentText;
    private int minSupport;
    private String url;

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public int getMinSupport() {
        return minSupport;
    }

    public void setMinSupport(int minSupport) {
        this.minSupport = minSupport;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public VersionModel parse(String json) throws JSONException {
        JSONObject object = new JSONObject(json);
        versionCode = object.getInt("versionCode");
        versionName = object.getString("versionName");
        contentText = object.getString("contentText");
        url = object.getString("url");
        minSupport = object.optInt("minSupport");

        return this;
    }
}
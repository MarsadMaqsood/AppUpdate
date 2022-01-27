package com.marsad.appupdate;


import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.fragment.app.FragmentActivity;

import com.marsad.appupdate.dialogs.UpdateActivity;
import com.marsad.appupdate.services.CheckUpdateTask;
import com.marsad.appupdate.services.VersionModel;
import com.marsad.appupdate.utils.Constant;
import com.marsad.appupdate.utils.NetworkUtils;
import com.marsad.appupdate.utils.PublicFunctionUtils;
import com.marsad.appupdate.utils.ToastUtils;

import java.util.Map;

public class UpdateWrapper {
    
    private Context mContext;
    private String mUrl;
    private String mToastMsg;
    private String mDownloadHeaderText;
    private String mUpdateTitle;
    private String mUpdateContentText;
    private CheckUpdateTask.Callback mCallback;
    private int mNotificationIcon;
    private long mTime;
    private boolean mIsShowToast = true;
    private boolean mIsShowNetworkErrorToast = true;
    private boolean mIsShowBackgroundDownload = true;
    private boolean mIsPost = false;
    private Map<String, String> mPostParams;
    private Class<? extends FragmentActivity> mCls;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final CheckUpdateTask.Callback mInnerCallBack = new CheckUpdateTask.Callback() {
        @Override
        public void callBack(VersionModel model, boolean hasNewVersion) {
            if (model == null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mIsShowToast) {
                            ToastUtils.show(mContext,
                                    TextUtils.isEmpty(mToastMsg) ?
                                            mContext.getResources().getString(R.string.update_lib_default_toast) : mToastMsg);
                        }

                    }
                });
                return;
            }

            PublicFunctionUtils.setLastCheckTime(mContext, System.currentTimeMillis());
            if (mCallback != null) {
                mCallback.callBack(model, hasNewVersion);
            }

            if (hasNewVersion || mIsShowToast) {
                start2Activity(mContext, model);
            }
        }

    };

    private UpdateWrapper() {
    }

    public void start() {
        if (!NetworkUtils.getNetworkStatus(mContext)) {
            if (mIsShowNetworkErrorToast) {
                ToastUtils.show(mContext, R.string.update_lib_network_not_available);
            }
            return;
        }
        if (TextUtils.isEmpty(mUrl)) {
            throw new RuntimeException("url should not be null");
        }

        if (checkUpdateTime(mTime)) {
            return;
        }
        new CheckUpdateTask(mContext, mUrl, mIsPost, mPostParams, mInnerCallBack).start();
    }

    private boolean checkUpdateTime(long time) {
        long lastCheckUpdateTime = PublicFunctionUtils.getLastCheckTime(mContext);
        long currentTime = System.currentTimeMillis();
        return currentTime - lastCheckUpdateTime <= time;
    }

    private void start2Activity(Context context, VersionModel model) {
        try {

            if (mDownloadHeaderText == null || mDownloadHeaderText.isEmpty()) {
                mDownloadHeaderText = context.getResources().getString(R.string.update_lib_file_download);
            }

            if (mUpdateTitle == null || mUpdateTitle.isEmpty()) {
                mUpdateTitle = context.getResources().getString(R.string.update_lib_dialog_title);
            }

            if (mUpdateContentText == null || mUpdateContentText.isEmpty()) {
                mUpdateContentText = "";
            }


            Intent intent = new Intent(context, mCls == null ? UpdateActivity.class : mCls);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constant.MODEL, model);
            intent.putExtra(Constant.TOAST_MSG, mToastMsg);
            intent.putExtra(Constant.NOTIFICATION_ICON, mNotificationIcon);
            intent.putExtra(Constant.IS_SHOW_TOAST_MSG, mIsShowToast);
            intent.putExtra(Constant.IS_SHOW_BACKGROUND_DOWNLOAD, mIsShowBackgroundDownload);
            intent.putExtra(Constant.DOWNLOAD_DIALOG_HEADER_TEXT, mDownloadHeaderText);
            intent.putExtra(Constant.UPDATE_TITLE, mUpdateTitle);
            intent.putExtra(Constant.UPDATE_CONTENT_TEXT, mUpdateContentText);
            context.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Builder {
        private final UpdateWrapper wrapper = new UpdateWrapper();

        public Builder(Context context) {
            wrapper.mContext = context;
        }

        public Builder setUrl(String url) {
            wrapper.mUrl = url;
            return this;
        }

        public Builder setTime(long time) {
            wrapper.mTime = time;
            return this;
        }

        public Builder setNotificationIcon(int notificationIcon) {
            wrapper.mNotificationIcon = notificationIcon;
            return this;
        }

        public Builder setCustomsActivity(Class<? extends FragmentActivity> cls) {
            wrapper.mCls = cls;
            return this;
        }

        public Builder setCallback(CheckUpdateTask.Callback callback) {
            wrapper.mCallback = callback;
            return this;
        }

        public Builder setUpdateTitle(String updateTitle) {
            wrapper.mUpdateTitle = updateTitle;
            return this;
        }

        public Builder setUpdateContentText(String updateContentText) {
            wrapper.mUpdateContentText = updateContentText;
            return this;
        }

        public Builder setToastMsg(String toastMsg) {
            wrapper.mToastMsg = toastMsg;
            return this;
        }

        public Builder setIsShowToast(boolean isShowToast) {
            wrapper.mIsShowToast = isShowToast;
            return this;
        }

        public Builder setIsShowNetworkErrorToast(boolean isShowNetworkErrorToast) {
            wrapper.mIsShowNetworkErrorToast = isShowNetworkErrorToast;
            return this;
        }

        public Builder setIsShowBackgroundDownload(boolean isShowBackgroundDownload) {
            wrapper.mIsShowBackgroundDownload = isShowBackgroundDownload;
            return this;
        }

        public Builder setIsPost(boolean isPost) {
            wrapper.mIsPost = isPost;
            return this;
        }

        public Builder setPostParams(Map<String, String> postParams) {
            wrapper.mPostParams = postParams;
            return this;
        }

        public Builder setDownloadDialogTitle(String downloadDialogTitle) {
            wrapper.mDownloadHeaderText = downloadDialogTitle;
            return this;
        }

        public UpdateWrapper build() {
            return wrapper;
        }
    }
}
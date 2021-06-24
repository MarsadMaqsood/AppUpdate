package com.marsad.appupdate.dialogs;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.marsad.appupdate.R;
import com.marsad.appupdate.services.DownLoadService;
import com.marsad.appupdate.services.DownloadTask;
import com.marsad.appupdate.utils.Constant;
import com.marsad.appupdate.utils.FileUtils;
import com.marsad.appupdate.utils.ToastUtils;

import java.io.File;

public class DownloadDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "DownLoadDialog";
    private final static int LOADING = 1000;
    private final static int DONE = 1001;
    private final static int ERROR = 1002;
    private String mDownloadUrl;
    private String mDownloadTitleText;
    private int notificationIcon;
    private int currentProgress;
    private ImageButton mBtnCancel;
    private Button mBtnBackground;
    private TextView mTvTitle;
    private TextView mPercentage, mDownloadTitle;
    private ProgressBar mProgressBar;
    private DownLoadService mDownLoadService;
    private boolean mMustUpdate;
    private boolean mIsShowBackgroundDownload;
    private OnFragmentOperation mOnFragmentOperation;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOADING:
                    mProgressBar.setProgress(msg.arg1);
                    Bundle bundle = msg.getData();
                    long bytesRead = bundle.getLong("bytesRead");
                    long contentLength = bundle.getLong("contentLength");
                    if (getActivity() != null) {
                        mTvTitle.setText(String.format(getResources().getString(R.string.update_lib_file_download_format),
                                Formatter.formatFileSize(getActivity().getApplication(), bytesRead),
                                Formatter.formatFileSize(getActivity().getApplication(), contentLength)));

                        mPercentage.setText(
                                String.format(getResources().getString(R.string.update_lib_file_percentage), (((bytesRead/1024) * 100) / (contentLength / 1024))
                                ));


                    }
                    break;
                case DONE:
                    if (getActivity() != null) {
                        getActivity().startActivity(FileUtils.openApkFile(getActivity(), new File(FileUtils.getApkFilePath(getActivity(), mDownloadUrl))));
                        getActivity().finish();
                        ToastUtils.show(getActivity(), R.string.update_lib_download_finish);
                    }
                    break;
                case ERROR:
                    if (getActivity() != null)
                        ToastUtils.show(getActivity(), R.string.update_lib_download_failed);
                    if (!mMustUpdate) {
                        dismiss();
                        if (getActivity() != null)
                            getActivity().finish();
                    } else {
                        dismiss();
                        if (mOnFragmentOperation != null) {
                            mOnFragmentOperation.onFailed();
                        }
                    }
                    break;
            }
        }
    };
    private DownloadTask.ProgressListener mProgressListener = new DownloadTask.ProgressListener() {
        @Override
        public void done() {
            mHandler.sendEmptyMessage(DONE);
        }

        @Override
        public void update(long bytesRead, long contentLength) {
            currentProgress = (int) (bytesRead * 100 / contentLength);
            if (currentProgress < 1) {
                currentProgress = 1;
            }
            Log.d(TAG, "" + bytesRead + "," + contentLength + ";current=" + currentProgress);
            Message message = mHandler.obtainMessage();
            message.what = LOADING;
            message.arg1 = currentProgress;
            Bundle bundle = new Bundle();
            bundle.putLong("bytesRead", bytesRead);
            bundle.putLong("contentLength", contentLength);
            message.setData(bundle);
            message.sendToTarget();
        }

        @Override
        public void onError() {
            mHandler.sendEmptyMessage(ERROR);
        }
    };
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownLoadService.DownLoadBinder binder = (DownLoadService.DownLoadBinder) service;
            mDownLoadService = binder.getService();
            mDownLoadService.registerProgressListener(mProgressListener);
            mDownLoadService.startDownLoad(mDownloadUrl);
            mDownLoadService.setNotificationIcon(notificationIcon);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mDownLoadService = null;
        }
    };

    public static DownloadDialog newInstance(String downLoadUrl, int notificationIcon) {
        Bundle args = new Bundle();
        args.putString(Constant.URL, downLoadUrl);
        args.putInt(Constant.NOTIFICATION_ICON, notificationIcon);
        DownloadDialog fragment = new DownloadDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static DownloadDialog newInstance(String downLoadUrl, int notificationIcon, boolean mustUpdate, boolean isShowBackgroundDownload) {
        Bundle args = new Bundle();
        args.putString(Constant.URL, downLoadUrl);
        args.putInt(Constant.NOTIFICATION_ICON, notificationIcon);
        args.putBoolean(Constant.MUST_UPDATE, mustUpdate);
        args.putBoolean(Constant.IS_SHOW_BACKGROUND_DOWNLOAD, isShowBackgroundDownload);
        DownloadDialog fragment = new DownloadDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static DownloadDialog newInstance(String downLoadUrl, int notificationIcon, boolean mustUpdate, boolean isShowBackgroundDownload, String downloadDialogText) {
        Bundle args = new Bundle();
        args.putString(Constant.URL, downLoadUrl);
        args.putString(Constant.DOWNLOAD_DIALOG_HEADER_TEXT, downloadDialogText);
        args.putInt(Constant.NOTIFICATION_ICON, notificationIcon);
        args.putBoolean(Constant.MUST_UPDATE, mustUpdate);
        args.putBoolean(Constant.IS_SHOW_BACKGROUND_DOWNLOAD, isShowBackgroundDownload);
        DownloadDialog fragment = new DownloadDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_download, container, false);
        mDownloadUrl = getArguments().getString(Constant.URL);
        notificationIcon = getArguments().getInt(Constant.NOTIFICATION_ICON);
        mMustUpdate = getArguments().getBoolean(Constant.MUST_UPDATE);
        mDownloadTitleText = getArguments().getString(Constant.DOWNLOAD_DIALOG_HEADER_TEXT);
        if (mMustUpdate) {
            setCancelable(false);
        }
        mIsShowBackgroundDownload = getArguments().getBoolean(Constant.IS_SHOW_BACKGROUND_DOWNLOAD);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTvTitle = view.findViewById(R.id.title);
        mDownloadTitle = view.findViewById(R.id.headerText);
        mPercentage = view.findViewById(R.id.percentage);
        mBtnCancel = view.findViewById(R.id.btnCancel);
        mBtnCancel.setOnClickListener(this);
        mBtnBackground = view.findViewById(R.id.btnBackground);
        mBtnBackground.setOnClickListener(this);
        mProgressBar = view.findViewById(R.id.progressBar);
        mProgressBar.setMax(100);

        Intent intent = new Intent(getActivity(), DownLoadService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        if (mMustUpdate) {
            view.findViewById(R.id.downLayout).setVisibility(View.GONE);
        }
        if (!mIsShowBackgroundDownload) {
            mBtnBackground.setVisibility(View.GONE);
        }

        mDownloadTitle.setText(mDownloadTitleText);


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentOperation) {
            mOnFragmentOperation = (OnFragmentOperation) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnFragmentOperation = null;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnCancel) {
            doCancel();
        } else if (id == R.id.btnBackground) {
            doBackground();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mConnection);
    }

    private void doCancel() {
        mDownLoadService.cancel();
        getActivity().finish();
        ToastUtils.show(getActivity(), R.string.update_lib_download_cancel);
    }

    private void doBackground() {
        mDownLoadService.setBackground(true);
        mDownLoadService.showNotification(currentProgress);
        if (getActivity() != null) {
            ToastUtils.show(getActivity(), R.string.update_lib_download_in_background);
            getActivity().finish();
        }
    }

    public interface OnFragmentOperation {
        void onFailed();
    }
}

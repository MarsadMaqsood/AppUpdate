package com.marsad.appupdate.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.marsad.appupdate.R;
import com.marsad.appupdate.base.AbstractFragment;
import com.marsad.appupdate.services.VersionModel;
import com.marsad.appupdate.utils.Constant;
import com.marsad.appupdate.utils.NetworkUtils;
import com.marsad.appupdate.utils.PackageUtils;
import com.marsad.appupdate.utils.PublicFunctionUtils;
import com.marsad.appupdate.utils.ToastUtils;

public class UpdateDialog extends AbstractFragment implements View.OnClickListener {

    protected VersionModel mModel;
    protected String mToastMsg;
    protected String mUpdateTitle;
    protected String mUpdateContentText;
    protected boolean mIsShowToast;
    private UpdateActivity mActivity;

    public static UpdateDialog newInstance(VersionModel model, String toastMsg, boolean isShowToast) {
        Bundle args = new Bundle();
        args.putSerializable(Constant.MODEL, model);
        args.putString(Constant.TOAST_MSG, toastMsg);
        args.putBoolean(Constant.IS_SHOW_TOAST_MSG, isShowToast);
        UpdateDialog fragment = new UpdateDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static UpdateDialog newInstance(VersionModel model, String toastMsg, boolean isShowToast, String updateTitle) {
        Bundle args = new Bundle();
        args.putSerializable(Constant.MODEL, model);
        args.putString(Constant.TOAST_MSG, toastMsg);
        args.putBoolean(Constant.IS_SHOW_TOAST_MSG, isShowToast);
        args.putString(Constant.UPDATE_TITLE, updateTitle);
        UpdateDialog fragment = new UpdateDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static UpdateDialog newInstance(VersionModel model, String toastMsg, boolean isShowToast,
                                           String updateTitle, String updateContentText) {
        Bundle args = new Bundle();
        args.putSerializable(Constant.MODEL, model);
        args.putString(Constant.TOAST_MSG, toastMsg);
        args.putBoolean(Constant.IS_SHOW_TOAST_MSG, isShowToast);
        args.putString(Constant.UPDATE_TITLE, updateTitle);
        args.putString(Constant.UPDATE_CONTENT_TEXT, updateContentText);
        UpdateDialog fragment = new UpdateDialog();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        this.getActivity().getWindow().setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.shape_update_lib_dialog_bg, null));
        super.onCreate(savedInstanceState);
        mModel = (VersionModel) getArguments().getSerializable(Constant.MODEL);
        mToastMsg = getArguments().getString(Constant.TOAST_MSG);
        mIsShowToast = getArguments().getBoolean(Constant.IS_SHOW_TOAST_MSG);

        try {
            mUpdateTitle = getArguments().getString(Constant.UPDATE_TITLE);
        } catch (Exception e) {
            System.out.println("No title");
            e.printStackTrace();
        }

        try {
            mUpdateContentText = getArguments().getString(Constant.UPDATE_CONTENT_TEXT);
        } catch (Exception e) {
            System.out.println("No Content Text");
            e.printStackTrace();
        }


        closeIfNoNewVersionUpdate();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayout(), parent, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(view, R.id.tvTitle);
        setContent(view, R.id.tvContent);
    }

    private void closeIfNoNewVersionUpdate() {
        if (mModel.getVersionCode() <= PackageUtils.getVersionCode(getActivity().getApplicationContext())) {
            isLatest();
            getActivity().finish();
        }
    }

    private String getTitle() {
        return mUpdateTitle;
    }

    private String getContent() {

        if (mUpdateContentText != null && !mUpdateContentText.isEmpty()) {
            return mUpdateContentText;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(getActivity().getResources().getString(R.string.update_lib_version_code))
                .append(mModel.getVersionName())
                .append(".")
                .append(mModel.getVersionCode())
                .append("\n")
                .append("\n")
                .append(getActivity().getResources().getString(R.string.update_lib_update_content))
                .append("\n")
                .append(mModel.getContentText().replaceAll("#", "\\\n"));
        return sb.toString();
    }


    private void isLatest() {
        if (mIsShowToast) {
            ToastUtils.show(getActivity(),
                    TextUtils.isEmpty(mToastMsg) ? getResources().getString(R.string.update_lib_default_toast) : mToastMsg);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnCancel || id == R.id.imgBtnCancel) {
            onCancel();
        } else if (id == R.id.btnUpdate) {
            onUpdate();
        }
    }

    protected void onCancel() {
        getActivity().finish();
    }

    protected void onUpdate() {
        if (!NetworkUtils.getNetworkStatus(mActivity.getApplicationContext())) {
            return;
        }
        mActivity.showDownLoadProgress();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof UpdateActivity) {
            mActivity = (UpdateActivity) context;
        }
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_update;


    }

    @Override
    protected void setContent(View view, int contentId) {
        TextView tvContext = (TextView) view.findViewById(contentId);
        tvContext.setText(getContent());
    }

    @Override
    protected void setTitle(View view, int titleId) {
        TextView tvTitle = (TextView) view.findViewById(titleId);
        tvTitle.setText(getTitle());
    }


    protected void initIfMustUpdate(View view, int id) {
        if (PackageUtils.getVersionCode(mActivity.getApplicationContext()) < mModel.getMinSupport()) {
            view.findViewById(id).setVisibility(View.GONE);
            PublicFunctionUtils.setLastCheckTime(getActivity().getApplicationContext(), 0);
        }
    }

    @Override
    protected void initView(View view) {
        bindUpdateListener(view, R.id.btnUpdate);
        bindCancelListener(view, R.id.btnCancel);
        bindCancelListener(view, R.id.imgBtnCancel);
        initIfMustUpdate(view, R.id.btnCancel);
        initIfMustUpdate(view, R.id.imgBtnCancel);
    }

    @Override
    protected void bindUpdateListener(View view, int updateId) {
        view.findViewById(updateId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpdate();
            }
        });
    }

    @Override
    protected void bindCancelListener(View view, int cancelId) {
        view.findViewById(cancelId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });
    }
}

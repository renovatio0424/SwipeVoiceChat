package com.example.renov.swipevoicechat.Network.network;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.renov.swipevoicechat.R;


public class ProgressHandler<T> extends Interceptor<T> {

    private FragmentActivity activity;
    private boolean cancelable;
    private MaterialDialog progress;
    private boolean showOnlyProgress;

    public ProgressHandler(FragmentActivity activity, boolean cancelable) {
        this(activity, cancelable, false);
    }

    public ProgressHandler(FragmentActivity activity, boolean cancelable, boolean showOnlyProgress) {
        this.activity = activity;
        this.cancelable = cancelable;
        this.showOnlyProgress = showOnlyProgress;
    }

    public boolean isCancelable() {
        return cancelable;
    }

    public Activity getActivity() {
        return activity;
    }

    @Override
    public void onStart() {
        if (activity != null && !activity.isFinishing()) {
            try {
                if (!showOnlyProgress) {

                    progress = new MaterialDialog.Builder(activity)
                            .content("잠시만 기다려주세요")
                            .progress(true, 0)
                            .show();


                }

            } catch (Exception e) {
            }
        }
    }

    @Override
    public void onCancel() {
        dismissDialog();
    }

    @Override
    public void onError(HttpNetworkError error) {
        dismissDialog();
    }

    @Override
    public void onResponse(T response) {
        dismissDialog();
    }

    private void dismissDialog() {
        if (progress != null ) {
            progress.dismiss();
            progress = null;
        }
    }
}

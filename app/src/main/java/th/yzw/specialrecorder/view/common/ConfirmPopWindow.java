package th.yzw.specialrecorder.view.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.MyClickListener;

public class ConfirmPopWindow extends PopupWindow {
    private IDialogDismiss dialogDismiss = null;
    private boolean confirmFlag = false;
    private Activity mActivity;
    public boolean isResumeAlpha = true;
    private TextView textView,thirdTV;

    public ConfirmPopWindow(Activity activity){
        mActivity = activity;
        createView(activity);
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(true);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.PopWindowAnim);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if(isResumeAlpha)
                    darkenBackground(1.0f);
                if(dialogDismiss != null)
                    dialogDismiss.onDismiss(confirmFlag);
            }
        });
    }

    private void createView(Activity activity){
        View view = LayoutInflater.from(activity).inflate(R.layout.popwindow_confirm_layout,null);
        textView = view.findViewById(R.id.messageTV);
        thirdTV = view.findViewById(R.id.thirdTV);
        thirdTV.setVisibility(View.INVISIBLE);
        thirdTV.setEnabled(false);
        view.findViewById(R.id.cancelTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isResumeAlpha = true;
                dismiss();
            }
        });
        view.findViewById(R.id.confirmTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmFlag = true;
                dismiss();
            }
        });
        setContentView(view);
    }

    public ConfirmPopWindow setThirdButton(String title, final MyClickListener clickListener){
        thirdTV.setText(title);
        thirdTV.setVisibility(View.VISIBLE);
        thirdTV.setEnabled(true);
        thirdTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.OnClick(v,-1);
                isResumeAlpha = true;
                dismiss();
            }
        });
        return this;
    }

    public void show(String message, IDialogDismiss dialogDismiss){
        this.dialogDismiss = dialogDismiss;
        this.textView.setText(message);
        Window window = mActivity.getWindow();
        showAtLocation(window.getDecorView(), Gravity.BOTTOM,0,0);
        darkenBackground(0.5f);
    }

    private void darkenBackground(Float bgcolor) {
        if(mActivity == null)
            return;
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = bgcolor;
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mActivity.getWindow().setAttributes(lp);
    }

}

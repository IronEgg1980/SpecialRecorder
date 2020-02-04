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

public class ConfirmPopWindow extends PopupWindow {
    private IDialogDismiss dialogDismiss = null;
    private boolean confirmFlag = false;
    private Activity mActivity = null;
    public boolean isResumeAlpha = true;
//    private View contentView;
//    private ObjectAnimator enterAnim,exitAnim;

    public ConfirmPopWindow(Context context,String message){
        createView(context,message);
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
            }
        });
    }

//    private void createAnim(int height){
//        enterAnim = ObjectAnimator.ofFloat(contentView,"translationY",200f,0f)
//                .setDuration(200);
//        enterAnim.setStartDelay(200);
//        enterAnim.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//                contentView.setVisibility(View.VISIBLE);
//            }
//        });
//        exitAnim =  ObjectAnimator.ofFloat(contentView,"translationY",0.0f,200f)
//                .setDuration(200);
//        exitAnim.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                dismiss();
//            }
//        });
//    }
    private void createView(Context context,String message){
        View view = LayoutInflater.from(context).inflate(R.layout.popwindow_confirm_layout,null);
        TextView textView = view.findViewById(R.id.messageTV);
        textView.setText(message);
        view.findViewById(R.id.cancelTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
//        contentView = view.findViewById(R.id.contentGroup);
//        view.findViewById(R.id.root_relativeLayout).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               exitAnim.start();
//            }
//        });
//        createAnim(contentView.getMeasuredHeight());
//        contentView.setVisibility(View.INVISIBLE);
        setContentView(view);
    }

    public void show(Activity activity,IDialogDismiss dialogDismiss){
        this.dialogDismiss = dialogDismiss;
        this.mActivity =activity;
        Window window = activity.getWindow();
        showAtLocation(window.getDecorView(), Gravity.BOTTOM,0,-200);
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

    @Override
    public void dismiss() {
        if(dialogDismiss != null)
            dialogDismiss.onDismiss(confirmFlag);
        super.dismiss();
    }
}

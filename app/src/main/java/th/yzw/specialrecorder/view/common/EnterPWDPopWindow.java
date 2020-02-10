package th.yzw.specialrecorder.view.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.concurrent.RunnableFuture;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.Result;

public class EnterPWDPopWindow extends PopupWindow {
    private TextView textView;
    private MaterialEditText editText;
    private TextView cancel;
    private TextView confirm;
    private Result result;
    private IDialogDismiss dialogDismiss;
    private Activity activity;
    //    private InputMethodManager imm;
    private String passWord;

    private void createView() {
        View view = LayoutInflater.from(activity).inflate(R.layout.share_total_data_dialog, null);
        textView = view.findViewById(R.id.title);
        editText = view.findViewById(R.id.edit_text);
        cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result = Result.CANCEL;
                dismiss();
            }
        });
        confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (confirmInput()) {
                    result = Result.OK;
                    dismiss();
                }
            }
        });
        setContentView(view);
    }

    public EnterPWDPopWindow setDialogDismiss(IDialogDismiss dialogDismiss) {
        this.dialogDismiss = dialogDismiss;
        return this;
    }

    public EnterPWDPopWindow setIcon(Drawable drawable) {
        if (drawable != null) {
            drawable.setBounds(0, 0, 48, 48);
            this.textView.setCompoundDrawables(drawable, null, null, null);
        }
        return this;
    }

    private boolean confirmInput() {
        if (TextUtils.isEmpty(editText.getText())) {
            editText.setError("请输入密码！");
            editText.requestFocus();
            return false;
        }
        passWord = editText.getText().toString().trim();
        return true;
    }

    public EnterPWDPopWindow(Activity activity, String title, String hint) {
        this.activity = activity;
        passWord = "";
        result = Result.CANCEL;
        createView();
        editText.setHint(hint);
        textView.setText(title);

        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setAnimationStyle(R.style.PopWindowAnim);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                darkenBackground(1f);
                if (dialogDismiss != null)
                    dialogDismiss.onDismiss(result, passWord);
            }
        });
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(false);

        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void darkenBackground(Float bgcolor) {
        if (activity == null)
            return;
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgcolor;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        activity.getWindow().setAttributes(lp);
    }

    public void show() {
        showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        darkenBackground(0.5f);
        editText.requestFocus();
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 500);
//        ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,InputMethodManager.HIDE_NOT_ALWAYS);

    }
}

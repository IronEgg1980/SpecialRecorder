package th.yzw.specialrecorder.view.setup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.view.common.ToastFactory;

public class EditPWDPopWindow extends PopupWindow {
    private MaterialEditText newPWDET,confirmPWDET;
    private TextView confirmTV,cancelTV;
    private String value = "";
    private Activity activity;
//    private InputMethodManager imm;

    public EditPWDPopWindow(Activity activity){
        this.activity = activity;
//        imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        createView();

        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setAnimationStyle(R.style.PopWindowAnim);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                darkenBackground(1f);
//                imm.hideSoftInputFromWindow(confirmPWDET.getWindowToken(),0);
            }
        });
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(false);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private boolean getInputValue(){
        if(TextUtils.isEmpty(newPWDET.getText())){
            newPWDET.setError("请输入新密码！");
            newPWDET.requestFocus();
            return false;
        }
        if(TextUtils.isEmpty(confirmPWDET.getText())){
            confirmPWDET.setError("请确认密码！");
            confirmPWDET.requestFocus();
            return false;
        }
        String firstValue = newPWDET.getText().toString().trim();
        String secondValue = confirmPWDET.getText().toString().trim();
        if(!firstValue.equals(secondValue)){
            confirmPWDET.setError("两次输入的密码不一致！");
            confirmPWDET.requestFocus();
            confirmPWDET.selectAll();
            return false;
        }
        if ("110".equals(firstValue) || "0".equals(firstValue)) {
            newPWDET.requestFocus();
            newPWDET.setError("【" + firstValue + "】为系统保留代码，请重新设置密码！");
            return false;
        }
        if(TextUtils.isDigitsOnly(firstValue))
            value = firstValue;
        else
            return false;
        return true;
    }

    private void createView(){
        View view = LayoutInflater.from(activity).inflate(R.layout.password_setup_dialog,null);
        newPWDET = view.findViewById(R.id.setup_newPWD_edittext);
        confirmPWDET = view.findViewById(R.id.setup_confirmPWD_edittext);
        confirmTV = view.findViewById(R.id.confirm);
        cancelTV = view.findViewById(R.id.cancel);
        confirmTV.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                if(getInputValue()){
                    AppSetupOperator.savePassWord(value);
                    new ToastFactory(activity).showCenterToast("修改成功！");
                    dismiss();
                }
            }
        });
        cancelTV.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                dismiss();
            }
        });
        setContentView(view);
    }

    private void darkenBackground(Float bgcolor) {
        if(activity == null)
            return;
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgcolor;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        activity.getWindow().setAttributes(lp);
    }

    public void show(){
        showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM,0,0);
        darkenBackground(0.5f);
        newPWDET.requestFocus();
        newPWDET.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 500);
//        ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_NOT_ALWAYS);
    }

}

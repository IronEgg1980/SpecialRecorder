package th.yzw.specialrecorder.view.setup;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.view.common.DialogFactory;
import th.yzw.specialrecorder.view.common.ToastFactory;

public class EditPassWordDialogFragment extends DialogFragment {
    private EditText newPWDET,confirmPWDET;
    private TextView confirmTV,cancelTV;
    private String value;

    private void initialView(View view){
        newPWDET = view.findViewById(R.id.setup_newPWD_edittext);
        confirmPWDET = view.findViewById(R.id.setup_confirmPWD_edittext);
        confirmTV = view.findViewById(R.id.confirm);
        cancelTV = view.findViewById(R.id.cancel);
    }

    private boolean getInputValue(){
        if(TextUtils.isEmpty(newPWDET.getText())||TextUtils.isEmpty(confirmPWDET.getText())){
            newPWDET.setError("请输入新密码，密码不能为空格！");
            newPWDET.requestFocus();
            return false;
        }
        String firstValue = newPWDET.getText().toString().trim();
        String secondValue = newPWDET.getText().toString().trim();
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        value = "";
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.password_setup_dialog,container,false);
        initialView(view);
        confirmTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getInputValue()){
                    AppSetupOperator.savePassWord(value);
                    new ToastFactory(getContext()).showCenterToast("修改成功！");
                    dismiss();
                }
            }
        });
        cancelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = (int) (dm.widthPixels * 0.75);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.setCanceledOnTouchOutside(true);
            Window window = dialog.getWindow();
            if (window!=null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(width, height);
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                newPWDET.requestFocus();
                window.setWindowAnimations(R.style.EditDialogAnim);
            }
        }
    }
}

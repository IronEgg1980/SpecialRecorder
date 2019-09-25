package th.yzw.specialrecorder.view.show_total;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;

public class ShareTotalDataDialogFragment extends DialogFragment {
    public void setOnDismissListener(IDialogDismiss onDismissListener) {
        this.onDismissListener = onDismissListener;
    }
    private IDialogDismiss onDismissListener;
    private EditText editText;
    private TextView cancel,titleTV,infoTV;
    private TextView confirm;
    private String passWord,title,info;
    private boolean isConfirm;
    private boolean confirmInput(){
        if(TextUtils.isEmpty(editText.getText())){
            editText.setError("请输入密码！");
            editText.requestFocus();
            return false;
        }
        passWord = editText.getText().toString().trim();
        return true;
    }
    private void hideInputKeyboard(){
        InputMethodManager inputMethodManager =(InputMethodManager)  editText.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static ShareTotalDataDialogFragment getInstance(String title,String info){
        ShareTotalDataDialogFragment fragment = new ShareTotalDataDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title",title);
        bundle.putString("info",info);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = "发送数据";
        info = "设置密码：";
        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString("title");
            info = bundle.getString("info");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        passWord = "";
        isConfirm = false;
        View view = inflater.inflate(R.layout.share_total_data_dialog,container,false);
        editText = view.findViewById(R.id.edit_text);
        cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConfirm = false;
                dismiss();
            }
        });
        confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(confirmInput()) {
                    isConfirm = true;
                    dismiss();
                }
            }
        });
        titleTV = view.findViewById(R.id.title);
        titleTV.setText(title);
        infoTV = view.findViewById(R.id.info_textview);
        infoTV.setText(info);
        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        hideInputKeyboard();
        onDismissListener.onDismiss(isConfirm,passWord);
        super.onDismiss(dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if(dialog!=null){
            dialog.setCanceledOnTouchOutside(false);
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            Window window = dialog.getWindow();
            if (window!=null) {
                window.setLayout((int) (dm.widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                editText.requestFocus();
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                window.setWindowAnimations(R.style.EditDialogAnim);
            }
        }
    }
}

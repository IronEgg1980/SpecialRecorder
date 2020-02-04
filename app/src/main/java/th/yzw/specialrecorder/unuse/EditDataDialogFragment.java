package th.yzw.specialrecorder.unuse;

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

public class EditDataDialogFragment extends DialogFragment {

    private String mName;
    private int mOldValue,newValue;
    private IDialogDismiss onDissmissListener;
    private EditText editText;
    private boolean isConfirm ;


    public void setOnDissmissListener(IDialogDismiss onDissmissListener) {
        this.onDissmissListener = onDissmissListener;
    }

    public static EditDataDialogFragment newInstant(String name,int oldValue){
        EditDataDialogFragment fragment = new EditDataDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("name",name);
        bundle.putInt("value",oldValue);
        fragment.setArguments(bundle);
        return fragment;
    }
    private boolean confirmInput(){
        if(editText!=null){
            if(TextUtils.isEmpty(editText.getText())){
                editText.setError("请输入数据!");
                editText.requestFocus();
                return false;
            }
            newValue = Integer.valueOf(editText.getText().toString());
            if(newValue<=0){
                editText.setError("请输入有效数据!");
                editText.requestFocus();
                editText.selectAll();
                return false;
            }
            return true;
        }else{
            return false;
        }
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            this.mName = getArguments().getString("name");
            this.mOldValue = getArguments().getInt("value");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_data_dialog_fragment,container,false);
        TextView infoTv = view.findViewById(R.id.info_tv);
        String s = "名称："+mName+"，请输入数量";
        infoTv.setText(s);
        editText = view.findViewById(R.id.edit_text);
        editText.setText(String.valueOf(mOldValue));
        TextView cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConfirm =false;
                newValue = 0;
                hideInputKeyboard();
                dismiss();
            }
        });
        TextView confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(confirmInput()) {
                    if(newValue == mOldValue){
                        isConfirm =false;
                    }else {
                        isConfirm = true;
                    }
                    hideInputKeyboard();
                    dismiss();
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        if (dialog != null) {
            dialog.setCanceledOnTouchOutside(false);
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            Window window = dialog.getWindow();
            if (window!=null) {
                window.setLayout((int) (dm.widthPixels * 0.8), ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                window.setWindowAnimations(R.style.EditDialogAnim);
            }
        }
        editText.requestFocus();
        editText.selectAll();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        onDissmissListener.onDismiss(isConfirm,newValue);
        super.onDismiss(dialog);
    }

    private void hideInputKeyboard(){
        InputMethodManager inputMethodManager =(InputMethodManager)  editText.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}

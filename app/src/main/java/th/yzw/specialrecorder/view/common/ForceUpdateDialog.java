package th.yzw.specialrecorder.view.common;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.interfaces.Result;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ForceUpdateDialog extends DialogFragment {
    private String mMessage = "";
    private Result result;
    private IDialogDismiss dialogDismiss = null;

    public ForceUpdateDialog setDialogDismiss(IDialogDismiss dialogDismiss) {
        this.dialogDismiss = dialogDismiss;
        return this;
    }

    public static ForceUpdateDialog getInstant(String message){
        ForceUpdateDialog  forceUpdateDialog = new ForceUpdateDialog();
        Bundle bundle = new Bundle();
        bundle.putString("message",message);
        forceUpdateDialog.setArguments(bundle);
        return forceUpdateDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if(dialogDismiss != null)
            dialogDismiss.onDismiss(result);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle!=null){
            mMessage = bundle.getString("message");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popwindow_confirm_layout, container,false);
        TextView textView = view.findViewById(R.id.messageTV);
        textView.setText(mMessage);
        view.findViewById(R.id.thirdTV).setVisibility(View.INVISIBLE);
        TextView cancelTV = view.findViewById(R.id.cancelTV);
        cancelTV.setText("退\t\t出");
        cancelTV.setOnClickListener(new NoDoubleClickListener(){
            @Override
            public void onNoDoubleClick(View v) {
                result = Result.CANCEL;
                dismiss();
            }
        });
        TextView confirmTV = view.findViewById(R.id.confirmTV);
        confirmTV.setText("更\t\t新");
        confirmTV.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                result = Result.OK;
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if(dialog!=null){
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            Window window = dialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(MATCH_PARENT,WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }
    }
}

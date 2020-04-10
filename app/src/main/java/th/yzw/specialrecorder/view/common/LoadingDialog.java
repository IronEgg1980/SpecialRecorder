package th.yzw.specialrecorder.view.common;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Locale;
import java.util.Objects;

import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.R;

public class LoadingDialog extends DialogFragment {
    private String mTitle, mInitialContent;
    private TextView infoTv;
    private TextView progressText;
    private ProgressBar progressBar;
    private boolean hideCancelButton;
    private BroadcastReceiver broadcastReceiver;

    private void initialBroast(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(Objects.equals(intent.getAction(), Broadcasts.CHANGE_LOADING_TEXT)){
                    String message = intent.getStringExtra("message");
                    changeText(message);
                }else if(Objects.equals(intent.getAction(), Broadcasts.CHANGE_LOADING_PROGRESS)){
                    int progress = intent.getIntExtra("progress",1);
                    float value = intent.getFloatExtra("value",1f);
                    changeProgress(progress,value);
                }else{
                    dismiss();
                }
            }
        };
        String[] actions = {Broadcasts.CHANGE_LOADING_TEXT,Broadcasts.CHANGE_LOADING_PROGRESS,Broadcasts.DISMISS_DIALOG};
        Broadcasts.bindBroadcast(getContext(),broadcastReceiver,actions);
    }

    public static LoadingDialog newInstant(String title, String initialContent, boolean hideCancelButton) {
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("initialContent", initialContent);
        bundle.putBoolean("cancelButtonVisible", hideCancelButton);
        LoadingDialog dialog = new LoadingDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialBroast();
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mTitle = bundle.getString("title");
            this.mInitialContent = bundle.getString("initialContent");
            this.hideCancelButton = bundle.getBoolean("cancelButtonVisible");
        } else {
            this.mTitle = "正在更新";
            this.mInitialContent = "开始查找文件...";
            this.hideCancelButton = false;
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        // 原来的commit()方法换成了commitAllowingStateLoss()
        ft.commitAllowingStateLoss();
    }

    public void setCancelClick(View.OnClickListener cancelClick) {
        this.cancelClick = cancelClick;
    }

    private View.OnClickListener cancelClick;

    public void changeText(String text) {
        if (infoTv != null)
            infoTv.setText(text);
    }

    public void changeProgress(int value, float progress) {
        if (progressBar != null && progressText != null) {
            progressBar.setProgress(value);
            progressText.setText(String.format(Locale.CHINA,"%1$.2f%2$s",progress,"%"));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loading_dialog_layout, container, false);
        infoTv = view.findViewById(R.id.infoTv);
        TextView titleTv = view.findViewById(R.id.title);
        titleTv.setText(mTitle);
        infoTv.setText(mInitialContent);
        TextView cancelTv = view.findViewById(R.id.cancelTv);
        cancelTv.setOnClickListener(cancelClick);
        if (hideCancelButton)
            cancelTv.setVisibility(View.GONE);
        else
            cancelTv.setVisibility(View.VISIBLE);
        progressBar = view.findViewById(R.id.progressBar);
        progressText = view.findViewById(R.id.progressText);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        changeProgress(0, 0f);
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = (int) (dm.widthPixels * 0.8);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.setCanceledOnTouchOutside(false);
            Window window = dialog.getWindow();
            if (window!=null) {
                window.setWindowAnimations(R.style.CommonDialogAnim);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(width, height);
            }
        }
    }

    @Override
    public void dismiss() {
        Broadcasts.unBindBroadcast(getContext(),broadcastReceiver);
        super.dismiss();
    }
}

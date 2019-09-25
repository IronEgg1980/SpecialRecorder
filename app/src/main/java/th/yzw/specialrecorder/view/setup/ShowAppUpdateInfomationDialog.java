package th.yzw.specialrecorder.view.setup;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.tools.FileTools;

public class ShowAppUpdateInfomationDialog extends DialogFragment {
    public static ShowAppUpdateInfomationDialog newInstant(String path) {
        ShowAppUpdateInfomationDialog showAppUpdateInfomationDialog = new ShowAppUpdateInfomationDialog();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        showAppUpdateInfomationDialog.setArguments(bundle);
        return showAppUpdateInfomationDialog;
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            switch (msg.what) {
                case 0:
                    changeInformation("正在查询文件信息...");
                    break;
                case 1:
                    String info = bundle.getString("content");
//                    updateFile = new File(bundle.getString("file"));
                    changeInformation(info);
                    confirm.setEnabled(true);
                    break;
                case 2:
                    String info2 = "查询失败！原因：\n" + bundle.getString("error");
                    changeInformation(info2);
                    break;
                case 3:
                    changeInformation("没有找到更新文件。");
                    break;
            }
            return true;
        }
    });

    private void initialBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                readEmailFile();
            }
        };
        Broadcasts.bindBroadcast(getContext(), receiver, Broadcasts.APP_UPDATEFILE_DOWNLOAD_SUCCESS);
    }


    private void readEmailFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
                File[] files = FileTools.readEmailFile(cachePath);
                if (files != null && files.length == 2) {
                    try {
                        File textFile = files[0];
                        File zipFile = files[1];
                        if (textFile != null && zipFile !=null) {
                            String content = FileTools.readContentText(textFile);
                            zipFilePath = zipFile.getAbsolutePath();
                            Bundle bundle = new Bundle();
                            bundle.putString("content", content);
                            Message handlerMSG = new Message();
                            handlerMSG.what = 1;
                            handlerMSG.setData(bundle);
                            handler.sendMessage(handlerMSG);
                        }else{
                            handler.sendEmptyMessage(3);
                        }
                    } catch (IOException | NullPointerException e) {
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("error","读取文件失败！\n"+ e.getMessage());
                        Message handlerMSG = new Message();
                        handlerMSG.what = 2;
                        handlerMSG.setData(bundle1);
                        handler.sendMessage(handlerMSG);
                        e.printStackTrace();
                    }
                }else{
                    handler.sendEmptyMessage(3);
                }
            }
        }).start();
    }

    private IDialogDismiss onDismiss;
    private BroadcastReceiver receiver;

    public void setOnDismiss(IDialogDismiss onDismiss) {
        this.onDismiss = onDismiss;
    }

    private TextView dialogAppupdateContentTextview;
    TextView confirm;
    private String cachePath;
    private String zipFilePath = "";
    private boolean isBeginInstall;


    private void initialView(View view) {
        dialogAppupdateContentTextview = view.findViewById(R.id.dialog_appupdate_content_textview);
        TextView cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBeginInstall = false;
                dismiss();
            }
        });
        confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBeginInstall = true;
                dismiss();
            }
        });
        confirm.setEnabled(false);
    }

    private void changeInformation(String info) {
        if (dialogAppupdateContentTextview != null)
            dialogAppupdateContentTextview.setText(info);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        cachePath = "";
        if (bundle != null)
            cachePath = bundle.getString("path");
        initialBroadcastReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_show_appupdate_info, container, false);
        initialView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = (int) (dm.widthPixels * 0.8);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.setCanceledOnTouchOutside(false);
            Window window = dialog.getWindow();
            if (window != null) {
                window.setWindowAnimations(R.style.CommonDialogAnim);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(width, height);
            }
        }
        readEmailFile();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        // 注销该广播接收器
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
        super.onDismiss(dialog);
        onDismiss.onDismiss(isBeginInstall, zipFilePath);
    }
}

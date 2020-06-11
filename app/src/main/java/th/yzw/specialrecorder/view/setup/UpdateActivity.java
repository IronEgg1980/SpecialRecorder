package th.yzw.specialrecorder.view.setup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.PopupWindow;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import th.yzw.specialrecorder.DAO.AppUpdater;
import th.yzw.specialrecorder.MyActivity;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.InfoPopWindow;
import th.yzw.specialrecorder.view.common.LoadingDialog;

public class UpdateActivity extends MyActivity {
    private LoadingDialog loadingDialog;
    private File zipFile;
    private AppUpdater updater;
    private boolean isRestart = true;
    private Uri uri;
    private Handler handler;
    private Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_item);
        loadingDialog = LoadingDialog.newInstant("开始升级", "正在打开升级文件...", false);
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            uri = intent.getData();
            readFile();
        }
        loadingDialog.setCancelClick(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                updater.cancleUpdate();
            }
        });
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.what == 0x01){
                    updateApp();
                }
                return true;
            }
        });
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                readFile();
                if(zipFile!=null)
                    handler.sendEmptyMessage(0x01);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBoolean("isRestart", isRestart);
        outState.putParcelable("uri", uri);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        isRestart = savedInstanceState.getBoolean("isRestart");
        uri = savedInstanceState.getParcelable("uri");
        readFile();
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void readFile() {
        zipFile = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            zipFile = new File(FileTools.appCache, "updateApp.tmp");
            outputStream = new FileOutputStream(zipFile);
            int c;
            byte[] buffer = new byte[8 * 1024];
            while ((c = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, c);
            }
        } catch (IOException e) {
            zipFile = null;
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateApp() {
        if (zipFile == null) {
            InfoPopWindow infoPopWindow = new InfoPopWindow(this);
            infoPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    finish();
                }
            });
            infoPopWindow.show("打开文件失败，请重试一次！");
            return;
        }
        updater = new AppUpdater(this, zipFile);
        updater.setOnFinish(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                loadingDialog.dismiss();
                if (result == Result.OK) {
                    File apkFile = (File) values[0];
                    if (apkFile != null) {
                        OtherTools.openAPKFile(UpdateActivity.this, apkFile);
                        finish();
                    } else {
                        String s = (String) values[0];
                        InfoPopWindow infoPopWindow = new InfoPopWindow(UpdateActivity.this);
                        infoPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                finish();
                            }
                        });
                        infoPopWindow.show(s);
                    }
                } else {
                    String s = (String) values[0];
                    InfoPopWindow infoPopWindow = new InfoPopWindow(UpdateActivity.this);
                    infoPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            finish();
                        }
                    });
                    infoPopWindow.show(s);
                }
            }
        });
        loadingDialog.show(getSupportFragmentManager(), "loading");
        updater.execute();
    }

    private void requestInstall() {
        if (hasInstallPermission()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                thread.start();
            } else {
                ActivityCompat.requestPermissions(this, PERMISSION_GROUP_STORAGE, 1000);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isRestart) {
            isRestart = false;
            requestInstall();
        }
    }

    @Override
    protected void onPermissionGranted(int requestCode) {
        requestInstall();
    }

    @Override
    protected void onPermissionDenied(int requestCode) {
        String s = "";
        if (requestCode == REQUST_CODE_INSTALL) {
            s = "请授予安装未知来源软件权限，以继续更新软件操作！";
        } else {
            s = "请授予使用存储权限，以继续更新软件操作！";
        }
        new ConfirmPopWindow(this)
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            requestInstall();
                        } else {
                            finish();
                        }
                    }
                })
                .toConfirm(s);
    }
}

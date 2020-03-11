package th.yzw.specialrecorder.view.setup;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.PopupWindow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import th.yzw.specialrecorder.DAO.ItemUpdater;
import th.yzw.specialrecorder.MyActivity;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.InfoPopWindow;
import th.yzw.specialrecorder.view.common.LoadingDialog;

public class UpdateItemActivity extends MyActivity {

    private File updateFile = null;
    private LoadingDialog loadingDialog;
    private InfoPopWindow infoPopWindow;
    private Handler handler;
    private Thread thread;
    private Uri uri;
    private boolean isRestart = true;

    private void updateItem() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
            thread.start();
        }else{
            ActivityCompat.requestPermissions(this,PERMISSION_GROUP_STORAGE,1001);
        }
    }

    private void update(){
        if (updateFile == null) {
            infoPopWindow.show("打开文件错误！");
        } else {
            ItemUpdater itemUpdater = new ItemUpdater(this, updateFile);
            itemUpdater.setOnFinished(new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... values) {
                    loadingDialog.dismiss();
                    String s = (String) values[0];
                    infoPopWindow.show(s);
                }
            });
            loadingDialog.show(getSupportFragmentManager(), "loading");
            itemUpdater.execute();
        }
    }

    private void readFile() {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            updateFile = new File(getCacheDir(), "updateItem.tmp");
            outputStream = new FileOutputStream(updateFile);
            int c;
            byte[] buffer = new byte[1024];
            while ((c = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, c);
            }
        } catch (IOException e) {
            updateFile = null;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_item);
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            uri = intent.getData();
            readFile();
        }
        loadingDialog = LoadingDialog.newInstant("正在更新", "正在查找文件...", true);
        loadingDialog.setCancelClick(null);
        loadingDialog.setCancelable(false);
        infoPopWindow = new InfoPopWindow(this);
        infoPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                finish();
            }
        });
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if(msg.what == 0xa1){
                    update();
                }
                return true;
            }
        });
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                readFile();
                if(updateFile != null){
                    handler.sendEmptyMessage(0xa1);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("uri",uri);
        outState.putBoolean("isRestart",isRestart);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        uri = savedInstanceState.getParcelable("uri");
        isRestart = savedInstanceState.getBoolean("isRestart");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isRestart) {
            isRestart = false;
            updateItem();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateFile = null;
    }

    @Override
    protected void onPermissionGranted(int requestCode) {
        updateItem();
    }

    @Override
    protected void onPermissionDenied(int requestCode) {
        new ConfirmPopWindow(this)
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if(result == Result.OK){
                            updateItem();
                        }else{
                            finish();
                        }
                    }
                })
                .toConfirm("请授予使用存储权限，以继续更新软件操作！");
    }
}

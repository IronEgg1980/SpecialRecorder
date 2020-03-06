package th.yzw.specialrecorder.view.setup;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import th.yzw.specialrecorder.DAO.AppUpdater;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.tools.PermissionHelper;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.InfoPopWindow;
import th.yzw.specialrecorder.view.common.LoadingDialog;

public class UpdateActivity extends AppCompatActivity {
    private LoadingDialog loadingDialog;
    private File zipFile;
    private AppUpdater updater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_item);
        loadingDialog = LoadingDialog.newInstant("开始升级", "正在打开升级文件...", false);
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            readFile(uri);
        }
        loadingDialog.setCancelClick(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                updater.cancleUpdate();
            }
        });
    }

    private void readFile(Uri uri) {
        zipFile = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            zipFile = new File(getCacheDir(), "updateApp.tmp");
//            if(updateFile.exists() && updateFile.delete())
//                updateFile.createNewFile();
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
            new InfoPopWindow(this).show("打开文件失败");
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
                        new ConfirmPopWindow(UpdateActivity.this).setDialogDismiss(new IDialogDismiss() {
                            @Override
                            public void onDismiss(Result result, Object... values) {
                                finish();
                            }
                        }).toConfirm(s);
                    }
                } else {
                    String s = (String) values[0];
                    new ConfirmPopWindow(UpdateActivity.this).setDialogDismiss(new IDialogDismiss() {
                        @Override
                        public void onDismiss(Result result, Object... values) {
                            finish();
                        }
                    }).toConfirm(s);
                }
            }
        });
        loadingDialog.show(getSupportFragmentManager(), "loading");
        updater.execute();
    }

    private void requestInstall(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !getPackageManager().canRequestPackageInstalls()) {
            ConfirmPopWindow confirmPopWindow = new ConfirmPopWindow(this);
            confirmPopWindow.setDialogDismiss(new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... values) {
                    if(result == Result.OK){
                        PermissionHelper helper = new PermissionHelper(UpdateActivity.this, UpdateActivity.this, new PermissionHelper.OnResult() {
                            @Override
                            public void hasPermission(boolean flag) {
                                if(flag) {
                                    requestInstall();
                                }else{
                                    finish();
                                }
                            }
                        });
                        helper.request(Permission.REQUEST_INSTALL_PACKAGES);
                    }else{
                        finish();
                    }
                }
            }).toConfirm("请授予安装未知来源软件的权限。");
        } else if (!XXPermissions.isHasPermission(this, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionHelper helper = new PermissionHelper(UpdateActivity.this, UpdateActivity.this, new PermissionHelper.OnResult() {
                @Override
                public void hasPermission(boolean flag) {
                    if(flag) {
                        requestInstall();
                    }else{
                        finish();
                    }
                }
            });
            helper.request(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);
        } else {
            updateApp();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestInstall();
    }
}

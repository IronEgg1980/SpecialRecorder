package th.yzw.specialrecorder.view.setup;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.tools.PermissionHelper;
import th.yzw.specialrecorder.view.common.DialogFactory;
import th.yzw.specialrecorder.view.common.LoadingDialog;

public class UpdateActivity extends AppCompatActivity {
    private LoadingDialog loadingDialog;
    private File zipFile;
    private AppUpdater updater;
    private DialogFactory dialogAndToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_item);
        loadingDialog = LoadingDialog.newInstant("开始升级", "正在打开升级文件...", false);
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            zipFile = readFile(uri);
        }
        loadingDialog.setCancelClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updater.cancleUpdate();
            }
        });
        dialogAndToast = new DialogFactory(this);
    }

    private File readFile(Uri uri) {
        File result = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            result = new File(getCacheDir(), "updateApp.tmp");
//            if(updateFile.exists() && updateFile.delete())
//                updateFile.createNewFile();
            outputStream = new FileOutputStream(result);
            int c;
            byte[] buffer = new byte[8 * 1024];
            while ((c = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, c);
            }
        } catch (IOException e) {
            result = null;
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
        return result;
    }

    private void updateApp() {
        if (zipFile == null) {
            dialogAndToast.showInfoDialog("打开文件失败");
            return;
        }
        updater = new AppUpdater(this, zipFile);
        updater.setOnFinish(new IDialogDismiss() {
            @Override
            public void onDismiss(boolean isConfirmed, Object... values) {
                loadingDialog.dismiss();
                if (isConfirmed) {
                    File apkFile = (File) values[0];
                    if (apkFile != null) {
                        OtherTools.openAPKFile(UpdateActivity.this, apkFile);
                        finish();
                    } else {
                        String s = (String) values[0];
                        dialogAndToast.showInfoDialog(s, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });
                    }
                } else {
                    String s = (String) values[0];
                    dialogAndToast.showInfoDialog(s, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
                }
            }
        });
        loadingDialog.show(getSupportFragmentManager(), "loading");
        updater.execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&!getPackageManager().canRequestPackageInstalls()) {
//            OtherTools.startInstallPermissionSettingActivity(UpdateActivity.this);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setIcon(R.drawable.ic_info_cyan_800_18dp)
                    .setMessage("请授予安装未知来源软件的权限。")
                    .setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PermissionHelper helper = new PermissionHelper(UpdateActivity.this, UpdateActivity.this, new PermissionHelper.OnResult() {
                                @Override
                                public void hasPermission() {
                                }
                            });
                            helper.setCancel(new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                            helper.request(Permission.REQUEST_INSTALL_PACKAGES);
                        }
                    })
                    .setNegativeButton("取消操作", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.show();
        } else if(!XXPermissions.isHasPermission(this,Permission.READ_EXTERNAL_STORAGE,Permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionHelper helper = new PermissionHelper(UpdateActivity.this, UpdateActivity.this, new PermissionHelper.OnResult() {
                @Override
                public void hasPermission() {
                   dialogAndToast.showInfoDialog("已授权，请重新点击升级文件安装。", new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialog, int which) {
                           finish();
                       }
                   });
                }
            });
            helper.setCancel(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            helper.request(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);
        }else{
            updateApp();
        }
    }
}

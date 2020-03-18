package th.yzw.specialrecorder.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import th.yzw.specialrecorder.ActivityManager;
import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.AppUpdater;
import th.yzw.specialrecorder.MyActivity;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.model.AppSetup;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.ForceUpdateDialog;
import th.yzw.specialrecorder.view.common.InfoPopWindow;
import th.yzw.specialrecorder.view.common.LoadingDialog;
import th.yzw.specialrecorder.view.common.ToastFactory;
import th.yzw.specialrecorder.view.input_data.KeyboardInputFragment;
import th.yzw.specialrecorder.view.input_data.TouchInputDataFragment;
import th.yzw.specialrecorder.view.merge_data.MergeDataActivity;
import th.yzw.specialrecorder.view.setup.SetupActivity;
import th.yzw.specialrecorder.view.setup.ShowAppUpdateInfomationDialog;
import th.yzw.specialrecorder.view.show_details.ShowDetailsActivity;

public class RecorderActivity extends MyActivity {
    private String TAG  = "殷宗旺";
    private final int UPDATE_FLAG = 0xb1,STORAGE_FLAG = 0xc1;
    private FragmentManager fragmentManager;
    private long firstTouch;
    private BroadcastReceiver receiver;
    private ToastFactory toastFactory;
    private DrawerLayout drawerLayout;
    private TextView badgeView;
    private int tipTimes;
    private boolean updateInfomationNotShown = true;

    private void showTips() {
        if (tipTimes > 0) {
            tipTimes--;
            AppSetupOperator.setTipsTimes(tipTimes);
            toastFactory.showLongToast("点击左上角图标或右滑以打开菜单");
        }
    }

    private void initialView() {
        drawerLayout = findViewById(R.id.drawerLayout);
        findViewById(R.id.menu_show_details_tv).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                showDetails();
            }
        });
        findViewById(R.id.menu_show_all_tv).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                showAll();
            }
        });
        findViewById(R.id.menu_merge_tv).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                mergeData();
            }
        });
        findViewById(R.id.menu_setup_tv).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                setup();
            }
        });
        findViewById(R.id.menuGroup).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                drawerLayout.closeDrawers();
            }
        });
        badgeView = findViewById(R.id.appUpdatedFlag);
    }

    private void showDetails() {
        drawerLayout.closeDrawers();
        Intent intent = new Intent(RecorderActivity.this, ShowDetailsActivity.class);
        startActivity(intent);
    }

    private void showAll() {
        drawerLayout.closeDrawers();
        Intent intent = new Intent(RecorderActivity.this, ShowTotalDataActivity.class);
        startActivity(intent);
    }

    private void mergeData() {
        drawerLayout.closeDrawers();
        Intent intent = new Intent(RecorderActivity.this, MergeDataActivity.class);
        startActivity(intent);
    }

    private void setup() {
        drawerLayout.closeDrawers();
        Intent intent = new Intent(RecorderActivity.this, SetupActivity.class);
        startActivity(intent);
    }

    private void showUpdateInfo() {
        badgeView.setVisibility(View.INVISIBLE);
        long currentVersion = AppSetupOperator.getLastAppVersion();
        long downloadVersion = AppSetupOperator.getDownloadAppVersion();
        if (downloadVersion > currentVersion) {
            badgeView.setVisibility(View.VISIBLE);
            if (AppSetupOperator.isForceUpdate()) {
                //force update
                showForceUpdateDialog();
            } else {
                showUpdateDialog();
            }
        }
    }

    private void beginUpdate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (getPackageManager().canRequestPackageInstalls()) {
                openUpdater();
            } else {
                Uri packageURI = Uri.parse("package:" + getPackageName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                startActivityForResult(intent, UPDATE_FLAG);
            }
        } else {
            openUpdater();
        }
    }

    private void openUpdater() {
        File zipFile = FileTools.getAppUpdateFile();
        if (zipFile == null) {
            toastFactory.showCenterToast("未找到升级文件！");
            AppSetupOperator.setForceUpdate(false);
        } else {
            LoadingDialog loadingDialog = LoadingDialog.newInstant("正在更新", "正在打开文件...", false);
            loadingDialog.setCancelable(false);
            AppUpdater updater = new AppUpdater(RecorderActivity.this, zipFile);
            updater.setOnFinish(new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... values) {
                    if (result == Result.OK) {
                        File updateFile = (File) values[0];
                        if (updateFile != null) {
                            OtherTools.openAPKFile(RecorderActivity.this, updateFile);
                        } else {
                            AppSetupOperator.setForceUpdate(false);
                            toastFactory.showCenterToast("安装文件损坏");
                        }
                    }
                }
            });
            loadingDialog.show(fragmentManager, "loading");
            updater.execute();
        }
    }

    private void showUpdateDialog() {
        ShowAppUpdateInfomationDialog showAppUpdateInfomationDialog = new ShowAppUpdateInfomationDialog();
        showAppUpdateInfomationDialog.setOnDismiss(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if (result == Result.OK) {
                    beginUpdate();
                }
            }
        });
        showAppUpdateInfomationDialog.show(getSupportFragmentManager(), "showUpdateInfo");
    }

    private void showForceUpdateDialog() {
        ForceUpdateDialog.getInstant("需要更新至最新版本才能正常使用，否则将退出程序。请点击更新！")
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            beginUpdate();
                        } else {
                            ActivityManager.closeAll();
                        }
                    }
                })
                .show(getSupportFragmentManager(), "showforceupdatedialog");
    }

    private void initialBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showUpdateInfo();
            }
        };
        Broadcasts.bindBroadcast(this, receiver, Broadcasts.APP_UPDATEFILE_DOWNLOAD_SUCCESS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.menu);
        setTitle("首页");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });
        firstTouch = 0;
        fragmentManager = getSupportFragmentManager();
        toastFactory = new ToastFactory(RecorderActivity.this);
        tipTimes = AppSetupOperator.getTipsTimes();

        initialView();
        initialBroadcastReceiver();
        showTips();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Fragment fragment;
        if (AppSetupOperator.getInputMethod() == 1) {
            fragment = new KeyboardInputFragment();
        } else {
            fragment = new TouchInputDataFragment();
        }
        fragmentManager.beginTransaction().replace(R.id.framelayout, fragment).commit();
        if (updateInfomationNotShown) {
            updateInfomationNotShown = false;
            showUpdateInfo();
        }
    }

    @Override
    protected void onDestroy() {
        // 注销该广播接收器
        Broadcasts.unBindBroadcast(this, receiver);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (drawerLayout.isDrawerOpen(Gravity.START)) {
                drawerLayout.closeDrawers();
                return true;
            }
            long touchTime = System.currentTimeMillis();
            if (touchTime - firstTouch > 2000) {
                firstTouch = touchTime;
                new ToastFactory(this).showCenterToast("再按一次退出");
            } else {
                ActivityManager.closeAll();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            openUpdater();
        } else {
            ConfirmPopWindow confirmPopWindow = new ConfirmPopWindow(this);
            confirmPopWindow.setDialogDismiss(new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... values) {
                    if (result == Result.OK) {
                        beginUpdate();
                    } else {
                        InfoPopWindow infoPopWindow = new InfoPopWindow(RecorderActivity.this);
                        infoPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                if (AppSetupOperator.isForceUpdate()) {
                                    ActivityManager.closeAll();
                                }
                            }
                        });
                        infoPopWindow.show("已取消更新");
                    }
                }
            });
            confirmPopWindow.toConfirm("未授予安装应用权限，安装无法完成！重新授予权限以继续更新吗？\n【确定】继续安装，【取消】退出安装。");
        }
    }
//
//    @Override
//    protected void onPermissionGranted(int requestCode) {
//        if(requestCode == STORAGE_FLAG){
//            beginUpdate();
//        }
//    }
//
//    @Override
//    protected void onPermissionDenied(int requestCode) {
//        if(requestCode == STORAGE_FLAG){
//            new ConfirmPopWindow(this)
//                    .setDialogDismiss(new IDialogDismiss() {
//                        @Override
//                        public void onDismiss(Result result, Object... values) {
//                            if(result == Result.OK){
//                                beginUpdate();
//                            }
//                        }
//                    })
//                    .toConfirm("未授予使用存储权限，安装无法完成！重新授予权限以继续更新吗？\n【确定】继续安装，【取消】退出安装。");
//        }
//    }
}

package th.yzw.specialrecorder.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
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
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.model.RecordEntity;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.view.common.DialogFactory;
import th.yzw.specialrecorder.view.common.LoadingDialog;
import th.yzw.specialrecorder.view.common.ToastFactory;
import th.yzw.specialrecorder.view.input_data.KeyboardInputFragment;
import th.yzw.specialrecorder.view.merge_data.MergeDataActivity;
import th.yzw.specialrecorder.view.setup.SetupActivity;
import th.yzw.specialrecorder.view.setup.ShowAppUpdateInfomationDialog;
import th.yzw.specialrecorder.view.input_data.TouchInputDataFragment;
import th.yzw.specialrecorder.view.show_details.ShowDetailsActivity;

public class RecorderActivity extends MyActivity {
    private String TAG = "殷宗旺";

    private FragmentManager fragmentManager;
    private long firstTouch;
    private android.support.v7.widget.Toolbar toolbar;
    private BroadcastReceiver receiver;
    private ToastFactory toastFactory;
    private DrawerLayout drawerLayout;
    private TextView badgeView;


    private void initialView() {
        drawerLayout=findViewById(R.id.drawerLayout);
        findViewById(R.id.menu_show_details_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetails();
            }
        });
        findViewById(R.id.menu_show_all_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAll();
            }
        });
        findViewById(R.id.menu_merge_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mergeData();
            }
        });
        findViewById(R.id.menu_setup_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setup();
            }
        });
        findViewById(R.id.menuGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
            }
        });
        badgeView = findViewById(R.id.appUpdatedFlag);
    }

    private void showDetails(){
        drawerLayout.closeDrawers();
        Intent intent = new Intent(RecorderActivity.this, ShowDetailsActivity.class);
        startActivity(intent);
    }

    private void showAll(){
        drawerLayout.closeDrawers();
        Intent intent = new Intent(RecorderActivity.this, ShowTotalDataActivity.class);
        startActivity(intent);
    }

    private void mergeData(){
        drawerLayout.closeDrawers();
        Intent intent = new Intent(RecorderActivity.this, MergeDataActivity.class);
        startActivity(intent);
    }

    private void setup(){
        drawerLayout.closeDrawers();
        Intent intent = new Intent(RecorderActivity.this, SetupActivity.class);
        startActivity(intent);
    }

    private void showUpdateInfo() {
        long currentVersion = AppSetupOperator.getLastAppVersion();
        long downloadVersion = AppSetupOperator.getDownloadAppVersion();
        if (downloadVersion > currentVersion) {
            badgeView.setVisibility(View.VISIBLE);
            if (AppSetupOperator.isForceUpdate()) {
                //force update
                showForceUpdateDialog();
            }else{
                showUpdateDialog();
            }
        } else {
            badgeView.setVisibility(View.INVISIBLE);
        }
    }

    private File getAppUpdateFile() {
        long downloadAppVersion = AppSetupOperator.getDownloadAppVersion();
        String filePath = getFilesDir().getAbsolutePath() + File.separator + "UpdateFiles" + File.separator + "VersionCode" + downloadAppVersion;
        File[] files = FileTools.readEmailFile(filePath);
        if (files != null && files.length == 2) {
            return files[1];
        } else {
           return null;
        }
    }

    private void openUpdater(File zipFile){
        LoadingDialog loadingDialog = LoadingDialog.newInstant("正在更新", "正在打开文件...", false);
        loadingDialog.setCancelable(false);
        final AppUpdater updater = new AppUpdater(RecorderActivity.this, zipFile);
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

    private void showUpdateDialog(){
        String filePath = getFilesDir().getAbsolutePath() + File.separator +
                "UpdateFiles" + File.separator + "VersionCode" +
                AppSetupOperator.getDownloadAppVersion();
        ShowAppUpdateInfomationDialog showAppUpdateInfomationDialog = ShowAppUpdateInfomationDialog.newInstant(filePath);
        showAppUpdateInfomationDialog.setOnDismiss(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if (result == Result.OK) {
                    String zipFilePath = (String) values[0];
                    File zipFile = new File(zipFilePath);
                    openUpdater(zipFile);
                }
            }
        });
        showAppUpdateInfomationDialog.show(getSupportFragmentManager(), "showUpdateInfo");
    }

    private void showForceUpdateDialog() {
        new DialogFactory(this).showWarningDialog("重要更新", "需要更新至最新版本才能正常使用，否则将退出程序。请点击更新！",
                "更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File zipFile = getAppUpdateFile();
                        if(zipFile == null){
                            toastFactory.showCenterToast("未找到升级文件！");
                            AppSetupOperator.setForceUpdate(false);
                        }else{
                            openUpdater(zipFile);
                        }
                    }
                },
                "退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityManager.closeAll();
                    }
                });
    }

    private void initialBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showUpdateInfo();
                Log.d(TAG, "onReceive: recoderactivity");
            }
        };
        Broadcasts.bindBroadcast(this, receiver, Broadcasts.APP_UPDATEFILE_DOWNLOAD_SUCCESS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recorder);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.menu_right);
        setTitle("首页");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.START);
            }
        });
        ActivityManager.add(this);
        firstTouch = 0;
        fragmentManager = getSupportFragmentManager();
        toastFactory = new ToastFactory(RecorderActivity.this);
        initialView();
        initialBroadcastReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Fragment fragment = null;
        if (AppSetupOperator.getInputMethod() == 1) {
            fragment = new KeyboardInputFragment();
        } else {
            fragment = new TouchInputDataFragment();
        }
        fragmentManager.beginTransaction().replace(R.id.framelayout, fragment).commit();
        showUpdateInfo();
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
            if(drawerLayout.isDrawerOpen(Gravity.START)){
                drawerLayout.closeDrawers();
                return true;
            }
            long touchTime = System.currentTimeMillis();
            if (touchTime - firstTouch > 2000) {
                firstTouch = touchTime;
                Toast toast = Toast.makeText(RecorderActivity.this, "再按一次退出", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            } else {
                ActivityManager.closeAll();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

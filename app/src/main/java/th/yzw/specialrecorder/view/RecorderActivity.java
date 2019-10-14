package th.yzw.specialrecorder.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import th.yzw.specialrecorder.ActivityManager;
import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.AppUpdater;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.view.common.DialogFactory;
import th.yzw.specialrecorder.view.common.LoadingDialog;
import th.yzw.specialrecorder.view.common.ToastFactory;
import th.yzw.specialrecorder.view.input_data.InputDataFragment;
import th.yzw.specialrecorder.view.merge_data.MergeDataFragment;
import th.yzw.specialrecorder.view.setup.SetupFragment;
import th.yzw.specialrecorder.view.show_details.ShowDetailsFragment;
import th.yzw.specialrecorder.view.show_total.ShowTotalDataFragment;
import th.yzw.specialrecorder.view.input_data.TouchInputDataFragment;

public class RecorderActivity extends AppCompatActivity {
    private String TAG = "殷宗旺";

    private FragmentManager fragmentManager;
    private long firstTouch;
    private android.support.v7.widget.Toolbar toolbar;
    private BottomNavigationView navigation;
    private BroadcastReceiver receiver;
    private TextView textView;
    private ToastFactory toastFactory;


    private void initialView() {
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return navigationClick(item.getItemId());
            }
        });
        navigation.setSelectedItemId(R.id.input);
        //获取整个的NavigationView
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
//这里就是获取所添加的每一个Tab(或者叫menu)，
        BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(4);
//加载我们的角标View，新创建的一个布局
        View badge = LayoutInflater.from(this).inflate(R.layout.setup_badge, itemView, false);
//添加到Tab上
        itemView.addView(badge);
        textView = badge.findViewById(R.id.appUpdatedFlag);
    }

    private void showUpdateInfo() {
        long currentVersion = AppSetupOperator.getLastAppVersion();
        long downloadVersion = AppSetupOperator.getDownloadAppVersion();
        if (downloadVersion > currentVersion) {
            textView.setVisibility(View.VISIBLE);
            if (AppSetupOperator.isForceUpdate()) {
                //force update
                showForceUpdateDialog();
            }
        } else {
            textView.setVisibility(View.INVISIBLE);
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
            public void onDismiss(boolean isConfirmed, Object... values) {
                if (isConfirmed) {
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

    private boolean navigationClick(int id) {
        switch (id) {
            case R.id.input:
                //输入数据
                toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.input_white_24dp));
                Fragment fragment = null;
                if (AppSetupOperator.getInputMethod() == 1) {
                    fragment = new InputDataFragment();
                } else {
                    fragment = new TouchInputDataFragment();
                }
                fragmentManager.beginTransaction().replace(R.id.framelayout, fragment).commit();
                return true;
            case R.id.show_details:
                //详细记录
                toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.show_white_24dp));
                Fragment fragment1 = new ShowDetailsFragment();
                fragmentManager.beginTransaction().replace(R.id.framelayout, fragment1).commit();
                return true;
            case R.id.show_alldata:
                //汇总记录
                toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.showtotal2_white_24dp));
                Fragment fragment2 = new ShowTotalDataFragment();
                fragmentManager.beginTransaction().replace(R.id.framelayout, fragment2).commit();
                return true;
            case R.id.merge_data:
                //合并数据
                toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.merge2_white_24dp));
                Fragment fragment3 = new MergeDataFragment();
                fragmentManager.beginTransaction().replace(R.id.framelayout, fragment3).commit();
                return true;
            case R.id.setup:
                //设置
                toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.settings_white_24dp));
                Fragment fragment4 = new SetupFragment();
                fragmentManager.beginTransaction().replace(R.id.framelayout, fragment4).commit();
                return true;
        }
        return false;
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
        toolbar.setNavigationIcon(getResources().getDrawable(R.mipmap.input_white_24dp));
        setTitle("SpecialRecorder");
        setSupportActionBar(toolbar);
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
        showUpdateInfo();
    }

    @Override
    protected void onDestroy() {
        // 注销该广播接收器
        Broadcasts.unBindBroadcast(this, receiver);
        super.onDestroy();
    }

    public void setTitle(String s) {
        toolbar.setTitle(s);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
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

    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources != null && resources.getConfiguration().fontScale != 1.0f) {
            android.content.res.Configuration configuration = resources.getConfiguration();
            configuration.fontScale = 1.0f;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
        return resources;
    }
}

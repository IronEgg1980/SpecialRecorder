package th.yzw.specialrecorder.view.setup;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import th.yzw.specialrecorder.ActivityManager;
import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.AppUpdater;
import th.yzw.specialrecorder.DAO.DataBackupAndRestore;
import th.yzw.specialrecorder.DAO.ItemUpdater;
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
import th.yzw.specialrecorder.view.common.SelectItemPopWindow;
import th.yzw.specialrecorder.view.common.ToastFactory;

public class SetupActivity extends MyActivity {

    private RadioButton inputMethodByKeyboard;
    private RadioButton inputMethodByTouch;
    private RadioButton button2Columns;
    private RadioButton button3Columns;
    private RadioButton button4Columns;
    private RadioGroup inputMethodGroup;
    private RadioGroup buttonColumnsGroup;
    private CardView buttonColumnsCard, vibrateSetupCard, infoLocationCard;
    private SwitchCompat showGroupButton;
    private SwitchCompat vibrateOn;
    private SeekBar vibrateLevelSeekbar;
    private LinearLayout dataSafeBackup;
    private LinearLayout dataSafeRestore;
    private LinearLayout dataSafeClearFiles;
    private SwitchCompat dataSafeAlarm;
    private LinearLayout othersSetupUpdateItems;
    private LinearLayout othersSetupPwd;
    private LinearLayout othersSetupUpdate;
    private LinearLayout cleaningApp;
    private LinearLayout aboutApp;
    private TextView closeApp;
    private RadioButton infoLocationNone;
    private RadioButton infoLocationButton;
    private RadioButton infoLocationTop;
    private RadioGroup infoLocationGroup;
    private LoadingDialog loadingDialog;
    private TextView appUpdatedFlagTV;
    private InfoPopWindow infoPopWindow;

    private final int WRITE_INPUT = 1, TOUCH_INPUT = 2;
    private int showInfoMode;
    private File selectedFile;
    private boolean alarmModeYes, setVibrateMode;
    private int vibrateLevel;
    private Vibrator vibrator;
    private boolean isHideMode;
    private BroadcastReceiver receiver;

    final private int REQUEST_CODE_CLEARAPP = 111;
    final private int REQUEST_CODE_CLEARBACKFILES = 222;
    final private int REQUEST_CODE_BACKUP = 333;
    final private int REQUEST_CODE_RESTORE = 444;
    final private int REQUEST_CODE_UPDATEITEM = 555;
    final private int REQUEST_CODE_UPDATEAPP = 666;

    private void initialView() {
        infoPopWindow = new InfoPopWindow(this);
        infoLocationCard = findViewById(R.id.info_location_cardview);
        infoLocationGroup = findViewById(R.id.info_location_group);
        infoLocationNone = findViewById(R.id.info_location_none);
        infoLocationButton = findViewById(R.id.info_location_button);
        infoLocationTop = findViewById(R.id.info_location_top);
        inputMethodByKeyboard = findViewById(R.id.input_method_byKeyboard);
        inputMethodByTouch = findViewById(R.id.input_method_byTouch);
        button2Columns = findViewById(R.id.button_2_columns);
        button3Columns = findViewById(R.id.button_3_columns);
        button4Columns = findViewById(R.id.button_4_columns);
        inputMethodGroup = findViewById(R.id.input_method_group);
        inputMethodByKeyboard = findViewById(R.id.input_method_byKeyboard);
        inputMethodByTouch = findViewById(R.id.input_method_byTouch);
        buttonColumnsGroup = findViewById(R.id.button_columns_group);
        buttonColumnsCard = findViewById(R.id.button_columns_cardview);
        vibrateSetupCard = findViewById(R.id.vibrate_setup_cardview);
        vibrateOn = findViewById(R.id.vibrate_on);
        vibrateLevelSeekbar = findViewById(R.id.vibrate_level_seekbar);
        dataSafeBackup = findViewById(R.id.data_safe_backup);
        dataSafeRestore = findViewById(R.id.data_safe_restore);
        dataSafeClearFiles = findViewById(R.id.data_safe_clearFiles);
        dataSafeAlarm = findViewById(R.id.data_safe_alarm);
        othersSetupPwd = findViewById(R.id.others_setup_pwd);
        othersSetupUpdate = findViewById(R.id.others_setup_update);
        closeApp = findViewById(R.id.coloseApp);
        othersSetupUpdateItems = findViewById(R.id.others_setup_updateItems);
        cleaningApp = findViewById(R.id.others_setup_cleaning);
        appUpdatedFlagTV = findViewById(R.id.appUpdatedFlag);
        showGroupButton = findViewById(R.id.showGroupButtonSwitchCompat);
        aboutApp = findViewById(R.id.others_setup_about);
        setAppUpdatedFlagVisible();

        initialInputMethod();
        initialInfoLocation();
        inputMethodGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                changeInputMethod();
            }
        });
        buttonColumnsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                changeButtonColumns(checkedId);
            }
        });
        infoLocationGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                changeInfoLocation(checkedId);
            }
        });
        showGroupButton.setChecked(AppSetupOperator.getShowGroupButtonStatus());
        showGroupButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppSetupOperator.setShowGroupButtonStatus(isChecked);
            }
        });
        vibrateOn.setChecked(setVibrateMode);
        vibrateOn.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                AppSetupOperator.setUseVibrate(vibrateOn.isChecked());
                vibrateLevelSeekbar.setEnabled(vibrateOn.isChecked());
            }
        });
        vibrateLevelSeekbar.setEnabled(setVibrateMode);
        dataSafeAlarm.setChecked(alarmModeYes);
        dataSafeAlarm.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                AppSetupOperator.setUseAlarmMode(dataSafeAlarm.isChecked());
                if (dataSafeAlarm.isChecked())
                    new ToastFactory(SetupActivity.this).showLongToast("已启用紧急模式，（/110/)你懂的！");
            }
        });
        vibrateLevelSeekbar.setProgress(vibrateLevel);
        vibrateLevelSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int i = seekBar.getProgress();
                if (i == 0)
                    return;
                if (Build.VERSION.SDK_INT >= 26) {
                    VibrationEffect effect = VibrationEffect.createOneShot(i, 125);
                    vibrator.vibrate(effect);
                }
                AppSetupOperator.setVibrateLevel(i);
                vibrateLevel = i;
            }
        });
        dataSafeBackup.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                backup();
            }
        });
        dataSafeRestore.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                restoreData();
            }
        });
        dataSafeClearFiles.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                clearBackupDirectory();
            }
        });
        othersSetupPwd.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                new EditPWDPopWindow(SetupActivity.this).show();
            }
        });
        othersSetupUpdateItems.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                updateItemDataClick();
            }
        });
        othersSetupUpdate.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                updateAPPClick();
            }
        });
        cleaningApp.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                cleaningApp();
            }
        });
        closeApp.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                ActivityManager.closeAll();
                System.exit(0);
            }
        });
        aboutApp.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                String s = "版本: " + OtherTools.getAppVersionName(SetupActivity.this) +
                        "\n代码: " + AppSetupOperator.getLastAppVersion() +
                        "\n\nEnjoy it !";
                infoPopWindow.show(s);
            }
        });
    }

    private void initialBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setAppUpdatedFlagVisible();
            }
        };
        Broadcasts.bindBroadcast(this, receiver, Broadcasts.APP_UPDATEFILE_DOWNLOAD_SUCCESS);
    }

    private void setAppUpdatedFlagVisible() {
        long currentVersion = AppSetupOperator.getLastAppVersion();
        long downloadVersion = AppSetupOperator.getDownloadAppVersion();
        if (downloadVersion > currentVersion)
            appUpdatedFlagTV.setVisibility(View.VISIBLE);
        else
            appUpdatedFlagTV.setVisibility(View.GONE);
    }

    private void playInAnimation() {
        buttonColumnsCard.setVisibility(View.INVISIBLE);
        int length = buttonColumnsCard.getMeasuredHeight();
        ObjectAnimator a01 = ObjectAnimator.ofFloat(infoLocationCard, "translationY", -length, 0, 0);
        a01.setDuration(500);

        ObjectAnimator a02 = ObjectAnimator.ofFloat(vibrateSetupCard, "translationY", -length, 0, 0);
        a02.setDuration(500);

        ObjectAnimator a2 = ObjectAnimator.ofFloat(buttonColumnsCard, "alpha", 0, 1f);
        a2.setDuration(500);
        a2.setStartDelay(100);

        ObjectAnimator a3 = ObjectAnimator.ofFloat(buttonColumnsCard, "translationY", -200, 0, 0);
        a3.setDuration(500);
        a3.setStartDelay(100);
        a3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                buttonColumnsCard.setVisibility(View.VISIBLE);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playTogether(a01, a02, a2, a3);
        set.start();
    }

    private void playOutAnimation() {
        int length = buttonColumnsCard.getMeasuredHeight() + OtherTools.dip2px(this, 8f);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(buttonColumnsCard, "alpha", 0.8f, 0.0f);
        a2.setDuration(400);
        a2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                buttonColumnsCard.setVisibility(View.GONE);
            }
        });

        ObjectAnimator a01 = ObjectAnimator.ofFloat(infoLocationCard, "translationY", length, 0, 0);
        a01.setDuration(500);
        a01.setStartDelay(300);

        ObjectAnimator a02 = ObjectAnimator.ofFloat(vibrateSetupCard, "translationY", length, 0, 0);
        a02.setDuration(500);
        a02.setStartDelay(400);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(a2, a01, a02);
        set.start();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isHideMode = AppSetupOperator.isHideMode();
        if (isHideMode) {
            setContentView(R.layout.setup_layout);
        } else {
            setContentView(R.layout.setup_layout2);
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.back2);
        toolbar.setTitle("APP设置");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        showInfoMode = AppSetupOperator.getShowInformationMode();
        vibrateLevel = AppSetupOperator.getVibrateLevel();
        alarmModeYes = AppSetupOperator.isUseAlarmMode();
        setVibrateMode = AppSetupOperator.isUseVibrate();
        initialView();
        initialBroadcastReceiver();
    }

    @Override
    public void onDestroy() {
        Broadcasts.unBindBroadcast(this, receiver);
        super.onDestroy();
    }

    private void initialButtonColumns() {
        int spanCount = AppSetupOperator.getSpanCount();
        button2Columns.setChecked(spanCount == 1);
        button3Columns.setChecked(spanCount == 2);
        button4Columns.setChecked(spanCount == 3);
    }

    private void initialInputMethod() {
        if (AppSetupOperator.getInputMethod() == WRITE_INPUT) {
            inputMethodByKeyboard.setChecked(true);
            buttonColumnsCard.setVisibility(View.GONE);
        } else {
            inputMethodByTouch.setChecked(true);
            buttonColumnsCard.setVisibility(View.VISIBLE);
            initialButtonColumns();
        }
    }

    private void initialInfoLocation() {
        switch (showInfoMode) {
            case 0:
                infoLocationNone.setChecked(true);
                break;
            case 1:
                infoLocationButton.setChecked(true);
                break;
            case 2:
                infoLocationTop.setChecked(true);
                break;
        }
    }

    private void changeInputMethod() {
        if (inputMethodByKeyboard.isChecked()) {
            AppSetupOperator.setInputMethod(WRITE_INPUT);
            playOutAnimation();
        }
        if (inputMethodByTouch.isChecked()) {
            AppSetupOperator.setInputMethod(TOUCH_INPUT);
            playInAnimation();
            initialButtonColumns();
        }
    }

    private void changeButtonColumns(int checkedId) {
        int i = 1;
        switch (checkedId) {
            case R.id.button_3_columns:
                i = 2;
                break;
            case R.id.button_4_columns:
                i = 3;
                break;
        }
        AppSetupOperator.setSpanCount(i);
    }

    private void changeInfoLocation(int checkedId) {
        int i = 0;
        switch (checkedId) {
            case R.id.info_location_button:
                i = 1;
                break;
            case R.id.info_location_top:
                i = 2;
                break;
        }
        AppSetupOperator.setShowInformationMode(i);
    }

    private void clearBackupFiles() {
        File path = new File(FileTools.BACKUP_DIR);
        boolean b = true;
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files.length == 0) {
                infoPopWindow.show("没有备份文件，不用清理！");
                return;
            }
            for (File file : files) {
                b = FileTools.clearFiles(file);
            }
            if (b)
                new ToastFactory(this).showCenterToast("备份文件夹清理完成！建议立即备份一次。");
            else
                infoPopWindow.show("备份文件夹清理失败！");
        } else {
            infoPopWindow.show("未找到备份文件夹！可能原因：还没有备份过。");
        }
    }

    private void cleaningApp() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                new ConfirmPopWindow(SetupActivity.this).setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            FileTools.cleanApp(Objects.requireNonNull(SetupActivity.this));
                            AppSetupOperator.setDownloadAppVersion(1);
                            AppSetupOperator.setForceUpdate(false);
                            new ToastFactory(SetupActivity.this).showCenterToast("已清理");
                        }
                    }
                }).toConfirm("是否清理所有数据文件、项目更新文件、App升级文件？");
            } else {
                infoPopWindow.show("SD卡不可用，请稍后再试一下看看…");
            }
        }else{
            ActivityCompat.requestPermissions(this,PERMISSION_GROUP_STORAGE,REQUEST_CODE_CLEARAPP);
        }
    }

    public void clearBackupDirectory() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED){
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                new ConfirmPopWindow(SetupActivity.this).setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            clearBackupFiles();
                        }
                    }
                }).toConfirm("清除所有备份文件后将不能恢复，是否继续？");
            } else {
                infoPopWindow.show("SD卡不可用，请稍后再试一下看看…");
            }
        }else{
            ActivityCompat.requestPermissions(this,PERMISSION_GROUP_STORAGE,REQUEST_CODE_CLEARBACKFILES);
        }
    }

    private void backup() {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File path = new File(FileTools.BACKUP_DIR);
            if (!path.exists() && !path.mkdirs()) {
                infoPopWindow.show("创建目录失败！请重试一次…");
                return;
            }
            loadingDialog = LoadingDialog.newInstant("备份数据", "准备中...", true);
            loadingDialog.setCancelClick(null);
            loadingDialog.setCancelable(false);
            DataBackupAndRestore dataBackuper = new DataBackupAndRestore(this, "backup");
            dataBackuper.setOnFinish(new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... values) {
                    loadingDialog.dismiss();
                    String s = (String) values[0];
                    new ToastFactory(SetupActivity.this).showCenterToast(s);
                }
            });
            loadingDialog.show(getSupportFragmentManager(), "loading");
            dataBackuper.execute();
        }else{
            ActivityCompat.requestPermissions(this,PERMISSION_GROUP_STORAGE,REQUEST_CODE_BACKUP);
        }
    }

    private void restore() {
        final ConfirmPopWindow popWindow = new ConfirmPopWindow(this);
        popWindow.setDialogDismiss(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if (result == Result.OK) {
                    if (selectedFile.exists()) {
                        try {
                            String s = FileTools.readEncryptFile(selectedFile);
                            if (s.startsWith("nothing")) {
                                popWindow.isResumeAlpha = false;
                                infoPopWindow.show("备份文件内无数据");
                                return;
                            }
                            loadingDialog = LoadingDialog.newInstant("恢复数据", "准备中...", true);
                            loadingDialog.setCancelClick(null);
                            loadingDialog.setCancelable(false);
                            final DataBackupAndRestore dataBackuper = new DataBackupAndRestore(SetupActivity.this, s);
                            dataBackuper.setOnFinish(new IDialogDismiss() {
                                @Override
                                public void onDismiss(Result result, Object... values) {
                                    loadingDialog.dismiss();
                                    String s = (String) values[0];
                                    new ToastFactory(SetupActivity.this).showCenterToast(s);
                                }
                            });
                            loadingDialog.show(getSupportFragmentManager(), "loading");
                            dataBackuper.execute();
                        } catch (IOException e) {
                            infoPopWindow.show("读取文件失败！\n" + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        infoPopWindow.show("备份文件丢失，请进入【" + FileTools.BACKUP_DIR + "】目录查看！");
                    }
                }
            }
        }).toConfirm("注意：【恢复数据】操作将会清除现有数据，恢复为已备份的记录。是否确认该操作？");
    }

    private void restoreData() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
           if(ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
               final String[] pathList = FileTools.getFileList(FileTools.BACKUP_DIR, ".backup");
               if (pathList.length > 0) {
                   selectedFile = new File(FileTools.BACKUP_DIR, pathList[0]);
                   final SelectItemPopWindow selectItemPopWindow = new SelectItemPopWindow(SetupActivity.this, pathList, false);
                   selectItemPopWindow.show(new IDialogDismiss() {
                       @Override
                       public void onDismiss(Result result, Object... values) {
                           if (result == Result.OK) {
                               selectItemPopWindow.isResumeAlpha = false;
                               selectedFile = new File(FileTools.BACKUP_DIR, pathList[(int) values[0]]);
                               restore();
                           }
                       }
                   });
               } else {
                   infoPopWindow.show("未找到备份记录！");
               }
           }else{
               ActivityCompat.requestPermissions(this,PERMISSION_GROUP_STORAGE,REQUEST_CODE_RESTORE);
           }
        } else {
            infoPopWindow.show("SD卡不可用，请稍后再试一下看看…");
        }

    }

    private void updateItemDataClick() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                final String[] temp = FileTools.getFileList(FileTools.MICROMSG_DIR, ".itemupdate");
                if (temp.length > 0) {
                    SelectItemPopWindow itemPopWindow = new SelectItemPopWindow(SetupActivity.this, temp, false);
                    itemPopWindow.show(new IDialogDismiss() {
                        @Override
                        public void onDismiss(Result result, Object... values) {
                            if (result == Result.OK) {
                                File f = new File(FileTools.MICROMSG_DIR, temp[(int) values[0]]);
                                loadingDialog = LoadingDialog.newInstant("正在更新", "正在打开文件...", true);
                                loadingDialog.setCancelClick(null);
                                loadingDialog.setCancelable(false);
                                ItemUpdater updater = new ItemUpdater(SetupActivity.this, f);
                                updater.setOnFinished(new IDialogDismiss() {
                                    @Override
                                    public void onDismiss(Result result, Object... values) {
                                        loadingDialog.dismiss();
                                        String s = (String) values[0];
                                        infoPopWindow.show(s);
                                    }
                                });
                                loadingDialog.show(getSupportFragmentManager(), "loading");
                                updater.execute();
                            }
                        }
                    });
                } else {
                    infoPopWindow.show(FileTools.MICROMSG_DIR + "未找到数据文件。");
                }
            }else{
                ActivityCompat.requestPermissions(this,PERMISSION_GROUP_STORAGE,REQUEST_CODE_UPDATEITEM);
            }
        } else {
            infoPopWindow.show("SD卡不可用，请稍后再试一下看看…");
        }
    }

    private void updateAPPClick() {
        if(hasInstallPermission()){
            if(ActivityCompat.checkSelfPermission(SetupActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                openUpdateAppDialog();
            }else{
                ActivityCompat.requestPermissions(this,PERMISSION_GROUP_STORAGE, REQUEST_CODE_UPDATEAPP);
            }
        }
    }

    private void openUpdateAppDialog() {
        long currentVersion = AppSetupOperator.getLastAppVersion();
        long downloadVersion = AppSetupOperator.getDownloadAppVersion();
        if (downloadVersion > currentVersion) {
            String filePath = getFilesDir().getAbsolutePath() + File.separator + "UpdateFiles" + File.separator + "VersionCode" + downloadVersion;
            ShowAppUpdateInfomationDialog showAppUpdateInfomationDialog = ShowAppUpdateInfomationDialog.newInstant(filePath);
            showAppUpdateInfomationDialog.setOnDismiss(new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... values) {
                    if (result == Result.OK) {
                        String zipFilePath = (String) values[0];
                        File zipFile = new File(zipFilePath);
                        updateAppByEmail(zipFile);
                    }
                }
            });
            showAppUpdateInfomationDialog.show(getSupportFragmentManager(), "showUpdateInfo");
        } else {
            updateAppByMicroMsg();
        }
    }

    private void updateAppByMicroMsg() {
        final String[] temp = FileTools.getFileList(FileTools.MICROMSG_DIR, ".update");
        if (temp.length > 0) {
            SelectItemPopWindow itemPopWindow = new SelectItemPopWindow(this, temp, false);
            itemPopWindow.show(new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... values) {
                    if (result == Result.OK) {
                        File f = new File(FileTools.MICROMSG_DIR, temp[(int) values[0]]);
                        loadingDialog = LoadingDialog.newInstant("正在更新", "正在打开文件...", false);
                        loadingDialog.setCancelable(false);
                        final AppUpdater updater = new AppUpdater(SetupActivity.this, f);
                        loadingDialog.setCancelClick(new NoDoubleClickListener() {
                            @Override
                            public void onNoDoubleClick(View v) {
                                updater.cancleUpdate();
                            }
                        });
                        updater.setOnFinish(new IDialogDismiss() {
                            @Override
                            public void onDismiss(Result result, Object... values) {
                                if (result == Result.OK) {
                                    File updateFile = (File) values[0];
                                    if (updateFile != null) {
                                        OtherTools.openAPKFile(SetupActivity.this, updateFile);
                                    } else {
                                        infoPopWindow.show("安装文件损坏");
                                    }
                                } else {
                                    String s = (String) values[0];
                                    infoPopWindow.show(s);
                                }
                            }
                        });
                        loadingDialog.show(getSupportFragmentManager(), "loading");
                        updater.execute();
                    }
                }
            });
        } else {
            infoPopWindow.show(FileTools.MICROMSG_DIR + "文件夹内未找到数据文件。");
        }
    }

    private void updateAppByEmail(File zipFile) {
        loadingDialog = LoadingDialog.newInstant("正在更新", "正在打开文件...", false);
        loadingDialog.setCancelable(false);
        final AppUpdater updater = new AppUpdater(this, zipFile);
        loadingDialog.setCancelClick(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                updater.cancleUpdate();
            }
        });
        updater.setOnFinish(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if (result == Result.OK) {
                    File updateFile = (File) values[0];
                    if (updateFile != null) {
                        OtherTools.openAPKFile(SetupActivity.this, updateFile);
                    } else {
                        infoPopWindow.show("安装文件损坏");
                    }
                } else {
                    String s = (String) values[0];
                    infoPopWindow.show(s);
                }
            }
        });
        loadingDialog.show(getSupportFragmentManager(), "loading");
        updater.execute();
    }

    @Override
    protected void onPermissionGranted(int requestCode) {
        switch (requestCode){
            case REQUEST_CODE_CLEARAPP:
                cleaningApp();
                break;
            case REQUEST_CODE_CLEARBACKFILES:
                clearBackupDirectory();
                break;
            case REQUEST_CODE_BACKUP:
                backup();
                break;
            case REQUEST_CODE_RESTORE:
                restoreData();
                break;
            case REQUEST_CODE_UPDATEAPP:
                updateAPPClick();
                break;
            case REQUEST_CODE_UPDATEITEM:
                updateItemDataClick();
                break;
            case REQUST_CODE_INSTALL:
                updateAPPClick();
                break;
            default:
                new ToastFactory(this).showCenterToast("已获得授权。");
        }
    }

    @Override
    protected void onPermissionDenied(int requestCode) {
        if(requestCode==REQUST_CODE_INSTALL){
            new InfoPopWindow(this).show("由于您拒绝授权，更新程序失败！");
        }else{
            new ConfirmPopWindow(this)
                    .setDialogDismiss(new IDialogDismiss() {
                        @Override
                        public void onDismiss(Result result, Object... values) {
                            if(result == Result.OK){
                                ActivityCompat.requestPermissions(SetupActivity.this,PERMISSION_GROUP_STORAGE,999);
                            }
                        }
                    })
                    .toConfirm("由于您拒绝授予读取存储权限，该功能无法使用！\n请点击【确定】授予权限。");
        }
    }
}

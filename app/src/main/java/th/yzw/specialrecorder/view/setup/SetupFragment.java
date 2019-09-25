package th.yzw.specialrecorder.view.setup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import th.yzw.specialrecorder.ActivityManager;
import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.AppUpdater;
import th.yzw.specialrecorder.DAO.DataBackupAndRestore;
import th.yzw.specialrecorder.DAO.ItemUpdater;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.SelectDialogClicker;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.tools.PermissionHelper;
import th.yzw.specialrecorder.view.RecorderActivity;
import th.yzw.specialrecorder.view.common.DialogFactory;
import th.yzw.specialrecorder.view.common.LoadingDialog;
import th.yzw.specialrecorder.view.common.ToastFactory;

public class SetupFragment extends Fragment {
    private RadioButton inputMethodByKeyboard;
    private RadioButton inputMethodByTouch;
    private RadioButton button2Columns;
    private RadioButton button3Columns;
    private RadioButton button4Columns;
    private RadioGroup inputMethodGroup;
    private RadioGroup buttonColumnsGroup;
    private SwitchCompat showGroupButton;
    private SwitchCompat vibrateOn;
    private SeekBar vibrateLevelSeekbar;
    private LinearLayout vibrateGroup;
    private LinearLayout dataSafeBackup;
    private LinearLayout dataSafeRestore;
    private LinearLayout dataSafeClearFiles;
    private SwitchCompat dataSafeAlarm;
    private LinearLayout othersSetupUpdateItems;
    private LinearLayout othersSetupPwd;
    private LinearLayout othersSetupUpdate;
    private LinearLayout cleaningApp;
    private TextView coloseApp;
    private RadioButton infoLocationNone;
    private RadioButton infoLocationButton;
    private RadioButton infoLocationTop;
    private RadioGroup infoLocationGroup;
    private LoadingDialog loadingDialog;
    private TextView appUpdatedFlagTV;
    private DialogFactory dialogFactory;
    private ToastFactory toastFactory;

    private final int WRITE_INPUT = 1, TOUCH_INPUT = 2;
//    private final String microMsgPath = Environment.getExternalStorageDirectory() + "/tencent/MicroMsg/download/";
//    private final String backFilesPath = Environment.getExternalStorageDirectory() + "/MyBackup/";
    private int showInfoMode;
    private File selectedFile;
    private boolean alarmModeYes, setVibrateMode;
    private RecorderActivity activity;
    private View view;
    private int vibrateLevel;
    private Vibrator vibrator;
    private boolean isHideMode;
    private BroadcastReceiver receiver;

    private void initialView(View view) {
        dialogFactory = new DialogFactory(getContext());
        toastFactory = new ToastFactory(getContext());
        infoLocationGroup = view.findViewById(R.id.info_location_group);
        infoLocationNone = view.findViewById(R.id.info_location_none);
        infoLocationButton = view.findViewById(R.id.info_location_button);
        infoLocationTop = view.findViewById(R.id.info_location_top);
        inputMethodByKeyboard = view.findViewById(R.id.input_method_byKeyboard);
        inputMethodByTouch = view.findViewById(R.id.input_method_byTouch);
        button2Columns = view.findViewById(R.id.button_2_columns);
        button3Columns = view.findViewById(R.id.button_3_columns);
        button4Columns = view.findViewById(R.id.button_4_columns);
        inputMethodGroup = view.findViewById(R.id.input_method_group);
        inputMethodByKeyboard = view.findViewById(R.id.input_method_byKeyboard);
        inputMethodByTouch = view.findViewById(R.id.input_method_byTouch);
        buttonColumnsGroup = view.findViewById(R.id.button_columns_group);
        vibrateGroup = view.findViewById(R.id.vibrate_setup_group);
        vibrateOn = view.findViewById(R.id.vibrate_on);
        vibrateLevelSeekbar = view.findViewById(R.id.vibrate_level_seekbar);
        dataSafeBackup = view.findViewById(R.id.data_safe_backup);
        dataSafeRestore = view.findViewById(R.id.data_safe_restore);
        dataSafeClearFiles = view.findViewById(R.id.data_safe_clearFiles);
        dataSafeAlarm = view.findViewById(R.id.data_safe_alarm);
        othersSetupPwd = view.findViewById(R.id.others_setup_pwd);
        othersSetupUpdate = view.findViewById(R.id.others_setup_update);
        coloseApp = view.findViewById(R.id.coloseApp);
        othersSetupUpdateItems = view.findViewById(R.id.others_setup_updateItems);
        cleaningApp = view.findViewById(R.id.others_setup_cleaning);
        appUpdatedFlagTV = view.findViewById(R.id.appUpdatedFlag);
        showGroupButton = view.findViewById(R.id.showGroupButtonSwitchCompat);
        setAppUpdatedFlagVisible();
    }

    private void initialBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setAppUpdatedFlagVisible();
            }
        };
        Broadcasts.bindBroadcast(getContext(), receiver, Broadcasts.APP_UPDATEFILE_DOWNLOAD_SUCCESS);
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
        buttonColumnsGroup.setVisibility(View.INVISIBLE);
        int length = buttonColumnsGroup.getMeasuredHeight();
        ObjectAnimator a01 = ObjectAnimator.ofFloat(infoLocationGroup, "translationY", -length, 0, 0);
        a01.setDuration(500);

        ObjectAnimator a02 = ObjectAnimator.ofFloat(vibrateGroup, "translationY", -length, 0, 0);
        a02.setDuration(500);

        ObjectAnimator a2 = ObjectAnimator.ofFloat(buttonColumnsGroup, "alpha", 0, 1f);
        a2.setDuration(500);
        a2.setStartDelay(100);

        ObjectAnimator a3 = ObjectAnimator.ofFloat(buttonColumnsGroup,"translationY",-200, 0, 0);
        a3.setDuration(500);
        a3.setStartDelay(100);
        a3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                buttonColumnsGroup.setVisibility(View.VISIBLE);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playTogether(a01,a02,a2,a3);
        set.start();
    }

    private void playOutAnimation() {
        int length = buttonColumnsGroup.getMeasuredHeight() + OtherTools.dip2px(getContext(),8f);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(buttonColumnsGroup, "alpha", 0.8f, 0.0f);
        a2.setDuration(700);
        a2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                buttonColumnsGroup.setVisibility(View.INVISIBLE);
            }
        });

        ObjectAnimator a01 = ObjectAnimator.ofFloat(infoLocationGroup, "translationY", 0,-length,-length);
        a01.setDuration(500);
        a01.setStartDelay(200);

        ObjectAnimator a02 = ObjectAnimator.ofFloat(vibrateGroup, "translationY", 0,-length,-length);
        a02.setDuration(500);
        a02.setStartDelay(200);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(a2,a01, a02);
        set.start();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (RecorderActivity) getActivity();
        activity.setTitle("程序设置");
        vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        showInfoMode = AppSetupOperator.getShowInformationMode();
        vibrateLevel = AppSetupOperator.getVibrateLevel();
        alarmModeYes = AppSetupOperator.isUseAlarmMode();
        setVibrateMode = AppSetupOperator.isUseVibrate();
        isHideMode = AppSetupOperator.isHideMode();
        initialBroadcastReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (isHideMode) {
            return inflater.inflate(R.layout.setup_layout, container, false);
        }
        view = inflater.inflate(R.layout.setup_layout2, container, false);
        initialView(view);
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
        vibrateOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppSetupOperator.setUseVibrate(vibrateOn.isChecked());
                vibrateLevelSeekbar.setEnabled(vibrateOn.isChecked());
            }
        });
        vibrateLevelSeekbar.setEnabled(setVibrateMode);
        dataSafeAlarm.setChecked(alarmModeYes);
        dataSafeAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppSetupOperator.setUseAlarmMode(dataSafeAlarm.isChecked());
                if (dataSafeAlarm.isChecked())
                    toastFactory.showLongToast("已启用紧急模式，（/110/)你懂的！");
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
        dataSafeBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backupData(v);
            }
        });
        dataSafeRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restoreData(v);
            }
        });
        dataSafeClearFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearBackupDirectory(v);
            }
        });
        othersSetupPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EditPassWordDialogFragment().show(getFragmentManager(), "editpassword");
            }
        });
        othersSetupUpdateItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateItemDataClick();
            }
        });
        othersSetupUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAPPClick();
            }
        });
        cleaningApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleaningApp();
            }
        });
        coloseApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityManager.closeAll();
                System.exit(0);
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        Broadcasts.unBindBroadcast(getContext(), receiver);
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
            buttonColumnsGroup.setVisibility(View.GONE);
        } else {
            inputMethodByTouch.setChecked(true);
            buttonColumnsGroup.setVisibility(View.VISIBLE);
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
                toastFactory.showCenterToast("没有备份文件，不用清理！");
                return;
            }
            for (File file : files) {
                b = FileTools.clearFiles(file);
            }
            if (b)
                toastFactory.showCenterToast("备份文件夹清理完成！建议立即备份一次。");
            else
                toastFactory.showCenterToast("备份文件夹清理失败！");
        } else {
            toastFactory.showCenterToast("未找到备份文件夹！可能原因：还没有备份过。");
        }

//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            AlertDialog dialog = new AlertDialog.Builder(getContext())
//                    .setTitle("确认")
//                    .setMessage("清除所有备份文件后将不能恢复，是否继续？")
//                    .setIcon(R.drawable.ic_warning_24dp)
//                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            clear();
//                        }
//                    })
//                    .setNegativeButton("取消", null).create();
//            dialog.show();
//
//        } else {
//            dialogAndToast.showCenterToast("SD卡不可用，请稍后再试一下看看…");
//        }
    }

    private void cleaningApp() {
        new PermissionHelper(activity, getContext(), new PermissionHelper.OnResult() {
            @Override
            public void hasPermission() {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    dialogFactory.showDefaultConfirmDialog("是否清理所有数据文件、项目更新文件、App升级文件？", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FileTools.cleanApp(Objects.requireNonNull(getContext()));
                            AppSetupOperator.setDownloadAppVersion(1);
                            AppSetupOperator.setForceUpdate(false);
                            toastFactory.showCenterToast("已清理");
                        }
                    });
                } else {
                    toastFactory.showCenterToast("SD卡不可用，请稍后再试一下看看…");
                }
            }
        }).request(Permission.Group.STORAGE);
    }

    public void clearBackupDirectory(View view) {
        new PermissionHelper(activity, getContext(), new PermissionHelper.OnResult() {
            @Override
            public void hasPermission() {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    dialogFactory.showDefaultConfirmDialog("清除所有备份文件后将不能恢复，是否继续？", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearBackupFiles();
                        }
                    });
                } else {
                    toastFactory.showCenterToast("SD卡不可用，请稍后再试一下看看…");
                }
            }
        }).request(Permission.Group.STORAGE);
    }

    private void backup() {
        File path = new File(FileTools.BACKUP_DIR);
        if (!path.exists() && !path.mkdirs()) {
            toastFactory.showCenterToast("创建目录失败！请重试一次…");
            return;
        }
        loadingDialog = LoadingDialog.newInstant("备份数据", "准备中...", true);
        loadingDialog.setCancelClick(null);
        loadingDialog.setCancelable(false);
        DataBackupAndRestore dataBackuper = new DataBackupAndRestore(getContext(), "backup");
        dataBackuper.setOnFinish(new IDialogDismiss() {
            @Override
            public void onDismiss(boolean isConfirmed, Object... values) {
                loadingDialog.dismiss();
                String s = (String) values[0];
                new DialogFactory(getContext()).showInfoDialog(s);
            }
        });
        loadingDialog.show(getFragmentManager(), "loading");
        dataBackuper.execute();
    }

    public void backupData(View view) {
        new PermissionHelper(activity, getContext(), new PermissionHelper.OnResult() {
            @Override
            public void hasPermission() {
                backup();
            }
        }).request(Permission.Group.STORAGE);
    }

    private void restore() {
        dialogFactory.showDefaultConfirmDialog("注意：【恢复数据】操作将会清除现有数据，恢复为已备份的记录。是否确认该操作？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedFile.exists()) {
                    try {
                        String s = FileTools.readEncryptFile(selectedFile);
                        if (s.startsWith("nothing")) {
                            toastFactory.showCenterToast("备份文件内无数据");
                            return;
                        }
                        loadingDialog = LoadingDialog.newInstant("恢复数据", "准备中...", true);
                        loadingDialog.setCancelClick(null);
                        loadingDialog.setCancelable(false);
                        final DataBackupAndRestore dataBackuper = new DataBackupAndRestore(getContext(), s);
                        dataBackuper.setOnFinish(new IDialogDismiss() {
                            @Override
                            public void onDismiss(boolean isConfirmed, Object... values) {
                                loadingDialog.dismiss();
                                String s = (String) values[0];
                                dialogFactory.showInfoDialog(s);
                            }
                        });
                        loadingDialog.show(getFragmentManager(), "loading");
                        dataBackuper.execute();
                    } catch (IOException e) {
                        dialogFactory.showInfoDialog("读取文件失败！\n" + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    dialogFactory.showInfoDialog("备份文件丢失，请进入【" + FileTools.BACKUP_DIR + "】目录查看！");
                }
            }
        });
    }

    private void restoreData(View view) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            new PermissionHelper(activity, getContext(), new PermissionHelper.OnResult() {
                @Override
                public void hasPermission() {
                    final String[] pathList = FileTools.getFileList(FileTools.BACKUP_DIR, ".backup");
                    if (pathList.length > 0) {
                        selectedFile = new File(FileTools.BACKUP_DIR, pathList[0]);
                        dialogFactory.showSingleSelectWithConfirmButton(pathList, new SelectDialogClicker() {
                            @Override
                            public void click(int checkedItem) {
                                selectedFile = new File(FileTools.BACKUP_DIR, pathList[checkedItem]);
                                restore();
                            }
                        });
//
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//                    builder.setTitle("请选择备份文件");
//                    builder.setIcon(R.drawable.ic_info_cyan_800_18dp);
//                    builder.setSingleChoiceItems(pathList, -1, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            selectedFile = new File(backFilesPath, pathList[which]);
//                            restore();
//                            dialog.dismiss();
//                        }
//                    });
//                    builder.setNegativeButton("关闭", null);
//                    builder.create().show();
                    } else {
                        toastFactory.showCenterToast("未找到备份记录！");
                    }
                }
            }).request(Permission.Group.STORAGE);
        }else {
            toastFactory.showCenterToast("SD卡不可用，请稍后再试一下看看…");
        }

    }

    private void updateItemDataClick() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            new PermissionHelper(activity, getContext(), new PermissionHelper.OnResult() {
                @Override
                public void hasPermission() {
                    final String[] temp = FileTools.getFileList(FileTools.MICROMSG_DIR, ".itemupdate");
                    if (temp.length > 0) {
                        dialogFactory.showSingleSelectWithConfirmButton(temp, new SelectDialogClicker() {
                            @Override
                            public void click(int checkedItem) {
                                File f = new File(FileTools.MICROMSG_DIR, temp[checkedItem]);
                                loadingDialog = LoadingDialog.newInstant("正在更新", "正在打开文件...", true);
                                loadingDialog.setCancelClick(null);
                                loadingDialog.setCancelable(false);
                                ItemUpdater updater = new ItemUpdater(getContext(), f);
                                updater.setOnFinished(new IDialogDismiss() {
                                    @Override
                                    public void onDismiss(boolean isConfirmed, Object... values) {
                                        loadingDialog.dismiss();
                                        String s = (String) values[0];
                                        toastFactory.showCenterToast(s);
                                    }
                                });
                                loadingDialog.show(getFragmentManager(), "loading");
                                updater.execute();
                            }
                        });
                    } else {
                        toastFactory.showCenterToast(FileTools.MICROMSG_DIR + "未找到数据文件。");
                    }
                }
            }).request(Permission.Group.STORAGE);
        }else {
            toastFactory.showCenterToast("SD卡不可用，请稍后再试一下看看…");
        }
    }

    private void updateAPPClick() {
        if (!XXPermissions.isHasPermission(getContext(), Permission.REQUEST_INSTALL_PACKAGES)) {
            dialogFactory.showDefaultConfirmDialog("请授予安装未知来源软件的权限。", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new PermissionHelper(activity, getContext(), new PermissionHelper.OnResult() {
                        @Override
                        public void hasPermission() {
                            toastFactory.showCenterToast("已获得授权，请重新点击按钮更新程序！");
                        }
                    }).request(Permission.REQUEST_INSTALL_PACKAGES);
                }
            });

        } else {
            new PermissionHelper(activity, getContext(), new PermissionHelper.OnResult() {
                @Override
                public void hasPermission() {
//                    updateApp();
                    openUpdateAppDialog();
                }
            }).request(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void openUpdateAppDialog() {
        long currentVersion = AppSetupOperator.getLastAppVersion();
        long downloadVersion = AppSetupOperator.getDownloadAppVersion();
        if (downloadVersion > currentVersion) {
            String filePath = activity.getFilesDir().getAbsolutePath() + File.separator + "UpdateFiles" + File.separator + "VersionCode" + downloadVersion;
            ShowAppUpdateInfomationDialog showAppUpdateInfomationDialog = ShowAppUpdateInfomationDialog.newInstant(filePath);
            showAppUpdateInfomationDialog.setOnDismiss(new IDialogDismiss() {
                @Override
                public void onDismiss(boolean isConfirmed, Object... values) {
                    if (isConfirmed) {
                        String zipFilePath = (String) values[0];
                        File zipFile = new File(zipFilePath);
                        updateAppByEmail(zipFile);
                    }
                }
            });
            showAppUpdateInfomationDialog.show(getFragmentManager(), "showUpdateInfo");
        } else {
//            dialogAndToast.showCenterToast("提示", "当前已是最新版本，不用更新！");
            updateAppByMicroMsg();
        }
    }

    private void updateAppByMicroMsg() {
        final String[] temp = FileTools.getFileList(FileTools.MICROMSG_DIR, ".update");
        if (temp.length > 0) {
            dialogFactory.showSingleSelectWithConfirmButton(temp, new SelectDialogClicker() {
                @Override
                public void click(int checkedItem) {
                    File f = new File(FileTools.MICROMSG_DIR, temp[checkedItem]);
                    loadingDialog = LoadingDialog.newInstant("正在更新", "正在打开文件...", false);
                    loadingDialog.setCancelable(false);
                    final AppUpdater updater = new AppUpdater(getContext(), f);
                    loadingDialog.setCancelClick(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            updater.cancleUpdate();
                        }
                    });
                    updater.setOnFinish(new IDialogDismiss() {
                        @Override
                        public void onDismiss(boolean isConfirmed, Object... values) {
                            if (isConfirmed) {
                                File updateFile = (File) values[0];
                                if (updateFile != null) {
                                    OtherTools.openAPKFile(activity, updateFile);
                                } else {
                                    toastFactory.showCenterToast("安装文件损坏");
                                }
                            } else {
                                String s = (String) values[0];
                                dialogFactory.showInfoDialog(s);
                            }
                        }
                    });
                    loadingDialog.show(getFragmentManager(), "loading");
                    updater.execute();
                }
            });
        } else {
            toastFactory.showCenterToast(FileTools.MICROMSG_DIR + "文件夹内未找到数据文件。");
        }
    }

    private void updateAppByEmail(File zipFile) {
        loadingDialog = LoadingDialog.newInstant("正在更新", "正在打开文件...", false);
        loadingDialog.setCancelable(false);
        final AppUpdater updater = new AppUpdater(getContext(), zipFile);
        loadingDialog.setCancelClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updater.cancleUpdate();
            }
        });
        updater.setOnFinish(new IDialogDismiss() {
            @Override
            public void onDismiss(boolean isConfirmed, Object... values) {
                if (isConfirmed) {
                    File updateFile = (File) values[0];
                    if (updateFile != null) {
                        OtherTools.openAPKFile(activity, updateFile);
                    } else {
                        toastFactory.showCenterToast("安装文件损坏");
                    }
                } else {
                    String s = (String) values[0];
                    dialogFactory.showInfoDialog(s);
                }
            }
        });
        loadingDialog.show(getFragmentManager(), "loading");
        updater.execute();
    }
}

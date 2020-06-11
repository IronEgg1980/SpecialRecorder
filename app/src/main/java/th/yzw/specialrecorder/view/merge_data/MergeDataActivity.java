package th.yzw.specialrecorder.view.merge_data;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.mail.MessagingException;

import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.DataMerger;
import th.yzw.specialrecorder.DAO.DownloadFileOperator;
import th.yzw.specialrecorder.DAO.ImportFileOperator;
import th.yzw.specialrecorder.DAO.ShowDataOperator;
import th.yzw.specialrecorder.DAO.SumTotalOperator;
import th.yzw.specialrecorder.JSON.ShowDataJSONHelper;
import th.yzw.specialrecorder.MyActivity;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.model.ImportedFile;
import th.yzw.specialrecorder.model.ShowDataEntity;
import th.yzw.specialrecorder.model.SumTotalRecord;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.MyDateUtils;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.tools.SendEmailHelper;
import th.yzw.specialrecorder.view.common.BaseAdapter;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.DateRangePopWindow;
import th.yzw.specialrecorder.view.common.EnterPWDPopWindow;
import th.yzw.specialrecorder.view.common.InfoPopWindow;
import th.yzw.specialrecorder.view.common.MyDividerItemDecoration;
import th.yzw.specialrecorder.view.common.SelectMonthPopWindow;
import th.yzw.specialrecorder.view.common.ToastFactory;
import th.yzw.specialrecorder.view.common.WaitingDialog;
import th.yzw.specialrecorder.view.service.DownloadMergeFileSVC;

public class MergeDataActivity extends MyActivity {
    private final int CLEAR_FILES_REQUESTCODE = 111;
    private final int IMPORT_FILES_REQUESTCODE = 222;
    private final int SHARE_REQUESTCODE = 333;
    private final String net_error_message = "发送数据需要使用网络，请打开WIFI或移动数据（流量使用大约0.01-0.03M，请放心使用）";
    private MergeDataWatingDialog dataWatingDialog;
    private TextView dateTextView;
    private View mergeDataBegin;
    private View mergeDataImportrecord;
    private View mergeDataImportfiles;
    private View mergeDataClearfiles;
    private View shareData;
    private TextView textView1, textView2, textView3, textView4;
    private String phoneId;
    private SimpleDateFormat format, fileNameFormater;
    private List<SumTotalRecord> list;
    private BaseAdapter<SumTotalRecord> adapter;
    private long mergeMonth;
    private boolean hasData;
    private boolean isHideMode;
    private DataMerger dataMerger;
    private boolean isCreate;
    private InfoPopWindow infoPopWindow;
    private SelectMonthPopWindow selectMonthPopWindow;
    private BroadcastReceiver receiver;
    private WaitingDialog waitingDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isHideMode = AppSetupOperator.isHideMode();
        if (isHideMode) {
            setContentView(R.layout.setup_layout);
        } else {
            setContentView(R.layout.merge_data_layout);
        }
        Toolbar toolbar = findViewById(R.id.include);
        toolbar.setNavigationIcon(R.mipmap.back2);
        toolbar.setTitle("合并数据");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        isCreate = true;
        mergeMonth = System.currentTimeMillis();
        phoneId = AppSetupOperator.getPhoneId();
        format = new SimpleDateFormat("正在合并【yyyy年M月份】数据", Locale.CHINA);
        fileNameFormater = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        dataWatingDialog = new MergeDataWatingDialog();
        selectMonthPopWindow = new SelectMonthPopWindow(this);
        selectMonthPopWindow.setDisMiss(new OnSelectDateRangeDismiss() {
            @Override
            public void onDissmiss(boolean isConfirm, long... timeInMillis) {
                if (isConfirm) {
                    mergeMonth = timeInMillis[0];
                    hasData = true;
                    changeButtonStatus(hasData);
                }
            }
        });
        initialData();
        initialView();
        initialReceiver();
    }

    @Override
    protected void onDestroy() {
        Broadcasts.unBindBroadcast(this, receiver);
        super.onDestroy();
    }

    private void initialReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Broadcasts.EMAIL_RECEIVE_SUCCESS.equals(action)) {
                    if (FileTools.getMergeFileList().length == 0) {
                        dataWatingDialog.dismiss();
                        infoPopWindow.show("未找到数据文件，请通知相关人员发送文件...");
                    } else
                        importFile();
                } else if (Broadcasts.EMAIL_RECEIVE_FAIL.equals(action)) {
                    dataWatingDialog.dismiss();
                    infoPopWindow.show("文件同步失败，请检查网络...\n（说明：需要开启网络访问邮件服务器以同步数据文件，每次同步大约需要使用0.1-0.3M流量，请放心使用。）");
                } else if (Objects.equals(intent.getAction(), Broadcasts.NET_DISCONNECTED)) {
                    waitingDialog.dismiss();
                    infoPopWindow.show(net_error_message);
                } else if (Objects.equals(intent.getAction(), Broadcasts.EMAIL_SEND_FAIL)) {
                    waitingDialog.dismiss();
                    infoPopWindow.show("发送失败！");
                } else if (Objects.equals(intent.getAction(), Broadcasts.EMAIL_SEND_SUCCESS)) {
                    waitingDialog.dismiss();
                    infoPopWindow.show("发送成功！");
                }
            }
        };
        Broadcasts.bindBroadcast(this, receiver,
                Broadcasts.EMAIL_RECEIVE_FAIL,
                Broadcasts.EMAIL_RECEIVE_SUCCESS,
                Broadcasts.NET_DISCONNECTED,
                Broadcasts.EMAIL_SEND_FAIL,
                Broadcasts.EMAIL_SEND_SUCCESS);
    }

    private Animator buttonClickAnima(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0, -30f);
        animator.setDuration(50);
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    private void initialView() {
        textView1 = findViewById(R.id.textview1);
        textView2 = findViewById(R.id.textview2);
        textView3 = findViewById(R.id.textview3);
        textView4 = findViewById(R.id.textview4);
        dateTextView = findViewById(R.id.dateTextView);
        mergeDataBegin = findViewById(R.id.merge_data_begin);
        mergeDataBegin.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                Animator animator = buttonClickAnima(mergeDataBegin);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        beginMergeClick(mergeDataBegin);
                    }
                });
                animator.start();
            }
        });
        mergeDataImportrecord = findViewById(R.id.merge_data_importrecord);
        mergeDataImportrecord.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                Animator animator = buttonClickAnima(mergeDataImportrecord);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        importThisDataClick();
                    }
                });
                animator.start();
            }
        });
        mergeDataImportfiles = findViewById(R.id.merge_data_importfiles);
        mergeDataImportfiles.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                Animator animator = buttonClickAnima(mergeDataImportfiles);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        importFileClick();
                    }
                });
                animator.start();
            }
        });
        mergeDataClearfiles = findViewById(R.id.merge_data_clearfiles);
        mergeDataClearfiles.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                Animator animator = buttonClickAnima(mergeDataClearfiles);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        clearReceivedFiles();
                    }
                });
                animator.start();
            }
        });
        shareData = findViewById(R.id.share_data);
        shareData.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                Animator animator = buttonClickAnima(shareData);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        shareData();
                    }
                });
                animator.start();
            }
        });
        RecyclerView mergeDataRecyclerView = findViewById(R.id.merge_data_recyclerView);
        mergeDataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BaseAdapter<SumTotalRecord>(R.layout.merge_data_item,list) {
            @Override
            public void bindData(BaseViewHolder baseViewHolder, SumTotalRecord data) {
                baseViewHolder.setText(R.id.show_item_name,data.getName());
                baseViewHolder.setText(R.id.show_item_count,String.valueOf(data.getCount()));
            }
        };
        mergeDataRecyclerView.setAdapter(adapter);
        mergeDataRecyclerView.addItemDecoration(new MyDividerItemDecoration());
        changeButtonStatus(hasData);
        infoPopWindow = new InfoPopWindow(this);
        waitingDialog = new WaitingDialog();
    }

    private void initialData() {
        list = new ArrayList<>();
        list.addAll(SumTotalOperator.getMergeDataAll());
        if (list.size() > 0) {
            SumTotalRecord record = list.get(0);
            mergeMonth = record.getMonth();
            if (mergeMonth < 1000)
                mergeMonth = System.currentTimeMillis() - 20 * MyDateUtils.ONE_DAY_MILLIS;
            hasData = true;
        } else {
            hasData = false;
        }
    }

    private void updateList() {
        list.clear();
        list.addAll(SumTotalOperator.getMergeDataAll());
        adapter.notifyDataSetChanged();
    }

    private void changeButtonStatus(boolean flag) {
        String title = flag ? "重新合并" : "开始合并";
        String dateString = flag ? format.format(mergeMonth) : "轻触左侧按钮开始合并数据...";
        dateTextView.setText(dateString);
        textView1.setText(title);
        if (flag) {
            mergeDataImportrecord.setVisibility(View.VISIBLE);
            mergeDataImportfiles.setVisibility(View.VISIBLE);
            shareData.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.VISIBLE);
            textView3.setVisibility(View.VISIBLE);
            textView4.setVisibility(View.VISIBLE);
        } else {
            mergeDataImportrecord.setVisibility(View.INVISIBLE);
            mergeDataImportfiles.setVisibility(View.INVISIBLE);
            shareData.setVisibility(View.INVISIBLE);
            textView2.setVisibility(View.INVISIBLE);
            textView3.setVisibility(View.INVISIBLE);
            textView4.setVisibility(View.INVISIBLE);
        }
        isCreate = false;
    }

    private void showSelectMonthDialog() {
        selectMonthPopWindow.setDate(mergeMonth)
                .show(mergeDataBegin);
    }

    private void reMergeData() {
        new ConfirmPopWindow(this).setDialogDismiss(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if (result == Result.OK) {
                    SumTotalOperator.deleAll();
                    ImportFileOperator.deleAll();
                    updateList();
                    hasData = false;
                    changeButtonStatus(hasData);
                    new ToastFactory(MergeDataActivity.this).showCenterToast("数据已清除");
                    showSelectMonthDialog();
                }
            }
        }).toConfirm("是否清除列表内所有数据并重新合并？");
    }

    private void beginMergeClick(View view) {
        if (hasData) {
            reMergeData();
        } else {
            showSelectMonthDialog();
        }
    }

    private void clear() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            new ConfirmPopWindow(this).setDialogDismiss(new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... value) {
                    if (result == Result.OK) {
                        FileTools.delMergeFiles();
                        DownloadFileOperator.deleAllMergeFile();
                        new ToastFactory(MergeDataActivity.this).showCenterToast("数据文件已清理干净");
                    }
                }
            }).toConfirm("是否清理所有数据文件？");
        } else {
            infoPopWindow.show("外置存储卡未准备好，请稍后重试！");
        }
    }

    // 清空接收到的文件
    public void clearReceivedFiles() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            clear();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSION_GROUP_STORAGE, CLEAR_FILES_REQUESTCODE);
        }
    }

    private void importFile() {
        dataMerger = new DataMerger(MergeDataActivity.this, mergeMonth);
        dataMerger.setOnFinished(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result1, Object... values) {
                infoPopWindow.show((String) values[0]);
                updateList();
            }
        });
        dataMerger.execute();
    }

    //导入数据
    public void importFileClick() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            dataWatingDialog.show(getSupportFragmentManager(), "waiting");
            startService(new Intent(this, DownloadMergeFileSVC.class));
        } else {
            ActivityCompat.requestPermissions(this, PERMISSION_GROUP_STORAGE, CLEAR_FILES_REQUESTCODE);
        }
    }

    private void importThisData() {
        final DateRangePopWindow dateRangePopWindow = new DateRangePopWindow(this);
        dateRangePopWindow.show(mergeDataImportrecord, new OnSelectDateRangeDismiss() {
            @Override
            public void onDissmiss(boolean isConfirm, long... timeInMillis) {
                if (isConfirm) {
                    long start = timeInMillis[0];
                    long end = timeInMillis[1];
                    List<SumTotalRecord> temp = SumTotalOperator.getSumData(start, end);
                    if (temp.isEmpty()) {
                        new ToastFactory(MergeDataActivity.this).showCenterToast("所选时间段内没有数据，请重新选择！");
                        updateList();
                        return;
                    }
                    for (SumTotalRecord record : temp) {
                        record.setPhoneId(phoneId);
                        record.setMonth(mergeMonth);
                    }
                    SumTotalOperator.saveAll(temp);
                    ImportedFile importedFile = new ImportedFile(mergeMonth, phoneId, "local");
                    importedFile.save();
                    new ToastFactory(MergeDataActivity.this).showCenterToast("导入完成！");
                }
                updateList();
            }
        });
    }

    private void importThisDataClick() {
        final ImportedFile localData = ImportFileOperator.findSingleByPhoneId(phoneId);
        if (localData != null) {
            new ConfirmPopWindow(this).setDialogDismiss(new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... values) {
                    if (result == Result.OK) {
                        ImportFileOperator.deleAll(phoneId);
                        SumTotalOperator.deleAll(phoneId);
                        importThisData();
                    }
                }
            }).toConfirm("已导入本机数据，是否重新导入？");
        } else {
            importThisData();
        }
    }

    private void sendShareFile(final String pwd) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = getShareFile(pwd);
                String title = file.getName();
                try {
                    String content = "This is total data file in last month,please download it later !" +
                            OtherTools.getPhoneInformation();
                    new SendEmailHelper().sendShareDataFile(MergeDataActivity.this, title, content, file);
                } catch (IOException | MessagingException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void share() {
        OtherTools.checkFileUriExposure();
        new EnterPWDPopWindow(this, "发送数据", "请设置密码")
                .setIcon(getDrawable(R.drawable.ic_send_18dp))
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            waitingDialog.show(getSupportFragmentManager(), "loading");
                            sendShareFile(OtherTools.getTotalDataFilePWD((String) values[0]));
//                            Uri fileUri = null;
//                            if (Build.VERSION.SDK_INT >= 24) {
//                                fileUri = FileProvider.getUriForFile(MergeDataActivity.this, "th.yzw.specialrecorder.fileprovider", file);
//                            } else {
//                                fileUri = Uri.fromFile(file);
//                            }
//                            Intent intent = new Intent(Intent.ACTION_SEND);
//                            intent.setType("*/*");
//                            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
//                            intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI"));
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(Intent.createChooser(intent, "发送给："));
                        }
                    }
                }).show();
    }

    private void shareData() {
        if (list.isEmpty()) {
            infoPopWindow.show("列表内没有数据！\n请先合并数据，全部完成后再发送。");
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            share();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSION_GROUP_STORAGE, CLEAR_FILES_REQUESTCODE);
        }
    }

    //生成加密的分享文件
    File getShareFile(String pwd) {
        FileTools.clearFiles(getCacheDir());
        String fileName = fileNameFormater.format(mergeMonth).substring(0, 7) +
                "(合并于" +
                fileNameFormater.format(System.currentTimeMillis()) +
                ")_"+
                MyDateUtils.getDateDiff()+
                ".total";
        File file = new File(getCacheDir(), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            StringBuilder builder = new StringBuilder();
            List<ShowDataEntity> sharedList = ShowDataOperator.getShareList(list, fileName);
            String jsonString = new ShowDataJSONHelper().toJSONArray(sharedList).toString();
            builder.append(pwd).append(jsonString);
            FileTools.writeDecryptFile(builder.toString(), file);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            infoPopWindow.show("写入文件出错！原因为：" + e.getMessage());
            return null;
        } catch (JSONException ex) {
            ex.printStackTrace();
            infoPopWindow.show("生成文件出错！原因为：" + ex.getMessage());
            return null;
        }
    }

    @Override
    protected void onPermissionGranted(int requestCode) {
        switch (requestCode) {
            case CLEAR_FILES_REQUESTCODE:
                clearReceivedFiles();
                break;
            case IMPORT_FILES_REQUESTCODE:
                importFileClick();
                break;
            case SHARE_REQUESTCODE:
                shareData();
                break;
            default:
                new ToastFactory(this).showCenterToast("已获取权限，请继续操作。");
        }
    }

    @Override
    protected void onPermissionDenied(int requestCode) {
        new ConfirmPopWindow(this)
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            ActivityCompat.requestPermissions(MergeDataActivity.this, PERMISSION_GROUP_STORAGE, 999);
                        }
                    }
                })
                .toConfirm("由于您拒绝授予读取存储权限，该功能无法使用！\n请点击【确定】授予权限。");
    }

}

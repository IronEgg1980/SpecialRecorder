package th.yzw.specialrecorder.view;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.mail.MessagingException;

import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.DataBackupAndRestore;
import th.yzw.specialrecorder.DAO.RecordEntityOperator;
import th.yzw.specialrecorder.DAO.SumTotalOperator;
import th.yzw.specialrecorder.MyActivity;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.model.SumTotalRecord;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.tools.SendEmailHelper;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.DateRangePopWindow;
import th.yzw.specialrecorder.view.common.InfoPopWindow;
import th.yzw.specialrecorder.view.common.LoadingDialog;
import th.yzw.specialrecorder.view.common.MyDividerItemDecoration;
import th.yzw.specialrecorder.view.common.ToastFactory;
import th.yzw.specialrecorder.view.common.WaitingDialog;

public class ShowTotalDataActivity extends MyActivity {
    protected class ShowTotalAdapter extends RecyclerView.Adapter<ShowTotalAdapter.ViewHolder> {
        void updateList(long start, long end) {
            recordEntityList.clear();
            List<SumTotalRecord> temp = SumTotalOperator.getSumData(start, end);
            for (SumTotalRecord record : temp) {
                record.setPhoneId(phoneId);
                calendar.setTimeInMillis(start);
                calendar.add(Calendar.DAY_OF_MONTH, 28);
                calendar.add(Calendar.MILLISECOND, -1);
                record.setMonth(calendar.getTimeInMillis());
                recordEntityList.add(record);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ShowTotalAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.show_details_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ShowTotalAdapter.ViewHolder viewHolder, int i) {
            final SumTotalRecord r = recordEntityList.get(i);
            viewHolder.name.setText(r.getName());
            viewHolder.count.setText(String.valueOf(r.getCount()));
        }

        @Override
        public int getItemCount() {
            return recordEntityList.size();
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, count;
            RelativeLayout root;

            ViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.show_item_name);
                count = itemView.findViewById((R.id.show_item_count));
                root = itemView.findViewById(R.id.root);
            }
        }
    }

    private final String TAG = "殷宗旺";
    private final String net_error_message = "发送数据需要使用网络，请打开WIFI或移动数据（流量使用大约0.01-0.03M，请放心使用）";
    private InfoPopWindow infoPopWindow;
    private List<SumTotalRecord> recordEntityList;
    private TextView showTotalNodata;
    private AppCompatTextView dateInfo;
    private RecyclerView showTotalFragmentRecycler;
    private long start, end;
    private Calendar calendar;
    private SimpleDateFormat format;
    private ShowTotalAdapter adapter;
    //    private File path, cacheDir;
    private String phoneId;
    private View changeDateBT, deleBT, shareBT;
    private int REQUEST_CODE_SHARE = 11, REQUEST_CODE_BACKUP = 22;
    private WaitingDialog waitingDialog;
    private BroadcastReceiver receiver;

    @NonNull
    private Animator buttonClickAnima(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0, -30f);
        animator.setDuration(50);
        animator.setRepeatCount(1);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    private void selectDateRange() {
        new DateRangePopWindow(this).show(changeDateBT, new OnSelectDateRangeDismiss() {
            @Override
            public void onDissmiss(boolean isConfirm, long... timeInMillis) {
                if (isConfirm) {
                    start = timeInMillis[0];
                    end = timeInMillis[1];
                    adapter.updateList(start, end);
                    showInfo();
                }
            }
        });
    }

    private void showInfo() {
        String s = format.format(start) + " 至 " + format.format(end);
        dateInfo.setText(s);
        if (adapter.getItemCount() == 0) {
            showTotalNodata.setVisibility(View.VISIBLE);
        } else {
            showTotalNodata.setVisibility(View.GONE);
            showTotalFragmentRecycler.smoothScrollToPosition(0);
        }
    }

    private void backup() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File path = new File(FileTools.BACKUP_DIR);
            if (!path.exists() && !path.mkdirs()) {
                infoPopWindow.show("创建目录失败！请重试一次…");
                return;
            }
            final LoadingDialog loadingDialog = LoadingDialog.newInstant("备份数据", "准备中...", true);
            loadingDialog.setCancelClick(null);
            loadingDialog.setCancelable(false);
            DataBackupAndRestore dataBackuper = new DataBackupAndRestore(this, "backup");
            dataBackuper.setOnFinish(new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... values) {
                    loadingDialog.dismiss();
                    if (result == Result.OK) {
                        RecordEntityOperator.deleAllBetweenDate(start, end);
                        adapter.updateList(start, end);
                        new ToastFactory(ShowTotalDataActivity.this).showCenterToast("备份后删除数据成功");
                    } else {
                        String s = (String) values[0];
                        infoPopWindow.show(s);
                    }
                }
            });
            loadingDialog.show(getSupportFragmentManager(), "loading");
            dataBackuper.execute();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSION_GROUP_STORAGE, REQUEST_CODE_BACKUP);
        }
    }

    private void showConfirmDeleDialog() {
        new ConfirmPopWindow(this)
                .setOtherButton("立即备份")
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OTHER) {
                            backup();
                        } else if (result == Result.OK) {
                            RecordEntityOperator.deleAllBetweenDate(start, end);
                            adapter.updateList(start, end);
                            new ToastFactory(ShowTotalDataActivity.this).showCenterToast("删除成功");
                        }
                    }
                })
                .toConfirm("确定删除吗？\n（注意：如果没有备份数据，删除数据后将不能恢复！建议立即备份数据，再执行删除操作。）");
    }

    private void dele() {
        if (adapter.getItemCount() == 0) {
            infoPopWindow.show("当前列表内没有数据");
            return;
        }
        final ConfirmPopWindow popWindow = new ConfirmPopWindow(this);
        popWindow.setDialogDismiss(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if (result == Result.OK) {
                    popWindow.isResumeAlpha = false;
                    showConfirmDeleDialog();
                }
            }
        })
                .toConfirm("是否要删除【 " + format.format(start) + " 】至【 " + format.format(end) + " 】内的所有数据？");
    }

    private void initialView() {
        showTotalNodata = findViewById(R.id.show_total_nodata);
        dateInfo = findViewById(R.id.dateTextView);
        changeDateBT = findViewById(R.id.changeDate);
        changeDateBT.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                Animator animator = buttonClickAnima(changeDateBT);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        selectDateRange();
                    }
                });
                animator.start();
            }
        });
        deleBT = findViewById(R.id.dele_data);
        deleBT.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                Animator animator = buttonClickAnima(deleBT);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        dele();
                    }
                });
                animator.start();
            }
        });
        shareBT = findViewById(R.id.send_data);
        shareBT.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                Animator animator = buttonClickAnima(shareBT);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        share();
                    }
                });
                animator.start();
            }
        });
        showTotalFragmentRecycler = findViewById(R.id.show_total_fragment_recycler);
        showTotalFragmentRecycler.setLayoutManager(new LinearLayoutManager(this));
        showTotalFragmentRecycler.setAdapter(adapter);
        showTotalFragmentRecycler.addItemDecoration(new MyDividerItemDecoration());
        waitingDialog = new WaitingDialog();
    }

    private void initialBroadcastReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                waitingDialog.dismiss();
                String message = "";
                if (Objects.equals(intent.getAction(), Broadcasts.NET_DISCONNECTED)) {
                    message = net_error_message;
                } else if (Objects.equals(intent.getAction(), Broadcasts.EMAIL_SEND_FAIL)) {
                    message = "发送失败！";
                } else if (Objects.equals(intent.getAction(), Broadcasts.EMAIL_SEND_SUCCESS)) {
                    message = "发送成功！";
                }
                infoPopWindow.show(message);
            }
        };
        Broadcasts.bindBroadcast(this, receiver, Broadcasts.NET_DISCONNECTED, Broadcasts.EMAIL_SEND_FAIL, Broadcasts.EMAIL_SEND_SUCCESS);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_total_data_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("查看/发送汇总数据");
        toolbar.setNavigationIcon(R.mipmap.back2);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        infoPopWindow = new InfoPopWindow(this);
        recordEntityList = new ArrayList<>();
        calendar = new GregorianCalendar(Locale.CHINA);
        end = calendar.getTimeInMillis();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        start = calendar.getTimeInMillis();
        format = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        phoneId = AppSetupOperator.getPhoneId();
        adapter = new ShowTotalAdapter();
        adapter.updateList(start, end);
//        cacheDir = getCacheDir();
        initialView();
        initialBroadcastReceiver();
    }

    @Override
    public void onStart() {
        super.onStart();
        showInfo();
    }

    // 分享数据
    private void share() {
        if (recordEntityList.isEmpty()) {
            infoPopWindow.show("该时间段内没有汇总数据！");
            return;
        }
        if (!OtherTools.isNetworkConnected(this)) {
            infoPopWindow.show(net_error_message);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            OtherTools.checkFileUriExposure();
//            Uri fileUri = null;
            final File file = FileTools.getShareFile(recordEntityList, new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... values) {
                    if (result == Result.CANCEL) {
                        infoPopWindow.show((String) values[0]);
                    }
                }
            });
            if (file == null) {
                return;
            }
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.indexOf("."));
            final String content = "My appid is " + AppSetupOperator.getPhoneId() +
                    " , this is my data between " + format.format(start) +
                    " and " + format.format(end) +
                    ". Please receive it!";
            final String finalFileName = fileName;
            waitingDialog.show(getSupportFragmentManager(), "waiting");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        new SendEmailHelper().sendMultiEmail(ShowTotalDataActivity.this, finalFileName, content, false, file);
                    } catch (IOException | MessagingException e) {
                        e.printStackTrace();
                        Broadcasts.sendBroadcast(ShowTotalDataActivity.this, Broadcasts.NET_DISCONNECTED);
                    }
                }
            }).start();
//            if (Build.VERSION.SDK_INT >= 24) {
//                fileUri = FileProvider.getUriForFile(this, "th.yzw.specialrecorder.fileprovider", file);
//            } else {
//                fileUri = Uri.fromFile(file);
//            }
//            Intent intent = new Intent(Intent.ACTION_SEND);
//            intent.setType("*/*");
//            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
//            intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI"));
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startActivity(Intent.createChooser(intent, "发送给："));
        } else {
            ActivityCompat.requestPermissions(this, PERMISSION_GROUP_STORAGE, REQUEST_CODE_SHARE);
        }
    }

    @Override
    protected void onDestroy() {
        // 注销该广播接收器
        Broadcasts.unBindBroadcast(this, receiver);
        super.onDestroy();
    }

//    private void clearSameFile(String fileName) {
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            path = new File(Environment.getExternalStorageDirectory() + "/tencent/MicroMsg/download/");
//            if (!path.exists()) {
//                path = new File("/tencent/MicroMsg/download/");
//            }
//        }
//        if (path.exists()) {
//            File file = new File(path, fileName);
//            if (file.exists())
//                file.delete();
//        } else {
//            path.mkdirs();
//        }
//    }
//
//    //生成加密的分享文件
//    private File getShareFile() {
//        FileTools.clearFiles(cacheDir);
//        long currentTime = System.currentTimeMillis();
//        String randomText = OtherTools.getRandomString(4) + "_" + new SimpleDateFormat("yyMMddHHmmss", Locale.CHINA).format(currentTime);
//        String fileName = "SendBy" + randomText + ".data";
//        clearSameFile(fileName);
//        try {
//            File file = new File(cacheDir, fileName);
//            if (file.exists()) {
//                file.delete();
//            }
//            file.createNewFile();
//            String s = new SumTotalJSONHelper().getSharedJSON(recordEntityList);
//            FileTools.writeDecryptFile(s, file);
//            return file;
//        } catch (IOException e) {
//            e.printStackTrace();
//            infoPopWindow.show("写入文件出错！原因为：" + e.getMessage());
//            return null;
//        } catch (JSONException ex) {
//            ex.printStackTrace();
//            infoPopWindow.show("生成文件出错！原因为：" + ex.getMessage());
//            return null;
//        }
//    }

    @Override
    protected void onPermissionGranted(int requestCode) {
        if (requestCode == REQUEST_CODE_SHARE) {
            share();
        } else if (requestCode == REQUEST_CODE_BACKUP) {
            backup();
        } else {
            new ToastFactory(this).showCenterToast("已获得存储权限。");
        }
    }

    @Override
    protected void onPermissionDenied(int requestCode) {
        new ConfirmPopWindow(this)
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            ActivityCompat.requestPermissions(ShowTotalDataActivity.this, PERMISSION_GROUP_STORAGE, 33);
                        }
                    }
                })
                .toConfirm("由于您拒绝授予读取存储权限，该功能无法使用！\n请点击【确定】授予权限。");
    }
}

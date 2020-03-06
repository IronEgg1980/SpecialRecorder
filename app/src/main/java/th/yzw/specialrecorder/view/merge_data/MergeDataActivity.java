package th.yzw.specialrecorder.view.merge_data;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.hjq.permissions.Permission;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.DataMerger;
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
import th.yzw.specialrecorder.tools.PermissionHelper;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.DateRangePopWindow;
import th.yzw.specialrecorder.view.common.EnterPWDPopWindow;
import th.yzw.specialrecorder.view.common.InfoPopWindow;
import th.yzw.specialrecorder.view.common.MyDividerItemDecoration;
import th.yzw.specialrecorder.view.common.SelectItemPopWindow;
import th.yzw.specialrecorder.view.common.SelectMonthPopWindow;
import th.yzw.specialrecorder.view.common.ToastFactory;

public class MergeDataActivity extends MyActivity {

    protected class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView name, count;
            View view;

            ViewHolder(View itemView) {
                super(itemView);
                view = itemView;
                name = (TextView) itemView.findViewById(R.id.show_item_name);
                count = (TextView) itemView.findViewById((R.id.show_item_count));
            }
        }

        private List<SumTotalRecord> recordEntityList;

        MyAdapter(List<SumTotalRecord> recordEntityList) {
            this.recordEntityList = recordEntityList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.merge_data_item, viewGroup, false);
            final ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
            final SumTotalRecord r = recordEntityList.get(i);
            viewHolder.name.setText(r.getName());
            viewHolder.count.setText(String.valueOf(r.getCount()));
        }

        @Override
        public int getItemCount() {
            return recordEntityList.size();
        }

    }

    private MergeDataWatingDialog dataWatingDialog;
    private TextView dateTextView;
    private View mergeDataBegin;
    private View mergeDataImportrecord;
    private View mergeDataImportfiles;
    private View mergeDataClearfiles;
    private View shareData;
    private TextView textView1, textView2, textView3, textView4;
    private String phoneId;
    private SimpleDateFormat format;
    private List<SumTotalRecord> list;
    private MyAdapter adapter;
    private long mergeMonth;
    private boolean hasData;
    private boolean isHideMode;
    private DataMerger dataMerger;
    private boolean isCreate;
    private InfoPopWindow infoPopWindow;
    private SelectMonthPopWindow selectMonthPopWindow;

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
                        importThisDataClick(mergeDataImportrecord);
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
        adapter = new MyAdapter(list);
        mergeDataRecyclerView.setAdapter(adapter);
        mergeDataRecyclerView.addItemDecoration(new MyDividerItemDecoration());
        changeButtonStatus(hasData);
        infoPopWindow = new InfoPopWindow(this);
    }

    private void playAnimation() {
        AccelerateInterpolator interpolator = new AccelerateInterpolator();
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mergeDataImportrecord, "alpha", 0.5f, 1f);
        animator1.setDuration(500);
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mergeDataImportrecord.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                textView2.setVisibility(View.VISIBLE);
            }
        });
        ObjectAnimator animator11 = ObjectAnimator.ofFloat(mergeDataImportrecord, "scaleX", 0.4f, 1.1f, 1f);
        animator11.setDuration(500);
        animator11.setInterpolator(interpolator);
        ObjectAnimator animator111 = ObjectAnimator.ofFloat(mergeDataImportrecord, "scaleY", 0.4f, 1.1f, 1f);
        animator111.setDuration(500);
        animator111.setInterpolator(interpolator);

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mergeDataImportfiles, "alpha", 0.5f, 1f);
        animator2.setDuration(500);
        animator2.setStartDelay(200);
        animator2.setInterpolator(interpolator);
        animator2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mergeDataImportfiles.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                textView3.setVisibility(View.VISIBLE);
            }
        });
        ObjectAnimator animator22 = ObjectAnimator.ofFloat(mergeDataImportfiles, "scaleY", 0.4f, 1.1f, 1f);
        animator22.setStartDelay(200);
        animator22.setDuration(500);
        animator22.setInterpolator(interpolator);
        ObjectAnimator animator222 = ObjectAnimator.ofFloat(mergeDataImportfiles, "scaleX", 0.4f, 1.1f, 1f);
        animator222.setDuration(500);
        animator222.setStartDelay(200);
        animator222.setInterpolator(interpolator);

        ObjectAnimator animator3 = ObjectAnimator.ofFloat(shareData, "alpha", 0.5f, 1f);
        animator3.setDuration(500);
        animator3.setStartDelay(400);
        animator3.setInterpolator(interpolator);
        animator3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                shareData.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                textView4.setVisibility(View.VISIBLE);
            }
        });
        ObjectAnimator animator33 = ObjectAnimator.ofFloat(shareData, "scaleY", 0.4f, 1.1f, 1f);
        animator33.setStartDelay(400);
        animator33.setDuration(500);
        animator33.setInterpolator(interpolator);
        ObjectAnimator animator333 = ObjectAnimator.ofFloat(shareData, "scaleX", 0.4f, 1.1f, 1f);
        animator333.setDuration(500);
        animator333.setStartDelay(400);
        animator333.setInterpolator(interpolator);

        AnimatorSet animationSet = new AnimatorSet();
        animationSet.playTogether(animator1, animator11, animator111, animator2, animator22, animator222, animator3, animator33, animator333);
        animationSet.start();
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
            if (isCreate) {
                mergeDataImportrecord.setVisibility(View.VISIBLE);
                mergeDataImportfiles.setVisibility(View.VISIBLE);
                shareData.setVisibility(View.VISIBLE);
                textView2.setVisibility(View.VISIBLE);
                textView3.setVisibility(View.VISIBLE);
                textView4.setVisibility(View.VISIBLE);
            } else {
                playAnimation();
            }
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
            if (!FileTools.isMicroMsgPathExist())
                infoPopWindow.show("未找到数据文件目录");
            new ConfirmPopWindow(this).setDialogDismiss(new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... value) {
                    if (result == Result.OK) {
                        FileTools.delMergeFiles();
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
        new PermissionHelper(this, this, new PermissionHelper.OnResult() {
            @Override
            public void hasPermission(boolean flag) {
                if(flag)
                    clear();
            }
        }).request(Permission.Group.STORAGE);
    }

    private void importFile(final String[] fileList) {
        if (fileList == null || fileList.length == 0) {
            infoPopWindow.show(FileTools.MICROMSG_DIR + " 目录内没有找到数据文件");
            return;
        }
        SelectItemPopWindow popWindow = new SelectItemPopWindow(this, fileList, true);
        popWindow.show(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if (result == Result.OK) {
                    if (values.length > 0) {
                        List<File> list = new ArrayList<>();
                        for (Object value : values) {
                            String name = fileList[(int) value];
                            list.add(new File(FileTools.MICROMSG_DIR, name));
                        }
                        dataMerger = new DataMerger(MergeDataActivity.this, list, mergeMonth);
                        dataMerger.setOnFinished(new IDialogDismiss() {
                            @Override
                            public void onDismiss(Result result1, Object... values) {
                                dataWatingDialog.changeInformation("");
                                dataWatingDialog.changeFile("");
                                dataWatingDialog.dismiss();
                                infoPopWindow.show((String) values[0]);
//                                dialogFactory.showInfoDialog((String) values[0]);
                                updateList();
                            }
                        });
                        dataWatingDialog.show(getSupportFragmentManager(), "loading");
                        dataMerger.execute();
                    } else
                        new ToastFactory(MergeDataActivity.this).showCenterToast("未选择数据文件");
                }
            }
        });
    }

    //导入数据
    public void importFileClick() {
        new PermissionHelper(this, this, new PermissionHelper.OnResult() {
            @Override
            public void hasPermission(boolean flag) {
                if(flag)
                    importFile(FileTools.getMergeFileList());
            }
        }).request(Permission.Group.STORAGE);
    }

    private void importThisData() {
        DateRangePopWindow dateRangePopWindow = new DateRangePopWindow(this);
        dateRangePopWindow.show(mergeDataImportrecord, new OnSelectDateRangeDismiss() {
            @Override
            public void onDissmiss(boolean isConfirm, long... timeInMillis) {
                if (isConfirm) {
                    long start = timeInMillis[0];
                    long end = timeInMillis[1];
                    List<SumTotalRecord> temp = SumTotalOperator.getSumData(start, end);
                    if (temp == null || temp.size() == 0) {
                        infoPopWindow.show("该时间段内没有数据，请重新选择！");
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

    private void importThisDataClick(View view) {
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

    private void share() {
        OtherTools.checkFileUriExposure();
        new EnterPWDPopWindow(this, "发送数据", "请设置密码")
                .setIcon(getDrawable(R.drawable.ic_send_18dp))
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            Uri fileUri = null;
                            String pwd = OtherTools.getTotalDataFilePWD((String) values[0]);
                            File file = getShareFile(pwd);
                            if (Build.VERSION.SDK_INT >= 24) {
                                fileUri = FileProvider.getUriForFile(MergeDataActivity.this, "th.yzw.specialrecorder.fileprovider", file);
                            } else {
                                fileUri = Uri.fromFile(file);
                            }
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("*/*");
                            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                            intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI"));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(Intent.createChooser(intent, "发送给："));
                        }
                    }
                }).show();
    }

    private void shareData() {
        if (list.isEmpty()) {
            infoPopWindow.show("列表内没有数据！\n请先合并数据，全部完成后再发送。");
            return;
        }
        new PermissionHelper(this, this, new PermissionHelper.OnResult() {
            @Override
            public void hasPermission(boolean flag) {
                if (flag)
                    share();
            }
        }).request(Permission.Group.STORAGE);
    }

    //生成加密的分享文件
    File getShareFile(String pwd) {
        FileTools.clearFiles(getCacheDir());
        String fileName = format.format(mergeMonth) + ".total";
        if (FileTools.isMicroMsgPathExist()) {
            File microMsgFile = new File(FileTools.MICROMSG_DIR, fileName);
            if (microMsgFile.exists())
                microMsgFile.delete();
        }
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
//            dialogFactory.showInfoDialog("写入文件出错！原因为：" + e.getMessage());
            return null;
        } catch (JSONException ex) {
            ex.printStackTrace();
            infoPopWindow.show("生成文件出错！原因为：" + ex.getMessage());
            return null;
        }
    }
}

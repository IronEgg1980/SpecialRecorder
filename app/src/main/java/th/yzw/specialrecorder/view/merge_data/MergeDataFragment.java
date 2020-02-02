package th.yzw.specialrecorder.view.merge_data;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
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
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;
import th.yzw.specialrecorder.interfaces.SelectDialogClicker;
import th.yzw.specialrecorder.model.ImportedFile;
import th.yzw.specialrecorder.model.ShowDataEntity;
import th.yzw.specialrecorder.model.SumTotalRecord;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.MyDateUtils;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.tools.PermissionHelper;
import th.yzw.specialrecorder.view.RecorderActivity;
import th.yzw.specialrecorder.view.common.DialogFactory;
import th.yzw.specialrecorder.unuse.SelectDateRangeDialogFragment;
import th.yzw.specialrecorder.view.common.SelectMonthDialogFragment;
import th.yzw.specialrecorder.view.common.ToastFactory;
import th.yzw.specialrecorder.view.show_total.ShareTotalDataDialogFragment;

public class MergeDataFragment extends Fragment {
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
    private Button mergeDataBegin;
    private Button mergeDataImportrecord;
    private Button mergeDataImportfiles;
    private Button mergeDataClearfiles;
    private RecorderActivity activity;
    private String phoneId;
    private SimpleDateFormat format, format1;
    private List<SumTotalRecord> list;
    private MyAdapter adapter;
    private long mergeMonth;
    private boolean hasData;
    private boolean isHideMode;
    private DataMerger dataMerger;
    private boolean isCreate;
    private DialogFactory dialogFactory;
    private ToastFactory toast;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCreate = true;
        activity = (RecorderActivity) getActivity();
        activity.setTitle("数据合并");
        setHasOptionsMenu(true);
        mergeMonth = System.currentTimeMillis();
        isHideMode = AppSetupOperator.isHideMode();
        phoneId = AppSetupOperator.getPhoneId();
        format = new SimpleDateFormat("yyyy年M月份", Locale.CHINA);
        format1 = new SimpleDateFormat("yyyyMM", Locale.CHINA);
        dataWatingDialog = new MergeDataWatingDialog();
        dialogFactory = new DialogFactory(getContext());
        toast = new ToastFactory(getContext());
        initialData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (isHideMode) {
            return inflater.inflate(R.layout.setup_layout, container, false);
        }
        View view = inflater.inflate(R.layout.merge_data_fragment, container, false);
        initialView(view);
        changeButtonStatus(hasData);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.share_data) {
            shareData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initialView(View view) {
        dateTextView = view.findViewById(R.id.dateTextView);
        mergeDataBegin = view.findViewById(R.id.merge_data_begin);
        mergeDataBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginMergeClick(v);
            }
        });
        mergeDataImportrecord = view.findViewById(R.id.merge_data_importrecord);
        mergeDataImportrecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importThisDataClick(v);
            }
        });
        mergeDataImportfiles = view.findViewById(R.id.merge_data_importfiles);
        mergeDataImportfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importFileClick();
            }
        });
        mergeDataClearfiles = view.findViewById(R.id.merge_data_clearfiles);
        mergeDataClearfiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearReceivedFiles(v);
            }
        });
        RecyclerView mergeDataRecyclerView = view.findViewById(R.id.merge_data_recyclerView);
        mergeDataRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MyAdapter(list);
        mergeDataRecyclerView.setAdapter(adapter);
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
        });
        ObjectAnimator animator22 = ObjectAnimator.ofFloat(mergeDataImportfiles, "scaleY", 0.4f, 1.1f, 1f);
        animator22.setStartDelay(200);
        animator22.setDuration(500);
        animator22.setInterpolator(interpolator);
        ObjectAnimator animator222 = ObjectAnimator.ofFloat(mergeDataImportfiles, "scaleX", 0.4f, 1.1f, 1f);
        animator222.setDuration(500);
        animator222.setStartDelay(200);
        animator222.setInterpolator(interpolator);
        AnimatorSet animationSet = new AnimatorSet();
        animationSet.playTogether(animator1, animator11, animator111, animator2, animator22, animator222);
//        animationSet.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                super.onAnimationEnd(animation);
//                mergeDataImportrecord.setVisibility(View.VISIBLE);
//                mergeDataImportfiles.setVisibility(View.VISIBLE);
//            }
//        });
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
        String title = flag ? "正在合并" + format.format(mergeMonth) + "数据" : "没有合并数据╭(╯^╰)╮";
        String s = flag ? "重新合并" : "开始合并";
        dateTextView.setText(title);
        mergeDataBegin.setText(s);
        if (flag) {
            if(isCreate){
                mergeDataImportrecord.setVisibility(View.VISIBLE);
                mergeDataImportfiles.setVisibility(View.VISIBLE);
            }else {
                playAnimation();
            }
        } else {
            mergeDataImportrecord.setVisibility(View.GONE);
            mergeDataImportfiles.setVisibility(View.GONE);
        }
        isCreate = false;
    }

    private void showSelectMonthDialog() {
        SelectMonthDialogFragment fragment = new SelectMonthDialogFragment();
        fragment.setOnSelectDateRangeDismiss(new OnSelectDateRangeDismiss() {
            @Override
            public void onDissmiss(boolean isConfirm, long... timeInMillis) {
                if (isConfirm) {
                    mergeMonth = timeInMillis[0];
                    hasData = true;
                    changeButtonStatus(hasData);
                }
            }
        });
        fragment.show(getFragmentManager(), "selectmonth");
    }

    private void reMergeData() {
        dialogFactory.showDefaultConfirmDialog("是否清除列表内数据并重新合并数据？", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SumTotalOperator.deleAll();
                ImportFileOperator.deleAll();
                updateList();
                hasData = false;
                changeButtonStatus(hasData);
                showSelectMonthDialog();
            }
        });
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
            dialogFactory.showDefaultConfirmDialog("是否清理所有数据文件？", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!FileTools.isMicroMsgPathExist()) {
                        toast.showCenterToast("未找到数据文件目录");
                    } else {
                        FileTools.delMergeFiles();
                        toast.showCenterToast("数据文件已清理干净");
                    }
                }
            });
        } else {
            toast.showCenterToast("外置存储卡未准备好，请稍后重试！");
        }
    }

    // 清空接收到的文件
    public void clearReceivedFiles(View view) {
        new PermissionHelper(activity, getContext(), new PermissionHelper.OnResult() {
            @Override
            public void hasPermission() {
                clear();
            }
        }).request(Permission.Group.STORAGE);
    }

//    private boolean isMicroMsgDirExist() {
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//            miroMsgPath = new File(Environment.getExternalStorageDirectory() + "/tencent/MicroMsg/download/");
//            if (!miroMsgPath.exists()) {
//                miroMsgPath = new File("/tencent/MicroMsg/download/");
//            }
//        }
//        return miroMsgPath.exists();
//    }

//    private String[] getFileList() {
//        String[] result = null;
//        if (!isMicroMsgDirExist()) {
//            dialogAndToast.showCenterToast("未找到微信下载目录，请确认是否已安装微信");
//        } else {
//            File[] files = path.listFiles();
//            if (files == null || files.length == 0) {
//                dialogAndToast.showCenterToast("微信下载目录内未找到文件");
//            } else {
//                List<String> temp = new ArrayList<>();
//                for (File file : files) {
//                    if (file.isFile()) {
//                        String name = file.getName();
//                        if (name.endsWith(".data")) {
//                            temp.add(name);
//                        }
//                    }
//                }
//                if (temp.size() > 0)
//                    result = temp.toArray(new String[0]);
//                else
//                    dialogAndToast.showCenterToast("未找到数据文件，请在微信聊天界面点击接收到的文件进行下载或通知人员重新发送。");
//            }
//        }
//        return result;
//    }

    private void importFile(final String[] fileList) {
        if (fileList == null || fileList.length == 0) {
            toast.showCenterToast(FileTools.MICROMSG_DIR + " 目录内没有找到数据文件");
            return;
        }
        dialogFactory.showMultiSelect(fileList, new SelectDialogClicker() {
            @Override
            public void click(boolean[] checkedItems) {
                List<File> list = new ArrayList<>();
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        String name = fileList[i];
                        list.add(new File(FileTools.MICROMSG_DIR, name));
                    }
                }
                if (list.size() > 0) {
                    dataMerger = new DataMerger(getContext(), list, mergeMonth);
                    dataMerger.setOnFinished(new IDialogDismiss() {
                        @Override
                        public void onDismiss(boolean isConfirmed, Object... values) {
                            dataWatingDialog.changeInformation("");
                            dataWatingDialog.changeFile("");
                            dataWatingDialog.dismiss();
                            dialogFactory.showInfoDialog((String) values[0]);
                            updateList();
                        }
                    });
                    dataWatingDialog.show(getFragmentManager(), "loading");
                    dataMerger.execute();
                } else
                    toast.showCenterToast("未选择数据文件");
            }
        });
    }

    //导入数据
    public void importFileClick() {
        new PermissionHelper(activity, getContext(), new PermissionHelper.OnResult() {
            @Override
            public void hasPermission() {
                importFile(FileTools.getMergeFileList());
            }
        }).request(Permission.Group.STORAGE);
    }

    private void importThisData() {
        SelectDateRangeDialogFragment dateRangeDialogFragment = new SelectDateRangeDialogFragment();
        dateRangeDialogFragment.setOnSelectDateRangeDismiss(new OnSelectDateRangeDismiss() {
            @Override
            public void onDissmiss(boolean isConfirm, long... timeInMillis) {
                if (isConfirm) {
                    long start = timeInMillis[0];
                    long end = timeInMillis[1];
                    List<SumTotalRecord> temp = SumTotalOperator.getSumData(start, end);
                    if (temp == null || temp.size() == 0) {
                        toast.showCenterToast("该时间段内没有数据，请重新选择！");
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
                    toast.showCenterToast("导入完成！");
                }
                updateList();
            }
        });
        dateRangeDialogFragment.show(getFragmentManager(), "selectDateRange");
    }

    private void importThisDataClick(View view) {
        final ImportedFile localData = ImportFileOperator.findSingleByPhoneId(phoneId);
        if (localData != null) {
            dialogFactory.showDefaultConfirmDialog("已导入本机数据，是否重新导入？",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ImportFileOperator.deleAll(phoneId);
                            SumTotalOperator.deleAll(phoneId);
                            importThisData();
                        }
                    });
        } else {
            importThisData();
        }
    }

    private void share() {
//        if (!Tools.isAppInstall(getContext(), "com.tencent.mm")) {
//            Toast.makeText(getContext(), "您没有安装微信，不能执行该操作！", Toast.LENGTH_SHORT).show();
//            return;
//        }
        OtherTools.checkFileUriExposure();
        ShareTotalDataDialogFragment fragment = new ShareTotalDataDialogFragment();
        fragment.setOnDismissListener(new IDialogDismiss() {
            @Override
            public void onDismiss(boolean isConfirmed, Object... values) {
                if (isConfirmed) {
                    Uri fileUri = null;
                    String pwd = OtherTools.getTotalDataFilePWD((String) values[0]);
                    File file = getShareFile(pwd);
                    if (Build.VERSION.SDK_INT >= 24) {
                        fileUri = FileProvider.getUriForFile(getContext(), "th.yzw.specialrecorder.fileprovider", file);
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
        });
        fragment.show(getFragmentManager(), "share");
    }

    private void shareData() {
        if (list.isEmpty()) {
            toast.showCenterToast("列表内没有数据！\n请先合并数据，全部完成后再发送。");
            return;
        }
        new PermissionHelper(activity, getContext(), new PermissionHelper.OnResult() {
            @Override
            public void hasPermission() {
                share();
            }
        }).request(Permission.Group.STORAGE);

//        if (!XXPermissions.isHasPermission(getContext(), Permission.Group.STORAGE)) {
//            XXPermissions.with(getActivity())
//                    .permission(Permission.Group.STORAGE)
//                    .request(new OnPermission() {
//                        @Override
//                        public void hasPermission(List<String> granted, boolean isAll) {
//                            if (isAll)
//                                share();
//                        }
//
//                        @Override
//                        public void noPermission(List<String> denied, boolean quick) {
//                            if (quick) {
//                                showToast("您已永久拒绝使用存储权限，请手动开启！");
//                                XXPermissions.gotoPermissionSettings(getContext());
//                            } else {
//                                showToast("您已拒绝使用存储权限，不能使用该功能！");
//                            }
//                        }
//                    });
//        } else {
//            share();
//        }
    }

    //生成加密的分享文件
    File getShareFile(String pwd) {
        FileTools.clearFiles(getContext().getCacheDir());
        String fileName = format1.format(mergeMonth) + ".total";
        if (FileTools.isMicroMsgPathExist()) {
            File microMsgFile = new File(FileTools.MICROMSG_DIR, fileName);
            if (microMsgFile.exists())
                microMsgFile.delete();
        }
        File file = new File(getContext().getCacheDir(), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
            StringBuilder builder = new StringBuilder();
            List<ShowDataEntity> sharedList = ShowDataOperator.getShareList(list,fileName);
            String jsonString = new ShowDataJSONHelper().toJSONArray(sharedList).toString();
            builder.append(pwd).append(jsonString);
            FileTools.writeDecryptFile(builder.toString(), file);
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            dialogFactory.showInfoDialog("写入文件出错！原因为：" + e.getMessage());
            return null;
        } catch (JSONException ex) {
            ex.printStackTrace();
            dialogFactory.showInfoDialog("生成文件出错！原因为：" + ex.getMessage());
            return null;
        }
    }
}

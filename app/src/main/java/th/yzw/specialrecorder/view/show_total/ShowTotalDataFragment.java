package th.yzw.specialrecorder.view.show_total;

import android.app.AlertDialog;
import android.app.Dialog;
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
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.hjq.permissions.Permission;
import com.marquee.dingrui.marqueeviewlib.MarqueeView;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import th.yzw.specialrecorder.DAO.DataBackupAndRestore;
import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.RecordEntityOperator;
import th.yzw.specialrecorder.DAO.SumTotalOperator;
import th.yzw.specialrecorder.JSON.SumTotalJSONHelper;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;
import th.yzw.specialrecorder.model.SumTotalRecord;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.MyDateUtils;
import th.yzw.specialrecorder.tools.PermissionHelper;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.view.RecorderActivity;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.DateRangePopWindow;
import th.yzw.specialrecorder.view.common.DialogFactory;
import th.yzw.specialrecorder.view.common.InfoPopWindow;
import th.yzw.specialrecorder.view.common.LoadingDialog;
import th.yzw.specialrecorder.view.common.ToastFactory;

public class ShowTotalDataFragment extends Fragment {
    protected class ShowTotalAdapter extends RecyclerView.Adapter<ShowTotalAdapter.ViewHolder> {
        void updateList(long start, long end) {
            recordEntityList.clear();
            List<SumTotalRecord> temp = SumTotalOperator.getSumData(start, end);
            for (SumTotalRecord record : temp) {
                record.setPhoneId(phoneId);
                record.setMonth(start + 28 * MyDateUtils.ONE_DAY_MILLIS - 1);
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
            View view;

            ViewHolder(View itemView) {
                super(itemView);
                view = itemView;
                name = itemView.findViewById(R.id.show_item_name);
                count = itemView.findViewById((R.id.show_item_count));
            }
        }
    }

    //    private DialogFactory dialogFactory;
    private ConfirmPopWindow confirmPopWindow;
    private InfoPopWindow infoPopWindow;
    //    private ToastFactory toastFactory;
    private RecorderActivity activity;
    private List<SumTotalRecord> recordEntityList;
    private TextView showTotalNodata;
    private MarqueeView marqueeview;
    private AppCompatTextView changeDate;
    private RecyclerView showTotalFragmentRecycler;
    private long start, end;
    private Calendar calendar;
    private SimpleDateFormat format;
    private ShowTotalAdapter adapter;
    private File path, cacheDir;
    private String phoneId;

    private void selectDateRange() {
        new DateRangePopWindow(activity).show(changeDate, new OnSelectDateRangeDismiss() {
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
        marqueeview.setContent(format.format(start) + " 至 " + format.format(end));
        if (adapter.getItemCount() == 0) {
            showTotalNodata.setVisibility(View.VISIBLE);
        } else {
            showTotalNodata.setVisibility(View.GONE);
        }
    }

    private void backup() {
        final LoadingDialog loadingDialog = LoadingDialog.newInstant("备份数据", "准备中...", true);
        loadingDialog.setCancelClick(null);
        loadingDialog.setCancelable(false);
        DataBackupAndRestore dataBackuper = new DataBackupAndRestore(getContext(), "backup");
        dataBackuper.setOnFinish(new IDialogDismiss() {
            @Override
            public void onDismiss(boolean isConfirmed, Object... values) {
                loadingDialog.dismiss();
                if (isConfirmed) {
                    RecordEntityOperator.deleAllBetweenDate(start, end);
                    adapter.updateList(start, end);
                    infoPopWindow.show("备份后删除数据成功");
                } else {
                    String s = (String) values[0];
                    infoPopWindow.show(s);
                }
            }
        });
        loadingDialog.show(getFragmentManager(), "loading");
        dataBackuper.execute();
    }

    private void showConfirmDeleDialog() {
        new ConfirmPopWindow(activity)
                .setThirdButton("立即备份", new MyClickListener() {
                    @Override
                    public void OnClick(View view, Object o) {
                        backup();
                    }
                })
                .show("确定删除吗？\n（注意：如果没有备份数据，删除数据后将不能恢复！建议立即备份数据，再执行删除操作。）",
                        new IDialogDismiss() {
                            @Override
                            public void onDismiss(boolean isConfirmed, Object... value) {
                                if (isConfirmed) {
                                    RecordEntityOperator.deleAllBetweenDate(start, end);
                                    adapter.updateList(start, end);
                                    infoPopWindow.show("删除成功");
                                }
                            }
                        });
    }

    private void dele() {
        if (adapter.getItemCount() == 0) {
            infoPopWindow.show("当前列表内没有数据");
            return;
        }
        confirmPopWindow.show("是否要删除【 " + format.format(start) + " 】至【 " + format.format(end) + " 】内的所有数据？",
                new IDialogDismiss() {
                    @Override
                    public void onDismiss(boolean isConfirmed, Object... value) {
                        if(isConfirmed)
                            showConfirmDeleDialog();
                    }
                });
    }

    private void initialView(View view) {
        showTotalNodata = view.findViewById(R.id.show_total_nodata);
        marqueeview = view.findViewById(R.id.marqueeview);
        changeDate = view.findViewById(R.id.changeDate);
        changeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDateRange();
            }
        });
        showTotalFragmentRecycler = view.findViewById(R.id.show_total_fragment_recycler);
        showTotalFragmentRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        showTotalFragmentRecycler.setAdapter(adapter);
        showTotalFragmentRecycler.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (RecorderActivity) getActivity();
        activity.setTitle("汇总记录");
        setHasOptionsMenu(true);
        confirmPopWindow = new ConfirmPopWindow(activity);
        infoPopWindow = new InfoPopWindow(activity);
//        dialogFactory = new DialogFactory(getContext());
//        toastFactory = new ToastFactory(getContext());
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
        cacheDir = activity.getCacheDir();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.show_total_data_fragment_layout2, container, false);
        initialView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        showInfo();
    }

    // 分享数据
    private void share() {
        OtherTools.checkFileUriExposure();
        Uri fileUri = null;
        File file = getShareFile();
        if (file == null) {
            infoPopWindow.show("获取文件失败！");
            return;
        }
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

    private void shareData() {
        if (adapter.getItemCount() == 0) {
            infoPopWindow.show("该时间段内没有汇总数据！");
            return;
        }
        new PermissionHelper(activity, getContext(), new PermissionHelper.OnResult() {
            @Override
            public void hasPermission() {
                share();
            }
        }).request(Permission.Group.STORAGE);
    }

    private void clearSameFile(String fileName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            path = new File(Environment.getExternalStorageDirectory() + "/tencent/MicroMsg/download/");
            if (!path.exists()) {
                path = new File("/tencent/MicroMsg/download/");
            }
        }
        if (path.exists()) {
            File file = new File(path, fileName);
            if (file.exists())
                file.delete();
        } else {
            path.mkdirs();
        }
    }

    //生成加密的分享文件
    private File getShareFile() {
        FileTools.clearFiles(cacheDir);
        long currentTime = System.currentTimeMillis();
        String randomText = OtherTools.getRandomString(4) + "_" + new SimpleDateFormat("yyMMddHHmmss", Locale.CHINA).format(currentTime);
        String fileName = "SendBy" + randomText + ".data";
        clearSameFile(fileName);
        try {
            File file = new File(cacheDir, fileName);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            String s = new SumTotalJSONHelper().getSharedJSON(recordEntityList);
            FileTools.writeDecryptFile(s, file);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.showtotal_toolbar_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share_data:
                shareData();
                break;
            case R.id.dele_data:
                dele();
                break;
        }
        return true;
    }
}

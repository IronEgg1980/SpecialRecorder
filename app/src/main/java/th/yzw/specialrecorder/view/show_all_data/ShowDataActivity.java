package th.yzw.specialrecorder.view.show_all_data;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import th.yzw.specialrecorder.ActivityManager;
import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.DownloadFileOperator;
import th.yzw.specialrecorder.DAO.ShowDataOperator;
import th.yzw.specialrecorder.JSON.ShowDataJSONHelper;
import th.yzw.specialrecorder.MyActivity;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.model.DownLoadFile;
import th.yzw.specialrecorder.model.ShowDataEntity;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.EditPopWindow;
import th.yzw.specialrecorder.view.common.EnterPWDPopWindow;
import th.yzw.specialrecorder.view.common.InfoPopWindow;
import th.yzw.specialrecorder.view.common.SelectItemPopWindow;
import th.yzw.specialrecorder.view.common.ToastFactory;
import th.yzw.specialrecorder.view.common.WaitingDialog;
import th.yzw.specialrecorder.view.service.DownloadMergeFileSVC;

public class ShowDataActivity extends MyActivity {
    String TAG = "殷宗旺";

    private String[] pathList;
    private RecyclerView recyclerView;
    private TextView fileNameTV, noDataTV;
    private ImageButton showMenuIB;
    private String mFileName;
    private long firstTouchTime = 0;
    private List<ShowDataEntity> list;
    private ShowDataAdapter adapter;
    private boolean isEditMode;
    private FrameLayout frameLayout;
    private Button exitEditModeBT;
    private int preIndex;
    private File path = null;
    private InfoPopWindow infoPopWindow;
    private EditPopWindow editPopWindow;
    private ShowDataJSONHelper jsonHelper;
    private int currentIndex = -1;
    private BroadcastReceiver receiver;
    private WaitingDialog waitingDialog;

    private void initialReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (waitingDialog != null && waitingDialog.isVisible())
                    waitingDialog.dismiss();
                String action = intent.getAction();
                if (Broadcasts.EMAIL_RECEIVE_FAIL.equals(action)) {
                    infoPopWindow.show("文件同步失败，请检查网络...\n（说明：需要开启网络访问邮件服务器以同步数据文件，每次同步大约需要使用0.1-0.3M流量，请放心使用。）");
                }
            }
        };
        Broadcasts.bindBroadcast(this, receiver,Broadcasts.EMAIL_RECEIVE_FAIL,Broadcasts.EMAIL_RECEIVE_SUCCESS);
    }

    private void downloadFile(){
        waitingDialog.show(getSupportFragmentManager(), "loading");
        startService(new Intent(ShowDataActivity.this, DownloadMergeFileSVC.class));
    }

    private void setFileName(String _fileName) {
        mFileName = _fileName;
        AppSetupOperator.setTotalFileName(_fileName);
    }

    private void openFile(String _fileName, String pwd) {
        try {
            path = new File(FileTools.totalFileDownloadDir);
            if (!path.exists()) {
                return;
            }
            File file = new File(path, _fileName);
            String s = FileTools.readEncryptFile(file);
            String _pwd = s.substring(0, 20);
            String realPWD = OtherTools.getTotalDataFilePWD(pwd);
            if (realPWD.equals(_pwd)) {
                s = s.substring(20);
                list.addAll(jsonHelper.parseList(s));
                ShowDataOperator.saveAll(list);
                setFileName(_fileName);
            } else {
                infoPopWindow.show("密码错误！");
                setFileName("none");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            infoPopWindow.show("解析数据失败！");
            setFileName("none");
        } catch (IOException ex) {
            ex.printStackTrace();
            infoPopWindow.show("读取文件失败！");
            setFileName("none");
        } finally {
            showInfo();
        }
    }

    private void readFile(final String _fileName) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            ShowDataOperator.deleAll(_fileName);
            if (list == null)
                list = new ArrayList<>();
            list.clear();
            new EnterPWDPopWindow(ShowDataActivity.this, "验证密码", "请输入密码")
                    .setIcon(getDrawable(R.drawable.ic_lock_24dp))
                    .setDialogDismiss(new IDialogDismiss() {
                        @Override
                        public void onDismiss(Result result, Object... values) {
                            if (result == Result.OK) {
                                String pwd = (String) values[0];
                                openFile(_fileName, pwd);
                            } else {
                                setFileName("none");
                                showInfo();
                            }
                        }
                    }).show();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSION_GROUP_STORAGE, 2004);
        }
    }

    private void clear() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            new ConfirmPopWindow(this)
                    .setDialogDismiss(new IDialogDismiss() {
                        @Override
                        public void onDismiss(Result result, Object... values) {
                            if (result == Result.OK) {
                                path = new File(FileTools.totalFileDownloadDir);
                                if (!path.exists()) {
                                    infoPopWindow.show("未找到文件目录");
                                    return;
                                }
                                File file = new File(path, mFileName);
                                if (file.delete()) {
                                    ShowDataOperator.deleAll(mFileName);
                                    DownloadFileOperator.deleOne(mFileName);
                                }
                                if (list == null)
                                    list = new ArrayList<>();
                                list.clear();
                                setFileName("none");
                                showInfo();
                                new ToastFactory(ShowDataActivity.this).showCenterToast("已删除文件");
                            }
                        }
                    }).toConfirm("是否删除数据文件【 " + mFileName + " 】？");
        } else {
            ActivityCompat.requestPermissions(this, PERMISSION_GROUP_STORAGE, 2003);
        }
    }

    private void updateList(String _fileName) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.clear();
        if (!"none".equals(_fileName)) {
            list.addAll(ShowDataOperator.findAll(_fileName));
        }
        setFileName(_fileName);
    }

    private void showInfo() {
        if ("none".equals(mFileName))
            fileNameTV.setText("");
        else
            fileNameTV.setText(mFileName);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (list.size() > 0) {
            noDataTV.setVisibility(View.GONE);
            recyclerView.smoothScrollToPosition(0);
        } else {
            noDataTV.setVisibility(View.VISIBLE);
        }
    }

    private void closeFile() {
        list.clear();
        setFileName("none");
        showInfo();
    }

    private void changeEditMode(boolean flag) {
        isEditMode = flag;
        if (flag) {
            new ToastFactory(this).showCenterToast("进入编辑模式，点击项目可以修改数据");
            fileNameTV.setText("编辑模式(点击需要修改的项目)");
            frameLayout.setBackground(new ColorDrawable(Color.RED));
            exitEditModeBT.setVisibility(View.VISIBLE);
//            fileNameTV.setVisibility(View.GONE);
            showMenuIB.setVisibility(View.GONE);
            if (preIndex >= 0) {
                ShowDataEntity entity = list.get(preIndex);
                entity.setSelected(false);
                adapter.notifyItemChanged(preIndex);
                preIndex = -1;
            }
        } else {
            new ToastFactory(this).showCenterToast("退出编辑模式");
            fileNameTV.setText(mFileName);
            frameLayout.setBackground(new ColorDrawable(Color.TRANSPARENT));
            exitEditModeBT.setVisibility(View.GONE);
//            fileNameTV.setVisibility(View.VISIBLE);
            showMenuIB.setVisibility(View.VISIBLE);
        }
    }

    private void showMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(ShowDataActivity.this, view);
        Menu menu = popupMenu.getMenu();
        menu.clear();

        menu.add(0, 2, 6, "清除所有数据");
        if (list.size() > 0) {
            menu.add(0, 0, 1, "切换文件");
            menu.add(0, 3, 2, "修改数据");
            menu.add(0, 5, 3, "关闭文件");
//            menu.add(0, 1, 4, "删除文件");
            menu.add(0, 4, 5, "重新加载数据");
        } else {
            menu.add(0, 0, 1, "打开文件");
            menu.add(0,6,2,"同步数据");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case 0:
                        selectFile();
                        break;
//                    case 1:
//                        clear();
//                        showInfo();
//                        break;
                    case 2:
                        clearFiles();
                        break;
                    case 3:
                        changeEditMode(true);
                        break;
                    case 4:
                        new ConfirmPopWindow(ShowDataActivity.this)
                                .setDialogDismiss(new IDialogDismiss() {
                                    @Override
                                    public void onDismiss(Result result, Object... values) {
                                        if (result == Result.OK) {
                                            readFile(mFileName);
                                        }
                                    }
                                }).toConfirm("该操作会删除所有对【 " + mFileName + " 】数据文件的修改，将数据恢复至原始状态。是否继续？");

                        break;
                    case 5:
                        closeFile();
                        break;
                    case 6:
                        downloadFile();
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void clearFiles() {
        new ConfirmPopWindow(this)
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            delFiles();
                            showInfo();
                        }
                    }
                }).toConfirm("即将删除所有汇总数据与汇总文件，是否继续？");
    }

    private void delFiles() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            File path = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                path = new File(FileTools.totalFileDownloadDir);
                if (!path.exists()) {
                    infoPopWindow.show("未找到文件目录");
                    return;
                }
            }
            FileTools.clearFiles(path);
            ShowDataOperator.deleAll();
            DownloadFileOperator.deleAllTotalFile();
            if (list == null)
                list = new ArrayList<>();
            list.clear();
            setFileName("none");
            new ToastFactory(ShowDataActivity.this).showCenterToast("已清除所有文件");
        } else {
            ActivityCompat.requestPermissions(this, PERMISSION_GROUP_STORAGE, 2001);
        }
    }

    private void showSelectItem(int position) {
        ShowDataEntity entity1 = list.get(position);
        boolean b = entity1.isSelected();
        entity1.setSelected(!b);
        adapter.notifyItemChanged(position);
        if (preIndex >= 0 && preIndex != position) {
            ShowDataEntity entity = list.get(preIndex);
            entity.setSelected(false);
            adapter.notifyItemChanged(preIndex);
        }
        preIndex = position;
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - firstTouchTime) < 2000) {
            ActivityManager.closeAll();
        } else {
            firstTouchTime = System.currentTimeMillis();
            new ToastFactory(this).showCenterToast("再按一次退出");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_show_data);
        ActivityManager.add(this);
        isEditMode = false;
        preIndex = -1;
        recyclerView = findViewById(R.id.recyclerview);
        fileNameTV = findViewById(R.id.file_name_tv);
        noDataTV = findViewById(R.id.nodata_tv);
        showMenuIB = findViewById(R.id.select_file_bt);
        mFileName = AppSetupOperator.getTotalFileName();
        list = new ArrayList<>();
        adapter = new ShowDataAdapter(list, ShowDataActivity.this);
        adapter.setClickListener(new ShowDataAdapter.itemClickListener() {
            @Override
            public void click(int position) {
                if (isEditMode) {
                    currentIndex = position;
                    editData(position);
                } else
                    showSelectItem(position);
            }
        });
        recyclerView = findViewById(R.id.recyclerview);
        frameLayout = findViewById(R.id.framelayout);
        exitEditModeBT = findViewById(R.id.exit_editmode_bt);
        exitEditModeBT.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                changeEditMode(false);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ShowDataActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(ShowDataActivity.this, DividerItemDecoration.VERTICAL));
        showMenuIB.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                showMenu(showMenuIB);
            }
        });
        infoPopWindow = new InfoPopWindow(this);
        jsonHelper = new ShowDataJSONHelper();
        editPopWindow = new EditPopWindow(this, true);
        editPopWindow.setDialogDismiss(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if (result == Result.OK) {
                    int value = (int) values[0];
                    if (currentIndex != -1) {
                        ShowDataEntity r = list.get(currentIndex);
                        r.setCount(value);
                        r.save();
                        adapter.notifyItemChanged(currentIndex);
                        new ToastFactory(ShowDataActivity.this).showCenterToast("修改成功!");
                    }

                }
            }
        });
        waitingDialog = new WaitingDialog();
        initialReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateList(mFileName);
        showInfo();
        downloadFile();
    }

    protected void selectFile() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                path = new File(FileTools.totalFileDownloadDir);
                if (path.exists()) {
                    pathList = FileTools.getFileList(path, ".total");
                }
                if (pathList != null && pathList.length > 0) {
                    final SelectItemPopWindow selectItemPopWindow = new SelectItemPopWindow(ShowDataActivity.this, pathList, false);
                    selectItemPopWindow.show(new IDialogDismiss() {
                        @Override
                        public void onDismiss(Result result, Object... values) {
                            if (result == Result.OK) {
                                mFileName = pathList[(int) values[0]];
                                updateList(mFileName);
                                if (list.size() == 0) {
                                    readFile(mFileName);
                                    selectItemPopWindow.isResumeAlpha = false;
                                } else
                                    showInfo();
                            }
                        }
                    });
                } else {
                    infoPopWindow.show("未找到数据文件！");
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, PERMISSION_GROUP_STORAGE, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        Broadcasts.unBindBroadcast(this, receiver);
        super.onDestroy();
    }

    protected void editData(final int position) {
        ShowDataEntity r = list.get(position);
        editPopWindow.setData(r.getName(), r.getCount()).show();
    }

    @Override
    protected void onPermissionGranted(int requestCode) {
        switch (requestCode) {
            case 2000:
                selectFile();
                break;
            case 2001:
                delFiles();
                break;
//            case 2003:
//                clear();
//                break;
            default:
                new ToastFactory(this).showCenterToast("已获取存储权限。");
        }
    }

    @Override
    protected void onPermissionDenied(int requestCode) {
        new ConfirmPopWindow(this)
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            ActivityCompat.requestPermissions(ShowDataActivity.this, PERMISSION_GROUP_STORAGE, 2002);
                        }
                    }
                })
                .toConfirm("如需使用该功能，请授予使用存储权限。");
    }
}

package th.yzw.specialrecorder.view.show_all_data;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
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

import com.hjq.permissions.Permission;
import com.sun.mail.imap.protocol.ID;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import th.yzw.specialrecorder.ActivityManager;
import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.ShowDataOperator;
import th.yzw.specialrecorder.JSON.ShowDataJSONHelper;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.interfaces.SelectDialogClicker;
import th.yzw.specialrecorder.model.ShowDataEntity;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.tools.PermissionHelper;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.DialogFactory;
import th.yzw.specialrecorder.unuse.EditDataDialogFragment;
import th.yzw.specialrecorder.view.common.EditPopWindow;
import th.yzw.specialrecorder.view.common.EnterPWDPopWindow;
import th.yzw.specialrecorder.view.common.InfoPopWindow;
import th.yzw.specialrecorder.view.common.SelectItemPopWindow;
import th.yzw.specialrecorder.view.common.ToastFactory;

public class ShowDataActivity extends AppCompatActivity {
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
//    private DialogFactory dialogFactory;
//    private ToastFactory toastFactory;
    private ShowDataJSONHelper jsonHelper;

    private void setFileName(String _fileName) {
        mFileName = _fileName;
        AppSetupOperator.setTotalFileName(_fileName);
    }

    private void openFile(String _fileName, String pwd) {
        try {
            path = new File(FileTools.MICROMSG_DIR);
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
        new PermissionHelper(ShowDataActivity.this, ShowDataActivity.this, new PermissionHelper.OnResult() {
            @Override
            public void hasPermission() {
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

//                ShareTotalDataDialogFragment fragment = ShareTotalDataDialogFragment.getInstance("需要密码", "请输入密码：");
//                fragment.setOnDismissListener(new IDialogDismiss() {
//                    @Override
//                    public void onDismiss(Result result, Object... values) {
//                        if (result == Result.OK) {
//                            String pwd = (String) values[0];
//                            openFile(_fileName, pwd);
//                        } else {
//                            setFileName("none");
//                            showInfo();
//                        }
//                    }
//                });
//                fragment.show(getSupportFragmentManager(), "inputpwd");
//                final EditText editText = new EditText(ShowDataActivity.this);
//                AlertDialog.Builder builder = new AlertDialog.Builder(ShowDataActivity.this);
//                builder.setTitle("需要密码")
//                        .setMessage("请输入密码")
//                        .setIcon(R.drawable.ic_info_cyan_800_18dp)
//                        .setView(editText)
//                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                setFileName("none");
//                                showInfo();
//                            }
//                        })
//                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                InputMethodManager inputMethodManager = (InputMethodManager) editText.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
//                                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                                String pwd = "";
//                                if (!TextUtils.isEmpty(editText.getText())) {
//                                    pwd = editText.getText().toString().trim();
//                                    openFile(_fileName, pwd);
//                                } else {
//                                    dialogAndToast.showCenterToast("请输入密码！");
//                                    setFileName("none");
//                                    showInfo();
//                                }
//                            }
//                        });
//                Dialog dialog = builder.create();
//                dialog.setCanceledOnTouchOutside(false);
//                dialog.show();
//                Window window = dialog.getWindow();
//                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//                editText.requestFocus();
            }
        }).request(Permission.Group.STORAGE);
    }

    private void clear() {
        new ConfirmPopWindow(this)
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if(result == Result.OK) {
                            new PermissionHelper(ShowDataActivity.this, ShowDataActivity.this, new PermissionHelper.OnResult() {
                                @Override
                                public void hasPermission() {
                                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                                        path = new File(FileTools.MICROMSG_DIR);
                                        if (!path.exists()) {
                                            infoPopWindow.show("未找到文件目录");
                                            return;
                                        }
                                        File file = new File(path, mFileName);
                                        file.delete();
                                        ShowDataOperator.deleAll(mFileName);
                                        if (list == null)
                                            list = new ArrayList<>();
                                        list.clear();
                                        setFileName("none");
                                        showInfo();
                                        infoPopWindow.show("已删除文件");
                                    }
                                }
                            }).request(Permission.Group.STORAGE);
                        }
                    }
                }).toConfirm("是否删除数据文件【 " + mFileName + " 】？");

//        dialogFactory.showDefaultConfirmDialog("是否删除数据文件【 " + mFileName + " 】？", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                new PermissionHelper(ShowDataActivity.this, ShowDataActivity.this, new PermissionHelper.OnResult() {
//                    @Override
//                    public void hasPermission() {
//                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                            path = new File(FileTools.MICROMSG_DIR);
//                            if (!path.exists()) {
//                                toastFactory.showCenterToast("未找到文件目录");
//                                return;
//                            }
//                            File file = new File(path, mFileName);
//                            file.delete();
//                            ShowDataOperator.deleAll(mFileName);
//                            if (list == null)
//                                list = new ArrayList<>();
//                            list.clear();
//                            setFileName("none");
//                            showInfo();
//                            toastFactory.showCenterToast("已删除文件");
//                        }
//                    }
//                }).request(Permission.Group.STORAGE);
//            }
//        });
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
            infoPopWindow.show("进入编辑模式，点击项目可以修改数据");
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
            infoPopWindow.show("退出编辑模式");
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
            menu.add(0, 1, 4, "删除文件");
            menu.add(0, 4, 5, "重新加载数据");
        } else {
            menu.add(0, 0, 1, "打开文件");
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case 0:
                        selectFile();
                        break;
                    case 1:
                        clear();
                        showInfo();
                        break;
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
                                    if(result == Result.OK){
                                        readFile(mFileName);
                                    }
                                }
                            }).toConfirm("该操作会删除所有对【 " + mFileName + " 】数据文件的修改，将数据恢复至原始状态。是否继续？");
//                        dialogFactory.showDefaultConfirmDialog("该操作会删除所有对【 " + mFileName + " 】数据文件的修改，将数据恢复至原始状态。是否继续？",
//                                new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        readFile(mFileName);
//                                    }
//                                });
                        break;
                    case 5:
                        closeFile();
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
                        if(result == Result.OK){
                            delFiles();
                            showInfo();
                        }
                    }
                }).toConfirm("即将删除所有汇总数据与汇总文件，是否继续？");
    }

    private void delFiles() {
        new PermissionHelper(ShowDataActivity.this, ShowDataActivity.this, new PermissionHelper.OnResult() {
            @Override
            public void hasPermission() {
                File path = null;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    path = new File(FileTools.MICROMSG_DIR);
                    if (!path.exists()) {
                        infoPopWindow.show("未找到文件目录");
                        return;
                    }
                }
                FileTools.deleAllFiles(path, ".total");
                ShowDataOperator.deleAll();
                if (list == null)
                    list = new ArrayList<>();
                list.clear();
                setFileName("none");
                infoPopWindow.show("已清除所有文件");
            }
        }).request(Permission.Group.STORAGE);
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
                if (isEditMode)
                    editData(position);
                else
                    showSelectItem(position);
            }
        });
        recyclerView = findViewById(R.id.recyclerview);
        frameLayout = findViewById(R.id.framelayout);
        exitEditModeBT = findViewById(R.id.exit_editmode_bt);
        exitEditModeBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEditMode(false);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ShowDataActivity.this));
        recyclerView.addItemDecoration(new DividerItemDecoration(ShowDataActivity.this, DividerItemDecoration.VERTICAL));
        showMenuIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(showMenuIB);
            }
        });
        infoPopWindow = new InfoPopWindow(this);
//        dialogFactory = new DialogFactory(this);
//        toastFactory = new ToastFactory(this);
        jsonHelper = new ShowDataJSONHelper();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateList(mFileName);
        showInfo();
    }

    protected void selectFile() {
        new PermissionHelper(ShowDataActivity.this, ShowDataActivity.this, new PermissionHelper.OnResult() {
            @Override
            public void hasPermission() {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    path = new File(FileTools.MICROMSG_DIR);
                    if (path.exists()) {
                        pathList = FileTools.getFileList(path, ".total");
                    }
                    if (pathList != null && pathList.length > 0) {
                        final SelectItemPopWindow selectItemPopWindow = new SelectItemPopWindow(ShowDataActivity.this,pathList,false);
                        selectItemPopWindow.show(new IDialogDismiss() {
                            @Override
                            public void onDismiss(Result result, Object... values) {
                                if(result == Result.OK){
                                    mFileName = pathList[(int) values[0]];
                                    updateList(mFileName);
                                    if (list.size() == 0) {
                                        readFile(mFileName);
                                        selectItemPopWindow.isResumeAlpha = false;
                                    }else
                                        showInfo();
                                }
                            }
                        });
//                        dialogFactory.showSingleSelectWithConfirmButton(pathList, new SelectDialogClicker() {
//                            @Override
//                            public void click(int checkedItem) {
//                                mFileName = pathList[checkedItem];
//                                updateList(mFileName);
//                                if (list.size() == 0)
//                                    readFile(mFileName);
//                                else
//                                    showInfo();
//                            }
//                        });
                    } else {
                        infoPopWindow.show("未找到数据文件！");
                    }
                }
            }
        }
        ).request(Permission.Group.STORAGE);
    }

    protected void editData(final int position) {
        final ShowDataEntity r = list.get(position);
        EditPopWindow editPopWindow = new EditPopWindow(ShowDataActivity.this,r.getName(),r.getCount());
        editPopWindow.show(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if(result == Result.OK){
                    int value = (int) values[0];
                    r.setCount(value);
                    r.save();
                    adapter.notifyItemChanged(position);
                    new ToastFactory(ShowDataActivity.this).showCenterToast("修改成功!");
                }
            }
        });

//        EditDataDialogFragment fragment = EditDataDialogFragment.newInstant(r.getName(), r.getCount());
//        fragment.setOnDissmissListener(new IDialogDismiss() {
//            @Override
//            public void onDismiss(Result result, Object... values) {
//                if (result == Result.OK) {
//                    int value = (int) values[0];
//                    r.setCount(value);
//                    r.save();
//                    adapter.notifyItemChanged(position);
//                    new ToastFactory(ShowDataActivity.this).showCenterToast("修改成功!");
//                }
//            }
//        });
//        fragment.show(getSupportFragmentManager(), "edit");
    }
}

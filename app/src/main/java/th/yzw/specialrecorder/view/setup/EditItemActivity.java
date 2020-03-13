package th.yzw.specialrecorder.view.setup;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.ItemNameOperator;
import th.yzw.specialrecorder.DAO.MyDBHelper;
import th.yzw.specialrecorder.JSON.ItemNameJSONHelper;
import th.yzw.specialrecorder.MyActivity;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.model.ItemName;
import th.yzw.specialrecorder.tools.DataTool;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.tools.SendEmailHelper;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.InfoPopWindow;
import th.yzw.specialrecorder.view.common.MenuPopWindow;
import th.yzw.specialrecorder.view.merge_data.MergeDataActivity;

//import java.io.File;

public class EditItemActivity extends MyActivity {
    class EditItemSetupAdapter extends RecyclerView.Adapter<EditItemSetupAdapter.MyViewHolder> {
        class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView nameTextView, itemTypeTextView, itemFormalationTextView, isOftenUseTextView;
            public LinearLayout root;

            public MyViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.name_textView);
                root = itemView.findViewById(R.id.root);
                itemTypeTextView = itemView.findViewById(R.id.itemtypeTextView);
                itemFormalationTextView = itemView.findViewById(R.id.itemformalationTextView);
                isOftenUseTextView = itemView.findViewById(R.id.item_name_ifOftenUse);
            }
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(EditItemActivity.this).inflate(R.layout.edit_item_adapter_item, viewGroup, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder viewHolder, int i) {
            final int index = viewHolder.getAdapterPosition();
            ItemName itemName = mItemNameList.get(index);
            String type = "Type:" + DataTool.getItemTypeString(itemName.getItemType());
            String formalation = "Formalation:" + DataTool.getItemFomalationString(itemName.getFormalation());
            viewHolder.isOftenUseTextView.setVisibility(itemName.isOftenUse() ? View.VISIBLE : View.INVISIBLE);
            viewHolder.itemTypeTextView.setText(type);
            viewHolder.itemFormalationTextView.setText(formalation);
            viewHolder.nameTextView.setText(itemName.getName());
            viewHolder.root.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    new ConfirmPopWindow(EditItemActivity.this).setDialogDismiss(new IDialogDismiss() {
                        @Override
                        public void onDismiss(Result result, Object... values) {
                            if (result == Result.OK) {
                                mItemNameList.get(index).delete();
                                mItemNameList.remove(index);
                                notifyDataSetChanged();
                            }
                        }
                    }).toConfirm("是否删除【" + mItemNameList.get(index).getName() + "】？");
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItemNameList.size();
        }
    }

    private LinearLayoutManager manager;
    private TextView itemtypeTextView;
    private TextView itemformalationTextView;
    private EditItemSetupAdapter adapter;
    private EditText editText;
    private RecyclerView recyclerView;
    private byte itemType, itemFormalation;
    private String[] itemTypeList, itemFormalationList;
    private List<ItemName> mItemNameList;
    private InfoPopWindow infoPopWindow;

    private void initialView() {
        infoPopWindow = new InfoPopWindow(this);
        recyclerView = findViewById(R.id.item_setup_recyclerview);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(EditItemActivity.this, DividerItemDecoration.VERTICAL));
        editText = findViewById(R.id.item_setup_name_edittext);
        findViewById(R.id.confirm).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                addItemName(v);
            }
        });
        TextView title = findViewById(R.id.title);
        title.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                PopupMenu popupMenu = new PopupMenu(EditItemActivity.this, v);
                Menu menu = popupMenu.getMenu();
                menu.add(1, 1, 1, "发送数据文件");
                menu.add(1, 2, 2, "发送旧版数据文件");
                menu.add(1, 3, 3, "导出数据文件");
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case 1://发送数据文件
                                outputData(getShareFile(false));
                                break;
                            case 2://发送旧版数据文件
                                outputData(getShareFile(true));
                                break;
                            case 3://导出数据文件
                                exportFile();
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });
        itemtypeTextView = findViewById(R.id.itemtypeTextView);
        itemtypeTextView.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                showSelectDialog(1);
            }
        });
        itemformalationTextView = findViewById(R.id.itemformalationTextView);
        itemformalationTextView.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                showSelectDialog(2);
            }
        });
        TextView marqueeView = findViewById(R.id.marqueeview);
        marqueeView.setText("输入项目名称后点击添加按钮可以添加项目，点击某个项目可以删除。");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item_fragment_dialog);
        mItemNameList = ItemNameOperator.findAll();
        itemType = -1;
        itemFormalation = -1;
        itemTypeList = DataTool.getItemTypeAll();
        itemFormalationList = DataTool.getItemFomalationAll();
        adapter = new EditItemSetupAdapter();
        manager = new LinearLayoutManager(EditItemActivity.this);
        initialView();
        if (adapter.getItemCount() > 0)
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void showSelectDialog(final int mode) {
        if (mode == 1) {
            MenuPopWindow menuPopWindow = new MenuPopWindow(this, itemTypeList, null);
            menuPopWindow.setClickListener(new MyClickListener() {
                @Override
                public void OnClick(View view, Object o) {
                    int index = (int) o;
                    itemType = (byte) (index + 1);
                    itemtypeTextView.setText(itemTypeList[index]);
                    editText.setError(null);
                }
            });
            menuPopWindow.showAsDropDown(itemtypeTextView);
        } else {
            MenuPopWindow menuPopWindow = new MenuPopWindow(this, itemFormalationList, null);
            menuPopWindow.setClickListener(new MyClickListener() {
                @Override
                public void OnClick(View view, Object o) {
                    int index = (int) o;
                    itemFormalation = (byte) (index + 1);
                    itemformalationTextView.setText(itemFormalationList[index]);
                    editText.setError(null);
                }
            });
            menuPopWindow.showAsDropDown(itemformalationTextView);
        }
    }

    private void addItemName(View view) {
        if (TextUtils.isEmpty(editText.getText())) {
            editText.requestFocus();
            editText.setError("请输入名称");
            return;
        }
        String name = editText.getText().toString().trim();
        if ("".equals(name)) {
            editText.requestFocus();
            editText.setError("请输入名称");
            return;
        }
        if (ItemNameOperator.isExist(name)) {
            editText.requestFocus();
            editText.selectAll();
            editText.setError("名称重复");
            return;
        }
        if (itemType < 1 || itemFormalation < 1) {
            editText.setError("请选择类型和种类");
            return;
        }
        ItemName itemName = new ItemName();
        itemName.setName(name);
        itemName.setOftenUse(false);
        itemName.setDataMode(MyDBHelper.DATA_MODE_NEWDATA);
        itemName.setItemType(itemType);
        itemName.setFormalation(itemFormalation);
        itemName.save();
        mItemNameList.add(itemName);
        int index = adapter.getItemCount() - 1;
        adapter.notifyItemInserted(index);
        recyclerView.smoothScrollToPosition(index);
        editText.setText("");
        itemtypeTextView.setText("类型");
        itemformalationTextView.setText("种类");
        itemType = -1;
        itemFormalation = -1;
        editText.requestFocus();
    }

    private void sendEmail(final long currentVersion, final File file) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String title = "itemupdateversion_" + currentVersion;
                    String content = "ItemName升级文件，当前版本号：" + currentVersion;
                    new SendEmailHelper().sendMultiEmail(title, content, true, file);
                } catch (MessagingException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void exportFile() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            export();
        } else {
            ActivityCompat.requestPermissions(this, PERMISSION_GROUP_STORAGE, 111);
        }
    }

    private void export() {
        final int currentVersion = AppSetupOperator.getItemVersion() + 1;
        File path = new File(FileTools.ITEMNAME_EXPORT_DIR);
        if (!path.exists())
            path.mkdir();
        for (File file : path.listFiles()) {
            if (file.isFile())
                file.delete();
        }
        String fileName = "UpdateFileVersion_" + currentVersion + ".itemupdate";
        try {
            File file = new File(path, fileName);
            String s = new ItemNameJSONHelper().getUpdateFileJSONString(currentVersion);
            FileTools.writeDecryptFile(s, file);
            AppSetupOperator.setItemVersion(currentVersion);
            sendEmail(currentVersion, file);
            infoPopWindow.show("已导出文件至" + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            infoPopWindow.show("写入文件出错！原因为：" + e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
            infoPopWindow.show("解析数据失败！原因为：" + e.getMessage());
        }
    }

    private File getShareFile(boolean isOldData) {
        ItemNameJSONHelper helper = new ItemNameJSONHelper();
        if (mItemNameList != null && mItemNameList.size() > 0) {
            FileTools.clearFiles(getCacheDir());
            long currentTime = System.currentTimeMillis();
            String dateString = new SimpleDateFormat("yyyyMMdd", Locale.CHINA).format(currentTime);
            String fileName = "UpdateFile" + dateString + ".itemupdate";
            FileTools.clearSameFile(fileName);
            try {
                int currentVersion = AppSetupOperator.getItemVersion() + 1;
                File file = new File(getCacheDir(), fileName);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                String s = "";
                if (isOldData)
                    s = helper.toJSONArray(mItemNameList).toString();
                else
                    s = helper.getUpdateFileJSONString(currentVersion);
                FileTools.writeDecryptFile(s, file);
                AppSetupOperator.setItemVersion(currentVersion);
                return file;
            } catch (IOException e) {
                e.printStackTrace();
                infoPopWindow.show("写入文件出错！原因为：" + e.getMessage());
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                infoPopWindow.show("解析数据失败！原因为：" + e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    private void outputData(File file) {
        OtherTools.checkFileUriExposure();
        Uri fileUri = null;
        if (file == null) {
            infoPopWindow.show("获取文件失败！");
            return;
        }
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = FileProvider.getUriForFile(EditItemActivity.this, "th.yzw.specialrecorder.fileprovider", file);
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

    @Override
    protected void onPermissionGranted(int requestCode) {
        export();
    }

    @Override
    protected void onPermissionDenied(int requestCode) {
        new ConfirmPopWindow(this)
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            ActivityCompat.requestPermissions(EditItemActivity.this, PERMISSION_GROUP_STORAGE, 111);
                        }
                    }
                })
                .toConfirm("由于您拒绝授予读取存储权限，该功能无法使用！\n请点击【确定】授予权限。");
    }

}

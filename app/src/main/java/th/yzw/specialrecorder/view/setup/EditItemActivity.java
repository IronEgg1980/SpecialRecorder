package th.yzw.specialrecorder.view.setup;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.hjq.permissions.Permission;
import com.marquee.dingrui.marqueeviewlib.MarqueeView;

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
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.SelectDialogClicker;
import th.yzw.specialrecorder.model.ItemName;
import th.yzw.specialrecorder.tools.DataTool;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.tools.PermissionHelper;
import th.yzw.specialrecorder.tools.SendEmailHelper;
import th.yzw.specialrecorder.view.common.DialogFactory;
import th.yzw.specialrecorder.view.common.ToastFactory;

//import java.io.File;

public class EditItemActivity extends AppCompatActivity {
    class EditItemSetupAdapter extends RecyclerView.Adapter<EditItemSetupAdapter.ViewHolder> {
        class ViewHolder extends RecyclerView.ViewHolder {
            public TextView nameTextView, itemTypeTextView, itemFormalationTextView, isOftenUseTextView;
            public LinearLayout root;

            public ViewHolder(View itemView) {
                super(itemView);
                nameTextView = itemView.findViewById(R.id.name_textView);
                root = itemView.findViewById(R.id.root);
                itemTypeTextView = itemView.findViewById(R.id.itemtypeTextView);
                itemFormalationTextView = itemView.findViewById(R.id.itemformalationTextView);
                isOftenUseTextView = itemView.findViewById(R.id.item_name_ifOftenUse);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.edit_item_adapter_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int i) {
            ItemName itemName = mItemNameList.get(i);
            String type = "Type:" + DataTool.getItemTypeString(itemName.getItemType());
            String formalation = "Formalation:" + DataTool.getItemFomalationString(itemName.getFormalation());
            boolean isOftenUse = itemName.isOftenUse();
            if (isOftenUse)
                viewHolder.isOftenUseTextView.setBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
            else
                viewHolder.isOftenUseTextView.setBackground(new ColorDrawable(Color.TRANSPARENT));
            viewHolder.itemTypeTextView.setText(type);
            viewHolder.itemFormalationTextView.setText(formalation);
            viewHolder.nameTextView.setText(itemName.getName());
            viewHolder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogFactory.showDefaultConfirmDialog("是否删除【" + mItemNameList.get(i).getName() + "】？", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mItemNameList.get(i).delete();
                            mItemNameList.remove(i);
                            notifyDataSetChanged();
                        }
                    });
//
//                    new AlertDialog.Builder(v.getContext())
//                            .setIcon(R.drawable.ic_warning_24dp)
//                            .setTitle("删除")
//                            .setMessage("是否删除【" + mItemNameList.get(i).getName() + "】？")
//                            .setPositiveButton("删除", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                }
//                            })
//                            .setNegativeButton("取消", null).show();
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
    //    private AlertDialog.Builder builder;
    private String[] itemTypeList, itemFormalationList;
//    private File path;
    private List<ItemName> mItemNameList;
    private DialogFactory dialogFactory;
    private ToastFactory toastFactory;

    private void initialView() {
        dialogFactory = new DialogFactory(this);
        toastFactory = new ToastFactory(this);
        recyclerView = findViewById(R.id.item_setup_recyclerview);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(EditItemActivity.this, DividerItemDecoration.VERTICAL));
        editText = findViewById(R.id.item_setup_name_edittext);
        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItemName(v);
            }
        });
        TextView title = findViewById(R.id.title);
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        itemtypeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDialog(1);
            }
        });
        itemformalationTextView = findViewById(R.id.itemformalationTextView);
        itemformalationTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectDialog(2);
            }
        });
        MarqueeView marqueeView = findViewById(R.id.marqueeview);
        marqueeView.setContent("输入项目名称后点击添加按钮可以添加项目，点击某个项目可以删除。");
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
//        builder = new AlertDialog.Builder(EditItemActivity.this);
//        builder.setTitle("请选择")
//                .setIcon(R.drawable.ic_info_cyan_800_18dp)
//                .setNegativeButton("取消", null);
        adapter = new EditItemSetupAdapter();
        manager = new LinearLayoutManager(EditItemActivity.this);
        initialView();
        if (adapter.getItemCount() > 0)
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
//        editText.requestFocus();
    }

    private void showSelectDialog(final int mode) {
        if (mode == 1) {
            dialogFactory.showSingleSelect(itemTypeList, new SelectDialogClicker() {
                @Override
                public void click(int checkedItem) {
                    itemType = (byte) (checkedItem + 1);
                    itemtypeTextView.setText(itemTypeList[checkedItem]);
                    editText.setError(null);
                }
            });

//            builder.setSingleChoiceItems(itemTypeList, -1, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog1, int which) {
//                    itemType = (byte) (which + 1);
//                    itemtypeTextView.setText(itemTypeList[which]);
//                    dialog1.dismiss();
//                    editText.setError(null);
//                }
//            });
        } else {
            dialogFactory.showSingleSelect(itemFormalationList, new SelectDialogClicker() {
                @Override
                public void click(int checkedItem) {
                    itemFormalation = (byte) (checkedItem + 1);
                    itemformalationTextView.setText(itemFormalationList[checkedItem]);
                    editText.setError(null);
                }
            });
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

    private void sendEmail(final long currentVersion, final File file){
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
        if (mItemNameList != null && mItemNameList.size() > 0) {
            PermissionHelper helper = new PermissionHelper(this, this, new PermissionHelper.OnResult() {
                @Override
                public void hasPermission() {
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
                        sendEmail(currentVersion,file);
                        toastFactory.showCenterToast("已导出文件至" + file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                        dialogFactory.showInfoDialog("写入文件出错！原因为：" + e.getMessage());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialogFactory.showInfoDialog("解析数据失败！原因为：" + e.getMessage());
                    }
                }
            });
            helper.request(Permission.Group.STORAGE);
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
                dialogFactory.showInfoDialog("写入文件出错！原因为：" + e.getMessage());
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                dialogFactory.showInfoDialog("解析数据失败！原因为：" + e.getMessage());
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
            toastFactory.showCenterToast("获取文件失败！");
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

    //    private void myCodebackup(){
//        try {
//            File file = new File(getCacheDir(), fileName);
//            if (file.exists()) {
//                file.delete();
//            }
//            file.createNewFile();
//            JSONArray jsonArray = new JSONArray();
//            JSONObject versionOBJ = new JSONObject();
//            int currentVersion = MyAppSetupUtils.getItemVersion() +1;
//            versionOBJ.put("itemversion",currentVersion);
//            jsonArray.put(versionOBJ);
//            for (int i = 0; i < list.size(); i++) {
//                ItemName record = list.get(i);
//                JSONObject object = new JSONObject();
//                object.put("name", record.getName());
//                object.put("isOftenUse", false);
//                object.put("formalation", record.getFormalation());
//                object.put("itemType", record.getItemType());
//                jsonArray.put(object);
//            }
//            String s = jsonArray.toString();
//            Tools.writeFile(s, file);
//            MyAppSetupUtils.setItemVersion(currentVersion);
//            return file;
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast toast = Toast.makeText(EditItemActivity.this, "写入文件出错！原因为：" + e.getMessage(), Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 0, 0);
//            toast.show();
//            return null;
//        } catch (JSONException ex) {
//            ex.printStackTrace();
//            Toast toast = Toast.makeText(EditItemActivity.this, "生成文件出错！原因为：" + ex.getMessage(), Toast.LENGTH_LONG);
//            toast.setGravity(Gravity.CENTER, 0, 0);
//            toast.show();
//            return null;
//        }
//    }
}

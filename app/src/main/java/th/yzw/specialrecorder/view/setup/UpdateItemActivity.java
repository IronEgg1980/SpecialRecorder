package th.yzw.specialrecorder.view.setup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hjq.permissions.Permission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import th.yzw.specialrecorder.DAO.ItemUpdater;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.tools.PermissionHelper;
import th.yzw.specialrecorder.view.common.InfoPopWindow;
import th.yzw.specialrecorder.view.common.LoadingDialog;

public class UpdateItemActivity extends AppCompatActivity {

    private File updateFile = null;
    private LoadingDialog loadingDialog;
    private InfoPopWindow infoPopWindow;

    private void updateItem() {
        if (updateFile == null) {
            infoPopWindow.show("打开文件错误！");
        } else {
            ItemUpdater itemUpdater = new ItemUpdater(this, updateFile);
            itemUpdater.setOnFinished(new IDialogDismiss() {
                @Override
                public void onDismiss(Result result, Object... values) {
                    loadingDialog.dismiss();
                    String s = (String) values[0];
                    infoPopWindow.show(s);
                }
            });
            loadingDialog.show(getSupportFragmentManager(), "loading");
            itemUpdater.execute();
        }
    }

    private void readFile(Uri uri) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(uri);
            updateFile = new File(getCacheDir(), "updateItem.tmp");
            outputStream = new FileOutputStream(updateFile);
            int c;
            byte[] buffer = new byte[1024];
            while ((c = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, c);
            }
        } catch (IOException e) {
            updateFile = null;
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_item);
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            readFile(intent.getData());
        }
        loadingDialog = LoadingDialog.newInstant("正在更新", "正在查找文件...", true);
        loadingDialog.setCancelClick(null);
        loadingDialog.setCancelable(false);
        infoPopWindow = new InfoPopWindow(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionHelper helper = new PermissionHelper(UpdateItemActivity.this, UpdateItemActivity.this, new PermissionHelper.OnResult() {
            @Override
            public void hasPermission(boolean flag) {
                if(flag)
                    updateItem();
                else
                    finish();
            }
        });
        helper.request(Permission.Group.STORAGE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateFile = null;
    }
}

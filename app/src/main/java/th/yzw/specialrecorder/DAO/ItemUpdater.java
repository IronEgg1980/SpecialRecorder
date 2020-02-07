package th.yzw.specialrecorder.DAO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.JSON.ItemNameJSONHelper;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.model.ItemName;
import th.yzw.specialrecorder.tools.FileTools;

public class ItemUpdater extends AsyncTask<Void, Integer, Void> {

    private Result result;
    private String message;
    private File updateFile = null;
    private float value;
    private int size, progress, currentIndex;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private ItemNameJSONHelper helper;

    public void setOnFinished(IDialogDismiss onFinished) {
        this.onFinished = onFinished;
    }

    private IDialogDismiss onFinished;

    public ItemUpdater(Context context) {
        this.message = "";
        this.mContext = context;
        this.result = Result.CANCEL;
        this.helper = new ItemNameJSONHelper();
    }

    public ItemUpdater(Context context, File updateFile) {
        this(context);
        this.updateFile = updateFile;
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        publishProgress(1);
        String s = "";
        try {
            s = FileTools.readEncryptFile(updateFile);
            sleep(1000);
        } catch (IOException e) {
            message = "读取文件出错！\n原因:" + e.getMessage();
            result = Result.CANCEL;
            e.printStackTrace();
            sleep(1000);
            return null;
        }

        publishProgress(2);
        List<ItemName> list = new ArrayList<>();
        int fileVersion = 0;
        int currentVersion = AppSetupOperator.getItemVersion();
        try {
            fileVersion = helper.parseUpdateFile(s, list);
            sleep(1000);
        } catch (JSONException e) {
            message = "解析数据出错！\n原因:" + e.getMessage();
            result = Result.CANCEL;
            e.printStackTrace();
            sleep(1000);
            return null;
        }

        if (fileVersion > currentVersion) {
            size = list.size();
            if (size > 0) {
                MyDBHelper.modifyDataMode(ItemName.class, MyDBHelper.DATA_MODE_OLDDATA);
                float per = 100f / size;
                for (int i = 0; i < size; i++) {
                    list.get(i).save();
                    publishProgress(3);
                    currentIndex = i + 1;
                    progress = Math.round(currentIndex * per);
                    value = currentIndex * per;
                    sleep(20);
                }
                result = Result.OK;
                MyDBHelper.deleteAllWithDataMode(ItemName.class, MyDBHelper.DATA_MODE_OLDDATA);
                AppSetupOperator.setItemVersion(fileVersion);
            } else {
                result = Result.CANCEL;
                message = "解析失败！得到的列表为空。";
                publishProgress(0);
                sleep(1000);
            }
        } else {
            result = Result.CANCEL;
            message = "数据文件版本低于当前版本，取消更新。";
            publishProgress(4);
            sleep(1000);
        }
        publishProgress(8);
        sleep(500);
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        switch (values[0]) {
            case 8:
                Broadcasts.sendChangeTextBroadcast(mContext, "清理临时数据...");
                Broadcasts.sendProgressBroadcast(mContext, 100, 100.0f);
                break;
            case 0:
                Broadcasts.sendChangeTextBroadcast(mContext, "解析失败...");
                break;
            case 1:
                Broadcasts.sendChangeTextBroadcast(mContext, "读取文件...");
                break;
            case 2:
                Broadcasts.sendChangeTextBroadcast(mContext, "解析数据...");
                break;
            case 3:
                Broadcasts.sendChangeTextBroadcast(mContext, "正在更新...(" + currentIndex + "/" + size + ")");
                Broadcasts.sendProgressBroadcast(mContext, progress, value);
                break;
            case 4:
                Broadcasts.sendChangeTextBroadcast(mContext, "版本比较...");
                Broadcasts.sendProgressBroadcast(mContext, 100, 100f);
                message = "数据文件版本低于当前版本，取消更新。";
                break;
        }
    }

    @Override
    protected void onPostExecute(Void v) {
        if (result == Result.OK)
            message = "数据已更新！";
        onFinished.onDismiss(result, message);
    }
}

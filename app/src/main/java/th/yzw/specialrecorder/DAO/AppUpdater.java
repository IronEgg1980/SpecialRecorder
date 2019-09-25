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
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.tools.FileTools;

public class AppUpdater extends AsyncTask<Void, Integer, Void> {
    private IDialogDismiss onFinish;

    public void setOnFinish(IDialogDismiss onFinish) {
        this.onFinish = onFinish;
    }

    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private boolean isCanceled;
    private String message;
    private boolean isUpdated;
    private File updateFile = null, zipFile;

    public AppUpdater(Context context, File zipFile) {
        this.mContext = context;
        this.zipFile = zipFile;
        this.isUpdated = false;
        this.message = "";
        this.isCanceled = false;
    }

    public void cancleUpdate() {
        isCanceled = true;
        message = "已取消更新";
    }

    private boolean unZipApkFile(File zipFile) {
        boolean isupdate = false;
        if (zipFile != null) {
            try {
                isupdate = FileTools.unzipAPKFile(zipFile);
            } catch (IOException ioErr) {
                ioErr.printStackTrace();
                message = "读取文件失败！\n" + ioErr.getMessage();
            } catch (JSONException jsErr) {
                jsErr.printStackTrace();
                message = "解析文件失败！\n" + jsErr.getMessage();
            }
        }
        return isupdate;
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
        sleep(1000);
        if (zipFile == null) {
            message = "升级文件已损坏！";
            isCanceled = true;
            return null;
        } else {
            publishProgress(1);
            sleep(1000);
        }
        if (isCanceled)
            return null;
        isUpdated = unZipApkFile(zipFile);
        publishProgress(2);
        sleep(1000);
        if (isUpdated) {
            publishProgress(6);
            isCanceled = true;
            message = "当前已是最新版本，不用更新。";
            sleep(1000);
            return null;
        }
        if (isCanceled)
            return null;
        updateFile = new File(FileTools.TEMP_DIR, "update.apk");
        if (updateFile.exists()) {
            publishProgress(3);
            sleep(1000);
        } else {
            message = "升级文件解压失败，请重试一次。";
            isCanceled = true;
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        float per = 100f / 3;
        float value = per;
        int progress = 0;
        switch (values[0]) {
            case 1:
                progress = Math.round(per);
                Broadcasts.sendChangeTextBroadcast(mContext,"已找到升级文件...");
                break;
            case 2:
                progress = Math.round(2 * per);
                value = 2f * per;
                Broadcasts.sendChangeTextBroadcast(mContext,"正在解压文件...");
                break;
            case 3:
                progress = 100;
                value = 100f;
                Broadcasts.sendChangeTextBroadcast(mContext,"解压完成，正在准备更新...");
                break;
            case 6:
                progress = 100;
                value = 100f;
                Broadcasts.sendChangeTextBroadcast(mContext,"版本比较中...");
                break;
        }
        Broadcasts.sendProgressBroadcast(mContext,progress, value);
    }

    @Override
    protected void onPostExecute(Void v) {
//        MyAppSetupUtils.setAppUpdated(false);
        Broadcasts.sendBroadcast(mContext,Broadcasts.DISMISS_DIALOG);
        if (isCanceled)
            onFinish.onDismiss(false, message);
        else
            onFinish.onDismiss(true, updateFile);
    }
}

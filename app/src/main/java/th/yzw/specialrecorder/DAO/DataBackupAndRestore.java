package th.yzw.specialrecorder.DAO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.JSON.AppSetupJSONHelper;
import th.yzw.specialrecorder.JSON.ItemNameJSONHelper;
import th.yzw.specialrecorder.JSON.ItemStatisticJSONHelper;
import th.yzw.specialrecorder.JSON.JSONHelper;
import th.yzw.specialrecorder.JSON.RecordEntityJSONHelper;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.model.AppSetup;
import th.yzw.specialrecorder.model.ItemName;
import th.yzw.specialrecorder.model.ItemStatisticalInformation;
import th.yzw.specialrecorder.model.RecordEntity;
import th.yzw.specialrecorder.tools.FileTools;

public class DataBackupAndRestore extends AsyncTask<Void, Integer, Void> {
    private final int JSON_ERROR = 0;
    private final int CREATE_FILE_ERROR = 1;
    private final int CREATE_FILE = 2;

    private final int BACKUP_ITEMNAME = 3;
    private final int BACKUP_RECORDENTITY = 4;
    private final int BACKUP_INFORMATION = 5;
    private final int BACKUP_APPSETUP = 55;

    private final int WRITE_FILE = 6;

    private final int RESTORE_PREPARE = 7;
    private final int RESTORE_READJSON = 8;
    private final int RESTORE_ITEMNAME = 9;
    private final int RESTORE_RECORDENTITY = 10;
    private final int RESTORE_INFORMATION = 11;
    private final int RESTORE_APPSETUP = 66;

    private final int CLEAR_OLDDATA = 12;
//    private String backupPath;
    private String jsonString;
    private String message;
    private boolean isSuccess;
    private float perValue;
    private boolean isBackupMode;
    private IDialogDismiss onFinish;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private ItemNameJSONHelper itemNameHelper;
    private RecordEntityJSONHelper recordHelper;
    private AppSetupJSONHelper appUpdateJSONHelper;
    private ItemStatisticJSONHelper itemStatisticJSONHelper;

    public void setOnFinish(IDialogDismiss onFinish) {
        this.onFinish = onFinish;
    }

    public DataBackupAndRestore(Context context, String jsonString) {
        this.mContext = context;
//        this.backupPath = Environment.getExternalStorageDirectory() + File.separator + "MyBackup";
        if ("backup".equals(jsonString)) {
            this.isBackupMode = true;
            this.jsonString = "";
        } else {
            this.isBackupMode = false;
            this.jsonString = jsonString;
        }
        this.message = "";
        this.isSuccess = false;
        this.itemNameHelper = new ItemNameJSONHelper();
        this.recordHelper = new RecordEntityJSONHelper();
        this.appUpdateJSONHelper = new AppSetupJSONHelper();
        this.itemStatisticJSONHelper = new ItemStatisticJSONHelper();
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private JSONObject backupItemName() throws JSONException {
        List<ItemName> list = ItemNameOperator.findAll();
        int size = list.size();
        perValue = 100.0f / size;
        publishProgress(BACKUP_ITEMNAME, 0, size);
        sleep(500);
        for (int i = 1; i <= size; i++) {
            publishProgress(BACKUP_ITEMNAME, i, size);
            sleep(10);
        }
        return itemNameHelper.backup(list);
    }

    private void saveItemName(JSONObject object) throws JSONException {
        List<ItemName> list = itemNameHelper.restore(object);
        int size = 0;
        if (list != null) {
            size = list.size();
        }
        perValue = 100.0f / size;
        for (int j = 1; j <= size; j++) {
            publishProgress(RESTORE_ITEMNAME, j, size);
            sleep(10);
        }
       ItemNameOperator.saveAll(list);
    }

    private JSONObject backupRecord() throws JSONException {
        List<RecordEntity> list = RecordEntityOperator.findAll();
        int size = list.size();
        perValue = 100.0f / size;
        publishProgress(BACKUP_RECORDENTITY, 0, size);
        sleep(500);
        for (int i = 1; i <= size; i++) {
            publishProgress(BACKUP_RECORDENTITY, i, size);
            sleep(10);
        }
        return recordHelper.backup(list);
    }

    private void saveRecordEntity(JSONObject object) throws JSONException {
        List<RecordEntity> list = recordHelper.restore(object);
        int size = list != null ? list.size() : 0;
        perValue = 100.0f / size;
        for (int j = 1; j <= size; j++) {
            publishProgress(RESTORE_RECORDENTITY, j, size);
            sleep(10);
        }
        RecordEntityOperator.saveAll(list);
    }

    private JSONObject backupAppSetup() throws JSONException {
        List<AppSetup> list = AppSetupOperator.findAll();
        int size = list.size();
        perValue = 100.0f / size;
        publishProgress(BACKUP_APPSETUP, 0, size);
        sleep(500);
        for (int i = 1; i <= size; i++) {
            publishProgress(BACKUP_APPSETUP, i, size);
            sleep(10);
        }
        return appUpdateJSONHelper.backup(list);
    }

    private void saveAppSetup(JSONObject object) throws JSONException {
        List<AppSetup> list = appUpdateJSONHelper.restore(object);
        int size = list == null ? 0 : list.size();
        perValue = 100.0f / size;
        for (int j = 1; j <= size; j++) {
            publishProgress(RESTORE_APPSETUP, j, size);
            sleep(10);
        }
        AppSetupOperator.saveAll(list);
    }

    private JSONObject backupFormalation() throws JSONException {
        List<ItemStatisticalInformation> list=ItemStatisticalInformationOperator.findAll();
        int size = list.size();
        perValue = 100.0f / size;
        publishProgress(BACKUP_INFORMATION, 0, size);
        sleep(500);
        for (int k = 1; k <= size; k++) {
            publishProgress(BACKUP_INFORMATION, k, size);
        }
        return itemStatisticJSONHelper.backup(list);
    }

    private void saveInformation(JSONObject object) throws JSONException {
        List<ItemStatisticalInformation> list = itemStatisticJSONHelper.restore(object);
        int size = list == null ? 0 : list.size();
        perValue = 100.0f / size;
        for (int j = 1; j <= size; j++) {
            publishProgress(RESTORE_INFORMATION, j, size);
        }
        ItemStatisticalInformationOperator.saveAll(list);
    }

    private void backup() {
        File backupFile = new File(FileTools.BACKUP_DIR, new SimpleDateFormat("yyMMddHHmm", Locale.CHINA).format(System.currentTimeMillis()) + ".backup");
        boolean b = true;
        if (backupFile.exists())
            b = backupFile.delete();
        if (b) {
            publishProgress(CREATE_FILE);
            boolean success = false;
            try {
                success = backupFile.createNewFile();
                sleep(500);
            } catch (IOException e) {
                message = e.getMessage();
                publishProgress(CREATE_FILE_ERROR);
                sleep(1000);
                e.printStackTrace();
                isSuccess = false;
                return;
            }
            if (success) {
                String backFileString = "";
                try {
                    JSONObject object1 = backupAppSetup();
                    JSONObject object2 = backupRecord();
                    JSONObject object3 = backupItemName();
                    JSONObject object4 = backupFormalation();
                    backFileString = JSONHelper.getBackupString(object1,object2,object3,object4);
                } catch (JSONException e) {
                    message = e.getMessage();
                    publishProgress(JSON_ERROR);
                    sleep(1000);
                    e.printStackTrace();
                    return;
                }

                publishProgress(WRITE_FILE);
                try {
                    FileTools.writeDecryptFile(backFileString, backupFile);
                    isSuccess = true;
                    message = "备份成功！";
                } catch (IOException e) {
                    message = e.getMessage();
                    publishProgress(CREATE_FILE_ERROR);
                    sleep(1000);
                    e.printStackTrace();
                    isSuccess = false;
                }
                sleep(1000);
            } else {
                publishProgress(CREATE_FILE_ERROR);
                sleep(1000);
                isSuccess = false;
            }
        } else {
            publishProgress(CREATE_FILE_ERROR);
            sleep(1000);
            isSuccess = false;
        }
    }

    private void restore() {
        publishProgress(RESTORE_PREPARE, 1);
        ImportFileOperator.deleAll();
        sleep(500);
        publishProgress(RESTORE_PREPARE, 2);
        SumTotalOperator.deleAll();
        sleep(500);
        restoreData();
    }

    private void restoreData() {
        publishProgress(RESTORE_PREPARE, 3);
        modifyData(MyDBHelper.DATA_MODE_OLDDATA);
        sleep(500);
        publishProgress(RESTORE_READJSON);
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            sleep(500);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                if(object.isNull(JSONHelper.CLASS))
                    continue;
                String objectClass = object.getString(JSONHelper.CLASS);
                if (AppSetupJSONHelper.CLASSNAME.equals(objectClass)) {
                    saveAppSetup(object);
                } else if (ItemNameJSONHelper.CLASSNAME.equals(objectClass)) {
                    saveItemName(object);
                } else if (RecordEntityJSONHelper.CLASSNAME.equals(objectClass)) {
                    saveRecordEntity(object);
                } else if (ItemStatisticJSONHelper.CLASSNAME.equals(objectClass)) {
                    saveInformation(object);
                }
            }

            sleep(100);
            publishProgress(CLEAR_OLDDATA);
            clearData(MyDBHelper.DATA_MODE_OLDDATA);
            sleep(1000);
            isSuccess = true;
            message = "数据恢复成功！";
        } catch (JSONException e) {
            rollBack();
            e.printStackTrace();
            message = e.getMessage();
            publishProgress(JSON_ERROR);
            sleep(1000);
            isSuccess = false;
        }
    }

    private void modifyData(int dataMode) {
        MyDBHelper.modifyDataMode(RecordEntity.class, dataMode);
        MyDBHelper.modifyDataMode(ItemStatisticalInformation.class, dataMode);
        MyDBHelper.modifyDataMode(ItemName.class, dataMode);
        MyDBHelper.modifyDataMode(AppSetup.class, dataMode);
    }

    private void clearData(int dataMode) {
        MyDBHelper.deleteAllWithDataMode(ItemName.class, dataMode);
        MyDBHelper.deleteAllWithDataMode(RecordEntity.class, dataMode);
        MyDBHelper.deleteAllWithDataMode(ItemStatisticalInformation.class, dataMode);
        MyDBHelper.deleteAllWithDataMode(AppSetup.class, dataMode);
    }

    private void rollBack() {
        clearData(MyDBHelper.DATA_MODE_NEWDATA);
        modifyData(MyDBHelper.DATA_MODE_NEWDATA);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        sleep(1000);
        if (isBackupMode)
            backup();
        else
            restore();
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int flag = values[0], size, value, step;
        float progress;
        switch (flag) {
            case JSON_ERROR:
                Broadcasts.sendBroadcast(mContext, Broadcasts.DISMISS_DIALOG);
                message = "解析数据出错。\n" + message;
                break;
            case CREATE_FILE_ERROR:
                Broadcasts.sendBroadcast(mContext, Broadcasts.DISMISS_DIALOG);
                message = "生成文件出错。\n" + message;
                break;
            case CREATE_FILE:
                Broadcasts.sendChangeTextBroadcast(mContext, "正在创建备份文件...");
                Broadcasts.sendProgressBroadcast(mContext, 0, 0.0f);
                break;
            case BACKUP_ITEMNAME:
                step = values[1];
                value = Math.round(step * perValue);
                size = values[2];
                progress = Math.round(step * perValue * 100.0f) / 100.0f;
                Broadcasts.sendChangeTextBroadcast(mContext, "正在备份项目数据...(" + step + "/" + size + ")");
                Broadcasts.sendProgressBroadcast(mContext, value, progress);
                break;
            case BACKUP_RECORDENTITY:
                step = values[1];
                value = Math.round(step * perValue);
                size = values[2];
                progress = Math.round(step * perValue * 100.0f) / 100.0f;
                Broadcasts.sendChangeTextBroadcast(mContext, "正在备份详细记录...(" + step + "/" + size + ")");
                Broadcasts.sendProgressBroadcast(mContext, value, progress);
                break;
            case BACKUP_INFORMATION:
                step = values[1];
                value = Math.round(step * perValue);
                size = values[2];
                progress = Math.round(step * perValue * 100.0f) / 100.0f;
                Broadcasts.sendChangeTextBroadcast(mContext, "正在备份软件信息...(" + step + "/" + size + ")");
                Broadcasts.sendProgressBroadcast(mContext, value, progress);
                break;
            case BACKUP_APPSETUP:
                step = values[1];
                value = Math.round(step * perValue);
                size = values[2];
                progress = Math.round(step * perValue * 100.0f) / 100.0f;
                Broadcasts.sendChangeTextBroadcast(mContext, "正在备份APP设置...(" + step + "/" + size + ")");
                Broadcasts.sendProgressBroadcast(mContext, value, progress);
                break;
            case WRITE_FILE:
                Broadcasts.sendChangeTextBroadcast(mContext, "正在写入文件...");
                Broadcasts.sendProgressBroadcast(mContext, 100, 100.0f);
                break;
            case RESTORE_PREPARE:
                step = values[1];
                Broadcasts.sendChangeTextBroadcast(mContext, "正在准备恢复数据...");
                Broadcasts.sendProgressBroadcast(mContext, step * 33, step * 33.33f);
                break;
            case RESTORE_READJSON:
                Broadcasts.sendChangeTextBroadcast(mContext, "正在读取数据...");
                Broadcasts.sendProgressBroadcast(mContext, 100, 100.0f);
                break;
            case RESTORE_ITEMNAME:
                step = values[1];
                value = Math.round(step * perValue);
                size = values[2];
                progress = Math.round(step * perValue * 100.0f) / 100.0f;
                Broadcasts.sendChangeTextBroadcast(mContext, "正在恢复项目数据...(" + step + "/" + size + ")");
                Broadcasts.sendProgressBroadcast(mContext, value, progress);
                break;
            case RESTORE_RECORDENTITY:
                step = values[1];
                value = Math.round(step * perValue);
                size = values[2];
                progress = Math.round(step * perValue * 100.0f) / 100.0f;
                Broadcasts.sendChangeTextBroadcast(mContext, "正在恢复详细记录...(" + step + "/" + size + ")");
                Broadcasts.sendProgressBroadcast(mContext, value, progress);
                break;
            case RESTORE_INFORMATION:
                step = values[1];
                value = Math.round(step * perValue);
                size = values[2];
                progress = Math.round(step * perValue * 100.0f) / 100.0f;
                Broadcasts.sendChangeTextBroadcast(mContext, "正在恢复软件信息...(" + step + "/" + size + ")");
                Broadcasts.sendProgressBroadcast(mContext, value, progress);
                break;
            case RESTORE_APPSETUP:
                step = values[1];
                value = Math.round(step * perValue);
                size = values[2];
                progress = Math.round(step * perValue * 100.0f) / 100.0f;
                Broadcasts.sendChangeTextBroadcast(mContext, "正在恢复APP设置...(" + step + "/" + size + ")");
                Broadcasts.sendProgressBroadcast(mContext, value, progress);
                break;
            case CLEAR_OLDDATA:
                Broadcasts.sendChangeTextBroadcast(mContext, "正在清理旧数据...");
                Broadcasts.sendProgressBroadcast(mContext, 100, 100.0f);
                break;


        }
    }

    @Override
    protected void onPostExecute(Void v) {
        onFinish.onDismiss(isSuccess, message);
    }


}

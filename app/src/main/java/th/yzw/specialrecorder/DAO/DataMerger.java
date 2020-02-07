package th.yzw.specialrecorder.DAO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.ArrayMap;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.JSON.SumTotalJSONHelper;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.model.ImportedFile;
import th.yzw.specialrecorder.model.SumTotalRecord;
import th.yzw.specialrecorder.tools.FileTools;

public class DataMerger extends AsyncTask<Void, Integer, Void> {
    private final int CHANGEFILE = 1;
    private final int JSONTOLIST = 2;
    private final int SAMEFILE = 3;
    private final int BADFILE = 4;
    private final int OLDFILE = 5;
    private final int THISPHONEFILE = 6;

    private List<String> badFile, sameFile, oldFile, thisPhoneFile;
    private List<File> mFiles;
    private int count;
    private String message, currentFileName, phoneId;
    private IDialogDismiss onFinished;
    private float perValue;
    private long mMergeMonth;
    private int size;
    private SumTotalJSONHelper helper;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;

    public void setOnFinished(IDialogDismiss onFinished) {
        this.onFinished = onFinished;
    }

    private void getMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("成功导入");
        builder.append(count);
        builder.append("个文件！\n");
        if (sameFile.size() > 0) {
            builder.append("\n跳过");
            builder.append(sameFile.size());
            builder.append("个重复文件：");
            for (String _s : sameFile) {
                builder.append(_s);
                builder.append("\n");
            }
        }
        if (oldFile.size() > 0) {
            builder.append("\n跳过");
            builder.append(oldFile.size());
            builder.append("个过期文件：");
            for (String _s : oldFile) {
                builder.append(_s);
                builder.append("\n");
            }
        }
        if (badFile.size() > 0) {
            builder.append("\n跳过");
            builder.append(badFile.size());
            builder.append("个损坏文件：");
            for (String _s : badFile) {
                builder.append(_s);
                builder.append("\n");
            }
        }
        if (thisPhoneFile.size() > 0) {
            builder.append("\n跳过");
            builder.append(thisPhoneFile.size());
            builder.append("个本机文件：");
            for (String _s : thisPhoneFile) {
                builder.append(_s);
                builder.append("\n");
            }
        }
        message = builder.toString();
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<SumTotalRecord> readJsonArrayByOldData(String jsonString) {
        size = jsonString.length();
        perValue = 100.0f / size;
        try {//写导入数据逻辑
            List<SumTotalRecord> tmp = helper.parseListWithOldMode(jsonString, mMergeMonth);
            for (int i = 1; i <= tmp.size(); i++) {
                publishProgress(JSONTOLIST, i);
                sleep(50);
            }
            return tmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveData(List<SumTotalRecord> list) {
        size = list.size();
        perValue = 100.0f / size;
        for (int i = 1; i <= size; i++) {
            publishProgress(JSONTOLIST, i);
            sleep(50);
        }
        SumTotalOperator.saveAll(list);
    }

    public DataMerger(Context context, List<File> files, long mergeMonth) {
        this.mContext = context;
        this.mMergeMonth = mergeMonth;
        this.mFiles = files;
        this.phoneId = AppSetupOperator.getPhoneId();
        this.badFile = new ArrayList<>();
        this.sameFile = new ArrayList<>();
        this.thisPhoneFile = new ArrayList<>();
        this.oldFile = new ArrayList<>();
        this.helper = new SumTotalJSONHelper();
    }

    private void oldVersionMerge(String content){
        ImportedFile importedFile = ImportFileOperator.findSingleByFileName(currentFileName);
        if (importedFile != null) {
            sameFile.add(currentFileName);
            count--;
            publishProgress(SAMEFILE);
            sleep(1000);
        } else {
            List<SumTotalRecord> tmp = readJsonArrayByOldData(content);
            if (tmp != null) {
                SumTotalOperator.saveAll(tmp);
                String id = ((SumTotalRecord) tmp.get(0)).getPhoneId();
                importedFile = new ImportedFile(mMergeMonth, id, currentFileName);
                importedFile.save();
            } else {
                badFile.add(currentFileName);
                count--;
                publishProgress(BADFILE);
                sleep(1000);
            }
        }
    }

    private void newVersionMerge(String content) throws JSONException{
        ArrayMap<String, Object> map = new ArrayMap<>();
        List<SumTotalRecord> list = helper.parseSharedJSON(content, map);
        final String _phoneId = String.valueOf(map.get(SumTotalJSONHelper.PHONEID));
        if (phoneId.equals(_phoneId)) {
            thisPhoneFile.add(currentFileName);
            count--;
            publishProgress(THISPHONEFILE);
            sleep(1000);
            return;
        }
        String sendTimeString = String.valueOf(map.get(SumTotalJSONHelper.SENDTIME));
        final long sendTime = Long.parseLong(sendTimeString);
        ImportedFile importedFile = ImportFileOperator.findSingleByPhoneId(_phoneId);
        if (sendTime < mMergeMonth) {
            oldFile.add(currentFileName);
            count--;
            publishProgress(OLDFILE);
            sleep(1000);
        } else if (importedFile != null) {
            sameFile.add(currentFileName);
            count--;
            publishProgress(SAMEFILE);
            sleep(1000);
        } else {
            if (list != null && !list.isEmpty()) {
                saveData(list);
                importedFile = new ImportedFile(sendTime, _phoneId, currentFileName);
                importedFile.save();
            } else {
                badFile.add(currentFileName);
                count--;
                publishProgress(BADFILE);
                sleep(1000);
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        count = mFiles.size();
        for (final File file : mFiles) {
            try {
                currentFileName = file.getName();
                publishProgress(CHANGEFILE);
                sleep(1000);
                if (currentFileName.endsWith(".data")) {
                    String content = FileTools.readEncryptFile(file);
                    if (currentFileName.contains("SendBy")) {
                        newVersionMerge(content);
                    } else if (currentFileName.contains("BySpecialRecorder_")) {
                        oldVersionMerge(content);
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                badFile.add(currentFileName);
                count--;
                publishProgress(BADFILE);
                sleep(1000);
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int flag = values[0];
        switch (flag) {
            case CHANGEFILE:
                Broadcasts.sendChangeFileNameBroadcast(mContext, currentFileName);
                break;
            case JSONTOLIST:
                float progress = perValue * values[1];
                int step = Math.round(progress);
                Broadcasts.sendProgressBroadcast(mContext, step, progress);
                Broadcasts.sendChangeTextBroadcast(mContext, "共" + size + "条数据，已合并" + values[1] + "条数据。");
                break;
            case SAMEFILE:
                Broadcasts.sendChangeTextBroadcast(mContext, "重复文件，正在跳过...");
                break;
            case BADFILE:
                Broadcasts.sendChangeTextBroadcast(mContext, "文件已损坏，无法解析数据。");
                break;
            case OLDFILE:
                Broadcasts.sendChangeTextBroadcast(mContext, "过期文件，正在跳过...");
                break;
            case THISPHONEFILE:
                Broadcasts.sendChangeTextBroadcast(mContext, "本机文件，正在跳过...");
                break;
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        getMessage();
        onFinished.onDismiss(Result.OK, message);
    }
}

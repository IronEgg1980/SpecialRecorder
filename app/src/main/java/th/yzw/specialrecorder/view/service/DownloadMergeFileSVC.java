package th.yzw.specialrecorder.view.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import javax.mail.Folder;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.DAO.DownloadFileOperator;
import th.yzw.specialrecorder.model.DownLoadMergeFile;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.ShareFileEmailHelper;

public class DownloadMergeFileSVC extends IntentService {
    private final String TAG = "殷宗旺";
    private ShareFileEmailHelper emailHelper;

    public DownloadMergeFileSVC() {
        super("downloadmergefile");
        Broadcasts.sendBroadcast(this, Broadcasts.CHANGE_LOADING_TEXT, "正在同步文件...");
        emailHelper = new ShareFileEmailHelper();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            int count = 1;
            Store store = emailHelper.getInbox();
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            javax.mail.Message[] messages = folder.getMessages();
            for (javax.mail.Message message : messages) {
                Log.d(TAG, "onHandleIntent: download file ======> "+(count++));
                MimeMessage msg = (MimeMessage) message;
                String title = emailHelper.getSubject(msg);
                String from = emailHelper.getFrom(msg);
                if (title.contains("SendBy") && from.contains("specialrecorder") && !DownloadFileOperator.isDownload(title)) {
                    Log.d(TAG, "onHandleIntent download: "+title);
                    emailHelper.saveAttachment(msg, FileTools.mergeFileDownloadDir);
                    DownLoadMergeFile downLoadMergeFile = new DownLoadMergeFile();
                    downLoadMergeFile.setDownloadTime(System.currentTimeMillis());
                    downLoadMergeFile.setFileName(title);
                    downLoadMergeFile.save();
                }

            }
            folder.close();
            store.close();
            Broadcasts.sendBroadcast(this, Broadcasts.EMAIL_RECEIVE_SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            Broadcasts.sendBroadcast(this, Broadcasts.EMAIL_RECEIVE_FAIL);
        }
    }
}

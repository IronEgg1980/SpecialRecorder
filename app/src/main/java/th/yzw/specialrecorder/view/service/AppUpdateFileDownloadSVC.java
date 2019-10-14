package th.yzw.specialrecorder.view.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;

import javax.mail.Folder;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.model.AppSetup;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.tools.ReceiveEmailHelper;

public final class AppUpdateFileDownloadSVC extends IntentService {
//    private String TAG = "殷宗旺";
    private ReceiveEmailHelper receiveEmailHelper;

    public AppUpdateFileDownloadSVC() {
        super("AppUpdateFileDownload");
        receiveEmailHelper = new ReceiveEmailHelper();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        try {
            Store store = receiveEmailHelper.getInbox();
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            javax.mail.Message[] messages = folder.getMessages();
            for (javax.mail.Message message : messages) {
                MimeMessage msg = (MimeMessage) message;
                String title = receiveEmailHelper.getSubject(msg);
                String from = receiveEmailHelper.getFrom(msg);
                if (title.contains("appupdate") && from.contains("yinzongwang")) {
                    int index = title.lastIndexOf("_") + 1;
                    long emailAppVersion = Long.valueOf(title.substring(index));
                    long downloadVersion = AppSetupOperator.getDownloadAppVersion();
                    if (emailAppVersion > downloadVersion) {
                        String filePath = getFilesDir().getAbsolutePath() + File.separator + "UpdateFiles" + File.separator + "VersionCode" + emailAppVersion;
                        FileTools.createPath(filePath);
                        FileTools.clearFiles(filePath);
                        receiveEmailHelper.saveAttachment(msg, filePath);
                        AppSetupOperator.setDownloadAppVersion(emailAppVersion);
                        if (title.startsWith("forced")) {
                            AppSetupOperator.setForceUpdate(true);
                        }
                        Broadcasts.sendBroadcast(this, Broadcasts.APP_UPDATEFILE_DOWNLOAD_SUCCESS);
                    }
                    break;
                }
            }
            folder.close();
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

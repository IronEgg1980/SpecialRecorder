package th.yzw.specialrecorder.view.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import th.yzw.specialrecorder.Broadcasts;
import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.ItemNameOperator;
import th.yzw.specialrecorder.DAO.MyDBHelper;
import th.yzw.specialrecorder.JSON.ItemNameJSONHelper;
import th.yzw.specialrecorder.model.ItemName;
import th.yzw.specialrecorder.tools.FileTools;
import th.yzw.specialrecorder.tools.ReceiveEmailHelper;

public final class ItemNameUpdateByEmailSVC extends IntentService {
    private ReceiveEmailHelper receiveEmailHelper;
    public ItemNameUpdateByEmailSVC() {
        super("ItemNameUpdateByEmail");
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
                if (title.contains("itemupdate") && from.contains("specialrecorder")) {// 判断有无新版本ItemName
                    int index = title.lastIndexOf("_") + 1;
                    int version = Integer.valueOf(title.substring(index));
                    if (version > AppSetupOperator.getItemVersion()) {
                        List<File> files = new ArrayList<>();
                        receiveEmailHelper.saveAttachment(msg, getFilesDir().getAbsolutePath(), files);
                        List<ItemName> list = new ArrayList<>();
                        String s = FileTools.readEncryptFile(files.get(0));
                        int fileItemVersion = new ItemNameJSONHelper().parseUpdateFile(s, list);
                        if (fileItemVersion > AppSetupOperator.getItemVersion()) {
                            MyDBHelper.modifyDataMode(ItemName.class, MyDBHelper.DATA_MODE_OLDDATA);
                            ItemNameOperator.saveAll(list);
                            MyDBHelper.deleteAllWithDataMode(ItemName.class, MyDBHelper.DATA_MODE_OLDDATA);
                        }
                        AppSetupOperator.setItemVersion(fileItemVersion);
                        Broadcasts.sendBroadcast(this,Broadcasts.ITEMNAME_UPDATE_FINISH);
                    }
//                    else{
//                        Broadcasts.sendBroadcast(this,Broadcasts.ITEMNAME_UPDATE_FAIL,"version is: "+version);
//                    }
                    break;
                }
            }
            folder.close();
            store.close();
        } catch (Exception e) {
//            Broadcasts.sendBroadcast(this,Broadcasts.ITEMNAME_UPDATE_FAIL,e.getMessage());
            e.printStackTrace();
        }
    }
}

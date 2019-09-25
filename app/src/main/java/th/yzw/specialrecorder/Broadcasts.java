package th.yzw.specialrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public final class Broadcasts {
    public final static String APP_UPDATEFILE_DOWNLOAD_SUCCESS = "th.yzw.specialrecorder.AppUpdateFileDownloadFinish";
    public final static String CHANGE_LOADING_TEXT = "th.yzw.broadcaset.changeloadingtext";
    public final static String CHANGE_LOADING_PROGRESS= "th.yzw.broadcast.changeloadingprogress";
    public final static String CHANGE_LOADING_FILENAME = "th.yzw.broadcaset.changeloadingfilename";
    public final static String DISMISS_DIALOG = "th.yzw.broadcast.closedialog";
    public final static String ITEMNAME_UPDATE_FINISH = "th.yzw.specialrecorder.ITEMNAME_UPDATE_FINISH";
    public final static String ITEMNAME_UPDATE_FAIL = "th.yzw.specialrecorder.ITEMNAME_UPDATE_FAIL";

    public static void sendBroadcast(Context context, String action){
        Intent intent = new Intent();
        intent.setAction(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendBroadcast(Context context,String action,String message){
        Intent intent = new Intent();
        intent.putExtra("message",message);
        intent.setAction(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendProgressBroadcast(Context context , int progress, float value){
        Intent intent = new Intent();
        intent.setAction(CHANGE_LOADING_PROGRESS);
        intent.putExtra("value",value);
        intent.putExtra("progress",progress);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void sendChangeFileNameBroadcast(Context context,String fileName){
        sendBroadcast(context,CHANGE_LOADING_FILENAME,fileName);
    }

    public static void sendChangeTextBroadcast(Context context,String messge){
        sendBroadcast(context,CHANGE_LOADING_TEXT,messge);
    }

    public static void bindBroadcast(Context context, BroadcastReceiver receiver,String...action){
        // 实例化IntentFilter
        IntentFilter intentFilter = new IntentFilter();
        for(String a :action) {
            if("".equalsIgnoreCase(a))
                continue;
            intentFilter.addAction(a);
        }
        // 注册本地广播
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, intentFilter);
    }

    public static void unBindBroadcast(Context context,BroadcastReceiver receiver){
        // 注销该广播接收器
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }
}

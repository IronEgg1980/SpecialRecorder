package th.yzw.specialrecorder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.tools.OpenPermissionSetting;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;

public abstract class MyActivity extends AppCompatActivity {
    final protected String[] PERMISSION_GROUP_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
    final public static int PERMISSION_SETTING_FOR_RESULT = 0xf01;
    final protected int REQUST_CODE_INSTALL = 0xa01;
    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        if (resources != null && resources.getConfiguration().fontScale != 1.0f) {
            android.content.res.Configuration configuration = resources.getConfiguration();
            configuration.fontScale = 1.0f;
            resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        }
        return resources;
    }

    protected void onPermissionGranted(int requestCode){

    }

    protected void onPermissionDenied(int requestCode){

    }

    final protected void onPermissionNeverAsk(int requestCode){
        String s = "";
        if(requestCode == REQUST_CODE_INSTALL){
           s = "您已拒绝安装权限！请进入设置页面手动授权。\n请点击【确定】继续。" ;
        }else {
           s =  "您已拒绝使用存储权限，不能使用该功能！请进入设置页面手动授权。\n请点击【确定】继续。";
        }
        new ConfirmPopWindow(this)
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            //进入设置详情页
                            OpenPermissionSetting.GoToSetting(MyActivity.this);
                        }
                    }
                })
                .toConfirm(s);
    }

    final protected boolean hasInstallPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            boolean b = getPackageManager().canRequestPackageInstalls();
            if(!b){
                Uri packageURI = Uri.parse("package:" + getPackageName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                startActivityForResult(intent, REQUST_CODE_INSTALL);
            }
            return b;
        }
        return false;
    }

    @Override
    final public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean flag1 = false;
        for(int i = 0;i<permissions.length;i++){
            String permission = permissions[i];
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                flag1 = true;
                if(!ActivityCompat.shouldShowRequestPermissionRationale(this,permission)){
                    onPermissionNeverAsk(requestCode);
                    return;
                }
                break;
            }
        }
        if(flag1){
            onPermissionDenied(requestCode);
        }else{
            onPermissionGranted(requestCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUST_CODE_INSTALL){
            if(resultCode == RESULT_OK){
                onPermissionGranted(requestCode);
            }else{
                onPermissionDenied(requestCode);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ActivityManager.add(this);
    }

}

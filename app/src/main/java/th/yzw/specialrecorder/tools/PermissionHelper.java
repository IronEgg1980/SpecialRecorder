package th.yzw.specialrecorder.tools;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.InfoPopWindow;

public final class PermissionHelper {
    public interface OnResult{
        void hasPermission();
    }

    private OnResult onResult;
    private Context mContext;
    private Activity mActivity;
    private DialogInterface.OnClickListener cancel = null;

    public void setCancel(DialogInterface.OnClickListener cancel) {
        this.cancel = cancel;
    }

   private PermissionHelper(){

    }

    public PermissionHelper(Activity activity, Context context, OnResult onResult){
        this.mActivity = activity;
        this.mContext = context;
        this.onResult = onResult;
    }

    public void request(String... permission){
        if(XXPermissions.isHasPermission(mContext,permission)){
            onResult.hasPermission();
        }else{
            XXPermissions.with(mActivity).permission(permission).request(new OnPermission() {
                @Override
                public void hasPermission(List<String> granted, boolean isAll) {
                    if(isAll)
                        onResult.hasPermission();
                    else
                        new InfoPopWindow(mContext,"您已拒绝授予部分权限，可能会影响正常使用，已取消操作。")
                                .show(mActivity);
//                        new DialogFactory1(mContext).showInfoDialog("您已拒绝授予部分权限，可能会影响正常使用，已取消操作。",cancel);
                }

                @Override
                public void noPermission(List<String> denied, boolean quick) {
                    if(quick) {
                        new ConfirmPopWindow(mContext,"您已永久拒绝授权，如需使用该功能，请打开设置页面手动授予权限。")
                                .show(mActivity, new IDialogDismiss() {
                                    @Override
                                    public void onDismiss(boolean isConfirmed, Object... values) {
                                        if(isConfirmed){
                                            XXPermissions.gotoPermissionSettings(mContext,true);
                                        }
                                    }
                                });
//                        new DialogFactory1(mContext).showWarningDialog("注意",
//                                "您已永久拒绝授权，如需使用该功能，请打开设置页面手动授予权限。",
//                                "去设置",
//                                new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                XXPermissions.gotoPermissionSettings(mContext,true);
//                            }
//                        },"",cancel);
                    }else
                        new InfoPopWindow(mContext,"您已拒绝授权，不能使用该功能，已取消操作。")
                                .show(mActivity);
//                        new DialogFactory1(mContext).showInfoDialog("您已拒绝授权，不能使用该功能，已取消操作。",cancel);
                }
            });
        }
    }
}

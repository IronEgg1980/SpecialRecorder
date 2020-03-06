package th.yzw.specialrecorder.tools;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.PopupWindow;

import com.hjq.permissions.OnPermission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.InfoPopWindow;

public final class PermissionHelper {
    public interface OnResult{
        void hasPermission(boolean flag);
    }

    private OnResult mOnResult;
    private Context mContext;
    private Activity mActivity;
    private InfoPopWindow infoPopWindow;

   private PermissionHelper(){

    }

    public PermissionHelper(Activity activity, Context context, OnResult onResult){
        this.mActivity = activity;
        this.mContext = context;
        mOnResult = onResult;
        this.infoPopWindow = new InfoPopWindow(activity);
        this.infoPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mOnResult.hasPermission(false);
            }
        });
    }

    public void request(String... permission){
        if(XXPermissions.isHasPermission(mContext,permission)){
            mOnResult.hasPermission(true);
        }else{
            XXPermissions.with(mActivity).permission(permission).request(new OnPermission() {
                @Override
                public void hasPermission(List<String> granted, boolean isAll) {
                    if(isAll)
                        mOnResult.hasPermission(true);
                    else {
                        infoPopWindow.show("您已拒绝授予部分权限，可能会影响正常使用，已取消操作。");
                    }
                }

                @Override
                public void noPermission(List<String> denied, boolean quick) {
                    if(quick) {
                        new ConfirmPopWindow(mActivity)
                                .setDialogDismiss(new IDialogDismiss() {
                                    @Override
                                    public void onDismiss(Result result, Object... values) {
                                        if(result == Result.OK){
                                            XXPermissions.gotoPermissionSettings(mContext,true);
                                        }
                                    }
                                })
                                .toConfirm("您已永久拒绝授权，如需使用该功能，请打开设置页面手动授予权限。");
                    }else {
                        infoPopWindow.show("您已拒绝授权，不能使用该功能，已取消操作。");
                    }
                }
            });
        }
    }
}

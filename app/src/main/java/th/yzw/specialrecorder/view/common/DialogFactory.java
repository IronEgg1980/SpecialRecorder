package th.yzw.specialrecorder.view.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import java.util.List;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.OnSelectDialogConfirmClick;
import th.yzw.specialrecorder.interfaces.SelectDialogClicker;

public final class DialogFactory {
    private Context context;

    public DialogFactory(Context context){
        this.context =context;
    }

    private void setCommonAnim(Window window){
        if(window!=null)
            window.setWindowAnimations(R.style.CommonDialogAnim);
    }
    private void setSelectAnim(Window window){
        if(window!=null)
            window.setWindowAnimations(R.style.SelectDialogAnim);
    }

    public void showInfoDialog(String message) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage(message)
                .setIcon(R.drawable.ic_info_18dp)
                .setNegativeButton("关闭", null)
                .setCancelable(true)
                .show();
        setCommonAnim(dialog.getWindow());
    }

    public void showInfoDialog(String message, DialogInterface.OnClickListener cancelClick) {
        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage(message)
                .setIcon(R.drawable.ic_info_18dp)
                .setNegativeButton("关闭", cancelClick)
                .setCancelable(true)
                .show();
        setCommonAnim(dialog.getWindow());
    }

    public void showWarningDialog(String title, String message,
                                  String confirmText, DialogInterface.OnClickListener confirmClick,
                                  String cancelText, DialogInterface.OnClickListener cancelClick) {
        Dialog dialog =  new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.ic_warning_18dp)
                .setPositiveButton(confirmText, confirmClick)
                .setNegativeButton(cancelText, cancelClick)
                .setCancelable(true)
                .show();
        setCommonAnim(dialog.getWindow());
    }

    public void showDefaultConfirmDialog(String message, DialogInterface.OnClickListener confirmClick) {
        showWarningDialog("请确认", message,
                "确认", confirmClick,
                "取消", null);
    }

    public void showQuickSingleSelect(final View view, final String[] items) {
        int checkedId = view.getTag() == null ? -1 : (int) view.getTag();
        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle("请选择")
                .setIcon(R.drawable.ic_info_18dp)
                .setSingleChoiceItems(items, checkedId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        view.setTag(which);
                        TextView textView = (TextView) view;
                        textView.setText(items[which]);
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .show();
        setSelectAnim(dialog.getWindow());
    }

    public void showSingleSelect(final String[] items, final SelectDialogClicker confirmClick) {
        Dialog dialog =  new AlertDialog.Builder(context)
                .setTitle("请选择")
                .setIcon(R.drawable.ic_info_18dp)
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        confirmClick.click(which);
                    }
                })
                .setNegativeButton("关闭", null)
                .setCancelable(true)
                .show();
        setSelectAnim(dialog.getWindow());
    }

    public void showSingleSelectSaveIndex(final String[] items, int currentIndex,final SelectDialogClicker confirmClick) {
        Dialog dialog =  new AlertDialog.Builder(context)
                .setTitle("请选择")
                .setIcon(R.drawable.ic_info_18dp)
                .setSingleChoiceItems(items, currentIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        confirmClick.click(which);
                    }
                })
                .setNegativeButton("关闭", null)
                .setCancelable(true)
                .show();
        setSelectAnim(dialog.getWindow());
    }

    public void showSingleSelectWithConfirmButton(final String[] items, final SelectDialogClicker confirmClick) {
        final int[] checkedId = {-1};
        Dialog dialog =  new AlertDialog.Builder(context)
                .setTitle("请选择")
                .setIcon(R.drawable.ic_info_18dp)
                .setSingleChoiceItems(items, checkedId[0], new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkedId[0] = which;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
                        if (checkedId[0] != -1)
                            confirmClick.click(checkedId[0]);
                    }
                })
                .setNegativeButton("取消", null)
                .setCancelable(true)
                .show();
        setSelectAnim(dialog.getWindow());
    }

    public void showMultiSelect(String[] items, final SelectDialogClicker confirmClick) {
        final boolean[] checkItems = new boolean[items.length];
        Dialog dialog =  new AlertDialog.Builder(context)
                .setTitle("请选择")
                .setIcon(R.drawable.ic_info_18dp)
                .setMultiChoiceItems(items, checkItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkItems[which] = isChecked;
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmClick.click(checkItems);
                    }
                })
                .setNegativeButton("取消", null)
                .setCancelable(true)
                .show();
        setSelectAnim(dialog.getWindow());
    }


}

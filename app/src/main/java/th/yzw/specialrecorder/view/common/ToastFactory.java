package th.yzw.specialrecorder.view.common;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public final class ToastFactory {
    private Context mContext;
    public ToastFactory(Context context){
        this.mContext = context;
    }

    public void showDefaultToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public void showLongToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    public void showTopToast(String message){
        Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

    public void showCenterToast(String _message) {
        Toast toast = Toast.makeText(mContext, _message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void showCenterToast(String _message,int xOffset,int yOffset) {
        Toast toast = Toast.makeText(mContext, _message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, xOffset, yOffset);
        toast.show();
    }

    public void showToast(String _message,int time, int position, int xOffset, int yOffset) {
        Toast toast = Toast.makeText(mContext, _message, time);
        toast.setGravity(position, xOffset, yOffset);
        toast.show();
    }
}

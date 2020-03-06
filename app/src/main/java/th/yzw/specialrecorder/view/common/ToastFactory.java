package th.yzw.specialrecorder.view.common;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import th.yzw.specialrecorder.R;

public final class ToastFactory {
    private Toast toast;
    private TextView textView;
    public ToastFactory(Context context){
        this.toast = new Toast(context);
        View view = LayoutInflater.from(context).inflate(R.layout.toast_view,null);
        textView = view.findViewById(R.id.textView);
        toast.setView(view);
    }

    public void showDefaultToast(String message) {
        showToast(message,Toast.LENGTH_SHORT,Gravity.BOTTOM,0,0);
    }

    public void showLongToast(String message) {
        showToast(message,Toast.LENGTH_LONG,Gravity.BOTTOM,0,0);
    }

    public void showTopToast(String message){
        showToast(message,Toast.LENGTH_SHORT,Gravity.TOP,0,100);
    }

    public void showCenterToast(String message) {
        showCenterToast(message,0,0);
    }

    public void showCenterToast(String message,int xOffset,int yOffset) {
        showToast(message,Toast.LENGTH_SHORT,Gravity.CENTER,xOffset,yOffset);
    }

    public void showToast(String message,int time, int position, int xOffset, int yOffset) {
        toast.setDuration(time);
        textView.setText(message);
        toast.setGravity(position, xOffset, yOffset);
        toast.show();
    }
}

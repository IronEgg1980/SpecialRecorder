package th.yzw.specialrecorder.interfaces;

import android.view.View;


public abstract class NoDoubleClickListener implements View.OnClickListener {
    private static long lastClickTime = 0;
    @Override
    public final void onClick(View v) {
        long current = System.currentTimeMillis();
        if(current - lastClickTime > 500){
            onNoDoubleClick(v);
            lastClickTime = current;
        }
    }

    public void onNoDoubleClick(View v){

    }
}

package th.yzw.specialrecorder.interfaces;

import android.view.View;

import th.yzw.specialrecorder.tools.OtherTools;


public abstract class NoDoubleClickListener implements View.OnClickListener {
    @Override
    public final void onClick(View v) {
        long current = System.currentTimeMillis();
        if(current - OtherTools.lastClickTime > 500){
            onNoDoubleClick(v);
            OtherTools.lastClickTime = current;
        }
    }

    public void onNoDoubleClick(View v){

    }
}

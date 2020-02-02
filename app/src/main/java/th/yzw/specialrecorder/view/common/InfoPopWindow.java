package th.yzw.specialrecorder.view.common;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import th.yzw.specialrecorder.R;

public class InfoPopWindow extends PopupWindow {
    private Context mContext;
    private String mMessage;
    private Activity mActivity = null;

    public InfoPopWindow(Context context,String message){
        this.mContext = context;
        this.mMessage = message;
        createView();
        setOutsideTouchable(true);
        setTouchable(true);
        setFocusable(true);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.PopWindowAnim);
    }

    private void createView(){
        View view = LayoutInflater.from(mContext).inflate(R.layout.popwindow_info_layout,null);
        TextView textView = view.findViewById(R.id.messageTV);
        textView.setText(mMessage);
        view.findViewById(R.id.confirmTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setContentView(view);
    }

    public void show(Activity activity){
        this.mActivity = activity;
        showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM,0,0);
        darkenBackground(0.5f);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                darkenBackground(1f);
            }
        });
    }

    private void darkenBackground(Float bgcolor) {
        if(mActivity == null)
            return;
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = bgcolor;
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mActivity.getWindow().setAttributes(lp);
    }
}

package th.yzw.specialrecorder.view.input_data;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import th.yzw.specialrecorder.R;

public class MyPopupWin2 extends PopupWindow {
    public void setTextSize(float textSize) {
        this.mTvSign.setTextSize(textSize);
    }
    private TextView mTvSign;
    private CountDownTimer timer;
    private long time;
    public void setDuration(long time){
        //这里填持续时间也就是隔多长时间消失
        this.time = time;
    }
    public void setContent(String content) {
        this.mTvSign.setText(content);
        if(timer!=null)
            timer.cancel();
        timer= new CountDownTimer(time, 100) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                dismiss2();
            }

        };
        timer.start();
    }

    public MyPopupWin2(Context context){
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.popwindow2_layout, null);
        mTvSign = contentView.findViewById(R.id.textView);
        ImageView close = contentView.findViewById(R.id.imageView);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss2();
            }
        });
        this.setContentView(contentView);
        this.setFocusable(false);
        this.setOutsideTouchable(true);
        this.setTouchable(true);
        this.setBackgroundDrawable(new ColorDrawable());
        this.setAnimationStyle(R.style.PopWindowAnimation2);
        setDuration(5000);
    }

    @Override
    public void dismiss() {
        //super.dismiss();
    }

    public void dismiss2() {
        super.dismiss();
    }
}

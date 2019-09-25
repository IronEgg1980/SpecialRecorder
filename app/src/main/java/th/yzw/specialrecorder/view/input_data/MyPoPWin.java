package th.yzw.specialrecorder.view.input_data;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import th.yzw.specialrecorder.R;

public class MyPoPWin extends PopupWindow {
    public void setTextSize(float textSize) {
        this.mTvSign.setTextSize(textSize);
    }
    private TextView mTvSign;
    private long time;
    public void setDuration(long time){
        //这里填持续时间也就是隔多长时间消失
        this.time = time;
    }
    public void setContent(String content) {
        this.mTvSign.setText(content);
    }

    public MyPoPWin(Context context) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.pop_textview, null);
        mTvSign = contentView.findViewById(R.id.textView);
        mTvSign.setTextSize(18);
        this.setContentView(contentView);
        this.setFocusable(false);
        this.setOutsideTouchable(true);
        this.setTouchable(false);
        this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setAnimationStyle(R.style.PopWindowAnimation);
        setDuration(500);
    }

    public MyPoPWin(Context context, String content) {
        this(context);
        setContent(content);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        CountDownTimer timer = new CountDownTimer(time, 100) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                dismiss();
            }

        };
        timer.start();
    }
}

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

public class MyPopWindow3 extends PopupWindow {
    private TextView textView;
    private CountDownTimer timer;
    private long time;
    public void setDuration(long time){
        //这里填持续时间也就是隔多长时间消失
        this.time = time;
    }
    public void changeText(String text){
        textView.setText(text);
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

    public MyPopWindow3(Context context) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.pop_window3, null);
        textView = contentView.findViewById(R.id.textview);
        textView.setTextSize(16);
        this.setContentView(contentView);
        this.setFocusable(false);
        this.setOutsideTouchable(true);
        this.setTouchable(false);
        this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setAnimationStyle(R.style.PopWindow3Animation);
        setDuration(2000);
    }

    public MyPopWindow3(Context context,String text){
        this(context);
        changeText(text);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
    }
    @Override
    public void dismiss() {
        //super.dismiss();
    }

    public void dismiss2() {
        super.dismiss();
    }
}

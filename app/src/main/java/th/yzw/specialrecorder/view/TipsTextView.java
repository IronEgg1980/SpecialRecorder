package th.yzw.specialrecorder.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.Gravity;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.tools.OtherTools;

public class TipsTextView extends AppCompatTextView {
    private CharSequence mText = "";
    private int endIndex = 1,count = 5;
    private long preTime = 0;
    private Paint mPaint;

    public TipsTextView(Context context) {
        super(context);
        initial();
    }

    public TipsTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initial();
    }

    public TipsTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initial();
    }

    private void initial(){
        mText = getText().toString();
        mPaint = new Paint();
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setColor(getCurrentTextColor());
        mPaint.setTextSize(getTextSize());
        mPaint.setTypeface(getTypeface());
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long time = System.currentTimeMillis();
        if(time - preTime > 100 && count < 0){
            preTime = time;
            endIndex ++;
            if(endIndex == mText.length())
                count = 200;
            if (endIndex > mText.length()) {
                endIndex = 1;
            }
        }
        String s = mText.toString().substring(0,endIndex);
        canvas.drawText(s,getWidth() / 2,getBaseline(),mPaint);
        count--;
        invalidate();
//        if(count > 0){
//            long time = System.currentTimeMillis();
//            if(time - preTime > 300){
//                preTime = time;
//                endIndex ++;
//                if (endIndex > mText.length()) {
//                    endIndex = 1;
//                    count--;
//                }
//            }
//            String s = mText.toString().substring(0,endIndex);
//            canvas.drawText(s,getWidth() / 2,getBaseline(),mPaint);
//            invalidate();
//        }else{
//            super.onDraw(canvas);
//        }
    }
}

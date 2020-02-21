package th.yzw.specialrecorder.view.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.rengwuxian.materialedittext.MaterialEditText;

public class FlagTextView extends AppCompatTextView {
    private boolean isShowLeftTopFlag = false;
    private boolean isShowRightBottomFlag = false;
    private Paint flagBgPaint,flagTextPaint;

    public FlagTextView(Context context) {
        super(context);
    }

    public FlagTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void showLeftTopFlag(){
        this.isShowLeftTopFlag = true;
        invalidate();
    }

    public void showRightBottomFlag(){
        this.isShowRightBottomFlag = true;
//        invalidate();
    }

    public void hideLeftTopFlag(){
        this.isShowLeftTopFlag = false;
//        invalidate();
    }

    public void hideRightBottomFlag(){
        this.isShowRightBottomFlag = false;
        invalidate();
    }

    private void initialPaint(int width){
        if(flagBgPaint == null){
            flagBgPaint = new Paint();
            flagBgPaint.setColor(Color.RED);
            flagBgPaint.setStyle(Paint.Style.FILL);
        }
        if(flagTextPaint == null){
            flagTextPaint = new Paint();
            flagTextPaint.setColor(Color.WHITE);
            flagTextPaint.setStyle(Paint.Style.FILL);
            flagTextPaint.setStrokeWidth(15);
            flagTextPaint.setTextSize(30);
        }
    }

    private void drawLeftTopFlag(Canvas canvas){
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        initialPaint(width);
        canvas.save();

        Path path = new Path();
        path.moveTo(width * 2 / 3,0);
        path.lineTo(0,0);
        path.lineTo(0,height *2 / 3);
        path.close();
        canvas.drawPath(path,flagBgPaint);

        canvas.restore();
    }

    private void drawRightBottomFlag(Canvas canvas){
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        initialPaint(width);
        canvas.save();

        Path path = new Path();
        path.moveTo(width,height / 3);
        path.lineTo(width,height);
        path.lineTo(width / 3,height);
        path.close();
        canvas.drawPath(path,flagBgPaint);

        canvas.restore();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isShowLeftTopFlag)
             drawLeftTopFlag(canvas);
        if(isShowRightBottomFlag)
            drawRightBottomFlag(canvas);
    }
}

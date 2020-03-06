package th.yzw.specialrecorder.view.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class MaqueeTextView extends AppCompatTextView {
    private int speed = 5;
    private float xOffset = 0;
    private Paint paint;
    private CharSequence text;
    private boolean isStart = true;

    public MaqueeTextView(Context context) {
        super(context);
        initial();
    }

    public MaqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initial();
    }

    public MaqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initial();
    }

    private void initial(){
        setSingleLine(true);
        paint = getPaint();
        text = getText().toString();
    }

    public void start(){
        isStart = true;
        invalidate();
    }

    public void stop(){
        isStart = false;
    }

    public void changeSpeed(int speed){
        this.speed = speed;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        this.text = text;
        this.xOffset = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        int textWidth = (int) paint.measureText(text.toString());
        canvas.drawText(text.toString(),(width - xOffset), getBaseline(),paint);
        xOffset += speed;
        if(width - xOffset + textWidth < 0)
            xOffset = 0;
        if(isStart) {
            invalidate();
        }
    }
}

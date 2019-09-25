package th.yzw.specialrecorder.view.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.OnIndexBarPressedListener;
import th.yzw.specialrecorder.tools.OtherTools;

public class SideIndexBarView extends View {
    private int mWidth, mHeight, letterHeight;
    private int textSize;
    private int touchedBGColor;
    private int radius;
    private boolean isTouch;
    private Rect indexBounds;//存放每个字母的矩形区域
    private RectF rectF;
    private int indexRectY;
    private String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z"};
    private OnIndexBarPressedListener pressedListener;
    private Paint textPaint, bgPaint, letterBGPaint;

    public void setPressedListener(OnIndexBarPressedListener pressedListener) {
        this.pressedListener = pressedListener;
    }

    public void setLetters(String[] letters) {
        if (letters != null && letters.length > 0)
            this.letters = letters;
    }

    private void initialPaint() {
        this.textPaint = new Paint();
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.GRAY);
        this.bgPaint = new Paint();
        bgPaint.setColor(touchedBGColor);
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.FILL);

        this.letterBGPaint = new Paint();
        letterBGPaint.setColor(getResources().getColor(R.color.colorAccent));
        letterBGPaint.setAntiAlias(true);
        letterBGPaint.setStyle(Paint.Style.FILL);

        indexBounds = new Rect();
        rectF = new RectF();
    }

    private void setAttributeSet(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SideIndexBarView, 0, 0);
        int n = typedArray.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = typedArray.getIndex(i);
            switch (attr) {
                case R.styleable.SideIndexBarView_textSize:
                    textSize = typedArray.getDimensionPixelSize(attr, textSize);
                    break;
                case R.styleable.SideIndexBarView_radius:
                    radius = typedArray.getDimensionPixelSize(attr, radius);
                    break;
                case R.styleable.SideIndexBarView_pressedBGColor:
                    touchedBGColor = typedArray.getColor(attr, touchedBGColor);
                    break;
                default:
                    break;
            }
        }
        typedArray.recycle();
    }


    public SideIndexBarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.touchedBGColor = Color.parseColor("#99000000");
        this.textSize = OtherTools.dip2px(context, 10f);
        this.radius = OtherTools.dip2px(context, 0f);
        setAttributeSet(context, attrs);
        initialPaint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 取出宽高的mode和size
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        int measureWidth = 0, measureHeight = 0; // 最终测量出来的宽高
        // 得到合适的宽度
        String letter;//绘制的字母
        int count = letters.length;
        for (String s : letters) {
            letter = s;
            textPaint.getTextBounds(letter, 0, letter.length(), indexBounds);
            measureWidth = Math.max(indexBounds.width(), measureWidth);
            measureHeight = Math.max(indexBounds.height(), measureHeight);
        }
        measureHeight = measureHeight * count;
        switch (wMode) {
            case MeasureSpec.EXACTLY:
                measureWidth = wSize;
                break;
            case MeasureSpec.AT_MOST:
                measureWidth = Math.min(measureWidth, wSize);
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }

        switch (hMode) {
            case MeasureSpec.EXACTLY:
                measureHeight = hSize;
                break;
            case MeasureSpec.AT_MOST:
                measureHeight = Math.min(measureHeight, hSize);
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        letterHeight = (mHeight - getPaddingTop() - getPaddingBottom()) / letters.length;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int t = getPaddingTop();
//        Rect indexBounds = new Rect();
//        RectF rectF = new RectF();
        rectF.set(0, 0, mWidth, mHeight);
        if (isTouch){
            canvas.drawRoundRect(rectF, radius, radius, bgPaint);
            float x = mWidth / 2f;
            float y = indexRectY+letterHeight / 2f;
            float r = Math.min(x,letterHeight / 2f);
            canvas.drawCircle(x,y,r,letterBGPaint);
//            indexBounds.set(0, indexRectY, mWidth, indexRectY + letterHeight);
//            canvas.drawRect(indexBounds, letterBGPaint);
        }
        String indexText;
        for (int i = 0; i < letters.length; i++) {
            indexText = letters[i];
            textPaint.getTextBounds(indexText, 0, indexText.length(), indexBounds);
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            int baseLine = (int) (letterHeight - fontMetrics.top - fontMetrics.bottom) / 2;
            canvas.drawText(indexText, (mWidth - indexBounds.width()) / 2, t + letterHeight * i + baseLine, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                textPaint.setColor(Color.WHITE);
                isTouch = true;
//                invalidate();
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                int pressedIndex = (int) (y - getPaddingTop()) / letterHeight;
                if (pressedIndex < 0) {
                    pressedIndex = 0;
                } else if (pressedIndex >= letters.length) {
                    pressedIndex = letters.length - 1;
                }
                indexRectY = getPaddingTop() + pressedIndex * letterHeight;
                if (null != pressedListener) {
                    pressedListener.onIndexBarPressed(pressedIndex, letters[pressedIndex]);
                }
                break;
            case MotionEvent.ACTION_UP:
                isTouch = false;
//                invalidate();
                textPaint.setColor(Color.GRAY);
//                setBackgroundResource(android.R.color.transparent);
                if (null != pressedListener) {
                    pressedListener.onMoutionEventEnd();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            default:
                break;
        }
        invalidate();
        return true;
    }
}


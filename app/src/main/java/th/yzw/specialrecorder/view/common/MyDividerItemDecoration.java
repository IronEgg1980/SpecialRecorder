package th.yzw.specialrecorder.view.common;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class MyDividerItemDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = "MyDividerItemDecoration";
    private final Rect mBounds;
    private Paint mPaint;
    private int dividerSize = 4;

    public MyDividerItemDecoration() {
        mBounds = new Rect();
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#dddddd"));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(dividerSize);
        mPaint.setAntiAlias(true);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = dividerSize;
        } else {
            outRect.bottom = 0;
        }
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (parent.getLayoutManager() != null) {
            this.drawVertical(c, parent);
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        canvas.save();
        int left;
        int right;
        if (parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            canvas.clipRect(left, parent.getPaddingTop(), right, parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        int childCount = parent.getChildCount();

        for(int i = 0; i < childCount; ++i) {
            View child = parent.getChildAt(i);
            if(parent.getChildAdapterPosition(child) != parent.getAdapter().getItemCount() - 1) {
                parent.getDecoratedBoundsWithMargins(child, this.mBounds);
                int bottom = this.mBounds.bottom + Math.round(child.getTranslationY());
                int center = bottom - dividerSize / 2;
                canvas.drawLine(left,center,right,center,mPaint);
            }
        }
        canvas.restore();
    }
}

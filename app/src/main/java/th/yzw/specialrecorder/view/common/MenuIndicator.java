package th.yzw.specialrecorder.view.common;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

public class MenuIndicator extends RecyclerView.ItemDecoration {
    private Paint mPaint;
    private Path topPath = null, bottomPath = null;
    private float indicatorWidth, indicatorHeight;
    private float x, topY, bottomY;

    MenuIndicator(int indicatorColor) {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(indicatorColor);

        indicatorWidth = 30;
        indicatorHeight = 20;
    }


    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int count = parent.getChildCount();
        x = parent.getLeft() + parent.getWidth() / 2;
        topY = parent.getTop();
        bottomY =  parent.getBottom();
//        View firstView = parent.getChildAt(0);
//        View lastView = parent.getChildAt(count - 1);
        if (parent.computeVerticalScrollOffset() > 0) {
            drawTopIndicator(c);
        }

        if (parent.computeVerticalScrollOffset() + parent.computeVerticalScrollExtent() < parent.computeVerticalScrollRange()) {
            drawBottomIndicator(c);
        }
    }

    private void drawTopIndicator(Canvas c) {
        c.save();
        if (topPath == null) {
            topPath = new Path();
            topPath.moveTo(x, topY);
            topPath.lineTo(x - indicatorWidth / 2, topY + indicatorHeight);
            topPath.lineTo(x + indicatorWidth / 2, topY + indicatorHeight);
            topPath.close();
        }
        c.drawPath(topPath, mPaint);
        c.restore();
    }

    private void drawBottomIndicator(Canvas c) {
        c.save();
        if (bottomPath == null) {
            bottomPath = new Path();
            bottomPath.moveTo(x, bottomY);
            bottomPath.lineTo(x - indicatorWidth / 2, bottomY - indicatorHeight);
            bottomPath.lineTo(x + indicatorWidth / 2 , bottomY - indicatorHeight);
            bottomPath.close();
        }
        c.drawPath(bottomPath, mPaint);
        c.restore();
    }
}

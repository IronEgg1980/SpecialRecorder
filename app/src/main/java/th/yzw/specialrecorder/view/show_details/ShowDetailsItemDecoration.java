package th.yzw.specialrecorder.view.show_details;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.tools.OtherTools;

public class ShowDetailsItemDecoration extends RecyclerView.ItemDecoration {

    private Paint linePaint,pointPaint;
    private Context mContext;
    public ShowDetailsItemDecoration(Context context){
        this.mContext = context;
        linePaint = new Paint();
        linePaint.setColor(mContext.getResources().getColor(R.color.colorPrimary));
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(2);
        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setColor(mContext.getResources().getColor(R.color.colorAccent));
        pointPaint.setStrokeWidth(15);
    }
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(100,0,0,0);
    }


    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if(!state.isMeasuring()) {
            int total = parent.getAdapter().getItemCount();
            int count = parent.getChildCount();
            for (int i = 0; i < count; i++) {
                View view = parent.getChildAt(i);
                TextView dateView = view.findViewById(R.id.dateTextView);
                int height = dateView.getMeasuredHeight();
                float x = view.getX() - 50;
                float y1 = view.getY();
                float y2 ;
                if (parent.getChildAdapterPosition(view)==(total -1))
                    y2 = view.getY() + height / 2;
                else {
                    y2 = view.getY()+view.getHeight()+ OtherTools.dip2px(mContext,20f);
                }
                c.drawLine(x, y1, x, y2, linePaint);
                c.drawCircle(x, view.getY() + height / 2, 15, pointPaint);
            }
        }
        super.onDraw(c, parent, state);
    }
}

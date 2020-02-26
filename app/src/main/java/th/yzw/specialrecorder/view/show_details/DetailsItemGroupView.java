package th.yzw.specialrecorder.view.show_details;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class DetailsItemGroupView extends RelativeLayout {
    private int[] clickPosition = new int[2];

    public int[] getClickPosition() {
        return clickPosition;
    }

    public DetailsItemGroupView(Context context) {
        super(context);
    }

    public DetailsItemGroupView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DetailsItemGroupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            clickPosition[0] = (int) ev.getRawX();
            clickPosition[1] = (int) ev.getRawY();
        }
        return super.onInterceptTouchEvent(ev);
    }
}

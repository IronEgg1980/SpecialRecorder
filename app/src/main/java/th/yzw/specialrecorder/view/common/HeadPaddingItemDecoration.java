package th.yzw.specialrecorder.view.common;

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

public class HeadPaddingItemDecoration extends RecyclerView.ItemDecoration {

    private Context mContext;
    public HeadPaddingItemDecoration(Context context){
        this.mContext = context;
    }
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if(parent.getChildAdapterPosition(view) == 0){
            outRect.top = OtherTools.dip2px(mContext,40);
        }
    }

}

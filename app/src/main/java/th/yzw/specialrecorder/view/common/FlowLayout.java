package th.yzw.specialrecorder.view.common;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.model.ItemName;

public class FlowLayout extends RelativeLayout{
    private String TAG = "殷宗旺";

    private int lastIndex = -1;
    private List<ItemName> mList;
    private int textViewMargin = 30;
    private int mWidth;
    private boolean mInited = false;
    private MyClickListener onItemClickListenr = null;

    public FlowLayout(Context context) {
        super(context);
        init(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(!mInited){
                    mInited = true;
                    createChildView();
                }
            }
        });
    }

    public void setDataSource(List<ItemName> list){
        this.mList = list;
        createChildView();
    }

    public void setTextViewMargin(int margin) {
        this.textViewMargin = margin;
    }

    public void setOnItemClickListenr(MyClickListener onItemClickListenr) {
        this.onItemClickListenr = onItemClickListenr;
    }

    private void createChildView(){
        if(!mInited || mList == null || mList.isEmpty())
            return;
        removeAllViews();
        int total = getPaddingLeft() + getPaddingRight();
        int index = 1;
        int alignTopAnchor = 1;

        for(int i = 0;i<mList.size();i++) {
            final TextView view = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.itemname_list_item, null);
            view.setId(index);
            String name = mList.get(i).getName();
            view.setText(name);
            view.setFocusable(true);
            view.setClickable(true);
            final int finalI = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    view.setBackground(getContext().getDrawable(R.drawable.keyboard_input_item_select_bg));
                    view.setTextColor(Color.WHITE);
                    if(lastIndex != -1){
                        TextView textView = (TextView) getChildAt(lastIndex);
                        textView.setBackground(getContext().getDrawable(R.drawable.keyboard_input_item_bg));
                        textView.setTextColor(getContext().getColor(R.color.textColor));
                    }
                    lastIndex = finalI;
                    onItemClickListenr.OnClick(view, finalI);
                }
            });
            view.measure(MeasureSpec.UNSPECIFIED,MeasureSpec.UNSPECIFIED);
            int childWidth = view.getMeasuredWidth();
            LayoutParams childLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            childLP.bottomMargin = textViewMargin;
            if(total + childWidth + textViewMargin > mWidth){
                childLP.addRule(RelativeLayout.BELOW,alignTopAnchor);
                total = getPaddingLeft() + getPaddingRight();
                alignTopAnchor = index;
            }else{
                childLP.addRule(RelativeLayout.ALIGN_TOP,alignTopAnchor);
                if(index != alignTopAnchor){
                    childLP.addRule(RelativeLayout.END_OF,index -1);
                    childLP.leftMargin = textViewMargin;
                    total += textViewMargin;
                }
            }
            addView(view,childLP);
            total += childWidth;
            index ++ ;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
    }
}

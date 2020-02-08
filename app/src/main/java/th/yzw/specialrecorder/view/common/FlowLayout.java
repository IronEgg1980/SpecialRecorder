package th.yzw.specialrecorder.view.common;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import th.yzw.specialrecorder.DAO.ItemNameOperator;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.model.ItemName;
import th.yzw.specialrecorder.tools.OtherTools;

public class FlowLayout extends RelativeLayout {
    private String TAG = "殷宗旺";

    private int lastIndex = -1;
    private List<ItemName> mList;
    //    private List<String> firstLetters;
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

    private void init(Context context) {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mInited) {
                    mInited = true;
                    createChildView();
                }
            }
        });
    }

    public void setDataSource(List<ItemName> list) {
        this.mList = list;
//        this.firstLetters = ItemNameOperator.getItemNameFistLetterList(list);
        createChildView();
    }

    public void setTextViewMargin(int margin) {
        this.textViewMargin = margin;
    }

    public void setOnItemClickListenr(MyClickListener onItemClickListenr) {
        this.onItemClickListenr = onItemClickListenr;
    }

    private void createChildView() {
        if (!mInited || mList == null || mList.isEmpty())
            return;

        removeAllViews();

        String firstLetter = "";
        int total = getPaddingLeft() + getPaddingRight();
        int index = generateViewId();
        int alignTopAnchor = generateViewId();
        int endOfId = -1;
        int textViewPadding = OtherTools.dip2px(getContext(),6);
        for (int i = 0; i < mList.size(); i++) {
            final TextView view = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.itemname_list_item, null);
            view.setId(index);
            String name = mList.get(i).getName();
            String _firstLetter = name.substring(0, 1).toUpperCase();
            view.setText(name);
            view.setFocusable(true);
            view.setClickable(true);
            final int finalI = i;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    view.setBackground(getContext().getDrawable(R.drawable.keyboard_input_item_select_bg));
                    view.setTextColor(Color.WHITE);
                    if (lastIndex != -1) {
                        TextView textView = findViewById(lastIndex);
                        textView.setBackground(getContext().getDrawable(R.drawable.keyboard_input_item_bg));
                        textView.setTextColor(getContext().getColor(R.color.colorPrimary));
                    }
                    lastIndex = view.getId();
                    onItemClickListenr.OnClick(view, finalI);
                }
            });
            view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            int childWidth = view.getMeasuredWidth();
            LayoutParams childLP = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            childLP.bottomMargin = textViewMargin;

            if (!firstLetter.equals(_firstLetter) || total + childWidth + textViewMargin > mWidth) {
                LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.bottomMargin = textViewMargin;
                lp.addRule(RelativeLayout.BELOW, alignTopAnchor);

                alignTopAnchor = generateViewId();

                TextView textView = new TextView(getContext());
                textView.setId(alignTopAnchor);
                textView.setPadding(0,textViewPadding,textViewPadding,textViewPadding);
                textView.setWidth(textViewMargin + textViewPadding*2 );
                textView.setHeight(view.getMeasuredHeight());

                if (!firstLetter.equals(_firstLetter))
                    textView.setText(_firstLetter);
                else
                    textView.setText("");

                childLP.addRule(END_OF, alignTopAnchor);
                childLP.addRule(ALIGN_TOP, alignTopAnchor);

                firstLetter = _firstLetter;
                total = getPaddingLeft() + getPaddingRight() + textViewMargin + textViewPadding*2;
                addView(textView, lp);
            } else {
                childLP.addRule(RelativeLayout.ALIGN_TOP, alignTopAnchor);
                childLP.addRule(RelativeLayout.END_OF, endOfId);
                childLP.leftMargin = textViewMargin;
                total += textViewMargin;
            }
            addView(view, childLP);
            total += childWidth;
            endOfId = index;
            index=generateViewId();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
    }
}

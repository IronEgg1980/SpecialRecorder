package th.yzw.specialrecorder.view.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import th.yzw.specialrecorder.R;

public class MyDatePicker extends LinearLayout {
    public interface DatePickerClickListener {
        void onClick(int year, int month, int dayOfMonth);
    }

    public void setClickListener(DatePickerClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private String TAG = "殷宗旺";
    private int margin = 20;
    private DatePickerClickListener clickListener;
    public boolean isMultiSelect = false;
    private long[] selectDateRange;
    private boolean isFirstSelect = true;
    private int dateTextNormalColor, dateTextSelectedColor, dateNormalBG, dateSelectedBG;
    private Drawable selectedBGDrawable;
    private Calendar mCalendar;
    private SimpleDateFormat dateFormat, dayOfMonthFormat;
    private long today, selectedDay, monthDay;
    private int[] ids;
    private int currentIndex;
    private TextView preMonthView, titleView, nextMonthView;
    private LinearLayout dateViewGroup;
    private AnimatorSet ltrAnimator,rtlAnimator,alphaAnimator;
    private GestureDetector gestureDetector;
    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener;

    public MyDatePicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyDatePicker);
        dateTextNormalColor = typedArray.getColor(R.styleable.MyDatePicker_dateTextNormalColor, Color.BLACK);
        dateTextSelectedColor = typedArray.getColor(R.styleable.MyDatePicker_dateTextSelectedColor, Color.WHITE);
        dateNormalBG = typedArray.getColor(R.styleable.MyDatePicker_dateNormalBG, Color.TRANSPARENT);
        dateSelectedBG = typedArray.getColor(R.styleable.MyDatePicker_dateSelectedBG, Color.parseColor("#0e7772"));
        selectedBGDrawable = typedArray.getDrawable(R.styleable.MyDatePicker_dateSelectedDrawable);
        typedArray.recycle();
        initial();
        initialViews();
        setSelectedDate(today);
    }

    public void setSelectedDate(long timeInMillis) {
        if(isMultiSelect)
            return;
        selectedDay = timeInMillis;
        setDateView(timeInMillis);
    }

    public long[] getSelectDateRange() {
        return selectDateRange;
    }

    private void setDateView(long timeInMillis) {
        mCalendar.setTimeInMillis(timeInMillis);
        mCalendar.set(Calendar.DAY_OF_MONTH, 15);
        monthDay = mCalendar.getTimeInMillis();

        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        int week = mCalendar.get(Calendar.DAY_OF_WEEK);
        int diff = week > 1 && week < 4 ? (-5 - week) % 7 - 7 : (-5 - week) % 7;
        mCalendar.add(Calendar.DAY_OF_MONTH, diff);

        for (int i = 0; i < 42; i++) {
            TextView textView = findViewById(ids[i]);
            if (i > 0)
                mCalendar.add(Calendar.DAY_OF_MONTH, 1);
            textView.setTag(mCalendar.getTimeInMillis());
        }
        mCalendar.setTimeInMillis(monthDay);
        titleView.setText(dateFormat.format(monthDay));
        changeDateViewUI();
    }

    private void initial() {
        mCalendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy年M月", Locale.CHINA);
        dayOfMonthFormat = new SimpleDateFormat("d", Locale.CHINA);
        today = mCalendar.getTimeInMillis();
        selectedDay = today;
        mCalendar.set(Calendar.DAY_OF_MONTH, 15);
        monthDay = mCalendar.getTimeInMillis();
        currentIndex = -1;
        selectDateRange = new long[2];
        ids = new int[42];
        for (int i = 0; i < 42; i++) {
            ids[i] = generateViewId();
        }
        simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if(velocityX > 500){
                    preMonthClick();
                }else if(velocityX < -500){
                    nextMonthClick();
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if(distanceX < -30){
                    preMonthClick();
                }else if(distanceX > 30){
                    nextMonthClick();
                }
                return true;
            }
        };
        gestureDetector = new GestureDetector(getContext(),simpleOnGestureListener);
    }

    private void initialViews() {
        setOrientation(VERTICAL);
        removeAllViews();
        addTitleViews();
        addWeekTitleViews();
        addDateViews();
    }

    private void addTitleViews() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(HORIZONTAL);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(margin, margin / 2, margin, margin / 2);

        preMonthView = new TextView(getContext());
        preMonthView.setGravity(Gravity.CENTER);
        preMonthView.setTextSize(20);
        preMonthView.setText("<");
        preMonthView.setTextColor(dateTextNormalColor);
        preMonthView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                preMonthClick();
            }
        });

        titleView = new TextView(getContext());
        titleView.setGravity(Gravity.CENTER);
        titleView.setTextSize(20);
        titleView.setText("");
        titleView.setTextColor(dateTextNormalColor);
        titleView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonthClick();
            }
        });

        nextMonthView = new TextView(getContext());
        nextMonthView.setGravity(Gravity.CENTER);
        nextMonthView.setTextSize(20);
        nextMonthView.setText(">");
        nextMonthView.setTextColor(dateTextNormalColor);
        nextMonthView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMonthClick();
            }
        });

        LayoutParams childLP = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        linearLayout.addView(preMonthView, childLP);
        linearLayout.addView(titleView, new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 2));
        linearLayout.addView(nextMonthView, childLP);

        addView(linearLayout, lp);
    }

    private void addWeekTitleViews() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(margin, 0, margin, 0);
        String[] weeks = {"一", "二", "三", "四", "五", "六", "日"};
        LayoutParams childLP = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        for (int i = 0; i < 7; i++) {
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(16);
            textView.setText(weeks[i]);
            if (i == 5)
                textView.setTextColor(Color.GREEN);
            if (i == 6)
                textView.setTextColor(Color.RED);
            linearLayout.addView(textView, childLP);
        }
        addView(linearLayout, lp);
    }

    private void addDateViews() {
        dateViewGroup = new LinearLayout(getContext());
        dateViewGroup.setOrientation(VERTICAL);
        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMargins(margin, 0, margin, margin/2);
        LayoutParams _lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
        _lp.setMargins(0,0,0,margin/4);
        LayoutParams childLP = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1);
        childLP.setMargins(0, 0, margin / 4, 0);
        LayoutParams child_child_LP = new LayoutParams(120, 120);
        child_child_LP.gravity = Gravity.CENTER;
        int index = 0;
        for (int i = 0; i < 6; i++) {
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(HORIZONTAL);
            for (int j = 0; j < 7; j++) {
                LinearLayout ll = new LinearLayout(getContext());
                final FlagTextView textView = new FlagTextView(getContext());
                textView.setGravity(Gravity.CENTER);
                textView.setTextSize(16);
                final int position = index;
                textView.setId(ids[index++]);
                textView.setTextColor(dateTextNormalColor);
                textView.setBackgroundColor(dateNormalBG);
                textView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isMultiSelect) {
                            dateViewMultiClick(position);
                        } else {
                            dateViewClick(position);
                        }
                    }
                });
                ll.addView(textView, child_child_LP);
                linearLayout.addView(ll, childLP);
            }
            dateViewGroup.addView(linearLayout, _lp);
        }
        addView(dateViewGroup, lp);
    }

    private void startLeftToRightAnimation(){
        if(ltrAnimator == null) {
            ltrAnimator = new AnimatorSet();
            int width = dateViewGroup.getMeasuredWidth();
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(dateViewGroup, "translationX", 0, width);
            objectAnimator1.setDuration(100);
            objectAnimator1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dateViewGroup.setVisibility(INVISIBLE);
                }
            });
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(dateViewGroup, "translationX", 0, -width);
            objectAnimator2.setDuration(10);
            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(dateViewGroup, "translationX", -width, 0);
            objectAnimator3.setDuration(100);
            objectAnimator3.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    setDateView(monthDay);
                    dateViewGroup.setVisibility(VISIBLE);
                }
            });
            ltrAnimator.playSequentially(objectAnimator1, objectAnimator2, objectAnimator3);
        }
        ltrAnimator.start();
    }

    private void startRightToLeftAnimation(){
        if(rtlAnimator == null) {
            rtlAnimator = new AnimatorSet();
            int width = dateViewGroup.getMeasuredWidth();
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(dateViewGroup, "translationX", 0, -width);
            objectAnimator1.setDuration(100);
            objectAnimator1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    dateViewGroup.setVisibility(INVISIBLE);
                }
            });
            ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(dateViewGroup, "translationX", 0, width);
            objectAnimator2.setDuration(10);
            ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(dateViewGroup, "translationX", width, 0);
            objectAnimator3.setDuration(100);
            objectAnimator3.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    setDateView(monthDay);
                    dateViewGroup.setVisibility(VISIBLE);
                }
            });
            rtlAnimator.playSequentially(objectAnimator1, objectAnimator2, objectAnimator3);
        }
        rtlAnimator.start();
    }

    private void startAlphaAnimation(){
        if(alphaAnimator == null) {
            alphaAnimator = new AnimatorSet();
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(dateViewGroup, "alpha", 1f, 0f);
            objectAnimator.setDuration(100);
            ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(dateViewGroup, "alpha", 0f, 1f);
            objectAnimator1.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    setDateView(monthDay);
                }
            });
            objectAnimator1.setDuration(100);
            alphaAnimator.playSequentially(objectAnimator,objectAnimator1);
        }
        alphaAnimator.start();
    }

    private void dateViewMultiClick(int position) {
        TextView textView = findViewById(ids[position]);
        long time = 0;
        if (textView != null) {
            time = (long) textView.getTag();
            if (isFirstSelect || compareDay(selectDateRange[0], time) > 0) {
                isFirstSelect = false;
                selectDateRange[0] = time;
                selectDateRange[1] = time;
            } else {
                isFirstSelect = true;
                selectDateRange[1] = time;
            }
            changeDateViewUI();
        }
    }

    private void changeDateViewUI() {
        for (int i = 0; i < 42; i++) {
            FlagTextView textView = findViewById(ids[i]);
            if (textView != null) {
                long time = (long) textView.getTag();
                String s = dayOfMonthFormat.format(time);
                textView.hideLeftTopFlag();
                textView.hideRightBottomFlag();
                textView.setText(s);
                textView.setBackgroundColor(dateNormalBG);
                if (compareMonth(time, monthDay) != 0) {
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(dateTextNormalColor);
                }
                if (isMultiSelect) {
                    int startDiff = compareDay(time, selectDateRange[0]);
                    int endDiff = compareDay(time, selectDateRange[1]);
                    if(startDiff >= 0 && endDiff <=0){
                        textView.setTextColor(dateTextSelectedColor);
                        if (selectedBGDrawable != null) {
                            textView.setBackground(selectedBGDrawable);
                        } else {
                            textView.setBackgroundColor(dateSelectedBG);
                        }
                        if(startDiff == 0)
                            textView.showLeftTopFlag();
                        if (isFirstSelect && endDiff == 0) {
                            textView.showRightBottomFlag();
                        }
                    }
                } else if (compareDay(time, selectedDay) == 0) {
                    textView.setTextColor(dateTextSelectedColor);
                    if (selectedBGDrawable != null) {
                        textView.setBackground(selectedBGDrawable);
                    } else {
                        textView.setBackgroundColor(dateSelectedBG);
                    }
                    currentIndex = i;
                }
            }
        }
    }

    private void dateViewClick(int position) {
        TextView textView = findViewById(ids[position]);
        long value = (long) textView.getTag();
        selectedDay = value;
        textView.setTextColor(dateTextSelectedColor);
        if (selectedBGDrawable != null) {
            textView.setBackground(selectedBGDrawable);
        } else {
            textView.setBackgroundColor(dateSelectedBG);
        }

        mCalendar.setTimeInMillis(value);
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int diff = compareMonth(value, monthDay);
        if (diff == 0) {
            if (currentIndex >= 0 && currentIndex < 42) {
                TextView oldTV = findViewById(ids[currentIndex]);
                if (oldTV != null) {
                    oldTV.setTextColor(dateTextNormalColor);
                    oldTV.setBackgroundColor(dateNormalBG);
                }
                currentIndex = position;
            }
        } else if (diff > 0) {
            nextMonthClick();
        } else {
            preMonthClick();
        }

        if (clickListener != null) {
            clickListener.onClick(year, month, day);
        }
        mCalendar.setTimeInMillis(monthDay);
    }

    private void preMonthClick() {
        mCalendar.add(Calendar.MONTH, -1);
        mCalendar.set(Calendar.DAY_OF_MONTH, 15);
        monthDay = mCalendar.getTimeInMillis();
        startLeftToRightAnimation();
    }

    private void currentMonthClick() {
        mCalendar.setTimeInMillis(today);
        mCalendar.set(Calendar.DAY_OF_MONTH, 15);
        monthDay = mCalendar.getTimeInMillis();
        startAlphaAnimation();
    }

    private void nextMonthClick() {
        mCalendar.add(Calendar.MONTH, 1);
        mCalendar.set(Calendar.DAY_OF_MONTH, 15);
        monthDay = mCalendar.getTimeInMillis();
        startRightToLeftAnimation();
    }

    private int compareMonth(long d1, long d2) {
        mCalendar.setTimeInMillis(d1);
        int y1 = mCalendar.get(Calendar.YEAR);
        int m1 = mCalendar.get(Calendar.MONTH);
        mCalendar.setTimeInMillis(d2);
        int y2 = mCalendar.get(Calendar.YEAR);
        int m2 = mCalendar.get(Calendar.MONTH);
        mCalendar.setTimeInMillis(monthDay);
        if (y1 == y2) {
            return Integer.compare(m1, m2);
        }
        return Integer.compare(y1, y2);
    }

    private int compareDay(long d1, long d2) {
        int i = compareMonth(d1, d2);
        if (i == 0) {
            mCalendar.setTimeInMillis(d1);
            int day1 = mCalendar.get(Calendar.DAY_OF_MONTH);
            mCalendar.setTimeInMillis(d2);
            int day2 = mCalendar.get(Calendar.DAY_OF_MONTH);
            mCalendar.setTimeInMillis(monthDay);
            return Integer.compare(day1, day2);
        }
        return i;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return gestureDetector.onTouchEvent(ev);
    }

}

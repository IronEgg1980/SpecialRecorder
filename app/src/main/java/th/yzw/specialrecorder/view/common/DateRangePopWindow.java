package th.yzw.specialrecorder.view.common;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;

public class DateRangePopWindow extends PopupWindow {
    private OnSelectDateRangeDismiss onSelectDateRangeDismiss;
    private Calendar calendar;
    private TextView[] quickSelectTextViews = new TextView[3];
//    private DatePicker startDatePicker, endDatePicker;
    private MyDatePicker datePicker;
    private long startDay, endDay;
    private boolean isConfirm = false;
    private int[] textViewId = {R.id.month1, R.id.month2, R.id.month3};
    private int[]  y = new int[3], m = new int[3],max = new int[3];

    private void initialView(View view) {
//        startDatePicker = view.findViewById(R.id.start_date);
//        startDatePicker.setMaxDate(calendar.getTimeInMillis());
//        startDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
//        endDatePicker = view.findViewById(R.id.end_date);
//        endDatePicker.setMaxDate(calendar.getTimeInMillis());
        datePicker = view.findViewById(R.id.date);
        datePicker.isMultiSelect = true;
        TextView cancel = view.findViewById(R.id.cancel);
        TextView confirm = view.findViewById(R.id.confirm);
        for (int i = 0; i < 3; i++) {
            quickSelectTextViews[i] = view.findViewById(textViewId[i]);
            y[i] = calendar.get(Calendar.YEAR);
            m[i] = calendar.get(Calendar.MONTH);
            max[i] = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (i == 0)
                max[0] = calendar.get(Calendar.DAY_OF_MONTH);
            String s = y[i] + "年" + (m[i] + 1) + "月";
            quickSelectTextViews[i].setText(s);
            final int finalI = i;
            quickSelectTextViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calendar.set(y[finalI], m[finalI], 1, 0, 0, 0);
                    startDay = calendar.getTimeInMillis();
                    calendar.set(y[finalI], m[finalI], max[finalI], 23, 59, 59);
                    endDay = calendar.getTimeInMillis();
                    isConfirm = true;
                    dismiss();
                }
            });
            calendar.add(Calendar.MONTH, -1);
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConfirm = false;
                dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConfirm = true;
                getDate();
                dismiss();
            }
        });
    }

    private void getDate() {
        long[] times = datePicker.getSelectDateRange();
        startDay = times[0];
        endDay = times[1];
//        calendar.set(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth(), 0, 0, 0);
//        startDay = calendar.getTimeInMillis();
//        calendar.set(endDatePicker.getYear(), endDatePicker.getMonth(), endDatePicker.getDayOfMonth(), 0, 0, 0);
//        endDay = calendar.getTimeInMillis();
//        if (startDay > endDay) {
//            startDay = endDay;
//            calendar.set(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth(), 23, 59, 59);
//            endDay = calendar.getTimeInMillis();
//        } else {
//            calendar.set(endDatePicker.getYear(), endDatePicker.getMonth(), endDatePicker.getDayOfMonth(), 23, 59, 59);
//            endDay = calendar.getTimeInMillis();
//        }
    }

    public DateRangePopWindow(Activity activity){
        this.calendar = new GregorianCalendar(Locale.CHINA);
        View view = LayoutInflater.from(activity).inflate(R.layout.select_startdate_endddate,null);
        initialView(view);
        setContentView(view);
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.DateRangePopWindowAnim);
//        setOnDismissListener(new OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                darkenBackground(1f);
//            }
//        });
    }

    @Override
    public void dismiss() {
        if(onSelectDateRangeDismiss != null)
            onSelectDateRangeDismiss.onDissmiss(isConfirm, startDay, endDay);
        super.dismiss();
    }

    public void show(View parent, OnSelectDateRangeDismiss onSelectDateRangeDismiss){
        this.onSelectDateRangeDismiss = onSelectDateRangeDismiss;
        int[] location = new int[2];
        parent.getLocationOnScreen(location);
//        showAsDropDown(parent,0,0);
        showAtLocation(parent,Gravity.NO_GRAVITY,location[0],location[1]);
    }
//    private void darkenBackground(Float bgcolor) {
//        if(mActivity == null)
//            return;
//        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
//        lp.alpha = bgcolor;
//        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        mActivity.getWindow().setAttributes(lp);
//    }
}

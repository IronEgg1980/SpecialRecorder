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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;

public class DateRangePopWindow extends PopupWindow {
    private OnSelectDateRangeDismiss onSelectDateRangeDismiss;
    private MyDatePicker datePicker;
    private TextView startDateTV,endDateTV;
    private long startDay, endDay;
    private boolean isConfirm = false;
    private SimpleDateFormat simpleDateFormat;
    private String tips = "请在日历上选择";
    private Activity mActivity;

    private void initialView(View view) {
        simpleDateFormat = new SimpleDateFormat("yyyy-M-d",Locale.CHINA);
        startDateTV = view.findViewById(R.id.startDateTV);
        startDateTV.setText(tips);
        endDateTV = view.findViewById(R.id.endDateTV);
        endDateTV.setText(tips);
        datePicker = view.findViewById(R.id.date);
        datePicker.isMultiSelect = true;
        datePicker.setClickListener(new MyDatePicker.DatePickerClickListener() {
            @Override
            public void onClick(int year, int month, int dayOfMonth) {

            }

            @Override
            public void onMultiClick(boolean isFirstClick) {
                showStartEndDate(isFirstClick);
            }
        });
        view.findViewById(R.id.cancel).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                isConfirm = false;
                dismiss();
            }
        });
        view.findViewById(R.id.confirm).setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                isConfirm = true;
                getDate();
                dismiss();
            }
        });
    }

    private void getDate() {
        long[] times = datePicker.getSelectDateRange();
        Calendar calendar = new GregorianCalendar(Locale.CHINA);
        calendar.setTimeInMillis(times[0]);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        startDay = calendar.getTimeInMillis();

        calendar.setTimeInMillis(times[1]);
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
        calendar.set(Calendar.MILLISECOND,999);
        endDay = calendar.getTimeInMillis();
    }

    private void showStartEndDate(boolean isFirstClick){
        getDate();
        if(isFirstClick){
            startDateTV.setText(simpleDateFormat.format(startDay));
            endDateTV.setText(tips);
        }else{
            startDateTV.setText(simpleDateFormat.format(startDay));
            endDateTV.setText(simpleDateFormat.format(endDay));
        }
    }

    public DateRangePopWindow(Activity activity){
        mActivity = activity;
        View view = LayoutInflater.from(activity).inflate(R.layout.select_startdate_endddate,null);
        initialView(view);
        setContentView(view);
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.DateRangePopWindowAnim);
    }

    @Override
    public void dismiss() {
        if(onSelectDateRangeDismiss != null)
            onSelectDateRangeDismiss.onDissmiss(isConfirm, startDay, endDay);
        super.dismiss();
        darkenBackground(1f);
    }

    public void show(View parent, OnSelectDateRangeDismiss onSelectDateRangeDismiss){
        darkenBackground(0.5f);
        this.onSelectDateRangeDismiss = onSelectDateRangeDismiss;
        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        showAtLocation(parent,Gravity.NO_GRAVITY,location[0],location[1]);
    }
    private void darkenBackground(Float bgcolor) {
        if(mActivity == null)
            return;
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = bgcolor;
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mActivity.getWindow().setAttributes(lp);
    }
}

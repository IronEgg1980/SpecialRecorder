package th.yzw.specialrecorder.view.common;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;

public class DatePopWindow extends PopupWindow {
    private MyDatePicker datePicker;
    private boolean isConfirm = false;
    private long date;
    private Calendar calendar;
    private OnSelectDateRangeDismiss onSelectDateRangeDismiss;

    public DatePopWindow(Activity activity,long date){
        calendar = new GregorianCalendar(Locale.CHINA);
        this.date = date;
        createView(activity);
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.DateRangePopWindowAnim);
    }
    public void show(View parent, OnSelectDateRangeDismiss onSelectDateRangeDismiss){
        this.onSelectDateRangeDismiss = onSelectDateRangeDismiss;
        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        showAtLocation(parent, Gravity.NO_GRAVITY,location[0],location[1]);
        moveDatePickerTitle();
        calendar.setTimeInMillis(date);
        datePicker.setSelectedDate(date);
//        datePicker.updateDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
    }
    private void moveDatePickerTitle(){
        ViewGroup viewGroup = (ViewGroup)datePicker.getChildAt(0);
        int count = viewGroup.getChildCount();
        for(int i = 0;i<count;i++){
            View view = viewGroup.getChildAt(i);
            if(view instanceof LinearLayout)
                view.setVisibility(View.GONE);
        }
    }
    @Override
    public void dismiss() {
        if(onSelectDateRangeDismiss != null)
            onSelectDateRangeDismiss.onDissmiss(isConfirm,date);
        super.dismiss();
    }

    private void createView(Activity activity){
        View view = LayoutInflater.from(activity).inflate(R.layout.select_date,null);
        datePicker = view.findViewById(R.id.date);
        datePicker = view.findViewById(R.id.date);
        datePicker.setClickListener(new MyDatePicker.DatePickerClickListener() {
            @Override
            public void onClick(int year, int month, int dayOfMonth) {
                isConfirm = true;
                calendar.set(year, month, dayOfMonth);
                date = calendar.getTimeInMillis();
                dismiss();
            }
        });
//        datePicker.setMaxDate(calendar.getTimeInMillis());
//        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isConfirm = false;
//                dismiss();
//            }
//        });
//        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isConfirm = true;
//                calendar.set(datePicker.getYear(),datePicker.getMonth(),datePicker.getDayOfMonth());
//                date = calendar.getTimeInMillis();
//                dismiss();
//            }
//        });
        setContentView(view);
    }
}

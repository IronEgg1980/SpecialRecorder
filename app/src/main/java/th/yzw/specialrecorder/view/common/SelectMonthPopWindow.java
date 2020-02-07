package th.yzw.specialrecorder.view.common;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;

public class SelectMonthPopWindow extends PopupWindow {
    private OnSelectDateRangeDismiss onSelectDateRangeDismiss;

    private DatePicker datePicker;
    private long date;
    private Calendar calendar;
    private View mParent;

    public SelectMonthPopWindow(View parent){
        mParent = parent;
        calendar = new GregorianCalendar(Locale.CHINA);

        createView();

        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.DateRangePopWindowAnim);

    }

    public void show(OnSelectDateRangeDismiss onDismiss){
        this.onSelectDateRangeDismiss = onDismiss;
        int[] location = new int[2];
        mParent.getLocationOnScreen(location);
        showAtLocation(mParent, Gravity.NO_GRAVITY,location[0],location[1]);
    }

    private void hideDayNumpicker(DatePicker _datePicker){
        if (_datePicker != null) {
            ViewGroup viewGroup = (ViewGroup) ((ViewGroup) _datePicker.getChildAt(0)).getChildAt(0);
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View view1 = viewGroup.getChildAt(i);
                if (view1 instanceof NumberPicker) {
                    NumberPicker numberPicker = (NumberPicker) view1;
                    int max = numberPicker.getMaxValue();
                    if (max > 11 && max < 32) {
                        view1.setVisibility(View.GONE);
                        break;
                    }
                }
            }
        }
    }

    private void createView(){
        View view = LayoutInflater.from(mParent.getContext()).inflate(R.layout.select_month,null);
        datePicker = view.findViewById(R.id.date);
        hideDayNumpicker(datePicker);
        datePicker.setMaxDate(calendar.getTimeInMillis());
        TextView cancel = view.findViewById(R.id.cancel);
        TextView confirm = view.findViewById(R.id.confirm);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onSelectDateRangeDismiss!=null)
                    onSelectDateRangeDismiss.onDissmiss(false,date);
                dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.set(datePicker.getYear(),datePicker.getMonth(),28,23,59,59);
                date = calendar.getTimeInMillis();
                if(onSelectDateRangeDismiss!=null)
                    onSelectDateRangeDismiss.onDissmiss(true,date);
                dismiss();
            }
        });
        setContentView(view);
    }

}
package th.yzw.specialrecorder.unuse;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;
import th.yzw.specialrecorder.view.common.MyDatePicker;

public class SelectDateRangeDialogFragment extends DialogFragment {
    private OnSelectDateRangeDismiss onSelectDateRangeDismiss;
    private Calendar calendar;
    private TextView[] quickSelectTextViews = new TextView[3];
    private MyDatePicker datePicker;
//    private DatePicker startDatePicker, endDatePicker;
    private long startDay, endDay;
    private boolean isConfirm;
    private int[] textViewId = {R.id.month1, R.id.month2, R.id.month3};
    private int[]  y = new int[3], m = new int[3],max = new int[3];


    public void setOnSelectDateRangeDismiss(OnSelectDateRangeDismiss onSelectDateRangeDismiss) {
        this.onSelectDateRangeDismiss = onSelectDateRangeDismiss;
    }

    private void initialView(View view) {
//        startDatePicker = view.findViewById(R.id.start_date);
//        startDatePicker.setMaxDate(calendar.getTimeInMillis());
//        startDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);
//        endDatePicker = view.findViewById(R.id.end_date);
//        endDatePicker.setMaxDate(calendar.getTimeInMillis());
        datePicker = view.findViewById(R.id.date);
        datePicker.isMultiSelect =true;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = new GregorianCalendar(Locale.CHINA);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_startdate_endddate, container, false);
        initialView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = (int) (dm.widthPixels * 0.85);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.setCanceledOnTouchOutside(false);
            Window window = dialog.getWindow();
            if (window!=null) {
                window.setWindowAnimations(R.style.CommonDialogAnim);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(width, height);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        onSelectDateRangeDismiss.onDissmiss(isConfirm, startDay, endDay);
    }
}

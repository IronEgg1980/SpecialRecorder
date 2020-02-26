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
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;
import th.yzw.specialrecorder.view.common.MyDatePicker;

public class SelectDateDialogFragment extends DialogFragment {
    private MyDatePicker datePicker;
    private boolean isConfirm;
    private long date;
    private Calendar calendar;

    public void setOnSelectDateRangeDismiss(OnSelectDateRangeDismiss onSelectDateRangeDismiss) {
        this.onSelectDateRangeDismiss = onSelectDateRangeDismiss;
    }

    private OnSelectDateRangeDismiss onSelectDateRangeDismiss;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = new GregorianCalendar(Locale.CHINA);
        isConfirm = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_date,container,false);
        datePicker = view.findViewById(R.id.date);
        datePicker.setClickListener(new MyDatePicker.DatePickerClickListener() {
            @Override
            public void onClick(int year, int month, int dayOfMonth) {
                isConfirm = true;
                calendar.set(year, month, dayOfMonth);
                date = calendar.getTimeInMillis();
                dismiss();
            }

            @Override
            public void onMultiClick(boolean isFirstClick) {

            }
        });
//        datePicker.setMaxDate(calendar.getTimeInMillis());
//        TextView cancel = view.findViewById(R.id.cancel);
//        TextView confirm = view.findViewById(R.id.confirm);
//        cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isConfirm = false;
//                dismiss();
//            }
//        });
//        confirm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isConfirm = true;
//                calendar.set(datePicker.getYear(),datePicker.getMonth(),datePicker.getDayOfMonth());
//                date = calendar.getTimeInMillis();
//                dismiss();
//            }
//        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = (int) (dm.widthPixels * 0.9);
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            dialog.setCanceledOnTouchOutside(false);
            Window window = dialog.getWindow();
            if(window!=null) {
                window.setWindowAnimations(R.style.CommonDialogAnim);
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                window.setLayout(width, height);
            }
            ViewGroup  viewGroup = (ViewGroup)datePicker.getChildAt(0);
            int count = viewGroup.getChildCount();
            for(int i = 0;i<count;i++){
                View view = viewGroup.getChildAt(i);
                if(view instanceof LinearLayout)
                    view.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        onSelectDateRangeDismiss.onDissmiss(isConfirm,date);
    }
}

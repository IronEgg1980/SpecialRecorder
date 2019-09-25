package th.yzw.specialrecorder.view.common;

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
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;

public class SelectMonthDialogFragment extends DialogFragment {
    private OnSelectDateRangeDismiss onSelectDateRangeDismiss;

    public void setOnSelectDateRangeDismiss(OnSelectDateRangeDismiss onSelectDateRangeDismiss) {
        this.onSelectDateRangeDismiss = onSelectDateRangeDismiss;
    }

    private DatePicker datePicker;
    private boolean isConfirm;
    private long date;
    private Calendar calendar;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = new GregorianCalendar(Locale.CHINA);
        isConfirm = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_month,container,false);
        datePicker = view.findViewById(R.id.date);
        hideDayNumpicker(datePicker);
        datePicker.setMaxDate(calendar.getTimeInMillis());
        TextView cancel = view.findViewById(R.id.cancel);
        TextView confirm = view.findViewById(R.id.confirm);
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
                calendar.set(datePicker.getYear(),datePicker.getMonth(),28,23,59,59);
                date = calendar.getTimeInMillis();
                dismiss();
            }
        });
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
                window.setLayout(width,height);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        onSelectDateRangeDismiss.onDissmiss(isConfirm,date);
    }
}

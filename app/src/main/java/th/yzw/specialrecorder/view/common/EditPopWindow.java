package th.yzw.specialrecorder.view.common;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.model.RecordEntity;

public class EditPopWindow extends PopupWindow {
    private Context mContext;
    private String mName;
    private int mCount;
    private int[] ids = {R.id.num0_IV, R.id.num1_IV, R.id.num2_IV, R.id.num3_IV, R.id.num4_IV,
            R.id.num5_IV, R.id.num6_IV, R.id.num7_IV, R.id.num8_IV, R.id.num9_IV};
    private boolean isFirstClick = true;
    private StringBuilder stringBuilder;
    private IDialogDismiss dialogDismiss;
    private TextView numTextView, infoTextView;

    public EditPopWindow(Context context, String name, int count) {
        this.mContext = context;
        this.mName = name;
        this.mCount = count;
        View view = createView();
        stringBuilder = new StringBuilder();
        stringBuilder.append(mCount);

        setContentView(view);
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setAnimationStyle(R.style.PopWindowAnim);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private View createView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.popwindow_edit_layout, null);
        TextView nameTextView = view.findViewById(R.id.record_name_TV);
        nameTextView.setText(mName);
        numTextView = view.findViewById(R.id.record_count_TV);
        infoTextView = view.findViewById(R.id.info_TV);
        for (int i = 0; i < 10; i++) {
            int id = ids[i];
            final int finalI = i;
            view.findViewById(id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    numClick(finalI);
                }
            });
        }
        view.findViewById(R.id.backspace_IV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backspaceClick(numTextView, infoTextView);
            }
        });
        view.findViewById(R.id.cancel_IV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogDismiss != null) {
                    dialogDismiss.onDismiss(false);
                    dismiss();
                }
            }
        });
        view.findViewById(R.id.ok_IV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(numTextView.getText())) {
                    infoTextView.setText("请输入有效数字");
                    return;
                }
                String count_s = numTextView.getText().toString().trim();
                int count = 0;
                try {
                    count = Integer.parseInt(count_s);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                if (count == 0) {
                    infoTextView.setText("请输入大于 0 的数字");
                    return;
                }
                if (count == mCount) {
                    dialogDismiss.onDismiss(false);
                } else {
                    dialogDismiss.onDismiss(true, count);
                }
                dismiss();
            }
        });
        return view;
    }

    private void numClick(int num) {
        if (isFirstClick) {
            isFirstClick = false;
            stringBuilder = new StringBuilder();
        }
        if(num != 0 || stringBuilder.length() > 0) {
            stringBuilder.append(num);
        }
        showInfo();
    }

    private void showInfo() {
        infoTextView.setText("");
        if (stringBuilder.length() > 5) {
            infoTextView.setText("最多输入5位数字");
            stringBuilder.deleteCharAt(5);
        }
        numTextView.setText(stringBuilder);
    }

    private void backspaceClick(TextView numTextView, TextView infoTextview) {
        if (isFirstClick) {
            isFirstClick = false;
            stringBuilder = new StringBuilder();
        }
        if(stringBuilder.length() > 0)
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        showInfo();
    }

    public void show(View parent, IDialogDismiss dialogDismiss) {
        this.dialogDismiss = dialogDismiss;
        showInfo();
        showAtLocation(parent, Gravity.BOTTOM, 0, 0);
    }
}

package th.yzw.specialrecorder.unuse;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.ItemNameOperator;
import th.yzw.specialrecorder.DAO.ItemStatisticalInformationOperator;
import th.yzw.specialrecorder.DAO.RecordEntityOperator;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;
import th.yzw.specialrecorder.interfaces.SelectDialogClicker;
import th.yzw.specialrecorder.model.ItemName;
import th.yzw.specialrecorder.view.RecorderActivity;
import th.yzw.specialrecorder.view.common.DatePopWindow;
import th.yzw.specialrecorder.view.common.DialogFactory;
import th.yzw.specialrecorder.view.common.ToastFactory;
import th.yzw.specialrecorder.view.input_data.MyPopWindow3;
import th.yzw.specialrecorder.view.input_data.MyPopupWin2;
import th.yzw.specialrecorder.view.input_data.TouchInputDataFragment;

public class InputDataFragment extends Fragment {
    private long date;
    private TextView dateTextView;
    private TextView name,selectDate;
    private EditText count;
    private SimpleDateFormat dateformat;
    private MyPopupWin2 popupWin2;
    private MyPopWindow3 popWindow3;
    private int showInfoMode;
    private InputMethodManager imm;
    private int currentIndex;
    private String[] itemNameList;
    private TranslateAnimation translateAnimation;
    private DialogFactory dialog;
    private ToastFactory toast;
    private int yOffset;
    private List<ItemName> list;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecorderActivity activity = (RecorderActivity) getActivity();
        activity.setTitle("首页");
        setHasOptionsMenu(true);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        currentIndex = -1;
        list = new ArrayList<>();
        itemNameList = ItemNameOperator.getItemNameList(list);
        showInfoMode = AppSetupOperator.getShowInformationMode();
        date = System.currentTimeMillis();
        dateformat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        initialAnimation();
        dialog = new DialogFactory(getContext());
        toast = new ToastFactory(getContext());
        yOffset = -70;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_layout, container, false);
        dateTextView = view.findViewById(R.id.add_activity_dateTextView);
        selectDate = view.findViewById(R.id.changeDate);
        dateTextView.setFocusable(true);
        dateTextView.setText(dateformat.format(date));
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate(v);
            }
        });
        name = view.findViewById(R.id.add_activity_name);
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectNameDialog(v);
            }
        });
        count = view.findViewById(R.id.add_activity_editTextView);
        view.findViewById(R.id.add_activity_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm(v);
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        count.requestFocus();
        imm.showSoftInput(count, 1);
    }

    private void initialAnimation() {
        translateAnimation = new TranslateAnimation(-10f,10f,0,0);
        translateAnimation.setInterpolator(new OvershootInterpolator());
        translateAnimation.setDuration(50);
        translateAnimation.setRepeatMode(Animation.RESTART);
        translateAnimation.setRepeatCount(2);
    }

    private void showSelectNameDialog(View view) {
        dialog.showSingleSelectSaveIndex(itemNameList, currentIndex,new SelectDialogClicker() {
            @Override
            public void click(int checkedItem) {
                currentIndex = checkedItem;
                name.setTextColor(Color.BLACK);
                name.setText(itemNameList[checkedItem]);
                count.requestFocus();
                count.selectAll();
                // 打开键盘
                imm.showSoftInput(count, 1);
            }
        });

//        dialogAndToast.showSingleSelect(itemNameList, currentIndex, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                currentIndex = which;
//                dialog.dismiss();
//                name.setText(itemNameList[which]);
//                count.requestFocus();
//                count.selectAll();
//                // 打开键盘
//                imm.showSoftInput(count, 1);
//            }
//        });
//                .setTitle("请选择")
//                .setIcon(R.drawable.ic_info_cyan_800_18dp)
//                .setSingleChoiceItems(itemNameList, currentIndex, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
//                .setNegativeButton("关闭", null)
//                .create();
    }

    private void closePopWin() {
        if (popupWin2 != null && popupWin2.isShowing())
            popupWin2.dismiss2();
        if (popWindow3 != null && popWindow3.isShowing())
            popWindow3.dismiss2();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.toolbar_to_touchuinput, menu);
    }

    @Override
    public void onPause() {
        super.onPause();
        closePopWin();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.to_touch_input) {
            TouchInputDataFragment fragment = new TouchInputDataFragment();
            getFragmentManager().beginTransaction().replace(R.id.framelayout, fragment).commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showInfoCenter(View view, String content) {
        if (popupWin2 == null) {
            popupWin2 = new MyPopupWin2(getContext());
            popupWin2.setWidth(view.getMeasuredWidth());
            popupWin2.setTextSize(20);
            popupWin2.setDuration(5000);
        }
        popupWin2.setContent(content);
        if (!popupWin2.isShowing()) {
            popupWin2.showAtLocation(view, Gravity.TOP, 0, 0);
        }
    }

    private void showPop3(View view, String content) {
        if (popWindow3 == null) {
            popWindow3 = new MyPopWindow3(view.getContext());
            popWindow3.setDuration(2000);
        }
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        int spc = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popWindow3.changeText(content);
        View view1 = popWindow3.getContentView();
        view1.measure(spc, spc);
        int x = xy[0] + (view.getMeasuredWidth() - view1.getMeasuredWidth()) / 2;
        int y = xy[1] - view1.getMeasuredHeight();
        if (!popWindow3.isShowing())
            popWindow3.showAtLocation(view, Gravity.NO_GRAVITY, x, y);

    }

//    private void showToast(String content) {
//        Toast toast = Toast.makeText(getContext(), content, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.TOP, 0, 0);
//        toast.show();
//    }
    private boolean isInputError(){
        if (currentIndex < 0) {
            new ToastFactory(getContext()).showCenterToast("请选择项目",0,yOffset);
            name.setTextColor(Color.RED);
            name.startAnimation(translateAnimation);
            return true;
        }
        if (TextUtils.isEmpty(count.getText())) {
            count.setError("请输入数量！");
            count.requestFocus();
            return true;
        }
        final int _count = Integer.valueOf(count.getText().toString());
        if (_count == 0) {
            count.setError("数量不能为0");
            count.requestFocus();
            return true;
        }
        return false;
    }

    public void confirm(View view) {
        if(isInputError())
            return;
        int _count = Integer.valueOf(count.getText().toString());
        ItemName itemName = list.get(currentIndex);
        final String _name = itemName.getName();
        RecordEntityOperator.saveOrMergeByDateAndCount(date,_name,_count);
//        ItemName itemName = ItemNameOperator.findSingle(_name);
        ItemStatisticalInformationOperator.saveItemStatisticalInformation(itemName,_count);
        count.setText("");
        String s = _name + "    +" + _count;
        if (showInfoMode == 1) {
            showPop3(view, s);
        } else if (showInfoMode == 2) {
            showInfoCenter(view, s);
        } else {
            toast.showCenterToast(s,0,yOffset);
        }
    }

    private void selectDate(View view) {
        dateTextView.requestFocus();
        DatePopWindow datePopWindow = new DatePopWindow(getActivity(),date);
        datePopWindow.show(selectDate, new OnSelectDateRangeDismiss() {
            @Override
            public void onDissmiss(boolean isConfirm, long... timeInMillis) {
                if (isConfirm) {
                    date = timeInMillis[0];
                    dateTextView.setText(dateformat.format(date));
                }
                count.requestFocus();
                count.selectAll();
                // 打开键盘
                imm.showSoftInput(count, 1);
            }
        });
//        SelectDateDialogFragment fragment = new SelectDateDialogFragment();
//        fragment.setOnSelectDateRangeDismiss(new OnSelectDateRangeDismiss() {
//            @Override
//            public void onDissmiss(boolean isConfirm, long... timeInMillis) {
//                if (isConfirm) {
//                    date = timeInMillis[0];
//                    dateTextView.setText(dateformat.format(date));
//                }
//                count.requestFocus();
//                count.selectAll();
//                // 打开键盘
//                imm.showSoftInput(count, 1);
//            }
//
//        });
//        fragment.show(getFragmentManager(), "selectDate");
    }

}

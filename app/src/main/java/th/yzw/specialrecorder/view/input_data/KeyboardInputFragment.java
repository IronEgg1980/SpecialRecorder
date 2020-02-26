package th.yzw.specialrecorder.view.input_data;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.ItemNameOperator;
import th.yzw.specialrecorder.DAO.ItemStatisticalInformationOperator;
import th.yzw.specialrecorder.DAO.RecordEntityOperator;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.model.ItemName;
import th.yzw.specialrecorder.tools.OtherTools;
import th.yzw.specialrecorder.view.RecorderActivity;
import th.yzw.specialrecorder.view.common.DatePopWindow;
import th.yzw.specialrecorder.view.common.EditPopWindow;
import th.yzw.specialrecorder.view.common.FlowLayout;
import th.yzw.specialrecorder.view.common.ToastFactory;

public class KeyboardInputFragment extends Fragment {
    private String TAG = "殷宗旺";

    private long date;
    private TextView dateTextView, selectDate;
    private SimpleDateFormat dateformat;
    private MyPopupWin2 popupWin2;
    private MyPopWindow3 popWindow3;
    private int showInfoMode;
    private int currentIndex;
    private TranslateAnimation translateAnimation;
    private List<ItemName> list;
    private ScrollView scrollView;
    private FlowLayout flowLayout;
    private View clickedView;
    private String infoMessage;
    private EditPopWindow editPopWindow;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecorderActivity activity = (RecorderActivity) getActivity();
        setHasOptionsMenu(true);
        currentIndex = -1;
        list = ItemNameOperator.findAll(true);
        showInfoMode = AppSetupOperator.getShowInformationMode();
        date = System.currentTimeMillis();
        dateformat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        editPopWindow = new EditPopWindow(getActivity(),false);
        editPopWindow.setDialogDismiss(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if(result == Result.OK){
                    confirm((Integer) values[0]);
                }
            }
        });
        initialAnimation();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.keyboard_input_layout, container, false);
        scrollView = view.findViewById(R.id.recyclerviewGroup);
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                hidePop3();
            }
        });
        flowLayout = view.findViewById(R.id.flowLayout);
        flowLayout.setTextViewMargin(OtherTools.dip2px(getContext(),6));
        flowLayout.setOnItemClickListenr(new MyClickListener() {
            @Override
            public void OnClick(View view, Object o) {
                clickedView = view;
                itemNameClick((Integer) o);
            }
        });
        flowLayout.setDataSource(list);
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
        return view;
    }

    private void itemNameClick(int position) {
        ItemName itemName = list.get(position);
        currentIndex = position;
        hidePop3();
        editPopWindow.setData(itemName.getName(),1).show();
    }



    private void initialAnimation() {
        translateAnimation = new TranslateAnimation(-20f, 20f, 0, 0);
        translateAnimation.setInterpolator(new OvershootInterpolator());
        translateAnimation.setDuration(50);
        translateAnimation.setRepeatMode(Animation.RESTART);
        translateAnimation.setRepeatCount(3);
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
        if (view == null)
            return;
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

    private void showPop3() {
        if (clickedView == null)
            return;
        if (popWindow3 == null) {
            popWindow3 = new MyPopWindow3(getContext());
            popWindow3.setDuration(2000);
        }
        int[] xy = new int[2];
        clickedView.getLocationOnScreen(xy);
        int spc = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popWindow3.changeText(infoMessage);
        View view1 = popWindow3.getContentView();
        view1.measure(spc, spc);
        int x = xy[0] + (clickedView.getMeasuredWidth() - view1.getMeasuredWidth()) / 2;
        int y = xy[1] - view1.getMeasuredHeight();
        if (!popWindow3.isShowing())
            popWindow3.showAtLocation(clickedView, Gravity.NO_GRAVITY, x, y);

    }

    private void hidePop3(){
        if(popWindow3!=null && popWindow3.isShowing()){
            popWindow3.dismiss2();
        }
    }

    public void confirm(int count) {
        ItemName itemName = list.get(currentIndex);
        final String _name = itemName.getName();
        RecordEntityOperator.saveOrMergeByDateAndCount(date, _name, count);
        ItemStatisticalInformationOperator.saveItemStatisticalInformation(itemName, count);
        if (showInfoMode == 1) {
            infoMessage = "+" + count;
            showPop3();
        }else {
            infoMessage = _name + "    +" + count;
            if(showInfoMode == 2)
                showInfoCenter(getActivity().getWindow().getDecorView(),infoMessage);
            else
                new ToastFactory(getActivity()).showCenterToast(infoMessage);
        }
    }

    private void selectDate(View view) {
        dateTextView.requestFocus();
        DatePopWindow datePopWindow = new DatePopWindow(getActivity(), date);
        datePopWindow.show(selectDate, new OnSelectDateRangeDismiss() {
            @Override
            public void onDissmiss(boolean isConfirm, long... timeInMillis) {
                if (isConfirm) {
                    date = timeInMillis[0];
                    dateTextView.setText(dateformat.format(date));
                }
            }
        });
    }

}

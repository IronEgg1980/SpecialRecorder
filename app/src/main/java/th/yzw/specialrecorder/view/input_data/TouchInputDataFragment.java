package th.yzw.specialrecorder.view.input_data;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.ItemNameOperator;
import th.yzw.specialrecorder.DAO.ItemStatisticalInformationOperator;
import th.yzw.specialrecorder.DAO.RecordEntityOperator;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.interfaces.OnIndexBarPressedListener;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;
import th.yzw.specialrecorder.view.RecorderActivity;
import th.yzw.specialrecorder.view.common.DatePopWindow;
import th.yzw.specialrecorder.view.common.SideIndexBarView;
import th.yzw.specialrecorder.view.common.ToastFactory;

public class TouchInputDataFragment extends Fragment {
    TouchInputAdapter adapter;
    SimpleDateFormat dateFormat;
    long date;
    TextView dateTextView, textView, selectDate;
    int count;
    String currentName, selectName;
    RecyclerView recyclerView;
    RadioGroup radioGroup;
    RadioButton radioButton1;
    int spanCount, vibrateLevel;
    Vibrator vibrator;
    int showInfoMode;
    RecorderActivity activity;
    boolean setVibrateMode;
    MyPopupWin2 popupWin2;
    long firstTouchTime;
    MyPopWindow3 popWindow3;
    boolean isReTouch;
    int pop3X, pop3Y;
    GridLayoutManager gridLayoutManager;
    SideIndexBarView sideBarIndex;
    TextView toastTV;

    private void itemClick(View view) {
        selectName = adapter.getCurrentItem().getName();
        long secondTime = (new GregorianCalendar()).getTimeInMillis();
        if ((secondTime - firstTouchTime) < 2000 && currentName.equals(selectName)) {
            count++;
            isReTouch = false;
        } else {
            count = 1;
            isReTouch = true;
        }
        firstTouchTime = secondTime;
        RecordEntityOperator.saveOrMergeByDateAndCount(date, selectName, 1);
        ItemStatisticalInformationOperator.saveItemStatisticalInformation(adapter.getCurrentItem(), 1, isReTouch);
        if (setVibrateMode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createOneShot(vibrateLevel, 125);
                vibrator.vibrate(effect);
            } else {
                vibrator.vibrate(vibrateLevel);
            }
        }
        if (showInfoMode == 1) {
            showPop3(view);
        } else if (showInfoMode == 2) {
            showInfoCenter(textView);
        } else {
            showToast(view);
        }
        currentName = selectName;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (RecorderActivity) getActivity();
        setHasOptionsMenu(true);
        vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        showInfoMode = AppSetupOperator.getShowInformationMode();
        vibrateLevel = AppSetupOperator.getVibrateLevel();
        setVibrateMode = AppSetupOperator.isUseVibrate();
        spanCount = AppSetupOperator.getSpanCount();
        count = 0;
        currentName = "";
        selectName = "";
        date = System.currentTimeMillis();
        firstTouchTime = System.currentTimeMillis();
        dateFormat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        adapter = new TouchInputAdapter(getResources().getColor(R.color.textColor));
        adapter.setMyClickListener(new MyClickListener() {
            @Override
            public void OnClick(View view, Object o) {
                itemClick(view);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.touch_input_fragment_layout, container, false);
        textView = view.findViewById(R.id.textView6);
        radioButton1 = view.findViewById(R.id.isOftenUse_radio);
        radioButton1.setChecked(true);
        radioGroup = view.findViewById(R.id.addActivity2RadioGroup);
        if (AppSetupOperator.getShowGroupButtonStatus())
            radioGroup.setVisibility(View.VISIBLE);
        else
            radioGroup.setVisibility(View.GONE);
        dateTextView = view.findViewById(R.id.add_activity2_dateTextView);
        selectDate = view.findViewById(R.id.changeDate);
        dateTextView.setText(dateFormat.format(date));
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDate(v);
            }
        });
        initialItemNameRecyclerView(view);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.isOftenUse_radio) {
                    adapter.update(true);
                } else {
                    adapter.update(false);
                }
                adapter.notifyDataSetChanged();
                if (adapter.getItemCount() > 0)
                    recyclerView.smoothScrollToPosition(0);
            }
        });
        toastTV = view.findViewById(R.id.indexToastTV);
        initialSideBar(view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.toolbar_to_input, menu);
    }

    private void initialItemNameRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.add_activity2_recycler);
        gridLayoutManager = new GridLayoutManager(getContext(), spanCount);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollRecyclerView(recyclerView, dx, dy);
            }
        });
    }

    private void initialSideBar(View view) {
        sideBarIndex = view.findViewById(R.id.sideBar);
        if (!AppSetupOperator.getShowGroupButtonStatus()) {
            String[] letters = ItemNameOperator.getItemNameFirstLetters();
            sideBarIndex.setLetters(letters);
            sideBarIndex.setPressedListener(new OnIndexBarPressedListener() {
                @Override
                public void onIndexBarPressed(int index, String text) {
                    toastTV.setText(text);
                    toastTV.setVisibility(View.VISIBLE);
                    for (int k = 0; k < adapter.getItemCount(); k++) {
                        String name = adapter.getItemsList().get(k).getName();
                        if (name.startsWith(text.toLowerCase())) {
                            // 滚动指定的项目到顶部可见位置
                            gridLayoutManager.scrollToPositionWithOffset(k,0);
                            break;
                        }
                    }
                }

                @Override
                public void onMoutionEventEnd() {
                    toastTV.setVisibility(View.GONE);
                }
            });
        }
    }

    private void scrollRecyclerView(RecyclerView recyclerView, int dx, int dy) {
        if (popWindow3 != null && popWindow3.isShowing()) {
            int position = adapter.getPosition();
            int[] temp = getFirstAndLastIndex(recyclerView);
            if (position < temp[0] || position > temp[1]) {
                popWindow3.dismiss2();
                return;
            }
            pop3X = pop3X - dx;
            pop3Y = pop3Y - dy;
            popWindow3.update(pop3X, pop3Y, -1, -1);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (popupWin2 != null && popupWin2.isShowing())
            popupWin2.dismiss2();
        if (popWindow3 != null && popWindow3.isShowing())
            popWindow3.dismiss2();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.to_keyboad_input) {
            Fragment fragment = new KeyboardInputFragment();
            getFragmentManager().beginTransaction().replace(R.id.framelayout, fragment).commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int[] getFirstAndLastIndex(RecyclerView recyclerView) {
        int[] temp = new int[2];
        GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
        temp[0] = manager.findFirstVisibleItemPosition();
        temp[1] = manager.findLastVisibleItemPosition();
        return temp;
    }

    private void showInfoCenter(View view) {
        if (popupWin2 == null) {
            popupWin2 = new MyPopupWin2(getContext());
            popupWin2.setWidth(view.getMeasuredWidth() * 4 / 5);
            popupWin2.setTextSize(16);
            popupWin2.setDuration(2000);
        }
        popupWin2.setContent(selectName + "  +" + count);
        if (!popupWin2.isShowing()) {
            popupWin2.showAtLocation(view, Gravity.TOP, 0, 0);
        }
    }

    private void showPop3(View view) {
        if (popWindow3 == null) {
            popWindow3 = new MyPopWindow3(view.getContext());
            popWindow3.setDuration(800);
        }
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        int spc = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popWindow3.changeText(selectName + "  +" + count);
        View view1 = popWindow3.getContentView();
        view1.measure(spc, spc);
        int x = xy[0] + (view.getMeasuredWidth() - view1.getMeasuredWidth()) / 2;
        int y = xy[1] - view1.getMeasuredHeight();
        pop3X = x;
        pop3Y = y;
        if (popWindow3.isShowing() && isReTouch) {
            popWindow3.dismiss2();
        }
        if (!popWindow3.isShowing())
            popWindow3.showAtLocation(view, Gravity.NO_GRAVITY, x, y);

    }

    private void showToast(View view) {
        new ToastFactory(getContext()).showTopToast(selectName + "  +" + count);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int i = adapter.getPosition();
        switch (item.getItemId()) {
            case 1:
                adapter.setOftenUse(false);
                break;
            case 2:
                adapter.setOftenUse(true);
                break;
        }
        adapter.notifyItemRemoved(i);
        adapter.notifyItemRangeChanged(i, adapter.getItemCount() - i);
        return true;
    }

    public void selectDate(View view) {
        DatePopWindow datePopWindow = new DatePopWindow(activity,date);
        datePopWindow.show(selectDate, new OnSelectDateRangeDismiss() {
            @Override
            public void onDissmiss(boolean isConfirm, long... timeInMillis) {
                if(isConfirm){
                    date = timeInMillis[0];
                    dateTextView.setText(dateFormat.format(date));
                }
            }
        });

//        SelectDateDialogFragment fragment = new SelectDateDialogFragment();
//        fragment.setOnSelectDateRangeDismiss(new OnSelectDateRangeDismiss() {
//            @Override
//            public void onDissmiss(boolean isConfirm, long... timeInMillis) {
//                if (isConfirm) {
//                    date = timeInMillis[0];
//                    dateTextView.setText(dateFormat.format(date));
//                }
//            }
//        });
//        fragment.show(getFragmentManager(), "selectDate");
    }
}

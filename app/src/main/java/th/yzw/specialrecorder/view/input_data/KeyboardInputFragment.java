package th.yzw.specialrecorder.view.input_data;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.ItemNameOperator;
import th.yzw.specialrecorder.DAO.ItemStatisticalInformationOperator;
import th.yzw.specialrecorder.DAO.RecordEntityOperator;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.interfaces.OnIndexBarPressedListener;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;
import th.yzw.specialrecorder.model.ItemName;
import th.yzw.specialrecorder.view.RecorderActivity;
import th.yzw.specialrecorder.view.common.DatePopWindow;
import th.yzw.specialrecorder.view.common.SideIndexBarView;
import th.yzw.specialrecorder.view.common.ToastFactory;

public class KeyboardInputFragment extends Fragment {
    private long date;
    private RecyclerView recyclerView;
    private RelativeLayout keyboardGroup;
    private int[] ids = {R.id.num0_IV, R.id.num1_IV, R.id.num2_IV, R.id.num3_IV, R.id.num4_IV,
            R.id.num5_IV, R.id.num6_IV, R.id.num7_IV, R.id.num8_IV, R.id.num9_IV};
    private ImageView[] numIVs = new ImageView[10];
    private TextView dateTextView,nameTextView,selectDate,countTextView,toastTV;
    private SimpleDateFormat dateformat;
    private MyPopupWin2 popupWin2;
    private MyPopWindow3 popWindow3;
    private int showInfoMode;
    private int currentIndex;
    private TranslateAnimation translateAnimation;
    private ToastFactory toast;
    private List<ItemName> list;
    private ItemNameAdapter adapter;
    private StringBuilder stringBuilder;
    private SideIndexBarView sideBarIndex;
    private RelativeLayout recyclerViewGroup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecorderActivity activity = (RecorderActivity) getActivity();
        activity.setTitle("首页");
        setHasOptionsMenu(true);
        currentIndex = -1;
        list = ItemNameOperator.findAll(true);
        adapter = new ItemNameAdapter(list);
        adapter.setClickListener(new MyClickListener() {
            @Override
            public void OnClick(View view, Object o) {
                itemNameClick((int)o);
            }
        });
        showInfoMode = AppSetupOperator.getShowInformationMode();
        date = System.currentTimeMillis();
        dateformat = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
        initialAnimation();
        toast = new ToastFactory(getContext());
        stringBuilder = new StringBuilder();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.keyboard_input_layout, container, false);
        recyclerView = view.findViewById(R.id.keyboad_input_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),3));
        recyclerView.setAdapter(adapter);
        recyclerViewGroup = view.findViewById(R.id.recyclerviewGroup);
        keyboardGroup = view.findViewById(R.id.keyboard_group_relativelayout);
        keyboardGroup.setVisibility(View.GONE);
        for(int i = 0;i<10;i++){
            numIVs[i] = view.findViewById(ids[i]);
            final int finalI = i;
            numIVs[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    numClick(finalI);
                }
            });
        }
        view.findViewById(R.id.backspace_IV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backSpace();
            }
        });
        final LinearLayout linearLayout = view.findViewById(R.id.name_group);
        view.findViewById(R.id.ok_IV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirm(linearLayout);
            }
        });
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
        nameTextView = view.findViewById(R.id.keyboad_input_name_TV);
        nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showItemList();
            }
        });
        countTextView = view.findViewById(R.id.keyboad_input_count_TV);
        countTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentIndex < 0) {
                    nameTextView.setTextColor(Color.RED);
                    nameTextView.setText("请先选择项目！");
                    nameTextView.startAnimation(translateAnimation);
                }else
                    showKeyboard();
            }
        });
        initialSideBar(view);
        return view;
    }

    private void initialSideBar(View view) {
        sideBarIndex = view.findViewById(R.id.sideBar);
        toastTV = view.findViewById(R.id.indexToastTV);
        if (!AppSetupOperator.getShowGroupButtonStatus()) {
            String[] letters = ItemNameOperator.getItemNameFirstLetters();
            sideBarIndex.setLetters(letters);
            sideBarIndex.setPressedListener(new OnIndexBarPressedListener() {
                @Override
                public void onIndexBarPressed(int index, String text) {
                    toastTV.setText(text);
                    toastTV.setVisibility(View.VISIBLE);
                    for (int k = 0; k < adapter.getItemCount(); k++) {
                        String name = list.get(k).getName();
                        if (name.startsWith(text.toLowerCase())) {
                            // 滚动指定的项目到顶部可见位置
                            ((GridLayoutManager)recyclerView.getLayoutManager()).scrollToPositionWithOffset(k,0);
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

    private void showItemList(){
        keyboardGroup.setVisibility(View.GONE);
        recyclerViewGroup.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();
    }

    private void itemNameClick(int position){
        nameTextView.setTextColor(Color.BLACK);
        ItemName itemName = list.get(position);
        nameTextView.setText(itemName.getName());
        currentIndex =adapter.getCurrentIndex();
        showKeyboard();
    }

    private void showKeyboard(){
        keyboardGroup.setVisibility(View.VISIBLE);
        recyclerViewGroup.setVisibility(View.GONE);
    }

    private void backSpace(){
        if(stringBuilder != null && stringBuilder.length() > 0){
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            countTextView.setText(stringBuilder);
        }
    }

    private void initialAnimation() {
        translateAnimation = new TranslateAnimation(-10f,10f,0,0);
        translateAnimation.setInterpolator(new OvershootInterpolator());
        translateAnimation.setDuration(50);
        translateAnimation.setRepeatMode(Animation.RESTART);
        translateAnimation.setRepeatCount(2);
    }

    private void numClick(int num){
        if(stringBuilder == null)
            stringBuilder = new StringBuilder();
        if(stringBuilder.length() == 0 && num == 0){
            return;
        }
        stringBuilder.append(num);
        if(stringBuilder.length() > 5){
            toast.showCenterToast("你确定有这么多数量吗？");
            stringBuilder.deleteCharAt(5);
        }
        countTextView.setTextColor(Color.BLACK);
        countTextView.setText(stringBuilder);
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
    public void onStart() {
        super.onStart();
        showItemList();
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

    private boolean isInputError(){
        if (currentIndex < 0) {
            nameTextView.setTextColor(Color.RED);
            nameTextView.setText("请先选择项目！");
            nameTextView.startAnimation(translateAnimation);
            return true;
        }
        if (stringBuilder == null || stringBuilder.length() == 0) {
            countTextView.setTextColor(Color.RED);
            countTextView.setText("请输入数量！");
            countTextView.startAnimation(translateAnimation);
            return true;
        }
        final int _count = Integer.valueOf(stringBuilder.toString());
        if (_count == 0) {
            countTextView.setTextColor(Color.RED);
            countTextView.setText("数量不能为 0");
            countTextView.startAnimation(translateAnimation);
            return true;
        }
        return false;
    }

    public void confirm(View view) {
        if(isInputError())
            return;
        int _count = Integer.valueOf(stringBuilder.toString());
        ItemName itemName = list.get(currentIndex);
        final String _name = itemName.getName();
        RecordEntityOperator.saveOrMergeByDateAndCount(date,_name,_count);
//        ItemName itemName = ItemNameOperator.findSingle(_name);
        ItemStatisticalInformationOperator.saveItemStatisticalInformation(itemName,_count);
        countTextView.setText("");
        String s = _name + "    +" + _count;
        if (showInfoMode == 1) {
            showPop3(view, s);
        } else if (showInfoMode == 2) {
            showInfoCenter(view, s);
        } else {
            toast.showCenterToast(s,0,0);
        }
        if(stringBuilder.length() > 0)
            stringBuilder.delete(0,stringBuilder.length());
        showItemList();
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
                showItemList();
            }
        });
    }

}

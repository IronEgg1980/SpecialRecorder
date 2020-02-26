package th.yzw.specialrecorder.view.show_details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import th.yzw.specialrecorder.ActivityManager;
import th.yzw.specialrecorder.DAO.ShowDetailsOperator;
import th.yzw.specialrecorder.MyActivity;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.model.ShowDetailsItemFatherEntity;
import th.yzw.specialrecorder.view.RecorderActivity;

public class ShowDetailsActivity extends MyActivity {

    private List<ShowDetailsItemFatherEntity> list;
    private Calendar calendar;
    private TextView nodata;
    private TextSwitcher dateSwitcher;
    private ShowDetailsFatherAdapter adapter;
    private RecyclerView recyclerView;
    private SimpleDateFormat format;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_details_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.back2);
        setTitle("查看/修改数据");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initialView();
    }

    protected void updateList() {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.clear();
        list.addAll(ShowDetailsOperator.getDetailsFatherList(calendar.getTimeInMillis()));
        if (list.isEmpty()) {
            nodata.setVisibility(View.VISIBLE);
        } else {
            nodata.setVisibility(View.GONE);
        }
    }

    public void initialView() {
        calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        format = new SimpleDateFormat("yyyy年M月", Locale.CHINA);
        nodata = findViewById(R.id.show_details_nodata);
        updateList();
        recyclerView = findViewById(R.id.show_details_fragment_recycler);
        dateSwitcher = findViewById(R.id.show_details_fagment_textswitcher);
        dateSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView tv = new TextView(ShowDetailsActivity.this);
                tv.setTextSize(16);
                tv.setTextColor(getResources().getColor(android.R.color.white));
                tv.setSingleLine(true);
                tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                tv.setGravity(Gravity.CENTER);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                lp.gravity = Gravity.CENTER;
                tv.setLayoutParams(lp);
                tv.setClickable(false);
                return tv;
            }
        });
        dateSwitcher.setCurrentText(format.format(calendar.getTime()));
        dateSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMonth(v);
            }
        });
        findViewById(R.id.show_details_fragment_preMonth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preMonth(v);
            }
        });
        findViewById(R.id.show_details_fragment_nextMonth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMonth(v);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        adapter = new ShowDetailsFatherAdapter(list, this, this);
        adapter.setClickItem(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                adapter.notifyDataSetChanged();
                boolean b = (boolean) v.getTag();
                if (b) {
                    scrollRecyclerView(adapter.getPreIndex());
                }
            }
        });
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new ShowDetailsItemDecoration(this));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter.getItemCount() > 0) {
            scrollRecyclerView(adapter.getItemCount() - 1);
        }
    }

    private void scrollRecyclerView(int position) {
        if (recyclerView != null && adapter != null) {
            LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (llm != null) {
                llm.scrollToPositionWithOffset(position, 0);
                llm.setStackFromEnd(false);
            }
        }
    }

    public void preMonth(View view) {
        calendar.add(Calendar.MONTH, -1);
        updateList();
        adapter.notifyDataSetChanged();
        if (adapter.getItemCount() > 0) {
            adapter.setPreIndex(adapter.getItemCount() - 1);
            adapter.expand(adapter.getPreIndex());
            scrollRecyclerView(adapter.getPreIndex());
        }
        dateSwitcher.setInAnimation(this, R.anim.rtl_in);
        dateSwitcher.setOutAnimation(this, R.anim.rtl_out);
        dateSwitcher.setText(format.format(calendar.getTime()));
    }

    public void currentMonth(View view) {
        calendar.setTimeInMillis(System.currentTimeMillis());
        updateList();
        adapter.notifyDataSetChanged();
        if (adapter.getItemCount() > 0) {
            adapter.setPreIndex(adapter.getItemCount() - 1);
            adapter.expand(adapter.getPreIndex());
            scrollRecyclerView(adapter.getPreIndex());
        }
        dateSwitcher.setInAnimation(this, android.R.anim.fade_in);
        dateSwitcher.setOutAnimation(this, android.R.anim.fade_out);
        dateSwitcher.setText(format.format(calendar.getTime()));
    }

    public void nextMonth(View view) {
        calendar.add(Calendar.MONTH, 1);
        updateList();
        adapter.notifyDataSetChanged();
        if (adapter.getItemCount() > 0) {
            adapter.setPreIndex(adapter.getItemCount() - 1);
            adapter.expand(adapter.getPreIndex());
            scrollRecyclerView(adapter.getPreIndex());
        }
        dateSwitcher.setInAnimation(this, R.anim.lft_in);
        dateSwitcher.setOutAnimation(this, R.anim.lft_out);
        dateSwitcher.setText(format.format(calendar.getTime()));
    }

}

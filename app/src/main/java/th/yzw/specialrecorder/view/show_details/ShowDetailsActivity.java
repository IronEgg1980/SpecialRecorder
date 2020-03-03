package th.yzw.specialrecorder.view.show_details;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import th.yzw.specialrecorder.DAO.ShowDetailsOperator;
import th.yzw.specialrecorder.MyActivity;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.OnSelectDateRangeDismiss;
import th.yzw.specialrecorder.model.ShowDetailsItemFatherEntity;
import th.yzw.specialrecorder.view.common.SelectMonthPopWindow;
import th.yzw.specialrecorder.view.common.HeadPaddingItemDecoration;

public class ShowDetailsActivity extends MyActivity {

    private List<ShowDetailsItemFatherEntity> list;
    private Calendar calendar;
    private TextView nodata;
    private SelectMonthPopWindow popWindow;
    private ShowDetailsFatherAdapter adapter;
    private RecyclerView recyclerView;
    private SimpleDateFormat format;
    private TextView dataTextView;

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
        dataTextView.setText(format.format(calendar.getTime()));
    }

    public void initialView() {
        calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        format = new SimpleDateFormat("yyyy年M月", Locale.CHINA);
        nodata = findViewById(R.id.show_details_nodata);
        dataTextView = findViewById(R.id.changeDate);
        dataTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popWindow.setDate(calendar.getTimeInMillis()).show(dataTextView);
            }
        });
        popWindow =  new SelectMonthPopWindow(this);
        popWindow.setDisMiss(new OnSelectDateRangeDismiss() {
            @Override
            public void onDissmiss(boolean isConfirm, long... timeInMillis) {
                if (isConfirm) {
                    changeDate(timeInMillis[0]);
                }
            }
        });
        updateList();
        recyclerView = findViewById(R.id.show_details_fragment_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        adapter = new ShowDetailsFatherAdapter(list, this, this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new HeadPaddingItemDecoration(this));
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter.getItemCount() > 0) {
            scrollRecyclerView(adapter.getItemCount() - 1);
        }
    }

    private void scrollRecyclerView(final int position) {
        if (recyclerView != null && adapter != null) {
            final LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (llm != null) {
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        llm.scrollToPositionWithOffset(position, 0);
                    }
                },200);
            }
        }
    }


    private void changeDate(long time){
        calendar.setTimeInMillis(time);
        updateList();
        adapter.notifyDataSetChanged();
        if (adapter.getItemCount() > 0) {
            scrollRecyclerView(adapter.getItemCount() - 1);
        }
    }
}

package th.yzw.specialrecorder.view.show_details;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.model.RecordEntity;
import th.yzw.specialrecorder.model.ShowDetailsItemFatherEntity;

public class ShowDetailsFatherAdapter extends RecyclerView.Adapter<ShowDetailsFatherAdapter.FatherViewHolder> {

//    public void setClickItem(View.OnClickListener clickItem) {
//        this.clickItem = clickItem;
//    }

//    private View.OnClickListener clickItem;

//    public int getPreIndex() {
//        return preIndex;
//    }

//    public void setPreIndex(int preIndex) {
//        this.preIndex = preIndex;
//    }

//    private int preIndex;
    private List<ShowDetailsItemFatherEntity> mList;
    private AppCompatActivity mActivity;
    private Context mContext;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    public ShowDetailsFatherAdapter(List<ShowDetailsItemFatherEntity> list, Context context, AppCompatActivity appCompatActivity) {
        this.mList = list;
        this.mContext = context;
        this.mActivity = appCompatActivity;
//        this.preIndex = getItemCount() - 1;
        this.calendar = new GregorianCalendar(Locale.CHINA);
        this.dateFormat = new SimpleDateFormat("记录时间：HH:mm:ss",Locale.CHINA);
//        expand(preIndex);
    }

    public void noData(int position) {
        if (position >= 0 && position < getItemCount()) {
            mList.remove(position);
            notifyItemRemoved(position);
            if (getItemCount() > 0) {
                notifyItemRemoved(position);
                if (position < getItemCount())
                    notifyItemRangeChanged(position, getItemCount() - position);
            }
        }
    }

    private String getDateString(long time) {
        StringBuilder stringBuilder = new StringBuilder();
        calendar.setTimeInMillis(time);
        stringBuilder.append(calendar.get(Calendar.DAY_OF_MONTH)).append("\n");
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case 2:
                stringBuilder.append("周一");
                break;
            case 3:
                stringBuilder.append("周二");
                break;
            case 4:
                stringBuilder.append("周三");
                break;
            case 5:
                stringBuilder.append("周四");
                break;
            case 6:
                stringBuilder.append("周五");
                break;
            case 7:
                stringBuilder.append("周六");
                break;
            case 1:
                stringBuilder.append("周日");
                break;
        }
        return stringBuilder.toString();
    }

    public void flushCurrent(int position) {
        if (position >= 0 && position < getItemCount())
            notifyItemChanged(position);
    }

//    public void expand(int position) {
//        if (position < 0)
//            return;
//        ShowDetailsItemFatherEntity entity = mList.get(position);
//        entity.setExpand(true);
//        notifyItemChanged(position);
//        if (preIndex != position) {
//            close(preIndex);
//        }
//        preIndex = position;
//    }

//    private void close(int position) {
//        if (position >= 0 && position < getItemCount()) {
//            ShowDetailsItemFatherEntity entity = mList.get(position);
//            entity.setExpand(false);
//            for (RecordEntity r : entity.getRecords()) {
//                r.setSelected(false);
//            }
//            notifyItemChanged(position);
//        }
//    }

    @NonNull
    @Override
    public FatherViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_data_details, viewGroup, false);
        return new FatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FatherViewHolder fatherViewHolder, final int i) {
        final ShowDetailsItemFatherEntity entity = mList.get(i);
        fatherViewHolder.dateTextView.setText(getDateString(entity.getDate()));
//        fatherViewHolder.root.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (entity.isExpand())
//                    close(i);
//                else {
//                    expand(i);
//                }
//                v.setTag(entity.isExpand());
//                clickItem.onClick(v);
//            }
//        });
        fatherViewHolder.timeTextView.setText(dateFormat.format(entity.getDate()));
        fatherViewHolder.recordCountTextView.setText(entity.getRecords().size() + "条记录");
        fatherViewHolder.recyclerview.setLayoutManager(new LinearLayoutManager(mActivity.getApplicationContext()));
        ShowDetailsSonAdapter adapter = new ShowDetailsSonAdapter(entity.getRecords(), mContext, mActivity, this);
        adapter.setFatherIndex(fatherViewHolder.getAdapterPosition());
        fatherViewHolder.recyclerview.setAdapter(adapter);
        fatherViewHolder.recyclerview.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    protected class FatherViewHolder extends RecyclerView.ViewHolder {
        private TextView dateTextView;
        private RecyclerView recyclerview;
        private TextView timeTextView;
        private TextView recordCountTextView;

        public FatherViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            recyclerview = itemView.findViewById(R.id.recyclerview);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            recordCountTextView = itemView.findViewById(R.id.recordCountTextView);
        }
    }
}

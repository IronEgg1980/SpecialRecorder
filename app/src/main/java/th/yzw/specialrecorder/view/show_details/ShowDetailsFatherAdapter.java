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
import java.util.List;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.model.RecordEntity;
import th.yzw.specialrecorder.model.ShowDetailsItemFatherEntity;

public class ShowDetailsFatherAdapter extends RecyclerView.Adapter<ShowDetailsFatherAdapter.FatherViewHolder> {

    public void setClickItem(View.OnClickListener clickItem) {
        this.clickItem = clickItem;
    }

    private View.OnClickListener clickItem;

    public int getPreIndex() {
        return preIndex;
    }

    public void setPreIndex(int preIndex) {
        this.preIndex = preIndex;
    }

    private int preIndex;
    private List<ShowDetailsItemFatherEntity> mList;
    private AppCompatActivity mActivity;
    private Context mContext;
    public ShowDetailsFatherAdapter(List<ShowDetailsItemFatherEntity> list,Context context,AppCompatActivity appCompatActivity){
        this.mList = list;
        this.mContext = context;
        this.mActivity = appCompatActivity;
        this.preIndex = getItemCount() -1;
        expand(preIndex);
    }
    public void noData(){
        if(preIndex >=0 && preIndex<getItemCount()){
            mList.remove(preIndex);
            notifyItemRemoved(preIndex);
            if(getItemCount()>0){
                notifyItemRemoved(preIndex);
                if (preIndex < getItemCount())
                    notifyItemRangeChanged(preIndex, getItemCount() - preIndex);
                preIndex = getItemCount() -1;
            }
        }
    }
    public void flushCurrent(){
        if(preIndex>=0 && preIndex <getItemCount())
            notifyItemChanged(preIndex);
    }
    public void expand(int position){
        if(position < 0)
            return;
        ShowDetailsItemFatherEntity entity = mList.get(position);
        entity.setExpand(true);
        notifyItemChanged(position);
        if(preIndex != position) {
            close(preIndex);
        }
        preIndex = position;
    }
    private void close(int position){
        if(position >=0 &&position < getItemCount()) {
            ShowDetailsItemFatherEntity entity = mList.get(position);
            entity.setExpand(false);
            for (RecordEntity r : entity.getRecords()) {
                r.setSelected(false);
            }
            notifyItemChanged(position);
        }
    }

    @NonNull
    @Override
    public FatherViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_data_details,viewGroup,false);
        return new FatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FatherViewHolder fatherViewHolder, final int i) {
        final ShowDetailsItemFatherEntity entity = mList.get(i);
        fatherViewHolder.dateTextView.setText(new SimpleDateFormat("yyyy年M月d日").format(entity.getDate()));
        fatherViewHolder.topLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(entity.isExpand())
                    close(i);
                else {
                    expand(i);
                }
                v.setTag(entity.isExpand());
                clickItem.onClick(v);
            }
        });
        fatherViewHolder.timeTextView.setText(new SimpleDateFormat("记录时间：HH:mm:ss").format(entity.getDate()));
        fatherViewHolder.recordCountTextView.setText(entity.getRecords().size()+"条记录");
        fatherViewHolder.recyclerview.setLayoutManager(new LinearLayoutManager(mActivity.getApplicationContext()));
        fatherViewHolder.recyclerview.setAdapter(new ShowDetailsSonAdapter(entity.getRecords(),mContext,mActivity,this));
        fatherViewHolder.recyclerview.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        if (entity.isExpand()) {
            fatherViewHolder.detailsLinear.setVisibility(View.VISIBLE);
            fatherViewHolder.expandImageView.setVisibility(View.GONE);
        }else{
            fatherViewHolder.detailsLinear.setVisibility(View.GONE);
            fatherViewHolder.expandImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    protected class FatherViewHolder extends RecyclerView.ViewHolder{
        private TextView dateTextView;
        private LinearLayout detailsLinear,topLinear;
        private RecyclerView recyclerview;
        private TextView timeTextView;
        private TextView recordCountTextView;
        private ImageView expandImageView;
        public FatherViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            detailsLinear = itemView.findViewById(R.id.details_linear);
            recyclerview = itemView.findViewById(R.id.recyclerview);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            recordCountTextView = itemView.findViewById(R.id.recordCountTextView);
            expandImageView = itemView.findViewById(R.id.expandImageView);
            topLinear = itemView.findViewById(R.id.linearTop);
        }
    }
}

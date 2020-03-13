package th.yzw.specialrecorder.view.show_all_data;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.model.ShowDataEntity;

public class ShowDataAdapter extends RecyclerView.Adapter<ShowDataAdapter.ViewHolder> {

    protected class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv1, tv2;
        ImageButton imageButton;
        LinearLayout root;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv1 = itemView.findViewById(R.id.name);
            tv2 = itemView.findViewById(R.id.count);
            imageButton = itemView.findViewById(R.id.imageButton);
            root = itemView.findViewById(R.id.root);
        }
    }

    public interface itemClickListener {
        void click(int position);
    }

    private itemClickListener clickListener;

    public void setClickListener(itemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    private List<ShowDataEntity> mList;
    private Context mContext;

    public ShowDataAdapter(List<ShowDataEntity> list,Context context) {
        this.mList = list;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.show_data_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        final ShowDataEntity sumTotalRecord = mList.get(i);
        viewHolder.tv1.setText(sumTotalRecord.getName());
        viewHolder.tv2.setText(String.valueOf(sumTotalRecord.getCount()));
        viewHolder.root.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                clickListener.click(i);
            }
        });
        boolean b=sumTotalRecord.isSelected();
        final boolean isDone = sumTotalRecord.isDone();
        if (isDone) {
            viewHolder.tv1.setPaintFlags(viewHolder.tv1.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.tv2.setPaintFlags(viewHolder.tv2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.tv1.setTextColor(mContext.getColor(R.color.black_overlay));
            viewHolder.tv2.setTextColor(mContext.getColor(R.color.black_overlay));
            viewHolder.tv1.setTextSize(14);
            viewHolder.tv2.setTextSize(14);
        }else{
            viewHolder.tv1.setPaintFlags(viewHolder.tv1.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            viewHolder.tv2.setPaintFlags(viewHolder.tv2.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            viewHolder.tv1.setTextColor(Color.BLACK);
            viewHolder.tv2.setTextColor(mContext.getColor(R.color.colorAccent));
            viewHolder.tv1.setTextSize(18);
            viewHolder.tv2.setTextSize(18);
        }
        if(b){
            viewHolder.root.setBackgroundColor(mContext.getColor(R.color.colorAccent));
            viewHolder.tv1.setTextColor(Color.WHITE);
            viewHolder.tv1.setTextSize(18);
            viewHolder.tv2.setTextColor(Color.WHITE);
            viewHolder.tv2.setTextSize(28);
            if (isDone) {
                viewHolder.imageButton.setImageDrawable(mContext.getDrawable(R.drawable.ic_replay_white_24dp));
            }else{
                viewHolder.imageButton.setImageDrawable(mContext.getDrawable(R.drawable.ic_done_white_24dp));
            }
            viewHolder.imageButton.setVisibility(View.VISIBLE);
            viewHolder.imageButton.setEnabled(true);
            viewHolder.imageButton.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    sumTotalRecord.setDone(!isDone);
                    sumTotalRecord.save();
                    notifyItemChanged(i);
                }
            });
        }else{
            viewHolder.imageButton.setVisibility(View.INVISIBLE);
            viewHolder.imageButton.setEnabled(false);
            viewHolder.root.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

}

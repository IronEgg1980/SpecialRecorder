package th.yzw.specialrecorder.unuse;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.model.ItemName;

public class ItemNameAdapter extends RecyclerView.Adapter<ItemNameAdapter.VH> {
    private List<ItemName> mList;
    private MyClickListener clickListener;
    private int currentIndex = -1;

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setClickListener(MyClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public ItemNameAdapter(List<ItemName> list){
        this.mList = list;
    }
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.itemname_list_item,viewGroup,false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH vh, int i) {
        final int index = vh.getAdapterPosition();
        if(index == currentIndex){
            vh.textView.setTextColor(Color.BLACK);
        }else{
            vh.textView.setTextColor(Color.GRAY);
        }
        ItemName itemName = mList.get(i);
        vh.textView.setText(itemName.getName());
        vh.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentIndex = index;
                clickListener.OnClick(v,index);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    protected class VH extends RecyclerView.ViewHolder{
        TextView textView;
        public VH(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.item_name_textview);
        }
    }
}

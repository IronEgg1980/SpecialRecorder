package th.yzw.specialrecorder.view.input_data;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import th.yzw.specialrecorder.DAO.AppSetupOperator;
import th.yzw.specialrecorder.DAO.ItemNameOperator;
import th.yzw.specialrecorder.DAO.ItemStatisticalInformationOperator;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.model.ItemName;

public class TouchInputAdapter extends RecyclerView.Adapter<TouchInputAdapter.AddViewHolder> {

    private List<ItemName> items;
    private List<String> top10, allInformation;
    private int normalTextColor,top10TextColor,unuseTextColor;
    private boolean isShowGropFlag;

    public List<ItemName> getItemsList() {
        return items;
    }

    public ItemName getCurrentItem() {
        return items.get(position);
    }

    public int getPosition() {
        return position;
    }

    private int position;

    // 点击监听接口
    private MyClickListener myClickListener;

    public void setMyClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public TouchInputAdapter(Context context) {
        this.normalTextColor = context.getColor(R.color.itemNormalTextColor);
        this.top10TextColor = context.getColor(R.color.itemTop10TextColor);
        this.unuseTextColor = context.getColor(R.color.itemUnuseTextColor);
        this.isShowGropFlag = AppSetupOperator.getShowGroupButtonStatus();
        update(true);
    }

    @Override
    public AddViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.touch_input_item, viewGroup, false);
        return new AddViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AddViewHolder addViewHolder, final int i) {
        String name = items.get(i).getName();
        addViewHolder.textView.setText(name);
        if (top10.contains(name)) {
            addViewHolder.textView.setTextColor(top10TextColor);
            addViewHolder.textView.setTypeface(Typeface.DEFAULT_BOLD);
        } else if (allInformation.contains(name)) {
            addViewHolder.textView.setTextColor(normalTextColor);
            addViewHolder.textView.setTypeface(Typeface.DEFAULT);
        } else {
            addViewHolder.textView.setTextColor(unuseTextColor);
            addViewHolder.textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
        }

        addViewHolder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = i;
                myClickListener.OnClick(v, i);
            }
        });
        addViewHolder.textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                position = i;
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void update(boolean isOftenUse) {
        if (AppSetupOperator.getShowGroupButtonStatus())
            items = ItemNameOperator.findAllByOftenUse(isOftenUse);
        else
            items = ItemNameOperator.findAll(true);
        if (top10 == null)
            top10 = new ArrayList<>();
        top10.clear();
        String[] strings = ItemStatisticalInformationOperator.getNameList();
        allInformation = Arrays.asList(strings);
        int length = allInformation.size();
        int index = length > 9 ? 10 : length;
        top10.addAll(allInformation.subList(0, index));
//        int i = 0; //false
//        if(isOftenUse)
//            i  = 1;//true
//        if(items !=null)
//            items.clear();
//        else
//            items = new ArrayList<>();
//        items.addAll(ItemNameOperator.findAllByOftenUse(isOftenUse));
    }

    @Override
    public void onViewRecycled(AddViewHolder holder) {
        holder.view.setOnLongClickListener(null);
        super.onViewRecycled(holder);
    }

    public void setOftenUse(boolean flag) {
        ItemName itemName = getCurrentItem();
        itemName.setOftenUse(flag);
        itemName.save();
        update(!flag);
    }

    class AddViewHolder extends RecyclerView.ViewHolder {
        public AppCompatTextView textView;
        public View view;

        public AddViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            if(isShowGropFlag) {
                view.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                        ItemName itemName = items.get(position);
                        menu.clear();
                        //menu.clearHeader();
                        menu.setHeaderTitle(itemName.getName());
                        if (itemName.isOftenUse()) {
                            menu.add(0, 1, 0, "移出常用项目");
                        } else {
                            menu.add(0, 2, 0, "加入常用项目");
                        }
                    }
                });
            }
            textView = itemView.findViewById(R.id.add2_itemname);
        }
    }
}

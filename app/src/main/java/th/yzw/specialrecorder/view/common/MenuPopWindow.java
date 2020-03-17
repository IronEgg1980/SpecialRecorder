package th.yzw.specialrecorder.view.common;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;

public class MenuPopWindow extends PopupWindow {
    private String TAG = "殷宗旺";
    private MyClickListener clickListener;
    private String[] mList;
    private Drawable[] icos;
    private Activity mActivity;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private MyDividerItemDecoration dividerItemDecoration;
    private int itemHeight;


    private void setView() {
        View itemView = LayoutInflater.from(mActivity).inflate(R.layout.menu_item,null);
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        itemView.measure(w,h);
        itemHeight = itemView.getMeasuredHeight();

        View view = LayoutInflater.from(mActivity).inflate(R.layout.menu_popwindow_layout, null);
        dividerItemDecoration = new MyDividerItemDecoration();
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.addItemDecoration(new MenuIndicator(Color.WHITE));
        setContentView(view);
    }

    public MenuPopWindow setClickListener(MyClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public MenuPopWindow setMaxShownItem(int shownItemCount) {
        int count = Math.min(mList.length,shownItemCount);
        setHeight(itemHeight * count + 10);
        return this;
    }

    public MenuPopWindow showDivider(){
        if(recyclerView.getItemDecorationCount() == 1){
            recyclerView.addItemDecoration(dividerItemDecoration);
            adapter.notifyDataSetChanged();
        }
        return this;
    }

    public MenuPopWindow removeDivider(){
        if(recyclerView.getItemDecorationCount() == 2){
            recyclerView.removeItemDecoration(dividerItemDecoration);
            adapter.notifyDataSetChanged();
        }
        return this;
    }

    public MenuPopWindow(Activity activity, String[] itemNames, Drawable[] itemIcos) {
        mList = itemNames;
        icos = itemIcos;
        mActivity = activity;
        adapter = new Adapter();

        setView();

        setMinWidth();
        setMaxHeight();
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void setMinWidth(){
        View view = getContentView();
        int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        view.measure(w,h);
        int width = view.getMeasuredWidth();
        setWidth(Math.max(width,280));
    }

    private void setMaxHeight(){
        setMaxShownItem(5);
    }

    private class VH extends RecyclerView.ViewHolder {
        private TextView textView;

        VH(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.select_popwindow_item_tv);
        }
    }

    private class Adapter extends RecyclerView.Adapter<VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new VH(LayoutInflater.from(mActivity).inflate(R.layout.menu_item, viewGroup,false));
        }

        @Override
        public void onBindViewHolder(@NonNull final VH vh, int i) {
            final int index = vh.getAdapterPosition();
            vh.textView.setText(mList[index]);
            vh.textView.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    clickListener.OnClick(vh.textView, index);
                    dismiss();
                }
            });
            if (icos != null && icos.length > index) {
                Drawable drawable = icos[index];
                if (drawable != null) {
                    drawable.setBounds(0, 0, 48, 48);
                    vh.textView.setCompoundDrawables(drawable, null, null, null);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mList == null ? 0 : mList.length;
        }
    }
}

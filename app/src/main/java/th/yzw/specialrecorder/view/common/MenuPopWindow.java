package th.yzw.specialrecorder.view.common;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.MenuPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.tools.OtherTools;

public class MenuPopWindow extends PopupWindow {
    private MyClickListener clickListener;
    private String[] mList;
    private Drawable[] icos;
    private Activity mActivity;

    private void createView(){
        View view = LayoutInflater.from(mActivity).inflate(R.layout.menu_popwindow_layout,null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setAdapter(new Adapter());
        recyclerView.addItemDecoration(new MyDividerItemDecoration());
        setContentView(view);
    }

    public MenuPopWindow setClickListener(MyClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public MenuPopWindow(Activity activity, String[] itemNames, Drawable[] itemIcos){
        mList = itemNames;
        icos = itemIcos;
        mActivity = activity;

        createView();

        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        int count = itemNames.length > 5?5:itemNames.length;
        int height = OtherTools.dip2px(activity,56) * count;
        setHeight(height);
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    protected class VH extends RecyclerView.ViewHolder{
        private TextView textView;
        public VH(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.select_popwindow_item_tv);
        }
    }

    protected class Adapter extends RecyclerView.Adapter<VH>{

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.menu_item,viewGroup,false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final VH vh, int i) {
            final int index = vh.getAdapterPosition();
            vh.textView.setText(mList[index]);
            vh.textView.setOnClickListener(new NoDoubleClickListener() {
                @Override
                public void onNoDoubleClick(View v) {
                    clickListener.OnClick(vh.textView,index);
                    dismiss();
                }
            });
            if(icos != null && icos.length > index){
                Drawable drawable = icos[index];
                if (drawable!=null) {
                    drawable.setBounds(0,0,48,48);
                    vh.textView.setCompoundDrawables(drawable, null, null, null);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mList == null ? 0: mList.length;
        }
    }
}

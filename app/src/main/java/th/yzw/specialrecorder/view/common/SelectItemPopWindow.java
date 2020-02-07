package th.yzw.specialrecorder.view.common;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.Result;

public class SelectItemPopWindow extends PopupWindow {
    private Activity mActivity;
    private String[] mList;
    private boolean isMutiSelectMode = false;
    private boolean[] selectedFlag;
    private IDialogDismiss onDisMiss;
    public boolean isResumeAlpha = true;
    public boolean isDarkBG = true;

    public SelectItemPopWindow(Activity activity,String[] list, final boolean isMutiSelectMode){
        mActivity = activity;
        mList = list;
        this.selectedFlag = new boolean[list.length];
        this.isMutiSelectMode = isMutiSelectMode;
        initialView();
        setFocusable(true);
        setTouchable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setAnimationStyle(R.style.PopWindowAnim);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                if(isResumeAlpha)
                    darkenBackground(1f);
            }
        });
    }

    private void initialView(){
        View view = LayoutInflater.from(mActivity).inflate(R.layout.popwindow_select_layout,null);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setAdapter(new Adapter());
        recyclerView.addItemDecoration(new DividerItemDecoration(mActivity,DividerItemDecoration.VERTICAL));
        TextView okTV = view.findViewById(R.id.confirmTV);
        okTV.setVisibility(isMutiSelectMode?View.VISIBLE:View.GONE);
        okTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Integer> selectedIndex = new ArrayList<>();
                for(int i = 0;i<selectedFlag.length;i++){
                    boolean b1 = selectedFlag[i];
                    if(b1){
                        selectedIndex.add(i);
                    }
                }
                Object[] indexs = selectedIndex.toArray();
                onDisMiss.onDismiss(Result.OK,indexs);
                dismiss();
            }
        });
        view.findViewById(R.id.cancelTV).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isResumeAlpha = true;
                onDisMiss.onDismiss(Result.CANCEL);
                dismiss();
            }
        });
        setContentView(view);
    }


    public void show(IDialogDismiss dialogDismiss) {
        this.onDisMiss = dialogDismiss;
        showAtLocation(mActivity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
        if(isDarkBG)
            darkenBackground(0.5f);
    }

    private void darkenBackground(Float bgcolor) {
        if(mActivity == null)
            return;
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = bgcolor;
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mActivity.getWindow().setAttributes(lp);
    }


    protected class VH extends RecyclerView.ViewHolder {
        private ImageView selectedFlagIV;
        private TextView itemNameTV;
        private LinearLayout root;

        public VH(@NonNull View itemView) {
            super(itemView);
            selectedFlagIV = itemView.findViewById(R.id.select_popwindow_item_iv);
            itemNameTV = itemView.findViewById(R.id.select_popwindow_item_tv);
            root = itemView.findViewById(R.id.root);
        }
    }

    protected class Adapter extends RecyclerView.Adapter<VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.select_popwindow_item, viewGroup, false);
            return new VH((view));
        }

        @Override
        public void onBindViewHolder(@NonNull VH vh, int i) {
            String s = mList[i];
            final int index = i;
            final boolean b = selectedFlag[index];
            vh.itemNameTV.setText(s);
            vh.selectedFlagIV.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
            vh.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isMutiSelectMode) {
                        selectedFlag[index] = !b;
                        notifyItemChanged(index);
                    } else if (onDisMiss != null) {
                        onDisMiss.onDismiss(Result.OK,index);
                        dismiss();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mList.length;
        }
    }
}

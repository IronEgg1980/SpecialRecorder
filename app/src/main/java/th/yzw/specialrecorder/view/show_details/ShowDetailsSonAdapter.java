package th.yzw.specialrecorder.view.show_details;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import th.yzw.specialrecorder.DAO.ItemStatisticalInformationOperator;
import th.yzw.specialrecorder.DAO.RecordEntityOperator;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.model.RecordEntity;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.DialogFactory;
import th.yzw.specialrecorder.view.common.EditDataDialogFragment;
import th.yzw.specialrecorder.view.common.EditPopWindow;

public class ShowDetailsSonAdapter extends RecyclerView.Adapter<ShowDetailsSonAdapter.SonViewHolder> {
    private List<RecordEntity> mList;
    private AppCompatActivity mActivity;
    private int preIndex;
    private Context mContext;
    private ShowDetailsFatherAdapter fatherAdapter;
    private TranslateAnimation showAnim;
//    private DialogFactory dialogFactory;

    public ShowDetailsSonAdapter(List<RecordEntity> list, Context context, AppCompatActivity activity, ShowDetailsFatherAdapter fatherAdapter) {
        this.mList = list;
        this.mContext = context;
        this.mActivity = activity;
        this.fatherAdapter = fatherAdapter;
        this.preIndex = -1;
//        dialogFactory = new DialogFactory(context);
        initialAnim();
    }
    private void initialAnim(){
//        this.hideAnim = new AlphaAnimation(1.0f,0.0f);
//        this.hideAnim.setDuration(400);
        this.showAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        this.showAnim.setDuration(400);
//        this.countRTL = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 2.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f);
//        this.countRTL.setDuration(400);
//        this.countLTR =new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
//                Animation.RELATIVE_TO_SELF, 2.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f,
//                Animation.RELATIVE_TO_SELF, 0.0f);
//        this.countLTR.setDuration(400);
    }
    protected void click(int position) {
        if (preIndex >= 0 && preIndex != position) {
            RecordEntity _entity = mList.get(preIndex);
            if(_entity.isSelected()) {
                _entity.setSelected(false);
                notifyItemChanged(preIndex);
            }
        }
        RecordEntity entity = mList.get(position);
        entity.setSelected(!entity.isSelected());
        notifyItemChanged(position);
        preIndex = position;
    }

    protected void editData(final int position,View view) {
        final RecordEntity r = mList.get(position);
        EditPopWindow editPopWindow = new EditPopWindow(mContext,r.getName(),r.getCount());
        editPopWindow.show(view, new IDialogDismiss() {
            @Override
            public void onDismiss(boolean isConfirmed, Object... values) {
                if(isConfirmed){
                    int value =(int)values[0];
                    int changeValue = value -  r.getCount();
                    RecordEntityOperator.update(r,value);
                    ItemStatisticalInformationOperator.update(r.getName(),changeValue);
                    r.setSelected(false);
                    preIndex = -1;
                    notifyItemChanged(position);
                }
            }
        });
//        EditDataDialogFragment fragment = EditDataDialogFragment.newInstant(r.getName(), r.getCount());
//        fragment.setOnDissmissListener(new IDialogDismiss() {
//            @Override
//            public void onDismiss(boolean isConfirmed, Object...values) {
//                if (isConfirmed) {
//                    int value =(int)values[0];
//                    int changeValue = value -  r.getCount();
//                    RecordEntityOperator.update(r,value);
//                    ItemStatisticalInformationOperator.update(r.getName(),changeValue);
//                    r.setSelected(false);
//                    preIndex = -1;
//                    notifyItemChanged(position);
//                }
//            }
//        });
//        fragment.show(mActivity.getSupportFragmentManager(), "edit");
        click(position);
    }

    protected void delData(View view,final int position) {
        final RecordEntity r = mList.get(position);
//        dialogFactory.showDefaultConfirmDialog("是否删除【" + r.getName() + "】的记录？", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                RecordEntityOperator.del(r);
//                ItemStatisticalInformationOperator.del(r.getName(),r.getCount());
//                mList.remove(position);
//                if (getItemCount() > 0) {
//                    fatherAdapter.flushCurrent();
//                    preIndex = -1;
//                } else {
//                    fatherAdapter.noData();
//                }
//            }
//        });

        ConfirmPopWindow confirmPopWindow = new ConfirmPopWindow(mContext,"是否删除【" + r.getName() + "】的记录？");
        confirmPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                darkenBackground(1.0f);
            }
        });
        confirmPopWindow.show(view, new IDialogDismiss() {
            @Override
            public void onDismiss(boolean isConfirmed, Object... values) {
                if (isConfirmed){
                    RecordEntityOperator.del(r);
                    ItemStatisticalInformationOperator.del(r.getName(),r.getCount());
                    mList.remove(position);
                    if (getItemCount() > 0) {
                        fatherAdapter.flushCurrent();
                        preIndex = -1;
                    } else {
                        fatherAdapter.noData();
                    }
                }
            }
        });
        click(position);
        darkenBackground(0.5f);
    }

    private void darkenBackground(Float bgcolor) {
        WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
        lp.alpha = bgcolor;
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mActivity.getWindow().setAttributes(lp);
    }

        @NonNull
    @Override
    public SonViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.show_details_item, viewGroup, false);
        return new SonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SonViewHolder sonViewHolder, int i) {
        final int index = sonViewHolder.getAdapterPosition();
        sonViewHolder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click(index);
            }
        });
        sonViewHolder.showItemEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editData(index,sonViewHolder.showItemEdit);
            }
        });
        sonViewHolder.showItemDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delData(sonViewHolder.showItemDel,index);
            }
        });
        RecordEntity entity = mList.get(index);
        sonViewHolder.showItemName.setText(entity.getName());
        sonViewHolder.showItemCount.setText(String.valueOf(entity.getCount()));
        if (entity.isSelected()) {
            showAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    sonViewHolder.showItemName.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
                    sonViewHolder.group.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            sonViewHolder.group.startAnimation(showAnim);
        } else if(sonViewHolder.group.getVisibility() == View.VISIBLE){
            sonViewHolder.showItemName.setTextColor(mContext.getResources().getColor(R.color.textColor));
            sonViewHolder.group.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    protected class SonViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout root;
        private TextView showItemName;
        private TextView showItemCount;
        private TextView showItemEdit;
        private TextView showItemDel;
        private LinearLayout group;

        public SonViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            showItemName = itemView.findViewById(R.id.show_item_name);
            showItemCount = itemView.findViewById(R.id.show_item_count);
            showItemEdit = itemView.findViewById(R.id.show_item_edit);
            showItemDel = itemView.findViewById(R.id.show_item_del);
            group = itemView.findViewById(R.id.group);
        }
    }
}

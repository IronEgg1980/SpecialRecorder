package th.yzw.specialrecorder.view.show_details;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.List;

import th.yzw.specialrecorder.DAO.ItemStatisticalInformationOperator;
import th.yzw.specialrecorder.DAO.RecordEntityOperator;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.interfaces.NoDoubleClickListener;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.model.RecordEntity;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.EditPopWindow;
import th.yzw.specialrecorder.view.common.MenuPopWindow;

public class ShowDetailsSonAdapter extends RecyclerView.Adapter<ShowDetailsSonAdapter.SonViewHolder> {
    private List<RecordEntity> mList;
    private AppCompatActivity mActivity;
    private int preIndex,fatherIndex = -1;
    private Context mContext;
    private ShowDetailsFatherAdapter fatherAdapter;
    private MenuPopWindow menuPopWindow;
    private EditPopWindow editPopWindow;
    private int menuWidth,menuHeight;

    public void setFatherIndex(int fatherIndex) {
        this.fatherIndex = fatherIndex;
    }

    public ShowDetailsSonAdapter(final List<RecordEntity> list, Context context, AppCompatActivity activity, ShowDetailsFatherAdapter fatherAdapter) {
        this.mList = list;
        this.mContext = context;
        this.mActivity = activity;
        this.fatherAdapter = fatherAdapter;
        this.preIndex = -1;
        this.editPopWindow = new EditPopWindow(activity, true);
        this.editPopWindow.setDialogDismiss(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if (result == Result.OK && preIndex != -1) {
                    RecordEntity r = list.get(preIndex);
                    int value = (int) values[0];
                    int changeValue = value - r.getCount();
                    RecordEntityOperator.update(r, value);
                    ItemStatisticalInformationOperator.update(r.getName(), changeValue);
                    r.setSelected(false);
                    notifyItemChanged(preIndex);
                    preIndex = -1;
                }
            }
        });
        String[] menuitem = {"修改", "删除"};
        Drawable[] icons = new Drawable[2];
        icons[0] = context.getDrawable(R.drawable.ic_edit_18dp);
        icons[1] = context.getDrawable(R.drawable.ic_delete_24dp);
        menuPopWindow = new MenuPopWindow(activity, menuitem, icons);
        menuPopWindow.setClickListener(new MyClickListener() {
            @Override
            public void OnClick(View view, Object o) {
                int index = (int) o;
                if (index == 0)
                    editData();
                else
                    delData();
            }
        });
        menuPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                click(preIndex);
            }
        });
        View view1 = menuPopWindow.getContentView();
        view1.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        menuWidth = view1.getMeasuredWidth();
        menuHeight = view1.getMeasuredHeight();
    }


    protected void click(int position) {
        if (preIndex >= 0 && preIndex != position) {
            RecordEntity _entity = mList.get(preIndex);
            if (_entity.isSelected()) {
                _entity.setSelected(false);
                notifyItemChanged(preIndex);
            }
        }
        RecordEntity entity = mList.get(position);
        entity.setSelected(!entity.isSelected());
        notifyItemChanged(position);
        preIndex = position;
    }

    private void showMenu(SonViewHolder sonViewHolder) {
        DetailsItemGroupView view = sonViewHolder.root;
        int[] location = view.getClickPosition();
        menuPopWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0] - menuWidth / 2, location[1] - menuHeight * 6 / 5);
    }

    protected void editData() {
        RecordEntity r = mList.get(preIndex);
        editPopWindow.setData(r.getName(),r.getCount()).show();
    }

    protected void delData() {
        final RecordEntity r = mList.get(preIndex);
        new ConfirmPopWindow(mActivity)
                .setDialogDismiss(new IDialogDismiss() {
                    @Override
                    public void onDismiss(Result result, Object... values) {
                        if (result == Result.OK) {
                            RecordEntityOperator.del(r);
                            ItemStatisticalInformationOperator.del(r.getName(), r.getCount());
                            mList.remove(preIndex);
                            if (getItemCount() > 0) {
                                fatherAdapter.flushCurrent(fatherIndex);
                                preIndex = -1;
                            } else {
                                fatherAdapter.noData(fatherIndex);
                            }
                        }
                    }
                })
                .toConfirm("是否删除【" + r.getName() + "】的记录？");
    }

    @NonNull
    @Override
    public SonViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.show_details_item, viewGroup, false);
        return new SonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SonViewHolder sonViewHolder, int i) {
        RecordEntity entity = mList.get(i);
        sonViewHolder.showItemName.setText(entity.getName());
        sonViewHolder.showItemCount.setText(String.valueOf(entity.getCount()));
        if (entity.isSelected()) {
            sonViewHolder.showItemName.setTextColor(mContext.getColor(R.color.colorAccent));
            sonViewHolder.showItemCount.setTextColor(mContext.getColor(R.color.colorAccent));
        } else {
            sonViewHolder.showItemName.setTextColor(mContext.getColor(R.color.textColor));
            sonViewHolder.showItemCount.setTextColor(mContext.getColor(R.color.textColor));
        }
        sonViewHolder.root.setOnClickListener(new NoDoubleClickListener() {
            @Override
            public void onNoDoubleClick(View v) {
                click(sonViewHolder.getAdapterPosition());
                showMenu(sonViewHolder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    protected class SonViewHolder extends RecyclerView.ViewHolder {
        private DetailsItemGroupView root;
        private TextView showItemName;
        private TextView showItemCount;

        public SonViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            showItemName = itemView.findViewById(R.id.show_item_name);
            showItemName.setClickable(false);
            showItemCount = itemView.findViewById(R.id.show_item_count);
            showItemCount.setClickable(false);
        }
    }
}

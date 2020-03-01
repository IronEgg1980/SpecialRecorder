package th.yzw.specialrecorder.view.show_details;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import th.yzw.specialrecorder.DAO.ItemStatisticalInformationOperator;
import th.yzw.specialrecorder.DAO.RecordEntityOperator;
import th.yzw.specialrecorder.R;
import th.yzw.specialrecorder.interfaces.IDialogDismiss;
import th.yzw.specialrecorder.interfaces.MyClickListener;
import th.yzw.specialrecorder.interfaces.Result;
import th.yzw.specialrecorder.model.RecordEntity;
import th.yzw.specialrecorder.view.common.ConfirmPopWindow;
import th.yzw.specialrecorder.view.common.EditPopWindow;
import th.yzw.specialrecorder.view.common.MenuPopWindow;

public class ShowDetailsAdapter extends RecyclerView.Adapter {
    private List<RecordEntity> mList;
    private Activity mActivity;
    private boolean noData = false;
    private int preIndex = -1;
    private EditPopWindow editPopWindow;
    private ConfirmPopWindow confirmPopWindow;

    public List<RecordEntity> getList() {
        return mList;
    }

    public ShowDetailsAdapter(Activity activity,List<RecordEntity> list) {
        mActivity = activity;
        mList = list;
        editPopWindow = new EditPopWindow(activity, true);
        editPopWindow.setDialogDismiss(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if (result == Result.OK && preIndex != -1) {
                    RecordEntity r = mList.get(preIndex);
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
        confirmPopWindow = new ConfirmPopWindow(activity)
                .setDialogDismiss(new IDialogDismiss() {
            @Override
            public void onDismiss(Result result, Object... values) {
                if(result == Result.OK)
                    delData();
            }
        });
        updateList();
    }

    public void updateList() {
        int offset = mList.size();
        noData = false;
        List<RecordEntity> list = RecordEntityOperator.findOrderByDateDescWithOffset(offset);
        if (list != null && list.size() > 0) {
            mList.addAll(list);
            noData = true;
            notifyItemRangeChanged(offset,list.size());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mList.size()) {
            return 0;
        }
        return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == 0) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.show_details_item_footer, viewGroup, false);
            return new EmpthVH(view);
        }
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.show_details_item, viewGroup, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        if (i == mList.size()) {
            EmpthVH vh = (EmpthVH) viewHolder;
            String s = noData?"下面没有了": "加载中...";
            vh.textView.setText(s);
        } else {
            final VH vh = (VH) viewHolder;
            RecordEntity r = mList.get(i);
            vh.showItemName.setText(r.getName());
            vh.showItemCount.setText(String.valueOf(r.getCount()));
            vh.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    click(viewHolder.getAdapterPosition());
                    showMenu(vh.root,viewHolder.getAdapterPosition());
                }
            });
            if (r.isSelected()) {
                vh.showItemName.setTextColor(mActivity.getColor(R.color.colorAccent));
                vh.showItemCount.setTextColor(mActivity.getColor(R.color.colorAccent));
            } else {
                vh.showItemName.setTextColor(mActivity.getColor(R.color.textColor));
                vh.showItemCount.setTextColor(mActivity.getColor(R.color.textColor));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList.size() + 1;
    }

    private void click(int position) {
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

    private void showMenu(DetailsItemGroupView view, final int position) {
        int[] location = view.getClickPosition();
        String[] menuitem = {"修改", "删除"};
        Drawable[] icons = new Drawable[2];
        icons[0] = view.getContext().getDrawable(R.drawable.ic_edit_18dp);
        icons[1] = view.getContext().getDrawable(R.drawable.ic_delete_white_24dp);
        final MenuPopWindow popWindow = new MenuPopWindow(view, menuitem, icons);
        popWindow.setClickListener(new MyClickListener() {
            @Override
            public void OnClick(View view, Object o) {
                int index = (int) o;
                if (index == 0)
                    editData();
                else {
                    confirmPopWindow.toConfirm("是否删除【" + mList.get(position).getName() + "】的记录？");
                }
            }
        });
        popWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                click(position);
            }
        });
        View view1 = popWindow.getContentView();
        view1.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int width = view1.getMeasuredWidth();
        int height = view1.getMeasuredHeight();
        popWindow.showAtLocation(view, Gravity.NO_GRAVITY, location[0] - width / 2, location[1] - height - 20);
    }

    private void editData() {
        RecordEntity r = mList.get(preIndex);
        editPopWindow.setData(r.getName(), r.getCount())
                .show();
    }

    private void delData() {
        if(preIndex < 0 || preIndex >= mList.size())
            return;
        RecordEntity r = mList.get(preIndex);
        RecordEntityOperator.del(r);
        ItemStatisticalInformationOperator.del(r.getName(), r.getCount());
        mList.remove(preIndex);
        notifyItemRemoved(preIndex);
        notifyItemRangeChanged(preIndex,mList.size() - preIndex);
        preIndex = -1;
    }

    protected class VH extends RecyclerView.ViewHolder {
        private DetailsItemGroupView root;
        private TextView showItemName;
        private TextView showItemCount;

        public VH(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.root);
            showItemName = itemView.findViewById(R.id.show_item_name);
            showItemName.setClickable(false);
            showItemCount = itemView.findViewById(R.id.show_item_count);
            showItemCount.setClickable(false);
        }
    }

    protected class EmpthVH extends RecyclerView.ViewHolder {
        private TextView textView;

        public EmpthVH(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textview);
        }
    }
}

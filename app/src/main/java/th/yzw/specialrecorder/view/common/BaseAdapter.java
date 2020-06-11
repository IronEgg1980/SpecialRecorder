package th.yzw.specialrecorder.view.common;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter {
    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        private SparseArray<View> views;

        BaseViewHolder(@NonNull View itemView) {
            super(itemView);
            this.views = new SparseArray<>();
            this.itemView.setClickable(true);
            this.itemView.setLongClickable(true);
        }

        public <T extends View> T getView(int id) {
            View view = views.get(id);
            if (view == null) {
                view = itemView.findViewById(id);
                views.put(id, view);
            }
            return (T) view;
        }

        public BaseViewHolder setText(int id, String text) {
            TextView textView = getView(id);
            textView.setText(text);
            return this;
        }

        public BaseViewHolder setImage(int id, int resoursId) {
            ImageView imageView = getView(id);
            imageView.setImageResource(resoursId);
            return this;
        }

        public BaseViewHolder setImage(int id, Drawable drawable) {
            ImageView imageView = getView(id);
            imageView.setImageDrawable(drawable);
            return this;
        }
    }

    private int layoutId;
    protected int parentWidth;
    protected List<T> mList;

    public BaseAdapter(int layoutId, List<T> list) {
        this.layoutId = layoutId;
        this.mList = list;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        bindData((BaseViewHolder) holder, mList.get(position));
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.parentWidth = parent.getMeasuredWidth();
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new BaseViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public abstract void bindData(BaseViewHolder baseViewHolder, T data);
}

package com.xiaoban.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.model.BindingRelationItem;

import java.util.ArrayList;
import java.util.List;

public class BoundFamilyAdapter extends RecyclerView.Adapter<BoundFamilyAdapter.ViewHolder> {

<<<<<<< HEAD
    public interface OnUnbindClickListener {
        void onUnbind(BindingRelationItem item);
    }

    private List<BindingRelationItem> items = new ArrayList<>();
    private OnUnbindClickListener unbindListener;

    public void setOnUnbindClickListener(OnUnbindClickListener listener) {
        this.unbindListener = listener;
    }
=======
    private List<BindingRelationItem> items = new ArrayList<>();
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9

    public void setItems(List<BindingRelationItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

<<<<<<< HEAD
    public void removeItem(long relationId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == relationId) {
                items.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bound_family, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

<<<<<<< HEAD
    class ViewHolder extends RecyclerView.ViewHolder {
=======
    static class ViewHolder extends RecyclerView.ViewHolder {
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
        private final TextView tvAvatar;
        private final TextView tvName;
        private final TextView tvPhone;
        private final TextView tvBindInfo;
<<<<<<< HEAD
        private final TextView btnUnbind;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvBindInfo = itemView.findViewById(R.id.tv_bind_info);
<<<<<<< HEAD
            btnUnbind = itemView.findViewById(R.id.btn_unbind);
        }

        void bind(BindingRelationItem item) {
            String name = item.getChildNickname();
            tvName.setText(name);
            tvAvatar.setText(name.isEmpty() ? "家" : name.substring(0, 1));
            tvPhone.setVisibility(View.GONE);
=======
        }

        void bind(BindingRelationItem item) {
            String name = item.getDisplayNameForElder();
            tvName.setText(name);
            tvAvatar.setText(name.isEmpty() ? "家" : name.substring(0, 1));
            tvPhone.setText(item.getChildPhoneMasked() != null ? item.getChildPhoneMasked() : "");
            tvPhone.setVisibility(
                    item.getChildPhoneMasked() != null && !item.getChildPhoneMasked().isEmpty()
                            ? View.VISIBLE : View.GONE);
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9

            String bindLabel = item.getBindTypeLabel() != null ? item.getBindTypeLabel() : "设备码绑定";
            String bindTime = item.getBindTime() != null && item.getBindTime().length() >= 10
                    ? item.getBindTime().substring(0, 10) : "";
            tvBindInfo.setText(bindLabel + (bindTime.isEmpty() ? "" : " · " + bindTime));
<<<<<<< HEAD

            btnUnbind.setOnClickListener(v -> {
                if (unbindListener != null) {
                    unbindListener.onUnbind(item);
                }
            });
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
        }
    }
}

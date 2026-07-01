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

    private List<BindingRelationItem> items = new ArrayList<>();

    public void setItems(List<BindingRelationItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

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

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatar;
        private final TextView tvName;
        private final TextView tvPhone;
        private final TextView tvBindInfo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvBindInfo = itemView.findViewById(R.id.tv_bind_info);
        }

        void bind(BindingRelationItem item) {
            String name = item.getDisplayNameForElder();
            tvName.setText(name);
            tvAvatar.setText(name.isEmpty() ? "家" : name.substring(0, 1));
            tvPhone.setText(item.getChildPhoneMasked() != null ? item.getChildPhoneMasked() : "");
            tvPhone.setVisibility(
                    item.getChildPhoneMasked() != null && !item.getChildPhoneMasked().isEmpty()
                            ? View.VISIBLE : View.GONE);

            String bindLabel = item.getBindTypeLabel() != null ? item.getBindTypeLabel() : "设备码绑定";
            String bindTime = item.getBindTime() != null && item.getBindTime().length() >= 10
                    ? item.getBindTime().substring(0, 10) : "";
            tvBindInfo.setText(bindLabel + (bindTime.isEmpty() ? "" : " · " + bindTime));
        }
    }
}

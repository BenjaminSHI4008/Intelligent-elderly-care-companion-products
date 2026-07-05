package com.xiaoban.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.xiaoban.app.R;
import com.xiaoban.app.model.HealthConcern;
import java.util.ArrayList;
import java.util.List;

public class HealthConcernAdapter extends RecyclerView.Adapter<HealthConcernAdapter.ViewHolder> {

    private List<HealthConcern> concerns = new ArrayList<>();

    public void setConcerns(List<HealthConcern> concerns) {
        this.concerns = concerns;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_health_concern, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthConcern concern = concerns.get(position);
        holder.bind(concern);
    }

    @Override
    public int getItemCount() {
        return concerns.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvConcernLevel;
        TextView tvConcernTitle;
        TextView tvConcernDetail;
        TextView tvConcernTag;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConcernLevel = itemView.findViewById(R.id.tv_concern_level);
            tvConcernTitle = itemView.findViewById(R.id.tv_concern_title);
            tvConcernDetail = itemView.findViewById(R.id.tv_concern_detail);
            tvConcernTag = itemView.findViewById(R.id.tv_concern_tag);
        }

        void bind(HealthConcern concern) {
            switch (concern.getLevel()) {
                case "high":
                    tvConcernLevel.setText("🔴");
                    tvConcernTag.setText("紧急关注");
                    tvConcernTag.setBackgroundResource(R.drawable.bg_tag_attention);
                    break;
                case "medium":
                    tvConcernLevel.setText("⚠️");
                    tvConcernTag.setText("需关注");
                    tvConcernTag.setBackgroundResource(R.drawable.bg_tag_pending);
                    break;
                case "low":
                    tvConcernLevel.setText("ℹ️");
                    tvConcernTag.setText("建议留意");
                    tvConcernTag.setBackgroundResource(R.drawable.bg_tag_confirmed);
                    break;
            }
            tvConcernTitle.setText(concern.getTitle());
            tvConcernDetail.setText(concern.getDetail());
        }
    }
}

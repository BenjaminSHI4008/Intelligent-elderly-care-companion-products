package com.xiaoban.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.xiaoban.app.R;
import com.xiaoban.app.model.Topic;
import java.util.ArrayList;
import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.ViewHolder> {

    private List<Topic> topics = new ArrayList<>();

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Topic topic = topics.get(position);
        holder.bind(topic);
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTopicIcon;
        TextView tvTopicName;
        TextView tvTopicDetail;
        TextView tvTopicCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTopicIcon = itemView.findViewById(R.id.tv_topic_icon);
            tvTopicName = itemView.findViewById(R.id.tv_topic_name);
            tvTopicDetail = itemView.findViewById(R.id.tv_topic_detail);
            tvTopicCount = itemView.findViewById(R.id.tv_topic_count);
        }

        void bind(Topic topic) {
            tvTopicIcon.setText(topic.getIcon());
            tvTopicName.setText(topic.getName());
            tvTopicDetail.setText(topic.getDetail());
            tvTopicCount.setText(topic.getCount() + "次");
        }
    }
}

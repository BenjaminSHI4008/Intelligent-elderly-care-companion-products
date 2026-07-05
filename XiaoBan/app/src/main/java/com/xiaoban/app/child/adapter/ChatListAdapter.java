package com.xiaoban.app.child.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.model.ConversationSummary;
import com.xiaoban.app.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {

    public interface OnConversationClickListener {
        void onConversationClick(ConversationSummary item);
    }

    private final List<ConversationSummary> items = new ArrayList<>();
    private OnConversationClickListener listener;

    public void setOnConversationClickListener(OnConversationClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ConversationSummary> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_list_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConversationSummary item = items.get(position);
        String name = item.getPeerName() != null ? item.getPeerName() : "老人";
        holder.tvName.setText(name);
        holder.tvPreview.setText(item.getPreviewText());
        holder.tvTime.setText(TimeUtil.formatChatListTime(item.getLastTime()));
        holder.tvAvatar.setText(name.isEmpty() ? "老" : name.substring(0, 1));

        int unread = item.getUnreadCount();
        if (unread > 0) {
            holder.tvUnreadBadge.setVisibility(View.VISIBLE);
            holder.tvUnreadBadge.setText(unread > 99 ? "99+" : String.valueOf(unread));
        } else {
            holder.tvUnreadBadge.setVisibility(View.GONE);
        }

        holder.tvMute.setVisibility(item.isMuted() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvAvatar;
        final TextView tvUnreadBadge;
        final TextView tvName;
        final TextView tvPreview;
        final TextView tvTime;
        final TextView tvMute;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            tvUnreadBadge = itemView.findViewById(R.id.tv_unread_badge);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPreview = itemView.findViewById(R.id.tv_preview);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvMute = itemView.findViewById(R.id.tv_mute);
        }
    }
}

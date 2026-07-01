package com.xiaoban.app.elder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.model.ChatHistorySession;
import com.xiaoban.app.util.ChatHistoryStore;

import java.util.ArrayList;
import java.util.List;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.HistoryViewHolder> {

    private final List<ChatHistorySession> sessions = new ArrayList<>();
    private OnSessionClickListener listener;

    public interface OnSessionClickListener {
        void onSessionClick(ChatHistorySession session);
    }

    public void setOnSessionClickListener(OnSessionClickListener listener) {
        this.listener = listener;
    }

    public void setSessions(List<ChatHistorySession> newSessions) {
        sessions.clear();
        if (newSessions != null) {
            sessions.addAll(newSessions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_history_session, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        ChatHistorySession session = sessions.get(position);
        holder.bind(session, listener);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvMeta;
        TextView tvPreview;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_history_title);
            tvMeta = itemView.findViewById(R.id.tv_history_meta);
            tvPreview = itemView.findViewById(R.id.tv_history_preview);
        }

        void bind(ChatHistorySession session, OnSessionClickListener listener) {
            tvTitle.setText(session.getTitle());
            tvMeta.setText(ChatHistoryStore.formatDisplayTime(session.getUpdatedAt())
                    + " · " + session.getMessageCount() + "轮对话");
            tvPreview.setText(session.getPreview());
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSessionClick(session);
                }
            });
        }
    }
}

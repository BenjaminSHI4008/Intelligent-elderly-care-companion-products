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
    private OnSessionClickListener sessionClickListener;
    private OnSessionDeleteListener sessionDeleteListener;

    public interface OnSessionClickListener {
        void onSessionClick(ChatHistorySession session);
    }

    public interface OnSessionDeleteListener {
        void onSessionDelete(ChatHistorySession session);
    }

    public void setOnSessionClickListener(OnSessionClickListener listener) {
        this.sessionClickListener = listener;
    }

    public void setOnSessionDeleteListener(OnSessionDeleteListener listener) {
        this.sessionDeleteListener = listener;
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
        holder.bind(session, sessionClickListener, sessionDeleteListener);
    }

    @Override
    public int getItemCount() {
        return sessions.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvMeta;
        TextView tvPreview;
        View btnDelete;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_history_title);
            tvMeta = itemView.findViewById(R.id.tv_history_meta);
            tvPreview = itemView.findViewById(R.id.tv_history_preview);
            btnDelete = itemView.findViewById(R.id.btn_delete_history);
        }

        void bind(ChatHistorySession session, OnSessionClickListener clickListener,
                  OnSessionDeleteListener deleteListener) {
            tvTitle.setText(session.getTitle());
            tvMeta.setText(ChatHistoryStore.formatDisplayTime(session.getUpdatedAt())
                    + " · " + session.getMessageCount() + "轮对话");
            tvPreview.setText(session.getPreview());
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onSessionClick(session);
                }
            });
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onSessionDelete(session);
                }
            });
        }
    }
}

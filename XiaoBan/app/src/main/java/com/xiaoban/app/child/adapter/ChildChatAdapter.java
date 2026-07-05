package com.xiaoban.app.child.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xiaoban.app.R;
import com.xiaoban.app.model.Message;
import com.xiaoban.app.util.ImageUrlUtil;
import com.xiaoban.app.util.TimeUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChildChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TIME = 0;
    private static final int TYPE_INCOMING = 1;
    private static final int TYPE_OUTGOING = 2;
    private static final long TIME_GAP_MS = 5 * 60 * 1000L;

    private final List<ChatRow> rows = new ArrayList<>();
    private long currentUserId;
    private long elderId;
    private boolean groupMode = true;
    private String selfAvatarText = "我";
    private OnMessageActionListener actionListener;

    public interface OnMessageActionListener {
        void onRecallMessage(Message message);
    }

    public void setOnMessageActionListener(OnMessageActionListener listener) {
        this.actionListener = listener;
    }

    public void setCurrentUserId(long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setElderId(long elderId) {
        this.elderId = elderId;
        this.groupMode = elderId > 0;
    }

    public void setMessages(List<Message> messages) {
        rows.clear();
        if (messages == null || messages.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        Date lastTime = null;
        for (Message message : messages) {
            Date currentTime = message.getCreateTime();
            if (lastTime == null || currentTime.getTime() - lastTime.getTime() > TIME_GAP_MS) {
                rows.add(ChatRow.time(TimeUtil.formatTime(currentTime)));
            }
            lastTime = currentTime;
            boolean mine = message.getSenderId() == currentUserId;
            rows.add(ChatRow.message(message, mine));
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ChatRow row = rows.get(position);
        if (row.type == TYPE_TIME) {
            return TYPE_TIME;
        }
        return row.outgoing ? TYPE_OUTGOING : TYPE_INCOMING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_TIME) {
            return new TimeHolder(inflater.inflate(R.layout.item_chat_time_divider, parent, false));
        }
        if (viewType == TYPE_OUTGOING) {
            return new MessageHolder(inflater.inflate(R.layout.item_chat_message_outgoing, parent, false), true);
        }
        return new MessageHolder(inflater.inflate(R.layout.item_chat_message_incoming, parent, false), false);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatRow row = rows.get(position);
        if (holder instanceof TimeHolder) {
            ((TimeHolder) holder).bind(row.timeLabel);
            return;
        }
        MessageHolder messageHolder = (MessageHolder) holder;
        boolean outgoing = getItemViewType(position) == TYPE_OUTGOING;
        messageHolder.bind(row.message, outgoing, currentUserId, elderId, groupMode, selfAvatarText, actionListener);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class TimeHolder extends RecyclerView.ViewHolder {
        private final TextView tvTime;

        TimeHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time_divider);
        }

        void bind(String label) {
            tvTime.setText(label);
        }
    }

    static class MessageHolder extends RecyclerView.ViewHolder {
        private final boolean outgoingLayout;
        private final TextView tvSenderName;
        private final TextView tvAvatar;
        private final View elderRing;
        private final TextView tvBubbleText;
        private final ImageView ivBubblePhoto;

        MessageHolder(@NonNull View itemView, boolean outgoingLayout) {
            super(itemView);
            this.outgoingLayout = outgoingLayout;
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            tvAvatar = itemView.findViewById(R.id.tv_avatar);
            elderRing = itemView.findViewById(R.id.elder_ring);
            tvBubbleText = itemView.findViewById(R.id.tv_bubble_text);
            ivBubblePhoto = itemView.findViewById(R.id.iv_bubble_photo);
        }

        void bind(Message message, boolean outgoing, long currentUserId, long elderId, boolean groupMode,
                  String selfAvatarText, OnMessageActionListener listener) {
            String senderName = message.getSenderName();
            String avatarText = outgoing ? selfAvatarText : getAvatarText(senderName);
            tvAvatar.setText(avatarText);

            if (tvSenderName != null && groupMode && !outgoing) {
                tvSenderName.setVisibility(View.VISIBLE);
                tvSenderName.setText(senderName);
            } else if (tvSenderName != null) {
                tvSenderName.setVisibility(View.GONE);
            }

            if (elderRing != null) {
                boolean isElderSender = !outgoing && elderId > 0 && message.getSenderId() == elderId;
                elderRing.setVisibility(isElderSender ? View.VISIBLE : View.GONE);
            }

            itemView.setOnLongClickListener(v -> {
                if (listener != null && outgoing && message.canRecall(currentUserId)) {
                    listener.onRecallMessage(message);
                    return true;
                }
                return false;
            });

            if (message.isRecalled()) {
                ivBubblePhoto.setVisibility(View.GONE);
                tvBubbleText.setVisibility(View.VISIBLE);
                tvBubbleText.setText(message.getRecallDisplayText(outgoing));
                tvBubbleText.setTextColor(itemView.getContext().getColor(R.color.chat_list_preview));
                return;
            }

            tvBubbleText.setTextColor(0xFF000000);
            boolean isPhoto = "photo".equals(message.getType());

            if (isPhoto) {
                tvBubbleText.setVisibility(View.GONE);
                ivBubblePhoto.setVisibility(View.VISIBLE);
                if (message.getMediaUrl() != null && !message.getMediaUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(ImageUrlUtil.resolve(message.getMediaUrl()))
                            .centerCrop()
                            .into(ivBubblePhoto);
                } else {
                    ivBubblePhoto.setImageResource(R.drawable.bg_message_photo);
                }
            } else {
                ivBubblePhoto.setVisibility(View.GONE);
                tvBubbleText.setVisibility(View.VISIBLE);
                tvBubbleText.setText(message.getContent());
            }
        }

        private String getAvatarText(String name) {
            if (name == null || name.isEmpty()) {
                return "家";
            }
            return name.substring(0, 1);
        }
    }

    private static final class ChatRow {
        final int type;
        final Message message;
        final boolean outgoing;
        final String timeLabel;

        private ChatRow(int type, Message message, boolean outgoing, String timeLabel) {
            this.type = type;
            this.message = message;
            this.outgoing = outgoing;
            this.timeLabel = timeLabel;
        }

        static ChatRow time(String label) {
            return new ChatRow(TYPE_TIME, null, false, label);
        }

        static ChatRow message(Message message, boolean outgoing) {
            return new ChatRow(outgoing ? TYPE_OUTGOING : TYPE_INCOMING, message, outgoing, null);
        }
    }
}

package com.xiaoban.app.elder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xiaoban.app.R;
import com.xiaoban.app.model.Message;
import com.xiaoban.app.util.ImageUrlUtil;
import com.xiaoban.app.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messages = new ArrayList<>();
    private OnMessageClickListener listener;
    private long currentUserId = 0L;

    public interface OnMessageClickListener {
        void onMessageClick(Message message);
        void onVoiceClick(Message message);
        void onPhotoClick(Message message);
        void onRecallMessage(Message message);
    }

    public void setOnMessageClickListener(OnMessageClickListener listener) {
        this.listener = listener;
    }

    public void setCurrentUserId(long currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public Message getLatestMessage() {
        return messages.isEmpty() ? null : messages.get(0);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_card, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position), listener, currentUserId);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        View unreadIndicator;
        View unreadDot;
        TextView tvSenderName;
        TextView tvTime;
        LinearLayout contentContainer;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            unreadDot = itemView.findViewById(R.id.unread_dot);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            tvTime = itemView.findViewById(R.id.tv_time);
            contentContainer = itemView.findViewById(R.id.message_content_container);
        }

        void bind(Message message, OnMessageClickListener listener, long currentUserId) {
            boolean isMine = currentUserId > 0 && message.getSenderId() == currentUserId;
            tvSenderName.setText(isMine ? "我" : message.getSenderName());
            tvTime.setText(TimeUtil.formatMessageTime(message.getCreateTime()));

            boolean showUnread = !isMine && !message.isRead() && message.getReceiverId() == currentUserId;
            unreadIndicator.setVisibility(showUnread ? View.VISIBLE : View.GONE);
            unreadDot.setVisibility(showUnread ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMessageClick(message);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null && isMine && message.canRecall(currentUserId)) {
                    listener.onRecallMessage(message);
                    return true;
                }
                return false;
            });

            contentContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(itemView.getContext());

            if (message.isRecalled()) {
                View textView = inflater.inflate(R.layout.message_content_text, contentContainer, false);
                TextView tvText = textView.findViewById(R.id.tv_text_content);
                tvText.setText(message.getRecallDisplayText(isMine));
                tvText.setTextColor(itemView.getContext().getColor(R.color.chat_list_preview));
                contentContainer.addView(textView);
                return;
            }

            switch (message.getType()) {
                case "voice":
                    View voiceView = inflater.inflate(R.layout.message_content_voice, contentContainer, false);
                    TextView tvDuration = voiceView.findViewById(R.id.tv_duration);
                    tvDuration.setText(formatDuration(message.getDuration()));
                    voiceView.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onVoiceClick(message);
                        }
                    });
                    contentContainer.addView(voiceView);
                    break;
                case "photo":
                    View photoView = inflater.inflate(R.layout.message_content_photo, contentContainer, false);
                    ImageView ivPhoto = photoView.findViewById(R.id.iv_photo_thumbnail);
                    if (message.getMediaUrl() != null && !message.getMediaUrl().isEmpty()) {
                        Glide.with(itemView.getContext())
                                .load(ImageUrlUtil.resolve(message.getMediaUrl()))
                                .centerCrop()
                                .into(ivPhoto);
                    } else {
                        ivPhoto.setImageResource(R.drawable.bg_message_photo);
                    }
                    photoView.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onPhotoClick(message);
                        }
                    });
                    contentContainer.addView(photoView);
                    break;
                case "text":
                default:
                    View textView = inflater.inflate(R.layout.message_content_text, contentContainer, false);
                    TextView tvText = textView.findViewById(R.id.tv_text_content);
                    tvText.setText(message.getContent());
                    contentContainer.addView(textView);
                    break;
            }
        }

        private String formatDuration(int seconds) {
            int min = seconds / 60;
            int sec = seconds % 60;
            return String.format("%d:%02d", min, sec);
        }
    }
}

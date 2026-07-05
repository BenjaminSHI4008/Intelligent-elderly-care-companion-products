package com.xiaoban.app.elder.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ELDER = 1;
    private static final int TYPE_AI = 2;
    private static final int TYPE_WARNING = 3;
    private static final int TYPE_CORRECTION = 4;

    private List<ChatItem> items = new ArrayList<>();
    private OnReplayClickListener replayListener;

    public interface OnReplayClickListener {
        void onReplay(String text);
    }

    public void setReplayListener(OnReplayClickListener listener) {
        this.replayListener = listener;
    }

    public void clearMessages() {
        items.clear();
        notifyDataSetChanged();
    }

    public void addElderMessage(String text) {
        if (!hasMeaningfulText(text)) {
            return;
        }
        items.add(new ChatItem(TYPE_ELDER, text.trim(), null));
        notifyItemInserted(items.size() - 1);
    }

    public void addAiMessage(String text, String category) {
        if (!hasMeaningfulText(text)) {
            return;
        }
        items.add(new ChatItem(TYPE_AI, text.trim(), category));
        notifyItemInserted(items.size() - 1);

        if ("health".equals(category)) {
            items.add(new ChatItem(TYPE_WARNING, "⚠️ 此回答已发给您的孩子确认，最好也问问医生哦", null));
            notifyItemInserted(items.size() - 1);
        } else if ("urgent".equals(category)) {
            items.add(new ChatItem(TYPE_WARNING, "⚠️ 建议您让家人知道，必要时拨打120", null));
            notifyItemInserted(items.size() - 1);
        }
    }

    public void addCorrectionMessage(String senderName, String text) {
        if (!hasMeaningfulText(text)) {
            return;
        }
        items.add(new ChatItem(TYPE_CORRECTION, "您" + senderName + "说，" + text.trim(), null));
        notifyItemInserted(items.size() - 1);
    }

    private static boolean hasMeaningfulText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String value = text.trim();
        for (int offset = 0; offset < value.length(); ) {
            int codePoint = value.codePointAt(offset);
            if (Character.isLetterOrDigit(codePoint)) {
                return true;
            }
            offset += Character.charCount(codePoint);
        }
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_ELDER:
                return new ElderViewHolder(inflater.inflate(R.layout.item_chat_elder, parent, false));
            case TYPE_AI:
                return new AiViewHolder(inflater.inflate(R.layout.item_chat_ai, parent, false));
            case TYPE_WARNING:
                return new WarningViewHolder(inflater.inflate(R.layout.item_chat_warning, parent, false));
            case TYPE_CORRECTION:
                return new CorrectionViewHolder(inflater.inflate(R.layout.item_chat_correction, parent, false));
            default:
                throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem item = items.get(position);
        if (holder instanceof ElderViewHolder) {
            ((ElderViewHolder) holder).bind(item);
        } else if (holder instanceof AiViewHolder) {
            ((AiViewHolder) holder).bind(item, replayListener);
        } else if (holder instanceof WarningViewHolder) {
            ((WarningViewHolder) holder).bind(item);
        } else if (holder instanceof CorrectionViewHolder) {
            ((CorrectionViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ChatItem {
        int type;
        String text;
        String category;

        ChatItem(int type, String text, String category) {
            this.type = type;
            this.text = text;
            this.category = category;
        }
    }

    static class ElderViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        ElderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_elder_message);
        }

        void bind(ChatItem item) {
            tvMessage.setText(item.text);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        View btnReplay;

        AiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_ai_message);
            btnReplay = itemView.findViewById(R.id.btn_replay);
        }

        void bind(ChatItem item, OnReplayClickListener listener) {
            tvMessage.setText(item.text);
            btnReplay.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReplay(item.text);
                }
            });
        }
    }

    static class WarningViewHolder extends RecyclerView.ViewHolder {
        TextView tvWarning;

        WarningViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWarning = itemView.findViewById(R.id.tv_warning);
        }

        void bind(ChatItem item) {
            tvWarning.setText(item.text);
        }
    }

    static class CorrectionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCorrection;

        CorrectionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCorrection = itemView.findViewById(R.id.tv_correction);
        }

        void bind(ChatItem item) {
            tvCorrection.setText(item.text);
        }
    }
}

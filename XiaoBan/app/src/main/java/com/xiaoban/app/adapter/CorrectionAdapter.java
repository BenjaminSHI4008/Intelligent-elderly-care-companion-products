package com.xiaoban.app.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.xiaoban.app.R;
import com.xiaoban.app.model.Correction;
import java.util.ArrayList;
import java.util.List;

public class CorrectionAdapter extends RecyclerView.Adapter<CorrectionAdapter.ViewHolder> {

    private List<Correction> corrections = new ArrayList<>();
    private OnCorrectionActionListener listener;

    public interface OnCorrectionActionListener {
        void onConfirm(Correction correction);
        void onCorrect(Correction correction, String correctedAnswer);
    }

    public void setCorrections(List<Correction> corrections) {
        this.corrections = corrections;
        notifyDataSetChanged();
    }

    public void setOnActionListener(OnCorrectionActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_correction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Correction correction = corrections.get(position);
        holder.bind(correction);
    }

    @Override
    public int getItemCount() {
        return corrections.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;
        TextView tvStatus;
        TextView tvElderQuestion;
        TextView tvAiAnswer;
        TextView btnConfirm;
        TextView btnCorrect;

        ViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_correction_time);
            tvStatus = itemView.findViewById(R.id.tv_correction_status);
            tvElderQuestion = itemView.findViewById(R.id.tv_elder_question);
            tvAiAnswer = itemView.findViewById(R.id.tv_ai_answer);
            btnConfirm = itemView.findViewById(R.id.btn_confirm);
            btnCorrect = itemView.findViewById(R.id.btn_correct);
        }

        void bind(Correction correction) {
            tvTime.setText(correction.getTime());
            tvElderQuestion.setText(correction.getElderQuestion());
            tvAiAnswer.setText(correction.getAiAnswer());

            String status = correction.getStatus();
            if ("pending".equals(status)) {
                tvStatus.setText("待确认");
                tvStatus.setBackgroundResource(R.drawable.bg_tag_pending);
                btnConfirm.setVisibility(View.VISIBLE);
                btnCorrect.setVisibility(View.VISIBLE);
            } else if ("confirmed".equals(status)) {
                tvStatus.setText("已确认");
                tvStatus.setBackgroundResource(R.drawable.bg_tag_confirmed);
                btnConfirm.setVisibility(View.GONE);
                btnCorrect.setVisibility(View.GONE);
            } else if ("corrected".equals(status)) {
                tvStatus.setText("已纠正");
                tvStatus.setBackgroundResource(R.drawable.bg_tag_attention);
                btnConfirm.setVisibility(View.GONE);
                btnCorrect.setVisibility(View.GONE);
                if (correction.getCorrectedAnswer() != null) {
                    tvAiAnswer.setText(correction.getCorrectedAnswer());
                }
            }

            btnConfirm.setOnClickListener(v -> {
                if (listener != null) listener.onConfirm(correction);
            });

            btnCorrect.setOnClickListener(v -> showCorrectionDialog(correction));
        }

        private void showCorrectionDialog(Correction correction) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setTitle("输入正确答案");

            final EditText input = new EditText(itemView.getContext());
            input.setHint("请输入正确的答案内容");
            input.setMinLines(3);
            input.setPadding(40, 20, 40, 20);
            builder.setView(input);

            builder.setPositiveButton("确定", (dialog, which) -> {
                String correctedAnswer = input.getText().toString().trim();
                if (!correctedAnswer.isEmpty() && listener != null) {
                    listener.onCorrect(correction, correctedAnswer);
                }
            });
            builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
            builder.show();
        }
    }
}

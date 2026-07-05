package com.xiaoban.app.child;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoban.app.R;
import com.xiaoban.app.adapter.CorrectionAdapter;
import com.xiaoban.app.base.BaseActivity;
import com.xiaoban.app.model.Correction;

import java.util.ArrayList;
import java.util.List;

public class ChildCorrectActivity extends BaseActivity {

    private ImageView ivBack;
    private RecyclerView rvCorrections;
    private LinearLayout emptyView;
    private CorrectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_correct);

        initViews();
        setupRecyclerView();
        loadCorrections();
    }

    private void initViews() {
        ivBack = findViewById(R.id.iv_back);
        rvCorrections = findViewById(R.id.rv_corrections);
        emptyView = findViewById(R.id.empty_view);

        ivBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new CorrectionAdapter();
        rvCorrections.setLayoutManager(new LinearLayoutManager(this));
        rvCorrections.setAdapter(adapter);

        adapter.setOnActionListener(new CorrectionAdapter.OnCorrectionActionListener() {
            @Override
            public void onConfirm(Correction correction) {
                confirmCorrection(correction);
            }

            @Override
            public void onCorrect(Correction correction, String correctedAnswer) {
                correctAnswer(correction, correctedAnswer);
            }
        });
    }

    private void loadCorrections() {
        loadTestData();
    }

    private void loadTestData() {
        List<Correction> corrections = new ArrayList<>();

        corrections.add(new Correction(
                "1",
                "我的降压药是早上吃还是晚上吃？",
                "根据您的用药记录，降压药建议每天早上7点服用，饭后半小时效果最佳。",
                "今天 14:32",
                "pending"
        ));

        corrections.add(new Correction(
                "2",
                "今天天气适合出门散步吗？",
                "今天天气晴朗，温度适宜，非常适合外出散步。建议您上午10点或下午4点左右出门，记得戴帽子防晒。",
                "今天 10:15",
                "pending"
        ));

        corrections.add(new Correction(
                "3",
                "晚饭吃什么比较好？",
                "建议您晚餐可以准备一些清淡的食物，比如蒸鱼、炒青菜和小米粥，营养均衡又容易消化。",
                "昨天 17:20",
                "confirmed"
        ));

        adapter.setCorrections(corrections);
        updateEmptyView(corrections.isEmpty());
    }

    private void confirmCorrection(Correction correction) {
        correction.setStatus("confirmed");
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "已确认", Toast.LENGTH_SHORT).show();
    }

    private void correctAnswer(Correction correction, String correctedAnswer) {
        correction.setStatus("corrected");
        correction.setCorrectedAnswer(correctedAnswer);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "已提交纠正，将通知老人", Toast.LENGTH_SHORT).show();
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            rvCorrections.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rvCorrections.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}

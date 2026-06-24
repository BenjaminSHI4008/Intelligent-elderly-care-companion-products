package com.xiaoban.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoban.server.entity.AlertKeyword;
import com.xiaoban.server.mapper.AlertKeywordMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class KeywordDetectionService {

    private final AlertKeywordMapper alertKeywordMapper;

    private final Map<String, AlertKeyword> keywordMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void refresh() {
        keywordMap.clear();
        alertKeywordMapper.selectList(
                new LambdaQueryWrapper<AlertKeyword>().eq(AlertKeyword::getIsActive, 1)
        ).forEach(k -> keywordMap.put(k.getKeyword(), k));
    }

    public String detectCategory(String text) {
        boolean hasUrgent = false;
        boolean hasHealth = false;
        for (Map.Entry<String, AlertKeyword> entry : keywordMap.entrySet()) {
            if (text.contains(entry.getKey())) {
                String category = entry.getValue().getCategory();
                if ("urgent".equals(category)) {
                    hasUrgent = true;
                    break;
                } else if ("health".equals(category)) {
                    hasHealth = true;
                }
            }
        }
        if (hasUrgent) return "urgent";
        if (hasHealth) return "health";
        return "normal";
    }

    public List<String> detectKeywords(String text) {
        List<String> matched = new ArrayList<>();
        for (String keyword : keywordMap.keySet()) {
            if (text.contains(keyword)) {
                matched.add(keyword);
            }
        }
        return matched;
    }
}

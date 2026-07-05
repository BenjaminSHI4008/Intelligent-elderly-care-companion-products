package com.xiaoban.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoban.server.common.BusinessException;
import com.xiaoban.server.common.ResultCode;
import com.xiaoban.server.entity.Conversation;
import com.xiaoban.server.entity.DailyReport;
import com.xiaoban.server.entity.User;
import com.xiaoban.server.mapper.ConversationMapper;
import com.xiaoban.server.mapper.DailyReportMapper;
import com.xiaoban.server.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final UserMapper userMapper;
    private final ConversationMapper conversationMapper;
    private final DailyReportMapper dailyReportMapper;
    private final AiChatService aiChatService;
    private final PushService pushService;
    private final ObjectMapper objectMapper;

    @Scheduled(cron = "0 0 21 * * *")
    public void generateDailyReports() {
        LocalDate today = LocalDate.now();
        List<User> elders = userMapper.selectList(
                new LambdaQueryWrapper<User>().eq(User::getRole, "elder"));

        for (User elder : elders) {
            try {
                generateOrUpdateReport(elder.getUserId(), today);
            } catch (Exception e) {
                log.error("generate daily report failed, elderId={}", elder.getUserId(), e);
            }
        }
    }

    public DailyReport generateOrUpdateReport(Long elderId, LocalDate date) {
        User elder = userMapper.selectById(elderId);
        if (elder == null || !"elder".equals(elder.getRole())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        try {
            DailyReport generated = buildReport(elder, date);
            DailyReport existing = dailyReportMapper.selectOne(
                    new LambdaQueryWrapper<DailyReport>()
                            .eq(DailyReport::getElderId, elderId)
                            .eq(DailyReport::getReportDate, date)
                            .orderByDesc(DailyReport::getCreatedAt)
                            .orderByDesc(DailyReport::getId)
                            .last("LIMIT 1"));

            if (existing == null) {
                dailyReportMapper.insert(generated);
                pushReportGenerated(elder);
                return generated;
            }

            existing.setTotalConversations(generated.getTotalConversations());
            existing.setMoodScore(generated.getMoodScore());
            existing.setSummaryText(generated.getSummaryText());
            existing.setHealthFlags(generated.getHealthFlags());
            existing.setTopicDistribution(generated.getTopicDistribution());
            dailyReportMapper.updateById(existing);
            pushReportGenerated(elder);
            return existing;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("generate daily report failed, elderId={}, date={}", elderId, date, e);
            throw new BusinessException(ResultCode.SERVER_ERROR);
        }
    }

    private DailyReport buildReport(User elder, LocalDate date) throws Exception {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<Conversation> conversations = conversationMapper.selectList(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getElderId, elder.getUserId())
                        .eq(Conversation::getIsPrivate, 0)
                        .between(Conversation::getCreatedAt, start, end));

        DailyReport report = DailyReport.builder()
                .elderId(elder.getUserId())
                .reportDate(date)
                .totalConversations(conversations.size())
                .moodScore(3)
                .build();

        if (conversations.isEmpty()) {
            report.setSummaryText("今天还没有可展示的对话记录。");
            return report;
        }

        String context = conversations.stream()
                .map(c -> "老人：" + c.getUserQuestion() + "\n小伴：" + c.getAiAnswer())
                .collect(Collectors.joining("\n\n"));

        String prompt = "请分析以下老人和AI助手的对话记录，生成一份简洁的日报。要求："
                + "1.用一段话总结今天的对话内容，不超过200字；"
                + "2.列出健康相关的关注项，如果没有则返回空数组；"
                + "3.给出情绪评分，1-5分，1=很低落，5=很开心；"
                + "4.统计话题分布比例，健康/生活/情感/其他。"
                + "请用JSON格式返回：{\"summary\":\"...\",\"healthFlags\":[\"...\"],"
                + "\"moodScore\":3,\"topics\":{\"健康\":30,\"生活\":40,\"情感\":20,\"其他\":10}}"
                + "\n\n对话记录：\n" + context;

        String aiResponse = aiChatService.chat(prompt, "normal");
        try {
            int startIndex = aiResponse.indexOf('{');
            int endIndex = aiResponse.lastIndexOf('}') + 1;
            if (startIndex >= 0 && endIndex > startIndex) {
                JsonNode json = objectMapper.readTree(aiResponse.substring(startIndex, endIndex));
                report.setSummaryText(json.path("summary").asText());
                report.setHealthFlags(json.path("healthFlags").toString());
                report.setMoodScore(json.path("moodScore").asInt(3));
                report.setTopicDistribution(json.path("topics").toString());
            } else {
                report.setSummaryText(aiResponse.substring(0, Math.min(200, aiResponse.length())));
            }
        } catch (Exception e) {
            report.setSummaryText(aiResponse.substring(0, Math.min(200, aiResponse.length())));
        }
        return report;
    }

    private void pushReportGenerated(User elder) {
        pushService.pushToElderFamily(
                elder.getUserId(),
                "小伴日报",
                elder.getNickname() + "今日小伴日报已生成");
    }
}

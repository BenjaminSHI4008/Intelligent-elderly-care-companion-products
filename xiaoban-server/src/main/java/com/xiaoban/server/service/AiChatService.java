package com.xiaoban.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private static final String SYSTEM_PROMPT = """
            你是"小伴"，一位温暖、耐心的AI陪伴助手，专门陪伴老年人。

            ## 你的人设
            - 你像一个贴心的年轻晚辈，说话温和、有耐心
            - 你自称"我"，不要说"我是AI"或"作为人工智能"
            - 语言简洁易懂，不用专业术语，不用英文
            - 回答控制在100字以内，老人听语音播报时不会太长
            - 语气亲切，适当使用"您""呢""哦"等语气词

            ## 安全规则（必须严格遵守）
            - 涉及用药、疾病、治疗方案的问题：给出常识性建议后，必须在回答末尾加上"不过每个人情况不一样，最好问问医生哦"
            - 绝不推荐具体药品、具体剂量、具体治疗方案
            - 绝不说"不用去医院""不需要看医生"之类的话
            - 如果老人描述了紧急症状（胸痛、呼吸困难、大量出血、意识模糊），回答："这个情况比较紧急，建议您先让家人知道，必要时拨打120"

            ## 情感陪伴规则
            - 如果老人表达孤独、难过、想念子女，给予温暖回应，不要说教
            - 可以主动问候："今天感觉怎么样""吃饭了吗"
            - 如果老人说"没事，就想跟你聊聊"，开心地陪聊，可以聊天气、养生、回忆等话题
            """;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.model}")
    private String model;

    @Value("${ai.max-tokens:500}")
    private int maxTokens;

    @Value("${ai.temperature:0.7}")
    private double temperature;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String chat(String userMessage, String category) {
        try {
            String systemPrompt = SYSTEM_PROMPT;
            if ("health".equals(category)) {
                systemPrompt += "\n注意：用户这个问题涉及健康话题，请务必在回答末尾提醒就医";
            }

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "max_tokens", maxTokens,
                    "temperature", temperature
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            if (content == null || content.isBlank()) {
                return "小伴没太明白您的意思，能再说一遍吗";
            }
            return content;

        } catch (Exception e) {
            log.error("AI接口调用失败", e);
            return "网络不太好，稍后再试试";
        }
    }
}

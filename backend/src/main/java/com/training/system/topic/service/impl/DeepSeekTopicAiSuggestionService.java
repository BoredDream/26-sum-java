package com.training.system.topic.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.system.common.ResultCode;
import com.training.system.exception.BusinessException;
import com.training.system.topic.dto.TopicAiSuggestionDTO;
import com.training.system.topic.service.TopicAiSuggestionService;
import com.training.system.topic.vo.TopicAiSuggestionVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekTopicAiSuggestionService implements TopicAiSuggestionService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String apiUrl;
    private final String model;

    public DeepSeekTopicAiSuggestionService(
            ObjectMapper objectMapper,
            @Value("${deepseek.api-key:}") String apiKey,
            @Value("${deepseek.api-url:https://api.deepseek.com/chat/completions}") String apiUrl,
            @Value("${deepseek.model:deepseek-chat}") String model) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public TopicAiSuggestionVO suggest(TopicAiSuggestionDTO dto) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(ResultCode.ERROR, "DeepSeek API Key 未配置，请在 application-local.yml 中配置 deepseek.api-key");
        }

        try {
            String requestBody = objectMapper.writeValueAsString(buildPayload(dto));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(ResultCode.ERROR, "DeepSeek 调用失败，请稍后重试");
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (!content.isTextual() || !StringUtils.hasText(content.asText())) {
                throw new BusinessException(ResultCode.ERROR, "DeepSeek 未返回有效建议");
            }
            return new TopicAiSuggestionVO(content.asText().trim());
        } catch (BusinessException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new BusinessException(ResultCode.ERROR, "DeepSeek 响应解析失败");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ResultCode.ERROR, "DeepSeek 调用已中断");
        } catch (Exception ex) {
            throw new BusinessException(ResultCode.ERROR, "DeepSeek 调用异常，请稍后重试");
        }
    }

    private Map<String, Object> buildPayload(TopicAiSuggestionDTO dto) {
        return Map.of(
                "model", model,
                "temperature", 0.4,
                "messages", List.of(
                        Map.of("role", "system", "content", buildSystemPrompt()),
                        Map.of("role", "user", "content", buildUserPrompt(dto))
                )
        );
    }

    private String buildSystemPrompt() {
        return """
                你是高校 Java 综合实训选题顾问，面向出题教师提供建议。
                请只围绕教学选题质量给出中文建议，不要替教师直接保存或承诺审核结果。
                输出使用清晰的 Markdown，包含：
                1. 题目定位与亮点
                2. 可补充或收敛的需求
                3. 技术路线建议
                4. 难度与工作量评估
                5. 验收指标建议
                6. 风险提醒
                建议要具体、可执行，控制在 600 字以内。
                """;
    }

    private String buildUserPrompt(TopicAiSuggestionDTO dto) {
        return """
                请根据以下待新增题目信息，给出面向出题教师的完善建议。

                题目名称：%s
                题目类型：%s
                难度：%s
                单队学生上限：%s
                团队数量上限：%s
                题目内容：%s
                技术路线：%s
                开发工具：%s
                """.formatted(
                valueOrBlank(dto.getTopicName()),
                valueOrBlank(dto.getTopicType()),
                valueOrBlank(dto.getDifficulty()),
                valueOrBlank(dto.getStudentLimit()),
                valueOrBlank(dto.getTeamLimit()),
                valueOrBlank(dto.getTopicContent()),
                valueOrBlank(dto.getTechnicalRoute()),
                valueOrBlank(dto.getDevelopTools())
        );
    }

    private String valueOrBlank(Object value) {
        return value == null || !StringUtils.hasText(String.valueOf(value)) ? "未填写" : String.valueOf(value);
    }
}

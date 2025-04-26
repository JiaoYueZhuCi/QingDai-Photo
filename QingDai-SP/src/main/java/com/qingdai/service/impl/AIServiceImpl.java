package com.qingdai.service.impl;

import com.qingdai.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIServiceImpl implements AIService {

    private static final Logger logger = LoggerFactory.getLogger(AIServiceImpl.class);

    @Value("${spring.ai.siliconflow.api-key}")
    private String apiKey;

    @Value("${spring.ai.siliconflow.base-url}")
    private String baseUrl;

    @Value("${spring.ai.siliconflow.model}")
    private String model;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String generateTest(String apiDescription) {
        return callSiliconflowWithPrompt(apiDescription, "你是一个专业的API测试专家，请根据API描述生成测试用例。");
    }

    private String callSiliconflowWithPrompt(String input, String systemPrompt) {
        try {
            // 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            // 构造请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", createMessages(input, systemPrompt));
            requestBody.put("temperature", 0.7);
            requestBody.put("top_p", 0.7);
            requestBody.put("max_tokens", 2000);
            requestBody.put("frequency_penalty", 0.5);
            requestBody.put("n", 1);

            // 发送请求
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> responseEntity = restTemplate.exchange(baseUrl + "/v1/chat/completions", HttpMethod.POST, requestEntity, Map.class);

            // 解析响应
            Map<String, Object> responseBody = responseEntity.getBody();
            if (responseBody != null && responseBody.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            throw new RuntimeException("Invalid response from AI service");

        } catch (Exception e) {
            logger.error("调用AI服务出错", e);
            throw new RuntimeException("调用AI服务出错: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> createMessages(String input, String systemPrompt) {
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", input);
        messages.add(userMessage);

        return messages;
    }
}
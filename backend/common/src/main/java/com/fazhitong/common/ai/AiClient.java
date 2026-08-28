package com.fazhitong.common.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 通用 AI 大模型客户端（OpenAI 兼容协议）。
 * 支持 DeepSeek / 通义千问(兼容模式) / Moonshot Kimi / 智谱GLM / OpenAI 等。
 * 通过环境变量配置：
 *   AI_API_KEY   - 必填，未配置时 isEnabled() 返回 false
 *   AI_BASE_URL  - 默认 https://api.deepseek.com/v1
 *   AI_MODEL     - 默认 deepseek-chat
 */
public final class AiClient {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AiClient() {
    }

    /** 是否已配置可用的 API Key */
    public static boolean isEnabled() {
        return !apiKey().isBlank();
    }

    /**
     * 发起一次对话补全，返回模型回复文本；失败或未配置时返回 null，由调用方降级。
     */
    public static String chat(String systemPrompt, String userPrompt) {
        if (!isEnabled()) {
            return null;
        }
        try {
            ObjectNode body = MAPPER.createObjectNode();
            body.put("model", model());
            body.put("temperature", 0.7);
            ArrayNode messages = body.putArray("messages");
            ObjectNode sys = messages.addObject();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            ObjectNode user = messages.addObject();
            user.put("role", "user");
            user.put("content", userPrompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl() + "/chat/completions"))
                    .timeout(Duration.ofSeconds(120))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return null;
            }
            JsonNode root = MAPPER.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            String text = content.isMissingNode() ? null : content.asText(null);
            return (text == null || text.isBlank()) ? null : text.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private static String apiKey() {
        return env("AI_API_KEY", "");
    }

    private static String baseUrl() {
        return env("AI_BASE_URL", "https://api.deepseek.com/v1");
    }

    private static String model() {
        return env("AI_MODEL", "deepseek-chat");
    }

    private static String env(String key, String defaultValue) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            v = System.getProperty(key);
        }
        return (v == null || v.isBlank()) ? defaultValue : v.trim();
    }
}

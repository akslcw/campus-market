package com.campus.market.service.ai;

import com.campus.market.config.DeepSeekProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekClient {

    private final DeepSeekProperties props;
    private RestClient client;

    private RestClient client() {
        if (client == null) {
            // 连接超时 + 读取超时，都用配置里的 timeout（默认10s），超时即抛异常，由上层降级
            SimpleClientHttpRequestFactory factory = new
                    SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(props.getTimeout());
            factory.setReadTimeout(props.getTimeout());

            client = RestClient.builder()
                    .requestFactory(factory)
                    .baseUrl(props.getBaseUrl())
                    .defaultHeader("Authorization", "Bearer " + props.getApiKey())
                    .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .build();
        }
        return client;
    }

    @SuppressWarnings("unchecked")
    public String chat(String systemPrompt, String userPrompt) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> body = Map.of(
                    "model", props.getModel(),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.7,
                    "stream", false
            );

            Map<String, Object> resp = client()
                    .post()
                    .uri("/chat/completions")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            if (resp == null) throw new RuntimeException("DeepSeek 返回为空");

            List<Map<String, Object>> choices = (List<Map<String, Object>>)
                    resp.get("choices");
            Map<String, Object> message = (Map<String, Object>)
                    choices.get(0).get("message");
            String content = (String) message.get("content");

            log.info("DeepSeek 调用成功，耗时 {}ms", System.currentTimeMillis() -
                    start);
            return content;
        } catch (Exception e) {
            log.error("DeepSeek 调用失败，耗时 {}ms，原因: {}",
                    System.currentTimeMillis() - start, e.getMessage());
            throw new RuntimeException("AI 服务暂不可用: " + e.getMessage(), e);
        }
    }
}
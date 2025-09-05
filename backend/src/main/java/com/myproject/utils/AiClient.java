/**
 * 文件路径: src/main/java/com/myproject/utils/AiClient.java
 * 说明:
 *  - 调用 DeepSeek（或类似 chat/completions）接口的客户端封装；
 *  - 优化了从 AI 文本回复中抽取内嵌 JSON 的逻辑（支持 ```json 包裹、以及不完整换行等情况）；
 *  - 返回优先为已解析的 JsonNode，若无法解析则返回 { "text": "...原始文本..." } 形式的包装节点。
 *
 * 使用说明:
 *  - 请在 application.yml 中配置 deepseek.base-url / deepseek.api-path / deepseek.api-key / deepseek.model
 *  - 替换后重启服务并调用 /api/ai/daily-analysis 验证。
 */

package com.myproject.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
public class AiClient {

    private static final Logger log = LoggerFactory.getLogger(AiClient.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${deepseek.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${deepseek.api-path:/chat/completions}")
    private String apiPath;

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    @Value("${deepseek.max-prompt-chars:3000}")
    private int maxPromptChars;

    private final RestTemplate restTemplate = new RestTemplate();


    /**
     * 使用 DeepSeek 的 chat/completions 接口发送 systemPrompt + userQuestion（消息序列）
     * 返回原始 JsonNode（方便上层解析）
     */
    public JsonNode chatWithContext(String systemPrompt, String userQuestion) throws Exception {
        String url = baseUrl + apiPath;
        log.info("准备调用 DeepSeek: url={}, model={}", url, model);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);

        // 构造 messages 数组
        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            Map<String, String> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.add(sys);
        }
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userQuestion == null ? "" : userQuestion);
        messages.add(userMsg);

        body.put("messages", messages);
        body.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // DeepSeek 的鉴权方式：使用 api-key 或者 Bearer，按你配置调整
        // 这里采用 Authorization: Bearer <apiKey>
        headers.setBearerAuth(apiKey);

        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(body), headers);

        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (resp.getStatusCode().is2xxSuccessful()) {
            String respBody = resp.getBody();
            log.debug("DeepSeek 返回长度={}", respBody == null ? 0 : respBody.length());
            try {
                JsonNode node = mapper.readTree(respBody);
                // 一些实现会把解析出的内嵌 JSON 放到 choices[0].message.content 中，或直接把结构化 JSON 返回
                return node;
            } catch (Exception e) {
                // 返回的不是 JSON，包装为 text 字段
                log.warn("DeepSeek 返回无法解析为 JSON，包装为 text 字段");
                Map<String, Object> wrap = new HashMap<>();
                wrap.put("text", respBody);
                return mapper.valueToTree(wrap);
            }
        } else {
            log.error("DeepSeek 调用失败，status={}, body={}", resp.getStatusCode().value(), resp.getBody());
            throw new RuntimeException("DeepSeek 调用失败: " + resp.getStatusCode().value());
        }
    }

    /**
     * 将 payloadJson 发送给 DeepSeek 并尽力返回解析后的 JsonNode。
     * 若无法解析 JSON 则返回 {"text": "...raw..."} 结构以便上层存储和排查。
     */
    public JsonNode generateReport(String payloadJson) throws Exception {
        if (payloadJson != null && payloadJson.length() > maxPromptChars) {
            log.warn("payloadJson 长度 {} 超过限制 {}，将截断发送（仅作警告）", payloadJson.length(), maxPromptChars);
            payloadJson = payloadJson.substring(0, Math.min(payloadJson.length(), maxPromptChars));
        }

        String url = (baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl) + apiPath;
        log.info("准备调用 DeepSeek: url={}, model={}", url, model);

        // system + user messages
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> sys = new HashMap<>();
        sys.put("role", "system");
        sys.put("content", "你是营养分析助手，请严格使用“简体中文”输出结果。\n\n任务：根据用户提供的 JSON（包含 meals 列表、images、description、calories 等），推断当天饮食的简要评估，并返回一个“仅包含 JSON 的回复”（不要输出任何解释性文字）。\n\n输出格式（字段名固定，英文）：{\n  healthScore: number, // 0-100，保留整数或一位小数\n  nutrition: { carbGrams: number, proteinGrams: number, fatGrams: number, summary: string },\n  suggestions: string[3], // 3 条，面向用户、简短可执行，中文\n  improvements: string[3] // 3 条，中文\n}\n\n要求：\n- 所有说明性文本（summary/suggestions/improvements）必须用简体中文，不要出现英文；\n- 即使信息不完整也要做合理估计，绝不报错；\n- 数值可四舍五入为整数；\n- 回复中不要包含除上述 JSON 以外的任何内容。");
        messages.add(sys);

        Map<String, Object> usr = new HashMap<>();
        usr.put("role", "user");
        usr.put("content", payloadJson == null ? "{}" : payloadJson);
        messages.add(usr);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", false);
        body.put("temperature", 0.0);

        String requestBody = mapper.writeValueAsString(body);
        log.debug("发送给 DeepSeek 请求体长度={}, 预览={}", requestBody.length(), requestBody.length() > 300 ? requestBody.substring(0, 300) + "..." : requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (StringUtils.hasText(apiKey)) {
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("x-api-key", apiKey);
        }

        HttpEntity<String> req = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> respEntity;
        try {
            respEntity = restTemplate.exchange(url, HttpMethod.POST, req, String.class);
        } catch (Exception ex) {
            log.error("调用 DeepSeek 接口失败: {}", ex.toString());
            throw ex;
        }

        if (respEntity == null || respEntity.getBody() == null) {
            log.warn("DeepSeek 返回 body 为空");
            ObjectNode wrapper = mapper.createObjectNode();
            wrapper.put("text", "");
            wrapper.put("parsedFromText", false);
            return wrapper;
        }

        String respText = respEntity.getBody();
        log.debug("DeepSeek 原始返回长度={}, 预览={}", respText.length(), respText.length() > 300 ? respText.substring(0, 300) + "..." : respText);

        // 尝试直接解析整段返回为 JSON（很多 API 返回结构化 JSON）
        try {
            JsonNode root = mapper.readTree(respText);

            // 尝试抽取常见路径中的文本 content（choices[].message.content / text / message.content）
            JsonNode contentCandidate = null;
            if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
                JsonNode first = root.get("choices").get(0);
                if (first.has("message") && first.get("message").has("content")) contentCandidate = first.get("message").get("content");
                else if (first.has("text")) contentCandidate = first.get("text");
            } else if (root.has("text")) {
                contentCandidate = root.get("text");
            } else if (root.has("message") && root.get("message").has("content")) {
                contentCandidate = root.get("message").get("content");
            }

            if (contentCandidate != null && contentCandidate.isTextual()) {
                String contentText = contentCandidate.asText();
                JsonNode extracted = tryParseEmbeddedJson(contentText);
                if (extracted != null) {
                    log.info("从 choices/message/text 中解析出内嵌 JSON，优先返回该 JSON");
                    return extracted;
                } else {
                    log.info("choices/message/text 中没有可解析的内嵌 JSON，返回 root（可能已是结构化 JSON）");
                    return root;
                }
            } else {
                log.debug("DeepSeek 返回为 JSON 且没有内嵌 text 字段，直接返回 root");
                return root;
            }
        } catch (Exception ex) {
            // 无法直接 parse 整体 JSON，尝试从原始文本中抽取内嵌 JSON（例如被 ```json 包裹）
            log.warn("AI 回复整体无法解析为 JSON，尝试从原始文本中抽取 JSON。长度={}", respText.length());
            JsonNode extracted = tryParseEmbeddedJson(respText);
            if (extracted != null) {
                ObjectNode wrapper = mapper.createObjectNode();
                wrapper.setAll((ObjectNode) extracted);
                wrapper.put("parsedFromText", true);
                wrapper.put("rawText", respText);
                return wrapper;
            } else {
                // 全部解析失败，返回原文包装
                ObjectNode wrapper = mapper.createObjectNode();
                wrapper.put("text", respText);
                wrapper.put("parsedFromText", false);
                return wrapper;
            }
        }
    }

    /**
     * 更稳健的内嵌 JSON 抽取器：
     * - 去掉常见的 ```json / ``` 标记；
     * - 从文本中找到第一个 '{'，然后向后扫描直到找到它对应的闭合 '}'（基于计数），以保证 JSON block 完整；
     * - 如果找不到成对的大括号，则退回到用正则寻找第一个 {...}（保底），仍解析失败则返回 null。
     */
    public JsonNode tryParseEmbeddedJson(String text) {
        if (text == null) return null;
        try {
            // 1. 简单清理代码块标记（忽略大小写）
            String cleaned = text.replaceAll("(?i)```\\s*json\\s*", "```")
                    .replaceAll("(?i)```", "");
            cleaned = cleaned.trim();

            // 2. 找到第一个 '{'
            int firstBrace = cleaned.indexOf('{');
            if (firstBrace >= 0) {
                int len = cleaned.length();
                int depth = 0;
                boolean inString = false;
                char prevChar = 0;
                for (int i = firstBrace; i < len; i++) {
                    char ch = cleaned.charAt(i);
                    // 处理字符串内的大括号、转义等
                    if (ch == '"' && prevChar != '\\') {
                        inString = !inString;
                    }
                    if (!inString) {
                        if (ch == '{') depth++;
                        else if (ch == '}') depth--;
                        if (depth == 0) {
                            // 找到匹配结束位置 i
                            String candidate = cleaned.substring(firstBrace, i + 1).trim();
                            try {
                                JsonNode node = mapper.readTree(candidate);
                                return node;
                            } catch (Exception e) {
                                // 如果解析失败，继续尝试后面的内容或回退到正则
                                log.debug("基于配对大括号抽取的 candidate 无法解析为 JSON，长度={}，错误={}", candidate.length(), e.toString());
                                break;
                            }
                        }
                    }
                    prevChar = ch;
                }
            }

            // 3. 保底：使用正则寻找第一个 {...} 匹配（原实现），仍需谨慎
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\{(.|\\r|\\n)*?\\}", java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher m = p.matcher(cleaned);
            if (m.find()) {
                String jsonStr = m.group(0).trim();
                try {
                    JsonNode node = mapper.readTree(jsonStr);
                    return node;
                } catch (Exception ex) {
                    log.warn("正则方式提取到的 JSON 仍然解析失败: {}", ex.toString());
                    return null;
                }
            }

            log.debug("tryParseEmbeddedJson 未找到内嵌 JSON，文本预览: {}", cleaned.length() > 200 ? cleaned.substring(0, 200) + "..." : cleaned);
            return null;
        } catch (Exception ex) {
            log.warn("tryParseEmbeddedJson 解析异常: {}", ex.toString());
            return null;
        }
    }
}

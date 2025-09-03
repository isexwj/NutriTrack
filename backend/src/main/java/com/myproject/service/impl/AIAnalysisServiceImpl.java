/**
 * 文件路径: src/main/java/com/myproject/service/impl/AIAnalysisServiceImpl.java
 * 说明: AI 分析服务实现（包含从 AI 返回文本中抽取 JSON、填充独立列并保存备份的完整逻辑）
 */

package com.myproject.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.myproject.entity.DailyDietAnalysis;
import com.myproject.entity.MealImage;
import com.myproject.entity.MealRecord;
import com.myproject.entity.User;
import com.myproject.mapper.DailyDietAnalysisMapper;
import com.myproject.mapper.MealImageMapper;
import com.myproject.mapper.MealRecordMapper;
import com.myproject.mapper.UserMapper;
import com.myproject.service.AIAnalysisService;
import com.myproject.utils.AiClient;
import com.myproject.utils.HashUtils;
import com.myproject.vo.DailyAnalysisReportVO;
import com.myproject.vo.ChatMessageVO;
import com.myproject.vo.TrendResponseVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AIAnalysisServiceImpl.class);

    private final MealRecordMapper mealRecordMapper;
    private final MealImageMapper mealImageMapper;
    private final DailyDietAnalysisMapper reportMapper;
    private final UserMapper userMapper;
    private final AiClient aiClient;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DailyAnalysisReportVO getOrGenerateDailyAnalysis(Long userId, String username, LocalDate date) throws Exception {
        log.info("开始处理 getOrGenerateDailyAnalysis，userId={}, username={}, date={}", userId, username, date);

        // 1) resolve userId if null
        if (userId == null && username != null) {
            log.debug("userId 为空，尝试根据 username 查找 userId: {}", username);
            User u = userMapper.selectByUsername(username);
            if (u != null) {
                userId = u.getId();
                log.debug("通过 username 找到 userId={}", userId);
            } else {
                log.warn("未找到指定 username 的用户: {}", username);
                throw new IllegalArgumentException("未找到用户: " + username);
            }
        }

        if (date == null) date = LocalDate.now();
        log.debug("实际使用的 date={}", date);

        // 2) 查询当天的 meal_records
        List<MealRecord> meals;
        try {
            meals = mealRecordMapper.selectByUserAndDate(userId, date);
            log.info("查询到的 meal_records 数量: {}", meals == null ? 0 : meals.size());
        } catch (Exception e) {
            log.error("查询 meal_records 失败 userId={}, date={}", userId, date, e);
            throw e;
        }
        if (meals == null) meals = Collections.emptyList();

        // 如果当天没有记录，直接返回“无记录提示”的 VO（不写入数据库空记录）
        if (meals.isEmpty()) {
            log.info("用户当天没有饮食记录，直接返回提示报告 userId={}, date={}", userId, date);
            DailyAnalysisReportVO vo = new DailyAnalysisReportVO();
            vo.setHealthScore(0.0);
            vo.setNutritionSummary("今日没有饮食记录");
            vo.setSuggestions(Collections.singletonList("请添加饮食记录后再尝试生成分析报告"));
            vo.setImprovements(Collections.singletonList("暂无改进建议"));
            Map<String, Integer> nutrition = new HashMap<>();
            nutrition.put("carbs", 0);
            nutrition.put("protein", 0);
            nutrition.put("fat", 0);
            vo.setNutrition(nutrition);
            vo.setLastUpdated(LocalDate.now().toString());
            return vo;
        }

        // 3) 构造 payload（与之前逻辑一致）
        List<Map<String,Object>> mealPayload = new ArrayList<>();
        for (MealRecord m : meals) {
            Map<String,Object> mm = new LinkedHashMap<>();
            mm.put("id", m.getId());
            mm.put("mealType", m.getMealType() == null ? null : m.getMealType().toString());
            mm.put("description", m.getDescription());
            mm.put("calories", m.getCalories());
            mm.put("rating", m.getRating());

            List<MealImage> imgs = Collections.emptyList();
            try {
                imgs = mealImageMapper.selectByRecordId(m.getId());
            } catch (Exception e) {
                log.warn("查询 meal_images 失败, recordId={}, error={}", m.getId(), e.toString());
            }
            List<String> imgUrls = new ArrayList<>();
            if (imgs != null) {
                for (MealImage im : imgs) imgUrls.add(im.getImageUrl());
            }
            mm.put("images", imgUrls);
            mealPayload.add(mm);
        }
        log.debug("构造的 mealPayload 条目数: {}, 首项预览: {}", mealPayload.size(), mealPayload.size() > 0 ? mealPayload.get(0) : "empty");

        // 4) 计算 mealsHash
        String mealsHash = HashUtils.mealsHash(mealPayload);
        log.info("computed mealsHash={}", mealsHash);

        // 5) 检查缓存（DB）
        DailyDietAnalysis existing = null;
        try {
            existing = reportMapper.selectByUserAndDate(userId, date);
            log.debug("查询 daily_diet_analysis 结果: {}", existing == null ? "null" : ("id=" + existing.getId()));
        } catch (Exception e) {
            log.error("查询 daily_diet_analysis 表失败, userId={}, date={}", userId, date, e);
            throw e;
        }

        // 如果已有缓存且 nutrition_summary 能解析出 mealsHash 相同则直接返回（避免再次调用 AI）
        if (existing != null && existing.getNutritionSummary() != null) {
            try {
                JsonNode stored = mapper.readTree(existing.getNutritionSummary());
                if (stored.has("mealsHash") && mealsHash.equals(stored.get("mealsHash").asText())) {
                    log.info("缓存命中（mealsHash 相同），直接返回已缓存报告，userId={}, date={}", userId, date);
                    return parseStoredJsonToVO(stored, existing);
                } else {
                    log.info("缓存不命中或 mealsHash 不同，准备重新生成。旧mealsHash={}, 新mealsHash={}",
                            stored.has("mealsHash") ? stored.get("mealsHash").asText() : "null", mealsHash);
                }
            } catch (Exception e) {
                log.warn("nutritionSummary 解析为 JSON 失败（可能是旧格式），将重新生成 AI 报告。详情: {}", e.toString());
            }
        } else {
            log.info("未命中缓存，需要生成新的 AI 报告");
        }

        // 6) 构造发送给 AI 的 payloadJson（传给 AiClient）
        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("userId", userId);
        payload.put("date", date.format(DateTimeFormatter.ISO_DATE));
        payload.put("meals", mealPayload);
        payload.put("mealsHash", mealsHash);

        String payloadJson = mapper.writeValueAsString(payload);
        log.debug("发送给 AI 的 payloadJson 长度={}，前300字符预览: {}", payloadJson.length(),
                payloadJson.length() > 300 ? payloadJson.substring(0,300) + "..." : payloadJson);

        // 7) 调用 AI 客户端（AiClient）并处理返回
        JsonNode aiResp;
        try {
            log.info("调用 AIClient.generateReport() 开始, userId={}, date={}", userId, date);
            aiResp = aiClient.generateReport(payloadJson);
            if (aiResp == null) {
                log.warn("AI 返回为 null（可能调用失败或解析失败）");
                throw new RuntimeException("AI 返回 null");
            }
            log.info("AI 返回长度={}，前100字符: {}", aiResp.toString().length(), aiResp.toString().length() > 100 ? aiResp.toString().substring(0,100) + "..." : aiResp.toString());
        } catch (Exception e) {
            log.error("调用 AIClient.generateReport 失败", e);
            throw e;
        }

        // 8) 解析 aiResp（如果 aiResp 中只有 text 字段，尝试提取内嵌 JSON）
        JsonNode parsedAiNode = aiResp;
        if (parsedAiNode != null && parsedAiNode.has("text") && parsedAiNode.get("text").isTextual()) {
            String text = parsedAiNode.get("text").asText();
            JsonNode extracted = aiClient.tryParseEmbeddedJson(text);
            if (extracted != null) {
                log.info("从 aiResp.text 中成功提取并解析出 JSON");
                parsedAiNode = extracted;
            } else {
                log.warn("aiResp 仅包含 text，但未能解析出内嵌 JSON；将保留原始 text");
            }
        }

        // 9) 将 parsedAiNode 连同 mealsHash 保存到 nutrition_summary，并把关键字段写入独立列（容错）
        ObjectNode storedMap = mapper.createObjectNode();
        storedMap.put("mealsHash", mealsHash);
        // 把原始 aiResp（可能是解析后的 node 或原始包装）放到 ai 字段里
        storedMap.set("ai", parsedAiNode == null ? mapper.nullNode() : parsedAiNode);
        String storedJson = mapper.writeValueAsString(storedMap);

        try {
            if (existing == null) {
                existing = reportMapper.selectByUserAndDate(userId, date); // <- 新增这一行
            }
            if (existing == null) {
                log.info("插入新的 daily_diet_analysis 记录 userId={}, date={}", userId, date);
                DailyDietAnalysis newReport = new DailyDietAnalysis();
                newReport.setUserId(userId);
                newReport.setAnalysisDate(date);

                // 优先从 parsedAiNode 中填充独立列
                if (parsedAiNode != null) {
                    // healthScore
                    if (parsedAiNode.has("healthScore") && !parsedAiNode.get("healthScore").isNull()) {
                        try { newReport.setHealthScore(parsedAiNode.get("healthScore").decimalValue()); }
                        catch (Exception ex) { newReport.setHealthScore(java.math.BigDecimal.valueOf(parsedAiNode.get("healthScore").asDouble())); }
                    }
                    // nutrition grams
                    if (parsedAiNode.has("nutrition") && parsedAiNode.get("nutrition").isObject()) {
                        JsonNode n = parsedAiNode.get("nutrition");
                        if (n.has("carbGrams") && !n.get("carbGrams").isNull()) newReport.setCarbGrams(n.get("carbGrams").decimalValue());
                        if (n.has("proteinGrams") && !n.get("proteinGrams").isNull()) newReport.setProteinGrams(n.get("proteinGrams").decimalValue());
                        if (n.has("fatGrams") && !n.get("fatGrams").isNull()) newReport.setFatGrams(n.get("fatGrams").decimalValue());
                        if (n.has("summary") && n.get("summary").isTextual()) newReport.setNutritionSummary(n.get("summary").asText());
                    }
                    if (parsedAiNode.has("suggestions")) newReport.setHealthSuggestions(parsedAiNode.get("suggestions").toString());
                    if (parsedAiNode.has("improvements")) newReport.setImprovementSuggestions(parsedAiNode.get("improvements").toString());
                }

                // 始终保存完整的 storedJson 到 nutrition_summary（以备查看）
                // 如果 newReport.nutritionSummary 之前没有以 summary 形式设置，则以 storedJson 保存
                if (newReport.getNutritionSummary() == null) {
                    newReport.setNutritionSummary(storedJson);
                } else {
                    // 如果已经从 nutrition.summary 写入了比较友好的文本，还是把完整 JSON 存到 nutrition_summary 字段里（覆盖）
                    newReport.setNutritionSummary(storedJson);
                }

                reportMapper.insert(newReport);
                existing = reportMapper.selectByUserAndDate(userId, date);
                log.info("插入完成，新记录 id={}", existing == null ? "null" : existing.getId());
            } else {
                log.info("更新已有 daily_diet_analysis id={} 的记录", existing.getId());
                // 更新 nutrition_summary 字段并尽量写入独立列
                existing.setNutritionSummary(storedJson);

                if (parsedAiNode != null) {
                    if (parsedAiNode.has("healthScore") && !parsedAiNode.get("healthScore").isNull()) {
                        try { existing.setHealthScore(parsedAiNode.get("healthScore").decimalValue()); }
                        catch (Exception ex) { existing.setHealthScore(java.math.BigDecimal.valueOf(parsedAiNode.get("healthScore").asDouble())); }
                    }
                    if (parsedAiNode.has("nutrition") && parsedAiNode.get("nutrition").isObject()) {
                        JsonNode n = parsedAiNode.get("nutrition");
                        if (n.has("carbGrams") && !n.get("carbGrams").isNull()) existing.setCarbGrams(n.get("carbGrams").decimalValue());
                        if (n.has("proteinGrams") && !n.get("proteinGrams").isNull()) existing.setProteinGrams(n.get("proteinGrams").decimalValue());
                        if (n.has("fatGrams") && !n.get("fatGrams").isNull()) existing.setFatGrams(n.get("fatGrams").decimalValue());
                    }
                    if (parsedAiNode.has("suggestions")) existing.setHealthSuggestions(parsedAiNode.get("suggestions").toString());
                    if (parsedAiNode.has("improvements")) existing.setImprovementSuggestions(parsedAiNode.get("improvements").toString());
                }

                reportMapper.updateById(existing);
                log.info("更新完成，id={}", existing.getId());
            }
        } catch (Exception e) {
            log.error("写入 daily_diet_analysis 表失败", e);
            throw e;
        }

        // 10) 解析并返回 VO
        JsonNode storedNode = mapper.readTree(storedJson);
        DailyDietAnalysis dbRow = reportMapper.selectByUserAndDate(userId, date);
        DailyAnalysisReportVO vo = parseStoredJsonToVO(storedNode, dbRow);
        log.info("生成的 VO 即将返回，userId={}, date={}, healthScore={}", userId, date, vo.getHealthScore());
        return vo;
    }

    /**
     * 把存储的 JSON（{mealsHash, ai}）或直接 ai 节点解析为前端需要的 VO
     */
    private DailyAnalysisReportVO parseStoredJsonToVO(JsonNode storedNode, DailyDietAnalysis dbRow) {
        DailyAnalysisReportVO vo = new DailyAnalysisReportVO();

        log.debug("parseStoredJsonToVO 开始, storedNode={} dbRowId={}",
                storedNode == null ? "null" : storedNode.toString(),
                dbRow == null ? "null" : dbRow.getId());

        // 1. 取 ai 节点，如果没有 ai 就直接用存储的节点
        JsonNode aiNode = storedNode != null && storedNode.has("ai") ? storedNode.get("ai") : storedNode;
        log.debug("解析 aiNode={}", aiNode == null ? "null" : aiNode.toString());

        Map<String, Integer> nutritionMap = new HashMap<>();

        // 2. nutrition 解析
        if (aiNode != null && aiNode.has("nutrition") && aiNode.get("nutrition").isObject()) {
            JsonNode n = aiNode.get("nutrition");

            // carbs
            if (n.has("carbGrams") && n.get("carbGrams").isNumber())
                nutritionMap.put("carbs", (int) Math.round(n.get("carbGrams").asDouble()));
            else if (n.has("carbs") && n.get("carbs").isNumber())
                nutritionMap.put("carbs", n.get("carbs").asInt());
            else nutritionMap.put("carbs", 0);

            // protein
            if (n.has("proteinGrams") && n.get("proteinGrams").isNumber())
                nutritionMap.put("protein", (int) Math.round(n.get("proteinGrams").asDouble()));
            else if (n.has("protein") && n.get("protein").isNumber())
                nutritionMap.put("protein", n.get("protein").asInt());
            else nutritionMap.put("protein", 0);

            // fat
            if (n.has("fatGrams") && n.get("fatGrams").isNumber())
                nutritionMap.put("fat", (int) Math.round(n.get("fatGrams").asDouble()));
            else if (n.has("fat") && n.get("fat").isNumber())
                nutritionMap.put("fat", n.get("fat").asInt());
            else nutritionMap.put("fat", 0);

            vo.setNutrition(nutritionMap);
            log.debug("解析 nutritionMap={}", nutritionMap);

            // summary 优先从 nutrition.summary 字段取
            if (n.has("summary") && n.get("summary").isTextual()) {
                vo.setNutritionSummary(n.get("summary").asText());
                log.info("nutrition.summary={}", n.get("summary").asText());
            } else {
                if (aiNode.has("summary") && aiNode.get("summary").isTextual()) {
                    vo.setNutritionSummary(aiNode.get("summary").asText());
                    log.info("aiNode.summary={}", aiNode.get("summary").asText());
                } else if (dbRow != null && dbRow.getNutritionSummary() != null) {
                    vo.setNutritionSummary(dbRow.getNutritionSummary());
                    log.info("dbRow.nutritionSummary fallback={}", dbRow.getNutritionSummary());
                }
            }
        } else if (dbRow != null) {
            // DB fallback
            double c = dbRow.getCarbGrams() != null ? dbRow.getCarbGrams().doubleValue() : 0.0;
            double p = dbRow.getProteinGrams() != null ? dbRow.getProteinGrams().doubleValue() : 0.0;
            double f = dbRow.getFatGrams() != null ? dbRow.getFatGrams().doubleValue() : 0.0;
            double total = c + p + f;

            if (total <= 0.0) {
                nutritionMap.put("carbs", 0);
                nutritionMap.put("protein", 0);
                nutritionMap.put("fat", 0);
            } else {
                nutritionMap.put("carbs", (int) Math.round(100.0 * c / total));
                nutritionMap.put("protein", (int) Math.round(100.0 * p / total));
                nutritionMap.put("fat", (int) Math.round(100.0 * f / total));
            }
            vo.setNutrition(nutritionMap);
            vo.setNutritionSummary(dbRow.getNutritionSummary());
            log.debug("dbRow fallback nutritionMap={} nutritionSummary={}", nutritionMap, dbRow.getNutritionSummary());
        }

        // 3. suggestions
        if (aiNode != null && aiNode.has("suggestions") && aiNode.get("suggestions").isArray()) {
            List<String> s = new ArrayList<>();
            aiNode.get("suggestions").forEach(j -> s.add(j.asText()));
            vo.setSuggestions(s);
            log.debug("aiNode.suggestions={}", s);
        } else if (dbRow != null && dbRow.getHealthSuggestions() != null) {
            vo.setSuggestions(Collections.singletonList(dbRow.getHealthSuggestions()));
            log.debug("dbRow.healthSuggestions fallback={}", dbRow.getHealthSuggestions());
        }

        // 4. improvements
        if (aiNode != null && aiNode.has("improvements") && aiNode.get("improvements").isArray()) {
            List<String> s = new ArrayList<>();
            aiNode.get("improvements").forEach(j -> s.add(j.asText()));
            vo.setImprovements(s);
            log.debug("aiNode.improvements={}", s);
        } else if (dbRow != null && dbRow.getImprovementSuggestions() != null) {
            vo.setImprovements(Collections.singletonList(dbRow.getImprovementSuggestions()));
            log.debug("dbRow.improvementSuggestions fallback={}", dbRow.getImprovementSuggestions());
        }

        // 5. healthScore
        if (aiNode != null && aiNode.has("healthScore") && aiNode.get("healthScore").isNumber()) {
            vo.setHealthScore(aiNode.get("healthScore").asDouble());
            log.debug("aiNode.healthScore={}", aiNode.get("healthScore").asDouble());
        } else if (dbRow != null && dbRow.getHealthScore() != null) {
            vo.setHealthScore(dbRow.getHealthScore().doubleValue());
            log.debug("dbRow.healthScore fallback={}", dbRow.getHealthScore().doubleValue());
        }

        // 6. lastUpdated
        if (dbRow != null) {
            if (dbRow.getUpdatedAt() != null) vo.setLastUpdated(dbRow.getUpdatedAt().toString());
            else if (dbRow.getCreatedAt() != null) vo.setLastUpdated(dbRow.getCreatedAt().toString());
            log.debug("lastUpdated={}", vo.getLastUpdated());
        }

        log.debug("parseStoredJsonToVO 完成, VO={}", vo);
        return vo;
    }


    ///  ///////////////////////////////////////////////////////////////////
    /**
     * AI 聊天：在内部将当日分析作为隐藏上下文加入提示，不对外暴露该上下文内容
     */
    @Override
    public ChatMessageVO chatWithAI(Long userId, String username, String question) throws Exception {
        log.info("收到 chatWithAI 请求，userId={}, username={}, questionLen={}", userId, username, question == null ? 0 : question.length());

        // 1) 尝试解析 username -> userId（如果 userId 为空）
        if (userId == null && username != null) {
            try {
                User u = userMapper.selectByUsername(username);
                if (u != null) {
                    userId = u.getId();
                    log.debug("通过 username 找到 userId={}", userId);
                }
            } catch (Exception e) {
                log.warn("根据 username 查 userId 失败: {}", e.toString());
            }
        }

        // 2) 获取今日分析报告作为系统上下文（不返回给前端）
        DailyAnalysisReportVO todayReport = null;
        try {
            todayReport = this.getOrGenerateDailyAnalysis(userId, username, LocalDate.now());
            log.debug("获取到当日分析报告（用于上下文），healthScore={}", todayReport == null ? "null" : todayReport.getHealthScore());
        } catch (Exception e) {
            // 获取报告失败时，不影响聊天，继续以空上下文调用 AI，但记录日志
            log.warn("获取当日分析报告失败（不会阻塞聊天），error={}", e.toString());
            todayReport = null;
        }

        // 3) 构造系统提示（system prompt） —— 把今日报告要点摘要化（仅用于 AI 的上下文）
        String systemPrompt = buildSystemPromptFromReport(todayReport);

        // 4) 调用 AiClient 进行对话（内部将 systemPrompt 和 userQuestion 一起发送）
        JsonNode aiRaw = null;
        try {
            aiRaw = aiClient.chatWithContext(systemPrompt, question);
            if (aiRaw == null) {
                log.warn("aiClient.chatWithContext 返回 null");
                throw new RuntimeException("AI 返回为空");
            }
        } catch (Exception e) {
            log.error("调用 aiClient.chatWithContext 失败", e);
            throw e;
        }

        // 5) 从 aiRaw 中解析出 AI 回复文本（兼容多种结构）
        String aiText = extractAssistantText(aiRaw);
        if (aiText == null || aiText.trim().isEmpty()) {
            log.warn("AI 回复文本为空，返回默认提示");
            aiText = "抱歉，AI 未返回有效回复，请稍后重试。";
        }

        // 6) 构造返回 VO（注意：不包含 systemPrompt 或任何前置知识）
        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(UUID.randomUUID().toString());
        vo.setType("ai");
        vo.setContent(aiText);
        vo.setTimestamp(ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        log.info("chatWithAI 完成，返回长度={}", aiText.length());
        return vo;
    }

    /**
     * 将 DailyAnalysisReportVO 摘要化为 system prompt（仅用于 AI 上下文，不展示给前端）。
     * 注意：摘要尽量简短，避免超过模型输入限制。
     */
    private String buildSystemPromptFromReport(DailyAnalysisReportVO report) {
        if (report == null) {
            return "你是一个专业的营养师助手，当前用户没有可用的当日饮食报告。请根据用户问题给出专业、简洁且温和的建议。";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的营养师助手。下面是该用户今日饮食的简要摘要（仅供参考，用于回答用户问题）：\n");
        if (report.getHealthScore() != null) {
            sb.append("健康指数: ").append(report.getHealthScore()).append("。 ");
        }
        if (report.getNutrition() != null && !report.getNutrition().isEmpty()) {
            sb.append("营养比例（%）: 碳水=").append(report.getNutrition().getOrDefault("carbs", 0))
                    .append(", 蛋白=").append(report.getNutrition().getOrDefault("protein", 0))
                    .append(", 脂肪=").append(report.getNutrition().getOrDefault("fat", 0)).append("。 ");
        }
        if (report.getNutritionSummary() != null && !report.getNutritionSummary().trim().isEmpty()) {
            String sum = report.getNutritionSummary();
            // 若 summary 很长，仅取前 300 字
            if (sum.length() > 300) sum = sum.substring(0, 300) + "...";
            sb.append("摘要: ").append(sum).append(" ");
        }
        if (report.getSuggestions() != null && !report.getSuggestions().isEmpty()) {
            sb.append("核心建议: ");
            for (int i = 0; i < Math.min(3, report.getSuggestions().size()); i++) {
                sb.append(report.getSuggestions().get(i));
                if (i < Math.min(3, report.getSuggestions().size()) - 1) sb.append("; ");
            }
            sb.append("。");
        }
        sb.append("\n使用这些信息只作为上下文；不要将该上下文原文返回给用户。回答应简洁、友好并可操作。");
        return sb.toString();
    }

    /**
     * 从 aiRaw（第三方返回）中解析出 assistant 的文本回复（兼容 choices[0].message.content / text / assistant 字段等）
     */
    private String extractAssistantText(JsonNode aiRaw) {
        if (aiRaw == null) return null;
        // 1) 尝试常见 deepseek/openai 结构：choices[0].message.content
        try {
            if (aiRaw.has("choices") && aiRaw.get("choices").isArray() && aiRaw.get("choices").size() > 0) {
                JsonNode first = aiRaw.get("choices").get(0);
                if (first.has("message") && first.get("message").has("content")) {
                    return first.get("message").get("content").asText();
                }
                if (first.has("message") && first.get("message").has("text")) {
                    // 有些返回可能使用 text
                    return first.get("message").get("text").asText();
                }
                if (first.has("text")) {
                    return first.get("text").asText();
                }
            }
            // 2) 直接 aiRaw.text
            if (aiRaw.has("text")) return aiRaw.get("text").asText();
            // 3) 如果 aiRaw 本身就是字符串结构（例如直接返回了 {"content":"..."}）
            if (aiRaw.has("content")) return aiRaw.get("content").asText();
            // 4) 否则返回 aiRaw 的 toString 作为兜底
            return aiRaw.toString();
        } catch (Exception e) {
            log.warn("解析 AI 回复文本异常: {}", e.toString());
            return aiRaw.toString();
        }
    }


    @Override
    @Transactional(readOnly = true)
    public TrendResponseVO getTrendAnalysis(Long userId, String username, int days) throws Exception {
        log.info("开始处理 getTrendAnalysis，userId={}, username={}, days={}", userId, username, days);

        // 1) resolve userId if null (同 getOrGenerateDailyAnalysis)
        if (userId == null && username != null) {
            try {
                User u = userMapper.selectByUsername(username);
                if (u != null) {
                    userId = u.getId();
                    log.info("通过 username 找到 userId={}", userId);
                }
            } catch (Exception e) {
                log.info("根据 username 查 userId 失败: {}", e.toString());
            }
        }

        if (userId == null) {
            log.info("getTrendAnalysis 无法确定 userId（既未传 userId，SecurityContext 也未解析到 username）");
            TrendResponseVO empty = new TrendResponseVO();
            empty.setDates(new ArrayList<>());
            empty.setCalories(new ArrayList<>());
            empty.setHealthScores(new ArrayList<>());
            return empty;
        }

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);

        List<String> dates = IntStream.rangeClosed(0, days - 1)
                .mapToObj(i -> start.plusDays(i).toString())
                .collect(Collectors.toList());

        List<Integer> caloriesList = new ArrayList<>(days);
        List<Double> healthList = new ArrayList<>(days);

        for (int i = 0; i < days; i++) {
            LocalDate d = start.plusDays(i);

            // 查询每日总卡路里
            int totalCalories = 0;
            try {
                List<MealRecord> meals = mealRecordMapper.selectByUserAndDate(userId, d);
                if (meals != null) {
                    for (MealRecord mr : meals) {
                        if (mr.getCalories() != null) totalCalories += mr.getCalories();
                    }
                }
                log.debug("日期 {} 的总卡路里: {}", d, totalCalories);
            } catch (Exception e) {
                log.info("查询 meal_records 失败 userId={}, date={}, err={}", userId, d, e.toString());
            }
            caloriesList.add(totalCalories);

            // 查询每日健康指数
            Double healthScore = null;
            try {
                DailyDietAnalysis r = reportMapper.selectByUserAndDate(userId, d);
                if (r != null && r.getHealthScore() != null) {
                    healthScore = r.getHealthScore().doubleValue();
                }
                log.info("日期 {} 的健康指数: {}", d, healthScore);
            } catch (Exception e) {
                log.info("查询 daily_diet_analysis 失败 userId={}, date={}, err={}", userId, d, e.toString());
            }
            healthList.add(healthScore);
        }

        TrendResponseVO vo = new TrendResponseVO();
        vo.setDates(dates);
        vo.setCalories(caloriesList);
        vo.setHealthScores(healthList);

        log.info("getTrendAnalysis 完成 userId={}, days={}, 返回数据 dates.size={}, calories.size={}, healthScores.size={}",
                userId, days, dates.size(), caloriesList.size(), healthList.size());
        log.debug("返回数据示例 dates={}, calories={}, healthScores={}",
                dates.size() > 0 ? dates.subList(0, Math.min(5, dates.size())) : "[]",
                caloriesList.size() > 0 ? caloriesList.subList(0, Math.min(5, caloriesList.size())) : "[]",
                healthList.size() > 0 ? healthList.subList(0, Math.min(5, healthList.size())) : "[]");

        return vo;
    }

}



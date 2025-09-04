/**
 * 文件路径: src/main/java/com/myproject/controller/AIAnalysisController.java
 * 说明: AI 分析控制器，增加详细日志，便于调试前端请求是否到达以及异常信息。
 */

package com.myproject.controller;

import com.myproject.result.ResponseResult;
import com.myproject.vo.TrendResponseVO;
import com.myproject.service.AIAnalysisService;
import com.myproject.vo.DailyAnalysisReportVO;
import com.myproject.dto.ChatRequestDTO;
import com.myproject.vo.ChatMessageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// import jakarta.servlet.http.HttpServletRequest; // 如果需要访问更多 header，可打开此行并注入 request
import org.springframework.web.bind.annotation.*;
import java.security.Principal;


import java.time.LocalDate;


@RestController
@RequestMapping("/api/ai")
public class AIAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AIAnalysisController.class);

    private final AIAnalysisService aiAnalysisService;

    @Autowired
    private AIAnalysisService aiService;   // ✅ 注入 Service 层

    @Autowired
    public AIAnalysisController(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    /**
     * AI 聊天接口
     * - 前端请求: POST /api/ai/chat  Body: { "question": "xxx" }
     * - 后端会把当日报告作为隐藏上下文加入给第三方 AI，前端只会得到 AI 的回答文本
     */
    @PostMapping("/chat")
    public ResponseResult<ChatMessageVO> chat(@RequestBody ChatRequestDTO request, Principal principal) {
        String username = (principal == null) ? null : principal.getName();
        log.info("收到 /api/ai/chat 请求, username={}, questionLen={}", username, request.getQuestion() == null ? 0 : request.getQuestion().length());

        try {
            ChatMessageVO vo = aiAnalysisService.chatWithAI(null, username, request.getQuestion());
            return ResponseResult.success(vo);
        } catch (Exception e) {
            log.error("处理 /api/ai/chat 失败", e);
            return ResponseResult.fail("AI 聊天失败：" + e.getMessage());
        }
    }

    /**
     * 新增：GET /api/ai/trend?period=7d|30d|3m (默认 7d)
     */
    @GetMapping("/trend")
    public TrendResponseVO getTrend(
            @RequestParam(name = "period", required = false, defaultValue = "7d") String period,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "username", required = false) String username) throws Exception {


        int days = 7;
        if ("30d".equals(period)) days = 30;
        else if ("3m".equals(period) || "90d".equals(period)) days = 90;
        else days = 7;

        log.info("收到 /api/ai/trend 请求 userId={}, username={}, period={} ({} 天)", userId, username, period, days);

        return aiService.getTrendAnalysis(userId, username, days);
    }

    /**
     * 获取或生成每日AI分析报告
     * Header: X-Username 可选
     * Query: date 可选 (yyyy-MM-dd)
     */
    @GetMapping("/daily-analysis")
    public ResponseResult<DailyAnalysisReportVO> getDailyAnalysis(
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestParam(value = "date", required = false) String dateStr
    ) {
        log.info("收到 /api/ai/daily-analysis 请求，username={}, date={}", username, dateStr);
        try {
            LocalDate targetDate = (dateStr == null || dateStr.isEmpty()) ? LocalDate.now() : LocalDate.parse(dateStr);
            log.debug("解析后的 targetDate={}", targetDate);

            // 调用 Service（注意 Service 方法会打印更详细的日志）
            DailyAnalysisReportVO report = aiAnalysisService.getOrGenerateDailyAnalysis(null, username, targetDate);

            if (report == null) {
                log.warn("生成/获取分析报告返回 null，username={}, date={}", username, targetDate);
                return ResponseResult.fail("未生成分析报告（返回为空）");
            }

            log.info("分析报告成功返回，username={}, date={}, healthScore={}", username, targetDate, report.getHealthScore());
            return ResponseResult.success(report);
        } catch (Exception e) {
            // 打印完整堆栈，便于调试
            log.error("获取/生成每日分析报告时发生异常，username={}, date={}, error={}", username, dateStr, e.toString(), e);
            return ResponseResult.fail("生成分析报告失败: " + e.getMessage());
        }
    }
}

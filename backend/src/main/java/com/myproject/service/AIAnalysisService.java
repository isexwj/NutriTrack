/**
 * 文件路径: src/main/java/com/myproject/service/AIAnalysisService.java
 * 说明: AI 分析服务接口，定义生成/获取每日分析报告的业务方法。
 *       实现类放在 com.myproject.service.impl.AIAnalysisServiceImpl
 */

package com.myproject.service;

import com.myproject.vo.DailyAnalysisReportVO;
import com.myproject.vo.ChatMessageVO;
import com.myproject.vo.TrendResponseVO;

import java.time.LocalDate;

public interface AIAnalysisService {
    /**
     * 生成或获取当天（或指定日期）的饮食分析报告
     *
     * 说明：
     *  - 若 userId 为 null，则实现应尝试通过 username 查找 userId；
     *  - 若 date 为 null，则使用 LocalDate.now();
     *  - 实现应尽量复用缓存（daily_diet_analysis.nutrition_summary），当 mealsHash 未变化时直接返回缓存；
     *  - 否则调用第三方 AI（通过 AiClient）生成并持久化结果，然后返回 VO。
     *
     * @param userId   可选用户 ID
     * @param username 可选用户名（从 Header 传入）
     * @param date     可选分析日期
     * @return DailyAnalysisReportVO 前端展示用 VO
     * @throws Exception 在生成或解析过程中抛出异常以便上层 Controller 返回失败信息
     */
    DailyAnalysisReportVO getOrGenerateDailyAnalysis(Long userId, String username, LocalDate date) throws Exception;
    /**
     * AI 聊天接口：传入 userId 或 username 和用户问题，服务端内部会把当日分析报告（如果有）作为隐藏上下文加入到提示中。
     * @param userId 可为 null，将使用 username 解析
     * @param username 登录用户名（可为空，但一般由鉴权填充）
     * @param question 用户的问题文本
     * @return ChatMessageVO（仅包含 AI 回复文本）
     */
    ChatMessageVO chatWithAI(Long userId, String username, String question) throws Exception;

    TrendResponseVO getTrendAnalysis(Long userId, String username, int days) throws Exception;
}

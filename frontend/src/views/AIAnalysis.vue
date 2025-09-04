<template>
  <div class="ai-analysis-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">AI分析助手</h2>
        <p class="page-subtitle">智能分析您的饮食记录，提供个性化健康建议</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="generateAnalysis" :loading="isAnalyzing">
          <el-icon><Star /></el-icon>
          生成分析报告
        </el-button>
      </div>
    </div>

    <!-- 分析概览卡片 -->
    <div class="analysis-overview">
      <div class="overview-card">
        <div class="card-header">
          <div class="card-icon">
            <el-icon size="24"><Star /></el-icon>
          </div>
          <div class="card-title">今日饮食概览</div>
        </div>
        <div class="card-content">
          <div class="overview-stats">
            <div class="stat-item">
              <div class="stat-value">{{ todayStats.meals }}</div>
              <div class="stat-label">用餐次数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ todayStats.calories }}</div>
              <div class="stat-label">总卡路里</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ todayStats.rating }}</div>
              <div class="stat-label">平均评分</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ todayStats.healthScore }}</div>
              <div class="stat-label">健康指数</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分析结果区域 -->
    <div class="analysis-results">
      <!-- AI分析报告 -->
      <div class="analysis-report">
        <div class="report-header">
          <div class="report-title">
            <el-icon><Star /></el-icon>
            AI分析报告
          </div>
          <div class="report-time">
            最后更新：{{ lastAnalysisTime }}
          </div>
        </div>
        <div class="report-content">
          <div v-if="!analysisReport" class="no-report">
            <el-empty description="暂无分析报告" :image-size="80">
              <el-button type="primary" @click="generateAnalysis">开始分析</el-button>
            </el-empty>
          </div>
          <div v-else class="report-sections">
            <!-- 营养分析 -->
            <div class="report-section">
              <div class="section-title">
                <el-icon><Star /></el-icon>
                营养分析
              </div>
              <div class="section-content">
                <div class="nutrition-chart">
                  <div class="chart-item">
                    <div class="chart-label">碳水化合物</div>
                    <div class="chart-bar">
                      <div class="chart-fill" :style="{ width: analysisReport.nutrition.carbs + '%' }"></div>
                    </div>
                    <div class="chart-value">{{ analysisReport.nutrition.carbs }}%</div>
                  </div>
                  <div class="chart-item">
                    <div class="chart-label">蛋白质</div>
                    <div class="chart-bar">
                      <div class="chart-fill protein" :style="{ width: analysisReport.nutrition.protein + '%' }"></div>
                    </div>
                    <div class="chart-value">{{ analysisReport.nutrition.protein }}%</div>
                  </div>
                  <div class="chart-item">
                    <div class="chart-label">脂肪</div>
                    <div class="chart-bar">
                      <div class="chart-fill fat" :style="{ width: analysisReport.nutrition.fat + '%' }"></div>
                    </div>
                    <div class="chart-value">{{ analysisReport.nutrition.fat }}%</div>
                  </div>
                </div>
                <div class="nutrition-summary">
                  {{ analysisReport.nutritionSummary }}
                </div>
              </div>
            </div>

            <!-- 健康建议 -->
            <div class="report-section">
              <div class="section-title">
                <el-icon><Star /></el-icon>
                健康建议
              </div>
              <div class="section-content">
                <div class="suggestions-list">
                  <div 
                    v-for="(suggestion, index) in analysisReport.suggestions" 
                    :key="index"
                    class="suggestion-item"
                  >
                    <div class="suggestion-icon">
                      <el-icon><Star /></el-icon>
                    </div>
                    <div class="suggestion-text">{{ suggestion }}</div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 改进建议 -->
            <div class="report-section">
              <div class="section-title">
                <el-icon><Star /></el-icon>
                改进建议
              </div>
              <div class="section-content">
                <div class="improvements-list">
                  <div 
                    v-for="(improvement, index) in analysisReport.improvements" 
                    :key="index"
                    class="improvement-item"
                  >
                    <div class="improvement-icon">
                      <el-icon><Star /></el-icon>
                    </div>
                    <div class="improvement-text">{{ improvement }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 历史趋势 -->
      <div class="trend-analysis">
        <div class="trend-header">
          <div class="trend-title">
            <el-icon><Star /></el-icon>
            健康趋势分析
          </div>
          <div class="trend-controls">
            <el-select v-model="trendPeriod" @change="updateTrend">
              <el-option label="最近7天" value="7d" />
              <el-option label="最近30天" value="30d" />
              <el-option label="最近3个月" value="3m" />
            </el-select>
          </div>
        </div>

        <div class="trend-content">
          <div class="trend-charts">
            <div class="chart-container">
              <div class="chart-title">卡路里摄入趋势</div>
              <div ref="caloriesChart" class="echart" style="height:220px;"></div>
            </div>
            <div class="chart-container">
              <div class="chart-title">健康指数变化</div>
              <div ref="healthChart" class="echart" style="height:220px;"></div>
            </div>
          </div>
        </div>
      </div>
    </div>


    <!-- AI对话助手 -->
    <div class="ai-chat">
      <div class="chat-header">
        <div class="chat-title">
          <el-icon><Star /></el-icon>
          AI营养师助手
        </div>
        <div class="chat-status">
          <el-tag type="success" size="small">在线</el-tag>
        </div>
      </div>
      <div class="chat-content">
        <div class="chat-messages">
          <div 
            v-for="message in chatMessages" 
            :key="message.id"
            class="message-item"
            :class="{ 'user-message': message.type === 'user' }"
          >
            <div class="message-avatar">
              <el-avatar :size="32" v-if="message.type === 'ai'">
                <el-icon><Star /></el-icon>
              </el-avatar>
              <el-avatar :size="32" v-else>
                {{ userStore.username.charAt(0).toUpperCase() }}
              </el-avatar>
            </div>
            <div class="message-content">
              <div class="message-text">{{ message.content }}</div>
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
        </div>
        <div class="chat-input">
          <el-input
            v-model="chatInput"
            placeholder="向AI营养师提问..."
            @keyup.enter="sendMessage"
            :disabled="isChatLoading"
          >
            <template #append>
              <el-button @click="sendMessage" :loading="isChatLoading">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import {chatWithAI, getTrendAnalysis} from '@/api/ai'
import {
  getDailyAnalysis
} from '@/api/ai'

import * as echarts from 'echarts'
import {getTodayStats} from "@/api/meal.js";


const caloriesChart = ref(null)
const healthChart = ref(null)
let caloriesChartInstance = null
let healthChartInstance = null


const userStore = useUserStore()

// 响应式数据
const isAnalyzing = ref(false)
const isChatLoading = ref(false)
const trendPeriod = ref('7d')
const chatInput = ref('')
const lastAnalysisTime = ref('')

// 今日统计数据（页面其它部分仍可从 API 加载，这里保留初始值）
const todayStats = ref({
  totalMeals: 0,
  totalCalories: 0,
  avgRating: 0,
  healthScore: 0
})

// AI 分析报告
const analysisReport = ref(null)

// 聊天消息（页面已有初始问候）
const chatMessages = ref([
  {
    id: 1,
    type: 'ai',
    content: '您好！我是您的AI营养师助手，可以帮您分析饮食记录并提供健康建议。有什么问题可以随时问我！',
    timestamp: new Date('2024-01-15 09:00:00')
  }
])

/**
 * 生成或获取当天分析报告
 * 调用后端 API: GET /api/ai/daily-analysis
 */
const generateAnalysis = async () => {
  isAnalyzing.value = true
  try {
    const res = await getDailyAnalysis()
    if (res && res.data) {
      // res.data 是后端返回的 DailyAnalysisReportVO
      analysisReport.value = res.data
      lastAnalysisTime.value = res.data.lastUpdated || new Date().toLocaleString('zh-CN')
      ElMessage.success('分析报告获取成功')
    } else {
      ElMessage.error('未返回分析数据')
    }
  } catch (error) {
    ElMessage.error('分析失败，请稍后重试')
  } finally {
    isAnalyzing.value = false
  }
}

/**
 * 页面载入时可以先拉取今日统计（本次实现仅关注分析报告，
 * 如果需要完整今日统计，请实装 getTodayStats API）
 */
const loadTodayStats = async () => {
  // 若需要实现，请在 api/ai.js 中添加 getTodayStats 并在此调用
  // 这里保留空实现以免影响页面其它功能
}

const fetchTodayStats = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]; // "2025-09-03"
    const res = await getTodayStats(today)
    todayStats.value = res.data // 需与后端返回格式一致
  } catch (error) {
    console.error('获取今日统计失败', error)
  }
}


const initCharts = async () => {
  await nextTick()
  try {
    if (caloriesChartInstance) {
      try { caloriesChartInstance.dispose() } catch (e) { console.warn('dispose caloriesChartInstance error', e) }
      caloriesChartInstance = null
    }
    if (healthChartInstance) {
      try { healthChartInstance.dispose() } catch (e) { console.warn('dispose healthChartInstance error', e) }
      healthChartInstance = null
    }
  } catch (e) {
    console.warn('dispose charts error', e)
  }

  if (caloriesChart.value) {
    caloriesChartInstance = echarts.init(caloriesChart.value)
  }
  if (healthChart.value) {
    healthChartInstance = echarts.init(healthChart.value)
  }

  // 让图表自适应容器
  window.requestAnimationFrame(() => {
    if (caloriesChartInstance) caloriesChartInstance.resize()
    if (healthChartInstance) healthChartInstance.resize()
  })

  // 初始载入趋势数据
  await loadTrendData()
}

const daysForPeriod = (period) => {
  if (period === '7d') return 7
  if (period === '30d') return 30
  if (period === '3m') return 90
  return 7
}

const loadTrendData = async () => {
  const days = daysForPeriod(trendPeriod.value)

  // 确保图表实例已初始化
  if (!caloriesChartInstance || !healthChartInstance) {
    console.debug('[trend] charts not initialized, calling initCharts()')
    await initCharts()
  }

  try {
    console.debug('[trend] requesting backend trend, period=', trendPeriod.value)
    const res = await getTrendAnalysis({
      period: trendPeriod.value,
      username: userStore.username
    })

// 打印后端原始返回
    console.log('[trend] raw backend response:', res.data)

// 后端返回结构示例： { dates: [...], calories: [...], healthScores: [...] }
    const payload = res.data
    if (payload && Array.isArray(payload.dates) && payload.dates.length > 0) {
      const labels = payload.dates
      const calData = payload.calories
      const healthData = payload.healthScores

      renderCaloriesChart(labels, calData)
      renderHealthChart(labels, healthData)
    } else {
      console.warn('[trend] backend returned empty or invalid data')
      fallbackSimulatedTrend(days)
    }
  } catch (err) {
    console.error('[trend] getTrendAnalysis failed, fallback to simulated data', err)
    fallbackSimulatedTrend(days)
  }

  // 确保 resize
  setTimeout(() => {
    if (caloriesChartInstance) caloriesChartInstance.resize()
    if (healthChartInstance) healthChartInstance.resize()
  }, 120)
}


// 回退函数：和你原来模拟逻辑一致
const fallbackSimulatedTrend = (days) => {
  const labels = []
  const calData = []
  const healthData = []
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    labels.push(`${d.getMonth() + 1}/${d.getDate()}`)
    calData.push(200 + Math.round(Math.random() * 600)) // 模拟卡路里
    healthData.push(50 + Math.round(Math.random() * 40)) // 模拟健康分（0-100）
  }
  console.debug('[trend] fallback labels:', labels)
  renderCaloriesChart(labels, calData)
  renderHealthChart(labels, healthData)
}

const baseAxisStyle = {
  axisLine: { show: true },
  axisTick: { show: true },
  axisLabel: { show: true, formatter: (v) => v },
  splitLine: { show: true, lineStyle: { type: 'dashed', opacity: 0.08 } }
}

const renderCaloriesChart = (labels, data) => {
  if (!caloriesChartInstance) return
  caloriesChartInstance.clear()

  const option = {
    tooltip: { trigger: 'axis' },
    legend: {
      show: true,
      right: 10, // 距离右边 10px
      top: 10,   // 距离顶部 10px
      data: ['卡路里']
    },
    grid: { left: '6%', right: '4%', top: '12%', bottom: '8%' },
    xAxis: {
      type: 'category',
      data: labels,
      boundaryGap: false,
      ...baseAxisStyle
    },
    yAxis: {
      type: 'value',
      name: 'kcal',
      min: function (value) { return Math.max(0, value.min - 50) },
      max: function (value) { return value.max + 50 },
      ...baseAxisStyle
    },
    series: [
      {
        name: '卡路里',
        type: 'line',
        smooth: true,
        data,
        showSymbol: true,      // 显示每个点
        symbolSize: 8,         // 点大小，默认 4
        areaStyle: { opacity: 0.12 },
        lineStyle: { width: 2 }
      }
    ]
  }

  caloriesChartInstance.setOption(option, true)
  caloriesChartInstance.resize()
}

const renderHealthChart = (labels, data) => {
  if (!healthChartInstance) return
  healthChartInstance.clear()

  const option = {
    tooltip: { trigger: 'axis' },
    legend: {
      show: true,
      right: 10, // 距离右边 10px
      top: 10,   // 距离顶部 10px
      data: ['健康指数']
    },
    grid: { left: '6%', right: '4%', top: '12%', bottom: '8%' },
    xAxis: {
      type: 'category',
      data: labels,
      boundaryGap: false,
      ...baseAxisStyle
    },
    yAxis: {
      type: 'value',
      name: 'score',
      min: 0,
      max: 100,
      ...baseAxisStyle
    },
    series: [
      {
        name: '健康指数',
        type: 'line',
        smooth: true,
        data,
        showSymbol: true,      // 显示每个点
        symbolSize: 8,         // 点大小，默认 4
        areaStyle: { opacity: 0.08 },
        lineStyle: { width: 2 }
      }
    ]
  }

  healthChartInstance.setOption(option, true)
  healthChartInstance.resize()
}

const updateTrend = async () => {
  await loadTrendData()
}


// 窗口尺寸变化处理（防抖）
let resizeTimer = null
window.addEventListener('resize', () => {
  if (resizeTimer) clearTimeout(resizeTimer)
  resizeTimer = setTimeout(() => {
    if (caloriesChartInstance) caloriesChartInstance.resize()
    if (healthChartInstance) healthChartInstance.resize()
  }, 150)
})
/**
 * 发送聊天消息（占位实现）
 * 目前未实现 chat 接口，如需启用，请在 api/ai.js 中添加 chat 接口并调用
 */
const sendMessage = async () => {
  if (!chatInput.value.trim()) return

  // 用户消息
  const userMessage = {
    id: Date.now(),
    type: 'user',
    content: chatInput.value,
    timestamp: new Date()
  }
  chatMessages.value.push(userMessage)

  const userQuestion = chatInput.value
  chatInput.value = ''
  isChatLoading.value = true

  try {
    const res = await chatWithAI({
      question: userQuestion,
      userId: userStore.userId,     // 必须有
      username: userStore.username  // 可选，保证后端查不到 userId 时还能用
    })

    // 只取 AI 返回的文字内容
    const aiText = res.data.data?.content || 'AI未返回内容'

    chatMessages.value.push({
      id: Date.now() + 1,
      type: 'ai',
      content: aiText,
      timestamp: new Date()
    })
  } catch (error) {
    ElMessage.error('发送失败，请稍后重试')
  } finally {
    isChatLoading.value = false
  }
}
/**
 * 格式化时间（仅用于聊天时间显示）
 */
const formatTime = (date) => {
  return new Date(date).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}


// 页面挂载时先尝试获取分析报告（并可同时调用 loadTodayStats）
// 当页面初次加载与切换周期时调用
// 页面挂载初始化
onMounted(async () => {
  await generateAnalysis()
  await initCharts()
  await fetchTodayStats()
})
</script>

<style scoped>

.echart {
  width: 100%;
  height: 220px;
}

.ai-analysis-container {
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.header-content {
  flex: 1;
}

.page-title {
  color: #1e293b;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.page-subtitle {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.analysis-overview {
  margin-bottom: 24px;
}

.overview-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.card-icon {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  padding: 8px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.card-content {
  padding: 24px;
}

.overview-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 24px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  color: #3b82f6;
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-label {
  color: #64748b;
  font-size: 12px;
}

.analysis-results {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.analysis-report {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  background: #f8fafc;
}

.report-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1e293b;
  font-size: 16px;
  font-weight: 600;
}

.report-time {
  color: #64748b;
  font-size: 12px;
}

.report-content {
  padding: 24px;
}

.no-report {
  text-align: center;
  padding: 40px;
}

.report-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.report-section {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 20px;
  background: #f8fafc;
  color: #1e293b;
  font-size: 14px;
  font-weight: 600;
  border-bottom: 1px solid #e2e8f0;
}

.section-content {
  padding: 20px;
}

.nutrition-chart {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}

.chart-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-label {
  width: 80px;
  color: #374151;
  font-size: 13px;
  font-weight: 500;
}

.chart-bar {
  flex: 1;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.chart-fill {
  height: 100%;
  background: #3b82f6;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.chart-fill.protein {
  background: #10b981;
}

.chart-fill.fat {
  background: #f59e0b;
}

.chart-value {
  width: 40px;
  text-align: right;
  color: #64748b;
  font-size: 12px;
  font-weight: 500;
}

.nutrition-summary {
  color: #374151;
  font-size: 13px;
  line-height: 1.6;
  padding: 12px;
  background: #f8fafc;
  border-radius: 6px;
}

.suggestions-list,
.improvements-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item,
.improvement-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.suggestion-icon {
  color: #10b981;
  margin-top: 2px;
}

.improvement-icon {
  color: #f59e0b;
  margin-top: 2px;
}

.suggestion-text,
.improvement-text {
  color: #374151;
  font-size: 13px;
  line-height: 1.5;
}

.trend-analysis {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  background: #f8fafc;
}

.trend-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1e293b;
  font-size: 16px;
  font-weight: 600;
}

.trend-content {
  padding: 24px;
}

.trend-charts {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-container {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.chart-title {
  padding: 12px 16px;
  background: #f8fafc;
  color: #374151;
  font-size: 13px;
  font-weight: 500;
  border-bottom: 1px solid #e2e8f0;
}

.chart-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #94a3b8;
  font-size: 12px;
  gap: 8px;
}

.ai-chat {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f1f5f9;
  background: #f8fafc;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1e293b;
  font-size: 14px;
  font-weight: 600;
}

.chat-content {
  display: flex;
  flex-direction: column;
  height: 400px;
}

.chat-messages {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.message-item.user-message {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
}

.user-message .message-content {
  text-align: right;
}

.message-text {
  background: #f1f5f9;
  padding: 8px 12px;
  border-radius: 12px;
  color: #374151;
  font-size: 13px;
  line-height: 1.4;
}

.user-message .message-text {
  background: #3b82f6;
  color: white;
}

.message-time {
  color: #94a3b8;
  font-size: 11px;
  margin-top: 4px;
}

.chat-input {
  padding: 16px;
  border-top: 1px solid #f1f5f9;
}

@media (max-width: 768px) {
  .ai-analysis-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .analysis-results {
    grid-template-columns: 1fr;
  }
  
  .overview-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .chart-item {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
  
  .chart-label {
    width: auto;
  }
  
  .chart-value {
    width: auto;
    text-align: left;
  }
}
</style>

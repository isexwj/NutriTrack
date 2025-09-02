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
              <div class="stat-value">{{ todayStats.totalMeals }}</div>
              <div class="stat-label">用餐次数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ todayStats.totalCalories }}</div>
              <div class="stat-label">总卡路里</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ todayStats.avgRating }}</div>
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
                  {{ analysisReport.nutrition.summary }}
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
              <div class="chart-placeholder">
                <el-icon size="48"><Star /></el-icon>
                <div>图表数据加载中...</div>
              </div>
            </div>
            <div class="chart-container">
              <div class="chart-title">健康指数变化</div>
              <div class="chart-placeholder">
                <el-icon size="48"><Star /></el-icon>
                <div>图表数据加载中...</div>
              </div>
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
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { 
  Document, 
  Star, 
  User
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 响应式数据
const isAnalyzing = ref(false)
const isChatLoading = ref(false)
const trendPeriod = ref('7d')
const chatInput = ref('')
const lastAnalysisTime = ref('')

// 今日统计数据
const todayStats = ref({
  totalMeals: 3,
  totalCalories: 1250,
  avgRating: 4.2,
  healthScore: 85
})

// AI分析报告
const analysisReport = ref(null)

// 聊天消息
const chatMessages = ref([
  {
    id: 1,
    type: 'ai',
    content: '您好！我是您的AI营养师助手，可以帮您分析饮食记录并提供健康建议。有什么问题可以随时问我！',
    timestamp: new Date('2024-01-15 09:00:00')
  }
])

// 方法
const generateAnalysis = async () => {
  isAnalyzing.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // 模拟分析结果
    analysisReport.value = {
      nutrition: {
        carbs: 45,
        protein: 25,
        fat: 30,
        summary: '您的饮食结构相对均衡，碳水化合物摄入适中，蛋白质含量良好，建议适当增加膳食纤维的摄入。'
      },
      suggestions: [
        '建议增加蔬菜和水果的摄入量，每天至少5份',
        '可以适当增加全谷物食品，如燕麦、糙米等',
        '保持充足的水分摄入，每天至少8杯水',
        '建议减少加工食品的摄入，选择天然食材'
      ],
      improvements: [
        '早餐可以增加蛋白质含量，如鸡蛋、牛奶等',
        '午餐建议增加绿叶蔬菜的比例',
        '晚餐可以适当减少碳水化合物的摄入',
        '建议增加坚果类健康脂肪的摄入'
      ]
    }
    
    lastAnalysisTime.value = new Date().toLocaleString('zh-CN')
    ElMessage.success('分析报告生成成功！')
  } catch (error) {
    ElMessage.error('分析失败，请稍后重试')
  } finally {
    isAnalyzing.value = false
  }
}

const updateTrend = () => {
  ElMessage.info(`切换到${trendPeriod.value}趋势分析`)
}

const sendMessage = async () => {
  if (!chatInput.value.trim()) return
  
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
    // 模拟AI回复
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    const aiResponse = generateAIResponse(userQuestion)
    const aiMessage = {
      id: Date.now() + 1,
      type: 'ai',
      content: aiResponse,
      timestamp: new Date()
    }
    
    chatMessages.value.push(aiMessage)
  } catch (error) {
    ElMessage.error('发送失败，请稍后重试')
  } finally {
    isChatLoading.value = false
  }
}

const generateAIResponse = (question) => {
  const responses = {
    '卡路里': '根据您今天的饮食记录，您摄入了约1250卡路里，这个数值在正常范围内。建议根据您的活动量适当调整。',
    '营养': '您的营养搭配整体不错，建议增加蔬菜水果的摄入，保持饮食多样化。',
    '减肥': '健康的减重需要控制总卡路里摄入，增加运动量。建议每天减少200-300卡路里，配合适量运动。',
    '增重': '健康增重需要增加优质蛋白质和健康脂肪的摄入，建议多吃坚果、鱼类、瘦肉等。',
    '早餐': '早餐是一天中最重要的一餐，建议包含蛋白质、复合碳水化合物和健康脂肪。',
    '晚餐': '晚餐建议清淡一些，减少碳水化合物摄入，多吃蔬菜和优质蛋白质。'
  }
  
  for (const [key, response] of Object.entries(responses)) {
    if (question.includes(key)) {
      return response
    }
  }
  
  return '感谢您的提问！根据您的饮食记录，我建议您保持均衡饮食，多吃新鲜蔬菜水果，适量运动。如果您有具体的营养问题，可以详细描述，我会为您提供更精准的建议。'
}

const formatTime = (date) => {
  return new Date(date).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  // 初始化时生成一次分析报告
  generateAnalysis()
})
</script>

<style scoped>
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

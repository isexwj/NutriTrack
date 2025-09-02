<template>
  <div class="dashboard-container">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <div class="header-content">
        <h2 class="dashboard-title">首页仪表盘</h2>
        <p class="dashboard-subtitle">欢迎回来，{{ userStore.username }}！今天是 {{ currentDate }}</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="quickAddMeal">
          <el-icon><Star /></el-icon>
          快速记录
        </el-button>
      </div>
    </div>
    
    <!-- 今日打卡状态 -->
    <div class="meal-checkin">
      <div class="checkin-title">
        <el-icon><Star /></el-icon>
        今日用餐打卡
      </div>
      <div class="checkin-cards">
        <div 
          v-for="meal in mealTypes" 
          :key="meal.type"
          class="checkin-card"
          :class="{ 
            'checked': meal.checked, 
            'missed': meal.missed,
            'upcoming': meal.upcoming 
          }"
          @click="handleMealCheckin(meal)"
        >
          <div class="meal-icon">
            <el-icon size="24"><component :is="meal.icon" /></el-icon>
          </div>
          <div class="meal-info">
            <div class="meal-name">{{ meal.name }}</div>
            <div class="meal-time">{{ meal.time }}</div>
            <div class="meal-status">
              <el-tag 
                :type="meal.checked ? 'success' : meal.missed ? 'danger' : 'info'" 
                size="small"
              >
                {{ meal.checked ? '已打卡' : meal.missed ? '已错过' : '待打卡' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-overview">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon calories">
            <el-icon size="20"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ todayStats.calories }}</div>
            <div class="stat-label">今日卡路里</div>
            <div class="stat-trend positive">+12%</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon meals">
            <el-icon size="20"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ todayStats.meals }}</div>
            <div class="stat-label">用餐次数</div>
            <div class="stat-trend neutral">0%</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon rating">
            <el-icon size="20"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ todayStats.rating }}</div>
            <div class="stat-label">平均评分</div>
            <div class="stat-trend positive">+0.3</div>
          </div>
        </div>
        
        <div class="stat-card">
          <div class="stat-icon health">
            <el-icon size="20"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ todayStats.healthScore }}</div>
            <div class="stat-label">健康指数</div>
            <div class="stat-trend positive">+5</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧：最近记录 -->
      <div class="content-left">
        <div class="recent-meals">
          <div class="section-header">
            <h3 class="section-title">最近记录</h3>
            <el-button type="text" @click="viewAllMeals">查看全部</el-button>
          </div>
          <div class="meals-list">
            <div v-if="recentMeals.length === 0" class="empty-meals">
              <el-empty description="暂无记录" :image-size="60" />
            </div>
            <div v-else class="meals-items">
              <div 
                v-for="meal in recentMeals" 
                :key="meal.id"
                class="meal-item"
              >
                <div class="meal-type-tag">
                  <el-tag :type="getMealTypeColor(meal.type)" size="small">
                    {{ getMealTypeName(meal.type) }}
                  </el-tag>
                </div>
                <div class="meal-content">
                  <div class="meal-description">{{ meal.description }}</div>
                  <div class="meal-meta">
                    <span class="meal-calories">{{ meal.calories }} 卡路里</span>
                    <span class="meal-time">{{ formatTime(meal.createdAt) }}</span>
                  </div>
                </div>
                <div class="meal-rating">
                  <el-rate v-model="meal.rating" disabled size="small" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：社区动态和AI建议 -->
      <div class="content-right">
        <!-- 社区排行榜 -->
        <div class="community-ranking">
          <div class="section-header">
            <h3 class="section-title">社区排行榜</h3>
            <el-button type="text" @click="viewCommunity">查看社区</el-button>
          </div>
          <div class="ranking-list">
            <div 
              v-for="(user, index) in communityRanking" 
              :key="user.id"
              class="ranking-item"
            >
              <div class="rank-number" :class="`rank-${index + 1}`">
                {{ index + 1 }}
              </div>
              <div class="user-info">
                <el-avatar :size="32">{{ user.name.charAt(0).toUpperCase() }}</el-avatar>
                <div class="user-details">
                  <div class="user-name">{{ user.name }}</div>
                  <div class="user-stats">{{ user.healthScore }} 健康指数</div>
                </div>
              </div>
              <div class="user-badge">
                <el-icon v-if="index < 3"><Star /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- AI健康建议 -->
        <div class="ai-suggestions">
          <div class="section-header">
            <h3 class="section-title">AI健康建议</h3>
            <el-button type="text" @click="viewAIAnalysis">详细分析</el-button>
          </div>
          <div class="suggestions-content">
            <div v-if="aiSuggestions.length === 0" class="no-suggestions">
              <el-empty description="暂无建议" :image-size="60" />
            </div>
            <div v-else class="suggestions-list">
              <div 
                v-for="(suggestion, index) in aiSuggestions" 
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
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { 
  Plus, 
  Calendar, 
  Star
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 响应式数据
const currentDate = ref('')
const todayStats = ref({
  calories: 1250,
  meals: 3,
  rating: 4.2,
  healthScore: 85
})

// 餐次类型数据
const mealTypes = ref([
  {
    type: 'breakfast',
    name: '早餐',
    time: '07:00-09:00',
    icon: Star,
    checked: true,
    missed: false,
    upcoming: false
  },
  {
    type: 'lunch',
    name: '午餐',
    time: '11:30-13:30',
    icon: Star,
    checked: true,
    missed: false,
    upcoming: false
  },
  {
    type: 'dinner',
    name: '晚餐',
    time: '17:30-19:30',
    icon: Star,
    checked: false,
    missed: false,
    upcoming: true
  }
])

// 最近记录数据
const recentMeals = ref([
  {
    id: 1,
    type: 'lunch',
    description: '健康午餐：藜麦沙拉配烤鸡胸肉',
    calories: 380,
    rating: 4,
    createdAt: new Date('2024-01-15 12:15:00')
  },
  {
    id: 2,
    type: 'breakfast',
    description: '营养早餐：全麦面包配牛油果和煎蛋',
    calories: 450,
    rating: 5,
    createdAt: new Date('2024-01-15 08:30:00')
  }
])

// 社区排行榜数据
const communityRanking = ref([
  { id: 1, name: '健康达人', healthScore: 95 },
  { id: 2, name: '营养师小王', healthScore: 92 },
  { id: 3, name: '健身教练', healthScore: 88 },
  { id: 4, name: '养生达人', healthScore: 85 },
  { id: 5, name: '美食爱好者', healthScore: 82 }
])

// AI建议数据
const aiSuggestions = ref([
  '建议增加蔬菜摄入量，每天至少5份',
  '可以适当增加全谷物食品的摄入',
  '保持充足的水分摄入，每天至少8杯水'
])

// 方法
const formatTime = (date) => {
  return new Date(date).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const getMealTypeName = (type) => {
  const typeMap = {
    breakfast: '早餐',
    lunch: '午餐',
    dinner: '晚餐'
  }
  return typeMap[type] || type
}

const getMealTypeColor = (type) => {
  const colorMap = {
    breakfast: 'warning',
    lunch: 'success',
    dinner: 'info'
  }
  return colorMap[type] || 'default'
}

const handleMealCheckin = (meal) => {
  if (meal.checked) {
    ElMessage.info(`${meal.name}已经打卡过了`)
    return
  }
  
  if (meal.missed) {
    ElMessage.warning(`${meal.name}时间已过，无法打卡`)
    return
  }
  
  // 模拟打卡
  meal.checked = true
  meal.upcoming = false
  ElMessage.success(`${meal.name}打卡成功！`)
}

const quickAddMeal = () => {
  ElMessage.info('跳转到添加记录页面')
  // 这里可以跳转到MyMeals页面并打开添加对话框
}

const viewAllMeals = () => {
  ElMessage.info('跳转到我的饮食记录页面')
  // 这里可以跳转到MyMeals页面
}

const viewCommunity = () => {
  ElMessage.info('跳转到社区记录页面')
  // 这里可以跳转到Community页面
}

const viewAIAnalysis = () => {
  ElMessage.info('跳转到AI分析页面')
  // 这里可以跳转到AIAnalysis页面
}

// 初始化
onMounted(() => {
  currentDate.value = new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
}

.dashboard-header {
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

.dashboard-title {
  color: #1e293b;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.dashboard-subtitle {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meal-checkin {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 24px;
  margin-bottom: 24px;
}

.checkin-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1e293b;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
}

.checkin-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.checkin-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.checkin-card:hover {
  border-color: #3b82f6;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

.checkin-card.checked {
  border-color: #10b981;
  background: #f0fdf4;
}

.checkin-card.missed {
  border-color: #ef4444;
  background: #fef2f2;
}

.checkin-card.upcoming {
  border-color: #3b82f6;
  background: #eff6ff;
}

.meal-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  color: #64748b;
}

.checkin-card.checked .meal-icon {
  background: #10b981;
  color: white;
}

.checkin-card.missed .meal-icon {
  background: #ef4444;
  color: white;
}

.checkin-card.upcoming .meal-icon {
  background: #3b82f6;
  color: white;
}

.meal-info {
  flex: 1;
}

.meal-name {
  color: #1e293b;
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.meal-time {
  color: #64748b;
  font-size: 12px;
  margin-bottom: 8px;
}

.stats-overview {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.calories {
  background: linear-gradient(135deg, #f59e0b, #f97316);
}

.stat-icon.meals {
  background: linear-gradient(135deg, #10b981, #059669);
}

.stat-icon.rating {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.stat-icon.health {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.stat-content {
  flex: 1;
}

.stat-value {
  color: #1e293b;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 4px;
}

.stat-label {
  color: #64748b;
  font-size: 12px;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 11px;
  font-weight: 500;
}

.stat-trend.positive {
  color: #10b981;
}

.stat-trend.negative {
  color: #ef4444;
}

.stat-trend.neutral {
  color: #64748b;
}

.main-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.content-left,
.content-right {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.recent-meals,
.community-ranking,
.ai-suggestions {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
  background: #f8fafc;
}

.section-title {
  color: #1e293b;
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.meals-list,
.ranking-list,
.suggestions-content {
  padding: 20px 24px;
}

.empty-meals,
.no-suggestions {
  text-align: center;
  padding: 40px 20px;
}

.meals-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.meal-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  transition: all 0.2s;
}

.meal-item:hover {
  border-color: #e2e8f0;
  background: #f8fafc;
}

.meal-type-tag {
  flex-shrink: 0;
}

.meal-content {
  flex: 1;
  min-width: 0;
}

.meal-description {
  color: #374151;
  font-size: 13px;
  line-height: 1.4;
  margin-bottom: 4px;
}

.meal-meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #94a3b8;
}

.meal-rating {
  flex-shrink: 0;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.ranking-item:hover {
  background: #f8fafc;
}

.rank-number {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.rank-number.rank-1 {
  background: #f59e0b;
}

.rank-number.rank-2 {
  background: #6b7280;
}

.rank-number.rank-3 {
  background: #cd7c2f;
}

.rank-number:not(.rank-1):not(.rank-2):not(.rank-3) {
  background: #e2e8f0;
  color: #64748b;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.user-name {
  color: #1e293b;
  font-size: 13px;
  font-weight: 500;
}

.user-stats {
  color: #64748b;
  font-size: 11px;
}

.user-badge {
  color: #f59e0b;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.suggestion-icon {
  color: #f59e0b;
  margin-top: 2px;
}

.suggestion-text {
  color: #374151;
  font-size: 13px;
  line-height: 1.4;
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }
  
  .dashboard-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .checkin-cards {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .main-content {
    grid-template-columns: 1fr;
  }
  
  .meal-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .meal-meta {
    flex-direction: column;
    gap: 4px;
  }
}
</style>

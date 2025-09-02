<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h2 class="dashboard-title">首页仪表盘</h2>
      <p class="dashboard-subtitle">欢迎回来，{{ userStore.username }}！</p>
    </div>
    
    <div class="dashboard-content">
      <div class="welcome-card">
        <div class="welcome-icon">
          <el-icon size="32"><House /></el-icon>
        </div>
        <div class="welcome-text">
          <h3>欢迎使用 NutriTrack</h3>
          <p>这里是您的饮食健康管理首页，您可以在这里查看各种统计信息和快速操作。</p>
        </div>
      </div>
      
      <div class="placeholder-content">
        <el-empty description="页面内容开发中..." />
      </div>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '@/store/user'
import { House } from '@element-plus/icons-vue'

const userStore = useUserStore()
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
}

.dashboard-header {
  margin-bottom: 32px;
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

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.welcome-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.welcome-icon {
  background: #3b82f6;
  color: white;
  border-radius: 50%;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.welcome-text h3 {
  color: #1e293b;
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.welcome-text p {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.placeholder-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 40px;
  text-align: center;
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 16px;
  }
  
  .welcome-card {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
}
</style>

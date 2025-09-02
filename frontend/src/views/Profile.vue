<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2 class="profile-title">个人资料</h2>
      <p class="profile-subtitle">查看和管理您的个人信息</p>
    </div>
    
    <div class="profile-content">
      <div class="user-info-card">
        <div class="user-avatar">
          <el-icon size="24"><User /></el-icon>
        </div>
        <div class="user-details">
          <h3>用户信息</h3>
          <p><strong>用户名：</strong>{{ userStore.username }}</p>
          <p><strong>登录状态：</strong><span class="status-success">已登录</span></p>
          <p><strong>登录时间：</strong>{{ loginTime }}</p>
        </div>
      </div>
      
      <div class="action-buttons">
        <el-button type="primary" size="default" @click="refreshInfo">
          <el-icon size="16"><Refresh /></el-icon>
          刷新信息
        </el-button>
        <el-button type="danger" size="default" @click="handleLogout">
          <el-icon size="16"><SwitchButton /></el-icon>
          退出登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Refresh, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loginTime = ref('')

onMounted(() => {
  loginTime.value = new Date().toLocaleString('zh-CN')
})

const refreshInfo = () => {
  loginTime.value = new Date().toLocaleString('zh-CN')
  ElMessage.success('信息已刷新')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    userStore.clearUser()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消操作
  }
}
</script>

<style scoped>
.profile-container {
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
}

.profile-header {
  margin-bottom: 32px;
}

.profile-title {
  color: #1e293b;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.profile-subtitle {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.profile-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 32px;
  max-width: 600px;
}

.user-info-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-avatar {
  background: #3b82f6;
  color: white;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.user-details {
  text-align: left;
  flex: 1;
}

.user-details h3 {
  color: #1e293b;
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 16px 0;
}

.user-details p {
  color: #475569;
  margin: 8px 0;
  font-size: 14px;
}

.status-success {
  color: #10b981;
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.action-buttons .el-button {
  min-width: 120px;
  height: 40px;
  font-size: 14px;
  border-radius: 8px;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 16px;
  }
  
  .profile-content {
    padding: 24px;
  }
  
  .user-info-card {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }
  
  .user-details {
    text-align: center;
  }
  
  .action-buttons {
    flex-direction: column;
  }
  
  .action-buttons .el-button {
    width: 100%;
  }
}
</style>

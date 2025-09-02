<template>
  <div class="app-container">
    <!-- 只在非Home页面显示导航栏 -->
    <el-header class="app-header" v-if="!isHomePage">
      <div class="header-content">
        <div class="logo-section">
          <h1 class="logo">NutriTrack</h1>
        </div>
        
        <div class="nav-section">
          <el-menu 
            mode="horizontal" 
            :router="true"
            class="nav-menu"
            background-color="transparent"
            text-color="#2c3e50"
            active-text-color="#409eff"
          >
            <el-menu-item index="/login" v-if="!userStore.token">
              <el-icon><User /></el-icon>
              登录
            </el-menu-item>
            
            <el-menu-item index="/register" v-if="!userStore.token">
              <el-icon><EditPen /></el-icon>
              注册
            </el-menu-item>
          </el-menu>
        </div>
        
        <div class="user-section" v-if="userStore.token">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="28" class="user-avatar">
                {{ userStore.username.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>
    
    <el-main class="app-main" :class="{ 'no-header': isHomePage }">
      <router-view />
    </el-main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  House, 
  User, 
  EditPen, 
  ArrowDown, 
  Setting, 
  SwitchButton 
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 判断当前是否为Home页面
const isHomePage = computed(() => route.path === '/home')

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料功能开发中...')
      break
    case 'settings':
      ElMessage.info('设置功能开发中...')
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = () => {
  userStore.clearUser()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
}

.app-header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 20px;
}

.logo-section .logo {
  color: #409eff;
  font-size: 22px;
  font-weight: 600;
  margin: 0;
}

.nav-section {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu {
  border: none;
}

.nav-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 6px;
  margin: 0 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-menu .el-menu-item:hover {
  background-color: #f5f7fa;
}

.nav-menu .el-menu-item.is-active {
  background-color: #ecf5ff;
  color: #409eff;
}

.user-section {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 6px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  background: #409eff;
  color: white;
  font-weight: 600;
  font-size: 12px;
}

.username {
  color: #2c3e50;
  font-weight: 500;
  font-size: 13px;
}

.app-main {
  padding: 0;
  background: #f5f7fa;
}

.app-main.no-header {
  background: white;
}

:deep(.el-header) {
  height: 48px;
  line-height: 48px;
}

:deep(.el-main) {
  padding: 0;
}

@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }
  
  .logo-section .logo {
    font-size: 20px;
  }
  
  .nav-section {
    display: none;
  }
  
  .user-section {
    margin-left: auto;
  }
  
  .username {
    display: none;
  }
}
</style>
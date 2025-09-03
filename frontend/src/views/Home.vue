<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <header class="home-header">
      <div class="header-content">
        <div class="logo-section">
          <h1 class="logo">NutriTrack</h1>
        </div>
        
        <div class="header-actions">
          <!-- 消息提示图标 -->
          <div class="notification-icon" @click="toggleNotification">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
              <el-icon size="20" class="notification-icon-svg">
                <Bell />
              </el-icon>
            </el-badge>
          </div>
          
          <!-- 用户信息 -->
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.username.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.username }}</span>
              <el-icon size="14"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon size="14"><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon size="14"><Setting /></el-icon>
                  设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon size="14"><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <!-- 消息提示下拉面板 -->
    <div v-if="showNotification" class="notification-panel" @click.stop>
      <div class="notification-header">
        <h3>消息提醒</h3>
        <el-button type="text" size="small" @click="markAllAsRead">全部已读</el-button>
      </div>
      <div class="notification-list">
        <div 
          v-for="notification in notifications" 
          :key="notification.id"
          class="notification-item"
          :class="{ unread: !notification.read }"
          @click="markAsRead(notification.id)"
        >
          <div class="notification-icon-small">
            <el-icon size="16" :color="notification.type === 'meal' ? '#f59e0b' : '#3b82f6'">
              <component :is="notification.type === 'meal' ? 'Food' : 'Bell'" />
            </el-icon>
          </div>
          <div class="notification-content">
            <div class="notification-title">{{ notification.title }}</div>
            <div class="notification-message">{{ notification.message }}</div>
            <div class="notification-time">{{ notification.time }}</div>
          </div>
        </div>
        <div v-if="notifications.length === 0" class="no-notifications">
          <el-empty description="暂无消息" :image-size="60" />
        </div>
      </div>
    </div>

    <!-- 消息提示遮罩层 -->
    <div v-if="showNotification" class="notification-overlay" @click="closeNotification"></div>

    <!-- 主体布局：左侧导航 + 右侧内容 -->
    <div class="main-layout">
      <!-- 左侧导航栏 -->
      <aside class="sidebar">
        <nav class="nav-menu">
          <div 
            v-for="item in menuItems" 
            :key="item.key"
            class="nav-item"
            :class="{ active: activeMenu === item.key }"
            @click="switchMenu(item.key)"
          >
            <el-icon size="18"><component :is="item.icon" /></el-icon>
            <span class="nav-text">{{ item.label }}</span>
          </div>
        </nav>
      </aside>

      <!-- 右侧内容区域 -->
      <main class="content-area">
       <component :is="currentComponent"
           @update-active-menu="switchMenu" />
         <!-- 监听子组件的切换事件 -->
      </main>
      
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  User, 
  SwitchButton, 
  ArrowDown, 
  Setting, 
  House, 
  Document, 
  Files, 
  Folder,
  Bell,
  Food
} from '@element-plus/icons-vue'

// 导入页面组件
import Dashboard from './Dashboard.vue'
import MyMeals from './MyMeals.vue'
import Community from './Community.vue'
import AIAnalysis from './AIAnalysis.vue'
import Profile from './Profile.vue'

const router = useRouter()
const userStore = useUserStore()

// 当前激活的菜单项
const activeMenu = ref('dashboard')

// 消息提示相关
const showNotification = ref(false)

// 示例消息数据
const notifications = ref([
  {
    id: 1,
    type: 'meal',
    title: '用餐提醒',
    message: '该吃早餐了！记得记录您的早餐内容哦~',
    time: '2分钟前',
    read: false
  },
  {
    id: 2,
    type: 'meal',
    title: '用餐提醒',
    message: '午餐时间到了，别忘了打卡记录！',
    time: '1小时前',
    read: false
  },
  {
    id: 3,
    type: 'system',
    title: '系统通知',
    message: '欢迎使用NutriTrack！开始记录您的健康饮食吧。',
    time: '3小时前',
    read: true
  },
  {
    id: 4,
    type: 'meal',
    title: '用餐提醒',
    message: '晚餐时间到了，记得记录您的晚餐内容！',
    time: '5小时前',
    read: true
  },
  {
    id: 5,
    type: 'system',
    title: '健康建议',
    message: '您今天还没有记录任何餐食，记得按时用餐哦！',
    time: '昨天',
    read: true
  }
])

// 计算未读消息数量
const unreadCount = computed(() => {
  return notifications.value.filter(n => !n.read).length
})

// 菜单项配置
const menuItems = [
  { key: 'dashboard', label: '首页仪表盘', icon: House },
  { key: 'myMeals', label: '我的饮食记录', icon: Document },
  { key: 'community', label: '社区记录', icon: Files },
  { key: 'aiAnalysis', label: 'AI分析助手', icon: Folder }
]

// 组件映射
const componentMap = {
  dashboard: Dashboard,
  myMeals: MyMeals,
  community: Community,
  aiAnalysis: AIAnalysis,
  profile: Profile
}

// 当前显示的组件
const currentComponent = computed(() => {
  return componentMap[activeMenu.value] || Dashboard
})

// 切换菜单
const switchMenu = (menuKey) => {
  activeMenu.value = menuKey
}

// 消息提示相关方法
const toggleNotification = () => {
  showNotification.value = !showNotification.value
}

const closeNotification = () => {
  showNotification.value = false
}

const markAsRead = (notificationId) => {
  const notification = notifications.value.find(n => n.id === notificationId)
  if (notification) {
    notification.read = true
  }
}

const markAllAsRead = () => {
  notifications.value.forEach(notification => {
    notification.read = true
  })
}

// 处理用户下拉菜单命令
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      activeMenu.value = 'profile'
      break
    case 'settings':
      ElMessage.info('设置功能开发中...')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
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
.home-container {
  min-height: 100vh;
  background: #f8fafc;
  display: flex;
  flex-direction: column;
}

.home-header {
  background: white;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 0;
  position: sticky;
  top: 0;
  z-index: 1000;
  flex-shrink: 0;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 24px;
}

.logo-section .logo {
  color: #1e293b;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-section {
  display: flex;
  align-items: center;
}

/* 消息提示图标样式 */
.notification-icon {
  position: relative;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.notification-icon:hover {
  background-color: #f1f5f9;
}

.notification-icon-svg {
  color: #64748b;
  transition: color 0.2s;
}

.notification-icon:hover .notification-icon-svg {
  color: #3b82f6;
}

.notification-badge {
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 消息提示面板样式 */
.notification-panel {
  position: absolute;
  top: 48px;
  right: 24px;
  width: 360px;
  max-height: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  overflow: hidden;
}

.notification-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
  background: #f8fafc;
}

.notification-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
}

.notification-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #f1f5f9;
}

.notification-item:hover {
  background-color: #f8fafc;
}

.notification-item.unread {
  background-color: #eff6ff;
}

.notification-item.unread:hover {
  background-color: #dbeafe;
}

.notification-icon-small {
  flex-shrink: 0;
  margin-top: 2px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 4px;
}

.notification-message {
  font-size: 13px;
  color: #64748b;
  line-height: 1.4;
  margin-bottom: 6px;
}

.notification-time {
  font-size: 12px;
  color: #94a3b8;
}

.no-notifications {
  padding: 40px 20px;
  text-align: center;
}

/* 消息提示遮罩层 */
.notification-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  background: transparent;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f1f5f9;
}

.user-avatar {
  background: #3b82f6;
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.username {
  color: #1e293b;
  font-weight: 500;
  font-size: 14px;
}

/* 主体布局 */
.main-layout {
  display: flex;
  flex: 1;
  min-height: calc(100vh - 48px);
}

/* 左侧导航栏 */
.sidebar {
  width: 240px;
  background: white;
  border-right: 1px solid #e2e8f0;
  flex-shrink: 0;
  overflow-y: auto;
}

.nav-menu {
  padding: 16px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 24px;
  cursor: pointer;
  transition: all 0.2s;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
}

.nav-item:hover {
  background-color: #f8fafc;
  color: #3b82f6;
}

.nav-item.active {
  background-color: #eff6ff;
  color: #3b82f6;
  border-right: 3px solid #3b82f6;
}

.nav-text {
  flex: 1;
}

/* 右侧内容区域 */
.content-area {
  flex: 1;
  overflow-y: auto;
  background: #f8fafc;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 16px;
  }
  
  .header-actions {
    gap: 12px;
  }
  
  .notification-panel {
    right: 16px;
    width: calc(100vw - 32px);
    max-width: 360px;
  }
  
  .main-layout {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #e2e8f0;
  }
  
  .nav-menu {
    display: flex;
    padding: 8px 0;
    overflow-x: auto;
  }
  
  .nav-item {
    flex-shrink: 0;
    padding: 8px 16px;
    border-right: none;
    border-bottom: 3px solid transparent;
  }
  
  .nav-item.active {
    border-right: none;
    border-bottom: 3px solid #3b82f6;
  }
  
  .nav-text {
    white-space: nowrap;
  }
}

@media (max-width: 480px) {
  .nav-text {
    display: none;
  }
  
  .nav-item {
    justify-content: center;
    padding: 12px;
  }
}
</style>

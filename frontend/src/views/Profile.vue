<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2 class="profile-title">个人资料</h2>
      <p class="profile-subtitle">查看和管理您的个人信息</p>
    </div>

    <div class="profile-content">
      <el-form
          :model="userInfo"
          :rules="rules"
          ref="profileFormRef"
          label-width="100px"
          class="profile-form"
      >
        <div class="user-info-card">
<!--          <div class="user-avatar-section">-->
<!--            <div class="user-avatar-preview">-->
<!--              <el-avatar :size="80" :src="userInfo.avatarUrl || defaultAvatar">-->
<!--                {{ userInfo.username?.charAt(0).toUpperCase() }}-->
<!--              </el-avatar>-->
<!--            </div>-->
<!--            <el-upload-->
<!--                class="avatar-uploader"-->
<!--                action="#"-->
<!--                :auto-upload="false"-->
<!--                :show-file-list="false"-->
<!--                :on-change="handleAvatarChange"-->
<!--            >-->
<!--              <el-button size="small" type="primary">上传头像</el-button>-->
<!--            </el-upload>-->
<!--          </div>-->

          <div class="user-details">
            <el-form-item label="用户名：" prop="username">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>

            <el-form-item label="昵称：" prop="nickname">
              <el-input v-model="userInfo.nickname" />
            </el-form-item>

            <el-form-item label="邮箱：" prop="email">
              <el-input v-model="userInfo.email" />
            </el-form-item>

            <el-form-item label="手机号：" prop="phoneNumber">
              <el-input v-model="userInfo.phoneNumber" />
            </el-form-item>

<!--            <el-form-item label="注册时间：" prop="createdAt">-->
<!--              <el-input :value="formatDateTime(userInfo.createdAt)" disabled />-->
<!--            </el-form-item>-->

<!--            <el-form-item label="最后登录：" prop="lastLoginAt">-->
<!--              <el-input :value="formatDateTime(userInfo.lastLoginAt)" disabled />-->
<!--            </el-form-item>-->
          </div>
        </div>

        <div class="form-actions">
          <el-button type="primary" @click="saveProfile" :loading="saving">
            保存信息
          </el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button type="danger" @click="deleteAccount" :loading="deactivating">
            注销账户
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getUserInfo, updateUserInfo, deactivateAccount } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User } from '@element-plus/icons-vue'

const userStore = useUserStore()
const profileFormRef = ref()
const saving = ref(false)
const deactivating = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userInfo = reactive({
  id: null,
  username: '',
  nickname: '',
  email: '',
  phoneNumber: '',
  createdAt: '',
  lastLoginAt: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度为2-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phoneNumber: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadUserInfo()
})

const loadUserInfo = async () => {
  try {
    const response = await getUserInfo(userStore.username)
    if (response.code === 200) {
      Object.assign(userInfo, response.data)
    } else {
      ElMessage.error(response.message || '获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

const saveProfile = async () => {
  try {
    await profileFormRef.value.validate()
    saving.value = true

    const updateData = {
      nickname: userInfo.nickname,
      email: userInfo.email,
      phoneNumber: userInfo.phoneNumber,
      avatarUrl: userInfo.avatarUrl
    }

    const response = await updateUserInfo(userStore.username, updateData)
    if (response.code === 200) {
      ElMessage.success('信息保存成功')
      loadUserInfo() // 重新加载用户信息
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const resetForm = () => {
  profileFormRef.value?.resetFields()
  loadUserInfo() // 重新加载原始数据
}

const handleAvatarChange = (file) => {
  // 这里应该上传文件到服务器并获取URL
  // 作为示例，我们使用一个本地URL
  const reader = new FileReader()
  reader.onload = (e) => {
    userInfo.avatarUrl = e.target.result
    ElMessage.success('头像已更新（演示用）')
  }
  reader.readAsDataURL(file.raw)
}

const getUserStatusType = (status) => {
  const statusMap = {
    'active': 'success',
    'inactive': 'warning',
    'suspended': 'danger',
    'deleted': 'info'
  }
  return statusMap[status] || 'info'
}

const getUserStatusText = (status) => {
  const statusMap = {
    'active': '活跃',
    'inactive': '未激活',
    'suspended': '被封禁',
    'deleted': '已注销'
  }
  return statusMap[status] || '未知'
}

const formatDateTime = (date) => {
  if (!date) return '暂无'
  return new Date(date).toLocaleString('zh-CN')
}

const deleteAccount = async () => {
  try {
    await ElMessageBox.confirm(
        '确定要注销您的账户吗？此操作不可恢复，所有数据将被永久删除。',
        '注销账户',
        {
          confirmButtonText: '确定注销',
          cancelButtonText: '取消',
          type: 'warning'
        }
    )

    deactivating.value = true
    const response = await deactivateAccount(userStore.username)

    if (response.code === 200) {
      ElMessage.success('账户注销成功')
      // 清除用户信息并跳转到登录页
      userStore.clearUser()
      window.location.href = '/login'
    } else {
      ElMessage.error(response.message || '注销失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('注销失败:', error)
      ElMessage.error('注销失败')
    }
  } finally {
    deactivating.value = false
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
  max-width: 800px;
  margin: 0 auto;
}

.user-info-card {
  display: flex;
  gap: 40px;
  margin-bottom: 32px;
  padding: 24px;
  background: #f8fafc;
  border-radius: 12px;
}

.user-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.user-avatar-preview {
  width: 80px;
  height: 80px;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
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
    align-items: center;
    text-align: center;
    gap: 24px;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions .el-button {
    width: 100%;
  }
}
</style>

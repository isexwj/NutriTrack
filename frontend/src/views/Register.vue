<template>
  <div class="register-container">
    <div class="form-wrapper">
      <div class="form-header">
        <h2>创建账户</h2>
        <p>请填写以下信息完成注册</p>
      </div>
      
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="formRef" 
        @submit.prevent="handleRegister"
        class="register-form"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入用户名"
            prefix-icon="User"
            size="default"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="default"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="请确认密码"
            prefix-icon="Lock"
            size="default"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleRegister" 
            :loading="loading"
            size="default"
            class="submit-btn"
          >
            {{ loading ? '注册中...' : '注册' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="form-footer">
        <span class="login-text">已有账户？</span>
        <el-button type="text" @click="$router.push('/login')" class="link-btn">
          立即登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { register } from '@/api/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const { confirmPassword, ...registerData } = form
    await register(registerData)
    
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('注册失败，请重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
  padding: 20px;
}

.form-wrapper {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  padding: 32px;
  width: 100%;
  max-width: 400px;
}

.form-header {
  text-align: center;
  margin-bottom: 28px;
}

.form-header h2 {
  color: #1e293b;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.form-header p {
  color: #64748b;
  margin: 0;
  font-size: 14px;
}

.register-form {
  margin-bottom: 20px;
}

.submit-btn {
  width: 100%;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
}

.form-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
}

.login-text {
  color: #64748b;
  font-size: 13px;
}

.link-btn {
  color: #3b82f6;
  font-size: 13px;
  padding: 6px 0;
}

.link-btn:hover {
  color: #2563eb;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  height: 40px;
}

:deep(.el-input__inner) {
  font-size: 14px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-input__prefix) {
  font-size: 16px;
}
</style>

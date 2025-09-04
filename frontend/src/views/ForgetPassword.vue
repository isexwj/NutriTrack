<template>
  <div class="forget-password-container">
    <div class="form-wrapper">
      <div class="form-header">
        <h2>重置密码</h2>
        <p>通过邮箱验证码验证身份并设置新密码</p>
      </div>
      
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="formRef" 
        @submit.prevent="handleResetPassword"
        class="reset-form"
      >
        <el-form-item prop="email">
          <el-input 
            v-model="form.email" 
            placeholder="请输入邮箱"
            prefix-icon="Message"
            size="default"
          />
        </el-form-item>

        <el-form-item prop="emailCode">
          <div style="display:flex; gap:8px; width:100%">
            <el-input 
              v-model="form.emailCode"
              placeholder="请输入邮箱验证码"
              size="default"
            />
            <el-button :disabled="sendCountdown>0 || sending" @click="handleSendCode" style="white-space:nowrap; height:40px;">
              {{ sending ? '发送中...' : (sendCountdown>0 ? sendCountdown + 's' : '发送验证码') }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item prop="newPassword">
          <el-input 
            v-model="form.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            prefix-icon="Lock"
            size="default"
            show-password
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="请确认新密码"
            prefix-icon="Lock"
            size="default"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleResetPassword" 
            :loading="loading"
            size="default"
            class="submit-btn"
          >
            {{ loading ? '重置中...' : '重置密码' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="form-footer">
        <el-button type="text" @click="$router.push('/login')" class="link-btn">
          <el-icon size="14"><ArrowLeft /></el-icon>
          返回登录
        </el-button>
        <el-button type="text" @click="$router.push('/register')" class="link-btn">
          还没有账户？立即注册
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { forgetPasswordSend, forgetPasswordReset } from '@/api/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const sending = ref(false)
const sendCountdown = ref(0)
let timer = null

const form = reactive({
  email: '',
  emailCode: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleSendCode = async () => {
  if (!form.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  try {
    sending.value = true
    await forgetPasswordSend({ email: form.email })
    ElMessage.success('验证码已发送，请查收邮箱')
    sendCountdown.value = 60
    timer = setInterval(() => {
      sendCountdown.value--
      if (sendCountdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('发送失败，请重试')
    }
  } finally {
    sending.value = false
  }
}

const handleResetPassword = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    const res = await forgetPasswordReset({ email: form.email, emailCode: form.emailCode, newPassword: form.newPassword, confirmPassword: form.confirmPassword })
    if (res && res.code === 200) {
      ElMessage.success('密码重置成功，请使用新密码登录')
      router.push('/login')
    } else {
      ElMessage.error(res?.message || '密码重置失败，请检查验证码')
    }
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('密码重置失败，请重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forget-password-container {
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

.reset-form {
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
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
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

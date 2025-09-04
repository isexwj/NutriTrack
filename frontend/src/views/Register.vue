<template>
  <div class="register-container">
    <div class="form-wrapper">
      <div class="form-header">
        <h2>创建账户</h2>
        <p v-if="step === 1">请填写账户信息</p>
        <p v-else>请输入邮箱并完成验证码验证</p>
      </div>
      
      <el-form 
        v-if="step === 1"
        :model="form" 
        :rules="rulesStep1" 
        ref="formRef"
        @submit.prevent="handleStep1"
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
        <el-form-item prop="nickname">
          <el-input 
            v-model="form.nickname" 
            placeholder="请输入昵称（可与用户名相同）"
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
            @click="handleStep1" 
            :loading="loading"
            size="default"
            class="submit-btn"
          >
            {{ loading ? '下一步...' : '下一步' }}
          </el-button>
        </el-form-item>
      </el-form>

      <el-form
        v-else
        :model="formStep2"
        :rules="rulesStep2"
        ref="formRef2"
        @submit.prevent="handleStep2"
        class="register-form"
      >
        <el-form-item prop="email">
          <el-input
            v-model="formStep2.email"
            placeholder="请输入邮箱"
            prefix-icon="Message"
            size="default"
          />
        </el-form-item>
        <el-form-item prop="emailCode">
          <div style="display:flex; gap:8px; width:100%">
            <el-input 
              v-model="formStep2.emailCode"
              placeholder="请输入邮箱验证码"
              size="default"
            />
            <el-button :disabled="sendCountdown>0 || sending" @click="handleSendCode" style="white-space:nowrap; height:40px;">
              {{ sending ? '发送中...' : (sendCountdown>0 ? sendCountdown + 's' : '发送验证码') }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleStep2" 
            :loading="loading"
            size="default"
            class="submit-btn"
          >
            {{ loading ? '提交中...' : '完成注册' }}
          </el-button>
        </el-form-item>
        <div class="form-footer" style="margin-top:-8px">
          <el-button type="text" @click="step=1">上一步</el-button>
        </div>
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
import { registerStep1, registerEmailSend, registerStep2 } from '@/api/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const formRef = ref()
const formRef2 = ref()
const loading = ref(false)
const sending = ref(false)
const step = ref(1)
const regToken = ref('')
const sendCountdown = ref(0)
let timer = null

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})
const formStep2 = reactive({
  email: '',
  emailCode: ''
})

const rulesStep1 = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
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
const rulesStep2 = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' }
  ]
}

const handleStep1 = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    loading.value = true
    const payload = {
      username: form.username,
      password: form.password,
      confirmPassword: form.confirmPassword,
      nickname: form.nickname
    }
    const res = await registerStep1(payload)
    regToken.value = res.data
    step.value = 2
    ElMessage.success('账户信息已保存，请绑定邮箱')
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('提交失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

const handleSendCode = async () => {
  if (!formStep2.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }
  try {
    sending.value = true
    await registerEmailSend({ regToken: regToken.value, email: formStep2.email })
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

const handleStep2 = async () => {
  if (!formRef2.value) return
  try {
    await formRef2.value.validate()
    loading.value = true
    await registerStep2({ regToken: regToken.value, email: formStep2.email, emailCode: formStep2.emailCode })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('提交失败，请重试')
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

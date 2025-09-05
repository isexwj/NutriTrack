<template>
  <div class="login-container">
    <div class="form-wrapper">
      <div class="form-header">
        <h2>欢迎回来</h2>
        <p>请登录您的账户以继续</p>
      </div>
      
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="formRef" 
        @submit.prevent="handleLogin"
        class="login-form"
      >
        <el-form-item prop="identifier">
          <el-input 
            v-model="form.identifier" 
            placeholder="请输入用户名或邮箱"
            prefix-icon="User"
            size="default"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="default"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item prop="captchaCode">
          <div style="display:flex; gap:8px; width:100%; align-items:center;">
            <el-input 
              v-model="form.captchaCode"
              placeholder="请输入验证码"
              size="default"
            />
            <img :src="captcha.imageBase64" :key="form.captchaId" @click="refreshCaptcha" style="height:40px; cursor:pointer; border-radius:6px;" title="点击刷新" alt="captcha"/>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleLogin" 
            :loading="loading"
            size="default"
            class="submit-btn"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="form-footer">
        <el-button type="text" @click="$router.push('/forgot')" class="link-btn">
          忘记密码？
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
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { login, getCaptcha } from '@/api/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  identifier: '',
  password: '',
  captchaId: '',
  captchaCode: ''
})
const captcha = reactive({ imageBase64: '' })
const captchaLoading = ref(false)
let lastCaptchaAt = 0

const rules = {
  identifier: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' }
  ]
}

const refreshCaptcha = async () => {
  const now = Date.now()
  if (captchaLoading.value || (now - lastCaptchaAt) < 800) return
  captchaLoading.value = true
  try {
    const res = await getCaptcha('login')
    console.debug('captcha resp:', res)
    if (res && res.code === 200 && res.data && res.data.imageBase64) {
      form.captchaId = res.data.captchaId
      const raw = String(res.data.imageBase64 || '').trim()
        .replace(/\s+/g, '')
        .replace(/^"|"$/g, '')
      const marker = 'base64,'
      const idx = raw.lastIndexOf(marker)
      const body = idx >= 0 ? raw.slice(idx + marker.length) : raw
      captcha.imageBase64 = body ? `data:image/png;base64,${body}` : ''
      console.debug('captcha img src len:', captcha.imageBase64.length)
    } else {
      console.warn('captcha invalid payload:', res)
    }
  } catch (e) {
    console.error('captcha request failed:', e)
  } finally {
    lastCaptchaAt = Date.now()
    captchaLoading.value = false
  }
}

refreshCaptcha()

const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    loading.value = true
    
    const res = await login({
      identifier: form.identifier,
      password: form.password,
      captchaId: form.captchaId,
      captchaCode: form.captchaCode
    })
    
    if (res.code === 200 && res.data) {
      userStore.setUser(res.data.token, res.data.username)
      ElMessage.success('登录成功')
      
      // 确保localStorage已经更新后再跳转
      setTimeout(() => {
        console.log('准备跳转到 /home，当前token:', localStorage.getItem('token'))
        router.push('/home').then(() => {
          console.log('路由跳转成功')
        }).catch((error) => {
          console.error('路由跳转失败:', error)
        })
      }, 100)
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch (error) {
    if (error.response?.data?.message) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('登录失败，请检查用户名和密码')
    }
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
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

.login-form {
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

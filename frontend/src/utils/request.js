import axios from 'axios'
import { useUserStore } from '@/store/user'

const service = axios.create({
  baseURL: '/',
  timeout: 5000
})

// 请求拦截器
service.interceptors.request.use(config => {
  const publicPaths = [
    '/user/login',
    '/user/captcha',
    '/user/register',
    '/user/register/step1',
    '/user/register/email/send',
    '/user/register/step2',
    '/user/forget-password',
    '/user/forget-password/send',
    '/user/forget-password/reset'
  ]
  const isPublic = publicPaths.some(p => (config.url || '').startsWith(p))

  if (!isPublic) {
    const userStore = useUserStore()
    const token = userStore.token || localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
  }
  return config
}, error => {
  return Promise.reject(error)
})

// 响应拦截器
service.interceptors.response.use(
  response => response.data,
  error => {
    return Promise.reject(error)
  }
)

export default service

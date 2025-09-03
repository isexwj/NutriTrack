import axios from 'axios'
import { useUserStore } from '@/store/user'

const service = axios.create({
  baseURL: 'http://localhost:8080', // 你的后端地址
  timeout: 5000
})

// 请求拦截器
service.interceptors.request.use(config => {
  const userStore = useUserStore()
    const token = userStore.token || localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
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

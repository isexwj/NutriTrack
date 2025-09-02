import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import ForgetPassword from '@/views/ForgetPassword.vue'

const routes = [
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/forgot', component: ForgetPassword },
  {
    path: '/home',
    component: () => import('@/views/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/my-meals',
    component: () => import('@/views/MyMeals.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/community',
    component: () => import('@/views/Community.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/ai-analysis',
    component: () => import('@/views/AIAnalysis.vue'),
    meta: { requiresAuth: true }
  },
  { path: '/', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫，检查 token
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  console.log('路由守卫检查:', { to: to.path, from: from.path, token: !!token })
  
  // 如果访问需要认证的页面但没有token，跳转到登录页
  if (to.meta.requiresAuth && !token) {
    console.log('需要认证但无token，跳转到登录页')
    next('/login')
  } 
  // 如果已经登录但访问登录页，跳转到首页
  else if (token && (to.path === '/login' || to.path === '/register' || to.path === '/forgot')) {
    console.log('已登录但访问登录页，跳转到首页')
    next('/home')
  } 
  // 如果访问根路径，根据登录状态跳转
  else if (to.path === '/') {
    console.log('访问根路径，根据登录状态跳转')
    next(token ? '/home' : '/login')
  }
  else {
    console.log('正常访问，允许通过')
    next()
  }
})

export default router

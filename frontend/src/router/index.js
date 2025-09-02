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
  { path: '/', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫，检查 token
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router

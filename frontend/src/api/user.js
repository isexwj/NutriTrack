// api/user.js
import request from '@/utils/request'

export function login(data) {
  return request.post('/user/login', data)
}

export function getCaptcha(scene = 'login') {
  return request.get('/user/captcha', { params: { scene } })
}

export function register(data) {
  return request.post('/user/register', data)
}

// 新增：注册两步
export function registerStep1(data) {
  return request.post('/user/register/step1', data)
}

export function registerEmailSend(data) {
  return request.post('/user/register/email/send', data)
}

export function registerStep2(data) {
  return request.post('/user/register/step2', data)
}

export function getUserInfo(username) {
  return request.get(`/user/${username}`)
}

export function updateUserInfo(username, data) {
  return request.put(`/user/${username}`, data)
}

export function deactivateAccount(username) {
  return request.delete(`/user/${username}`)
}

export function forgetPassword(data) {
  return request.post('/user/forget-password', data)
}

// 新增：忘记密码（邮箱验证码）
export function forgetPasswordSend(data) {
  return request.post('/user/forget-password/send', data)
}

export function forgetPasswordReset(data) {
  return request.post('/user/forget-password/reset', data)
}

// 通知相关
export function listNotifications(username) {
  return request.get('/notifications', { params: { username } })
}

export function markNotificationRead(id) {
  return request.post(`/notifications/${id}/read`)
}

export function markAllNotificationsRead(username) {
  return request.post('/notifications/read-all', null, { params: { username } })
}

import request from '@/utils/request'

export function login(data) {
  return request.post('/user/login', data)
}

export function register(data) {
  return request.post('/user/register', data)
}

export function getUserInfo(username) {
  return request.get(`/user/${username}`)
}

export function forgetPassword(data) {
  return request.post('/user/forget-password', data)
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

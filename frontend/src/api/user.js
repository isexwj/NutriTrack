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

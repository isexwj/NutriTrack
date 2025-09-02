import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || ''
  }),
  actions: {
    setUser(token, username) {
      this.token = token
      this.username = username
      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
    },
    clearUser() {
      this.token = ''
      this.username = ''
      localStorage.removeItem('token')
      localStorage.removeItem('username')
    }
  }
})

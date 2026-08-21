import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('admin_userInfo') || '{}'))

  const isLoggedIn = computed(() => !!token.value)

  function setUser(data) {
    token.value = data.token
    userInfo.value = { userId: data.userId, nickname: data.nickname, userType: data.userType }
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_userInfo', JSON.stringify(userInfo.value))
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_userInfo')
  }

  return { token, userInfo, isLoggedIn, setUser, logout }
})

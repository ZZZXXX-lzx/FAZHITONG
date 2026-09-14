import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const permCodes = ref(JSON.parse(localStorage.getItem('permCodes') || '[]'))

  const isLoggedIn = computed(() => !!token.value)
  const userType = computed(() => userInfo.value?.userType || '')

  function setUser(data) {
    token.value = data.token
    userInfo.value = { userId: data.userId, nickname: data.nickname, userType: data.userType }
    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    loadPerms()
  }

  async function loadPerms() {
    if (!token.value) {
      permCodes.value = []
      localStorage.setItem('permCodes', '[]')
      return
    }
    try {
      const codes = await userApi.perms()
      permCodes.value = Array.isArray(codes) ? codes : []
      localStorage.setItem('permCodes', JSON.stringify(permCodes.value))
    } catch {
      /* 拉取失败保持现状，菜单按角色兜底 */
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    permCodes.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('permCodes')
  }

  return { token, userInfo, isLoggedIn, userType, permCodes, setUser, logout, loadPerms }
})

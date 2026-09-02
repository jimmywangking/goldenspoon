import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/utils/request'
import type { UserInfo, PagePermission } from '@/types'

interface LoginRequest {
  username: string
  password: string
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const refreshToken = ref<string>(localStorage.getItem('refreshToken') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const isOrgAdmin = computed(() => userInfo.value?.role === 'ORG_ADMIN')

  function canAccess(pageCode: string): boolean {
    if (!userInfo.value || userInfo.value.role === 'ADMIN' || userInfo.value.role === 'ORG_ADMIN') return true
    return userInfo.value.permissions.some(p => p.pageCode === pageCode && p.canView)
  }

  async function login(data: LoginRequest) {
    const res = await axios.post('/api/auth/login', data)
    token.value = res.data.token
    refreshToken.value = res.data.refreshToken
    userInfo.value = res.data.user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('refreshToken', res.data.refreshToken)
    return res.data
  }

  async function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  async function fetchCurrentUser() {
    const res = await axios.get('/api/auth/me')
    userInfo.value = res.data
  }

  function setUserInfo(user: UserInfo) {
    userInfo.value = user
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    isAdmin,
    isOrgAdmin,
    canAccess,
    login,
    logout,
    fetchCurrentUser,
    setUserInfo
  }
})

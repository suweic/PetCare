import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, setAdmin, removeAdmin, getAdmin } from '@/utils/auth'
import { adminLogin, getAdminInfo } from '@/api/auth'
import type { AdminInfo } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const admin = ref<AdminInfo | null>(getAdmin())
  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const { data } = await adminLogin({ username, password })
    if (data.code === 200) {
      token.value = data.data.token
      admin.value = data.data.user
      setToken(data.data.token)
      setAdmin(data.data.user)
    }
    return data
  }

  async function refreshInfo() {
    const { data } = await getAdminInfo()
    if (data.code === 200) {
      admin.value = data.data
      setAdmin(data.data)
    }
    return data
  }

  function logout() {
    token.value = null
    admin.value = null
    removeToken()
    removeAdmin()
  }

  return { token, admin, isLoggedIn, login, refreshInfo, logout }
})

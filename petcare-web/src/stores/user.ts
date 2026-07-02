import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, setUser, removeUser, getUser } from '@/utils/auth'
import { loginByPhone, loginByCode, register, getUserInfo } from '@/api/auth'
import type { UserInfo } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const userInfo = ref<UserInfo | null>(getUser())
  const isLoggedIn = computed(() => !!token.value)

  /** 手机号密码登录 */
  async function login(phone: string, password: string) {
    const { data } = await loginByPhone({ phone, password })
    if (data.code === 200) {
      token.value = data.data.token
      userInfo.value = data.data.user
      setToken(data.data.token)
      setUser(data.data.user)
    }
    return data
  }

  /** 验证码登录 */
  async function loginWithCode(phone: string, code: string) {
    const { data } = await loginByCode({ phone, code })
    if (data.code === 200) {
      token.value = data.data.token
      userInfo.value = data.data.user
      setToken(data.data.token)
      setUser(data.data.user)
    }
    return data
  }

  /** 注册 */
  async function doRegister(phone: string, password: string, code: string) {
    const { data } = await register({ phone, password, code })
    return data
  }

  /** 刷新用户信息 */
  async function refreshUserInfo() {
    const { data } = await getUserInfo()
    if (data.code === 200) {
      userInfo.value = data.data
      setUser(data.data)
    }
    return data
  }

  /** 退出登录 */
  function logout() {
    token.value = null
    userInfo.value = null
    removeToken()
    removeUser()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    loginWithCode,
    doRegister,
    refreshUserInfo,
    logout,
  }
})

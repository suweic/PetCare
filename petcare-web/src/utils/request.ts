import axios, { type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '@/utils/auth'
import router from '@/router'
import type { ApiResult } from '@/types'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

/** 请求拦截器 — 附加 Bearer token */
request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

/** 响应拦截器 — 统一处理 code!==200 和 401 */
request.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const { data } = response
    if (data.code === 200) return response
    // 401 跳登录
    if (data.code === 401) {
      clearAuth()
      router.replace('/login')
      return Promise.reject(new Error(data.message || '未登录'))
    }
    ElMessage.error(data.message || '请求失败')
    return Promise.reject(new Error(data.message || '请求失败'))
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuth()
      router.replace('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  },
)

export default request

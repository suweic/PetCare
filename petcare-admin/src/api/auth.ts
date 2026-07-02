import request from '@/utils/request'
import type { ApiResult, LoginResult, AdminInfo } from '@/types'

/** 管理员登录 */
export function adminLogin(data: { username: string; password: string }) {
  return request.post<ApiResult<LoginResult>>('/login', data)
}

/** 获取当前管理员信息 */
export function getAdminInfo() {
  return request.get<ApiResult<AdminInfo>>('/me')
}

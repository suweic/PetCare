import request from '@/utils/request'
import type { ApiResult, UserInfo, PageData } from '@/types'

/** 用户列表 */
export function getUserList(params?: {
  keyword?: string
  status?: number
  page?: number
  size?: number
}) {
  return request.get<ApiResult<PageData<UserInfo>>>('/user/list', { params })
}

/** 启用/禁用用户 */
export function toggleUserStatus(id: number, status: number) {
  return request.put<ApiResult<null>>(`/user/${id}/status`, { status })
}

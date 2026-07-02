import request from '@/utils/request'
import type { ApiResult, DashboardStats } from '@/types'

/** 获取仪表盘统计数据 */
export function getDashboardStats() {
  return request.get<ApiResult<DashboardStats>>('/dashboard/stats')
}

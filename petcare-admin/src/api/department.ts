import request from '@/utils/request'
import type { ApiResult } from '@/types'

export interface Department {
  id: number
  name: string
  description: string
  icon: string
  sort: number
}

/** 获取所有启用科室列表（按sort排序） */
export function getDepartments() {
  return request.get<ApiResult<Department[]>>('/department/list')
}

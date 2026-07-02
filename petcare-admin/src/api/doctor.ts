import request from '@/utils/request'
import type { ApiResult, Doctor, PageData } from '@/types'

/** 待审核医生列表 */
export function getPendingDoctors(params?: { page?: number; size?: number }) {
  return request.get<ApiResult<PageData<Doctor>>>('/doctor/pending', { params })
}

/** 医生列表 */
export function getDoctorList(params?: {
  keyword?: string
  status?: number
  deptId?: number
  page?: number
  size?: number
}) {
  return request.get<ApiResult<PageData<Doctor>>>('/doctor/list', { params })
}

/** 审核医生 */
export function auditDoctor(id: number, data: { auditStatus: number; auditComment?: string }) {
  return request.post<ApiResult<null>>(`/doctor/${id}/audit`, data)
}

/** 启用/禁用医生 */
export function toggleDoctorStatus(id: number, status: number) {
  return request.put<ApiResult<null>>(`/doctor/${id}/status`, { status })
}

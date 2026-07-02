import request from '@/utils/request'
import type { ApiResult, DoctorDetail, DoctorListItem, EvaluationDTO, PageData } from '@/types'

/** 医生列表 */
export function getDoctorList(params?: {
  deptId?: number
  keyword?: string
  page?: number
  size?: number
}) {
  return request.get<ApiResult<PageData<DoctorListItem>>>('/doctor/list', { params })
}

/** 医生详情 */
export function getDoctorDetail(id: number) {
  return request.get<ApiResult<DoctorDetail>>(`/doctor/${id}`)
}

/** 医生排班 */
export function getDoctorSchedules(id: number, params?: { page?: number; size?: number }) {
  return request.get<ApiResult<PageData<any>>>(`/doctor/${id}/schedules`, { params })
}

/** 医生评价列表 */
export function getDoctorEvaluations(
  id: number,
  params?: { page?: number; size?: number },
) {
  return request.get<ApiResult<PageData<EvaluationDTO>>>(`/doctor/${id}/evaluations`, { params })
}

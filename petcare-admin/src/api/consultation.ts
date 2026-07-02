import request from '@/utils/request'
import type { ApiResult, Consultation, PageData } from '@/types'

/** 问诊列表 */
export function getConsultationList(params?: {
  status?: number
  type?: number
  keyword?: string
  page?: number
  size?: number
}) {
  return request.get<ApiResult<PageData<Consultation>>>('/consultation/list', { params })
}

/** 问诊详情 */
export function getConsultationDetail(id: number) {
  return request.get<ApiResult<Consultation>>(`/consultation/${id}`)
}

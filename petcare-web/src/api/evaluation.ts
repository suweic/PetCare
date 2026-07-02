import request from '@/utils/request'
import type { ApiResult, EvaluationDTO } from '@/types'

/** 创建评价 */
export function createEvaluation(data: {
  consultationId: number
  rating: number
  content?: string
  isAnonymous?: number
}) {
  return request.post<ApiResult<EvaluationDTO>>('/evaluation/create', data)
}

/** 医生回复评价 */
export function replyEvaluation(id: number, reply: string) {
  return request.post<ApiResult<EvaluationDTO>>(`/evaluation/${id}/reply`, { reply })
}

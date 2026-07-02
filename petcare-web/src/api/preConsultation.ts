import request from '@/utils/request'
import type { ApiResult, PreConsultationRequest, PreConsultationResult } from '@/types'

/** AI预问诊分析：提交症状，获取科室和医生推荐 */
export function analyzeSymptoms(data: PreConsultationRequest) {
  return request.post<ApiResult<PreConsultationResult>>('/pre-consultation/analyze', data)
}

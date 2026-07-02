import request from '@/utils/request'
import type {
  ApiResult,
  Consultation,
  ConsultationMessage,
  PageData,
  PrescriptionDTO,
} from '@/types'

/** 创建问诊 */
export function createConsultation(data: {
  petId: number
  departmentId?: number
  type: number
  chiefComplaint?: string
  symptoms?: string
  imageUrls?: string[]
}) {
  return request.post<ApiResult<Consultation>>('/consultation/create', data)
}

/** 问诊列表 */
export function getConsultationList(params?: { status?: number; page?: number; size?: number }) {
  return request.get<ApiResult<PageData<Consultation>>>('/consultation/list', { params })
}

/** 问诊详情 */
export function getConsultationDetail(id: number) {
  return request.get<ApiResult<Consultation>>(`/consultation/${id}`)
}

/** 问诊消息记录 */
export function getConsultationMessages(id: number) {
  return request.get<ApiResult<ConsultationMessage[]>>(`/consultation/${id}/messages`)
}

/** 取消问诊 */
export function cancelConsultation(id: number) {
  return request.put<ApiResult<null>>(`/consultation/${id}/cancel`)
}

/** 获取问诊处方 */
export function getPrescription(consultationId: number) {
  return request.get<ApiResult<PrescriptionDTO>>(`/prescription/${consultationId}`)
}

/** 上传聊天图片 */
export function uploadChatImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResult<{ url: string }>>('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 医生开具处方 */
export function createPrescription(data: {
  consultationId: number
  diagnosis?: string
  advice?: string
  items: {
    medicineId?: number
    medicineName: string
    specification?: string
    dosage: string
    frequency: string
    duration: string
    quantity?: number
    remarks?: string
  }[]
}) {
  return request.post<ApiResult<PrescriptionDTO>>('/doctor/prescription/create', data)
}

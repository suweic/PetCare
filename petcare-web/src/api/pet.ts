import request from '@/utils/request'
import type { ApiResult, PageData, Pet } from '@/types'

/** 获取宠物列表 */
export function getPetList(params?: { page?: number; size?: number }) {
  return request.get<ApiResult<PageData<Pet>>>('/pet/list', { params })
}

/** 获取宠物详情 */
export function getPetDetail(id: number) {
  return request.get<ApiResult<Pet>>(`/pet/${id}`)
}

/** 创建宠物 */
export function createPet(data: {
  name: string
  species: number
  breed?: string
  gender?: number
  birthday?: string
  weight?: number
  avatar?: string
  medicalHistory?: string
  allergyInfo?: string
  sterilized?: number
}) {
  return request.post<ApiResult<Pet>>('/pet/create', data)
}

/** 更新宠物 */
export function updatePet(id: number, data: Record<string, any>) {
  return request.put<ApiResult<Pet>>(`/pet/${id}`, data)
}

/** 删除宠物 */
export function deletePet(id: number) {
  return request.delete<ApiResult<null>>(`/pet/${id}`)
}

/** 上传宠物头像 */
export function uploadPetAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResult<{ url: string }>>('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 统一响应格式 */
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}

/** 分页数据 */
export interface PageData<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/** 用户信息 */
export interface UserInfo {
  id: number
  phone: string
  nickname: string
  avatar: string
  realName: string
  userType: number
}

/** 登录结果 */
export interface LoginResult {
  token: string
  user: UserInfo
}

/** 宠物 */
export interface Pet {
  id: number
  userId: number
  name: string
  species: number
  speciesName: string
  breed: string
  gender: number
  genderName: string
  birthDate: string
  weight: number
  avatar: string
  medicalHistory: string
  allergyInfo: string
  sterilized: number
}

/** 医生列表项 */
export interface DoctorListItem {
  id: number
  realName: string
  avatar: string
  title: string
  departmentId: number
  departmentName: string
  specialty: string
  experience: number
  hospital: string
  rating: number
  consultationCount: number
  consultationFee: number
}

/** 医生详情 */
export interface DoctorDetail {
  id: number
  userId: number
  realName: string
  avatar: string
  phone: string
  departmentId: number
  departmentName: string
  title: string
  specialty: string
  experience: number
  education: string
  hospital: string
  introduction: string
  consultationFee: number
  rating: number
  consultationCount: number
  recentEvaluations: EvaluationDTO[]
}

/** 问诊 */
export interface Consultation {
  id: number
  userId: number
  doctorId: number
  petId: number
  departmentId: number
  type: number
  status: number
  chiefComplaint: string
  symptoms: string
  scheduledTime: string
  startTime: string
  endTime: string
  createTime: string
}

/** 问诊消息 */
export interface ConsultationMessage {
  id: number
  consultationId: number
  senderType: number
  senderId: number
  messageType: number
  content: string
  mediaUrl: string
  duration: number
  isRead: number
  createTime: string
}

/** 处方 */
export interface PrescriptionDTO {
  id: number
  consultationId: number
  userId: number
  doctorId: number
  petId: number
  diagnosis: string
  advice: string
  status: number
  createTime: string
  items: PrescriptionItemDTO[]
}

export interface PrescriptionItemDTO {
  medicineId: number
  medicineName: string
  specification: string
  dosage: string
  frequency: string
  duration: string
  quantity: number
  remarks: string
}

/** AI预问诊请求 */
export interface PreConsultationRequest {
  petId?: number
  species?: number
  breed?: string
  ageYears?: number
  ageMonths?: number
  symptoms: string
  symptomDuration?: string
  additionalInfo?: string
}

/** AI推荐医生 */
export interface RecommendedDoctor {
  doctorId: number
  doctorName: string
  title: string
  hospital: string
  specialty: string
  rating: number
  consultationFee: number
  matchReason: string
}

/** AI预问诊结果 */
export interface PreConsultationResult {
  preConsultationId: number
  departmentId: number
  departmentName: string
  departmentDescription: string
  aiAnalysis: string
  recommendedDoctors: RecommendedDoctor[]
  generalAdvice: string
  llmModel: string
}

/** 评价 */
export interface EvaluationDTO {
  id: number
  consultationId: number
  userId: number
  userName: string
  userAvatar: string
  doctorId: number
  rating: number
  content: string
  isAnonymous: number
  reply: string
  replyTime: string
  status: number
  createTime: string
}

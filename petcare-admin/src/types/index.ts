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

/** 管理员信息 */
export interface AdminInfo {
  id: number
  username: string
  realName: string
  phone: string
  email: string
  roleId: number
  roleName: string
  avatar: string
}

/** 登录结果（对应后端 LoginResultDTO，user 字段为 UserInfoDTO） */
export interface LoginResult {
  token: string
  user: AdminInfo
}

/** 仪表盘统计 */
export interface DashboardStats {
  totalUsers: number
  totalDoctors: number
  totalConsultations: number
  todayConsultations: number
  todayNewUsers: number
  pendingDoctors: number
  revenue: number
  trendData: TrendPoint[]
  deptDistribution: DeptStat[]
}

export interface TrendPoint {
  date: string
  count: number
}

export interface DeptStat {
  name: string
  value: number
}

/** 医生 */
export interface Doctor {
  id: number
  userId: number
  realName: string
  phone: string
  departmentName: string
  title: string
  specialty: string
  education: string
  experience: number
  hospital: string
  rating: number
  consultationCount: number
  status: number
  createTime: string
}

/** 用户 */
export interface UserInfo {
  id: number
  phone: string
  nickname: string
  realName: string
  userType: number
  status: number
  createTime: string
}

/** 问诊 */
export interface Consultation {
  id: number
  userId: number
  userName: string
  doctorId: number
  doctorName: string
  petId: number
  petName: string
  departmentName: string
  type: number
  status: number
  chiefComplaint: string
  symptoms: string
  createTime: string
}

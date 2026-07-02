import request from '@/utils/request'
import type { ApiResult, LoginResult, UserInfo } from '@/types'

/** 用户手机号密码登录 */
export function loginByPhone(data: { phone: string; password: string }) {
  return request.post<ApiResult<LoginResult>>('/user/login', data)
}

/** 发送验证码 */
export function sendCode(phone: string) {
  return request.post<ApiResult<null>>('/user/send-code', { phone })
}

/** 手机号验证码登录 */
export function loginByCode(data: { phone: string; code: string }) {
  return request.post<ApiResult<LoginResult>>('/user/login-by-code', data)
}

/** 用户注册 */
export function register(data: { phone: string; password: string; code: string }) {
  return request.post<ApiResult<string>>('/user/register', data)
}

/** 获取当前用户信息 */
export function getUserInfo() {
  return request.get<ApiResult<UserInfo>>('/user/me')
}

/** 医生登录 */
export function doctorLogin(data: { phone: string; password: string }) {
  return request.post<ApiResult<LoginResult>>('/doctor/login', data)
}

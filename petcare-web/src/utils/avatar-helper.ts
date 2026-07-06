/**
 * 头像辅助组合函数（模板内可用）
 *  - avatarOf(url, isDoctor?)：安全归一化头像 URL，避免 petcare.com 域名占位
 *  - onAvatarError(e)         ：<el-avatar @error> 兜底处理，阻止再次请求失败资源
 *
 * 实现基于 ./avatar.ts。
 */
import { safeAvatar, DEFAULT_AVATAR, DEFAULT_DOCTOR_AVATAR } from './avatar'

export function avatarOf(
  raw: string | null | undefined,
  isDoctor = false,
): string {
  return safeAvatar(raw, isDoctor ? DEFAULT_DOCTOR_AVATAR : DEFAULT_AVATAR)
}

/**
 * el-avatar @error 兜底：直接把 <img>.src 替换为内联默认头像，
 * 避免浏览器反复重试无法解析的 URL。
 */
export function onAvatarError(e: Event): void {
  const img = e.target as HTMLImageElement | null
  if (img) img.src = DEFAULT_AVATAR
}

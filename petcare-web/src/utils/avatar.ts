/**
 * 头像 URL 安全处理
 *
 * 后端 mock 数据中硬编码了 https://petcare.com/avatar/... 占位域名，
 * 本地开发环境无法解析，会导致浏览器反复请求失败并污染控制台。
 * 这里统一在赋值给 <el-avatar :src> 前完成替换，并在 @error 事件上兜底。
 */

/** 内联默认头像：浅绿底 + 用户剪影（data URI） */
export const DEFAULT_AVATAR =
  "data:image/svg+xml;utf8," +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64">` +
      `<rect width="64" height="64" rx="32" fill="#e8f5e9"/>` +
      `<circle cx="32" cy="24" r="11" fill="#a5d6a7"/>` +
      `<path d="M10 58c0-12 10-20 22-20s22 8 22 20" fill="#a5d6a7"/>` +
    `</svg>`,
  )

/** 医生默认头像：浅蓝底 + 听诊器剪影 */
export const DEFAULT_DOCTOR_AVATAR =
  "data:image/svg+xml;utf8," +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64">` +
      `<rect width="64" height="64" rx="32" fill="#e3f2fd"/>` +
      `<circle cx="32" cy="22" r="10" fill="#64b5f6"/>` +
      `<path d="M12 58c0-11 9-18 20-18s20 7 20 18" fill="#64b5f6"/>` +
      `<rect x="42" y="30" width="3" height="14" rx="1.5" fill="#1976d2"/>` +
      `<circle cx="43.5" cy="46" r="3" fill="#1976d2"/>` +
    `</svg>`,
  )

/** 非法占位域名（mock 数据硬编码）— 一律替换为默认头像 */
const PLACEHOLDER_HOST_RE = /^(https?:)?\/\/(www\.)?petcare\.com\//i

/**
 * 把任意来源的 URL 规范化为「可安全显示」的值
 * - 空 / null / undefined / 非法占位 URL → 默认头像
 * - 真实 http(s) URL 原样返回
 * - 已经是 data: URI 原样返回
 */
export function safeAvatar(
  raw: string | null | undefined,
  fallback: string = DEFAULT_AVATAR,
): string {
  if (!raw) return fallback
  const v = String(raw).trim()
  if (!v) return fallback
  if (v.startsWith('data:')) return v
  if (PLACEHOLDER_HOST_RE.test(v)) return fallback
  // 其它非 http(s) 协议也兜底
  if (!/^https?:\/\//i.test(v)) return fallback
  return v
}

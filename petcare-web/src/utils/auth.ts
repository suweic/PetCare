/**
 * 认证工具模块。
 *
 * ⚠️ 安全注意事项：
 * 当前 Token 存储在 localStorage 中。localStorage 可被同源 JavaScript 读取，
 * 因此任何成功的 XSS 攻击都能窃取 Token。生产环境建议迁移到以下方案之一：
 *
 *   方案A (推荐): HttpOnly Secure SameSite Cookie
 *     - 后端在登录响应中 Set-Cookie: token=xxx; HttpOnly; Secure; SameSite=Strict
 *     - 前端自动携带，JavaScript 无法读取
 *     - 需要同步启用 CSRF Token 防护
 *
 *   方案B (渐进): Token + Fingerprint
 *     - Token 中嵌入浏览器指纹/IP Hash
 *     - 后端验证 fingerprint，即使 Token 泄露也难以在其他环境使用
 *
 *   方案C (短期): 短期 Token + Refresh Token
 *     - Access Token 有效期 15 分钟
 *     - Refresh Token 存储在 HttpOnly Cookie 中
 *     - 即使 Access Token 泄露，攻击窗口也只有 15 分钟
 */

const TOKEN_KEY = 'petcare_token'
const USER_KEY = 'petcare_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUser(): any | null {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    removeUser()
    return null
  }
}

export function setUser(user: any): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function removeUser(): void {
  localStorage.removeItem(USER_KEY)
}

export function clearAuth(): void {
  removeToken()
  removeUser()
}

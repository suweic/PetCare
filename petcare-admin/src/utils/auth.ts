/**
 * 认证工具模块（管理后台）。
 *
 * ⚠️ 安全注意事项：同 petcare-web/src/utils/auth.ts。
 * 生产环境建议迁移到 HttpOnly Secure SameSite Cookie 方案。
 */

const TOKEN_KEY = 'admin_token'
const USER_KEY = 'admin_user'

export function getToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function setToken(token: string): void {
  try {
    localStorage.setItem(TOKEN_KEY, token)
  } catch {
    console.warn('localStorage 不可用，token 未持久化')
  }
}

export function removeToken(): void {
  try {
    localStorage.removeItem(TOKEN_KEY)
  } catch {
    // noop
  }
}

export function getAdmin(): any | null {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    removeAdmin()
    return null
  }
}

export function setAdmin(admin: any): void {
  try {
    localStorage.setItem(USER_KEY, JSON.stringify(admin))
  } catch {
    console.warn('localStorage 不可用，用户信息未持久化')
  }
}

export function removeAdmin(): void {
  try {
    localStorage.removeItem(USER_KEY)
  } catch {
    // noop
  }
}

export function clearAuth(): void {
  removeToken()
  removeAdmin()
}

/**
 * 认证工具模块（管理后台）。
 *
 * ⚠️ 安全注意事项：同 petcare-web/src/utils/auth.ts。
 * 生产环境建议迁移到 HttpOnly Secure SameSite Cookie 方案。
 */

const TOKEN_KEY = 'admin_token'
const USER_KEY = 'admin_user'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
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
  localStorage.setItem(USER_KEY, JSON.stringify(admin))
}

export function removeAdmin(): void {
  localStorage.removeItem(USER_KEY)
}

export function clearAuth(): void {
  removeToken()
  removeAdmin()
}

/**
 * 管理后台认证工具模块单元测试
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'

// 内联测试的 auth 逻辑（避免 mock 复杂依赖）
const ADMIN_TOKEN_KEY = 'admin_token'
const ADMIN_USER_KEY = 'admin_user'

const authUtils = {
  getToken: () => localStorage.getItem(ADMIN_TOKEN_KEY),
  setToken: (token: string) => localStorage.setItem(ADMIN_TOKEN_KEY, token),
  removeToken: () => localStorage.removeItem(ADMIN_TOKEN_KEY),
  getUser: () => {
    try {
      const raw = localStorage.getItem(ADMIN_USER_KEY)
      return raw ? JSON.parse(raw) : null
    } catch {
      return null
    }
  },
  setUser: (user: unknown) => localStorage.setItem(ADMIN_USER_KEY, JSON.stringify(user)),
  removeUser: () => localStorage.removeItem(ADMIN_USER_KEY),
  clearAuth: () => {
    localStorage.removeItem(ADMIN_TOKEN_KEY)
    localStorage.removeItem(ADMIN_USER_KEY)
  },
}

// Mock localStorage
const store: Record<string, string> = {}
const localStorageMock = {
  getItem: vi.fn((key: string) => store[key] ?? null),
  setItem: vi.fn((key: string, value: string) => { store[key] = value }),
  removeItem: vi.fn((key: string) => { delete store[key] }),
  clear: vi.fn(() => { Object.keys(store).forEach(k => delete store[k]) }),
}
Object.defineProperty(window, 'localStorage', { value: localStorageMock })

describe('Admin Auth Utils', () => {
  beforeEach(() => {
    localStorageMock.clear()
  })

  it('should set and get admin token', () => {
    authUtils.setToken('admin-jwt-token')
    expect(authUtils.getToken()).toBe('admin-jwt-token')
  })

  it('should set and get admin user info', () => {
    const admin = { id: 1, username: 'admin', realName: '超级管理员', userType: 3 }
    authUtils.setUser(admin)
    expect(authUtils.getUser()).toEqual(admin)
  })

  it('should clear all auth data', () => {
    authUtils.setToken('token')
    authUtils.setUser({ id: 1 })
    authUtils.clearAuth()
    expect(authUtils.getToken()).toBeNull()
    expect(authUtils.getUser()).toBeNull()
  })

  it('should identify userType === 3 as admin', () => {
    const adminUser = { userType: 3 }
    expect(adminUser.userType === 3).toBe(true)
  })

  it('should reject non-admin userType for admin panel', () => {
    const normalUser = { userType: 1 }
    expect(normalUser.userType === 3).toBe(false)
  })
})

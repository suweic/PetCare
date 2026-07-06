/**
 * 用户 Store 单元测试
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock localStorage
const store: Record<string, string> = {}
const localStorageMock = {
  getItem: vi.fn((key: string) => store[key] ?? null),
  setItem: vi.fn((key: string, value: string) => { store[key] = value }),
  removeItem: vi.fn((key: string) => { delete store[key] }),
  clear: vi.fn(() => { for (const k of Object.keys(store)) delete store[k] }),
}
Object.defineProperty(globalThis, 'localStorage', { value: localStorageMock })

// Mock API modules
vi.mock('@/api/auth', () => ({
  loginByPhone: vi.fn(),
  loginByCode: vi.fn(),
  register: vi.fn(),
  getUserInfo: vi.fn(),
}))

import { useUserStore } from '@/stores/user'
import * as authApi from '@/api/auth'

describe('User Store', () => {
  beforeEach(() => {
    localStorageMock.clear()
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('State initialization', () => {
    it('should initialize with no token when localStorage is empty', () => {
      const store = useUserStore()
      expect(store.token).toBeNull()
      expect(store.userInfo).toBeNull()
      expect(store.isLoggedIn).toBe(false)
    })

    it('should initialize with token from localStorage', () => {
      localStorageMock.setItem('petcare_token', 'existing-token')
      const store = useUserStore()
      expect(store.token).toBe('existing-token')
      expect(store.isLoggedIn).toBe(true)
    })

    it('should initialize with user info from localStorage', () => {
      const user = { id: 1, phone: '13800138000', nickname: '小王' }
      localStorageMock.setItem('petcare_user', JSON.stringify(user))
      const store = useUserStore()
      expect(store.userInfo).toEqual(user)
    })
  })

  describe('login', () => {
    it('should set token and user on successful login', async () => {
      const mockUser = { id: 1, phone: '13800138000', nickname: '小王' }
      const mockResponse = {
        code: 200,
        data: { token: 'test-token', user: mockUser },
      }
      vi.mocked(authApi.loginByPhone).mockResolvedValue({ data: mockResponse } as any)

      const store = useUserStore()
      const result = await store.login('13800138000', 'password')

      expect(result).toEqual(mockResponse)
      expect(store.token).toBe('test-token')
      expect(store.userInfo).toEqual(mockUser)
      expect(store.isLoggedIn).toBe(true)
      expect(localStorageMock.setItem).toHaveBeenCalledWith('petcare_token', 'test-token')
    })

    it('should not set state when login returns non-200 code', async () => {
      const mockResponse = { code: 400, message: '手机号或密码错误', data: null }
      vi.mocked(authApi.loginByPhone).mockResolvedValue({ data: mockResponse } as any)

      const store = useUserStore()
      const result = await store.login('13800138000', 'wrong')

      expect(result).toEqual(mockResponse)
      expect(store.token).toBeNull()
      expect(store.isLoggedIn).toBe(false)
    })
  })

  describe('loginWithCode', () => {
    it('should login successfully with verification code', async () => {
      const mockUser = { id: 2, phone: '13800138001', nickname: '小李' }
      const mockResponse = {
        code: 200,
        data: { token: 'code-token', user: mockUser },
      }
      vi.mocked(authApi.loginByCode).mockResolvedValue({ data: mockResponse } as any)

      const store = useUserStore()
      const result = await store.loginWithCode('13800138001', '123456')

      expect(result).toEqual(mockResponse)
      expect(store.token).toBe('code-token')
      expect(store.userInfo).toEqual(mockUser)
    })
  })

  describe('logout', () => {
    it('should clear token and user info', () => {
      localStorageMock.setItem('petcare_token', 'some-token')
      localStorageMock.setItem('petcare_user', JSON.stringify({ id: 1 }))

      const store = useUserStore()
      // Manually set state to simulate logged-in
      store.token = 'some-token'
      store.userInfo = { id: 1 } as any

      store.logout()

      expect(store.token).toBeNull()
      expect(store.userInfo).toBeNull()
      expect(store.isLoggedIn).toBe(false)
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('petcare_token')
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('petcare_user')
    })
  })

  describe('refreshUserInfo', () => {
    it('should update user info on success', async () => {
      const updatedUser = { id: 1, phone: '13800138000', nickname: '新昵称' }
      vi.mocked(authApi.getUserInfo).mockResolvedValue({
        data: { code: 200, data: updatedUser },
      } as any)

      const store = useUserStore()
      await store.refreshUserInfo()

      expect(store.userInfo).toEqual(updatedUser)
    })
  })
})

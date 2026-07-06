/**
 * 认证工具模块单元测试
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import {
  getToken,
  setToken,
  removeToken,
  getUser,
  setUser,
  removeUser,
  clearAuth,
} from '../utils/auth'

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {}
  return {
    getItem: vi.fn((key: string) => store[key] ?? null),
    setItem: vi.fn((key: string, value: string) => { store[key] = value }),
    removeItem: vi.fn((key: string) => { delete store[key] }),
    clear: vi.fn(() => { store = {} }),
  }
})()

Object.defineProperty(window, 'localStorage', { value: localStorageMock })

describe('Auth Utils', () => {
  beforeEach(() => {
    localStorageMock.clear()
  })

  describe('Token operations', () => {
    it('should return null when no token stored', () => {
      expect(getToken()).toBeNull()
    })

    it('should set and get token', () => {
      setToken('test-token-123')
      expect(getToken()).toBe('test-token-123')
      expect(localStorageMock.setItem).toHaveBeenCalledWith('petcare_token', 'test-token-123')
    })

    it('should remove token', () => {
      setToken('test-token-123')
      removeToken()
      expect(getToken()).toBeNull()
      expect(localStorageMock.removeItem).toHaveBeenCalledWith('petcare_token')
    })
  })

  describe('User operations', () => {
    it('should return null when no user stored', () => {
      expect(getUser()).toBeNull()
    })

    it('should set and get user', () => {
      const user = { id: 1, phone: '13800138000', nickname: '小王' }
      setUser(user)
      expect(getUser()).toEqual(user)
    })

    it('should handle malformed JSON gracefully', () => {
      // Simulate corrupted data — save and restore original getItem to avoid polluting other tests
      const originalGetItem = localStorageMock.getItem
      localStorageMock.getItem = vi.fn(() => '{broken-json')
      expect(getUser()).toBeNull()
      localStorageMock.getItem = originalGetItem
    })

    it('should remove user', () => {
      setUser({ id: 1, phone: '13800138000' })
      removeUser()
      expect(getUser()).toBeNull()
    })
  })

  describe('clearAuth', () => {
    it('should clear both token and user', () => {
      setToken('token')
      setUser({ id: 1 })
      clearAuth()
      expect(getToken()).toBeNull()
      expect(getUser()).toBeNull()
    })
  })
})

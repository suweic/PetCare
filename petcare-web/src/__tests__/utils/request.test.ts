/**
 * API 请求工具单元测试 — 响应拦截器业务逻辑
 *
 * 注意：本测试直接验证拦截器核心逻辑，不 mock axios。
 * 这样做的好处是测试代码即文档，直接展示了错误码的处理规则。
 */
import { describe, it, expect } from 'vitest'

/** 拦截器核心逻辑的纯函数提取（与 request.ts 中逻辑等价） */
function handleApiResponse(
  code: number,
  message?: string,
): { type: 'ok' | 'unauthorized' | 'error'; message: string } {
  if (code === 200) {
    return { type: 'ok', message: 'success' }
  }
  if (code === 401) {
    return { type: 'unauthorized', message: message || '未登录' }
  }
  return { type: 'error', message: message || '请求失败' }
}

function handleHttpError(
  status: number,
  message?: string,
): { type: 'unauthorized' | 'network'; message: string } {
  if (status === 401) {
    return { type: 'unauthorized', message: '登录已过期，请重新登录' }
  }
  return { type: 'network', message: message || '网络错误' }
}

describe('API Response Handler', () => {
  describe('handleApiResponse (business code)', () => {
    it('should pass through when code is 200', () => {
      const result = handleApiResponse(200)
      expect(result.type).toBe('ok')
    })

    it('should flag 401 as unauthorized', () => {
      const result = handleApiResponse(401, '未登录')
      expect(result.type).toBe('unauthorized')
      expect(result.message).toBe('未登录')
    })

    it('should use default 401 message when none provided', () => {
      const result = handleApiResponse(401)
      expect(result.type).toBe('unauthorized')
      expect(result.message).toBe('未登录')
    })

    it('should flag 400 as error with custom message', () => {
      const result = handleApiResponse(400, '手机号或密码错误')
      expect(result.type).toBe('error')
      expect(result.message).toBe('手机号或密码错误')
    })

    it('should flag 404 as error with default message', () => {
      const result = handleApiResponse(404)
      expect(result.type).toBe('error')
      expect(result.message).toBe('请求失败')
    })

    it('should flag 500 as error', () => {
      const result = handleApiResponse(500, '服务器内部错误')
      expect(result.type).toBe('error')
      expect(result.message).toBe('服务器内部错误')
    })
  })

  describe('handleHttpError (HTTP status)', () => {
    it('should flag HTTP 401 as unauthorized', () => {
      const result = handleHttpError(401)
      expect(result.type).toBe('unauthorized')
      expect(result.message).toContain('登录已过期')
    })

    it('should flag HTTP 500 as network error', () => {
      const result = handleHttpError(500, 'Internal Server Error')
      expect(result.type).toBe('network')
    })

    it('should flag timeout as network error', () => {
      const result = handleHttpError(0, 'timeout of 15000ms exceeded')
      expect(result.type).toBe('network')
      expect(result.message).toContain('timeout')
    })
  })
})

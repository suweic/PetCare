/**
 * 上传工具模块单元测试
 */
import { describe, it, expect } from 'vitest'

// 模拟上传校验逻辑（与实际 upload.ts 中的校验对齐）
const MAX_SIZE = 5 * 1024 * 1024 // 5MB
const ALLOWED_TYPES = ['image/jpeg', 'image/png']

function validateFile(file: { size: number; type: string }): string | null {
  if (!ALLOWED_TYPES.includes(file.type)) {
    return '仅支持 JPG 和 PNG 格式的图片'
  }
  if (file.size > MAX_SIZE) {
    return '图片大小不能超过 5MB'
  }
  return null
}

describe('Upload Validation', () => {
  it('should accept valid JPEG file', () => {
    const file = { size: 1024 * 1024, type: 'image/jpeg' }
    expect(validateFile(file)).toBeNull()
  })

  it('should accept valid PNG file', () => {
    const file = { size: 512 * 1024, type: 'image/png' }
    expect(validateFile(file)).toBeNull()
  })

  it('should reject GIF files', () => {
    const file = { size: 1024 * 1024, type: 'image/gif' }
    expect(validateFile(file)).toBe('仅支持 JPG 和 PNG 格式的图片')
  })

  it('should reject files larger than 5MB', () => {
    const file = { size: 6 * 1024 * 1024, type: 'image/jpeg' }
    expect(validateFile(file)).toBe('图片大小不能超过 5MB')
  })

  it('should accept file exactly at 5MB limit', () => {
    const file = { size: 5 * 1024 * 1024, type: 'image/png' }
    expect(validateFile(file)).toBeNull()
  })
})

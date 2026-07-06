/**
 * 宠物信息校验工具单元测试
 *
 * 校验逻辑参考 PetServiceImpl 枚举定义：
 *   species: 1-猫 2-狗 3-其他
 *   gender:  1-公 2-母
 */
import { describe, it, expect } from 'vitest'

/** 物种枚举（与后端 PetServiceImpl 对齐） */
const VALID_SPECIES = [1, 2, 3] as const
const SPECIES_NAMES: Record<number, string> = { 1: '猫', 2: '狗', 3: '其他' }

const VALID_GENDERS = [1, 2] as const
const GENDER_NAMES: Record<number, string> = { 1: '公', 2: '母' }

function validateSpecies(species: number): { valid: boolean; name?: string } {
  if (VALID_SPECIES.includes(species as any)) {
    return { valid: true, name: SPECIES_NAMES[species] }
  }
  return { valid: false }
}

function validateGender(gender: number): { valid: boolean; name?: string } {
  if (VALID_GENDERS.includes(gender as any)) {
    return { valid: true, name: GENDER_NAMES[gender] }
  }
  return { valid: false }
}

function validatePetName(name: string): { valid: boolean; message?: string } {
  if (!name || name.trim().length === 0) {
    return { valid: false, message: '宠物名称不能为空' }
  }
  if (name.trim().length > 20) {
    return { valid: false, message: '宠物名称不能超过20个字符' }
  }
  return { valid: true }
}

describe('Pet Validation', () => {
  describe('validateSpecies', () => {
    it('should accept species 1 (cat)', () => {
      const result = validateSpecies(1)
      expect(result.valid).toBe(true)
      expect(result.name).toBe('猫')
    })

    it('should accept species 2 (dog)', () => {
      const result = validateSpecies(2)
      expect(result.valid).toBe(true)
      expect(result.name).toBe('狗')
    })

    it('should accept species 3 (other)', () => {
      const result = validateSpecies(3)
      expect(result.valid).toBe(true)
      expect(result.name).toBe('其他')
    })

    it('should reject species 0', () => {
      expect(validateSpecies(0).valid).toBe(false)
    })

    it('should reject species 99', () => {
      expect(validateSpecies(99).valid).toBe(false)
    })
  })

  describe('validateGender', () => {
    it('should accept gender 1 (male)', () => {
      const result = validateGender(1)
      expect(result.valid).toBe(true)
      expect(result.name).toBe('公')
    })

    it('should accept gender 2 (female)', () => {
      const result = validateGender(2)
      expect(result.valid).toBe(true)
      expect(result.name).toBe('母')
    })

    it('should reject gender 0', () => {
      expect(validateGender(0).valid).toBe(false)
    })

    it('should reject gender 99', () => {
      expect(validateGender(99).valid).toBe(false)
    })
  })

  describe('validatePetName', () => {
    it('should accept valid name', () => {
      expect(validatePetName('旺财').valid).toBe(true)
    })

    it('should reject empty name', () => {
      const result = validatePetName('')
      expect(result.valid).toBe(false)
      expect(result.message).toBe('宠物名称不能为空')
    })

    it('should reject whitespace-only name', () => {
      const result = validatePetName('   ')
      expect(result.valid).toBe(false)
    })

    it('should reject name exceeding 20 characters', () => {
      const longName = '这是一个超过了二十个字符限制的超长宠物名称测试数据'
      const result = validatePetName(longName)
      expect(result.valid).toBe(false)
      expect(result.message).toContain('20')
    })

    it('should accept name exactly at 20 characters', () => {
      const name = '一二三四五六七八九十一二三四五六七八九十' // 20 Chinese chars
      expect(validatePetName(name).valid).toBe(true)
    })
  })
})

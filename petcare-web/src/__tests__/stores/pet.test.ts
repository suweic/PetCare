/**
 * 宠物 Store 单元测试
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock API
vi.mock('@/api/pet', () => ({
  getPetList: vi.fn(),
  getPetDetail: vi.fn(),
  createPet: vi.fn(),
  updatePet: vi.fn(),
  deletePet: vi.fn(),
}))

import { usePetStore } from '@/stores/pet'
import * as petApi from '@/api/pet'
import type { Pet } from '@/types'

describe('Pet Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('fetchPets', () => {
    it('should populate pets and total on successful fetch', async () => {
      const mockPets: Pet[] = [
        {
          id: 1, userId: 1, name: '旺财', species: 2,
          speciesName: '狗', breed: '金毛', gender: 1,
          genderName: '公', birthDate: '2022-06-01', weight: 25.5,
          avatar: '', medicalHistory: '', allergyInfo: '', sterilized: 1,
        },
        {
          id: 2, userId: 1, name: '咪咪', species: 1,
          speciesName: '猫', breed: '英短', gender: 2,
          genderName: '母', birthDate: '2023-03-15', weight: 4.2,
          avatar: '', medicalHistory: '', allergyInfo: '', sterilized: 0,
        },
      ]

      vi.mocked(petApi.getPetList).mockResolvedValue({
        data: {
          code: 200,
          data: { records: mockPets, total: 2, size: 10, current: 1, pages: 1 },
        },
      } as any)

      const store = usePetStore()
      await store.fetchPets()

      expect(store.pets).toHaveLength(2)
      expect(store.total).toBe(2)
      expect(store.pets[0].name).toBe('旺财')
      expect(store.pets[1].name).toBe('咪咪')
    })

    it('should set loading to true during fetch', async () => {
      vi.mocked(petApi.getPetList).mockReturnValue(
        new Promise((resolve) => {
          setTimeout(() => resolve({
            data: { code: 200, data: { records: [], total: 0, size: 10, current: 1, pages: 0 } },
          } as any), 50)
        }),
      )

      const store = usePetStore()
      const promise = store.fetchPets()
      expect(store.loading).toBe(true)
      await promise
      expect(store.loading).toBe(false)
    })

    it('should keep empty list and set loading false on API error', async () => {
      vi.mocked(petApi.getPetList).mockRejectedValue(new Error('Network error'))

      const store = usePetStore()
      // fetchPets has try...finally but no catch, so error propagates
      await expect(store.fetchPets()).rejects.toThrow('Network error')

      // finally block still runs — loading should be reset, pets remain empty
      expect(store.pets).toHaveLength(0)
      expect(store.loading).toBe(false)
    })
  })

  describe('addPet', () => {
    it('should add pet to the beginning of list on success', async () => {
      const newPet: Pet = {
        id: 3, userId: 1, name: '雪球', species: 1,
        speciesName: '猫', breed: '布偶', gender: 1,
        genderName: '公', birthDate: '2023-08-20', weight: 5.8,
        avatar: '', medicalHistory: '', allergyInfo: '', sterilized: 0,
      }

      vi.mocked(petApi.createPet).mockResolvedValue({
        data: { code: 200, data: newPet },
      } as any)

      const store = usePetStore()
      store.pets = [{ id: 1 } as Pet, { id: 2 } as Pet]

      const result = await store.addPet({ name: '雪球', species: 1 })
      expect(result.code).toBe(200)
      expect(store.pets).toHaveLength(3)
      expect(store.pets[0].id).toBe(3) // new pet at front
    })
  })

  describe('removePet', () => {
    it('should remove pet from list on success', async () => {
      vi.mocked(petApi.deletePet).mockResolvedValue({
        data: { code: 200 },
      } as any)

      const store = usePetStore()
      store.pets = [
        { id: 1 } as Pet,
        { id: 2 } as Pet,
        { id: 3 } as Pet,
      ]

      const result = await store.removePet(2)
      expect(result.code).toBe(200)
      expect(store.pets).toHaveLength(2)
      expect(store.pets.find(p => p.id === 2)).toBeUndefined()
    })
  })
})

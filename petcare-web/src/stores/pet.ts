import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getPetList, getPetDetail, createPet, updatePet, deletePet } from '@/api/pet'
import type { Pet, PageData } from '@/types'

export const usePetStore = defineStore('pet', () => {
  const pets = ref<Pet[]>([])
  const total = ref(0)
  const loading = ref(false)

  /** 加载宠物列表 */
  async function fetchPets(page = 1, size = 10) {
    loading.value = true
    try {
      const { data } = await getPetList({ page, size })
      if (data.code === 200) {
        pets.value = data.data.records
        total.value = data.data.total
      }
    } finally {
      loading.value = false
    }
  }

  /** 添加宠物 */
  async function addPet(petData: Parameters<typeof createPet>[0]) {
    const { data } = await createPet(petData)
    if (data.code === 200) {
      pets.value.unshift(data.data)
    }
    return data
  }

  /** 编辑宠物 */
  async function editPet(id: number, petData: Record<string, any>) {
    const { data } = await updatePet(id, petData)
    if (data.code === 200) {
      const idx = pets.value.findIndex((p) => p.id === id)
      if (idx > -1) pets.value[idx] = data.data
    }
    return data
  }

  /** 删除宠物 */
  async function removePet(id: number) {
    const { data } = await deletePet(id)
    if (data.code === 200) {
      pets.value = pets.value.filter((p) => p.id !== id)
    }
    return data
  }

  return { pets, total, loading, fetchPets, addPet, editPet, removePet }
})

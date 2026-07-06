/**
 * 电子处方组件与工具函数单元测试
 *
 * 测试 Prescription.vue 的数据渲染逻辑和 formatDate 辅助函数。
 * 使用 @vue/test-utils 挂载组件并验证条件渲染的正确性。
 */
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'

// Mock router
vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({
    params: { consultationId: '1' },
  })),
  useRouter: vi.fn(() => ({
    back: vi.fn(),
  })),
}))

// Mock API
vi.mock('@/api/consultation', () => ({
  getPrescription: vi.fn(),
}))

import Prescription from '@/views/consult/Prescription.vue'
import NavBar from '@/layouts/NavBar.vue'
import * as consultationApi from '@/api/consultation'

/** 构造模拟处方数据 */
function mockPrescriptionDTO(overrides: Record<string, any> = {}) {
  return {
    id: 1,
    consultationId: 1,
    userId: 1,
    doctorId: 1,
    petId: 1,
    diagnosis: '犬急性胃炎',
    advice: '禁食24小时，多喝水',
    status: 1,
    createTime: '2026-06-20T10:30:00',
    items: [
      {
        medicineId: 1,
        medicineName: '奥美拉唑胶囊',
        specification: '20mg*14粒',
        dosage: '1粒/次',
        frequency: '每日1次',
        duration: '3天',
        quantity: 1,
        remarks: '饭后服用',
      },
      {
        medicineId: 2,
        medicineName: '益生菌粉',
        specification: '2g*30袋',
        dosage: '1袋/次',
        frequency: '每日2次',
        duration: '5天',
        quantity: 1,
        remarks: null,
      },
    ],
    ...overrides,
  }
}

describe('Prescription Page', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('Loading state', () => {
    it('should show skeleton while loading', async () => {
      vi.mocked(consultationApi.getPrescription).mockReturnValue(
        new Promise(() => {}), // never resolves
      )

      const wrapper = mount(Prescription, {
        global: {
          stubs: { NavBar: true, ElTag: true, ElSkeleton: true },
          directives: { loading: {} },
        },
      })

      expect(wrapper.find('.rx-loading').exists()).toBe(true)
      wrapper.unmount()
    })
  })

  describe('Successful data load', () => {
    it('should render prescription header with ID', async () => {
      vi.mocked(consultationApi.getPrescription).mockResolvedValue({
        data: { code: 200, data: mockPrescriptionDTO() },
      } as any)

      const wrapper = mount(Prescription, {
        global: {
          components: { NavBar },
          stubs: { ElTag: true, ElSkeleton: true },
        },
      })
      await nextTick()
      await nextTick()

      expect(wrapper.text()).toContain('电子处方笺')
      expect(wrapper.text()).toContain('#1')
      expect(wrapper.text()).toContain('犬急性胃炎')
      wrapper.unmount()
    })

    it('should render medicine list with correct items', async () => {
      vi.mocked(consultationApi.getPrescription).mockResolvedValue({
        data: { code: 200, data: mockPrescriptionDTO() },
      } as any)

      const wrapper = mount(Prescription, {
        global: {
          components: { NavBar },
          stubs: { ElTag: true, ElSkeleton: true },
        },
      })
      await nextTick()
      await nextTick()

      expect(wrapper.text()).toContain('奥美拉唑胶囊')
      expect(wrapper.text()).toContain('益生菌粉')
      expect(wrapper.text()).toContain('药品清单')
      wrapper.unmount()
    })

    it('should render advice section', async () => {
      vi.mocked(consultationApi.getPrescription).mockResolvedValue({
        data: { code: 200, data: mockPrescriptionDTO() },
      } as any)

      const wrapper = mount(Prescription, {
        global: {
          components: { NavBar },
          stubs: { ElTag: true, ElSkeleton: true },
        },
      })
      await nextTick()
      await nextTick()

      expect(wrapper.text()).toContain('医嘱建议')
      expect(wrapper.text()).toContain('禁食24小时，多喝水')
      wrapper.unmount()
    })
  })

  describe('Empty data', () => {
    it('should not show diagnosis section when no diagnosis', async () => {
      vi.mocked(consultationApi.getPrescription).mockResolvedValue({
        data: { code: 200, data: mockPrescriptionDTO({ diagnosis: null }) },
      } as any)

      const wrapper = mount(Prescription, {
        global: {
          components: { NavBar },
          stubs: { ElTag: true, ElSkeleton: true },
        },
      })
      await nextTick()
      await nextTick()

      expect(wrapper.text()).not.toContain('诊断结果')
      wrapper.unmount()
    })

    it('should not show medicine section when items is empty', async () => {
      vi.mocked(consultationApi.getPrescription).mockResolvedValue({
        data: { code: 200, data: mockPrescriptionDTO({ items: [] }) },
      } as any)

      const wrapper = mount(Prescription, {
        global: {
          components: { NavBar },
          stubs: { ElTag: true, ElSkeleton: true },
        },
      })
      await nextTick()
      await nextTick()

      expect(wrapper.text()).not.toContain('药品清单')
      wrapper.unmount()
    })

    it('should not show advice section when no advice', async () => {
      vi.mocked(consultationApi.getPrescription).mockResolvedValue({
        data: { code: 200, data: mockPrescriptionDTO({ advice: null }) },
      } as any)

      const wrapper = mount(Prescription, {
        global: {
          components: { NavBar },
          stubs: { ElTag: true, ElSkeleton: true },
        },
      })
      await nextTick()
      await nextTick()

      expect(wrapper.text()).not.toContain('医嘱建议')
      wrapper.unmount()
    })
  })

  describe('API error', () => {
    it('should handle API failure gracefully', async () => {
      vi.mocked(consultationApi.getPrescription).mockRejectedValue(new Error('Network error'))

      const wrapper = mount(Prescription, {
        global: {
          components: { NavBar },
          stubs: { ElTag: true, ElSkeleton: true },
          directives: { loading: {} },
        },
      })
      await nextTick()
      await nextTick()

      // Component should not crash; rx remains null, loading skeleton visible
      expect(wrapper.find('.rx-loading').exists()).toBe(true)
      wrapper.unmount()
    })
  })
})

describe('formatDate helper', () => {
  /** 将 Prescription.vue 中的 formatDate 逻辑提取为纯函数 */
  function formatDate(d?: string): string {
    return d ? d.slice(0, 16).replace('T', ' ') : ''
  }

  it('should format ISO date string to readable format', () => {
    expect(formatDate('2026-06-20T10:30:00')).toBe('2026-06-20 10:30')
  })

  it('should return empty string for undefined input', () => {
    expect(formatDate(undefined)).toBe('')
  })

  it('should return empty string for empty input', () => {
    expect(formatDate('')).toBe('')
  })

  it('should handle date without time', () => {
    expect(formatDate('2026-06-20')).toBe('2026-06-20')
  })

  it('should handle date with Z suffix', () => {
    const result = formatDate('2026-06-20T10:30:00Z')
    // slice(0,16) gives "2026-06-20T10:30"
    expect(result).toBe('2026-06-20 10:30')
  })
})

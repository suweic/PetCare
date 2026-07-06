import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  createConsultation,
  getConsultationList,
  getConsultationDetail,
  getConsultationMessages,
  cancelConsultation,
  getPrescription,
  createPrescription,
} from '@/api/consultation'
import { createEvaluation, replyEvaluation } from '@/api/evaluation'
import { connectWs, disconnectWs, sendWsMessage } from '@/utils/websocket'
import type { Consultation, ConsultationMessage, PrescriptionDTO, EvaluationDTO } from '@/types'

export const useConsultationStore = defineStore('consultation', () => {
  const list = ref<Consultation[]>([])
  const current = ref<Consultation | null>(null)
  const messages = ref<ConsultationMessage[]>([])
  const prescription = ref<PrescriptionDTO | null>(null)
  const loading = ref(false)

  /** 获取问诊列表 */
  async function fetchList(params?: { status?: number; page?: number; size?: number }) {
    loading.value = true
    try {
      const { data } = await getConsultationList(params)
      if (data.code === 200) {
        list.value = data.data.records
      }
    } finally {
      loading.value = false
    }
  }

  /** 获取问诊详情 */
  async function fetchDetail(id: number) {
    const { data } = await getConsultationDetail(id)
    if (data.code === 200) {
      current.value = data.data
    }
    return data
  }

  /** 获取历史消息 */
  async function fetchMessages(id: number) {
    const { data } = await getConsultationMessages(id)
    if (data.code === 200) {
      messages.value = data.data.records
    }
    return data
  }

  /** 新建问诊 */
  async function create(params: Parameters<typeof createConsultation>[0]) {
    const { data } = await createConsultation(params)
    if (data.code === 200) {
      current.value = data.data
      list.value.unshift(data.data)
    }
    return data
  }

  /** 取消问诊 */
  async function cancel(id: number) {
    const { data } = await cancelConsultation(id)
    if (data.code === 200 && current.value) {
      current.value.status = 3
    }
    return data
  }

  /** 加载处方 */
  async function fetchPrescription(consultationId: number) {
    const { data } = await getPrescription(consultationId)
    if (data.code === 200) {
      prescription.value = data.data
    }
    return data
  }

  /** 开具处方 */
  async function writePrescription(
    params: Parameters<typeof createPrescription>[0],
  ) {
    const { data } = await createPrescription(params)
    if (data.code === 200) {
      prescription.value = data.data
    }
    return data
  }

  /** 创建评价 */
  async function submitEvaluation(params: Parameters<typeof createEvaluation>[0]) {
    return await createEvaluation(params)
  }

  /** 回复评价 */
  async function replyToEvaluation(id: number, reply: string) {
    return await replyEvaluation(id, reply)
  }

  // ---------- WebSocket ----------
  let wsMessageListener: ((msg: any) => void) | null = null

  function connectChat(consultationId: number, onMessage: (msg: any) => void) {
    wsMessageListener = onMessage
    connectWs(consultationId, (msg) => {
      messages.value.push(msg as any)
      onMessage(msg)
    })
  }

  function sendMessage(body: { consultationId: number; senderType: number; senderId: number; messageType: number; content: string }) {
    sendWsMessage('/app/chat', body)
  }

  function disconnectChat() {
    disconnectWs()
    wsMessageListener = null
  }

  function resetCurrent() {
    current.value = null
    messages.value = []
    prescription.value = null
    disconnectChat()
  }

  return {
    list,
    current,
    messages,
    prescription,
    loading,
    fetchList,
    fetchDetail,
    fetchMessages,
    create,
    cancel,
    fetchPrescription,
    writePrescription,
    submitEvaluation,
    replyToEvaluation,
    connectChat,
    sendMessage,
    disconnectChat,
    resetCurrent,
  }
})

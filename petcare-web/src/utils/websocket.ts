import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { ref } from 'vue'
import { getToken } from '@/utils/auth'

let client: Client | null = null
let reconnectAttempts = 0
let intentionalClose = false
let reconnectTimer: ReturnType<typeof setTimeout> | null = null
const MAX_RECONNECT = 5
const RECONNECT_DELAY = 3000

/** 连接状态 */
export const wsConnected = ref(false)
export const wsReconnecting = ref(false)

export interface WsMessage {
  consultationId: number
  senderType: number
  senderId: number
  messageType: number
  content: string
  mediaUrl?: string
  duration?: number
}

/**
 * 连接 WebSocket
 * @param consultationId 问诊ID
 * @param onMessage      消息回调
 */
export function connectWs(
  consultationId: number,
  onMessage: (msg: WsMessage & { createTime: string }) => void,
): void {
  if (client?.active) disconnectWs()

  reconnectAttempts = 0
  intentionalClose = false
  wsConnected.value = false
  wsReconnecting.value = false

  client = new Client({
    webSocketFactory: () => new SockJS(`${import.meta.env.VITE_WS_URL || '/ws'}?token=${getToken() || ''}`),
    connectHeaders: {
      Authorization: `Bearer ${getToken() || ''}`,
    },
    debug: (str: string) => {
      if (import.meta.env.DEV) console.log('[STOMP]', str)
    },
    // 禁用 STOMP 自带重连，使用手动逻辑
    reconnectDelay: 0,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  })

  client.onConnect = () => {
    wsConnected.value = true
    wsReconnecting.value = false
    reconnectAttempts = 0
    if (import.meta.env.DEV) console.log('[WS] 已连接')

    client!.subscribe(`/topic/consultation/${consultationId}`, (msg: IMessage) => {
      try {
        const body = JSON.parse(msg.body)
        onMessage(body)
      } catch {
        console.warn('[WS] 消息解析失败', msg.body)
      }
    })
  }

  client.onDisconnect = () => {
    wsConnected.value = false
  }

  client.onStompError = (frame) => {
    console.error('[WS] STOMP error', frame.headers['message'])
    attemptReconnect(consultationId, onMessage)
  }

  client.onWebSocketError = () => {
    console.warn('[WS] WebSocket error')
    wsConnected.value = false
    attemptReconnect(consultationId, onMessage)
  }

  client.onWebSocketClose = () => {
    wsConnected.value = false
    if (!intentionalClose) {
      attemptReconnect(consultationId, onMessage)
    }
  }

  client.activate()
}

/** 手动重连：3s间隔，最多5次。使用计时器引用防止并发重连链。 */
function attemptReconnect(
  consultationId: number,
  onMessage: (msg: WsMessage & { createTime: string }) => void,
): void {
  if (reconnectAttempts >= MAX_RECONNECT) {
    console.warn(`[WS] 重连 ${MAX_RECONNECT} 次失败，停止重连`)
    wsReconnecting.value = false
    return
  }

  // 防止并发重连：如果已有排队的重连任务则直接跳过
  if (reconnectTimer !== null) {
    if (import.meta.env.DEV) console.log('[WS] 已有排队的重连任务，跳过')
    return
  }

  wsReconnecting.value = true
  reconnectAttempts++
  if (import.meta.env.DEV) console.log(`[WS] 将在 ${RECONNECT_DELAY / 1000}s 后第 ${reconnectAttempts}/${MAX_RECONNECT} 次重连...`)

  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    wsReconnecting.value = false

    // 二次检查：如果在此期间已连接成功或达到上限，则不再重连
    if (wsConnected.value || reconnectAttempts >= MAX_RECONNECT) {
      if (reconnectAttempts >= MAX_RECONNECT) {
        console.warn(`[WS] 重连 ${MAX_RECONNECT} 次失败，停止重连`)
      }
      return
    }

    // 先停掉旧的，重新建连
    if (client) {
      try { client.deactivate() } catch { /* ignore */ }
    }
    client = null
    connectWs(consultationId, onMessage)
  }, RECONNECT_DELAY)
}

/**
 * 发送 WebSocket 消息
 */
export function sendWsMessage(destination: string, body: Record<string, any>): void {
  if (!client?.active) {
    console.warn('[WS] 未连接，无法发送')
    return
  }
  client.publish({ destination, body: JSON.stringify(body) })
}

/**
 * 断开 WebSocket
 */
export function disconnectWs(): void {
  intentionalClose = true
  wsConnected.value = false
  wsReconnecting.value = false
  reconnectAttempts = 0

  // 清除任何排队的重连计时器
  if (reconnectTimer !== null) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }

  if (client) {
    client.deactivate()
    client = null
  }
}

/** 检查是否已连接 */
export function isWsConnected(): boolean {
  return client?.active ?? false
}

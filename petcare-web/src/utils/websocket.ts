import { Client, type IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { ref } from 'vue'
import { getToken } from '@/utils/auth'

let client: Client | null = null
let reconnectAttempts = 0
let intentionalClose = false
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
    webSocketFactory: () => new SockJS('/ws/chat'),
    connectHeaders: {
      Authorization: `Bearer ${getToken() || ''}`,
    },
    debug: (str) => {
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
    console.log('[WS] 已连接')

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

/** 手动重连：3s间隔，最多5次 */
function attemptReconnect(
  consultationId: number,
  onMessage: (msg: WsMessage & { createTime: string }) => void,
): void {
  if (reconnectAttempts >= MAX_RECONNECT) {
    console.warn(`[WS] 重连 ${MAX_RECONNECT} 次失败，停止重连`)
    wsReconnecting.value = false
    return
  }
  if (wsReconnecting.value) return // 已经在重连中

  wsReconnecting.value = true
  reconnectAttempts++
  console.log(`[WS] 将在 ${RECONNECT_DELAY / 1000}s 后第 ${reconnectAttempts}/${MAX_RECONNECT} 次重连...`)

  setTimeout(() => {
    wsReconnecting.value = false
    if (!wsConnected.value && reconnectAttempts < MAX_RECONNECT) {
      // 先停掉旧的，重新建连
      if (client) {
        try { client.deactivate() } catch { /* ignore */ }
      }
      client = null
      connectWs(consultationId, onMessage)
    }
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
  if (client) {
    client.deactivate()
    client = null
  }
}

/** 检查是否已连接 */
export function isWsConnected(): boolean {
  return client?.active ?? false
}

/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

declare module 'sockjs-client'

declare module '@stomp/stompjs' {
  interface IMessage {
    body: string
    headers: Record<string, string>
    ack: (headers?: Record<string, string>) => void
    nack: (headers?: Record<string, string>) => void
  }

  class Client {
    constructor(conf?: Record<string, any>)
    activate(): void
    deactivate(): void
    readonly connected: boolean
    readonly active: boolean
    onConnect: (frame: Record<string, any>) => void
    onDisconnect: (frame: Record<string, any>) => void
    onStompError: (frame: Record<string, any>) => void
    onWebSocketError: (evt: Event) => void
    onWebSocketClose: (evt: CloseEvent) => void
    subscribe(
      destination: string,
      callback: (message: IMessage) => void,
      headers?: Record<string, string>,
    ): { unsubscribe: () => void }
    publish(params: { destination: string; body: string; headers?: Record<string, string> }): void
    debug: (str: string) => void
  }

  export { Client, IMessage }
}

declare module 'element-plus/dist/locale/zh-cn.mjs'

<template>
  <div class="chat-page">
    <!-- 顶部医生信息 -->
    <div class="chat-header">
      <div class="header-left" @click="handleBack">
        <el-icon :size="20"><ArrowLeft /></el-icon>
      </div>
      <div class="header-info">
        <el-avatar :size="36" :src="doctorInfo?.avatar" />
        <div class="header-text">
          <div class="header-name">{{ doctorInfo?.realName || '问诊中' }}</div>
          <div class="header-status">
            <span class="ws-dot" :class="{ connected: wsConnected, reconnecting: wsReconnecting }" />
            {{ wsStatusText }}
          </div>
        </div>
      </div>
      <div class="header-right">
        <el-icon :size="20" @click="showPrescription = !showPrescription"><Document /></el-icon>
      </div>
    </div>

    <!-- WebSocket 重连提示 -->
    <div class="reconnect-bar" v-if="wsReconnecting">
      <span>连接断开，正在重连 ({{ reconnectCount }}/5)...</span>
    </div>

    <!-- 消息区域 -->
    <div class="message-area" ref="msgAreaRef" @scroll="onScroll">
      <div v-if="store.messages.length === 0 && !loadingHistory" class="chat-empty">
        <span class="empty-icon">💬</span>
        <p>开始和医生沟通吧</p>
        <p class="text-muted">请描述宠物的症状和问题</p>
      </div>

      <div v-if="loadingHistory" class="loading-history">
        <el-icon class="loading-icon" :size="18"><Loading /></el-icon>
        <span>加载消息...</span>
      </div>

      <div
        v-for="(msg, idx) in store.messages"
        :key="msg.id || idx"
        class="msg-row"
        :class="{ 'msg-self': msg.senderType === 1 }"
      >
        <!-- 医生头像（左侧） -->
        <el-avatar v-if="msg.senderType === 2" :size="32" :src="doctorInfo?.avatar" class="msg-avatar" />

        <div class="msg-wrapper">
          <div class="msg-bubble" :class="{ 'bubble-self': msg.senderType === 1, 'bubble-other': msg.senderType === 2 }">
            <div v-if="msg.content" class="msg-text">{{ msg.content }}</div>
            <img
              v-if="msg.mediaUrl"
              :src="msg.mediaUrl"
              class="msg-image"
              @click="previewImage(msg.mediaUrl)"
            />
          </div>
          <div class="msg-time" :class="{ 'time-right': msg.senderType === 1 }">
            {{ formatMsgTime(msg.createTime) }}
          </div>
        </div>

        <!-- 用户头像（右侧） -->
        <el-avatar v-if="msg.senderType === 1" :size="32" :src="userStore.userInfo?.avatar" class="msg-avatar" />
      </div>
    </div>

    <!-- 处方面板 -->
    <div class="prescription-panel" v-if="showPrescription && store.prescription">
      <div class="rx-header flex-between">
        <span class="rx-title">📋 电子处方</span>
        <el-icon @click="showPrescription = false"><Close /></el-icon>
      </div>
      <div class="rx-body">
        <div class="rx-row" v-if="store.prescription.diagnosis">
          <span class="rx-label">诊断结果</span>
          <span>{{ store.prescription.diagnosis }}</span>
        </div>
        <div class="rx-row" v-if="store.prescription.advice">
          <span class="rx-label">医嘱建议</span>
          <span>{{ store.prescription.advice }}</span>
        </div>
        <div class="rx-meds" v-if="store.prescription.items?.length">
          <div class="rx-label" style="margin-bottom:6px">药品清单</div>
          <div class="rx-med-item" v-for="item in store.prescription.items" :key="item.medicineName">
            <div class="med-name">{{ item.medicineName }} <span v-if="item.specification">({{ item.specification }})</span></div>
            <div class="med-usage">{{ item.dosage }} · {{ item.frequency }} · {{ item.duration }} × {{ item.quantity || 1 }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area safe-bottom">
      <!-- 图片预览 -->
      <div class="img-preview" v-if="previewUrl">
        <img :src="previewUrl" />
        <el-icon class="img-remove" @click="removePreview"><CircleCloseFilled /></el-icon>
      </div>
      <div class="input-row">
        <label class="upload-btn" :class="{ disabled: uploading }">
          <el-icon :size="22" color="#666"><Picture /></el-icon>
          <input
            type="file"
            accept="image/jpeg,image/png,image/jpg"
            class="file-input-hidden"
            :disabled="uploading"
            @change="handleImagePicked"
          />
        </label>
        <el-input
          v-model="inputText"
          placeholder="输入消息..."
          class="text-input"
          resize="none"
          @keyup.enter.exact="sendText"
          :disabled="!wsConnected"
        />
        <el-button
          type="primary"
          :icon="Promotion"
          circle
          size="small"
          class="send-btn"
          :disabled="!inputText.trim() || !wsConnected"
          @click="sendText"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Document, Promotion, Loading,
  Picture, CircleCloseFilled, Close,
} from '@element-plus/icons-vue'
import { useConsultationStore } from '@/stores/consultation'
import { useUserStore } from '@/stores/user'
import { getDoctorDetail } from '@/api/doctor'
import { uploadChatImage } from '@/api/consultation'
import { validateImage } from '@/utils/upload'
import {
  connectWs, disconnectWs, sendWsMessage,
  wsConnected, wsReconnecting,
} from '@/utils/websocket'

const route = useRoute()
const router = useRouter()
const store = useConsultationStore()
const userStore = useUserStore()

const msgAreaRef = ref<HTMLElement>()
const inputText = ref('')
const showPrescription = ref(false)
const previewUrl = ref('')
const previewFile = ref<File | null>(null)
const uploading = ref(false)
const loadingHistory = ref(false)
const reconnectCount = ref(0)

const consultationId = computed(() => Number(route.params.id))

// 医生信息 (从 store.current 获取)
const doctorInfo = ref<{ avatar?: string; realName?: string; title?: string } | null>(null)

const wsStatusText = computed(() => {
  if (wsConnected.value) return '在线'
  if (wsReconnecting.value) return '重连中...'
  return '连接中...'
})

// 组件挂载状态标记，防止 unmount 后 async 延续执行
let isMounted = true

onMounted(async () => {
  loadingHistory.value = true
  // 加载问诊详情
  await store.fetchDetail(consultationId.value)
  if (!isMounted) return

  // 加载医生信息
  if (store.current?.doctorId) {
    try {
      const { data } = await getDoctorDetail(store.current.doctorId)
      if (!isMounted) return
      if (data.code === 200) doctorInfo.value = data.data
    } catch { /* ignore */ }
  }

  // 加载历史消息
  await store.fetchMessages(consultationId.value)
  if (!isMounted) return
  loadingHistory.value = false

  // 连接 WebSocket（带重连）
  if (!isMounted) return
  connectWs(consultationId.value, () => {
    nextTick(() => scrollToBottom())
  })

  // 监听重连次数
  const timer = setInterval(() => {
    if (wsReconnecting.value) reconnectCount.value++
    else reconnectCount.value = 0
  }, 3000)

  onUnmounted(() => clearInterval(timer))

  // 加载处方tab
  if (route.query.tab === 'prescription') {
    await store.fetchPrescription(consultationId.value)
    if (!isMounted) return
    showPrescription.value = true
  }

  if (!isMounted) return
  nextTick(() => scrollToBottom())
})

onUnmounted(() => {
  isMounted = false
  disconnectWs()
  store.resetCurrent()
})

function handleBack() {
  disconnectWs()
  store.resetCurrent()
  router.back()
}

// ----- 消息发送 -----
async function sendText() {
  const text = inputText.value.trim()
  if (!text || !wsConnected.value) return
  inputText.value = ''

  try {
    sendWsMessage('/app/chat', {
      consultationId: consultationId.value,
      senderType: 1,
      senderId: userStore.userInfo?.id || 0,
      messageType: 1,
      content: text,
    })
    nextTick(() => scrollToBottom())
  } catch {
    ElMessage.warning('发送失败，请重试')
  }
}

// ----- 图片上传 -----
function handleImagePicked(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return

  if (!validateImage(file)) {
    (e.target as HTMLInputElement).value = ''
    return
  }

  // 本地预览
  const reader = new FileReader()
  reader.onload = () => { previewUrl.value = reader.result as string }
  reader.readAsDataURL(file)

  uploadAndSendImage(file)
}

async function uploadAndSendImage(file: File) {
  uploading.value = true
  try {
    const { data } = await uploadChatImage(file)
    if (data.code === 200 && wsConnected.value) {
      sendWsMessage('/app/chat', {
        consultationId: consultationId.value,
        senderType: 1,
        senderId: userStore.userInfo?.id || 0,
        messageType: 2,
        content: '',
        mediaUrl: data.data.url,
      })
      nextTick(() => scrollToBottom())
    }
    previewUrl.value = ''
    previewFile.value = null
  } catch {
    ElMessage.warning('图片上传失败')
  } finally {
    uploading.value = false
  }
}

function removePreview() {
  previewUrl.value = ''
  previewFile.value = null
}

// ----- 辅助 -----
function scrollToBottom() {
  if (msgAreaRef.value) {
    msgAreaRef.value.scrollTop = msgAreaRef.value.scrollHeight
  }
}

function onScroll() {
  // 可扩展：上拉加载更多历史消息
}

function formatMsgTime(t?: string): string {
  if (!t) return ''
  const d = new Date(t)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function previewImage(url: string) {
  // 防止 XSS：仅允许 http/https 协议的 URL
  if (!/^https?:\/\//i.test(url)) {
    console.warn('[Chat] 拒绝打开非安全URL:', url)
    return
  }
  window.open(url, '_blank', 'noopener,noreferrer')
}
</script>

<style scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #ededed;
}

/* 顶部医生信息 */
.chat-header {
  display: flex;
  align-items: center;
  height: 52px;
  padding: 0 12px;
  background: #fff;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}
.header-left { cursor: pointer; padding: 4px; }
.header-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: 4px;
}
.header-name { font-size: 15px; font-weight: 600; }
.header-status {
  font-size: 11px;
  color: var(--text-muted);
  display: flex;
  align-items: center;
  gap: 4px;
}
.ws-dot {
  width: 6px; height: 6px; border-radius: 50%; background: #ccc;
}
.ws-dot.connected { background: #67c23a; }
.ws-dot.reconnecting { background: #e6a23c; animation: blink 1s infinite; }
@keyframes blink { 50% { opacity: 0.3; } }
.header-right { cursor: pointer; padding: 4px; }

/* 重连提示 */
.reconnect-bar {
  background: #fdf6ec; color: #e6a23c; font-size: 12px;
  text-align: center; padding: 6px;
}

/* 消息区 */
.message-area {
  flex: 1;
  overflow-y: auto;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  -webkit-overflow-scrolling: touch;
}
.chat-empty {
  text-align: center; padding-top: 80px; color: var(--text-secondary);
}
.empty-icon { font-size: 40px; display: block; margin-bottom: 8px; }
.loading-history {
  text-align: center; padding: 12px; font-size: 12px; color: var(--text-muted);
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
.loading-icon { animation: spin 1s linear infinite; }
@keyframes spin { 100% { transform: rotate(360deg); } }

/* 消息行 */
.msg-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.msg-self { flex-direction: row-reverse; }
.msg-avatar { flex-shrink: 0; }
.msg-wrapper { max-width: 70%; }
.msg-bubble {
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}
.bubble-other {
  background: #fff;
  border-top-left-radius: 4px;
}
.bubble-self {
  background: #95ec69;
  border-top-right-radius: 4px;
}
.msg-image {
  max-width: 180px;
  border-radius: 8px;
  cursor: pointer;
}
.msg-time {
  font-size: 10px;
  color: var(--text-muted);
  margin-top: 3px;
}
.time-right { text-align: right; }

/* 处方面板 */
.prescription-panel {
  background: #fff;
  margin: 0 10px;
  border-radius: 12px;
  max-height: 35vh;
  overflow-y: auto;
  box-shadow: 0 -2px 12px rgba(0,0,0,0.08);
}
.rx-header { padding: 12px 16px; border-bottom: 1px solid var(--border); }
.rx-title { font-size: 16px; font-weight: 600; }
.rx-body { padding: 12px 16px; }
.rx-row {
  display: flex; gap: 8px; font-size: 13px; margin-bottom: 10px; line-height: 1.5;
}
.rx-label {
  font-weight: 500; color: var(--text-primary); flex-shrink: 0; min-width: 56px;
}
.rx-med-item {
  padding: 8px 0; border-bottom: 1px dashed var(--border);
}
.rx-med-item:last-child { border-bottom: none; }
.med-name { font-size: 13px; font-weight: 500; }
.med-usage { font-size: 12px; color: var(--text-secondary); margin-top: 2px; }

/* 输入区域 */
.input-area {
  background: #fff;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}
.img-preview {
  position: relative;
  display: inline-block;
  padding: 8px 0 0 12px;
}
.img-preview img {
  width: 60px; height: 60px; object-fit: cover; border-radius: 8px;
}
.img-remove {
  position: absolute; top: 2px; right: -6px;
  color: #f56c6c; cursor: pointer; font-size: 18px;
}
.input-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 10px 12px;
}
.upload-btn {
  display: flex; align-items: center; justify-content: center;
  width: 36px; height: 36px; cursor: pointer; flex-shrink: 0;
}
.upload-btn.disabled { opacity: 0.4; cursor: not-allowed; }
.file-input-hidden { display: none; }
.text-input { flex: 1; }
.send-btn { flex-shrink: 0; }
</style>

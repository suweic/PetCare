<template>
  <MainLayout>
    <div class="settings-page">
      <NavBar title="账号设置" />

      <!-- 头像编辑卡片 -->
      <div class="avatar-card">
        <el-avatar
          :size="64"
          :src="avatarOf(userStore.userInfo?.avatar, true)"
          @error="onAvatarError"
        />
        <div class="avatar-meta">
          <div class="avatar-name">{{ userStore.userInfo?.nickname || '宠主' }}</div>
          <div class="avatar-phone">{{ maskedPhone }}</div>
        </div>
        <el-button size="small" plain @click="avatarDialog = true">修改头像</el-button>
      </div>

      <!-- 偏好设置 -->
      <div class="menu-section">
        <div class="section-title">偏好</div>
        <div class="menu-item">
          <div class="menu-left">
            <el-icon color="#673ab7"><Moon /></el-icon>
            <span>深色模式</span>
          </div>
          <el-switch v-model="darkMode" @change="toggleDark" />
        </div>
        <div class="menu-item">
          <div class="menu-left">
            <el-icon color="#2196f3"><Notification /></el-icon>
            <span>消息推送</span>
          </div>
          <el-switch v-model="notifEnabled" @change="savePrefs" />
        </div>
        <div class="menu-item" @click="languageDialog = true">
          <div class="menu-left">
            <el-icon color="#009688"><Position /></el-icon>
            <span>语言</span>
          </div>
          <div class="menu-right">
            <span class="menu-value">{{ currentLang.label }}</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
      </div>

      <!-- 数据与缓存 -->
      <div class="menu-section">
        <div class="section-title">数据</div>
        <div class="menu-item" @click="clearCache">
          <div class="menu-left">
            <el-icon color="#ff9800"><Delete /></el-icon>
            <span>清理缓存</span>
          </div>
          <div class="menu-right">
            <span class="menu-value">{{ cacheSize }}</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
        <div class="menu-item" @click="exportData">
          <div class="menu-left">
            <el-icon color="#4caf50"><Download /></el-icon>
            <span>导出我的数据</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 关于 -->
      <div class="menu-section">
        <div class="section-title">关于</div>
        <div class="menu-item" @click="aboutDialog = true">
          <div class="menu-left">
            <el-icon color="#9c27b0"><InfoFilled /></el-icon>
            <span>关于 PetCare</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item">
          <div class="menu-left">
            <el-icon color="#607d8b"><Document /></el-icon>
            <span>版本</span>
          </div>
          <span class="menu-value">v1.0.0</span>
        </div>
        <div class="menu-item" @click="checkUpdate">
          <div class="menu-left">
            <el-icon color="#00bcd4"><Refresh /></el-icon>
            <span>检查更新</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 退出登录 -->
      <div class="logout-area">
        <el-button class="logout-btn" size="large" @click="handleLogout">退出登录</el-button>
      </div>

      <!-- 修改头像弹窗 -->
      <el-dialog v-model="avatarDialog" title="修改头像" width="85%">
        <div class="avatar-uploader">
          <el-avatar
            :size="96"
            :src="previewAvatar || avatarOf(userStore.userInfo?.avatar, true)"
            @error="onAvatarError"
          />
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            style="display:none"
            @change="onFileChange"
          />
          <div style="margin-top:12px">
            <el-button @click="fileInput?.click()">选择图片</el-button>
            <el-button
              v-if="previewAvatar"
              type="primary"
              :loading="avatarUploading"
              @click="uploadAvatar"
            >上传</el-button>
          </div>
          <p class="upload-tip">支持 JPG / PNG，大小不超过 2MB</p>
        </div>
      </el-dialog>

      <!-- 关于弹窗 -->
      <el-dialog v-model="aboutDialog" title="关于 PetCare" width="85%">
        <div class="about-content">
          <div class="about-logo">🐾</div>
          <h3>PetCare 宠物在线问诊</h3>
          <p>连接宠物主与资深兽医，提供图文 / 视频 / 语音在线问诊、电子处方、宠物健康管理。</p>
          <p class="version">版本 1.0.0 · build {{ buildTime }}</p>
        </div>
      </el-dialog>

      <!-- 语言选择弹窗 -->
      <el-dialog v-model="languageDialog" title="选择语言" width="80%">
        <el-radio-group v-model="langValue" class="lang-group" @change="savePrefs">
          <el-radio
            v-for="l in langs"
            :key="l.value"
            :value="l.value"
            class="lang-item"
          >{{ l.label }}</el-radio>
        </el-radio-group>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowRight, Moon, Notification, Position, Delete, Download,
  InfoFilled, Document, Refresh,
} from '@element-plus/icons-vue'
import MainLayout from '@/layouts/MainLayout.vue'
import NavBar from '@/layouts/NavBar.vue'
import { useUserStore } from '@/stores/user'
import { avatarOf, onAvatarError } from '@/utils/avatar-helper'
import { uploadPetAvatar } from '@/api/pet'

const router = useRouter()
const userStore = useUserStore()

// ----- 基础信息 -----
const maskedPhone = computed(() => {
  const p = userStore.userInfo?.phone || ''
  return p.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
})

// ----- 偏好 -----
const darkMode = ref(localStorage.getItem('pc:darkMode') === '1')
const notifEnabled = ref(localStorage.getItem('pc:notif') !== '0')
const langValue = ref(localStorage.getItem('pc:lang') || 'zh-CN')
const cacheSize = ref('0 KB')

const langs = [
  { label: '简体中文', value: 'zh-CN' },
  { label: '繁體中文', value: 'zh-TW' },
  { label: 'English',  value: 'en-US' },
]
const currentLang = computed(() =>
  langs.find((l) => l.value === langValue.value) || langs[0]
)

function toggleDark(v: boolean) {
  localStorage.setItem('pc:darkMode', v ? '1' : '0')
  document.documentElement.classList.toggle('pc-dark', v)
  ElMessage.success(v ? '已开启深色模式' : '已关闭深色模式')
}
function savePrefs() {
  localStorage.setItem('pc:notif', notifEnabled.value ? '1' : '0')
  localStorage.setItem('pc:lang', langValue.value)
}

onMounted(() => {
  // 初始化深色模式 class
  if (darkMode.value) document.documentElement.classList.add('pc-dark')
  // 计算缓存大小
  let total = 0
  for (let i = 0; i < localStorage.length; i++) {
    const k = localStorage.key(i)
    if (k) total += (k.length + (localStorage.getItem(k) || '').length) * 2
  }
  cacheSize.value = total < 1024 ? `${total} B` : `${(total / 1024).toFixed(1)} KB`
})

// ----- 清理缓存 / 导出数据 -----
async function clearCache() {
  try {
    await ElMessageBox.confirm(
      `将清空本地缓存（${cacheSize.value}），不会删除你的账号数据。`,
      '清理缓存',
      { type: 'warning' }
    )
  } catch { return }
  // 保留 token / userInfo / 偏好
  const keep = ['pc:darkMode', 'pc:notif', 'pc:lang']
  const saved: Record<string, string> = {}
  keep.forEach((k) => {
    const v = localStorage.getItem(k)
    if (v != null) saved[k] = v
  })
  const token = localStorage.getItem('pc:token')
  const user  = localStorage.getItem('pc:user')
  localStorage.clear()
  Object.entries(saved).forEach(([k, v]) => localStorage.setItem(k, v))
  if (token) localStorage.setItem('pc:token', token)
  if (user)  localStorage.setItem('pc:user', user)
  ElMessage.success('缓存已清理')
  cacheSize.value = '0 B'
}

function exportData() {
  const data = {
    userInfo: userStore.userInfo,
    exportedAt: new Date().toISOString(),
  }
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `petcare-my-data-${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('已导出')
}

// ----- 修改头像 -----
const avatarDialog = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const previewAvatar = ref('')
const avatarUploading = ref(false)
const pendingFile = ref<File | null>(null)

function onFileChange(e: Event) {
  const f = (e.target as HTMLInputElement).files?.[0]
  if (!f) return
  if (f.size > 2 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 2MB')
    return
  }
  pendingFile.value = f
  const reader = new FileReader()
  reader.onload = () => { previewAvatar.value = String(reader.result || '') }
  reader.readAsDataURL(f)
}

async function uploadAvatar() {
  if (!pendingFile.value) return
  avatarUploading.value = true
  try {
    const { data } = await uploadPetAvatar(pendingFile.value)
    if (data.code === 200 && data.data) {
      // 复用 pet 头像上传端点；本地同步更新
      if (userStore.userInfo) {
        userStore.userInfo.avatar = data.data.url
      }
      previewAvatar.value = ''
      pendingFile.value = null
      avatarDialog.value = false
      ElMessage.success('头像已更新（前端缓存）')
    } else {
      ElMessage.error(data.message || '上传失败')
    }
  } catch {
    ElMessage.error('上传失败，请稍后重试')
  } finally {
    avatarUploading.value = false
  }
}

// ----- 关于 / 检查更新 -----
const aboutDialog = ref(false)
const languageDialog = ref(false)
const buildTime = new Date().toISOString().slice(0, 10)

function checkUpdate() {
  ElMessage.info('当前已是最新版本 v1.0.0')
}

// ----- 退出登录 -----
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  } catch { return }
  userStore.logout()
  ElMessage.success('已退出登录')
  router.replace('/login')
}
</script>

<style scoped>
.settings-page { min-height: 100%; padding-bottom: 80px; }

.avatar-card {
  display: flex; align-items: center; gap: 14px;
  background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: #fff; padding: 20px 16px;
}
.avatar-meta { flex: 1; }
.avatar-name { font-size: 16px; font-weight: 600; }
.avatar-phone { font-size: 12px; opacity: 0.85; margin-top: 2px; }

.menu-section { background: #fff; margin: 10px 0; }
.section-title {
  font-size: 12px; color: var(--text-muted);
  padding: 8px 16px 4px; background: var(--bg);
}
.menu-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; border-bottom: 1px solid var(--border); cursor: pointer;
}
.menu-item:last-child { border-bottom: none; }
.menu-left { display: flex; align-items: center; gap: 10px; font-size: 14px; }
.menu-right { display: flex; align-items: center; gap: 6px; color: var(--text-muted); }
.menu-value { font-size: 13px; color: var(--text-muted); }

.avatar-uploader { text-align: center; padding: 8px 0; }
.upload-tip { margin-top: 12px; font-size: 12px; color: var(--text-muted); }

.logout-area { padding: 24px 16px; }
.logout-btn { width: 100%; }

.about-content { text-align: center; padding: 8px 0; }
.about-logo { font-size: 48px; margin-bottom: 8px; }
.about-content h3 { margin: 4px 0 12px; }
.about-content p { font-size: 13px; color: var(--text-secondary); line-height: 1.6; }
.version { font-size: 12px; color: var(--text-muted); margin-top: 8px; }

.lang-group { display: flex; flex-direction: column; gap: 12px; }
.lang-item { margin-right: 0; }
</style>

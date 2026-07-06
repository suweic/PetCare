<template>
  <MainLayout>
    <div class="home-page">
      <!-- 头部区域 -->
      <div class="home-header">
        <div class="header-top flex-between">
          <div class="greeting">
            <h2>{{ greeting }}</h2>
            <p>{{ userStore.userInfo?.nickname || '宠主' }}，你的宠物今天怎么样？</p>
          </div>
          <el-avatar :size="44" :src="avatarOf(userStore.userInfo?.avatar)" class="header-avatar" @error="onAvatarError" />
        </div>
        <!-- 搜索栏 -->
        <div class="search-bar" @click="$router.push('/doctor-list')">
          <el-icon><Search /></el-icon>
          <span>搜索医生、科室…</span>
        </div>
      </div>

      <!-- 快捷入口 -->
      <div class="quick-actions">
        <div class="action-item ai-entry" @click="$router.push('/pre-consultation')">
          <div class="action-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
            <span class="ai-emoji">🤖</span>
          </div>
          <span>AI预问诊</span>
        </div>
        <div class="action-item" @click="$router.push('/consult')">
          <div class="action-icon" style="background: #e3f2fd">
            <el-icon :size="22" color="#2196f3"><ChatDotRound /></el-icon>
          </div>
          <span>我的问诊</span>
        </div>
        <div class="action-item" @click="$router.push('/doctor-list')">
          <div class="action-icon" style="background: #e8f5e9">
            <el-icon :size="22" color="#4caf50"><Plus /></el-icon>
          </div>
          <span>在线问诊</span>
        </div>
        <div class="action-item" @click="$router.push('/pet/list')">
          <div class="action-icon" style="background: #fff3e0">
            <el-icon :size="22" color="#ff9800"><PawIcon /></el-icon>
          </div>
          <span>宠物档案</span>
        </div>
        <div class="action-item" @click="$router.push('/profile')">
          <div class="action-icon" style="background: #f3e5f5">
            <el-icon :size="22" color="#9c27b0"><User /></el-icon>
          </div>
          <span>个人中心</span>
        </div>
      </div>

      <!-- 科室精选 - 横向滚动 -->
      <div class="section">
        <div class="section-header flex-between">
          <span class="section-title">科室精选</span>
          <span class="section-more" @click="$router.push('/doctor-list')">全部 ›</span>
        </div>
        <div class="dept-scroll">
          <div
            v-for="d in departments"
            :key="d.id"
            class="dept-item"
            @click="$router.push(`/doctor-list?deptId=${d.id}`)"
          >
            <div class="dept-icon-box" :style="{ background: d.bg }">
              <span class="dept-emoji">{{ d.icon }}</span>
            </div>
            <span class="dept-name">{{ d.name }}</span>
          </div>
        </div>
      </div>

      <!-- 推荐医生 - 卡片式 -->
      <div class="section">
        <div class="section-header flex-between">
          <span class="section-title">推荐医生</span>
          <span class="section-more" @click="$router.push('/doctor-list')">更多 ›</span>
        </div>
        <div class="doc-cards" v-if="doctors.length">
          <div
            v-for="doc in doctors"
            :key="doc.id"
            class="doc-card"
            @click="$router.push(`/consult/doctor/${doc.id}`)"
          >
            <div class="doc-card-top">
              <el-avatar :size="56" :src="avatarOf(doc.avatar, true)" @error="onAvatarError" />
              <div class="doc-badge" v-if="doc.rating">⭐ {{ doc.rating.toFixed(1) }}</div>
            </div>
            <div class="doc-card-body">
              <div class="doc-name">{{ doc.realName }}</div>
              <div class="doc-title">{{ doc.title }}</div>
              <div class="doc-dept">{{ doc.departmentName }} · {{ doc.hospital }}</div>
              <div class="doc-meta">
                <span>{{ doc.consultationCount }} 次问诊</span>
                <span class="doc-fee">¥{{ doc.consultationFee }}</span>
              </div>
            </div>
            <el-button class="doc-btn" size="small" type="primary" plain>立即问诊</el-button>
          </div>
        </div>
        <div v-else class="doc-loading">
          <el-skeleton :rows="2" animated />
        </div>
      </div>
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import MainLayout from '@/layouts/MainLayout.vue'
import PawIcon from '@/components/PawIcon.vue'
import { useUserStore } from '@/stores/user'
import { getDoctorList } from '@/api/doctor'
import { avatarOf, onAvatarError } from '@/utils/avatar-helper'
import type { DoctorListItem } from '@/types'

const userStore = useUserStore()
const doctors = ref<DoctorListItem[]>([])

const departments = [
  { id: 1, name: '内科', icon: '💊', bg: '#e3f2fd' },
  { id: 2, name: '外科', icon: '🔧', bg: '#fff3e0' },
  { id: 3, name: '皮肤科', icon: '🩺', bg: '#fce4ec' },
  { id: 4, name: '眼科', icon: '👁️', bg: '#e8f5e9' },
  { id: 5, name: '口腔科', icon: '🦷', bg: '#f3e5f5' },
  { id: 6, name: '营养科', icon: '🥗', bg: '#e0f7fa' },
  { id: 7, name: '行为学', icon: '🧠', bg: '#fff8e1' },
  { id: 8, name: '急诊科', icon: '🚨', bg: '#ffebee' },
]

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了 🌙'
  if (h < 11) return '早上好 ☀️'
  if (h < 14) return '中午好 🌤️'
  if (h < 18) return '下午好 🌈'
  return '晚上好 🌙'
})

onMounted(async () => {
  try {
    const { data } = await getDoctorList({ page: 1, size: 4 })
    if (data.code === 200) doctors.value = data.data.records
  } catch { /* ignore */ }
})
</script>

<style scoped>
.home-page { padding-bottom: 16px; }

/* 头部 */
.home-header {
  background: linear-gradient(135deg, var(--primary) 0%, #66bb6a 100%);
  padding: 20px 16px 16px;
  color: #fff;
}
.header-avatar {
  border: 2px solid rgba(255, 255, 255, 0.6);
}
.greeting h2 { font-size: 18px; }
.greeting p {
  font-size: 12px;
  opacity: 0.85;
  margin-top: 3px;
}
.search-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.22);
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
}

/* 快捷入口 */
.quick-actions {
  display: flex;
  justify-content: space-around;
  padding: 20px 12px;
  background: #fff;
  margin: 10px 0;
}
.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  cursor: pointer;
}
.action-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ai-emoji {
  font-size: 24px;
}
.action-item.ai-entry span {
  color: #667eea;
  font-weight: 500;
}

/* 通用区块 */
.section {
  background: #fff;
  padding: 16px;
  margin-top: 10px;
}
.section-header { margin-bottom: 14px; }
.section-title { font-size: 16px; font-weight: 600; }
.section-more {
  font-size: 13px;
  color: var(--text-muted);
  cursor: pointer;
}

/* 科室横向滚动 */
.dept-scroll {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  padding-bottom: 4px;
}
.dept-scroll::-webkit-scrollbar { display: none; }
.dept-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  flex-shrink: 0;
  width: 64px;
}
.dept-icon-box {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.dept-emoji { font-size: 26px; }
.dept-name {
  font-size: 11px;
  color: var(--text-secondary);
  white-space: nowrap;
}

/* 推荐医生卡片 */
.doc-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.doc-card {
  background: var(--bg);
  border-radius: 12px;
  padding: 14px 12px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: transform 0.2s;
}
.doc-card:active { transform: scale(0.97); }
.doc-card-top {
  position: relative;
  margin-bottom: 8px;
}
.doc-badge {
  position: absolute;
  bottom: -4px;
  right: -12px;
  background: #fff;
  border-radius: 10px;
  padding: 1px 6px;
  font-size: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.12);
}
.doc-card-body {
  text-align: center;
}
.doc-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}
.doc-title {
  font-size: 11px;
  color: var(--primary);
  margin: 2px 0;
}
.doc-dept {
  font-size: 11px;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 140px;
  margin: 2px 0 4px;
}
.doc-meta {
  display: flex;
  justify-content: center;
  gap: 8px;
  font-size: 10px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.doc-fee {
  color: var(--primary);
  font-weight: 600;
}
.doc-btn {
  width: 100%;
}

.doc-loading { padding: 8px 0; }
</style>

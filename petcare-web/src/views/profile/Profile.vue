<template>
  <MainLayout>
    <div class="profile-page">
      <!-- 用户信息卡片 -->
      <div class="profile-header">
        <el-avatar :size="64" :src="avatarOf(userStore.userInfo?.avatar)" @error="onAvatarError" />
        <div class="profile-name">{{ userStore.userInfo?.nickname || '宠主' }}</div>
        <div class="profile-phone">{{ maskedPhone }}</div>
      </div>

      <!-- 功能列表 -->
      <div class="menu-section">
        <div class="menu-item" @click="$router.push('/pet/list')">
          <div class="menu-left">
            <el-icon color="#ff9800"><Stamp /></el-icon>
            <span>我的宠物</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="$router.push('/consult')">
          <div class="menu-left">
            <el-icon color="#4caf50"><ChatDotRound /></el-icon>
            <span>问诊记录</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
        <div class="menu-item" @click="$router.push('/settings')">
          <div class="menu-left">
            <el-icon color="#2196f3"><Setting /></el-icon>
            <span>账号设置</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <div class="menu-section">
        <div class="menu-item" @click="aboutDialog = true">
          <div class="menu-left">
            <el-icon color="#9c27b0"><InfoFilled /></el-icon>
            <span>关于我们</span>
          </div>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>

      <!-- 退出登录 -->
      <div class="logout-area">
        <el-button class="logout-btn" size="large" @click="handleLogout">退出登录</el-button>
      </div>

      <!-- 关于弹窗 -->
      <el-dialog v-model="aboutDialog" title="关于 PetCare" width="85%">
        <p style="line-height:1.8;font-size:14px;color:#666;">
          PetCare 是一款专业的宠物在线问诊平台，连接宠物主与资深兽医，提供图文/视频/语音在线问诊、电子处方、宠物健康管理等服务。
        </p>
        <p style="margin-top:10px;font-size:12px;color:#999;">版本 1.0.0</p>
      </el-dialog>
    </div>
  </MainLayout>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { ArrowRight, Stamp, ChatDotRound, Setting, InfoFilled } from '@element-plus/icons-vue'
import MainLayout from '@/layouts/MainLayout.vue'
import { useUserStore } from '@/stores/user'
import { avatarOf, onAvatarError } from '@/utils/avatar-helper'

const router = useRouter()
const userStore = useUserStore()
const aboutDialog = ref(false)

const maskedPhone = computed(() => {
  const p = userStore.userInfo?.phone || ''
  return p.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
})

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    userStore.logout()
    router.replace('/login')
  } catch { /* cancel */ }
}
</script>

<style scoped>
.profile-page { min-height: 100%; padding-bottom: 70px; }
.profile-header {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 32px 16px 24px; background: linear-gradient(135deg, var(--primary), var(--primary-light));
  color: #fff;
}
.profile-name { font-size: 18px; font-weight: 600; }
.profile-phone { font-size: 13px; opacity: 0.8; }
.menu-section { background: #fff; margin: 10px 0; }
.menu-item {
  display: flex; align-items: center; justify-content: space-between;
  padding: 14px 16px; border-bottom: 1px solid var(--border); cursor: pointer;
}
.menu-item:last-child { border-bottom: none; }
.menu-left { display: flex; align-items: center; gap: 10px; font-size: 14px; }
.logout-area { padding: 24px 16px; }
.logout-btn { width: 100%; }
</style>

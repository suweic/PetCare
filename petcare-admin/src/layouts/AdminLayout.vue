<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="sidebar-logo">
        <span class="logo-icon">🐾</span>
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">PetCare</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-sub-menu index="/doctor">
          <template #title>
            <el-icon><UserFilled /></el-icon>
            <span>医生管理</span>
          </template>
          <el-menu-item index="/doctor/audit">
            <el-icon><Checked /></el-icon>
            <span>医生审核</span>
          </el-menu-item>
          <el-menu-item index="/doctor/list">
            <el-icon><List /></el-icon>
            <span>医生列表</span>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/user/list">
          <el-icon><Avatar /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/consultation/list">
          <el-icon><ChatDotRound /></el-icon>
          <span>问诊管理</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 右侧主体 -->
    <div class="main-area" :class="{ collapsed: appStore.sidebarCollapsed }">
      <!-- 顶栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar()">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="topbar-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="user-name">{{ userStore.admin?.realName || '管理员' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区 (keep-alive) -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <keep-alive :include="cachedViewNames">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Odometer, UserFilled, Checked, List, Avatar,
  ChatDotRound, Fold, Expand, ArrowDown,
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { KEEP_ALIVE_NAMES } from '@/router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title as string)
const cachedViewNames = KEEP_ALIVE_NAMES

// 自动将当前路由加入 keep-alive 缓存
watch(
  () => route.name,
  (name) => {
    if (name && typeof name === 'string') {
      appStore.addCachedView(name)
    }
  },
  { immediate: true },
)

function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' }).then(() => {
      userStore.logout()
      router.replace('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* ===== 侧边栏 ===== */
.sidebar {
  width: var(--sidebar-width);
  background: #304156;
  transition: width 0.3s;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
}
.sidebar.collapsed {
  width: var(--sidebar-collapse-width);
}
.sidebar-logo {
  height: var(--topbar-height);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.logo-icon { font-size: 24px; }
.logo-text { white-space: nowrap; }
.sidebar :deep(.el-menu) {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

/* ===== 右侧主体 ===== */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 0;
  transition: margin-left 0.3s;
  overflow: hidden;
  min-width: 0;
}

/* ===== 顶栏 ===== */
.topbar {
  height: var(--topbar-height);
  background: #fff;
  border-bottom: 1px solid var(--border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
}
.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--text-regular);
}
.collapse-btn:hover { color: var(--primary); }
.topbar-right { display: flex; align-items: center; }
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-regular);
}
.user-name { max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* ===== 内容区 ===== */
.content {
  flex: 1;
  overflow-y: auto;
  background: var(--bg);
}
</style>

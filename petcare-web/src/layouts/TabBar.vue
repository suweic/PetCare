<template>
  <nav class="tabbar safe-bottom">
    <div
      v-for="tab in tabs"
      :key="tab.path"
      class="tabbar-item"
      :class="{ active: currentPath === tab.path }"
      @click="navigate(tab.path)"
    >
      <el-icon :size="20"><component :is="tab.icon" /></el-icon>
      <span>{{ tab.label }}</span>
    </div>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { HomeFilled, ChatDotRound, Plus, User } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const tabs = [
  { path: '/home', label: '首页', icon: HomeFilled },
  { path: '/consult', label: '问诊', icon: ChatDotRound },
  { path: '/doctor-list', label: '找医生', icon: Plus },
  { path: '/profile', label: '我的', icon: User },
]

const currentPath = computed(() => route.path)

function navigate(path: string) {
  router.push(path)
}
</script>

<style scoped>
.tabbar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 480px;
  height: 56px;
  display: flex;
  align-items: center;
  background: #fff;
  border-top: 1px solid var(--border);
  z-index: 200;
}
.tabbar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  color: var(--text-muted);
  cursor: pointer;
  transition: color 0.2s;
}
.tabbar-item.active {
  color: var(--primary);
}
</style>

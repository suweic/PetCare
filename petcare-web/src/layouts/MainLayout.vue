<template>
  <div class="main-layout">
    <slot />
    <TabBar v-if="showTab" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import TabBar from './TabBar.vue'

const route = useRoute()

// 仅在首页/问诊/医生列表/个人中心显示底部导航
const tabRoutes = ['/home', '/consult', '/doctor-list', '/profile']
const showTab = computed(() => tabRoutes.includes(route.path))
</script>

<style scoped>
.main-layout {
  min-height: 100%;
  background: var(--bg);
}
</style>

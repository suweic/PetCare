<template>
  <header class="navbar">
    <div class="navbar-left" @click="$emit('back')">
      <el-icon v-if="showBack"><ArrowLeft /></el-icon>
    </div>
    <span class="navbar-title">{{ title }}</span>
    <div class="navbar-right">
      <slot name="right" />
    </div>
  </header>
</template>

<script setup lang="ts">
defineProps<{ title: string; showBack?: boolean }>()
defineEmits<{ back: [] }>()
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 16px;
  background: #fff;
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
}
.navbar-left {
  width: 36px;
  display: flex;
  align-items: center;
  cursor: pointer;
}
.navbar-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.navbar-right {
  width: 36px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}
</style>

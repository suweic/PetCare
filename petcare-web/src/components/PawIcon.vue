<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 1024 1024"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
    :class="['paw-icon', { 'paw-spin': spin }]"
  >
    <!-- 主掌垫 -->
    <ellipse
      cx="512"
      cy="680"
      rx="180"
      ry="150"
      :fill="color"
    />
    <!-- 左下指垫 -->
    <ellipse
      cx="285"
      cy="540"
      rx="78"
      ry="95"
      :fill="color"
      transform="rotate(-25 285 540)"
    />
    <!-- 右下指垫 -->
    <ellipse
      cx="739"
      cy="540"
      rx="78"
      ry="95"
      :fill="color"
      transform="rotate(25 739 540)"
    />
    <!-- 左上指垫 -->
    <ellipse
      cx="240"
      cy="380"
      rx="68"
      ry="85"
      :fill="color"
      transform="rotate(-40 240 380)"
    />
    <!-- 右上指垫 -->
    <ellipse
      cx="784"
      cy="380"
      rx="68"
      ry="85"
      :fill="color"
      transform="rotate(40 784 380)"
    />
    <!-- 顶部中指垫 -->
    <ellipse
      cx="512"
      cy="350"
      rx="70"
      ry="85"
      :fill="color"
    />
  </svg>
</template>

<script setup lang="ts">
/**
 * 自绘爪子印图标
 * Element Plus Icons 包（@element-plus/icons-vue@2.3.2）并未提供 PawPrint 组件，
 * 本组件以纯 SVG 渲染 "宠物档案" 入口所需的爪印视觉，主题与 PetCare 品牌一致。
 */
withDefaults(
  defineProps<{
    size?: number | string
    color?: string
    spin?: boolean
  }>(),
  {
    size: 22,
    color: 'currentColor',
    spin: false,
  },
)
</script>

<style scoped>
.paw-icon {
  display: inline-block;
  vertical-align: middle;
}
.paw-spin {
  animation: paw-spin 2.4s linear infinite;
  transform-origin: 50% 50%;
}
@keyframes paw-spin {
  to { transform: rotate(360deg); }
}
</style>

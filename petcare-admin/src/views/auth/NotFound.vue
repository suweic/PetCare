<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" @click="$router.replace('/dashboard')">返回工作台</el-button>
  </div>
</template>

<style scoped>
.not-found {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--bg);
}
.not-found h1 { font-size: 80px; color: var(--primary); }
.not-found p { font-size: 16px; color: var(--text-secondary); margin: 8px 0 20px; }
</style>

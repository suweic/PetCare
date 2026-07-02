<template>
  <div class="not-found flex-center">
    <div>
      <h1>404</h1>
      <p>页面不存在</p>
      <el-button type="primary" @click="$router.replace('/home')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped>
.not-found {
  height: 100vh;
  text-align: center;
  flex-direction: column;
}
.not-found h1 {
  font-size: 64px;
  color: var(--primary);
}
.not-found p {
  font-size: 16px;
  color: var(--text-muted);
  margin: 8px 0 20px;
}
</style>

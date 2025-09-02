<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">第二页</h2>
      <p class="page-subtitle">这里是第二页的内容</p>
    </div>
    
    <div class="page-content">
      <div class="placeholder-content">
        <el-empty description="页面内容开发中..." />
      </div>
    </div>
  </div>
</template>

<script setup>
// 页面逻辑
</script>

<style scoped>
.page-container {
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  color: #1e293b;
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.page-subtitle {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.page-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  padding: 40px;
  text-align: center;
}

@media (max-width: 768px) {
  .page-container {
    padding: 16px;
  }
}
</style>

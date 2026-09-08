<template>
  <div class="error-wrap">
    <div class="error-inner">
      <div class="error-code">403</div>
      <div class="error-title">权限不足</div>
      <div class="error-desc">您没有权限访问此页面，请联系管理员授权</div>
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped>
.error-wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1E1B4B;
  position: relative;
  overflow: hidden;
}
.error-inner {
  text-align: center;
  position: relative;
  z-index: 1;
}
.error-code {
  font-size: 120px;
  font-weight: 800;
  color: rgba(167, 139, 250, 0.3);
  line-height: 1;
  margin-bottom: -20px;
}
.error-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 12px;
}
.error-desc {
  font-size: 15px;
  color: rgba(255,255,255,0.6);
  margin-bottom: 32px;
}
.error-wrap .el-button {
  background: #7C3AED;
  border-color: #7C3AED;
  border-radius: 10px;
  padding: 12px 32px;
  font-size: 15px;
  font-weight: 600;
}
.error-wrap .el-button:hover {
  background: #5B21B6;
  border-color: #5B21B6;
}
</style>

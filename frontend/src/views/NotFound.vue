<template>
  <div class="not-found">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <h2 class="error-title">页面不存在</h2>
      <p class="error-description">抱歉，您访问的页面不存在或已被删除</p>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.not-found {
  min-height: calc(100vh - 60px);
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f8f9fa;
}

.not-found-content {
  text-align: center;
  padding: 60px 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  max-width: 500px;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 20px;
  line-height: 1;
}

.error-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 15px;
}

.error-description {
  font-size: 16px;
  color: #606266;
  margin-bottom: 40px;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}
</style>

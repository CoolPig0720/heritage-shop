<template>
  <div class="dashboard">
    <h1 class="page-title">数据概览</h1>
    
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon" style="background: #409EFF">
          <el-icon :size="32"><Goods /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">56</div>
          <div class="stat-label">商品总数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: #67C23A">
          <el-icon :size="32"><ShoppingCart /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">234</div>
          <div class="stat-label">订单总数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: #E6A23C">
          <el-icon :size="32"><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">12</div>
          <div class="stat-label">待处理订单</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon" style="background: #F56C6C">
          <el-icon :size="32"><Money /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">¥45,678</div>
          <div class="stat-label">本月销售额</div>
        </div>
      </div>
    </div>
    
    <div class="charts-section">
      <el-card class="chart-card">
        <template #header>
          <span>销售趋势</span>
        </template>
        <div class="chart-placeholder">
          <el-empty description="图表区域" />
        </div>
      </el-card>
      
      <el-card class="chart-card">
        <template #header>
          <span>商品销量排行</span>
        </template>
        <div class="chart-placeholder">
          <el-empty description="图表区域" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { Goods, ShoppingCart, Document, Money } from '@element-plus/icons-vue'
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-card {
  background: #fff;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>

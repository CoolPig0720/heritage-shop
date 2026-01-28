<template>
  <div class="custom-orders">
    <h1 class="page-title">定制订单</h1>
    
    <el-card class="table-card">
      <div class="table-header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索订单"
          style="width: 300px"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="订单状态" style="width: 150px" clearable>
          <el-option label="全部" value="" />
          <el-option label="待处理" value="PENDING" />
          <el-option label="处理中" value="PROCESSING" />
          <el-option label="已完成" value="COMPLETED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </div>
      
      <el-table :data="orders" style="width: 100%">
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="customerName" label="客户姓名" width="120" />
        <el-table-column prop="productName" label="定制产品" />
        <el-table-column prop="requirements" label="需求描述" show-overflow-tooltip />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            ¥{{ row.amount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small">查看详情</el-button>
            <el-button v-if="row.status === 'PENDING'" type="success" link size="small">接单</el-button>
            <el-button v-if="row.status === 'PROCESSING'" type="warning" link size="small">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'

const searchKeyword = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

const orders = ref([
  { 
    id: 1, 
    orderNo: 'CO20240116001', 
    customerName: '张三', 
    productName: '定制剪纸', 
    requirements: '需要制作一幅关于家庭团圆的剪纸作品，尺寸为50x50cm',
    amount: 599, 
    status: 'PENDING', 
    createTime: '2024-01-16 10:00:00' 
  },
  { 
    id: 2, 
    orderNo: 'CO20240115002', 
    customerName: '李四', 
    productName: '定制蜡染', 
    requirements: '需要定制一条蓝色蜡染围巾，图案为山水画',
    amount: 899, 
    status: 'PROCESSING', 
    createTime: '2024-01-15 14:30:00' 
  },
  { 
    id: 3, 
    orderNo: 'CO20240114003', 
    customerName: '王五', 
    productName: '定制苏绣', 
    requirements: '需要苏绣团扇，图案为牡丹花',
    amount: 699, 
    status: 'COMPLETED', 
    createTime: '2024-01-14 09:15:00' 
  }
])

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'PROCESSING': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待处理',
    'PROCESSING': '处理中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}
</script>

<style scoped>
.custom-orders {
  padding: 20px;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 20px;
}

.table-card {
  background: #fff;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

<template>
  <div class="heritage">
    <h1 class="page-title">非遗文化管理</h1>
    
    <el-card class="table-card">
      <div class="table-header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索非遗项目"
          style="width: 300px"
          clearable
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加项目
        </el-button>
      </div>
      
      <el-table :data="heritageItems" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="项目名称" />
        <el-table-column prop="category" label="类别" width="120" />
        <el-table-column prop="origin" label="发源地" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link size="small">编辑</el-button>
            <el-button type="danger" link size="small">删除</el-button>
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
import { Search, Plus } from '@element-plus/icons-vue'

const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(100)

const heritageItems = ref([
  { id: 1, name: '剪纸艺术', category: '传统美术', origin: '陕西', status: 'ACTIVE', createTime: '2024-01-01' },
  { id: 2, name: '蜡染工艺', category: '传统技艺', origin: '贵州', status: 'ACTIVE', createTime: '2024-01-02' },
  { id: 3, name: '景泰蓝', category: '传统技艺', origin: '北京', status: 'ACTIVE', createTime: '2024-01-03' }
])

const handleAdd = () => {
  console.log('Add heritage item')
}
</script>

<style scoped>
.heritage {
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
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

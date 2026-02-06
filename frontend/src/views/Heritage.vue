<template>
  <div class="heritage">
    <div class="section-container">
      <div class="section-header">
        <h2 class="section-title">非遗文化</h2>
      </div>

      <el-card class="list-card" shadow="never">
        <div class="filter-row">
          <el-select
            v-model="selectedCategoryId"
            placeholder="类型"
            clearable
            filterable
            style="width: 240px"
            :loading="categoryLoading"
            @change="handleTypeChange"
            @clear="handleTypeChange"
          >
            <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>

          <el-input
            v-model="keyword"
            placeholder="关键词：项目名称"
            clearable
            style="width: 320px"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />

          <el-button type="primary" :loading="loading" @click="handleSearch">搜索</el-button>

          <div class="filter-spacer" />
          <span class="result-count">共 {{ total }} 条</span>
        </div>

        <el-table v-loading="loading" :data="records" style="width: 100%">
          <el-table-column label="序号" width="90" :index="indexMethod" type="index" />
          <el-table-column prop="categoryName" label="类型" width="180" />
          <el-table-column label="项目名称" min-width="220">
            <template #default="{ row }">
              <el-link type="primary" :underline="false" @click="goProject(row.id)">
                {{ row.name }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="传承人" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              <template v-if="row.inheritors && row.inheritors.length > 0">
                <span v-for="(p, idx) in row.inheritors" :key="p.id">
                  <el-link type="primary" :underline="false" @click="goInheritor(p.id)">{{ p.name }}</el-link>
                  <span v-if="idx < row.inheritors.length - 1">、</span>
                </span>
              </template>
              <span v-else>暂无</span>
            </template>
          </el-table-column>
          <el-table-column prop="applyUnit" label="申报单位" min-width="180" show-overflow-tooltip />
          <el-table-column prop="protectUnit" label="保护单位" min-width="180" show-overflow-tooltip />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="primary" @click="goProject(row.id)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="size"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getHeritageCategoryTree, pageHeritageProjects } from '@/api/heritage'

const router = useRouter()

const categoryLoading = ref(false)
const categories = ref([])
const selectedCategoryId = ref(null)

const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const records = ref([])

const flattenCategories = (nodes, depth = 0, out = []) => {
  const list = Array.isArray(nodes) ? nodes : []
  list.forEach((n) => {
    const name = (n?.name ?? '').toString()
    const prefix = depth > 0 ? `${'—'.repeat(depth)} ` : ''
    out.push({ value: n?.id, label: `${prefix}${name}` })
    if (Array.isArray(n?.children) && n.children.length > 0) {
      flattenCategories(n.children, depth + 1, out)
    }
  })
  return out
}

const categoryOptions = computed(() => flattenCategories(categories.value))

const loadCategories = async () => {
  categoryLoading.value = true
  try {
    const res = await getHeritageCategoryTree()
    categories.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    categories.value = []
  } finally {
    categoryLoading.value = false
  }
}

const loadProjects = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value
    }
    const kw = (keyword.value || '').trim()
    if (kw) params.keyword = kw
    if (selectedCategoryId.value) params.categoryId = selectedCategoryId.value

    const res = await pageHeritageProjects(params)
    const data = res.data || {}
    records.value = Array.isArray(data.records) ? data.records : []
    total.value = Number(data.total ?? 0)
  } catch (e) {
    records.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadProjects()
}

const handleTypeChange = () => {
  page.value = 1
  loadProjects()
}

const handleSizeChange = () => {
  page.value = 1
  loadProjects()
}

const handleCurrentChange = () => {
  loadProjects()
}

const goProject = (id) => {
  router.push(`/heritage/projects/${id}`)
}

const goInheritor = (id) => {
  router.push(`/heritage/inheritors/${id}`)
}

const indexMethod = (index) => (page.value - 1) * size.value + index + 1

onMounted(async () => {
  await loadCategories()
  await loadProjects()
})
</script>

<style scoped>
.heritage {
  padding-top: 60px;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}

.section-title {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.list-card {
  border-radius: 12px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.filter-spacer {
  flex: 1;
}

.result-count {
  color: #909399;
  font-size: 13px;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>

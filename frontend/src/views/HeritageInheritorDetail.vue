<template>
  <div class="inheritor-detail">
    <div class="section-container" v-loading="loading">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="backTo">{{ backLabel }}</el-breadcrumb-item>
        <el-breadcrumb-item>传承人详情</el-breadcrumb-item>
      </el-breadcrumb>

      <el-card class="detail-card" shadow="never">
        <div class="top">
          <el-avatar :size="96" :src="normalizeUrl(detail.photo)" class="avatar" />
          <div class="info">
            <h1 class="name">{{ detail.name || '传承人详情' }}</h1>
            <div class="meta">
              <el-tag v-if="detail.gender" type="info">{{ detail.gender }}</el-tag>
              <el-tag v-if="detail.nation" type="info">{{ detail.nation }}</el-tag>
              <span v-if="detail.birthDate" class="meta-item">{{ detail.birthDate }}</span>
            </div>
          </div>
        </div>

        <div v-if="detail.description" class="block">
          <h3 class="block-title">介绍</h3>
          <div class="text">{{ detail.description }}</div>
        </div>

        <div v-if="detail.projects.length > 0" class="block">
          <h3 class="block-title">相关项目</h3>
          <el-table :data="detail.projects" style="width: 100%">
            <el-table-column prop="id" label="ID" width="90" />
            <el-table-column label="项目名称" min-width="200">
              <template #default="{ row }">
                <el-link type="primary" :underline="false" @click="goProject(row.id)">
                  {{ row.name }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="categoryName" label="分类" width="160" />
            <el-table-column prop="applyUnit" label="申报单位" min-width="160" show-overflow-tooltip />
            <el-table-column prop="protectUnit" label="保护单位" min-width="160" show-overflow-tooltip />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button link type="primary" @click="goProject(row.id)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-empty v-if="!loading && !detail.id" description="未找到该传承人" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHeritageInheritorDetail } from '@/api/heritage'

const route = useRoute()
const router = useRouter()

const fromManage = computed(() => route.query?.from === 'manage')
const backLabel = computed(() => (fromManage.value ? '非遗管理' : '非遗文化'))
const backTo = computed(() => (fromManage.value ? '/manage/heritage' : '/heritage'))

const loading = ref(false)
const detail = ref({
  id: null,
  name: '',
  gender: '',
  birthDate: '',
  nation: '',
  photo: '',
  description: '',
  projects: []
})

const normalizeUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

const loadDetail = async (id) => {
  if (!id) return
  loading.value = true
  try {
    const res = await getHeritageInheritorDetail(id)
    const data = res.data || {}
    detail.value = {
      ...detail.value,
      ...data,
      projects: Array.isArray(data.projects) ? data.projects : []
    }
  } catch (e) {
    ElMessage.error('获取传承人详情失败')
    detail.value = {
      id: null,
      name: '',
      gender: '',
      birthDate: '',
      nation: '',
      photo: '',
      description: '',
      projects: []
    }
  } finally {
    loading.value = false
  }
}

const goProject = (id) => {
  const path = `/heritage/projects/${id}`
  if (fromManage.value) {
    router.push({ path, query: { from: 'manage' } })
    return
  }
  router.push(path)
}

onMounted(() => {
  loadDetail(route.params.id)
})

watch(
  () => route.params.id,
  (id) => {
    loadDetail(id)
  }
)
</script>

<style scoped>
.inheritor-detail {
  padding-top: 60px;
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.detail-card {
  margin-top: 14px;
  border-radius: 12px;
}

.top {
  display: flex;
  gap: 16px;
  align-items: center;
}

.name {
  margin: 0;
  font-size: 26px;
  color: #303133;
}

.meta {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
  color: #606266;
}

.meta-item {
  font-size: 14px;
}

.block {
  margin-top: 22px;
}

.block-title {
  margin: 0 0 10px;
  font-size: 16px;
  color: #303133;
}

.text {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>

<template>
  <div class="heritage-detail">
    <div class="section-container" v-loading="loading">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="backTo">{{ backLabel }}</el-breadcrumb-item>
        <el-breadcrumb-item>项目详情</el-breadcrumb-item>
      </el-breadcrumb>

      <el-card class="detail-card" shadow="never">
        <div class="header">
          <div class="title-area">
            <h1 class="title">{{ detail.name || '项目详情' }}</h1>
            <div class="meta">
              <el-tag v-if="detail.categoryName" type="info">{{ detail.categoryName }}</el-tag>
              <span v-if="detail.applyUnit" class="meta-item">申报单位：{{ detail.applyUnit }}</span>
              <span v-if="detail.protectUnit" class="meta-item">保护单位：{{ detail.protectUnit }}</span>
            </div>
          </div>
        </div>

        <div v-if="detail.description" class="block">
          <h3 class="block-title">项目介绍</h3>
          <div class="text">{{ detail.description }}</div>
        </div>

        <div v-if="detail.id" class="block">
          <div class="block-header">
            <h3 class="block-title">展示图</h3>
            <div v-if="canManageMedia" class="block-actions">
              <el-upload
                :action="uploadUrl"
                name="file"
                :headers="uploadHeaders"
                :show-file-list="false"
                multiple
                :before-upload="beforeUploadImage"
                :on-success="handleUploadSuccess('DISPLAY_IMAGE')"
              >
                <el-button size="small" type="primary">上传展示图</el-button>
              </el-upload>
            </div>
          </div>
          <div v-if="displayImages.length > 0" class="image-grid">
            <div v-for="img in displayImages" :key="img.id" class="media-item">
              <el-image
                :src="normalizeUrl(img.mediaUrl)"
                fit="cover"
                :preview-src-list="displayImagesPreview"
                preview-teleported
                class="image"
              />
              <el-button
                v-if="canManageMedia"
                class="media-delete"
                type="danger"
                size="small"
                @click.stop="deleteMedia(img)"
              >
                删除
              </el-button>
            </div>
          </div>
          <el-empty v-else description="暂无展示图" />
        </div>

        <div v-if="detail.id" class="block">
          <div class="block-header">
            <h3 class="block-title">工艺流程图</h3>
            <div v-if="canManageMedia" class="block-actions">
              <el-upload
                :action="uploadUrl"
                name="file"
                :headers="uploadHeaders"
                :show-file-list="false"
                multiple
                :before-upload="beforeUploadImage"
                :on-success="handleUploadSuccess('PROCESS_IMAGE')"
              >
                <el-button size="small" type="primary">上传流程图</el-button>
              </el-upload>
            </div>
          </div>
          <div v-if="processImages.length > 0" class="image-grid">
            <div v-for="img in processImages" :key="img.id" class="media-item">
              <el-image
                :src="normalizeUrl(img.mediaUrl)"
                fit="cover"
                :preview-src-list="processImagesPreview"
                preview-teleported
                class="image"
              />
              <el-button
                v-if="canManageMedia"
                class="media-delete"
                type="danger"
                size="small"
                @click.stop="deleteMedia(img)"
              >
                删除
              </el-button>
            </div>
          </div>
          <el-empty v-else description="暂无工艺流程图" />
        </div>

        <div v-if="detail.id" class="block">
          <div class="block-header">
            <h3 class="block-title">视频</h3>
            <div v-if="canManageMedia" class="block-actions">
              <el-upload
                :action="uploadUrl"
                name="file"
                :headers="uploadHeaders"
                :show-file-list="false"
                multiple
                :before-upload="beforeUploadVideo"
                :on-success="handleUploadSuccess('VIDEO')"
              >
                <el-button size="small" type="primary">上传视频</el-button>
              </el-upload>
            </div>
          </div>
          <div v-if="videos.length > 0" class="video-list">
            <div v-for="v in videos" :key="v.id" class="media-item">
              <video class="video" controls :src="normalizeUrl(v.mediaUrl)" />
              <el-button
                v-if="canManageMedia"
                class="media-delete"
                type="danger"
                size="small"
                @click.stop="deleteMedia(v)"
              >
                删除
              </el-button>
            </div>
          </div>
          <el-empty v-else description="暂无视频" />
        </div>

        <div class="block">
          <h3 class="block-title">相关传承人</h3>
          <div v-if="detail.inheritors.length > 0" class="inheritor-grid">
            <el-card
              v-for="p in detail.inheritors"
              :key="p.id"
              shadow="hover"
              class="inheritor-card"
              @click="goInheritor(p.id)"
            >
              <div class="inheritor-inner">
                <el-avatar :size="56" :src="normalizeUrl(p.photo)" />
                <div class="inheritor-info">
                  <div class="inheritor-name">{{ p.name }}</div>
                  <div class="inheritor-meta">
                    <span v-if="p.gender">{{ p.gender }}</span>
                    <span v-if="p.nation">{{ p.nation }}</span>
                    <span v-if="p.birthDate">{{ p.birthDate }}</span>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
          <el-empty v-else description="暂无传承人" />
        </div>

        <el-empty v-if="!loading && !detail.id" description="未找到该项目" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createAdminHeritageProjectMedia, deleteAdminHeritageProjectMedia, getHeritageProjectDetail } from '@/api/heritage'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const fromManage = computed(() => route.query?.from === 'manage')
const backLabel = computed(() => (fromManage.value ? '非遗管理' : '非遗文化'))
const backTo = computed(() => (fromManage.value ? '/manage/heritage' : '/heritage'))

const canManageMedia = computed(() => fromManage.value && userStore.userInfo?.role === 'ADMIN')

const loading = ref(false)
const detail = ref({
  id: null,
  categoryId: null,
  categoryName: '',
  name: '',
  description: '',
  applyUnit: '',
  protectUnit: '',
  medias: [],
  inheritors: []
})

const normalizeUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

const uploadUrl = 'http://localhost:8080/api/file/upload'
const uploadHeaders = computed(() => {
  const token = userStore.token || localStorage.getItem('token')
  if (!token) return {}
  return { Authorization: `Bearer ${token}` }
})

const displayImages = computed(() =>
  (detail.value.medias || []).filter((m) => m?.mediaType === 'DISPLAY_IMAGE')
)
const processImages = computed(() =>
  (detail.value.medias || []).filter((m) => m?.mediaType === 'PROCESS_IMAGE')
)
const videos = computed(() => (detail.value.medias || []).filter((m) => m?.mediaType === 'VIDEO'))

const displayImagesPreview = computed(() => displayImages.value.map((m) => normalizeUrl(m.mediaUrl)))
const processImagesPreview = computed(() => processImages.value.map((m) => normalizeUrl(m.mediaUrl)))

const beforeUploadImage = (file) => {
  if (!file?.type?.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return false
  }
  const maxSizeMb = 10
  if (file.size > maxSizeMb * 1024 * 1024) {
    ElMessage.error(`图片大小不能超过 ${maxSizeMb}MB`)
    return false
  }
  return true
}

const beforeUploadVideo = (file) => {
  if (!file?.type?.startsWith('video/')) {
    ElMessage.error('请上传视频文件')
    return false
  }
  const maxSizeMb = 200
  if (file.size > maxSizeMb * 1024 * 1024) {
    ElMessage.error(`视频大小不能超过 ${maxSizeMb}MB`)
    return false
  }
  return true
}

const handleUploadSuccess = (mediaType) => async (response) => {
  if (response?.code !== 200) {
    ElMessage.error(response?.message || '上传失败')
    return
  }
  const mediaUrl = response?.data || ''
  if (!detail.value.id) return

  try {
    await createAdminHeritageProjectMedia(detail.value.id, {
      mediaType,
      mediaUrl,
      sortOrder: 0
    })
    ElMessage.success('上传成功')
    loadDetail(detail.value.id)
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const deleteMedia = (media) => {
  if (!canManageMedia.value || !media?.id) return
  ElMessageBox.confirm('确定要删除该资源吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteAdminHeritageProjectMedia(media.id)
        ElMessage.success('删除成功')
        loadDetail(detail.value.id)
      } catch (e) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

const loadDetail = async (id) => {
  if (!id) return
  loading.value = true
  try {
    const res = await getHeritageProjectDetail(id)
    const data = res.data || {}
    detail.value = {
      ...detail.value,
      ...data,
      medias: Array.isArray(data.medias) ? data.medias : [],
      inheritors: Array.isArray(data.inheritors) ? data.inheritors : []
    }
  } catch (e) {
    ElMessage.error('获取项目详情失败')
    detail.value = {
      id: null,
      categoryId: null,
      categoryName: '',
      name: '',
      description: '',
      applyUnit: '',
      protectUnit: '',
      medias: [],
      inheritors: []
    }
  } finally {
    loading.value = false
  }
}

const goInheritor = (id) => {
  const path = `/heritage/inheritors/${id}`
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
.heritage-detail {
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

.title {
  margin: 0;
  font-size: 26px;
  color: #303133;
}

.meta {
  margin-top: 10px;
  display: flex;
  gap: 12px;
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

.block-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.block-actions {
  display: flex;
  justify-content: flex-end;
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

.image-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.image {
  width: 100%;
  height: 160px;
  border-radius: 10px;
  overflow: hidden;
}

.media-item {
  position: relative;
}

.media-delete {
  position: absolute;
  right: 10px;
  top: 10px;
}

.video-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}

.video {
  width: 100%;
  border-radius: 12px;
  background: #000;
}

.inheritor-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.inheritor-card {
  border-radius: 12px;
  cursor: pointer;
}

.inheritor-inner {
  display: flex;
  gap: 12px;
  align-items: center;
}

.inheritor-name {
  font-weight: 600;
  color: #303133;
}

.inheritor-meta {
  margin-top: 4px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  color: #909399;
  font-size: 12px;
}

@media (max-width: 1024px) {
  .image-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .inheritor-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .video-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .image-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .inheritor-grid {
    grid-template-columns: 1fr;
  }
}
</style>

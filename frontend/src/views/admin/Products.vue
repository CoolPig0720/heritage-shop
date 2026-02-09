<template>
  <div class="products">
    <h1 class="page-title">商品管理</h1>
    
    <el-card class="table-card">
      <div class="table-header">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品"
          class="search-input"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          添加商品
        </el-button>
      </div>
      
      <el-table v-loading="loading" :data="products" style="width: 100%">
        <el-table-column label="序号" width="80" align="center">
          <template #default="{ $index }">
            {{ (currentPage - 1) * pageSize + $index + 1 }}
          </template>
        </el-table-column>
        <el-table-column label="商品信息" min-width="280" align="center">
          <template #default="{ row }">
            <div class="product-info-cell">
              <el-image :src="getCoverUrl(row)" fit="cover" class="product-cover" />
              <div class="product-details">
                <div class="product-name">{{ row.name }}</div>
                <div class="product-meta">商家：{{ row.merchantName || '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" width="120" align="center">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              inline-prompt
              active-text="上架"
              inactive-text="下架"
              @change="val => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
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
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
          />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            :step="10"
            placeholder="不填默认 999"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="isEdit" label="溯源码" prop="traceCode">
          <el-input v-model="form.traceCode" placeholder="可选" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="溯源二维码" prop="traceQrUrl">
          <div class="upload-row">
            <el-upload
              :action="uploadUrl"
              name="file"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUploadTraceQr"
              :on-success="handleTraceQrSuccess"
            >
              <el-button size="small" type="primary">上传二维码</el-button>
            </el-upload>

            <div v-if="form.traceQrUrl" class="upload-preview">
              <el-image :src="normalizeUrl(form.traceQrUrl)" style="width: 64px; height: 64px" fit="cover" />
              <el-button size="small" @click="clearTraceQr">删除</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="isEdit" label="3D 模型" prop="model3dUrl">
          <div class="upload-row">
            <el-upload
              :action="uploadUrl"
              name="file"
              :headers="uploadHeaders"
              :show-file-list="false"
              :before-upload="beforeUploadModel"
              :on-success="handleModelSuccess"
            >
              <el-button size="small" type="primary">上传 3D 模型</el-button>
            </el-upload>

            <div v-if="form.model3dUrl" class="upload-preview">
              <el-link :href="normalizeUrl(form.model3dUrl)" target="_blank" type="primary">
                {{ uploadedModelName || getFileName(form.model3dUrl) }}
              </el-link>
              <el-button size="small" @click="modelPreviewVisible = !modelPreviewVisible">
                {{ modelPreviewVisible ? '收起预览' : '预览' }}
              </el-button>
              <el-button size="small" @click="clearModel">删除</el-button>
            </div>
            <div v-if="form.model3dUrl && modelPreviewVisible" class="model-preview">
              <div ref="modelPreviewRef" class="model-preview-canvas" />
              <div v-if="modelPreviewLoading" class="model-preview-loading">模型加载中...</div>
              <div v-if="modelPreviewError" class="model-preview-error">{{ modelPreviewError }}</div>
            </div>
          </div>
        </el-form-item>

        <el-form-item v-if="isEdit" label="商品图片">
          <div class="images-panel">
            <div class="images-toolbar">
              <el-upload
                :action="uploadUrl"
                name="file"
                :headers="uploadHeaders"
                :show-file-list="false"
                multiple
                :before-upload="beforeUploadProductImage"
                :on-success="handleProductImageUploadSuccess"
              >
                <el-button size="small" type="primary">上传图片</el-button>
              </el-upload>
              <el-button size="small" :loading="imagesLoading" @click="fetchProductImages">刷新</el-button>
            </div>

            <el-table
              v-if="productImages.length"
              v-loading="imagesLoading"
              :data="productImages"
              size="small"
              style="width: 100%"
            >
              <el-table-column label="预览" width="90">
                <template #default="{ row }">
                  <el-image :src="normalizeUrl(row.imageUrl)" style="width: 64px; height: 64px" fit="cover" />
                </template>
              </el-table-column>
              <el-table-column label="图片地址" min-width="220">
                <template #default="{ row }">
                  <el-link :href="normalizeUrl(row.imageUrl)" target="_blank" type="primary">
                    {{ getFileName(row.imageUrl) }}
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column label="封面" width="80">
                <template #default="{ row }">
                  <el-tag v-if="row.isCover === 1" type="success">封面</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="排序" width="120">
                <template #default="{ row }">
                  <el-input-number v-model="row.sortOrder" :min="0" :step="1" controls-position="right" />
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220">
                <template #default="{ row }">
                  <el-button link size="small" :disabled="row.isCover === 1" @click="setCoverImage(row)">
                    设为封面
                  </el-button>
                  <el-button link size="small" @click="saveImageRow(row)">保存</el-button>
                  <el-button link size="small" type="danger" @click="deleteImageRow(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无图片" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { Search, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import {
  addProductImages,
  createProduct,
  deleteProduct,
  deleteProductImage,
  listProductImages,
  pageProducts,
  updateProduct,
  updateProductImage,
  updateProductStatus
} from '@/api/product'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

const products = ref([])

const dialogVisible = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  name: '',
  description: '',
  price: null,
  status: 1,
  traceCode: '',
  traceQrUrl: '',
  model3dUrl: ''
})

const rules = {
  name: [{ required: true, message: '商品名称不能为空', trigger: 'blur' }],
  description: [{ required: true, message: '商品描述不能为空', trigger: 'blur' }]
}

const dialogTitle = computed(() => (isEdit.value ? '编辑商品' : '添加商品'))

const uploadUrl = 'http://localhost:8080/api/file/upload'
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const normalizeUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

const PLACEHOLDER_IMAGE =
  'data:image/svg+xml;charset=utf-8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="400" viewBox="0 0 600 400">
      <rect width="600" height="400" fill="#f5f7fa"/>
      <path d="M160 280l80-100 70 80 60-60 110 140H160z" fill="#dcdfe6"/>
      <circle cx="240" cy="160" r="28" fill="#dcdfe6"/>
      <text x="300" y="330" text-anchor="middle" font-size="18" fill="#909399">暂无图片</text>
    </svg>`
  )

const getCoverUrl = (row) => {
  const url = normalizeUrl(row?.coverImageUrl)
  return url || PLACEHOLDER_IMAGE
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const getFileName = (url) => {
  if (!url) return ''
  const parts = url.split('/')
  return parts[parts.length - 1] || url
}

const beforeUploadTraceQr = (file) => {
  if (!file?.type?.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return false
  }
  const maxSizeMb = 5
  if (file.size > maxSizeMb * 1024 * 1024) {
    ElMessage.error(`图片大小不能超过 ${maxSizeMb}MB`)
    return false
  }
  return true
}

const beforeUploadModel = (file) => {
  const name = (file?.name || '').toLowerCase()
  const is3d = name.endsWith('.glb') || name.endsWith('.gltf')
  if (!is3d) {
    ElMessage.error('请上传 .glb 或 .gltf 文件')
    return false
  }
  const maxSizeMb = 50
  if (file.size > maxSizeMb * 1024 * 1024) {
    ElMessage.error(`文件大小不能超过 ${maxSizeMb}MB`)
    return false
  }
  return true
}

const beforeUploadProductImage = (file) => {
  if (!file?.type?.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return false
  }
  const maxSizeMb = 5
  if (file.size > maxSizeMb * 1024 * 1024) {
    ElMessage.error(`图片大小不能超过 ${maxSizeMb}MB`)
    return false
  }
  return true
}

const handleTraceQrSuccess = (response) => {
  if (response?.code === 200) {
    form.traceQrUrl = response.data || ''
    ElMessage.success('上传成功')
    return
  }
  ElMessage.error(response?.message || '上传失败')
}

const handleModelSuccess = async (response, uploadFile) => {
  if (response?.code === 200) {
    form.model3dUrl = response.data || ''
    uploadedModelName.value = uploadFile?.name || ''
    modelPreviewVisible.value = true
    if (isEdit.value && form.id) {
      try {
        await updateProduct(form.id, { model3dUrl: form.model3dUrl })
        ElMessage.success('上传并保存成功')
      } catch (e) {
        ElMessage.success('上传成功')
        ElMessage.error('保存 3D 模型失败')
      }
      return
    }
    ElMessage.success('上传成功')
    return
  }
  ElMessage.error(response?.message || '上传失败')
}

const clearTraceQr = () => {
  form.traceQrUrl = ''
}

const clearModel = async () => {
  form.model3dUrl = ''
  uploadedModelName.value = ''
  modelPreviewVisible.value = false
  destroyModelPreview()
  if (isEdit.value && form.id) {
    try {
      await updateProduct(form.id, { model3dUrl: '' })
      ElMessage.success('已删除')
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }
}

const productImages = ref([])
const imagesLoading = ref(false)

const uploadedModelName = ref('')
const modelPreviewVisible = ref(false)
const modelPreviewRef = ref()
const modelPreviewLoading = ref(false)
const modelPreviewError = ref('')
let modelPreview = null

const destroyModelPreview = () => {
  if (!modelPreview) return
  try {
    if (modelPreview.animationId) {
      cancelAnimationFrame(modelPreview.animationId)
    }
    if (modelPreview.onResize) {
      window.removeEventListener('resize', modelPreview.onResize)
    }
    if (modelPreview.controls) {
      modelPreview.controls.dispose()
    }
    if (modelPreview.renderer) {
      modelPreview.renderer.dispose()
      if (modelPreview.renderer.domElement?.parentNode) {
        modelPreview.renderer.domElement.parentNode.removeChild(modelPreview.renderer.domElement)
      }
    }
    if (modelPreview.scene) {
      modelPreview.scene.traverse((obj) => {
        if (obj?.geometry) {
          obj.geometry.dispose?.()
        }
        if (obj?.material) {
          const materials = Array.isArray(obj.material) ? obj.material : [obj.material]
          materials.forEach((m) => {
            if (!m) return
            Object.keys(m).forEach((k) => {
              const v = m[k]
              if (v && v.isTexture) v.dispose?.()
            })
            m.dispose?.()
          })
        }
      })
    }
  } finally {
    modelPreview = null
    modelPreviewLoading.value = false
  }
}

const initModelPreview = async () => {
  const container = modelPreviewRef.value
  if (!container) return
  const modelUrl = normalizeUrl(form.model3dUrl)
  if (!modelUrl) return

  destroyModelPreview()
  modelPreviewLoading.value = true
  modelPreviewError.value = ''

  const width = container.clientWidth || 520
  const height = 260

  const scene = new THREE.Scene()
  scene.background = new THREE.Color('#f5f7fa')

  const camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 5000)
  camera.position.set(0, 1.2, 3)

  const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2))
  renderer.setSize(width, height)
  container.innerHTML = ''
  container.appendChild(renderer.domElement)

  const controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true

  const hemi = new THREE.HemisphereLight(0xffffff, 0x444444, 1.0)
  scene.add(hemi)
  const dir = new THREE.DirectionalLight(0xffffff, 1.0)
  dir.position.set(3, 5, 2)
  scene.add(dir)

  const loader = new GLTFLoader()

  const fitCameraToObject = (obj3d) => {
    const box = new THREE.Box3().setFromObject(obj3d)
    if (!isFinite(box.min.x) || !isFinite(box.max.x)) return
    const size = box.getSize(new THREE.Vector3())
    const center = box.getCenter(new THREE.Vector3())

    controls.target.copy(center)
    const maxDim = Math.max(size.x, size.y, size.z) || 1
    const fov = (camera.fov * Math.PI) / 180
    const distance = Math.abs((maxDim / 2) / Math.tan(fov / 2)) * 1.6

    const dir = new THREE.Vector3(1, 0.8, 1).normalize()
    camera.position.copy(center.clone().add(dir.multiplyScalar(distance)))
    camera.near = distance / 100
    camera.far = distance * 100
    camera.updateProjectionMatrix()
    controls.update()
  }

  const onResize = () => {
    const w = container.clientWidth || width
    const h = height
    camera.aspect = w / h
    camera.updateProjectionMatrix()
    renderer.setSize(w, h)
  }
  window.addEventListener('resize', onResize)

  modelPreview = {
    scene,
    camera,
    renderer,
    controls,
    animationId: null,
    onResize
  }

  loader.load(
    modelUrl,
    (gltf) => {
      const model = gltf.scene || gltf.scenes?.[0]
      if (model) {
        scene.add(model)
        fitCameraToObject(model)
      }
      modelPreviewLoading.value = false
    },
    undefined,
    (err) => {
      modelPreviewLoading.value = false
      modelPreviewError.value = err?.message || '模型加载失败'
    }
  )

  const animate = () => {
    if (!modelPreview) return
    modelPreview.controls?.update()
    modelPreview.renderer.render(modelPreview.scene, modelPreview.camera)
    modelPreview.animationId = requestAnimationFrame(animate)
  }
  animate()
}

watch(
  () => dialogVisible.value,
  (open) => {
    if (!open) {
      modelPreviewVisible.value = false
      destroyModelPreview()
    }
  }
)

watch(
  [() => dialogVisible.value, () => form.model3dUrl, () => modelPreviewVisible.value],
  async ([open, url, visible]) => {
    if (!open || !visible || !url) {
      destroyModelPreview()
      return
    }
    await nextTick()
    await initModelPreview()
  }
)

const fetchProductImages = async () => {
  if (!form.id) return
  imagesLoading.value = true
  try {
    const res = await listProductImages(form.id)
    productImages.value = res.data || []
  } catch (e) {
    productImages.value = []
  } finally {
    imagesLoading.value = false
  }
}

const handleProductImageUploadSuccess = async (response) => {
  if (response?.code !== 200) {
    ElMessage.error(response?.message || '上传失败')
    return
  }
  const url = response.data
  if (!url) {
    ElMessage.error('上传失败')
    return
  }
  try {
    await addProductImages(form.id, { imageUrls: [url], setFirstAsCover: true })
    ElMessage.success('上传成功')
    await fetchProductImages()
  } catch (e) {
    ElMessage.error('保存图片失败')
  }
}

const setCoverImage = async (row) => {
  if (!row?.id) return
  await updateProductImage(row.id, { isCover: 1 })
  ElMessage.success('已设置封面')
  await fetchProductImages()
}

const saveImageRow = async (row) => {
  if (!row?.id) return
  await updateProductImage(row.id, { sortOrder: row.sortOrder ?? 0 })
  ElMessage.success('已保存')
  await fetchProductImages()
}

const deleteImageRow = async (row) => {
  if (!row?.id) return
  await ElMessageBox.confirm('确认删除该图片吗？', '提示', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await deleteProductImage(row.id)
  ElMessage.success('删除成功')
  await fetchProductImages()
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.description = ''
  form.price = null
  form.status = 1
  form.traceCode = ''
  form.traceQrUrl = ''
  form.model3dUrl = ''
  productImages.value = []
}

const fetchProducts = async () => {
  loading.value = true
  try {
    const res = await pageProducts({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined
    })
    products.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  currentPage.value = 1
  await fetchProducts()
}

const handleCurrentChange = async (page) => {
  currentPage.value = page
  await fetchProducts()
}

const handleSizeChange = async (size) => {
  pageSize.value = size
  currentPage.value = 1
  await fetchProducts()
}

const handleAdd = async () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

const handleEdit = async (row) => {
  isEdit.value = true
  resetForm()
  form.id = row.id
  form.name = row.name || ''
  form.description = row.description || ''
  form.price = row.price ?? null
  form.status = row.status ?? 1
  form.traceCode = row.traceCode || ''
  form.traceQrUrl = row.traceQrUrl || ''
  form.model3dUrl = row.model3dUrl || ''
  uploadedModelName.value = ''
  modelPreviewVisible.value = false
  dialogVisible.value = true
  await nextTick()
  formRef.value?.clearValidate()
  await fetchProductImages()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate()
  saving.value = true
  try {
    if (!isEdit.value) {
      const payload = {
        name: form.name,
        description: form.description
      }
      if (form.price !== null && form.price !== undefined) {
        payload.price = form.price
      }
      await createProduct(payload)
      ElMessage.success('添加成功')
    } else {
      const payload = {
        name: form.name,
        description: form.description,
        status: form.status
      }
      if (form.price !== null && form.price !== undefined) {
        payload.price = form.price
      }
      if (form.traceCode !== undefined) payload.traceCode = form.traceCode
      if (form.traceQrUrl !== undefined) payload.traceQrUrl = form.traceQrUrl
      if (form.model3dUrl !== undefined) payload.model3dUrl = form.model3dUrl
      await updateProduct(form.id, payload)
      ElMessage.success('保存成功')
    }
    dialogVisible.value = false
    await fetchProducts()
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除商品「${row.name}」吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消'
  })
  await deleteProduct(row.id)
  ElMessage.success('删除成功')
  await fetchProducts()
}

const handleStatusChange = async (row, val) => {
  const old = row.status === 1 ? 0 : 1
  try {
    await updateProductStatus(row.id, { status: val })
    ElMessage.success(val === 1 ? '已上架' : '已下架')
  } catch (e) {
    row.status = old
  }
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.products {
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
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 14px;
  flex-wrap: wrap;
}

.search-input {
  width: 320px;
  max-width: 100%;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.product-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  justify-content: center;
}

.product-cover {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  overflow: hidden;
  flex: 0 0 50px;
}

.product-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  text-align: left;
}

.product-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.product-meta {
  font-size: 12px;
  color: #909399;
}

.upload-row {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.model-preview {
  width: 100%;
  margin-top: 10px;
  position: relative;
}

.model-preview-canvas {
  width: 100%;
  height: 260px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.model-preview-loading {
  position: absolute;
  left: 12px;
  top: 12px;
  padding: 6px 10px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border-radius: 6px;
  font-size: 12px;
}

.model-preview-error {
  margin-top: 8px;
  color: #f56c6c;
  font-size: 12px;
}

.images-panel {
  width: 100%;
}

.images-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

:deep(.el-table) {
  border-radius: 10px;
  overflow: hidden;
}

:deep(.el-table__header th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>

<template>
  <div class="product-detail">
    <div class="section-container" v-loading="loading">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="backTo">{{ backLabel }}</el-breadcrumb-item>
        <el-breadcrumb-item>商品详情</el-breadcrumb-item>
      </el-breadcrumb>

      <el-card class="detail-card" shadow="never">
        <div class="detail-container">
          <div class="detail-left">
            <el-carousel v-if="product.imageUrls.length > 0" height="400px" indicator-position="outside">
              <el-carousel-item v-for="url in product.imageUrls" :key="url">
                <el-image :src="url" fit="cover" style="width: 100%; height: 400px" />
              </el-carousel-item>
            </el-carousel>
            <el-image v-else :src="product.coverImageUrl" fit="contain" style="width: 100%; height: 400px" />
          </div>
          <div class="detail-right">
            <h1 class="title">{{ product.name }}</h1>
            <p class="price">¥{{ product.price }}</p>
            <div class="info">
              <p><span>状态：</span>{{ product.status === 1 ? '上架' : '下架' }}</p>
              <p v-if="product.traceCode"><span>溯源码：</span>{{ product.traceCode }}</p>
              <p v-if="product.model3dUrl">
                <span>3D 模型：</span>
                <el-link :href="product.model3dUrl" target="_blank" type="primary">查看</el-link>
              </p>
            </div>
            <div class="description">
              <h3>商品描述</h3>
              <p>{{ product.description }}</p>
            </div>
            <div class="model3d">
              <div class="section-header">
                <h3>3D 模型预览</h3>
                <el-link v-if="product.model3dUrl" :href="product.model3dUrl" target="_blank" type="primary">
                  新窗口查看
                </el-link>
              </div>
              <div v-if="product.model3dUrl" class="model3d-panel">
                <div ref="modelPreviewRef" class="model3d-canvas" />
                <div v-if="modelPreviewLoading" class="model3d-loading">模型加载中...</div>
                <div v-if="modelPreviewError" class="model3d-error">{{ modelPreviewError }}</div>
              </div>
              <div v-else class="model3d-empty">
                <el-empty description="暂无 3D 模型" />
              </div>
            </div>
            <div class="trace">
              <h3>溯源二维码</h3>
              <div class="trace-box">
                <el-image
                  v-if="product.traceQrUrl"
                  :src="product.traceQrUrl"
                  fit="contain"
                  style="width: 180px; height: 180px"
                />
                <el-empty v-else description="暂无溯源二维码" :image-size="80" />
              </div>
            </div>
            <div class="actions">
              <el-input-number v-model="quantity" :min="1" :max="99" />
              <el-button type="primary" @click="addToCart">加入购物车</el-button>
              <el-button @click="buyNow">立即购买</el-button>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import * as THREE from 'three'
import { GLTFLoader } from 'three/examples/jsm/loaders/GLTFLoader.js'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { getProductDetail } from '@/api/shop'
import { addCartItem } from '@/api/cart'

const route = useRoute()
const router = useRouter()

const quantity = ref(1)
const loading = ref(false)
const modelPreviewRef = ref()
const modelPreviewLoading = ref(false)
const modelPreviewError = ref('')
let modelPreview = null

const backTo = computed(() => {
  const from = route.query?.from
  if (typeof from === 'string' && from.startsWith('/')) return from
  return '/products'
})

const backLabel = computed(() => {
  const from = route.query?.from
  if (typeof from === 'string') {
    if (from.startsWith('/home')) return '首页'
    if (from.startsWith('/products')) return '商品列表'
    if (from.startsWith('/cart')) return '购物车'
    if (from.startsWith('/orders')) return '我的订单'
  }
  return '商品列表'
})

const PLACEHOLDER_IMAGE =
  'data:image/svg+xml;charset=utf-8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="800" height="600" viewBox="0 0 800 600">
      <rect width="800" height="600" fill="#f5f7fa"/>
      <path d="M210 420l120-150 100 110 80-80 160 200H210z" fill="#dcdfe6"/>
      <circle cx="320" cy="240" r="38" fill="#dcdfe6"/>
      <text x="400" y="500" text-anchor="middle" font-size="22" fill="#909399">暂无图片</text>
    </svg>`
  )

const product = ref({
  id: null,
  name: '',
  description: '',
  price: 0,
  status: 1,
  traceCode: '',
  traceQrUrl: '',
  model3dUrl: '',
  coverImageUrl: PLACEHOLDER_IMAGE,
  imageUrls: []
})

const normalizeUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

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
  const modelUrl = product.value?.model3dUrl
  if (!container || !modelUrl) return

  destroyModelPreview()
  modelPreviewLoading.value = true
  modelPreviewError.value = ''

  const width = container.clientWidth || 520
  const height = 320

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

    const direction = new THREE.Vector3(1, 0.8, 1).normalize()
    camera.position.copy(center.clone().add(direction.multiplyScalar(distance)))
    camera.near = distance / 100
    camera.far = distance * 100
    camera.updateProjectionMatrix()
    controls.update()
  }

  const onResize = () => {
    const w = container.clientWidth || width
    camera.aspect = w / height
    camera.updateProjectionMatrix()
    renderer.setSize(w, height)
  }
  window.addEventListener('resize', onResize)

  modelPreview = { scene, camera, renderer, controls, animationId: null, onResize }

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

const loadDetail = async (id) => {
  if (!id) return
  loading.value = true
  try {
    const res = await getProductDetail(id)
    const data = res.data || {}
    product.value = {
      ...data,
      coverImageUrl: normalizeUrl(data.coverImageUrl) || PLACEHOLDER_IMAGE,
      traceQrUrl: data.traceQrUrl ? normalizeUrl(data.traceQrUrl) : '',
      model3dUrl: data.model3dUrl ? normalizeUrl(data.model3dUrl) : '',
      imageUrls: Array.isArray(data.imageUrls) ? data.imageUrls.map(normalizeUrl) : []
    }
    await nextTick()
    await initModelPreview()
  } catch (e) {
    ElMessage.error('获取商品详情失败')
    product.value = {
      id: null,
      name: '',
      description: '',
      price: 0,
      status: 1,
      traceCode: '',
      traceQrUrl: '',
      model3dUrl: '',
      coverImageUrl: PLACEHOLDER_IMAGE,
      imageUrls: []
    }
    destroyModelPreview()
  } finally {
    loading.value = false
  }
}

const addToCart = async () => {
  if (!product.value?.id) return
  try {
    await addCartItem({ productId: product.value.id, quantity: quantity.value })
    ElMessage.success('已加入购物车')
  } catch (e) {
    ElMessage.error('加入购物车失败')
  }
}

const buyNow = async () => {
  if (!product.value?.id) return
  try {
    await addCartItem({ productId: product.value.id, quantity: quantity.value })
    router.push('/cart')
  } catch (e) {
    ElMessage.error('加入购物车失败')
  }
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

watch(
  () => product.value.model3dUrl,
  async (url) => {
    if (!url) {
      destroyModelPreview()
      return
    }
    await nextTick()
    await initModelPreview()
  }
)

onBeforeUnmount(() => {
  destroyModelPreview()
})
</script>

<style scoped>
.product-detail {
  padding: 40px 20px;
  min-height: calc(100vh - 60px);
  background: var(--app-bg);
}

.section-container {
  max-width: var(--app-max-width);
  margin: 0 auto;
}

.detail-card {
  border-radius: var(--app-radius);
  border: 1px solid var(--app-border);
  overflow: hidden;
  margin-top: 14px;
}

.detail-card :deep(.el-card__body) {
  padding: 24px;
}

.detail-container {
  display: flex;
  gap: 32px;
}

.detail-left {
  flex: 1;
  min-width: 0;
}

.detail-left .el-image {
  width: 100%;
  height: 400px;
}

.detail-right {
  flex: 1;
  min-width: 0;
}

.detail-right .title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 20px;
}

.detail-right .price {
  font-size: 32px;
  color: #F56C6C;
  font-weight: bold;
  margin-bottom: 20px;
}

.detail-right .info {
  margin-bottom: 20px;
}

.detail-right .info p {
  margin: 10px 0;
  color: #606266;
}

.detail-right .info span {
  color: #909399;
  margin-right: 10px;
}

.detail-right .description {
  margin-bottom: 30px;
}

.detail-right .description h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 10px;
}

.detail-right .description p {
  color: #606266;
  line-height: 1.8;
}

.detail-right .actions {
  display: flex;
  gap: 20px;
  align-items: center;
}

.detail-right .model3d {
  margin-bottom: 20px;
}

.detail-right .section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.detail-right .model3d-panel {
  position: relative;
}

.detail-right .model3d-canvas {
  width: 100%;
  height: 320px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.detail-right .model3d-loading {
  position: absolute;
  left: 12px;
  top: 12px;
  padding: 6px 10px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border-radius: 6px;
  font-size: 12px;
}

.detail-right .model3d-error {
  margin-top: 8px;
  color: #f56c6c;
  font-size: 12px;
}

.detail-right .model3d-empty {
  padding: 8px 0;
}

.detail-right .trace {
  margin-bottom: 20px;
}

.detail-right .trace h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 10px;
}

.detail-right .trace-box {
  width: 180px;
  height: 180px;
  border: 1px dashed #dcdfe6;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafafa;
}

@media (max-width: 960px) {
  .detail-container {
    flex-direction: column;
    gap: 18px;
  }
  .detail-card :deep(.el-card__body) {
    padding: 16px;
  }
  .detail-right .actions {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>

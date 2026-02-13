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
            <el-carousel v-if="product.imageUrls.length > 0" height="500px" indicator-position="outside">
              <el-carousel-item v-for="url in product.imageUrls" :key="url">
                <el-image :src="url" fit="cover" style="width: 100%; height: 500px" :preview-src-list="product.imageUrls" />
              </el-carousel-item>
            </el-carousel>
            <el-image v-else :src="product.coverImageUrl" fit="contain" style="width: 100%; height: 500px" :preview-src-list="[product.coverImageUrl]" />
          </div>
          
          <div class="detail-right">
            <div class="product-header">
              <h1 class="title">{{ product.name }}</h1>
              <div class="price-status-row">
                <div class="price-row">
                  <span class="currency">¥</span>
                  <span class="price">{{ product.price }}</span>
                </div>
                <el-tag :type="product.status === 1 ? 'success' : 'info'" effect="plain" class="status-tag">
                  {{ product.status === 1 ? '上架销售中' : '已下架' }}
                </el-tag>
              </div>
            </div>

            <div class="product-description-preview">
              <h3>商品简介</h3>
              <p>{{ product.description }}</p>
            </div>

            <div class="digital-assets-buttons">
              <el-button v-if="product.traceQrUrl" @click="qrDialogVisible = true">
                <el-icon><View /></el-icon> 查看溯源二维码
              </el-button>
              <el-button v-if="product.model3dUrl" @click="modelDialogVisible = true">
                <el-icon><View /></el-icon> 查看3D模型
              </el-button>
            </div>

            <div class="product-actions">
              <div class="quantity-selector">
                <span class="label">数量</span>
                <el-input-number v-model="quantity" :min="1" :max="99" />
              </div>
              <div class="action-buttons">
                <el-button type="primary" size="default" class="buy-btn" @click="addToCart">加入购物车</el-button>
                <el-button type="danger" size="default" class="buy-btn" plain @click="buyNow">立即购买</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- Traceability QR Code Dialog -->
      <el-dialog v-model="qrDialogVisible" title="商品溯源信息" width="600px" align-center>
        <div class="dialog-content">
          <div class="qr-display">
             <el-image :src="product.traceQrUrl" fit="contain" class="qr-dialog-image" />
             <p class="dialog-hint">溯源码：{{ product.traceCode }}</p>
          </div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="qrDialogVisible = false">关闭</el-button>
            <el-button type="primary" @click="handleDownload(product.traceQrUrl, `${product.name}-trace-qr.png`)">
              <el-icon><Download /></el-icon> 下载二维码
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 3D Model Dialog -->
      <el-dialog v-model="modelDialogVisible" title="3D 模型预览" width="800px" align-center class="model-dialog">
        <div class="dialog-content">
          <model-viewer
            class="model3d-viewer"
            :src="product.model3dUrl"
            :poster="product.coverImageUrl"
            :alt="product.name"
            camera-controls
            auto-rotate
            shadow-intensity="1"
            exposure="1"
            touch-action="pan-y"
            @load="handleModelLoad"
            @error="handleModelError"
          />
          <div v-if="modelViewerLoading" class="model3d-loading">模型加载中...</div>
          <div v-if="modelViewerError" class="model3d-error">{{ modelViewerError }}</div>
        </div>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="modelDialogVisible = false">关闭</el-button>
            <el-button type="primary" @click="handleDownload(product.model3dUrl, `${product.name}-model.glb`)">
              <el-icon><Download /></el-icon> 下载模型
            </el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail } from '@/api/shop'
import { addCartItem } from '@/api/cart'
import { FullScreen, Picture as IconPicture, TopRight, View, Download } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const quantity = ref(1)
const loading = ref(false)
const modelViewerLoading = ref(false)
const modelViewerError = ref('')
const qrDialogVisible = ref(false)
const modelDialogVisible = ref(false)

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

const handleModelLoad = () => {
  modelViewerLoading.value = false
  modelViewerError.value = ''
}

const handleModelError = (e) => {
  modelViewerLoading.value = false
  modelViewerError.value = e?.detail?.message || '模型加载失败'
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
    modelViewerLoading.value = !!product.value.model3dUrl
    modelViewerError.value = ''
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
    modelViewerLoading.value = false
    modelViewerError.value = ''
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
  (url) => {
    modelViewerLoading.value = !!url
    modelViewerError.value = ''
  }
)

const handleDownload = (url, filename) => {
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
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
  padding: 0;
}

.detail-container {
  display: flex;
  background: #fff;
  border-bottom: 1px solid var(--app-border);
}

.detail-left {
  flex: 0 0 500px;
  width: 500px;
  border-right: 1px solid var(--app-border);
}

.detail-left .el-image {
  width: 100%;
  height: 500px;
}

.detail-right {
  flex: 1;
  padding: 32px 40px;
  display: flex;
  flex-direction: column;
}

.product-header {
  margin-bottom: 24px;
}

.title {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.3;
  margin-bottom: 16px;
}

.price-status-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.price-row {
  display: flex;
  align-items: baseline;
  color: #f56c6c;
}

.status-tag {
  height: 28px;
  padding: 0 12px;
}

.currency {
  font-size: 20px;
  margin-right: 4px;
}

.price {
  font-size: 36px;
  font-weight: 700;
}

.product-description-preview {
  margin-bottom: 24px;
  color: #606266;
  line-height: 1.6;
}

.product-description-preview h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
}

.digital-assets-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 32px;
}

.qr-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px 0;
}

.qr-dialog-image {
  width: 380px;
  height: 380px;
}

.dialog-hint {
  color: #606266;
  font-size: 14px;
}

.model-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.dialog-content {
  position: relative;
}

.product-actions {
  margin-top: auto;
  padding-top: 32px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #f7f9fc;
  border-radius: 12px;
  padding: 10px 14px;
  border: 1px solid #e6eef9;
  width: fit-content;
  --el-color-primary: #e0e6ef;
  --el-input-focus-border-color: #e0e6ef;
  --el-input-hover-border-color: #e0e6ef;
}

.quantity-selector .label {
  font-size: 14px;
  color: #606266;
  font-weight: 600;
}

.quantity-selector :deep(.el-input-number) {
  box-shadow: none;
}

.quantity-selector :deep(.el-input-number:focus-within) {
  box-shadow: none;
}

.quantity-selector :deep(.el-input-number.is-controls-right:focus-within .el-input__wrapper),
.quantity-selector :deep(.el-input-number:focus-within .el-input__wrapper) {
  box-shadow: none;
  border-color: #e0e6ef;
}

.quantity-selector :deep(.el-input-number),
.quantity-selector :deep(.el-input-number.is-controls-right) {
  border-color: #e0e6ef;
  box-shadow: none;
}

.quantity-selector :deep(.el-input-number__wrap),
.quantity-selector :deep(.el-input-number__wrap:focus),
.quantity-selector :deep(.el-input-number__wrap:focus-visible) {
  outline: none;
  box-shadow: none;
}

.quantity-selector :deep(.el-input-number__decrease),
.quantity-selector :deep(.el-input-number__increase) {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #fff;
  border: 1px solid #e0e6ef;
  color: #5f6b7a;
}

.quantity-selector :deep(.el-input-number__decrease:hover),
.quantity-selector :deep(.el-input-number__increase:hover) {
  border-color: #e0e6ef;
  color: #5f6b7a;
}

.quantity-selector :deep(.el-input-number__decrease:focus),
.quantity-selector :deep(.el-input-number__increase:focus),
.quantity-selector :deep(.el-input-number__decrease:focus-visible),
.quantity-selector :deep(.el-input-number__increase:focus-visible) {
  outline: none;
  border-color: #e0e6ef;
  box-shadow: none;
}

.quantity-selector :deep(.el-input-number__decrease:active),
.quantity-selector :deep(.el-input-number__increase:active) {
  border-color: #e0e6ef;
  box-shadow: none;
}

.quantity-selector :deep(.el-input-number__input) {
  height: 32px;
  line-height: 32px;
  font-weight: 600;
  color: #2c3e50;
}

.quantity-selector :deep(.el-input__wrapper.is-focus) {
  box-shadow: none;
  border-color: #e0e6ef;
}

.quantity-selector :deep(.el-input__wrapper) {
  box-shadow: none;
}

.quantity-selector :deep(.el-input__wrapper:hover) {
  border-color: #e0e6ef;
}

.quantity-selector :deep(.el-input-number__decrease.is-disabled),
.quantity-selector :deep(.el-input-number__increase.is-disabled) {
  border-color: #e0e6ef;
  color: #c0c4cc;
}

.action-buttons {
  display: flex;
  gap: 16px;
}

.buy-btn {
  flex: 1;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.5px;
  border-radius: 12px;
  box-shadow: 0 10px 20px rgba(64, 158, 255, 0.2);
}

.buy-btn.is-plain {
  box-shadow: none;
  border-width: 1px;
}

.buy-btn:hover {
  transform: translateY(-1px);
}

/* Product Content Tabs */
.product-content {
  padding: 20px 40px 40px;
  min-height: 400px;
}

.content-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  height: 50px;
  line-height: 50px;
}

.description-content {
  padding: 20px 0;
  color: #303133;
  line-height: 1.8;
  font-size: 15px;
}

/* Digital Experience Tab */
.digital-container {
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.digital-section {
  width: 100%;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  border-left: 4px solid #409EFF;
  padding-left: 12px;
}

.section-title h3 {
  font-size: 18px;
  color: #303133;
  margin: 0;
}

.model-wrapper {
  position: relative;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.model3d-viewer {
  width: 100%;
  height: 500px;
  background: radial-gradient(circle at center, #2d3748 0%, #1a202c 100%);
}

.model3d-loading {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  background: rgba(0,0,0,0.6);
  padding: 8px 16px;
  border-radius: 4px;
}

.model3d-error {
  color: #f56c6c;
  text-align: center;
  padding: 20px;
}

.trace-wrapper {
  display: flex;
  justify-content: flex-start;
}

.trace-card {
  background: #f8f9fa;
  padding: 24px;
  border-radius: 12px;
  text-align: center;
  border: 1px solid #ebeef5;
}

.qr-container {
  position: relative;
  width: 160px;
  height: 160px;
  margin: 0 auto 12px;
  background: #fff;
  padding: 8px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  cursor: pointer;
}

.trace-qr-img {
  width: 100%;
  height: 100%;
}

.qr-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.6);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 8px;
  gap: 4px;
}

.qr-container:hover .qr-overlay {
  opacity: 1;
}

.trace-hint {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.qr-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

@media (max-width: 960px) {
  .detail-container {
    flex-direction: column;
  }
  
  .detail-left {
    flex: none;
    width: 100%;
    border-right: none;
    border-bottom: 1px solid var(--app-border);
  }
  
  .detail-left .el-image,
  .detail-left .el-carousel {
    height: 360px !important;
  }
  
  .detail-right {
    padding: 24px;
  }
  
  .product-content {
    padding: 20px;
  }
  
  .model3d-viewer {
    height: 300px;
  }
}
</style>

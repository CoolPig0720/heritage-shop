<template>
  <div class="product-detail">
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
        <h1>{{ product.name }}</h1>
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
        <div v-if="product.traceQrUrl" class="trace">
          <h3>溯源二维码</h3>
          <el-image :src="product.traceQrUrl" fit="contain" style="width: 180px; height: 180px" />
        </div>
        <div class="actions">
          <el-input-number v-model="quantity" :min="1" :max="99" />
          <el-button type="primary" @click="addToCart">加入购物车</el-button>
          <el-button @click="buyNow">立即购买</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail } from '@/api/shop'

const route = useRoute()

const quantity = ref(1)
const loading = ref(false)

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
  } finally {
    loading.value = false
  }
}

const addToCart = () => {
  ElMessage.success('已加入购物车')
}

const buyNow = () => {
  ElMessage.success('跳转到订单确认页面')
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
.product-detail {
  padding: 20px;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  gap: 40px;
  background: #fff;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.detail-left {
  flex: 1;
}

.detail-left .el-image {
  width: 100%;
  height: 400px;
}

.detail-right {
  flex: 1;
}

.detail-right h1 {
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

.detail-right .trace {
  margin-bottom: 20px;
}

.detail-right .trace h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 10px;
}
</style>

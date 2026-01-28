<template>
  <div class="shop-products">
    <div class="section-container">
      <div class="section-header">
        <h2 class="section-title">商品列表</h2>
        <el-button type="primary" :loading="loading" @click="refreshProducts">换一批</el-button>
      </div>

      <el-card class="filter-card" shadow="never">
        <div class="filter-row">
          <el-input
            v-model="keyword"
            placeholder="搜索商品名称/描述"
            clearable
            style="width: 280px"
          />

          <div class="price-range">
            <span class="label">价格</span>
            <el-input-number v-model="minPrice" :min="0" :precision="2" :step="10" controls-position="right" />
            <span class="separator">-</span>
            <el-input-number v-model="maxPrice" :min="0" :precision="2" :step="10" controls-position="right" />
          </div>

          <el-select v-model="sort" style="width: 140px" placeholder="排序">
            <el-option label="默认排序" value="default" />
            <el-option label="价格从低到高" value="priceAsc" />
            <el-option label="价格从高到低" value="priceDesc" />
          </el-select>

          <el-switch v-model="hasImageOnly" active-text="仅看有图" inactive-text="全部" />

          <div class="filter-actions">
            <el-button @click="resetFilters">重置</el-button>
            <span class="result-count">共 {{ filteredProducts.length }} 件</span>
          </div>
        </div>
      </el-card>

      <div v-loading="loading" class="product-grid">
        <div
          v-for="product in filteredProducts"
          :key="product.id"
          class="product-card"
          @click="goToDetail(product.id)"
        >
          <div class="product-image">
            <img :src="getCover(product)" :alt="product.name" />
          </div>
          <div class="product-info">
            <h3 class="product-name">{{ product.name }}</h3>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-footer">
              <span class="price">¥{{ product.price }}</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="!loading && products.length === 0" class="empty">
        <el-empty description="暂无商品" />
      </div>
      <div v-else-if="!loading && filteredProducts.length === 0" class="empty">
        <el-empty description="未找到符合条件的商品" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getRecommendProducts } from '@/api/shop'

const router = useRouter()

const loading = ref(false)
const products = ref([])

const keyword = ref('')
const minPrice = ref(null)
const maxPrice = ref(null)
const sort = ref('default')
const hasImageOnly = ref(false)

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

const normalizeUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

const refreshProducts = async () => {
  loading.value = true
  try {
    const res = await getRecommendProducts({ count: 50 })
    products.value = res.data || []
  } finally {
    loading.value = false
  }
}

watch([minPrice, maxPrice], ([min, max]) => {
  if (min === null || min === undefined) return
  if (max === null || max === undefined) return
  const minNum = Number(min)
  const maxNum = Number(max)
  if (Number.isNaN(minNum) || Number.isNaN(maxNum)) return
  if (minNum > maxNum) {
    minPrice.value = maxNum
    maxPrice.value = minNum
  }
})

const filteredProducts = computed(() => {
  let list = Array.isArray(products.value) ? [...products.value] : []

  const kw = (keyword.value || '').trim().toLowerCase()
  if (kw) {
    list = list.filter((p) => {
      const name = (p?.name || '').toString().toLowerCase()
      const desc = (p?.description || '').toString().toLowerCase()
      return name.includes(kw) || desc.includes(kw)
    })
  }

  if (hasImageOnly.value) {
    list = list.filter((p) => !!normalizeUrl(p?.coverImageUrl))
  }

  const min = minPrice.value
  if (min !== null && min !== undefined && min !== '') {
    const minNum = Number(min)
    if (!Number.isNaN(minNum)) {
      list = list.filter((p) => Number(p?.price ?? 0) >= minNum)
    }
  }

  const max = maxPrice.value
  if (max !== null && max !== undefined && max !== '') {
    const maxNum = Number(max)
    if (!Number.isNaN(maxNum)) {
      list = list.filter((p) => Number(p?.price ?? 0) <= maxNum)
    }
  }

  if (sort.value === 'priceAsc') {
    list.sort((a, b) => Number(a?.price ?? 0) - Number(b?.price ?? 0))
  } else if (sort.value === 'priceDesc') {
    list.sort((a, b) => Number(b?.price ?? 0) - Number(a?.price ?? 0))
  }

  return list
})

const resetFilters = () => {
  keyword.value = ''
  minPrice.value = null
  maxPrice.value = null
  sort.value = 'default'
  hasImageOnly.value = false
}

const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

const getCover = (product) => {
  const url = normalizeUrl(product?.coverImageUrl)
  return url || PLACEHOLDER_IMAGE
}

onMounted(() => {
  refreshProducts()
})
</script>

<style scoped>
.shop-products {
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

.filter-card {
  margin-bottom: 16px;
  border-radius: 12px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.price-range {
  display: flex;
  align-items: center;
  gap: 10px;
}

.price-range .label {
  color: #606266;
  font-size: 14px;
}

.price-range .separator {
  color: #909399;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
}

.result-count {
  color: #909399;
  font-size: 13px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.product-image {
  height: 180px;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  padding: 16px;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 13px;
  color: #909399;
  margin: 0 0 12px;
  line-height: 1.6;
  height: 42px;
  overflow: hidden;
}

.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #F56C6C;
}

.empty {
  margin-top: 40px;
}

@media (max-width: 1200px) {
  .product-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 900px) {
  .product-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>

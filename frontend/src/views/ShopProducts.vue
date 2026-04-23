<template>
  <div class="shop-products">
    <div class="section-container">
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
            <el-input-number
              v-model="minPrice"
              :min="0"
              :precision="2"
              :step="10"
              controls-position="right"
            />
            <span class="separator">-</span>
            <el-input-number
              v-model="maxPrice"
              :min="0"
              :precision="2"
              :step="10"
              controls-position="right"
            />
          </div>

          <el-select v-model="sort" style="width: 140px" placeholder="排序">
            <el-option label="默认排序" value="default" />
            <el-option label="价格从低到高" value="priceAsc" />
            <el-option label="价格从高到低" value="priceDesc" />
          </el-select>

          <el-switch
            v-model="hasImageOnly"
            active-text="仅看有图"
            inactive-text="全部"
          />

          <div class="filter-actions">
            <el-button @click="resetFilters">重置</el-button>
            <span class="result-count"
              >共 {{ filteredProducts.length }} 件</span
            >
          </div>
        </div>
      </el-card>

      <div v-loading="loading" class="product-grid">
        <div
          v-for="product in pagedProducts"
          :key="product.id"
          class="product-card"
          @click="goToDetail(product.id)"
        >
          <div class="product-image">
            <img :src="getCover(product)" :alt="product.name" />
          </div>
          <div class="product-info">
            <h3 class="product-name"><TransText :text="product.name" /></h3>
            <p class="product-desc"><TransText :text="product.description" :enabled="!!product.description" />{{ !product.description ? "暂无描述" : "" }}</p>
            <div class="product-footer">
              <span class="price">¥{{ product.price }}</span>
              <span class="product-rating" v-if="product.ratingCount > 0">
                <el-rate
                  :model-value="product.avgRating"
                  disabled
                  :colors="['#F7BA2A', '#F7BA2A', '#F7BA2A']"
                  size="small"
                />
                <span class="rating-text">{{ product.avgRating }}</span>
              </span>
            </div>
          </div>
        </div>
      </div>

      <AppPagination
        v-if="filteredProducts.length > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="filteredProducts.length"
        :page-sizes="[8, 12, 20, 40]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />

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
import { computed, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getRecommendProducts } from "@/api/shop";
import { getImageUrl } from "@/config/api.js";
import AppPagination from "@/components/AppPagination.vue";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const products = ref([]);

const keyword = ref("");
const minPrice = ref(null);
const maxPrice = ref(null);
const sort = ref("default");
const hasImageOnly = ref(false);

const currentPage = ref(1);
const pageSize = ref(8);

const PLACEHOLDER_IMAGE =
  "data:image/svg+xml;charset=utf-8," +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="600" height="400" viewBox="0 0 600 400">
      <rect width="600" height="400" fill="#f5f7fa"/>
      <path d="M160 280l80-100 70 80 60-60 110 140H160z" fill="#dcdfe6"/>
      <circle cx="240" cy="160" r="28" fill="#dcdfe6"/>
      <text x="300" y="330" text-anchor="middle" font-size="18" fill="#909399">暂无图片</text>
    </svg>`,
  );

const normalizeUrl = (url) => {
  return getImageUrl(url);
};

const refreshProducts = async () => {
  loading.value = true;
  try {
    const res = await getRecommendProducts({ count: 50 });
    products.value = res.data || [];
  } finally {
    loading.value = false;
  }
};

watch([minPrice, maxPrice], ([min, max]) => {
  if (min === null || min === undefined) return;
  if (max === null || max === undefined) return;
  const minNum = Number(min);
  const maxNum = Number(max);
  if (Number.isNaN(minNum) || Number.isNaN(maxNum)) return;
  if (minNum > maxNum) {
    minPrice.value = maxNum;
    maxPrice.value = minNum;
  }
});

const filteredProducts = computed(() => {
  let list = Array.isArray(products.value) ? [...products.value] : [];

  const kw = (keyword.value || "").trim().toLowerCase();
  if (kw) {
    list = list.filter((p) => {
      const name = (p?.name || "").toString().toLowerCase();
      const desc = (p?.description || "").toString().toLowerCase();
      return name.includes(kw) || desc.includes(kw);
    });
  }

  if (hasImageOnly.value) {
    list = list.filter((p) => !!normalizeUrl(p?.coverImageUrl));
  }

  const min = minPrice.value;
  if (min !== null && min !== undefined && min !== "") {
    const minNum = Number(min);
    if (!Number.isNaN(minNum)) {
      list = list.filter((p) => Number(p?.price ?? 0) >= minNum);
    }
  }

  const max = maxPrice.value;
  if (max !== null && max !== undefined && max !== "") {
    const maxNum = Number(max);
    if (!Number.isNaN(maxNum)) {
      list = list.filter((p) => Number(p?.price ?? 0) <= maxNum);
    }
  }

  if (sort.value === "priceAsc") {
    list.sort((a, b) => Number(a?.price ?? 0) - Number(b?.price ?? 0));
  } else if (sort.value === "priceDesc") {
    list.sort((a, b) => Number(b?.price ?? 0) - Number(a?.price ?? 0));
  } else {
    // 默认排序：有评分 > 无评分，星级降序 > 评论数降序 > 创建时间降序
    list.sort((a, b) => {
      const aHasRating = (a?.ratingCount ?? 0) > 0 ? 1 : 0;
      const bHasRating = (b?.ratingCount ?? 0) > 0 ? 1 : 0;
      if (aHasRating !== bHasRating) return bHasRating - aHasRating;
      const ratingDiff = Number(b?.avgRating ?? 0) - Number(a?.avgRating ?? 0);
      if (ratingDiff !== 0) return ratingDiff;
      const countDiff = (b?.ratingCount ?? 0) - (a?.ratingCount ?? 0);
      if (countDiff !== 0) return countDiff;
      return new Date(b?.createTime ?? 0) - new Date(a?.createTime ?? 0);
    });
  }

  return list;
});

const pagedProducts = computed(() => {
  const list = filteredProducts.value;
  const start = (currentPage.value - 1) * pageSize.value;
  return list.slice(start, start + pageSize.value);
});

const handleSizeChange = (val) => {
  pageSize.value = val;
  currentPage.value = 1;
};

const handleCurrentChange = (val) => {
  currentPage.value = val;
};

const resetFilters = () => {
  keyword.value = "";
  minPrice.value = null;
  maxPrice.value = null;
  sort.value = "default";
  hasImageOnly.value = false;
  currentPage.value = 1;
};

const goToDetail = (id) => {
  router.push({ path: `/product/${id}`, query: { from: route.fullPath } });
};

const getCover = (product) => {
  const url = normalizeUrl(product?.coverImageUrl);
  return url || PLACEHOLDER_IMAGE;
};

onMounted(() => {
  refreshProducts();
});
</script>

<style scoped>
.shop-products {
  padding: 40px 20px;
  min-height: calc(100vh - 60px);
  background: var(--app-bg);
}

.section-container {
  max-width: var(--app-max-width);
  margin: 0 auto;
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
  border-radius: var(--app-radius);
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
  transition:
    transform 0.2s,
    box-shadow 0.2s;
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

.product-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.product-rating :deep(.el-rate) {
  height: 16px;
}

.product-rating :deep(.el-rate__icon) {
  font-size: 12px !important;
}

.rating-text {
  font-size: 12px;
  color: #f7ba2a;
  font-weight: 600;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #f56c6c;
}

.empty {
  margin-top: 40px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
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

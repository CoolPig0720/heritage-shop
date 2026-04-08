<template>
  <div class="home">
    <div class="banner">
      <div class="banner-content">
        <h1 class="banner-title">非遗商城</h1>
        <p class="banner-subtitle">传承非遗文化，定制专属记忆</p>
        <div class="banner-buttons">
          <el-button
            type="primary"
            size="large"
            @click="goToProducts"
            class="banner-btn"
          >
            <el-icon><ShoppingCart /></el-icon>
            浏览商品
          </el-button>
          <el-button size="large" @click="goToCustomize" class="banner-btn">
            <el-icon><MagicStick /></el-icon>
            智能定制
          </el-button>
        </div>
      </div>
    </div>

    <div class="features-section">
      <div class="section-container">
        <h2 class="section-title">特色功能</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="40"><Star /></el-icon>
            </div>
            <h3>非遗传承</h3>
            <p>精选国家级非遗项目，传承传统文化</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="40"><MagicStick /></el-icon>
            </div>
            <h3>AI定制</h3>
            <p>智能生成个性化定制方案</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="40"><Trophy /></el-icon>
            </div>
            <h3>品质保证</h3>
            <p>严选优质非遗产品，品质保障</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="40"><Van /></el-icon>
            </div>
            <h3>快速配送</h3>
            <p>全国配送，安全送达</p>
          </div>
        </div>
      </div>
    </div>

    <div class="products-section">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">热门商品</h2>
          <el-button type="primary" link @click="goToProducts"
            >查看更多 <el-icon><ArrowRight /></el-icon
          ></el-button>
        </div>
        <div v-loading="productsLoading" class="product-grid">
          <div
            v-for="product in products"
            :key="product.id"
            class="product-card"
            @click="goToDetail(product.id)"
          >
            <div class="product-image">
              <img :src="getCover(product)" :alt="product.name" />
              <div class="product-badge">推荐</div>
            </div>
            <div class="product-info">
              <h3 class="product-name"><TransText :text="product.name" /></h3>
              <p class="product-desc">
                <TransText
                  :text="product.description"
                  :enabled="!!product.description"
                />{{ !product.description ? "暂无描述" : "" }}
              </p>
              <div class="product-footer">
                <span class="price">¥{{ product.price }}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-if="!productsLoading && products.length === 0" class="empty">
          <el-empty description="暂无商品" />
        </div>
      </div>
    </div>

    <div class="heritage-section">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">非遗项目</h2>
          <el-button type="primary" link @click="goToHeritage"
            >查看全部 <el-icon><ArrowRight /></el-icon
          ></el-button>
        </div>
        <div v-loading="heritageLoading" class="heritage-grid">
          <div
            v-for="item in heritageItems"
            :key="item.id"
            class="heritage-card"
            @click="goToHeritage"
          >
            <div class="heritage-cover">
              <div class="heritage-icon">
                <el-icon :size="34"><CollectionTag /></el-icon>
              </div>
              <div class="heritage-title"><TransText :text="item.name" /></div>
            </div>
            <div class="heritage-info">
              <p><TransText :text="item.description" /></p>
              <div class="heritage-meta"><TransText :text="item.meta" /></div>
            </div>
          </div>
        </div>
        <div
          v-if="!heritageLoading && heritageItems.length === 0"
          class="empty"
        >
          <el-empty description="暂无数据" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  CollectionTag,
  ShoppingCart,
  MagicStick,
  Star,
  Trophy,
  Van,
  ArrowRight,
} from "@element-plus/icons-vue";
import { getRecommendProducts } from "@/api/shop";
import { getHeritageCategoryTree, pageHeritageProjects } from "@/api/heritage";
import { pageAiImageRecords } from "@/api/aiImage";
import { getImageUrl } from "@/config/api.js";

const route = useRoute();
const router = useRouter();

const productsLoading = ref(false);
const products = ref([]);

const heritageLoading = ref(false);
const heritageCategories = ref([]);
const heritageItems = computed(() => {
  const root = Array.isArray(heritageCategories.value)
    ? heritageCategories.value
    : [];
  return root.slice(0, 4).map((x) => {
    const name = (x?.name ?? "").toString() || "未命名分类";
    const childCount = Array.isArray(x?.children) ? x.children.length : 0;
    const desc = childCount > 0 ? `包含 ${childCount} 个子类` : "点击了解更多";
    const meta = childCount > 0 ? "更多分类内容" : "更多非遗内容";
    return { id: x?.id ?? name, name, description: desc, meta };
  });
});

const projectTotal = ref(0);
const myAiRecordTotal = ref(0);

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

const getCover = (product) => {
  const url = normalizeUrl(product?.coverImageUrl);
  return url || PLACEHOLDER_IMAGE;
};

const flattenCategories = (nodes, out = []) => {
  const list = Array.isArray(nodes) ? nodes : [];
  list.forEach((n) => {
    out.push(n);
    if (Array.isArray(n?.children) && n.children.length > 0) {
      flattenCategories(n.children, out);
    }
  });
  return out;
};

const categoryCount = computed(
  () => flattenCategories(heritageCategories.value).length,
);

const loadHomeData = async () => {
  productsLoading.value = true;
  heritageLoading.value = true;
  try {
    const results = await Promise.allSettled([
      getRecommendProducts({ count: 8 }),
      getHeritageCategoryTree(),
      pageHeritageProjects({ page: 1, size: 1 }),
      pageAiImageRecords({ page: 1, size: 1 }),
    ]);

    const [productRes, categoryRes, projectRes, aiRecordRes] = results.map(
      (r) => (r.status === "fulfilled" ? r.value : null),
    );

    products.value = Array.isArray(productRes?.data) ? productRes.data : [];
    heritageCategories.value = Array.isArray(categoryRes?.data)
      ? categoryRes.data
      : [];

    const projData = projectRes?.data || {};
    projectTotal.value = Number(projData.total ?? 0);

    const aiData = aiRecordRes?.data || {};
    myAiRecordTotal.value = Number(aiData.total ?? 0);
  } catch (e) {
    products.value = [];
    heritageCategories.value = [];
    projectTotal.value = 0;
    myAiRecordTotal.value = 0;
  } finally {
    productsLoading.value = false;
    heritageLoading.value = false;
  }
};

const goToProducts = () => {
  router.push("/products");
};

const goToCustomize = () => {
  router.push("/customize");
};

const goToHeritage = () => {
  router.push("/heritage");
};

const goToDetail = (id) => {
  router.push({ path: `/product/${id}`, query: { from: route.fullPath } });
};

onMounted(() => {
  loadHomeData();
});
</script>

<style scoped>
.home {
  padding-top: 0;
}

.banner {
  height: 600px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

.banner::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320"><path fill="rgba(255,255,255,0.1)" d="M0,96L48,112C96,128,192,160,288,186.7C384,213,480,235,576,213.3C672,192,768,128,864,128C960,128,1056,192,1152,208C1248,224,1344,192,1392,176L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path></svg>')
    no-repeat bottom;
  background-size: cover;
}

.banner-content {
  text-align: center;
  color: #fff;
  z-index: 1;
  padding: 0 20px;
}

.banner-title {
  font-size: 56px;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  animation: fadeInDown 1s ease-out;
}

.banner-subtitle {
  font-size: 28px;
  margin-bottom: 40px;
  opacity: 0.95;
  animation: fadeInUp 1s ease-out 0.3s both;
}

.banner-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  animation: fadeInUp 1s ease-out 0.6s both;
}

.banner-btn {
  height: 50px;
  padding: 0 30px;
  font-size: 18px;
  font-weight: bold;
  border-radius: 25px;
  transition: all 0.3s;
}

.banner-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.section-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.section-title {
  text-align: center;
  font-size: 36px;
  font-weight: bold;
  color: var(--text-color-primary);
  margin-bottom: 50px;
  position: relative;
}

.section-title::after {
  content: "";
  position: absolute;
  bottom: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 2px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.features-section {
  padding: 80px 0;
  background: var(--bg-page);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.feature-card {
  background: var(--card-bg);
  padding: 40px 30px;
  border-radius: 12px;
  text-align: center;
  box-shadow: var(--shadow-light);
  border: 1px solid var(--card-border);
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.feature-card h3 {
  font-size: 20px;
  margin-bottom: 12px;
  color: var(--text-color-primary);
}

.feature-card p {
  font-size: 14px;
  color: var(--text-color-secondary);
  line-height: 1.6;
}

.products-section {
  padding: 80px 0;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
}

.empty {
  padding: 30px 0;
}

.product-card {
  background: var(--card-bg);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-light);
  border: 1px solid var(--card-border);
  cursor: pointer;
  transition: all 0.3s;
}

.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.product-image {
  position: relative;
  height: 220px;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.1);
}

.product-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(245, 108, 108, 0.92);
  backdrop-filter: blur(6px);
  color: #fff;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.product-info {
  padding: 20px;
}

.product-name {
  font-size: 18px;
  margin-bottom: 8px;
  color: var(--text-color-primary);
  font-weight: 500;
}

.product-desc {
  font-size: 14px;
  color: var(--text-color-secondary);
  margin-bottom: 16px;
  line-height: 1.5;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.heritage-section {
  padding: 80px 0;
  background: var(--bg-page);
}

.heritage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.heritage-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s;
}

.heritage-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.heritage-cover {
  height: 160px;
  padding: 18px;
  color: #fff;
  background:
    radial-gradient(
      circle at 20% 20%,
      rgba(255, 255, 255, 0.35) 0%,
      rgba(255, 255, 255, 0) 60%
    ),
    linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.heritage-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
}

.heritage-title {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.heritage-info {
  padding: 20px;
}

.heritage-info p {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 10px;
}

.heritage-meta {
  font-size: 12px;
  color: #909399;
}

.stats-section {
  padding: 80px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 40px;
  text-align: center;
}

.stat-item {
  padding: 20px;
}

.stat-number {
  font-size: 48px;
  font-weight: bold;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 18px;
  opacity: 0.9;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 768px) {
  .banner-title {
    font-size: 36px;
  }

  .banner-subtitle {
    font-size: 20px;
  }

  .banner-buttons {
    flex-direction: column;
    align-items: center;
  }

  .banner-btn {
    width: 200px;
  }

  .section-title {
    font-size: 28px;
  }

  .features-grid,
  .product-grid,
  .heritage-grid {
    grid-template-columns: 1fr;
  }

  .stat-number {
    font-size: 36px;
  }

  .stat-label {
    font-size: 16px;
  }
}
</style>

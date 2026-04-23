<template>
  <div class="home">
    <div class="banner">
      <!-- 轮播背景图 -->
      <div class="banner-slides">
        <div
          v-for="(slide, index) in bannerSlides"
          :key="index"
          class="banner-slide"
          :class="{ active: currentSlide === index }"
          :style="{ backgroundImage: `url(${slide.image})` }"
        />
        <div class="banner-overlay" />
      </div>

      <!-- 装饰性飘动元素 -->
      <div class="floating-elements">
        <div
          v-for="n in 6"
          :key="n"
          class="floating-item"
          :class="`item-${n}`"
        />
      </div>

      <div class="banner-content">
        <h1 class="banner-title">
          <span
            v-for="(char, index) in titleChars"
            :key="index"
            class="title-char"
            :style="{ animationDelay: `${index * 0.1}s` }"
          >
            {{ char }}
          </span>
        </h1>
        <p class="banner-subtitle">
          <span class="subtitle-text">传承非遗文化，定制专属记忆</span>
        </p>
        <div class="banner-buttons">
          <el-button
            type="primary"
            size="large"
            @click="goToProducts"
            class="banner-btn primary-btn"
          >
            <el-icon><ShoppingCart /></el-icon>
            浏览商品
          </el-button>
          <el-button
            size="large"
            @click="goToCustomize"
            class="banner-btn secondary-btn"
          >
            <el-icon><MagicStick /></el-icon>
            智能定制
          </el-button>
        </div>
      </div>

      <!-- 轮播指示器 -->
      <div class="banner-indicators">
        <span
          v-for="(slide, index) in bannerSlides"
          :key="index"
          class="indicator"
          :class="{ active: currentSlide === index }"
          @click="goToSlide(index)"
        />
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
              <div class="product-badge">热门</div>
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

        <!-- 横向滚动容器 -->
        <div v-loading="heritageLoading" class="heritage-carousel-wrapper">
          <el-button
            v-if="heritageProjects.length > 0"
            class="scroll-btn scroll-left"
            circle
            @click="scrollHeritage('left')"
          >
            <el-icon><ArrowLeft /></el-icon>
          </el-button>

          <div ref="heritageScrollRef" class="heritage-scroll-container">
            <div
              v-for="item in heritageProjects"
              :key="item.id"
              class="heritage-card"
              @click="goToHeritageProject(item.id)"
            >
              <div
                class="heritage-image"
                :style="{ backgroundImage: `url(${item.image})` }"
              >
                <div class="heritage-category-badge">{{ item.categoryName }}</div>
                <div class="heritage-image-overlay">
                  <div class="heritage-icon">
                    <el-icon :size="28"><CollectionTag /></el-icon>
                  </div>
                </div>
              </div>
              <div class="heritage-content">
                <h3 class="heritage-name">
                  <TransText :text="item.name" />
                </h3>
                <div class="heritage-inheritor" v-if="item.firstInheritor">
                  <el-icon :size="14"><User /></el-icon>
                  <span>传承人：<TransText :text="item.firstInheritor" /></span>
                </div>
                <div class="heritage-inheritor" v-else>
                  <el-icon :size="14"><User /></el-icon>
                  <span class="no-inheritor">暂无传承人</span>
                </div>
                <div class="heritage-meta">
                  <span class="heritage-location" v-if="item.applyUnit">
                    <el-icon :size="14"><Location /></el-icon>
                    <TransText :text="item.applyUnit" />
                  </span>
                </div>
              </div>
            </div>
          </div>

          <el-button
            v-if="heritageProjects.length > 0"
            class="scroll-btn scroll-right"
            circle
            @click="scrollHeritage('right')"
          >
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>

        <div
          v-if="!heritageLoading && heritageProjects.length === 0"
          class="empty"
        >
          <el-empty description="暂无数据" />
        </div>
      </div>
    </div>

    <!-- 回到顶部按钮 -->
    <el-backtop
      :visibility-height="200"
      :right="40"
      :bottom="40"
      class="home-backtop"
    >
      <div class="backtop-content">
        <el-icon :size="20"><ArrowUp /></el-icon>
      </div>
    </el-backtop>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, onUnmounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import {
  CollectionTag,
  ShoppingCart,
  MagicStick,
  Star,
  Trophy,
  Van,
  ArrowRight,
  ArrowLeft,
  User,
  Location,
  ArrowUp,
} from "@element-plus/icons-vue";
import { getHotProducts } from "@/api/shop";
import { getHeritageCategoryTree, pageHeritageProjects } from "@/api/heritage";
import { pageAiImageRecords } from "@/api/aiImage";
import { getImageUrl } from "@/config/api.js";

const route = useRoute();
const router = useRouter();

// Banner轮播相关 - 使用本地图片
const bannerSlides = ref([
  {
    image: "/images/banner/banner1.jpg",
    title: "非遗商城",
  },
  {
    image: "/images/banner/banner2.webp",
    title: "传统技艺",
  },
  {
    image: "/images/banner/banner3.jpg",
    title: "匠心传承",
  },
]);
const currentSlide = ref(0);
let slideInterval = null;

const titleChars = computed(() => "非遗商城".split(""));

const goToSlide = (index) => {
  currentSlide.value = index;
  resetSlideInterval();
};

const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % bannerSlides.value.length;
};

const resetSlideInterval = () => {
  if (slideInterval) {
    clearInterval(slideInterval);
  }
  slideInterval = setInterval(nextSlide, 5000);
};

const productsLoading = ref(false);
const products = ref([]);

const heritageLoading = ref(false);
const heritageCategories = ref([]);
const heritageProjects = ref([]);
const heritageScrollRef = ref(null);

// 非遗项目封面图（在线图片）
const heritageImages = [
  "https://images.unsplash.com/photo-1567225557594-88d73e55f2cb?w=800&q=80",
  "https://images.unsplash.com/photo-1545562083-a600704fa46e?w=800&q=80",
  "https://images.unsplash.com/photo-1582738411706-bfc8e691d1c2?w=800&q=80",
  "https://images.unsplash.com/photo-1610701596007-11502861dcfa?w=800&q=80",
  "https://images.unsplash.com/photo-1606293926075-69a00febf780?w=800&q=80",
  "https://images.unsplash.com/photo-1544967082-d9d25d867d66?w=800&q=80",
];

const scrollHeritage = (direction) => {
  if (!heritageScrollRef.value) return;
  const scrollAmount = 320;
  const currentScroll = heritageScrollRef.value.scrollLeft;
  heritageScrollRef.value.scrollTo({
    left:
      direction === "left"
        ? currentScroll - scrollAmount
        : currentScroll + scrollAmount,
    behavior: "smooth",
  });
};

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
      getHotProducts({ count: 8 }),
      getHeritageCategoryTree(),
      pageHeritageProjects({ page: 1, size: 8 }),
      pageAiImageRecords({ page: 1, size: 1 }),
    ]);

    const [productRes, categoryRes, projectRes, aiRecordRes] = results.map(
      (r) => (r.status === "fulfilled" ? r.value : null),
    );

    products.value = Array.isArray(productRes?.data) ? productRes.data : [];
    heritageCategories.value = Array.isArray(categoryRes?.data)
      ? categoryRes.data
      : [];

    // 处理非遗项目数据
    const projData = projectRes?.data || {};
    const projects = Array.isArray(projData.records) ? projData.records : [];
    heritageProjects.value = projects.map((item, index) => ({
      ...item,
      image: heritageImages[index % heritageImages.length],
      firstInheritor: item.inheritors?.[0]?.name || null,
    }));
    projectTotal.value = Number(projData.total ?? 0);

    const aiData = aiRecordRes?.data || {};
    myAiRecordTotal.value = Number(aiData.total ?? 0);
  } catch (e) {
    products.value = [];
    heritageCategories.value = [];
    heritageProjects.value = [];
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

const goToHeritageProject = (id) => {
  router.push(`/heritage/projects/${id}`);
};

const goToDetail = (id) => {
  router.push({ path: `/product/${id}`, query: { from: route.fullPath } });
};

onMounted(() => {
  loadHomeData();
  resetSlideInterval();
});

onUnmounted(() => {
  if (slideInterval) {
    clearInterval(slideInterval);
  }
});
</script>

<style scoped>
.home {
  padding-top: 0;
}

.banner {
  height: 600px;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
}

/* 轮播背景图 */
.banner-slides {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.banner-slide {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
  opacity: 0;
  transition: opacity 1s ease-in-out;
  transform: scale(1.1);
}

.banner-slide.active {
  opacity: 1;
  animation: kenBurns 8s ease-out forwards;
}

.banner-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    135deg,
    rgba(60, 60, 70, 0.6) 0%,
    rgba(40, 40, 50, 0.7) 100%
  );
  z-index: 1;
}

/* 装饰性飘动元素 */
.floating-elements {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 2;
  pointer-events: none;
  overflow: hidden;
}

.floating-item {
  position: absolute;
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
  animation: float 15s infinite ease-in-out;
}

.floating-item::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 30px;
  height: 30px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.item-1 {
  top: 10%;
  left: 10%;
  animation-delay: 0s;
  width: 80px;
  height: 80px;
}

.item-2 {
  top: 20%;
  right: 15%;
  animation-delay: 2s;
  width: 50px;
  height: 50px;
}

.item-3 {
  bottom: 30%;
  left: 20%;
  animation-delay: 4s;
  width: 70px;
  height: 70px;
}

.item-4 {
  bottom: 20%;
  right: 25%;
  animation-delay: 6s;
  width: 45px;
  height: 45px;
}

.item-5 {
  top: 50%;
  left: 5%;
  animation-delay: 8s;
  width: 35px;
  height: 35px;
}

.item-6 {
  top: 40%;
  right: 10%;
  animation-delay: 10s;
  width: 55px;
  height: 55px;
}

.banner-content {
  text-align: center;
  color: #fff;
  z-index: 3;
  padding: 0 20px;
  position: relative;
}

/* 标题逐字动画 */
.banner-title {
  font-size: 56px;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
}

.title-char {
  display: inline-block;
  opacity: 0;
  transform: translateY(30px);
  animation: charFadeInUp 0.6s ease-out forwards;
}

.banner-subtitle {
  font-size: 28px;
  margin-bottom: 40px;
  opacity: 0;
  animation: fadeInUp 1s ease-out 0.6s forwards;
}

.subtitle-text {
  display: inline-block;
  position: relative;
}

.subtitle-text::after {
  content: "";
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 2px;
}

.banner-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  opacity: 0;
  animation: fadeInUp 1s ease-out 0.9s forwards;
}

.banner-btn {
  height: 50px;
  padding: 0 30px;
  font-size: 18px;
  font-weight: bold;
  border-radius: 25px;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.primary-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  border: none;
}

.primary-btn::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  transition: left 0.5s;
}

.primary-btn:hover::before {
  left: 100%;
}

.secondary-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 2px solid rgba(255, 255, 255, 0.8);
  color: #fff;
}

.secondary-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  border-color: #fff;
}

.banner-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
}

/* 轮播指示器 */
.banner-indicators {
  position: absolute;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 10px;
  z-index: 3;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: all 0.3s;
}

.indicator:hover {
  background: rgba(255, 255, 255, 0.7);
}

.indicator.active {
  background: #fff;
  transform: scale(1.2);
}

/* 动画定义 */
@keyframes kenBurns {
  0% {
    transform: scale(1.1);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0) translateX(0);
    opacity: 0.3;
  }
  25% {
    transform: translateY(-20px) translateX(10px);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-10px) translateX(-10px);
    opacity: 0.4;
  }
  75% {
    transform: translateY(-30px) translateX(5px);
    opacity: 0.5;
  }
}

@keyframes charFadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 40px;
}

.section-title {
  text-align: center;
  font-size: 32px;
  font-weight: bold;
  color: var(--text-color-primary);
  margin-bottom: 30px;
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
  margin-bottom: 24px;
}

.features-section {
  padding: 60px 0;
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
  padding: 60px 0;
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
  padding: 16px;
  display: flex;
  flex-direction: column;
  height: 160px;
}

.product-name {
  font-size: 16px;
  margin-bottom: 8px;
  color: var(--text-color-primary);
  font-weight: 500;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex-shrink: 0;
  height: 22px;
}

.product-desc {
  font-size: 13px;
  color: var(--text-color-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
  margin-bottom: 12px;
  height: 62px;
}

.product-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
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
  font-size: 24px;
  color: #f56c6c;
  font-weight: bold;
}

.heritage-section {
  padding: 60px 0;
  background: var(--bg-page);
}

/* 横向滚动容器 */
.heritage-carousel-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  gap: 16px;
}

.heritage-scroll-container {
  display: flex;
  gap: 24px;
  overflow-x: auto;
  overflow-y: hidden;
  scroll-behavior: smooth;
  scrollbar-width: none;
  -ms-overflow-style: none;
  padding: 10px 0;
  width: 100%;
}

.heritage-scroll-container::-webkit-scrollbar {
  display: none;
}

.scroll-btn {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  background: #fff;
  border: 1px solid var(--card-border);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  z-index: 2;
}

.scroll-btn:hover {
  background: var(--el-color-primary);
  color: #fff;
  border-color: var(--el-color-primary);
  transform: scale(1.1);
}

.scroll-left {
  left: 0;
}

.scroll-right {
  right: 0;
}

/* 非遗卡片 */
.heritage-card {
  flex: 0 0 280px;
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.heritage-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.heritage-card:hover .heritage-image {
  transform: scale(1.05);
}

.heritage-image {
  height: 180px;
  background-size: cover;
  background-position: center;
  position: relative;
  transition: transform 0.5s ease;
}

.heritage-level-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  z-index: 2;
}

.heritage-image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0.1) 0%,
    rgba(0, 0, 0, 0.4) 100%
  );
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  padding: 12px;
}

.heritage-image-overlay .heritage-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
}

.heritage-category-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  z-index: 2;
}

.heritage-content {
  padding: 20px;
}

.heritage-name {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-color-primary);
  margin-bottom: 12px;
  line-height: 1.4;
}

.heritage-inheritor {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-color-secondary);
  margin-bottom: 12px;
}

.heritage-inheritor .el-icon {
  color: var(--el-color-primary);
}

.heritage-inheritor .no-inheritor {
  color: #909399;
  font-style: italic;
}

.heritage-meta {
  display: flex;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--card-border);
}

.heritage-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-color-secondary);
}

.heritage-location .el-icon {
  color: var(--el-color-primary);
}

/* 回到顶部按钮 */
.home-backtop {
  z-index: 100;
}

.backtop-content {
  width: 50px;
  height: 50px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s;
}

.backtop-content:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
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

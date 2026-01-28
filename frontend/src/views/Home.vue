<template>
  <div class="home">
    <div class="banner">
      <div class="banner-content">
        <h1 class="banner-title">非遗产品智能定制商城</h1>
        <p class="banner-subtitle">传承千年文化，定制专属非遗</p>
        <div class="banner-buttons">
          <el-button type="primary" size="large" @click="goToProducts" class="banner-btn">
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
        <h2 class="section-title">我们的特色</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="40"><Star /></el-icon>
            </div>
            <h3>非遗精品</h3>
            <p>精选全国优质非遗产品，传承传统工艺</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="40"><MagicStick /></el-icon>
            </div>
            <h3>智能定制</h3>
            <p>AI辅助设计，打造独一无二的专属产品</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="40"><Trophy /></el-icon>
            </div>
            <h3>品质保证</h3>
            <p>严格品控，确保每件产品都是精品</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon :size="40"><Van /></el-icon>
            </div>
            <h3>快速配送</h3>
            <p>全国包邮，安全快捷的物流服务</p>
          </div>
        </div>
      </div>
    </div>
    
    <div class="products-section">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">热门商品</h2>
          <el-button type="primary" link @click="goToProducts">查看更多 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <div class="product-grid">
          <div v-for="product in products" :key="product.id" class="product-card" @click="goToDetail(product.id)">
            <div class="product-image">
              <img :src="product.coverImage || '/placeholder.jpg'" :alt="product.name" />
              <div class="product-badge" v-if="product.isHot">热销</div>
            </div>
            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>
              <div class="product-footer">
                <span class="price">¥{{ product.price }}</span>
                <el-button type="primary" size="small" @click.stop="addToCart(product)">
                  <el-icon><ShoppingCart /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="heritage-section">
      <div class="section-container">
        <div class="section-header">
          <h2 class="section-title">非遗文化</h2>
          <el-button type="primary" link @click="goToHeritage">了解更多 <el-icon><ArrowRight /></el-icon></el-button>
        </div>
        <div class="heritage-grid">
          <div v-for="item in heritageItems" :key="item.id" class="heritage-card" @click="goToHeritage">
            <div class="heritage-image">
              <img :src="item.image || '/placeholder.jpg'" :alt="item.name" />
            </div>
            <div class="heritage-info">
              <h3>{{ item.name }}</h3>
              <p>{{ item.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="stats-section">
      <div class="section-container">
        <div class="stats-grid">
          <div class="stat-item">
            <div class="stat-number">1000+</div>
            <div class="stat-label">非遗产品</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">500+</div>
            <div class="stat-label">传承大师</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">10000+</div>
            <div class="stat-label">满意客户</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">100+</div>
            <div class="stat-label">非遗项目</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart, MagicStick, Star, Trophy, Van, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()

const products = ref([
  { 
    id: 1, 
    name: '手工剪纸艺术画', 
    price: 299, 
    coverImage: '/placeholder.jpg',
    description: '传统剪纸工艺，精美绝伦',
    isHot: true
  },
  { 
    id: 2, 
    name: '传统蜡染围巾', 
    price: 599, 
    coverImage: '/placeholder.jpg',
    description: '天然染料，手工制作',
    isHot: true
  },
  { 
    id: 3, 
    name: '景泰蓝花瓶', 
    price: 1299, 
    coverImage: '/placeholder.jpg',
    description: '皇家工艺，精美绝伦',
    isHot: false
  },
  { 
    id: 4, 
    name: '苏绣团扇', 
    price: 399, 
    coverImage: '/placeholder.jpg',
    description: '苏州刺绣，细腻精美',
    isHot: true
  }
])

const heritageItems = ref([
  {
    id: 1,
    name: '剪纸艺术',
    description: '中国传统的民间艺术，用剪刀或刻刀在纸上剪刻花纹',
    image: '/placeholder.jpg'
  },
  {
    id: 2,
    name: '蜡染工艺',
    description: '中国传统纺织印染工艺，具有独特的民族风格',
    image: '/placeholder.jpg'
  },
  {
    id: 3,
    name: '景泰蓝',
    description: '铜胎掐丝珐琅，中国著名的特种工艺品',
    image: '/placeholder.jpg'
  },
  {
    id: 4,
    name: '苏绣',
    description: '苏州地区刺绣产品的总称，中国四大名绣之一',
    image: '/placeholder.jpg'
  }
])

const goToProducts = () => {
  router.push('/products')
}

const goToCustomize = () => {
  router.push('/customize')
}

const goToHeritage = () => {
  router.push('/heritage')
}

const goToDetail = (id) => {
  router.push(`/product/${id}`)
}

const addToCart = (product) => {
  ElMessage.success(`已将 ${product.name} 添加到购物车`)
}
</script>

<style scoped>
.home {
  padding-top: 60px;
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
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1440 320"><path fill="rgba(255,255,255,0.1)" d="M0,96L48,112C96,128,192,160,288,186.7C384,213,480,235,576,213.3C672,192,768,128,864,128C960,128,1056,192,1152,208C1248,224,1344,192,1392,176L1440,160L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path></svg>') no-repeat bottom;
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
  color: #303133;
  margin-bottom: 50px;
  position: relative;
}

.section-title::after {
  content: '';
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
  background: #f8f9fa;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.feature-card {
  background: #fff;
  padding: 40px 30px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
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
  color: #303133;
}

.feature-card p {
  font-size: 14px;
  color: #606266;
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

.product-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
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
  background: #F56C6C;
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
  color: #303133;
  font-weight: 500;
}

.product-desc {
  font-size: 14px;
  color: #909399;
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
  color: #F56C6C;
  font-weight: bold;
}

.heritage-section {
  padding: 80px 0;
  background: #f8f9fa;
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

.heritage-image {
  height: 200px;
  overflow: hidden;
}

.heritage-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.heritage-card:hover .heritage-image img {
  transform: scale(1.1);
}

.heritage-info {
  padding: 20px;
}

.heritage-info h3 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #303133;
  font-weight: 500;
}

.heritage-info p {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
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

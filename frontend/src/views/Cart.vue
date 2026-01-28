<template>
  <div class="cart">
    <div class="page-header">
      <h1>购物车</h1>
    </div>
    <div class="cart-container">
      <div v-if="cartItems.length > 0">
        <div class="cart-list">
          <div v-for="item in cartItems" :key="item.id" class="cart-item">
            <el-checkbox v-model="item.selected" />
            <img :src="item.coverImage" :alt="item.name" />
            <div class="item-info">
              <h3>{{ item.name }}</h3>
              <p class="price">¥{{ item.price }}</p>
            </div>
            <el-input-number v-model="item.quantity" :min="1" :max="item.stock" size="small" />
            <el-button type="danger" size="small" @click="removeItem(item.id)">删除</el-button>
          </div>
        </div>
        <div class="cart-footer">
          <div class="total">
            <span>总计：</span>
            <span class="price">¥{{ totalPrice }}</span>
          </div>
          <el-button type="primary" @click="checkout">结算</el-button>
        </div>
      </div>
      <el-empty v-else description="购物车为空" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'

const cartItems = ref([
  { id: 1, name: '手工剪纸艺术画', price: 299, quantity: 1, stock: 100, coverImage: '/placeholder.jpg', selected: true },
  { id: 2, name: '传统蜡染围巾', price: 599, quantity: 2, stock: 50, coverImage: '/placeholder.jpg', selected: true }
])

const totalPrice = computed(() => {
  return cartItems.value
    .filter(item => item.selected)
    .reduce((total, item) => total + item.price * item.quantity, 0)
})

const removeItem = (id) => {
  cartItems.value = cartItems.value.filter(item => item.id !== id)
  ElMessage.success('商品已删除')
}

const checkout = () => {
  ElMessage.success('跳转到订单确认页面')
}
</script>

<style scoped>
.cart {
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 36px;
  color: #303133;
}

.cart-container {
  max-width: 1000px;
  margin: 0 auto;
}

.cart-list {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.cart-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #EBEEF5;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  margin: 0 20px;
}

.item-info {
  flex: 1;
}

.item-info h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 5px;
}

.item-info .price {
  font-size: 18px;
  color: #F56C6C;
  font-weight: bold;
}

.cart-footer {
  margin-top: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cart-footer .total {
  font-size: 18px;
  color: #303133;
}

.cart-footer .total .price {
  font-size: 24px;
  color: #F56C6C;
  font-weight: bold;
  margin-left: 10px;
}
</style>

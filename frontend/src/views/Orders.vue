<template>
  <div class="orders">
    <div class="orders-container">
      <h1 class="page-title">我的订单</h1>
      
      <el-tabs v-model="activeTab" class="orders-tabs">
        <el-tab-pane label="全部订单" name="all">
          <div class="orders-list">
            <div v-for="order in orders" :key="order.id" class="order-card">
              <div class="order-header">
                <span class="order-id">订单号：{{ order.orderNo }}</span>
                <span class="order-status" :class="getStatusClass(order.status)">
                  {{ getStatusText(order.status) }}
                </span>
              </div>
              <div class="order-content">
                <div class="order-items">
                  <div v-for="item in order.items" :key="item.id" class="order-item">
                    <img :src="item.image || '/placeholder.jpg'" :alt="item.name" />
                    <div class="item-info">
                      <h4>{{ item.name }}</h4>
                      <p>数量：{{ item.quantity }}</p>
                    </div>
                    <span class="item-price">¥{{ item.price }}</span>
                  </div>
                </div>
                <div class="order-footer">
                  <span class="order-total">总计：¥{{ order.totalAmount }}</span>
                  <div class="order-actions">
                    <el-button v-if="order.status === 'PENDING_PAYMENT'" type="primary" size="small">
                      立即支付
                    </el-button>
                    <el-button v-if="order.status === 'PENDING_DELIVERY'" type="primary" size="small">
                      确认收货
                    </el-button>
                    <el-button size="small">查看详情</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="待付款" name="pending_payment">
          <div class="orders-list">
            <div v-for="order in pendingPaymentOrders" :key="order.id" class="order-card">
              <div class="order-header">
                <span class="order-id">订单号：{{ order.orderNo }}</span>
                <span class="order-status status-pending">待付款</span>
              </div>
              <div class="order-content">
                <div class="order-items">
                  <div v-for="item in order.items" :key="item.id" class="order-item">
                    <img :src="item.image || '/placeholder.jpg'" :alt="item.name" />
                    <div class="item-info">
                      <h4>{{ item.name }}</h4>
                      <p>数量：{{ item.quantity }}</p>
                    </div>
                    <span class="item-price">¥{{ item.price }}</span>
                  </div>
                </div>
                <div class="order-footer">
                  <span class="order-total">总计：¥{{ order.totalAmount }}</span>
                  <div class="order-actions">
                    <el-button type="primary" size="small">立即支付</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="待发货" name="pending_delivery">
          <div class="orders-list">
            <div v-for="order in pendingDeliveryOrders" :key="order.id" class="order-card">
              <div class="order-header">
                <span class="order-id">订单号：{{ order.orderNo }}</span>
                <span class="order-status status-shipping">待发货</span>
              </div>
              <div class="order-content">
                <div class="order-items">
                  <div v-for="item in order.items" :key="item.id" class="order-item">
                    <img :src="item.image || '/placeholder.jpg'" :alt="item.name" />
                    <div class="item-info">
                      <h4>{{ item.name }}</h4>
                      <p>数量：{{ item.quantity }}</p>
                    </div>
                    <span class="item-price">¥{{ item.price }}</span>
                  </div>
                </div>
                <div class="order-footer">
                  <span class="order-total">总计：¥{{ order.totalAmount }}</span>
                  <div class="order-actions">
                    <el-button size="small">查看详情</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="待收货" name="pending_receipt">
          <div class="orders-list">
            <div v-for="order in pendingReceiptOrders" :key="order.id" class="order-card">
              <div class="order-header">
                <span class="order-id">订单号：{{ order.orderNo }}</span>
                <span class="order-status status-shipping">待收货</span>
              </div>
              <div class="order-content">
                <div class="order-items">
                  <div v-for="item in order.items" :key="item.id" class="order-item">
                    <img :src="item.image || '/placeholder.jpg'" :alt="item.name" />
                    <div class="item-info">
                      <h4>{{ item.name }}</h4>
                      <p>数量：{{ item.quantity }}</p>
                    </div>
                    <span class="item-price">¥{{ item.price }}</span>
                  </div>
                </div>
                <div class="order-footer">
                  <span class="order-total">总计：¥{{ order.totalAmount }}</span>
                  <div class="order-actions">
                    <el-button type="primary" size="small">确认收货</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="已完成" name="completed">
          <div class="orders-list">
            <div v-for="order in completedOrders" :key="order.id" class="order-card">
              <div class="order-header">
                <span class="order-id">订单号：{{ order.orderNo }}</span>
                <span class="order-status status-completed">已完成</span>
              </div>
              <div class="order-content">
                <div class="order-items">
                  <div v-for="item in order.items" :key="item.id" class="order-item">
                    <img :src="item.image || '/placeholder.jpg'" :alt="item.name" />
                    <div class="item-info">
                      <h4>{{ item.name }}</h4>
                      <p>数量：{{ item.quantity }}</p>
                    </div>
                    <span class="item-price">¥{{ item.price }}</span>
                  </div>
                </div>
                <div class="order-footer">
                  <span class="order-total">总计：¥{{ order.totalAmount }}</span>
                  <div class="order-actions">
                    <el-button size="small">再次购买</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <el-empty v-if="filteredOrders.length === 0" description="暂无订单" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const activeTab = ref('all')

const orders = ref([
  {
    id: 1,
    orderNo: '20240116001',
    status: 'PENDING_PAYMENT',
    totalAmount: 899,
    items: [
      { id: 1, name: '手工剪纸艺术画', price: 299, quantity: 1, image: '/placeholder.jpg' },
      { id: 2, name: '苏绣团扇', price: 600, quantity: 1, image: '/placeholder.jpg' }
    ]
  },
  {
    id: 2,
    orderNo: '20240115002',
    status: 'PENDING_DELIVERY',
    totalAmount: 1299,
    items: [
      { id: 3, name: '景泰蓝花瓶', price: 1299, quantity: 1, image: '/placeholder.jpg' }
    ]
  },
  {
    id: 3,
    orderNo: '20240114003',
    status: 'COMPLETED',
    totalAmount: 599,
    items: [
      { id: 4, name: '传统蜡染围巾', price: 599, quantity: 1, image: '/placeholder.jpg' }
    ]
  }
])

const pendingPaymentOrders = computed(() => 
  orders.value.filter(order => order.status === 'PENDING_PAYMENT')
)

const pendingDeliveryOrders = computed(() => 
  orders.value.filter(order => order.status === 'PENDING_DELIVERY')
)

const pendingReceiptOrders = computed(() => 
  orders.value.filter(order => order.status === 'PENDING_RECEIPT')
)

const completedOrders = computed(() => 
  orders.value.filter(order => order.status === 'COMPLETED')
)

const filteredOrders = computed(() => {
  switch (activeTab.value) {
    case 'pending_payment':
      return pendingPaymentOrders.value
    case 'pending_delivery':
      return pendingDeliveryOrders.value
    case 'pending_receipt':
      return pendingReceiptOrders.value
    case 'completed':
      return completedOrders.value
    default:
      return orders.value
  }
})

const getStatusText = (status) => {
  const statusMap = {
    'PENDING_PAYMENT': '待付款',
    'PENDING_DELIVERY': '待发货',
    'PENDING_RECEIPT': '待收货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getStatusClass = (status) => {
  const classMap = {
    'PENDING_PAYMENT': 'status-pending',
    'PENDING_DELIVERY': 'status-shipping',
    'PENDING_RECEIPT': 'status-shipping',
    'COMPLETED': 'status-completed',
    'CANCELLED': 'status-cancelled'
  }
  return classMap[status] || ''
}
</script>

<style scoped>
.orders {
  padding: 40px 20px;
  min-height: calc(100vh - 60px);
  background: #f8f9fa;
}

.orders-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 30px;
  text-align: center;
}

.orders-tabs {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.orders-list {
  margin-top: 20px;
}

.order-card {
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 20px;
  overflow: hidden;
  transition: all 0.3s;
}

.order-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.order-id {
  font-size: 14px;
  color: #606266;
}

.order-status {
  font-size: 14px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 4px;
}

.status-pending {
  background: #fef0f0;
  color: #f56c6c;
}

.status-shipping {
  background: #ecf5ff;
  color: #409eff;
}

.status-completed {
  background: #f0f9ff;
  color: #67c23a;
}

.status-cancelled {
  background: #f4f4f5;
  color: #909399;
}

.order-content {
  padding: 20px;
}

.order-items {
  margin-bottom: 20px;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.order-item:last-child {
  border-bottom: none;
}

.order-item img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.item-info {
  flex: 1;
}

.item-info h4 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
}

.item-info p {
  font-size: 14px;
  color: #909399;
}

.item-price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.order-total {
  font-size: 18px;
  color: #303133;
  font-weight: bold;
}

.order-actions {
  display: flex;
  gap: 10px;
}
</style>

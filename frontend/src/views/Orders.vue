<template>
  <div class="orders">
    <div class="orders-container">
      <h1 class="page-title">我的订单</h1>
      
      <div class="orders-toolbar">
        <el-tabs v-model="activeTab" class="orders-tabs">
          <el-tab-pane label="全部订单" name="all" />
          <el-tab-pane label="待付款" name="unpaid" />
          <el-tab-pane label="已支付" name="paid" />
          <el-tab-pane label="已取消" name="cancelled" />
        </el-tabs>
        <el-button class="refresh-btn" size="small" text :icon="Refresh" :loading="loading" @click="fetchOrders">
          刷新
        </el-button>
      </div>

      <el-skeleton v-if="loading" :rows="8" animated />

      <el-empty v-else-if="orders.length === 0" description="暂无订单" />

      <div v-else class="orders-list">
        <el-card v-for="order in orders" :key="order.id" class="order-card" shadow="never">
          <template #header>
            <div class="order-header">
              <div class="order-header-left">
                <div class="order-no">订单号：{{ order.orderNo }}</div>
                <div class="order-time">下单时间：{{ formatDateTime(order.createTime) }}</div>
              </div>
              <el-tag :type="getStatusTagType(order.status)" effect="light">
                {{ getStatusText(order.status) }}
              </el-tag>
            </div>
          </template>

          <div class="order-meta">
            <div class="meta-line">
              <span class="meta-label">收货信息</span>
              <span class="meta-value">{{ formatReceiver(order) }}</span>
            </div>
            <div class="meta-line">
              <span class="meta-label">收货地址</span>
              <span class="meta-value">{{ formatAddress(order) }}</span>
            </div>
          </div>

          <div class="order-items">
            <div v-for="item in order.items" :key="item.id" class="order-item">
              <el-image class="item-image" :src="item.coverImageUrl || PLACEHOLDER_IMAGE" fit="cover">
                <template #error>
                  <img class="item-image-fallback" :src="PLACEHOLDER_IMAGE" alt="" />
                </template>
              </el-image>
              <div class="item-info">
                <div class="item-name">{{ item.productName }}</div>
                <div class="item-meta">¥{{ formatMoney(item.unitPrice) }} × {{ item.quantity }}</div>
              </div>
              <div class="item-subtotal">¥{{ formatMoney(item.subtotal) }}</div>
            </div>
          </div>

          <div class="order-footer">
            <div class="order-total">
              <span class="total-label">合计</span>
              <span class="total-value">¥{{ formatMoney(order.totalAmount) }}</span>
            </div>
            <div class="order-actions">
              <el-button v-if="order.status === 'UNPAID'" type="primary" size="small" :loading="creatingPayment" @click="openPay(order)">
                去支付
              </el-button>
              <el-button v-if="order.status === 'UNPAID'" size="small" :loading="cancellingOrder" @click="cancel(order)">
                取消订单
              </el-button>
              <el-button size="small" @click="openDetail(order.id)">查看详情</el-button>
            </div>
          </div>
        </el-card>

        <AppPagination
          variant="orders"
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          @current-change="fetchOrders"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <el-dialog v-model="payDialogVisible" title="模拟收银台" width="560px">
      <div v-if="payment" class="pay-body">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="支付单号">{{ payment.payNo }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ formatMoney(payment.amount) }}</el-descriptions-item>
          <el-descriptions-item label="渠道">{{ payment.channel }}</el-descriptions-item>
          <el-descriptions-item label="Pay URL">
            <div class="pay-url">
              <span class="pay-url-text">{{ payment.payUrl }}</span>
              <el-button size="small" text @click="copyText(payment.payUrl)">复制</el-button>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getPaymentTagType(payment.status)" effect="light">{{ payment.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="payment.failReason" label="失败原因">
            <el-tag type="danger" effect="light">{{ payment.failReason }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <template #footer>
        <el-button @click="payDialogVisible = false">关闭</el-button>
        <el-button
          v-if="payment?.status === 'INIT'"
          :loading="paying"
          @click="doMockPay('CANCEL')"
        >
          取消支付
        </el-button>
        <el-button
          v-if="payment?.status === 'INIT'"
          type="primary"
          :loading="paying"
          @click="doMockPay('CONFIRM')"
        >
          确认支付
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="订单详情" width="720px">
      <el-skeleton v-if="detailLoading" :rows="8" animated />
      <div v-else-if="detailOrder" class="detail-body">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号" :span="2">{{ detailOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTagType(detailOrder.status)" effect="light">{{ getStatusText(detailOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="总金额">¥{{ formatMoney(detailOrder.totalAmount) }}</el-descriptions-item>
          <el-descriptions-item label="收货人">{{ detailOrder.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detailOrder.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收货地址" :span="2">
            {{ detailOrder.regionNamePath }}{{ detailOrder.detailAddress }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(detailOrder.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ formatDateTime(detailOrder.paidTime) }}</el-descriptions-item>
          <el-descriptions-item label="取消时间" :span="2">{{ formatDateTime(detailOrder.cancelTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-items">
          <div class="detail-items-title">商品明细</div>
          <div v-for="item in detailOrder.items" :key="item.id" class="detail-item">
            <el-image class="detail-item-image" :src="item.coverImageUrl || PLACEHOLDER_IMAGE" fit="cover">
              <template #error>
                <img class="detail-item-image-fallback" :src="PLACEHOLDER_IMAGE" alt="" />
              </template>
            </el-image>
            <div class="detail-item-info">
              <div class="detail-item-name">{{ item.productName }}</div>
              <div class="detail-item-meta">¥{{ formatMoney(item.unitPrice) }} × {{ item.quantity }}</div>
            </div>
            <div class="detail-item-subtotal">¥{{ formatMoney(item.subtotal) }}</div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import AppPagination from '@/components/AppPagination.vue'
import { getOrderDetail, pageOrders, cancelOrder } from '@/api/order'
import { createPayment, mockPay } from '@/api/payment'

const activeTab = ref('all')
const loading = ref(false)
const orders = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const creatingPayment = ref(false)
const cancellingOrder = ref(false)
const payDialogVisible = ref(false)
const paying = ref(false)
const payment = ref(null)

const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailOrder = ref(null)

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

const formatMoney = (v) => {
  const n = Number(v || 0)
  if (Number.isNaN(n)) return '0.00'
  return n.toFixed(2)
}

const formatAddress = (order) => {
  if (!order) return ''
  return `${order.regionNamePath || ''}${order.detailAddress || ''}`
}

const formatReceiver = (order) => {
  if (!order) return ''
  const name = order.receiverName || ''
  const phone = order.receiverPhone || ''
  return `${name} ${phone}`.trim()
}

const statusParam = computed(() => {
  if (activeTab.value === 'unpaid') return 'UNPAID'
  if (activeTab.value === 'paid') return 'PAID'
  if (activeTab.value === 'cancelled') return 'CANCELLED'
  return undefined
})

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await pageOrders({ page: page.value, size: size.value, status: statusParam.value })
    const data = res.data || {}
    orders.value = (data.records || []).map((o) => ({
      ...o,
      items: (o.items || []).map((it) => ({
        ...it,
        coverImageUrl: normalizeUrl(it.coverImageUrl)
      }))
    }))
    total.value = Number(data.total || 0)
    page.value = Number(data.current || page.value)
    size.value = Number(data.size || size.value)
  } finally {
    loading.value = false
  }
}

const handleSizeChange = async () => {
  page.value = 1
  await fetchOrders()
}

const openPay = async (order) => {
  creatingPayment.value = true
  try {
    const res = await createPayment({ orderId: order.id, channel: 'MOCK' })
    payment.value = res.data || null
    payDialogVisible.value = true
  } catch (e) {
    ElMessage.error('创建支付单失败')
  } finally {
    creatingPayment.value = false
  }
}

const doMockPay = async (action) => {
  if (!payment.value?.id) return
  paying.value = true
  try {
    const res = await mockPay(payment.value.id, { action })
    payment.value = res.data || payment.value
    if (payment.value.status === 'SUCCESS') {
      ElMessage.success('支付成功')
      payDialogVisible.value = false
      await fetchOrders()
      return
    }
    if (payment.value.status === 'FAIL') {
      ElMessage.error(`支付失败：${payment.value.failReason || 'FAIL'}`)
      await fetchOrders()
    }
  } catch (e) {
    ElMessage.error('支付操作失败')
  } finally {
    paying.value = false
  }
}

const cancel = async (order) => {
  try {
    await ElMessageBox.confirm('确认取消该订单吗？', '提示', { type: 'warning' })
    cancellingOrder.value = true
    await cancelOrder(order.id)
    ElMessage.success('订单已取消')
    await fetchOrders()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('取消失败')
    }
  } finally {
    cancellingOrder.value = false
  }
}

const openDetail = async (orderId) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  detailOrder.value = null
  try {
    const res = await getOrderDetail(orderId)
    const o = res.data || null
    if (!o) return
    detailOrder.value = {
      ...o,
      items: (o.items || []).map((it) => ({
        ...it,
        coverImageUrl: normalizeUrl(it.coverImageUrl)
      }))
    }
  } finally {
    detailLoading.value = false
  }
}

const formatDateTime = (v) => {
  if (!v) return ''
  return String(v).replace('T', ' ')
}

const copyText = async (text) => {
  try {
    if (!text) return
    await navigator.clipboard.writeText(String(text))
    ElMessage.success('已复制')
  } catch (e) {
    ElMessage.error('复制失败')
  }
}

watch(activeTab, async () => {
  page.value = 1
  await fetchOrders()
})

onMounted(() => {
  fetchOrders()
})

const getStatusText = (status) => {
  const statusMap = {
    UNPAID: '待付款',
    PAID: '已支付',
    CANCELLED: '已取消'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  if (status === 'UNPAID') return 'danger'
  if (status === 'PAID') return 'success'
  if (status === 'CANCELLED') return 'info'
  return 'info'
}

const getPaymentTagType = (status) => {
  if (status === 'INIT') return 'warning'
  if (status === 'SUCCESS') return 'success'
  if (status === 'FAIL') return 'danger'
  return 'info'
}

const getStatusClass = (status) => {
  const classMap = {
    UNPAID: 'status-pending',
    PAID: 'status-completed',
    CANCELLED: 'status-cancelled'
  }
  return classMap[status] || ''
}
</script>

<style scoped>
.orders {
  padding: 40px 20px;
  min-height: calc(100vh - 60px);
  background: var(--app-bg);
}

.orders-container {
  max-width: var(--app-max-width);
  margin: 0 auto;
}

.page-title {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 30px;
  text-align: center;
}

.orders-toolbar {
  background: #fff;
  border-radius: var(--app-radius);
  padding: 8px 12px;
  border: 1px solid var(--app-border);
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 暗色模式下的工具栏样式 */
html.dark .orders-toolbar {
  background: #2d2d2d;
  border-color: #444;
}

/* 工具栏整体背景 - 更微妙 */
.orders-toolbar {
  background: rgba(245, 247, 250, 0.6) !important; /* 更透明 */
  backdrop-filter: blur(10px); /* 添加模糊效果 */
}

html.dark .orders-toolbar {
  background: rgba(45, 45, 45, 0.6) !important; /* 暗色模式下也更透明 */
  backdrop-filter: blur(10px);
}

/* 强制重写 tab 样式 */
.orders-tabs :deep(.el-tabs__nav-wrap) {
  background-color: transparent !important;
  border-radius: 8px;
  padding: 4px;
}

.orders-tabs :deep(.el-tabs__item) {
  transition: all 0.3s ease !important;
  border-radius: 6px !important;
  margin: 0 !important;  /* 移除margin */
  padding: 0 16px !important;  /* 统一padding */
  color: #606266 !important;
  font-weight: normal !important;
  position: relative !important;
  border: none !important;
  background: transparent !important;
  height: 40px !important;  /* 统一高度 */
  line-height: 40px !important;  /* 垂直居中 */
}

/* 暗色模式下的 tab 文字颜色 */
html.dark .orders-tabs :deep(.el-tabs__item) {
  color: #c0c4cc !important;
}

/* 激活状态的 tab - 更微妙的样式 */
.orders-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff !important;
  font-weight: 500 !important;
  background-color: rgba(64, 158, 255, 0.05) !important; /* 降低透明度 */
}

/* 暗色模式下激活 tab */
html.dark .orders-tabs :deep(.el-tabs__item.is-active) {
  color: #409eff !important;
  background-color: rgba(64, 158, 255, 0.08) !important; /* 暗色模式下稍深一点 */
}

/* hover 状态 */
.orders-tabs :deep(.el-tabs__item:hover) {
  color: #409eff !important;
  background-color: rgba(64, 158, 255, 0.03) !important;
}

/* 暗色模式下 hover */
html.dark .orders-tabs :deep(.el-tabs__item:hover) {
  background-color: rgba(64, 158, 255, 0.05) !important;
}

/* 底部指示线 - 修复宽度问题 */
.orders-tabs :deep(.el-tabs__active-bar) {
  display: none !important;
}

.orders-tabs :deep(.el-tabs__item.is-active)::after {
  content: '' !important;
  position: absolute !important;
  bottom: 0 !important;
  left: 0 !important;  /* 改为从左侧开始 */
  transform: none !important;  /* 移除transform */
  width: 100% !important;  /* 全宽 */
  height: 1px !important;
  background-color: #409eff !important;
  border-radius: 1px !important;
  opacity: 0.7 !important;
}

.orders-tabs :deep(.el-tabs__header) {
  margin: 0;
}

.orders-tabs :deep(.el-tabs__content) {
  display: none;
}

.orders-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 0;
}

.refresh-btn {
  flex-shrink: 0;
}

.orders-list {
  margin-top: 20px;
}

.order-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
  margin-bottom: 16px;
  overflow: hidden;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

/* 暗色模式下的卡片样式 */
html.dark .order-card {
  border-color: #444;
  background-color: #2d2d2d;
}

html.dark .order-card :deep(.el-card__header) {
  background-color: #333;
  border-color: #444;
}

html.dark .order-card :deep(.el-card__body) {
  background-color: #2d2d2d;
}

.order-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.06);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.order-header-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.order-no {
  font-size: 14px;
  color: #303133;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 暗色模式下的文字颜色 */
html.dark .order-no {
  color: #e5eaf3;
}

.order-time {
  font-size: 12px;
  color: #909399;
}

html.dark .order-time {
  color: #a3a6ad;
}

.order-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 0 14px;
}

.meta-line {
  display: flex;
  gap: 12px;
}

.meta-label {
  width: 72px;
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
}

html.dark .meta-label {
  color: #a3a6ad;
}

.meta-value {
  flex: 1;
  color: #606266;
  font-size: 12px;
  word-break: break-all;
}

html.dark .meta-value {
  color: #c0c4cc;
}

.order-items {
  margin-bottom: 14px;
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px dashed #ebeef5;
}

.order-item:last-child {
  border-bottom: none;
}

.item-image {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  flex-shrink: 0;
}

.item-image-fallback {
  width: 64px;
  height: 64px;
  object-fit: cover;
  display: block;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  font-size: 12px;
  color: #909399;
}

.item-subtotal {
  font-size: 14px;
  color: #f56c6c;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

html.dark .item-subtotal {
  color: #f56c6c;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

html.dark .order-footer {
  border-color: #444;
}

.order-total {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.total-label {
  font-size: 12px;
  color: #909399;
}

html.dark .total-label {
  color: #a3a6ad;
}

.total-value {
  font-size: 18px;
  color: #303133;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
}

html.dark .total-value {
  color: #e5eaf3;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.pay-url {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pay-url-text {
  flex: 1;
  word-break: break-all;
}

.detail-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  gap: 12px;
}

.detail-label {
  width: 80px;
  color: #909399;
}

.detail-value {
  flex: 1;
  word-break: break-all;
}

.detail-items {
  margin-top: 10px;
  border-top: 1px solid #ebeef5;
  padding-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-items-title {
  font-weight: 700;
  color: #303133;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-item-image {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid #ebeef5;
  flex-shrink: 0;
}

.detail-item-image-fallback {
  width: 60px;
  height: 60px;
  object-fit: cover;
  display: block;
}

.detail-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item-name {
  font-weight: 600;
}

.detail-item-meta {
  color: #909399;
  font-size: 12px;
}

.detail-item-subtotal {
  color: #f56c6c;
  font-weight: 600;
}
</style>

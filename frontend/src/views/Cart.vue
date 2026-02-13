<template>
  <div class="cart">
    <div class="page-header">
      <h1>购物车</h1>
    </div>
    <div class="cart-container">
      <el-skeleton v-if="loading" :rows="6" animated />
      <div v-else-if="cartItems.length > 0">
        <el-card class="cart-card" shadow="never">
          <template #header>
            <div class="cart-card-header">
              <div class="header-left">
                <div class="header-title">商品清单</div>
                <div class="header-subtitle">已选 {{ selectedCount }} 件</div>
              </div>
              <el-button size="small" :loading="loading || updating" @click="fetchCart">刷新</el-button>
            </div>
            <div class="cart-table-header">
              <div class="col product">商品</div>
              <div class="col price">单价</div>
              <div class="col qty">数量</div>
              <div class="col subtotal">小计</div>
              <div class="col ops">操作</div>
            </div>
          </template>

          <div class="cart-list">
            <div v-for="item in pagedCartItems" :key="item.id" class="cart-item" :class="{ disabled: item.disabled }">
              <div class="col product">
                <el-checkbox
                  v-model="item.selected"
                  :disabled="updating || item.disabled"
                  @change="(val) => handleToggleSelected(item, val)"
                />
                <el-image class="item-image" :src="item.coverImageUrl || PLACEHOLDER_IMAGE" fit="cover">
                  <template #error>
                    <img class="item-image-fallback" :src="PLACEHOLDER_IMAGE" alt="" />
                  </template>
                </el-image>
                <div class="item-info">
                  <div class="item-name">
                    <span>{{ item.productName }}</span>
                    <el-tag v-if="item.disabled" size="small" type="info">已下架</el-tag>
                  </div>
                  <div class="item-meta">商品ID：{{ item.productId }}</div>
                </div>
              </div>

              <div class="col price">
                <span class="money">¥{{ formatMoney(item.price) }}</span>
              </div>

              <div class="col qty">
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="99"
                  size="small"
                  :disabled="updating || item.disabled"
                  controls-position="right"
                  @change="(val) => handleQuantityChange(item, val)"
                />
              </div>

              <div class="col subtotal">
                <span class="money strong">¥{{ formatMoney(itemSubtotal(item)) }}</span>
              </div>

              <div class="col ops">
                <el-button type="danger" size="small" plain round :loading="updating" @click="removeItem(item)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
            
            <div class="pagination-container">
              <AppPagination
                v-model:current-page="cartCurrentPage"
                v-model:page-size="cartPageSize"
                :total="cartItems.length"
              />
            </div>
          </div>
        </el-card>

        <div class="cart-footer">
          <div class="total">
            <div class="total-line">
              <span class="label">合计</span>
              <span class="price">¥{{ totalPrice }}</span>
            </div>
            <div class="total-hint">仅计算已选商品</div>
          </div>
          <el-button type="primary" :loading="creatingOrder" :disabled="selectedCount === 0" @click="checkout">去结算</el-button>
        </div>
      </div>
      <el-empty v-else description="购物车为空" />
    </div>

    <el-dialog v-model="addressDialogVisible" title="选择收货地址" width="640px">
      <el-skeleton v-if="addressLoading" :rows="5" animated />

      <el-empty v-else-if="addresses.length === 0" description="暂无地址">
        <el-button type="primary" @click="goManageAddress">去添加地址</el-button>
      </el-empty>

      <div v-else class="address-list">
        <el-radio-group v-model="selectedAddressId" class="address-radio-group">
          <div v-for="addr in addresses" :key="addr.id" class="address-item">
            <el-radio :label="addr.id">
              <div class="address-item-main">
                <div class="address-item-title">
                  <span class="address-item-name">{{ addr.receiverName }}</span>
                  <span class="address-item-phone">{{ addr.receiverPhone }}</span>
                  <el-tag v-if="addr.isDefault === 1" type="success" size="small">默认</el-tag>
                </div>
                <div class="address-item-detail">{{ formatAddressLine(addr) }}</div>
              </div>
            </el-radio>
          </div>
        </el-radio-group>
      </div>

      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="creatingOrder" :disabled="!selectedAddressId" @click="submitOrder">提交订单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import AppPagination from '@/components/AppPagination.vue'
import { deleteCartItem, listCartItems, updateCartItemQuantity, updateCartItemSelected } from '@/api/cart'
import { createOrder } from '@/api/order'
import { listAddresses } from '@/api/auth'

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

const router = useRouter()

const loading = ref(false)
const updating = ref(false)
const cartItems = ref([])
const cartCurrentPage = ref(1)
const cartPageSize = ref(5)

const pagedCartItems = computed(() => {
  const start = (cartCurrentPage.value - 1) * cartPageSize.value
  const end = start + cartPageSize.value
  return cartItems.value.slice(start, end)
})

const addressDialogVisible = ref(false)
const addressLoading = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)
const creatingOrder = ref(false)

const normalizeUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

const formatAddressLine = (item) => {
  const region = item.regionNamePath || `${item.province || ''}${item.city || ''}${item.district || ''}`
  return `${region}${item.detailAddress || ''}`
}

const fetchCart = async () => {
  loading.value = true
  try {
    const res = await listCartItems()
    cartItems.value = (res.data || []).map((it) => ({
      ...it,
      disabled: it.productStatus !== 1,
      selected: it.selected === 1 && it.productStatus === 1,
      coverImageUrl: normalizeUrl(it.coverImageUrl)
    }))
  } finally {
    loading.value = false
  }
}

const formatMoney = (v) => {
  const n = Number(v || 0)
  if (Number.isNaN(n)) return '0.00'
  return n.toFixed(2)
}

const itemSubtotal = (item) => {
  return Number(item?.price || 0) * Number(item?.quantity || 0)
}

const selectedCount = computed(() => {
  return cartItems.value.filter((i) => i.selected).reduce((acc, i) => acc + Number(i.quantity || 0), 0)
})

const totalPrice = computed(() => {
  return cartItems.value
    .filter((item) => item.selected)
    .reduce((total, item) => total + Number(item.price || 0) * Number(item.quantity || 0), 0)
    .toFixed(2)
})

const handleToggleSelected = async (item, val) => {
  if (item.disabled) return
  const prev = item.selected
  item.selected = val
  updating.value = true
  try {
    await updateCartItemSelected(item.id, { selected: item.selected ? 1 : 0 })
  } catch (e) {
    item.selected = prev
  } finally {
    updating.value = false
  }
}

const handleQuantityChange = async (item, val) => {
  if (item.disabled) return
  const nextQty = Number(val || 1)
  const prevQty = item.quantity
  item.quantity = nextQty
  updating.value = true
  try {
    await updateCartItemQuantity(item.id, { quantity: item.quantity })
  } catch (e) {
    item.quantity = prevQty
  } finally {
    updating.value = false
  }
}

const removeItem = async (item) => {
  try {
    await ElMessageBox.confirm('确认删除该商品吗？', '提示', { type: 'warning' })
    updating.value = true
    await deleteCartItem(item.id)
    ElMessage.success('商品已删除')
    await fetchCart()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error('删除失败')
    }
  } finally {
    updating.value = false
  }
}

const openAddressDialog = async () => {
  addressDialogVisible.value = true
  addressLoading.value = true
  try {
    const res = await listAddresses()
    addresses.value = res.data || []
    const defaultOne = addresses.value.find((a) => a.isDefault === 1)
    selectedAddressId.value = defaultOne?.id || addresses.value[0]?.id || null
  } finally {
    addressLoading.value = false
  }
}

const checkout = async () => {
  const selected = cartItems.value.filter((i) => i.selected)
  if (selected.length === 0) {
    ElMessage.warning('请先选择要结算的商品')
    return
  }
  await openAddressDialog()
}

const submitOrder = async () => {
  if (!selectedAddressId.value) return
  creatingOrder.value = true
  try {
    await createOrder({ addressId: selectedAddressId.value })
    ElMessage.success('下单成功')
    addressDialogVisible.value = false
    await fetchCart()
    router.push('/orders')
  } catch (e) {
    ElMessage.error('下单失败')
  } finally {
    creatingOrder.value = false
  }
}

const goManageAddress = () => {
  addressDialogVisible.value = false
  router.push('/profile')
}

onMounted(() => {
  fetchCart()
})
</script>

<style scoped>
.cart {
  padding: 40px 20px;
  min-height: calc(100vh - 60px);
  background: var(--app-bg);
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

.cart-card {
  border-radius: var(--app-radius);
  border: 1px solid var(--app-border);
  overflow: hidden;
}

.cart-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 12px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

.header-subtitle {
  font-size: 12px;
  color: #909399;
}

.cart-table-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 12px;
  background: #f5f7fa;
  border: 1px solid var(--app-border);
  border-left: none;
  border-right: none;
  color: #606266;
  font-size: 12px;
}

.cart-list {
  background: #fff;
}

.cart-footer {
  margin-top: 14px;
  background: #fff;
  padding: 20px;
  border-radius: var(--app-radius);
  border: 1px solid var(--app-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  bottom: 12px;
  backdrop-filter: blur(8px);
}

.total {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.total-line {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.total-line .label {
  color: #606266;
  font-size: 14px;
}

.total-line .price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: 800;
}

.total-hint {
  font-size: 12px;
  color: #909399;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 12px;
  border-bottom: 1px solid #ebeef5;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item.disabled {
  opacity: 0.65;
}

.col {
  display: flex;
  align-items: center;
}

.col.product {
  flex: 1;
  min-width: 320px;
  gap: 12px;
}

.col.price {
  width: 110px;
  justify-content: flex-end;
}

.col.qty {
  width: 150px;
  justify-content: center;
}

.col.subtotal {
  width: 120px;
  justify-content: flex-end;
}

.col.ops {
  width: 90px;
  justify-content: flex-end;
}

.item-image {
  width: 72px;
  height: 72px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
  border: 1px solid #ebeef5;
}

.item-image-fallback {
  width: 72px;
  height: 72px;
  object-fit: cover;
  display: block;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.item-name {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  color: #303133;
}

.item-name span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  font-size: 12px;
  color: #909399;
}

.money {
  font-variant-numeric: tabular-nums;
  color: #303133;
}

.money.strong {
  color: #f56c6c;
  font-weight: 700;
}

.address-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-radio-group {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.address-item {
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  padding: 12px;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.address-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.address-item-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.address-item-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.address-item-name {
  font-weight: 600;
}

.address-item-phone {
  color: #606266;
}

.address-item-detail {
  color: #606266;
  word-break: break-all;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}
</style>

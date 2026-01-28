<template>
  <div class="header">
    <div class="header-container">
      <div class="header-left">
        <router-link to="/" class="logo">
          <span>非遗商城</span>
        </router-link>
      </div>
      <div class="header-center">
        <el-menu
          :default-active="activeMenu"
          mode="horizontal"
          router
          background-color="#409EFF"
          text-color="#fff"
          active-text-color="#fff"
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/heritage">非遗文化</el-menu-item>
          <el-menu-item index="/products">商品列表</el-menu-item>
          <el-menu-item index="/customize">智能定制</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <template v-if="userStore.token">
          <el-dropdown>
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo.avatar || ''">
                {{ userStore.userInfo.name?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo.name }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goToProfile">个人中心</el-dropdown-item>
                <el-dropdown-item @click="goToCart">购物车</el-dropdown-item>
                <el-dropdown-item @click="goToOrders">我的订单</el-dropdown-item>
                <el-dropdown-item v-if="canGoUsers" @click="goToUsers" :divided="true">用户管理</el-dropdown-item>
                <el-dropdown-item v-if="canGoProducts" @click="goToManageProducts" :divided="!canGoUsers">商品管理</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="goToLogin">登录</el-button>
          <el-button @click="goToRegister">注册</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getProfile } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const role = computed(() => userStore.userInfo?.role)
const canGoUsers = computed(() => role.value === 'ADMIN')
const canGoProducts = computed(() => role.value === 'ADMIN' || role.value === 'MERCHANT')

const validateToken = async () => {
  if (userStore.token) {
    try {
      const res = await getProfile()
      if (res.code === 200) {
        const avatar = res.data.avatar
        const userInfo = {
          ...res.data,
          avatar: avatar?.startsWith('http') ? avatar : `http://localhost:8080${avatar}`
        }
        userStore.setUserInfo(userInfo)
      } else {
        userStore.logout()
      }
    } catch (error) {
      userStore.logout()
    }
  }
}

onMounted(() => {
  validateToken()
})

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/login')
}

const goToProfile = () => {
  router.push('/profile')
}

const goToCart = () => {
  router.push('/cart')
}

const goToOrders = () => {
  router.push('/orders')
}

const goToUsers = () => {
  router.push('/manage/users')
}

const goToManageProducts = () => {
  if (role.value === 'MERCHANT') {
    router.push('/merchant/products')
    return
  }
  router.push('/manage/products')
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 60px;
  background-color: #409EFF;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left .logo {
  font-size: 24px;
  font-weight: bold;
  color: #fff;
  text-decoration: none;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #fff;
}

.username {
  font-size: 14px;
}

:deep(.el-menu--horizontal) {
  border-bottom: none;
}
</style>

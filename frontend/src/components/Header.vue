<template>
  <div class="header">
    <div class="header-container">
      <div class="header-left">
        <router-link to="/" class="logo">
          <span>{{ $t('header.brand') }}</span>
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
          <el-menu-item index="/home">{{ $t('header.home') }}</el-menu-item>
          <el-menu-item index="/heritage">{{ $t('header.heritage') }}</el-menu-item>
          <el-menu-item index="/products">{{ $t('header.products') }}</el-menu-item>
          <el-menu-item index="/customize">{{ $t('header.customize') }}</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <el-switch
          :model-value="dark"
          :active-icon="Moon"
          :inactive-icon="Sunny"
          @change="handleToggleDark"
        />

        <el-dropdown @command="handleSetLang">
          <span class="lang-switch">{{ langLabel }}</span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="zh">{{ $t('header.langZh') }}</el-dropdown-item>
              <el-dropdown-item command="en">{{ $t('header.langEn') }}</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

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
                <el-dropdown-item @click="goToProfile">{{ $t('header.profile') }}</el-dropdown-item>
                <el-dropdown-item @click="goToCart">{{ $t('header.cart') }}</el-dropdown-item>
                <el-dropdown-item @click="goToOrders">{{ $t('header.orders') }}</el-dropdown-item>
                <el-dropdown-item v-if="canGoUsers" @click="goToUsers" :divided="true">{{ $t('header.users') }}</el-dropdown-item>
                <el-dropdown-item v-if="canGoProducts" @click="goToManageProducts" :divided="!canGoUsers">{{ $t('header.manageProducts') }}</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">{{ $t('header.logout') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="goToLogin">{{ $t('header.login') }}</el-button>
          <el-button @click="goToRegister">{{ $t('header.register') }}</el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getProfile } from '@/api/auth'
import { Moon, Sunny } from '@element-plus/icons-vue'
import { i18n } from '@/i18n'
import { toggleDark, isDark } from '@/utils/theme'
import { setLang } from '@/utils/lang'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const role = computed(() => userStore.userInfo?.role)
const canGoUsers = computed(() => role.value === 'ADMIN')
const canGoProducts = computed(() => role.value === 'ADMIN' || role.value === 'MERCHANT')

const dark = ref(isDark())
const langLabel = computed(() => (i18n.global.locale.value === 'zh' ? '中文' : 'EN'))

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
  ElMessage.success(i18n.global.t('common.logoutSuccess'))
  router.push('/login')
}

const handleToggleDark = () => {
  toggleDark()
  dark.value = isDark()
}

const handleSetLang = (lang) => {
  setLang(lang)
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

.lang-switch {
  cursor: pointer;
  color: #fff;
  user-select: none;
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

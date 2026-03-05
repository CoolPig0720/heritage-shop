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
          class="header-menu"
          :default-active="activeMenu"
          mode="horizontal"
          router
          :ellipsis="false"
        >
          <el-menu-item index="/home">{{ $t('header.home') }}</el-menu-item>
          <el-menu-item index="/products">{{ $t('header.products') }}</el-menu-item>
          <el-menu-item index="/heritage">{{ $t('header.heritage') }}</el-menu-item>
          <el-menu-item index="/customize">{{ $t('header.customize') }}</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleThemeChange" trigger="click">
          <el-button circle :icon="themeIcon" class="theme-btn" />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="light" :class="{ 'is-active': themeMode === 'light' }">
                <el-icon><Sunny /></el-icon>
                亮色模式
              </el-dropdown-item>
              <el-dropdown-item command="dark" :class="{ 'is-active': themeMode === 'dark' }">
                <el-icon><Moon /></el-icon>
                暗色模式
              </el-dropdown-item>
              <el-dropdown-item command="system" :class="{ 'is-active': themeMode === 'system' }">
                <el-icon><Monitor /></el-icon>
                跟随系统
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

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
                <el-dropdown-item v-if="canGoHeritageManage" @click="goToManageHeritage">{{ $t('header.manageHeritage') }}</el-dropdown-item>
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
import { Moon, Sunny, Monitor } from '@element-plus/icons-vue'
import { i18n } from '@/i18n'
import { getThemeMode, setThemeMode, isDark, initTheme, ThemeMode } from '@/utils/theme'
import { setLang } from '@/utils/lang'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const role = computed(() => userStore.userInfo?.role)
const canGoUsers = computed(() => role.value === 'ADMIN')
const canGoProducts = computed(() => role.value === 'ADMIN' || role.value === 'MERCHANT')
const canGoHeritageManage = computed(() => role.value === 'ADMIN')

const themeMode = ref(getThemeMode())
const dark = ref(isDark())
const langLabel = computed(() => (i18n.global.locale.value === 'zh' ? '中文' : 'EN'))

// 主题图标
const themeIcon = computed(() => {
  if (themeMode.value === ThemeMode.DARK) return Moon
  if (themeMode.value === ThemeMode.LIGHT) return Sunny
  return Monitor
})

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
  initTheme()
  themeMode.value = getThemeMode()
  dark.value = isDark()
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

const goToManageHeritage = () => {
  router.push('/manage/heritage')
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success(i18n.global.t('common.logoutSuccess'))
  router.push('/login')
}

const handleThemeChange = (mode) => {
  setThemeMode(mode)
  themeMode.value = mode
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
  background-color: var(--nav-bg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  transition: background-color 0.3s ease;
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

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.header-left .logo {
  font-size: 24px;
  font-weight: bold;
  color: var(--nav-text);
  text-decoration: none;
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 12px;
  }
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

.theme-btn {
  background: transparent;
  border: none;
  color: var(--nav-text);
}

.theme-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--nav-text);
}

.lang-switch {
  cursor: pointer;
  color: var(--nav-text);
  user-select: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--nav-text);
}

.username {
  font-size: 14px;
}

:deep(.el-menu--horizontal) {
  border-bottom: none;
  background-color: transparent;
}

:deep(.header-menu.el-menu--horizontal) {
  flex: 0 0 auto;
  background-color: transparent;
}

:deep(.header-menu.el-menu--horizontal .el-menu-item) {
  font-size: 16px;
  font-weight: 600;
  padding: 0 20px;
  border-radius: 999px;
  border: none;
  margin: 0 4px;
  color: rgba(255, 255, 255, 0.85) !important;
  background-color: transparent;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.header-menu.el-menu--horizontal .el-menu-item:not(.is-disabled):hover) {
  background-color: rgba(255, 255, 255, 0.15);
  color: var(--nav-text-hover) !important;
}

:deep(.header-menu.el-menu--horizontal .el-menu-item::after) {
  display: none !important;
}

:deep(.header-menu.el-menu--horizontal .el-menu-item.is-active) {
  background-color: var(--nav-active-bg);
  color: var(--nav-text-hover) !important;
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

/* 暗色模式下菜单样式调整 */
html.dark .header-menu.el-menu--horizontal .el-menu-item {
  color: var(--nav-text) !important;
}

html.dark .header-menu.el-menu--horizontal .el-menu-item:hover {
  background-color: var(--nav-active-bg);
}
</style>

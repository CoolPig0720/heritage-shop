import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录注册' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'heritage',
        name: 'Heritage',
        component: () => import('@/views/admin/Heritage.vue'),
        meta: { title: '非遗文化' }
      },
      {
        path: 'products',
        name: 'Products',
        component: () => import('@/views/ShopProducts.vue'),
        meta: { title: '商品列表' }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/ProductDetail.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'customize',
        name: 'Customize',
        component: () => import('@/views/Customize.vue'),
        meta: { title: '智能定制' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/Cart.vue'),
        meta: { title: '购物车' }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/Orders.vue'),
        meta: { title: '我的订单' }
      }
    ]
  },
  {
    path: '/manage',
    name: 'Manage',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/manage/dashboard',
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'dashboard',
        name: 'ManageDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理后台' }
      },
      {
        path: 'users',
        name: 'ManageUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'products',
        name: 'ManageProducts',
        component: () => import('@/views/admin/Products.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'heritage',
        name: 'ManageHeritage',
        component: () => import('@/views/admin/Heritage.vue'),
        meta: { title: '非遗文化管理' }
      }
    ]
  },
  {
    path: '/merchant',
    name: 'Merchant',
    component: () => import('@/layouts/MerchantLayout.vue'),
    redirect: '/merchant/dashboard',
    meta: { requiresAuth: true, requiresMerchant: true },
    children: [
      {
        path: 'dashboard',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/Dashboard.vue'),
        meta: { title: '商家后台' }
      },
      {
        path: 'products',
        name: 'MerchantProducts',
        component: () => import('@/views/merchant/Products.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'custom-orders',
        name: 'MerchantCustomOrders',
        component: () => import('@/views/merchant/CustomOrders.vue'),
        meta: { title: '定制订单' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '非遗产品智能定制商城'

  const userStore = useUserStore()

  if (to.meta.requiresAuth && !userStore.token) {
    next('/login')
    return
  }

  const role = userStore.userInfo?.role
  const roles = to.meta.requiresRoles
  if (Array.isArray(roles) && roles.length > 0 && !roles.includes(role)) {
    next('/')
    return
  }
  if (to.meta.requiresAdmin && role !== 'ADMIN') {
    next('/')
    return
  }
  if (to.meta.requiresMerchant && role !== 'MERCHANT') {
    next('/')
    return
  }

  next()
})

export default router

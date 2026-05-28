import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/home',
    name: 'home',
    component: () => import('../views/HomeView.vue')
  },
  {
    path: '/publish',
    name: 'publish',
    component: () => import('../views/PublishView.vue')
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('../views/ProfileView.vue')
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('../views/RegisterView.vue')
  },

  {
    path: '/hot',
    name: 'Hot',
    component: () => import('../views/Hot.vue')
  },
  {
    path: '/message',
    name: 'Message',
    component: () => import('../views/Message.vue')
  },
  {
    path: '/user/:userId',
    name: 'userProfile',
    component: () => import('../views/UserProfile.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ✅ 新版本路由守卫（无 next，无警告！）
router.beforeEach((to, from) => {
  const isLoggedIn = localStorage.getItem('username') // 适配你的项目

  // 未登录，且要去的不是登录/注册 → 跳登录
  if (!isLoggedIn && to.name !== 'login' && to.name !== 'register') {
    return { name: 'login' }
  }
})

export default router
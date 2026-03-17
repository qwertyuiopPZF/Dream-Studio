import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      name: 'layout',
      component: () => import('@/views/Layout.vue'),
      redirect: '/home',
      children: [
        {
          path: 'home',
          name: 'home',
          component: () => import('@/views/Home.vue'),
        },
        {
          path: 'tag/:id',
          name: 'tag',
          component: () => import('@/views/Tags.vue'),
        },
        {
          path: 'archive',
          name: 'archive',
          component: () => import('@/views/Archive.vue'),
        },
        {
          path: 'about',
          name: 'about',
          component: () => import('@/views/About.vue'),
        },
        {
          path: 'moment',
          name: 'moment',
          component: () => import('@/views/Moment.vue'),
        },
        {
          path: 'friendlinks',
          name: 'friendLinks',
          component: () => import('@/views/FriendLinks.vue'),
        },
        {
          path: 'forum',
          name: 'forum',
          component: () => import('@/views/Forum.vue'),
        },
        {
          path: 'forum/:id',
          name: 'forumPostDetail',
          component: () => import('@/views/ForumPostDetail.vue'),
        },
        {
          path: 'profile',
          name: 'profileCenter',
          component: () => import('@/views/ProfileCenter.vue'),
        },
        {
          path: 'article/:id',
          name: 'articleDetail',
          component: () => import('@/views/ArticleDetail.vue'),
        },
        {
          path: 'category/:id',
          name: 'categoryDetail',
          component: () => import('@/views/CategoryDetail.vue'),
        },
      ],
    },
  ],
  // 路由切换时自动滚动到页面顶部
  scrollBehavior(to, from, savedPosition) {
    // 如果路径变了（从页面A跳到页面B），但没有锚点，才回到顶部
    if (to.path !== from.path && !to.hash) {
      const container = document.querySelector('.main-scroll-container')
      if (container) {
        container.scrollTop = 0
      }
    }

    // 如果有锚点，我们什么都不做，让 el-anchor 自己去处理跳转
    if (to.hash) {
      return false // 返回 false 表示不干预滚动
    }
  },
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  const isPublicRoute = Boolean(to.meta?.public)
  const isLoggedIn = Boolean(authStore.accessToken)

  if (!isLoggedIn && !isPublicRoute) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  if (isLoggedIn && to.path === '/login' && !to.query.code) {
    return '/profile'
  }

  return true
})

export default router

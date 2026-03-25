import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useUserStore } from '@/store/user'

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
      meta: { requiresAuth: true },
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
          redirect: '/forum',
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
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      redirect: '/admin/home',
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: 'home',
          name: 'adminHome',
          component: () => import('@/views/admin/AdminHome.vue'),
        },
        {
          path: 'articlemgmt',
          name: 'adminArticleMgmt',
          component: () => import('@/views/admin/AdminContent.vue'),
        },
        {
          path: 'commentmgmt',
          name: 'adminCommentMgmt',
          component: () => import('@/views/admin/AdminContent.vue'),
        },
        {
          path: 'momentsmgmt',
          name: 'adminMomentsMgmt',
          component: () => import('@/views/admin/AdminContent.vue'),
        },
        {
          path: 'content',
          name: 'adminContent',
          component: () => import('@/views/admin/AdminContent.vue'),
        },
        {
          path: 'writearticle',
          name: 'adminWriteArticle',
          component: () => import('@/views/admin/AdminWriteArticle.vue'),
        },
        {
          path: 'article/edit/:id',
          name: 'adminEditArticle',
          component: () => import('@/views/admin/AdminWriteArticle.vue'),
        },
        {
          path: 'writemoment',
          name: 'adminWriteMoment',
          component: () => import('@/views/admin/AdminWriteMoment.vue'),
        },
        {
          path: 'categorisemgmt',
          name: 'adminCategoriseMgmt',
          component: () => import('@/views/admin/AdminSite.vue'),
        },
        {
          path: 'tagsmgmt',
          name: 'adminTagsMgmt',
          component: () => import('@/views/admin/AdminSite.vue'),
        },
        {
          path: 'forum-entry',
          name: 'adminForumEntry',
          component: () => import('@/views/admin/AdminSite.vue'),
        },
        {
          path: 'site',
          name: 'adminSite',
          component: () => import('@/views/admin/AdminSite.vue'),
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
  const userStore = useUserStore()
  const isLoggedIn = Boolean(authStore.accessToken)
  const requiresAuth = Boolean(to.meta?.requiresAuth)
  const requiresAdmin = Boolean(to.meta?.requiresAdmin)

  if (!isLoggedIn && requiresAuth) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  if (isLoggedIn && to.path === '/login' && !to.query.code) {
    return '/home'
  }

  if (requiresAdmin && !userStore.isAdmin) {
    return '/profile'
  }

  return true
})

export default router

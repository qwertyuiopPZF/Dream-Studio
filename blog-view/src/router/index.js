import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useUserStore } from '@/store/user'

<<<<<<< HEAD
=======
const workspaceViewLoaders = {
  user: {
    layout: () => import('@/views/user/Layout.vue'),
    articleManagement: () => import('@/views/user/ArticleManagement.vue'),
    momentManagement: () => import('@/views/user/MomentManagement.vue'),
    articleEditor: () => import('@/views/user/PostArticle.vue'),
    momentEditor: () => import('@/views/user/PostMoment.vue'),
    forumPublish: () => import('@/views/user/PostForumThread.vue'),
    forumManagement: () => import('@/views/user/ForumThreadManagement.vue'),
  },
  admin: {
    layout: () => import('@/views/admin/Layout.vue'),
    articleManagement: () => import('@/views/admin/ArticleManagement.vue'),
    commentManagement: () => import('@/views/admin/AdminCommentManagement.vue'),
    momentManagement: () => import('@/views/admin/MomentManagement.vue'),
    articleEditor: () => import('@/views/admin/PostArticle.vue'),
    momentEditor: () => import('@/views/admin/PostMoment.vue'),
    categoryManagement: () => import('@/views/admin/AdminCategoryManagement.vue'),
    tagManagement: () => import('@/views/admin/AdminTagManagement.vue'),
    forumPublish: () => import('@/views/admin/PostForumThread.vue'),
    forumManagement: () => import('@/views/admin/ForumThreadManagement.vue'),
    siteOverview: () => import('@/views/admin/AdminSiteOverview.vue'),
    userManagement: () => import('@/views/admin/AdminUserManagement.vue'),
  },
}

const createWorkspaceChildren = (workspaceMode) => {
  const isAdminWorkspace = workspaceMode === 'admin'
  const routeBase = isAdminWorkspace ? '/admin' : '/profile'
  const routeNamePrefix = isAdminWorkspace ? 'admin' : 'profile'
  const views = workspaceViewLoaders[workspaceMode]

  const routes = [
    {
      path: 'articlemgmt',
      name: `${routeNamePrefix}ArticleMgmt`,
      component: views.articleManagement,
    },
  ]

  if (isAdminWorkspace) {
    routes.push({
      path: 'commentmgmt',
      name: 'adminCommentMgmt',
      component: views.commentManagement,
    })
  }

  routes.push(
    {
      path: 'momentsmgmt',
      name: `${routeNamePrefix}MomentsMgmt`,
      component: views.momentManagement,
    },
    {
      path: 'content',
      redirect: `${routeBase}/articlemgmt`,
    },
    {
      path: 'writearticle',
      name: `${routeNamePrefix}WriteArticle`,
      component: views.articleEditor,
    },
    {
      path: 'article/edit/:id',
      name: `${routeNamePrefix}EditArticle`,
      component: views.articleEditor,
    },
    {
      path: 'writemoment',
      name: `${routeNamePrefix}WriteMoment`,
      component: views.momentEditor,
    },
  )

  if (isAdminWorkspace) {
    routes.push(
      {
        path: 'categorisemgmt',
        name: 'adminCategoriseMgmt',
        component: views.categoryManagement,
      },
      {
        path: 'tagsmgmt',
        name: 'adminTagsMgmt',
        component: views.tagManagement,
      },
      {
        path: 'usermgmt',
        name: 'adminUserMgmt',
        component: views.userManagement,
      },
    )
  }

  routes.push(
    {
      path: 'forum-publish',
      name: `${routeNamePrefix}ForumPublish`,
      component: views.forumPublish,
    },
    {
      path: 'forum-manage',
      name: `${routeNamePrefix}ForumManage`,
      component: views.forumManagement,
    },
    {
      path: 'forum-entry',
      redirect: `${routeBase}/forum-publish`,
    },
  )

  if (isAdminWorkspace) {
    routes.push({
      path: 'site',
      name: 'adminSite',
      component: views.siteOverview,
    })
  }

  return routes
}

>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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
<<<<<<< HEAD
        {
          path: 'profile',
          name: 'profileCenter',
          component: () => import('@/views/ProfileCenter.vue'),
        },
=======

>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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
<<<<<<< HEAD
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
=======
      path: '/profile',
      component: workspaceViewLoaders.user.layout,
      redirect: '/profile/articlemgmt',
      meta: { requiresAuth: true },
      children: createWorkspaceChildren('user'),
    },
    {
      path: '/admin',
      component: workspaceViewLoaders.admin.layout,
      redirect: '/admin/articlemgmt',
      meta: { requiresAuth: true, requiresAdmin: true },
      children: createWorkspaceChildren('admin'),
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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
<<<<<<< HEAD
    return '/profile'
=======
    return '/profile/articlemgmt'
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
  }

  return true
})

export default router

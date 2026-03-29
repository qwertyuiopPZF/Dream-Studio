import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useUserStore } from '@/store/user'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'

const profileViewLoaders = {
  user: {
    layout: () => import('@/views/profile/layout/UserLayout.vue'),
    overview: () => import('@/views/profile/user/Overview.vue'),
    notifications: () => import('@/views/profile/shared/Notifications.vue'),
    account: () => import('@/views/profile/user/AccountSettings.vue'),
    articleManagement: () => import('@/views/profile/shared/ArticleManagement.vue'),
    momentManagement: () => import('@/views/profile/shared/MomentManagement.vue'),
    articleEditor: () => import('@/views/profile/shared/ArticleEditor.vue'),
    momentEditor: () => import('@/views/profile/shared/MomentEditor.vue'),
    forumPublish: () => import('@/views/profile/shared/ForumPublish.vue'),
    forumManagement: () => import('@/views/profile/shared/ForumThreadManagement.vue'),
  },
  admin: {
    layout: () => import('@/views/profile/layout/AdminLayout.vue'),
    overview: () => import('@/views/profile/admin/Overview.vue'),
    announcementManagement: () => import('@/views/profile/admin/AnnouncementOverview.vue'),
    notifications: () => import('@/views/profile/shared/Notifications.vue'),
    articleManagement: () => import('@/views/profile/shared/ArticleManagement.vue'),
    commentManagement: () => import('@/views/profile/admin/CommentManagement.vue'),
    reportManagement: () => import('@/views/profile/admin/ReportManagement.vue'),
    momentManagement: () => import('@/views/profile/shared/MomentManagement.vue'),
    articleEditor: () => import('@/views/profile/shared/ArticleEditor.vue'),
    momentEditor: () => import('@/views/profile/shared/MomentEditor.vue'),
    taxonomyManagement: () => import('@/views/profile/admin/TaxonomyManagement.vue'),
    forumPublish: () => import('@/views/profile/shared/ForumPublish.vue'),
    forumManagement: () => import('@/views/profile/shared/ForumThreadManagement.vue'),
    siteOverview: () => import('@/views/profile/admin/SiteOverview.vue'),
    userManagement: () => import('@/views/profile/admin/UserManagement.vue'),
  },
}

const createProfileChildren = (profileMode) => {
  const isAdminWorkspace = profileMode === 'admin'
  const routeBase = isAdminWorkspace ? '/admin' : '/profile'
  const routeNamePrefix = isAdminWorkspace ? 'admin' : 'profile'
  const views = profileViewLoaders[profileMode]

  const routes = [
    {
      path: 'overview',
      name: `${routeNamePrefix}Overview`,
      component: views.overview,
      meta: {
        requiresAuth: true,
        requiredCapability: isAdminWorkspace
          ? WORKSPACE_CAPABILITIES.DASHBOARD_VIEW
          : WORKSPACE_CAPABILITIES.PROFILE_OVERVIEW_VIEW,
      },
    },
    {
      path: 'articlemgmt',
      name: `${routeNamePrefix}ArticleMgmt`,
      component: views.articleManagement,
      meta: {
        requiresAuth: true,
        requiredCapability: isAdminWorkspace
          ? WORKSPACE_CAPABILITIES.ARTICLE_MANAGE_ALL
          : WORKSPACE_CAPABILITIES.ARTICLE_MANAGE_OWN,
      },
    },
  ]

  if (!isAdminWorkspace) {
    routes.splice(1, 0,
      {
        path: 'notifications',
        name: `${routeNamePrefix}Notifications`,
        component: views.notifications,
        meta: {
          requiresAuth: true,
          requiredCapability: WORKSPACE_CAPABILITIES.PROFILE_NOTIFICATIONS_VIEW,
        },
      },
      {
        path: 'account',
        name: `${routeNamePrefix}Account`,
        component: views.account,
        meta: {
          requiresAuth: true,
          requiredCapability: WORKSPACE_CAPABILITIES.PROFILE_ACCOUNT_EDIT,
        },
      },
    )
  }

  if (isAdminWorkspace) {
    routes.splice(1, 0, {
      path: 'notifications',
      name: 'adminNotifications',
      component: views.notifications,
      props: { workspaceMode: 'admin' },
      meta: {
        requiresAuth: true,
        requiredCapability: WORKSPACE_CAPABILITIES.PROFILE_NOTIFICATIONS_VIEW,
      },
    })

    routes.push({
      path: 'commentmgmt',
      name: 'adminCommentMgmt',
      component: views.commentManagement,
      meta: {
        requiresAuth: true,
        requiredCapability: WORKSPACE_CAPABILITIES.COMMENT_MODERATE,
      },
    })

    routes.push({
      path: 'reportmgmt',
      name: 'adminReportMgmt',
      component: views.reportManagement,
      meta: {
        requiresAuth: true,
        requiredCapability: WORKSPACE_CAPABILITIES.REPORT_REVIEW,
      },
    })
  }

  routes.push(
    {
      path: 'momentsmgmt',
      name: `${routeNamePrefix}MomentsMgmt`,
      component: views.momentManagement,
      meta: {
        requiresAuth: true,
        requiredCapability: isAdminWorkspace
          ? WORKSPACE_CAPABILITIES.MOMENT_MANAGE_ALL
          : WORKSPACE_CAPABILITIES.MOMENT_MANAGE_OWN,
      },
    },
    {
      path: 'content',
      redirect: `${routeBase}/articlemgmt`,
    },
    {
      path: 'writearticle',
      name: `${routeNamePrefix}WriteArticle`,
      component: views.articleEditor,
      meta: {
        requiresAuth: true,
        requiredCapability: WORKSPACE_CAPABILITIES.ARTICLE_WRITE,
      },
    },
    {
      path: 'article/edit/:id',
      name: `${routeNamePrefix}EditArticle`,
      component: views.articleEditor,
      meta: {
        requiresAuth: true,
        requiredCapability: WORKSPACE_CAPABILITIES.ARTICLE_WRITE,
      },
    },
    {
      path: 'writemoment',
      name: `${routeNamePrefix}WriteMoment`,
      component: views.momentEditor,
      meta: {
        requiresAuth: true,
        requiredCapability: WORKSPACE_CAPABILITIES.MOMENT_WRITE,
      },
    },
  )

  if (isAdminWorkspace) {
    routes.push(
      {
        path: 'categorisemgmt',
        name: 'adminCategoriseMgmt',
        component: views.taxonomyManagement,
        props: { mode: 'category' },
        meta: {
          requiresAuth: true,
          requiredCapability: WORKSPACE_CAPABILITIES.TAXONOMY_MANAGE,
        },
      },
      {
        path: 'tagsmgmt',
        name: 'adminTagsMgmt',
        component: views.taxonomyManagement,
        props: { mode: 'tag' },
        meta: {
          requiresAuth: true,
          requiredCapability: WORKSPACE_CAPABILITIES.TAXONOMY_MANAGE,
        },
      },
      {
        path: 'usermgmt',
        name: 'adminUserMgmt',
        component: views.userManagement,
        meta: {
          requiresAuth: true,
          requiredCapability: WORKSPACE_CAPABILITIES.USER_MANAGE,
        },
      },
    )
  }

  routes.push(
    {
      path: 'forum-publish',
      name: `${routeNamePrefix}ForumPublish`,
      component: views.forumPublish,
      meta: {
        requiresAuth: true,
        requiredCapability: WORKSPACE_CAPABILITIES.FORUM_POST_WRITE,
      },
    },
    {
      path: 'forum-manage',
      name: `${routeNamePrefix}ForumManage`,
      component: views.forumManagement,
      meta: {
        requiresAuth: true,
        requiredCapability: isAdminWorkspace
          ? WORKSPACE_CAPABILITIES.FORUM_POST_MODERATE
          : WORKSPACE_CAPABILITIES.FORUM_POST_MANAGE_OWN,
      },
    },
    {
      path: 'forum-entry',
      redirect: `${routeBase}/forum-publish`,
    },
  )

  if (isAdminWorkspace) {
    routes.push({
      path: 'announcements',
      name: 'adminAnnouncements',
      component: views.announcementManagement,
      meta: {
        requiresAuth: true,
        requiredCapability: WORKSPACE_CAPABILITIES.SITE_MANAGE,
      },
    })

    routes.push({
      path: 'site',
      name: 'adminSite',
      component: views.siteOverview,
      meta: {
        requiresAuth: true,
        requiredCapability: WORKSPACE_CAPABILITIES.SITE_MANAGE,
      },
    })
  }

  return routes
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/Login.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      name: 'layout',
      component: () => import('@/views/public/PublicLayout.vue'),
      redirect: '/home',
      meta: { requiresAuth: true },
      children: [
        {
          path: 'home',
          name: 'home',
          component: () => import('@/views/public/Home.vue'),
        },
        {
          path: 'tag/:id',
          name: 'tag',
          component: () => import('@/views/public/Tags.vue'),
        },
        {
          path: 'archive',
          name: 'archive',
          component: () => import('@/views/public/Archive.vue'),
        },
        {
          path: 'resources',
          name: 'resources',
          component: () => import('@/views/public/Resources.vue'),
        },
        {
          path: 'about',
          name: 'about',
          component: () => import('@/views/public/About.vue'),
        },
        {
          path: 'moment',
          name: 'moment',
          component: () => import('@/views/public/Moment.vue'),
        },
        {
          path: 'friendlinks',
          redirect: '/forum',
        },
        {
          path: 'forum',
          name: 'forum',
          component: () => import('@/views/public/Forum.vue'),
        },
        {
          path: 'forum/:id',
          name: 'forumPostDetail',
          component: () => import('@/views/public/ForumPostDetail.vue'),
        },

        {
          path: 'article/:id',
          name: 'articleDetail',
          component: () => import('@/views/public/ArticleDetail.vue'),
        },
        {
          path: 'category/:id',
          name: 'categoryDetail',
          component: () => import('@/views/public/CategoryDetail.vue'),
        },
      ],
    },
    {
      path: '/profile',
      component: profileViewLoaders.user.layout,
      redirect: '/profile/overview',
      meta: { requiresAuth: true },
      children: createProfileChildren('user'),
    },
    {
      path: '/admin',
      component: profileViewLoaders.admin.layout,
      redirect: '/admin/overview',
      meta: { requiresAuth: true, requiresAdmin: true },
      children: createProfileChildren('admin'),
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
  const requiredCapability = to.meta?.requiredCapability

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
    return '/profile/overview'
  }

  if (to.path.startsWith('/profile') && userStore.isAdmin) {
    return '/admin/overview'
  }

  if (to.path.startsWith('/admin') && isLoggedIn && !userStore.isAdmin) {
    return '/profile/overview'
  }

  if (requiredCapability && !userStore.hasCapability(requiredCapability)) {
    return userStore.hasCapability(WORKSPACE_CAPABILITIES.DASHBOARD_VIEW)
      ? '/admin/overview'
      : '/profile/overview'
  }

  return true
})

export default router

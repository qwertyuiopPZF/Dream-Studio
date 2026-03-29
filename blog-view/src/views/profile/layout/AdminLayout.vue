<template>
  <WorkspaceLayoutShell
    variant="admin"
    :brand-title="profile.nickname || profile.username || '管理员'"
    :brand-subtitle="profile.role === 'ADMIN' ? 'Administrator' : 'Console User'"
    :brand-avatar-src="profile.avatar"
    :brand-avatar-fallback="userInitial"
    :grouped-menu-sections="groupedMenuSections"
    :active-section-label="activeSectionLabel"
    :current-page-title="currentPageTitle"
    :active-menu-path="menuActivePath"
    @brand-click="router.push('/home')"
    @menu-click="router.push($event)"
    @overview-click="router.push('/admin/overview')"
    @refresh-click="syncHeaderMeta"
  >
    <template #actions>
      <button type="button" class="topbar-icon-button" @click="router.push('/admin/notifications')">
        <el-icon><Bell /></el-icon>
        <span v-if="headerMeta.unreadNotificationCount" class="icon-badge">
          {{ formatCompactCount(headerMeta.unreadNotificationCount) }}
        </span>
      </button>

      <button type="button" class="topbar-icon-button" @click="router.push('/admin/reportmgmt')">
        <el-icon><Warning /></el-icon>
        <span v-if="headerMeta.pendingReportCount" class="icon-badge is-danger">
          {{ formatCompactCount(headerMeta.pendingReportCount) }}
        </span>
      </button>

      <button type="button" class="topbar-icon-button" @click="router.push('/admin/site')">
        <el-icon><Setting /></el-icon>
      </button>
    </template>

    <router-view v-slot="{ Component }">
      <transition name="app-fade" mode="out-in">
        <component :is="Component" :key="route.fullPath" />
      </transition>
    </router-view>
  </WorkspaceLayoutShell>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchAdminAnnouncements } from '@/api/admin/site'
import { fetchCurrentUserOverview } from '@/api/user'
import WorkspaceLayoutShell from '@/components/profile/workspace/WorkspaceLayoutShell.vue'
import { useUserStore } from '@/store/user'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'
import {
  Bell,
  ChatSquare,
  Comment,
  Discount,
  Document,
  EditPen,
  HomeFilled,
  House,
  Promotion,
  Odometer,
  Setting,
  UserFilled,
  WalletFilled,
  Warning,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const ADMIN_MENU_SECTIONS = [
  {
    key: 'overview',
    label: '仪表盘',
  },
  {
    key: 'content',
    label: '内容管理',
  },
  {
    key: 'community',
    label: '社区运维',
  },
  {
    key: 'system',
    label: '系统管理',
  },
]

const ADMIN_MENU_ITEMS = [
  {
    path: 'overview',
    label: '仪表盘',
    title: '管理概览',
    description: '快速查看后台统计卡片、年度贡献图和整站运行状态。',
    icon: Odometer,
    section: 'overview',
    tone: 'amber',
    requiredCapability: WORKSPACE_CAPABILITIES.DASHBOARD_VIEW,
  },
  {
    path: 'notifications',
    label: '通知中心',
    title: '后台通知中心',
    description: '集中处理站内通知、审核提醒和相关跳转入口。',
    icon: Bell,
    section: 'overview',
    tone: 'blue',
    requiredCapability: WORKSPACE_CAPABILITIES.PROFILE_NOTIFICATIONS_VIEW,
  },
  {
    path: 'articlemgmt',
    label: '内容管理',
    title: '文章管理',
    description: '集中维护文章内容、发布状态和文章列表。',
    icon: Document,
    section: 'content',
    tone: 'violet',
    requiredCapability: WORKSPACE_CAPABILITIES.ARTICLE_MANAGE_ALL,
  },
  {
    path: 'commentmgmt',
    label: '评论管理',
    title: '评论管理',
    description: '查看评论记录并处理需要清理或审核的留言。',
    icon: Comment,
    section: 'content',
    tone: 'blue',
    requiredCapability: WORKSPACE_CAPABILITIES.COMMENT_MODERATE,
  },
  {
    path: 'momentsmgmt',
    label: '动态管理',
    title: '动态管理',
    description: '维护时间轴动态内容和最近发布记录。',
    icon: House,
    section: 'content',
    tone: 'green',
    requiredCapability: WORKSPACE_CAPABILITIES.MOMENT_MANAGE_ALL,
  },
  {
    path: 'writearticle',
    label: '发布文章',
    title: '写文章',
    description: '新建文章草稿并继续当前写作流程。',
    icon: WalletFilled,
    section: 'content',
    tone: 'rose',
    requiredCapability: WORKSPACE_CAPABILITIES.ARTICLE_WRITE,
  },
  {
    path: 'writemoment',
    label: '发布动态',
    title: '发布动态',
    description: '快速发布一条新的站点动态。',
    icon: ChatSquare,
    section: 'content',
    tone: 'cyan',
    requiredCapability: WORKSPACE_CAPABILITIES.MOMENT_WRITE,
  },
  {
    path: 'categorisemgmt',
    label: '分类管理',
    title: '分类管理',
    description: '维护文章分类并整理内容结构。',
    icon: HomeFilled,
    section: 'content',
    tone: 'teal',
    requiredCapability: WORKSPACE_CAPABILITIES.TAXONOMY_MANAGE,
  },
  {
    path: 'tagsmgmt',
    label: '标签管理',
    title: '标签管理',
    description: '调整标签体系并统一标签命名。',
    icon: Discount,
    section: 'content',
    tone: 'orange',
    requiredCapability: WORKSPACE_CAPABILITIES.TAXONOMY_MANAGE,
  },
  {
    path: 'reportmgmt',
    label: '举报审核',
    title: '举报审核',
    description: '优先处理社区举报和待审反馈。',
    icon: Bell,
    section: 'community',
    tone: 'red',
    requiredCapability: WORKSPACE_CAPABILITIES.REPORT_REVIEW,
  },
  {
    path: 'forum-publish',
    label: '发布帖子',
    title: '发布帖子',
    description: '从后台直接发起新的论坛讨论。',
    icon: EditPen,
    section: 'community',
    tone: 'emerald',
    requiredCapability: WORKSPACE_CAPABILITIES.FORUM_POST_WRITE,
  },
  {
    path: 'forum-manage',
    label: '帖子管理',
    title: '帖子管理',
    description: '整理帖子状态并查看互动情况。',
    icon: Document,
    section: 'community',
    tone: 'indigo',
    requiredCapability: WORKSPACE_CAPABILITIES.FORUM_POST_MODERATE,
  },
  {
    path: 'usermgmt',
    label: '用户管理',
    title: '用户管理',
    description: '查看账号状态并调整管理员权限。',
    icon: UserFilled,
    section: 'community',
    tone: 'sky',
    requiredCapability: WORKSPACE_CAPABILITIES.USER_MANAGE,
  },
  {
    path: 'announcements',
    label: '公告管理',
    title: '公告管理',
    description: '单独维护站点公告，支持发布、查看和删除展示内容。',
    icon: Promotion,
    section: 'system',
    tone: 'amber',
    requiredCapability: WORKSPACE_CAPABILITIES.SITE_MANAGE,
  },
  {
    path: 'site',
    label: '网站管理',
    title: '站点设置',
    description: '维护站点名称、描述和页面基础配置。',
    icon: Setting,
    section: 'system',
    tone: 'slate',
    requiredCapability: WORKSPACE_CAPABILITIES.SITE_MANAGE,
  },
]

const headerMeta = reactive({
  unreadNotificationCount: 0,
  pendingReportCount: 0,
  announcementCount: 0,
})

const profile = computed(() => userStore.profile || {})
const userInitial = computed(() => profile.value.nickname?.slice(0, 1)?.toUpperCase() || 'A')

const formatCompactCount = (value) => {
  const numericValue = Number(value || 0)

  if (numericValue > 99) return '99+'
  return `${numericValue}`
}

const menuItems = computed(() =>
  ADMIN_MENU_ITEMS.map((item) => ({
    ...item,
    index: `/admin/${item.path}`,
      badge:
        item.path === 'reportmgmt' && headerMeta.pendingReportCount > 0
          ? formatCompactCount(headerMeta.pendingReportCount)
          : item.path === 'announcements' && headerMeta.announcementCount > 0
            ? formatCompactCount(headerMeta.announcementCount)
            : '',
  })).filter((item) => userStore.hasCapability(item.requiredCapability)),
)

const groupedMenuSections = computed(() =>
  ADMIN_MENU_SECTIONS.map((section) => ({
    ...section,
    items: menuItems.value.filter((item) => item.section === section.key),
  })).filter((section) => section.items.length),
)

const menuActivePath = computed(() => {
  if (route.path.startsWith('/admin/article/edit')) {
    return '/admin/writearticle'
  }

  if (route.path.startsWith('/admin/content')) {
    return '/admin/articlemgmt'
  }

  if (route.path.startsWith('/admin/forum-entry')) {
    return '/admin/forum-publish'
  }

  return route.path
})

const activeMenuItem = computed(() =>
  menuItems.value.find(
    (item) => menuActivePath.value === item.index || route.path.startsWith(`${item.index}/`),
  ),
)

const activeSectionLabel = computed(() => {
  const section = groupedMenuSections.value.find(
    (group) => group.key === activeMenuItem.value?.section,
  )
  return section?.label || '后台管理'
})

const currentPageTitle = computed(() => activeMenuItem.value?.title || '管理概览')

const syncHeaderMeta = async () => {
  const [userOverviewResult, announcementResult] = await Promise.allSettled([
    fetchCurrentUserOverview(),
    fetchAdminAnnouncements(),
  ])

  if (userOverviewResult.status === 'fulfilled') {
    const response = userOverviewResult.value || {}
    headerMeta.unreadNotificationCount = Number(response.unreadNotificationCount || 0)
    headerMeta.pendingReportCount = Number(response.pendingReportCount || 0)

    if (response.profile) {
      userStore.hydrateFromServerProfile(response.profile)
    }
  }

  if (announcementResult.status === 'fulfilled') {
    headerMeta.announcementCount = Array.isArray(announcementResult.value)
      ? announcementResult.value.length
      : 0
  }
}

watch(
  () => route.path,
  () => {
    syncHeaderMeta()
  },
  { immediate: true },
)
</script>

<template>
  <WorkspaceLayoutShell
    variant="user"
    :brand-title="profile.nickname || profile.username || '创作者'"
    :brand-subtitle="profile.role === 'ADMIN' ? 'Creator Console' : 'Creator Hub'"
    :brand-avatar-src="profile.avatar"
    :brand-avatar-fallback="userInitial"
    :grouped-menu-sections="groupedMenuSections"
    :active-section-label="activeSectionLabel"
    :current-page-title="currentPageTitle"
    :active-menu-path="menuActivePath"
    @brand-click="router.push('/home')"
    @menu-click="router.push($event)"
    @overview-click="router.push('/profile/overview')"
    @refresh-click="syncHeaderMeta"
  >
    <template #actions>
      <button type="button" class="topbar-icon-button" @click="router.push('/profile/notifications')">
        <el-icon><Bell /></el-icon>
        <span v-if="headerMeta.unreadNotificationCount" class="icon-badge">
          {{ formatCompactCount(headerMeta.unreadNotificationCount) }}
        </span>
      </button>

      <button type="button" class="topbar-icon-button" @click="router.push('/profile/account')">
        <el-icon><Setting /></el-icon>
      </button>

      <button
        v-if="canViewAdminDashboard"
        type="button"
        class="topbar-icon-button"
        @click="router.push('/admin/overview')"
      >
        <el-icon><Monitor /></el-icon>
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
import { fetchCurrentUserOverview } from '@/api/user'
import WorkspaceLayoutShell from '@/components/profile/workspace/WorkspaceLayoutShell.vue'
import { useUserStore } from '@/store/user'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'
import {
  Bell,
  ChatSquare,
  Document,
  EditPen,
  House,
  Monitor,
  Odometer,
  Setting,
  WalletFilled,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const USER_MENU_SECTIONS = [
  {
    key: 'overview',
    label: '仪表盘',
  },
  {
    key: 'content',
    label: '创作管理',
  },
  {
    key: 'community',
    label: '互动中心',
  },
  {
    key: 'account',
    label: '账号设置',
  },
]

const USER_MENU_ITEMS = [
  {
    path: 'overview',
    label: '仪表盘',
    title: '个人总览',
    description: '查看创作数据、消息提醒和最近活跃记录。',
    icon: Odometer,
    section: 'overview',
    tone: 'green',
    requiredCapability: WORKSPACE_CAPABILITIES.PROFILE_OVERVIEW_VIEW,
  },
  {
    path: 'notifications',
    label: '通知中心',
    title: '通知中心',
    description: '集中查看站内通知，并处理未读提醒。',
    icon: Bell,
    section: 'overview',
    tone: 'blue',
    requiredCapability: WORKSPACE_CAPABILITIES.PROFILE_NOTIFICATIONS_VIEW,
  },
  {
    path: 'articlemgmt',
    label: '我的文章',
    title: '我的文章',
    description: '维护文章列表并继续已有创作内容。',
    icon: Document,
    section: 'content',
    tone: 'violet',
    requiredCapability: WORKSPACE_CAPABILITIES.ARTICLE_MANAGE_OWN,
  },
  {
    path: 'momentsmgmt',
    label: '我的动态',
    title: '我的动态',
    description: '查看和整理你发布过的动态内容。',
    icon: House,
    section: 'content',
    tone: 'green',
    requiredCapability: WORKSPACE_CAPABILITIES.MOMENT_MANAGE_OWN,
  },
  {
    path: 'writearticle',
    label: '写文章',
    title: '写文章',
    description: '继续写作、保存草稿或发布新文章。',
    icon: WalletFilled,
    section: 'content',
    tone: 'rose',
    requiredCapability: WORKSPACE_CAPABILITIES.ARTICLE_WRITE,
  },
  {
    path: 'writemoment',
    label: '发动态',
    title: '发布动态',
    description: '快速发布新的图文动态到时间轴。',
    icon: ChatSquare,
    section: 'content',
    tone: 'cyan',
    requiredCapability: WORKSPACE_CAPABILITIES.MOMENT_WRITE,
  },
  {
    path: 'forum-publish',
    label: '发布帖子',
    title: '发布帖子',
    description: '开始新的讨论话题并同步到论坛。',
    icon: EditPen,
    section: 'community',
    tone: 'emerald',
    requiredCapability: WORKSPACE_CAPABILITIES.FORUM_POST_WRITE,
  },
  {
    path: 'forum-manage',
    label: '我的帖子',
    title: '我的帖子',
    description: '查看自己的论坛帖子和最近互动情况。',
    icon: Document,
    section: 'community',
    tone: 'indigo',
    requiredCapability: WORKSPACE_CAPABILITIES.FORUM_POST_MANAGE_OWN,
  },
  {
    path: 'account',
    label: '账号设置',
    title: '账号设置',
    description: '更新头像、密码和当前账号资料。',
    icon: Setting,
    section: 'account',
    tone: 'slate',
    requiredCapability: WORKSPACE_CAPABILITIES.PROFILE_ACCOUNT_EDIT,
  },
]

const headerMeta = reactive({
  unreadNotificationCount: 0,
  postCount: 0,
})

const profile = computed(() => userStore.profile || {})
const userInitial = computed(() => profile.value.nickname?.slice(0, 1)?.toUpperCase() || 'U')
const canViewAdminDashboard = computed(() =>
  userStore.hasCapability(WORKSPACE_CAPABILITIES.DASHBOARD_VIEW),
)

const formatCompactCount = (value) => {
  const numericValue = Number(value || 0)

  if (numericValue > 99) return '99+'
  return `${numericValue}`
}

const menuItems = computed(() =>
  USER_MENU_ITEMS.map((item) => ({
    ...item,
    index: `/profile/${item.path}`,
    badge:
      item.path === 'notifications' && headerMeta.unreadNotificationCount > 0
        ? formatCompactCount(headerMeta.unreadNotificationCount)
        : item.path === 'forum-manage' && headerMeta.postCount > 0
          ? formatCompactCount(headerMeta.postCount)
          : '',
  })).filter((item) => userStore.hasCapability(item.requiredCapability)),
)

const groupedMenuSections = computed(() =>
  USER_MENU_SECTIONS.map((section) => ({
    ...section,
    items: menuItems.value.filter((item) => item.section === section.key),
  })).filter((section) => section.items.length),
)

const menuActivePath = computed(() => {
  if (route.path.startsWith('/profile/article/edit')) {
    return '/profile/writearticle'
  }

  if (route.path.startsWith('/profile/content')) {
    return '/profile/articlemgmt'
  }

  if (route.path.startsWith('/profile/forum-entry')) {
    return '/profile/forum-publish'
  }

  return route.path
})

const activeMenuItem = computed(() =>
  menuItems.value.find(
    (item) => menuActivePath.value === item.index || route.path.startsWith(`${item.index}/`),
  ),
)

const activeSectionLabel = computed(() => {
  const section = groupedMenuSections.value.find((group) => group.key === activeMenuItem.value?.section)
  return section?.label || '个人中心'
})

const currentPageTitle = computed(() => activeMenuItem.value?.title || '个人总览')

const syncHeaderMeta = async () => {
  try {
    const response = await fetchCurrentUserOverview()
    headerMeta.unreadNotificationCount = Number(response?.unreadNotificationCount || 0)
    headerMeta.postCount = Number(response?.postCount || 0)

    if (response?.profile) {
      userStore.hydrateFromServerProfile(response.profile)
    }
  } catch (error) {
    console.error('同步个人中心顶部数据失败', error)
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

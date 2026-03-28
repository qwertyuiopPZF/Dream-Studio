<template>
  <div class="workspace-shell" :class="`mode-${mode}`">
    <aside class="workspace-sidebar">
      <button type="button" class="workspace-brand" @click="router.push('/home')">
        <el-avatar :size="50" :src="profile.avatar">{{ userInitial }}</el-avatar>
        <div class="workspace-brand-meta">
          <span class="brand-kicker">{{ workspaceConfig.brandKicker }}</span>
          <h3>{{ profile.nickname || workspaceConfig.defaultTitle }}</h3>
          <p>{{ workspaceConfig.brandDescription }}</p>
        </div>
      </button>

      <el-scrollbar class="workspace-menu-scroll">
        <el-menu :default-active="menuActivePath" class="workspace-menu" router>
          <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.label }}</span>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </aside>

    <div class="workspace-main">
      <header class="workspace-header">
        <div class="workspace-header-main">
          <el-breadcrumb separator=">">
            <el-breadcrumb-item @click="router.push(`${basePath}/articlemgmt`)">
              {{ workspaceConfig.breadcrumbLabel }}
            </el-breadcrumb-item>
          </el-breadcrumb>
          <div class="page-kicker">{{ workspaceConfig.pageKicker }}</div>
          <h1>{{ currentPageTitle }}</h1>
          <p>{{ currentPageDescription }}</p>
        </div>
      </header>

      <main class="workspace-content">
        <router-view v-slot="{ Component }">
          <transition name="app-fade" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'
import {
  Bell,
  ChatSquare,
  Comment,
  Document,
  Discount,
  EditPen,
  HomeFilled,
  House,
  Odometer,
  Setting,
  UserFilled,
  WalletFilled,
} from '@element-plus/icons-vue'

const props = defineProps({
  mode: {
    type: String,
    required: true,
    validator: (value) => ['user', 'admin'].includes(value),
  },
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const WORKSPACE_CONFIGS = {
  user: {
    brandKicker: 'Dream Studio',
    breadcrumbLabel: '个人中心',
    defaultTitle: '个人总览',
    brandDescription: '查看个人数据、管理创作内容并跟进互动反馈。',
    pageKicker: 'Personal Workspace',
    tagText: '个人中心',
    tagType: 'warning',
    homeDescription: '把个人创作、通知和互动反馈集中到一个独立空间里维护。',
  },
  admin: {
    brandKicker: 'Dream Studio',
    breadcrumbLabel: '后台管理',
    defaultTitle: '管理概览',
    brandDescription: '集中查看站点状态，并维护内容、社区和基础配置。',
    pageKicker: 'Admin Console',
    tagText: '管理员模式',
    tagType: 'success',
    homeDescription: '查看并维护整站内容、互动数据和站点配置。',
  },
}

const WORKSPACE_MENUS = {
  user: [
    {
      path: 'overview',
      label: '总览',
      title: '个人总览',
      description: '查看个人资料、通知提醒、创作数据和最近互动。',
      icon: Odometer,
      requiredCapability: WORKSPACE_CAPABILITIES.PROFILE_OVERVIEW_VIEW,
    },
    {
      path: 'notifications',
      label: '通知中心',
      title: '通知中心',
      description: '查看站内通知，并按需标记为已读。',
      icon: Bell,
      requiredCapability: WORKSPACE_CAPABILITIES.PROFILE_NOTIFICATIONS_VIEW,
    },
    {
      path: 'account',
      label: '账号设置',
      title: '账号设置',
      description: '更新头像、设置站内密码并确认当前账号资料。',
      icon: Setting,
      requiredCapability: WORKSPACE_CAPABILITIES.PROFILE_ACCOUNT_EDIT,
    },
    {
      path: 'articlemgmt',
      label: '我的文章',
      title: '我的文章',
      description: '查看、筛选和维护你发布过的文章。',
      icon: HomeFilled,
      requiredCapability: WORKSPACE_CAPABILITIES.ARTICLE_MANAGE_OWN,
    },
    {
      path: 'momentsmgmt',
      label: '我的动态',
      title: '我的动态',
      description: '集中查看和删除自己发布的动态内容。',
      icon: House,
      requiredCapability: WORKSPACE_CAPABILITIES.MOMENT_MANAGE_OWN,
    },
    {
      path: 'writearticle',
      label: '写文章',
      title: '写文章',
      description: '继续写作、保存草稿或直接发布文章。',
      icon: WalletFilled,
      requiredCapability: WORKSPACE_CAPABILITIES.ARTICLE_WRITE,
    },
    {
      path: 'writemoment',
      label: '发动态',
      title: '发布动态',
      description: '记录近况并把图文动态发布到时间轴。',
      icon: ChatSquare,
      requiredCapability: WORKSPACE_CAPABILITIES.MOMENT_WRITE,
    },
    {
      path: 'forum-publish',
      label: '发布帖子',
      title: '发布帖子',
      description: '开始新的讨论，把内容同步到论坛页面。',
      icon: EditPen,
      requiredCapability: WORKSPACE_CAPABILITIES.FORUM_POST_WRITE,
    },
    {
      path: 'forum-manage',
      label: '我的帖子',
      title: '我的帖子',
      description: '查看、整理和删除自己发布的论坛帖子。',
      icon: Document,
      requiredCapability: WORKSPACE_CAPABILITIES.FORUM_POST_MANAGE_OWN,
    },
  ],
  admin: [
    {
      path: 'overview',
      label: '总览',
      title: '管理概览',
      description: '总览整站内容状态、待处理事项和运营趋势。',
      icon: Odometer,
      requiredCapability: WORKSPACE_CAPABILITIES.DASHBOARD_VIEW,
    },
    {
      path: 'articlemgmt',
      label: '文章管理',
      title: '文章管理',
      description: '统一维护站点文章内容、状态和发布节奏。',
      icon: HomeFilled,
      requiredCapability: WORKSPACE_CAPABILITIES.ARTICLE_MANAGE_ALL,
    },
    {
      path: 'commentmgmt',
      label: '评论管理',
      title: '评论管理',
      description: '审核评论内容并快速定位到对应页面。',
      icon: Comment,
      requiredCapability: WORKSPACE_CAPABILITIES.COMMENT_MODERATE,
    },
    {
      path: 'reportmgmt',
      label: '举报审核',
      title: '举报审核',
      description: '处理论坛举报，并记录审核结论与备注。',
      icon: Bell,
      requiredCapability: WORKSPACE_CAPABILITIES.REPORT_REVIEW,
    },
    {
      path: 'momentsmgmt',
      label: '动态管理',
      title: '动态管理',
      description: '查看、清理和维护站点动态内容。',
      icon: House,
      requiredCapability: WORKSPACE_CAPABILITIES.MOMENT_MANAGE_ALL,
    },
    {
      path: 'writearticle',
      label: '写文章',
      title: '写文章',
      description: '新建文章、保存草稿或继续编辑既有内容。',
      icon: WalletFilled,
      requiredCapability: WORKSPACE_CAPABILITIES.ARTICLE_WRITE,
    },
    {
      path: 'writemoment',
      label: '发布动态',
      title: '发布动态',
      description: '以管理员身份快速发布新的站点动态。',
      icon: ChatSquare,
      requiredCapability: WORKSPACE_CAPABILITIES.MOMENT_WRITE,
    },
    {
      path: 'categorisemgmt',
      label: '分类管理',
      title: '分类管理',
      description: '集中维护文章分类并保持内容结构清晰。',
      icon: UserFilled,
      requiredCapability: WORKSPACE_CAPABILITIES.TAXONOMY_MANAGE,
    },
    {
      path: 'tagsmgmt',
      label: '标签管理',
      title: '标签管理',
      description: '统一整理文章标签和站点内容索引。',
      icon: Discount,
      requiredCapability: WORKSPACE_CAPABILITIES.TAXONOMY_MANAGE,
    },
    {
      path: 'forum-publish',
      label: '发布帖子',
      title: '发布帖子',
      description: '从后台直接发起论坛讨论并发布内容。',
      icon: EditPen,
      requiredCapability: WORKSPACE_CAPABILITIES.FORUM_POST_WRITE,
    },
    {
      path: 'forum-manage',
      label: '帖子管理',
      title: '帖子管理',
      description: '管理论坛帖子状态、置顶和加精展示。',
      icon: Document,
      requiredCapability: WORKSPACE_CAPABILITIES.FORUM_POST_MODERATE,
    },
    {
      path: 'site',
      label: '站点设置',
      title: '站点设置',
      description: '维护站点信息、公告和论坛页展示内容。',
      icon: Setting,
      requiredCapability: WORKSPACE_CAPABILITIES.SITE_MANAGE,
    },
    {
      path: 'usermgmt',
      label: '用户管理',
      title: '用户管理',
      description: '查看账号状态，并设置管理员或删除用户。',
      icon: UserFilled,
      requiredCapability: WORKSPACE_CAPABILITIES.USER_MANAGE,
    },
  ],
}

const profile = computed(() => userStore.profile || {})
const basePath = computed(() => (props.mode === 'admin' ? '/admin' : '/profile'))
const workspaceConfig = computed(() => WORKSPACE_CONFIGS[props.mode])
const menuItems = computed(() =>
  WORKSPACE_MENUS[props.mode].map((item) => ({
    ...item,
    index: `${basePath.value}/${item.path}`,
  })).filter((item) => userStore.hasCapability(item.requiredCapability)),
)
const userInitial = computed(() => profile.value.nickname?.slice(0, 1)?.toUpperCase() || 'D')

const findCurrentMenuItem = () => {
  const currentPath = route.path
  return menuItems.value.find(
    (item) => currentPath === item.index || currentPath.startsWith(`${item.index}/`),
  )
}

const menuActivePath = computed(() => {
  if (route.path.startsWith(`${basePath.value}/article/edit`)) {
    return `${basePath.value}/writearticle`
  }

  if (route.path.startsWith(`${basePath.value}/content`)) {
    return `${basePath.value}/articlemgmt`
  }

  if (route.path.startsWith(`${basePath.value}/forum-entry`)) {
    return `${basePath.value}/forum-publish`
  }

  return route.path
})

const currentPageTitle = computed(() => {
  if (route.path.startsWith(`${basePath.value}/article/edit`)) {
    return '编辑文章'
  }

  return findCurrentMenuItem()?.title || workspaceConfig.value.defaultTitle
})

const currentPageDescription = computed(() => {
  if (route.path.startsWith(`${basePath.value}/article/edit`)) {
    return props.mode === 'admin'
      ? '继续调整文章内容和发布设置，更新后会回到文章管理页。'
      : '继续打磨文章内容，更新后会返回个人文章列表。'
  }

  return findCurrentMenuItem()?.description || workspaceConfig.value.homeDescription
})
</script>

<style scoped>
.workspace-shell {
  display: grid;
  grid-template-columns: 268px minmax(0, 1fr);
  align-items: start;
  min-height: 100vh;
}

.workspace-shell.mode-user {
  --workspace-accent: #ea580c;
  --workspace-accent-soft: rgba(234, 88, 12, 0.12);
  --workspace-title: #7c2d12;
  --workspace-surface: rgba(255, 251, 245, 0.9);
  --workspace-sidebar-bg: rgba(255, 247, 237, 0.86);
  background:
    radial-gradient(circle at top left, rgba(255, 237, 213, 0.96), transparent 34%),
    linear-gradient(180deg, #fff8f1 0%, #fffdf9 38%, #ffffff 100%);
}

.workspace-shell.mode-admin {
  --workspace-accent: #0f766e;
  --workspace-accent-soft: rgba(15, 118, 110, 0.12);
  --workspace-title: #134e4a;
  --workspace-surface: rgba(247, 252, 252, 0.9);
  --workspace-sidebar-bg: rgba(240, 253, 250, 0.82);
  background:
    radial-gradient(circle at top left, rgba(204, 251, 241, 0.96), transparent 34%),
    linear-gradient(180deg, #f0fdfa 0%, #f8fafc 36%, #ffffff 100%);
}

.workspace-sidebar {
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100vh;
  padding: 24px 18px;
  border-right: 1px solid rgba(15, 23, 42, 0.08);
  background: var(--workspace-sidebar-bg);
  backdrop-filter: blur(12px);
  box-sizing: border-box;
  overflow: hidden;
}

.workspace-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 16px;
  border: 0;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(255, 255, 255, 0.72));
  text-align: left;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.workspace-brand:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.14);
}

.workspace-brand-meta h3,
.workspace-header h1 {
  margin: 0;
  color: var(--workspace-title);
}

.workspace-brand-meta p,
.workspace-header p {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.7;
}

.brand-kicker,
.page-kicker {
  display: block;
  margin-bottom: 6px;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(100, 116, 139, 0.88);
}

.workspace-menu {
  border-right: 0;
  background: transparent;
}

.workspace-menu-scroll {
  height: 100%;
}

:deep(.workspace-menu .el-menu-item) {
  height: 52px;
  margin-bottom: 6px;
  border-radius: 12px;
}

:deep(.workspace-menu .el-menu-item:hover) {
  background: var(--workspace-accent-soft);
}

:deep(.workspace-menu .el-menu-item.is-active) {
  background: var(--workspace-accent-soft);
  color: var(--workspace-accent);
}

.workspace-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.workspace-header {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-start;
  padding: 24px 24px 14px;
}

.workspace-header-main {
  max-width: 760px;
}

.workspace-header h1 {
  margin-top: 8px;
  font-size: clamp(30px, 4vw, 40px);
  line-height: 1.08;
}

:deep(.el-breadcrumb__item) {
  cursor: pointer;
}

:deep(.el-breadcrumb__inner) {
  color: #64748b;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.workspace-content {
  min-width: 0;
  padding: 0 24px 24px;
}

@media (max-width: 960px) {
  .workspace-shell {
    grid-template-columns: 1fr;
  }

  .workspace-sidebar {
    position: static;
    height: auto;
    border-right: 0;
    border-bottom: 1px solid rgba(15, 23, 42, 0.08);
    overflow: visible;
  }

  .workspace-header {
    flex-direction: column;
  }
}

@media (max-width: 720px) {
  .workspace-content,
  .workspace-header {
    padding-left: 18px;
    padding-right: 18px;
  }

  .header-actions {
    width: 100%;
  }
}
</style>

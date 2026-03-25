<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <button type="button" class="admin-brand" @click="router.push('/home')">
        <el-avatar :size="50" :src="profile.avatar">{{ userInitial }}</el-avatar>
        <div class="admin-brand-meta">
          <span class="brand-kicker">Dream Studio</span>
          <h3>{{ profile.nickname || '管理员' }}</h3>
        </div>
      </button>

      <el-scrollbar class="admin-menu-scroll">
        <el-menu :default-active="menuActivePath" class="admin-menu" router>
          <el-menu-item index="/admin/home">
            <el-icon><Platform /></el-icon>
            <span>Home</span>
          </el-menu-item>
          <el-menu-item index="/admin/articlemgmt">
            <el-icon><HomeFilled /></el-icon>
            <span>文章管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/writearticle">
            <el-icon><WalletFilled /></el-icon>
            <span>编辑文章</span>
          </el-menu-item>
          <el-menu-item index="/admin/momentsmgmt">
            <el-icon><House /></el-icon>
            <span>动态管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/writemoment">
            <el-icon><ChatSquare /></el-icon>
            <span>编辑动态</span>
          </el-menu-item>
          <el-menu-item index="/admin/categorisemgmt">
            <el-icon><UserFilled /></el-icon>
            <span>分类管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/commentmgmt">
            <el-icon><Comment /></el-icon>
            <span>评论管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/tagsmgmt">
            <el-icon><Discount /></el-icon>
            <span>标签管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/forum-entry">
            <el-icon><ChatDotRound /></el-icon>
            <span>Forum 入口</span>
          </el-menu-item>
          <el-menu-item index="/admin/site">
            <el-icon><Setting /></el-icon>
            <span>站点配置</span>
          </el-menu-item>
        </el-menu>
      </el-scrollbar>
    </aside>

    <div class="admin-main">
      <header class="admin-header">
        <div>
          <el-breadcrumb separator=">">
            <el-breadcrumb-item @click="router.push('/profile')">个人中心</el-breadcrumb-item>
            <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
          <div class="page-kicker">Admin Workspace</div>
          <h1>{{ pageTitle }}</h1>
        </div>

        <div class="header-actions">
          <el-tag type="success">管理员模式</el-tag>
          <el-button text @click="router.push('/home')">前台首页</el-button>
        </div>
      </header>

      <main class="admin-content">
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
import {
  ChatDotRound,
  ChatSquare,
  Comment,
  Discount,
  HomeFilled,
  House,
  Platform,
  Setting,
  UserFilled,
  WalletFilled,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const profile = computed(() => userStore.profile || {})
const userInitial = computed(() => profile.value.nickname?.slice(0, 1)?.toUpperCase() || 'A')

const menuActivePath = computed(() => {
  if (route.path.startsWith('/admin/article/edit')) return '/admin/writearticle'
  if (route.path.startsWith('/admin/content')) return '/admin/articlemgmt'
  if (route.path.startsWith('/admin/site')) return '/admin/categorisemgmt'
  return route.path
})

const pageTitle = computed(() => {
  if (route.path.startsWith('/admin/article/edit')) return '编辑文章'
  if (route.path.startsWith('/admin/writearticle')) return '写文章'
  if (route.path.startsWith('/admin/writemoment')) return '发布动态'
  if (route.path.startsWith('/admin/articlemgmt')) return '文章管理'
  if (route.path.startsWith('/admin/commentmgmt')) return '评论管理'
  if (route.path.startsWith('/admin/momentsmgmt')) return '动态管理'
  if (route.path.startsWith('/admin/categorisemgmt')) return '分类管理'
  if (route.path.startsWith('/admin/tagsmgmt')) return '标签管理'
  if (route.path.startsWith('/admin/forum-entry')) return 'Forum 入口'
  if (route.path.startsWith('/admin/content')) return '内容管理'
  if (route.path.startsWith('/admin/site')) return '站点配置'
  return '后台总览'
})
</script>

<style scoped>
.admin-shell {
  display: grid;
  grid-template-columns: 248px minmax(0, 1fr);
  align-items: start;
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(217, 242, 255, 0.9), transparent 28%),
    linear-gradient(180deg, #eef5ff 0%, #f7fafc 32%, #ffffff 100%);
}

.admin-sidebar {
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100vh;
  padding: 24px 18px;
  border-right: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(12px);
  box-sizing: border-box;
  overflow: hidden;
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 14px;
  border: 0;
  border-radius: 22px;
  text-align: left;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.admin-brand:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.2);
}


.brand-kicker,
.admin-brand-meta small,
.page-kicker {
  display: block;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.brand-kicker,
.page-kicker {
  font-size: 11px;
  color: rgba(148, 163, 184, 0.92);
}

.admin-brand-meta strong {
  display: block;
  margin: 6px 0 4px;
  font-size: 24px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.admin-brand-meta small {
  font-size: 12px;
  line-height: 1.6;
  color: rgba(226, 232, 240, 0.88);
}

.admin-menu {
  border-right: 0;
  background: transparent;
}

.admin-menu-scroll {
  height: calc(100vh - 260px);
}

:deep(.admin-menu .el-menu-item) {
  height: 50px;
  margin-bottom: 6px;
  border-radius: 8px;
}

:deep(.admin-menu .el-menu-item.is-active) {
  background: rgba(59, 130, 246, 0.1);
  color: #1d4ed8;
}

.admin-sidebar-footer {
  margin-top: auto;
}

.admin-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  padding: 24px 28px 16px;
}

.admin-header h1 {
  margin: 6px 0 0;
  font-size: 32px;
  line-height: 1.1;
  color: #0f172a;
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
}

.admin-content {
  min-width: 0;
  padding: 0 28px 28px;
}

@media (max-width: 960px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }

  .admin-sidebar {
    position: static;
    height: auto;
    border-right: 0;
    border-bottom: 1px solid rgba(15, 23, 42, 0.08);
    overflow: visible;
  }

  .admin-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 720px) {
  .admin-content,
  .admin-header {
    padding-left: 18px;
    padding-right: 18px;
  }
}
</style>

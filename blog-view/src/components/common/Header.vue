<template>
  <div class="header-wrapper" :class="{ 'is-hidden': isHeaderHidden }">
    <div class="header-container">
      <div class="logo">
<<<<<<< HEAD
        <a href="/home">Dream-studio </a>
=======
        <router-link to="/home">Dream-studio</router-link>
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
      </div>
      <div class="search-bar">
        <el-autocomplete
          v-model="searchInput"
          class="search-input"
          placeholder="Please Input"
          :prefix-icon="Search"
          :fetch-suggestions="querySearchArticles"
          :trigger-on-focus="false"
          value-key="title"
          clearable
          @select="handleSelectArticle"
          :popper-append-to-body="false"
        />
      </div>
      <el-menu
        :default-active="activeIndex"
        class="nav-menu"
        mode="horizontal"
        background-color="transparent"
        :text-color="'var(--app-text-color)'"
        :active-text-color="'var(--app-text-color)'"
        :ellipsis="false"
        :router="true"
      >
        <el-menu-item index="/home"
          ><el-icon><HomeFilled /></el-icon>Home</el-menu-item
        >
<<<<<<< HEAD
        <el-sub-menu index="/categories">
=======
        <el-sub-menu index="categories-menu">
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
          <template #title
            ><el-icon><Grid /></el-icon>categories</template
          >
          <el-menu-item
            v-for="category in categories"
            :key="category.id"
            :index="`/category/${category.id}`"
          >
            {{ category.name }}
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/moment"
          ><el-icon><Comment /></el-icon>Moment</el-menu-item
        >

        <el-menu-item index="/forum"
          ><el-icon><ChatDotRound /></el-icon>Forum</el-menu-item
        >
        <el-menu-item index="/archive"
          ><el-icon><WalletFilled /></el-icon>Archive</el-menu-item
        >
        <el-menu-item index="/about"
          ><el-icon><UserFilled /></el-icon>About</el-menu-item
        >
      </el-menu>
      <div class="github-link">
        <a href="https://github.com/Eleven-Mouse/Dream-Studio" target="_blank">
          <span>GitHub</span> <el-icon size="15"><Promotion /></el-icon>
        </a>
      </div>
      <div class="user-entry" @click="goToProfile">
        <el-badge v-if="isAdmin" value="Admin" class="user-badge">
          <el-avatar :size="36" :src="userAvatar">{{ userInitial }}</el-avatar>
        </el-badge>
        <el-avatar v-else :size="36" :src="userAvatar">{{ userInitial }}</el-avatar>
        <div class="user-meta">
          <span class="user-name">{{ displayName }}</span>
<<<<<<< HEAD
          <small>{{ isAdmin ? '后台管理' : isLoggedIn ? '个人中心' : '游客模式' }}</small>
=======
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
        </div>
      </div>
      <el-button v-if="isLoggedIn" text class="logout-button" @click.stop="handleLogout"
        >退出登录</el-button
      >
      <theme-switcher class="theme-switch" />
    </div>
    <!-- 页面滚动进度条 -->
    <el-progress
      :percentage="scrollProgress"
      :stroke-width="2.5"
      :show-text="false"
      class="scroll-progress"
      status="success"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchCategories } from '@/api/categories'
import { fetchArticles } from '@/api/article.js'
import { useAuthStore } from '@/store/auth'
import { useUserStore } from '@/store/user'
import { openAdminApp } from '@/utils/adminBridge'
import {
  Search,
  HomeFilled,
  Grid,
  UserFilled,
  WalletFilled,
  Promotion,
  Comment,
  ChatDotRound,
} from '@element-plus/icons-vue'
import ThemeSwitcher from './ThemeSwitcher.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()
const activeIndex = ref('/home')
const searchInput = ref('')
const categories = ref([])
const searchLoading = ref(false)
const currentCategoryId = ref(null)
const scrollProgress = ref(0)
const isHeaderHidden = ref(false)

const HEADER_HIDE_THRESHOLD = 90
const HEADER_DIRECTION_THRESHOLD = 6

const currentProfile = computed(() => userStore.profile)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const displayName = computed(() => currentProfile.value.nickname)
const userAvatar = computed(() => currentProfile.value.avatar)
const userInitial = computed(() => displayName.value?.slice(0, 1)?.toUpperCase() || 'D')
const isAdmin = computed(() => userStore.isAdmin)

// 获取分类列表
const loadCategories = async () => {
  try {
    categories.value = await fetchCategories()
  } catch (error) {
    console.error('获取分类列表失败', error)
  }
}

// 监听路由变化，更新导航菜单激活状态
watch(
  () => route.path,
  (newPath) => {
    if (newPath === '/home' || newPath === '/') {
      activeIndex.value = '/home'
    } else if (newPath.startsWith('/forum')) {
      activeIndex.value = '/forum'
    } else if (newPath.startsWith('/category/')) {
<<<<<<< HEAD
      activeIndex.value = '/categories'
=======
      activeIndex.value = 'categories-menu'
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
      currentCategoryId.value = route.params.id
    } else {
      activeIndex.value = newPath
    }
  },
  { immediate: true },
)

// 监听路由变化，获取categoryId
watch(
  () => route.params.id,
  (newId) => {
    if (route.path.startsWith('/category/')) {
      currentCategoryId.value = newId
    }
  },
  { immediate: true },
)

let scrollContainer = null

const showHeader = () => {
  isHeaderHidden.value = false
}

const updateHeaderVisibility = (scrollTop) => {
  const previousScrollTop = scrollContainer?.dataset.lastScrollTop
    ? Number(scrollContainer.dataset.lastScrollTop)
    : 0
  const delta = scrollTop - previousScrollTop

  if (scrollTop <= HEADER_HIDE_THRESHOLD) {
    showHeader()
  } else if (delta > HEADER_DIRECTION_THRESHOLD) {
    isHeaderHidden.value = true
  } else if (delta < -HEADER_DIRECTION_THRESHOLD) {
    showHeader()
  }

  if (scrollContainer) {
    scrollContainer.dataset.lastScrollTop = String(scrollTop)
  }
}

const handleMainScroll = (e) => {
  // 注意：这里我们通过 e.target 获取滚动的元素
  const target = e.target
  if (!target) return

  const scrollTop = target.scrollTop
  const scrollHeight = target.scrollHeight
  const clientHeight = target.clientHeight

  const scrollableHeight = scrollHeight - clientHeight
  const progress = scrollableHeight > 0 ? (scrollTop / scrollableHeight) * 100 : 0

  scrollProgress.value = Math.min(Math.max(progress, 0), 100)
  updateHeaderVisibility(scrollTop)
}

onMounted(() => {
  loadCategories()
  // 监听页面滚动
  // 注意：querySelector 前面的点 . 代表 class
  scrollContainer = document.querySelector('.main-scroll-container')

  // 2. 如果找到了，就手动添加原生事件监听
  if (scrollContainer) {
    scrollContainer.dataset.lastScrollTop = String(scrollContainer.scrollTop)
    scrollContainer.addEventListener('scroll', handleMainScroll)
  } else {
    console.warn('未找到滚动容器 .main-scroll-container')
  }
})

onUnmounted(() => {
  // 3. 组件销毁时，记得移除监听，防止内存泄漏
  if (scrollContainer) {
    scrollContainer.removeEventListener('scroll', handleMainScroll)
  }
})

watch(
  () => route.fullPath,
  () => {
    showHeader()
    if (scrollContainer) {
      scrollContainer.dataset.lastScrollTop = String(scrollContainer.scrollTop)
    }
  },
)

// 自动补全：根据输入关键字异步获取文章标题列表
const querySearchArticles = async (queryString, cb) => {
  const keyword = queryString.trim()
  if (!keyword) {
    cb([])
    return
  }

  searchLoading.value = true
  try {
    const res = await fetchArticles({
      page: 1,
      size: 5,
      keyword,
    })
    const list = res?.data || []
    cb(list)
  } catch (e) {
    console.error('搜索文章失败', e)
    cb([])
  } finally {
    searchLoading.value = false
  }
}

// 选择某个搜索建议后，跳转到文章详情，并清空搜索框
const handleSelectArticle = (item) => {
  if (!item || !item.id) return
  router.push(`/article/${item.id}`)
  // 跳转后清空输入内容
  searchInput.value = ''
}

const goToProfile = () => {
  if (isAdmin.value) {
    openAdminApp({
      isAdmin: true,
      accessToken: authStore.accessToken,
      router,
<<<<<<< HEAD
=======
      targetPath: '/admin/articlemgmt',
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
    })
    return
  }

<<<<<<< HEAD
  router.push('/profile')
=======
  router.push('/profile/articlemgmt')
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
}

const handleLogout = async () => {
  authStore.logout()
}
</script>

<style scoped>
.header-container {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 58px;
  padding: 0 22px;
  border: 1px solid var(--header-glass-border, rgba(255, 255, 255, 0.66));
  border-radius: 22px;
  background: var(--header-glass-bg, rgba(255, 255, 255, 0.56));
  box-shadow: var(--header-glass-shadow);
  backdrop-filter: blur(18px) saturate(160%);
  -webkit-backdrop-filter: blur(18px) saturate(160%);
}
/* 针对输入框的焦点状态 */
.search-bar ::v-deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--app-text-color) inset !important; /* !important确保覆盖默认样式 */
}

.logo a {
  font-size: 22px;
  font-weight: bold;
  color: var(--app-text-color);
  text-decoration: none;
  margin-right: 20px;
<<<<<<< HEAD
  background-image: url('../assets/\ \(4\).png');
=======
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
}

.search-bar {
  width: 220px;
  margin: 0 auto;
}

.search-bar .el-input {
  --el-input-bg-color: rgba(255, 255, 255, 0.44);
  --el-input-text-color: var(--app-text-color);
  --el-input-border-color: rgba(148, 163, 184, 0.18);
}

.nav-menu {
  border-bottom: none;
}

.nav-menu .el-menu-item:hover,
.nav-menu .el-sub-menu__title:hover {
  background-color: rgba(255, 255, 255, 0.34) !important;
}
/* 带有子菜单的标题悬停 */
:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.34) !important;
}

:deep(.el-menu--horizontal > .el-menu-item.is-active),
:deep(.el-menu--horizontal > .el-sub-menu.is-active .el-sub-menu__title) {
  background: rgba(255, 255, 255, 0.3) !important;
  border-bottom-color: rgba(109, 160, 81, 0.45) !important;
}
.github-link {
  margin-left: auto;
}

.github-link a {
  color: var(--app-text-color);
  text-decoration: none;
  display: flex;
  align-items: center;
}

.github-link a:hover {
  color: var(--app-text-color);
}

.github-link span {
  margin-left: 8px;
}

.theme-switch {
  margin-left: 16px;
}

.logout-button {
  margin-left: 10px;
}

.user-entry {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: 18px;
  padding: 6px 10px;
  border-radius: 999px;
  cursor: pointer;
  transition: background-color 0.25s ease;
}

.user-entry:hover {
  background: rgba(255, 255, 255, 0.34);
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-name {
  color: var(--app-text-color);
  font-size: 14px;
  font-weight: 600;
}

.user-meta small {
  color: #8d8d8d;
}

.user-badge :deep(.el-badge__content) {
  transform: translateY(-30%) translateX(30%);
}

@media screen and (max-width: 1200px) {
  .user-meta {
    display: none;
  }

  .logout-button {
    padding: 0 6px;
  }
}

@media screen and (max-width: 700px) {
  .header-wrapper {
    padding: 0px 4px 0;
  }

  .header-container {
    padding: 0 4px;
  }
}

.header-wrapper {
  position: relative;
  width: 100%;
  max-width: 100vw;
  margin: 0 auto;

  box-sizing: border-box;
  transform: translateY(0);
  opacity: 1;
  filter: blur(0);
  transition:
    transform 0.56s cubic-bezier(0.22, 1, 0.36, 1),
    opacity 0.4s ease,
    filter 0.42s ease;
  will-change: transform, opacity, filter;
}

.header-wrapper.is-hidden {
  transform: translateY(calc(-100% - 18px));
  opacity: 0;
  filter: blur(8px);
  pointer-events: none;
}

.scroll-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  border-radius: 0;
}
</style>

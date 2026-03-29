<template>
  <div class="forum-container">
    <div class="forum-shell">
      <aside class="left-sidebar">
        <div class="panel-card side-panel-shell left-panel-shell">
          <section class="left-section">
            <div class="sidebar-title">论坛页面</div>
            <div class="left-nav-list">
              <button
                v-for="item in leftNavItems"
                :key="item.key"
                type="button"
                class="left-nav-item"
                :class="{ active: sort === item.key }"
                @click="changeSort(item.key)"
              >
                <span>{{ item.label }}</span>
                <em>{{ item.count }}</em>
              </button>
            </div>
          </section>

          <section class="left-section left-section-info">
            <div class="sidebar-title secondary">参与关注</div>
            <div class="left-note">
              论坛页现在专注于浏览、评论、点赞与分享，发帖入口已经迁移到个人中心的独立发帖页。
            </div>
          </section>
        </div>
      </aside>

      <main class="center-content">
        <div v-if="loading" class="loading-tip">正在加载帖子...</div>
        <div v-else-if="error" class="error-tip">{{ error }}</div>
        <template v-else>
          <div  class="post-list">
            <ForumPostCard v-for="post in filteredPosts" :key="post.id" :post="post" />
          </div>
     

          <div v-if="pagination.total > pagination.size" class="post-pagination">
            <el-pagination
              :current-page="pagination.currentPage"
              :page-size="pagination.size"
              :total="pagination.total"
              layout="total, prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </template>
      </main>

      <aside class="right-sidebar">
        <div class="panel-card side-panel-shell right-panel-shell">
          <div class="side-panel-scroll">
            <section class="sidebar-card side-inner-card">
              <div class="panel-title">热门帖子</div>
              <div v-if="hotPosts.length" class="hot-posts-list">
                <router-link
                  v-for="post in hotPosts"
                  :key="post.id"
                  :to="`/forum/${post.id}`"
                  class="hot-post-link"
                >
                  <span class="hot-post-title">{{ post.title }}</span>
                  <small>{{ post.commentCount || 0 }} 评论</small>
                </router-link>
              </div>
              <div v-else class="empty-tip">暂无热门帖子</div>
            </section>

            <section class="sidebar-card side-inner-card">
              <div class="panel-title">热门标签</div>
              <div v-if="hotTags.length" class="hot-tags-list">
                <button
                  v-for="tag in hotTags"
                  :key="tag.name"
                  type="button"
                  class="hot-tag-item"
                  @click="applyTagFilter(tag.name)"
                >
                  <span>#{{ tag.name }}</span>
                  <em>{{ tag.count }}</em>
                </button>
              </div>
              <div v-else class="empty-tip">等待第一批内容出现</div>
            </section>

          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import ForumPostCard from '@/components/ForumPostCard.vue'
import { fetchForumPosts } from '@/api/forum'

const PAGE_SIZE = 10

const sort = ref('latest')
const activeTag = ref('')
const posts = ref([])
const sidebarPosts = ref([])
const loading = ref(false)
const error = ref('')
const pagination = ref({
  currentPage: 1,
  total: 0,
  size: PAGE_SIZE,
})
const forumStats = ref({
  total: 0,
  featuredCount: 0,
  pinnedCount: 0,
})
const featuredCount = computed(() => Number(forumStats.value.featuredCount || 0))

const extractTags = (post) => {
  if (Array.isArray(post?.tags)) {
    return post.tags.map((item) => String(item).trim()).filter(Boolean).slice(0, 6)
  }

  if (typeof post?.tags === 'string' && post.tags.trim()) {
    return post.tags
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
      .slice(0, 6)
  }

  const source = `${post?.title || ''} ${post?.summary || ''} ${post?.content || ''}`
  const matches = source.match(/[A-Za-z][A-Za-z0-9+#.-]{1,18}/g) || []
  return [...new Set(matches.map((item) => item.toLowerCase()))].slice(0, 6)
}

const hotPosts = computed(() => {
  return [...sidebarPosts.value]
    .sort(
      (a, b) =>
        (b.commentCount || 0) + (b.viewCount || 0) - ((a.commentCount || 0) + (a.viewCount || 0)),
    )
    .slice(0, 5)
})

const hotTags = computed(() => {
  const counter = new Map()
  sidebarPosts.value.forEach((post) => {
    extractTags(post).forEach((tag) => {
      counter.set(tag, (counter.get(tag) || 0) + 1)
    })
  })
  return [...counter.entries()]
    .map(([name, count]) => ({ name, count }))
    .sort((a, b) => b.count - a.count)
    .slice(0, 8)
})

const filteredPosts = computed(() => {
  if (!activeTag.value) return posts.value
  return posts.value.filter((post) => extractTags(post).includes(activeTag.value))
})

const leftNavItems = computed(() => [
  { key: 'latest', label: '最新帖子', count: pagination.value.total },
  { key: 'hot', label: '热门帖子', count: pagination.value.total },
  { key: 'featured', label: '精选文章', count: featuredCount.value },
])

const loadPosts = async (page = pagination.value.currentPage) => {
  loading.value = true
  error.value = ''
  try {
    const response = await fetchForumPosts({ sort: sort.value, page, size: pagination.value.size })
    const totalCount = Number(response?.stats?.total || response?.pagination?.total || 0)

    posts.value = response?.data || []
    pagination.value = {
      ...pagination.value,
      currentPage: Number(response?.pagination?.currentPage || page),
      total: Number(response?.pagination?.total || 0),
      size: Number(response?.pagination?.size || pagination.value.size),
    }
    forumStats.value = {
      total: totalCount,
      featuredCount: Number(response?.stats?.featuredCount || 0),
      pinnedCount: Number(response?.stats?.pinnedCount || 0),
    }

    if (!totalCount) {
      sidebarPosts.value = []
    } else if (sidebarPosts.value.length !== totalCount) {
      void loadSidebarPosts(totalCount)
    }
  } catch (err) {
    error.value = '获取论坛帖子失败，请稍后重试。'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const loadSidebarPosts = async (totalHint = forumStats.value.total || pagination.value.total || PAGE_SIZE) => {
  const safeSize = Math.max(Number(totalHint) || 0, PAGE_SIZE)
  if (!safeSize) {
    sidebarPosts.value = []
    return
  }

  try {
    const response = await fetchForumPosts({ sort: 'latest', page: 1, size: safeSize })
    sidebarPosts.value = response?.data || []
  } catch (err) {
    console.error('获取论坛侧栏数据失败', err)
  }
}

const changeSort = (nextSort) => {
  sort.value = nextSort
  activeTag.value = ''
  pagination.value.currentPage = 1
  loadPosts(1)
}

const applyTagFilter = (tagName) => {
  activeTag.value = tagName
}

const handlePageChange = (page) => {
  loadPosts(page)
  const scrollContainer = document.querySelector('.main-scroll-container')
  if (scrollContainer) {
    scrollContainer.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.forum-container {
  width: min(1440px, calc(100vw - 48px));
  margin-right: auto;
  animation: fadeIn 0.5s ease-out 0.3s forwards;
  opacity: 0;
  align-self: stretch;
  height: auto;
  margin-top: calc(var(--forum-layout-offset) * 1);
  padding-top: var(--forum-sticky-gap);
  padding-bottom: 32px;
  box-sizing: border-box;

  --forum-left-width: 332px;
  --forum-right-width: 236px;
  --forum-gap: 22px;
  --forum-layout-offset: 20px;
  --forum-sticky-gap: 12px;
}

.forum-shell {
  display: grid;
  grid-template-columns: var(--forum-left-width) minmax(0, 1fr) var(--forum-right-width);
  gap: var(--forum-gap);
  align-items: start;
}

.left-sidebar,
.center-content,
.right-sidebar {
  min-width: 250px;
}

.left-sidebar,
.right-sidebar {
  position: sticky;
  top: var(--forum-sticky-gap);
  align-self: start;
}

.panel-card {
  background: var(--card-bg-color, #fff);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 10px;
  box-shadow: none;
}

.right-panel-shell {
  display: flex;
  flex-direction: column;
}

.left-panel-shell {
  display: flex;
  flex-direction: column;
  padding: 28px 20px 24px;

  width: 200px;
}

.left-panel-shell,
.right-panel-shell {
  scrollbar-width: thin;
  scrollbar-color: rgba(120, 120, 120, 0.35) transparent;
}

.left-panel-shell::-webkit-scrollbar,
.right-panel-shell::-webkit-scrollbar {
  width: 6px;
}

.left-panel-shell::-webkit-scrollbar-thumb,
.right-panel-shell::-webkit-scrollbar-thumb {
  background: rgba(120, 120, 120, 0.35);
  border-radius: 999px;
}

.sidebar-title {
  margin-bottom: 18px;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
  color: #2f3747;
}

.sidebar-title.secondary {
  margin-top: 0;
  margin-bottom: 16px;
  font-size: 16px;
  color: #3e4553;
}

.left-section + .left-section {
  margin-top: 20px;
}

.left-section-info {
  margin-top: auto;
}

.left-nav-list {
  display: flex;
  flex-direction: column;
  width: 100%;
  gap: 10px;
}

.left-nav-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border: 1px solid transparent;
  border-radius: 18px;
  background: transparent;
  color: #5e6572;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.left-nav-item.active,
.left-nav-item:hover {
  background: #f0f0f0;
  border-color: rgba(15, 23, 42, 0.04);
  color: #2f3747;
}

.left-nav-item em {
  font-style: normal;
  color: #9aa1ac;
  font-size: 28px;
  line-height: 1;
  font-weight: 300;
}

.left-nav-item.active em,
.left-nav-item:hover em {
  color: #a0a6b1;
}

.left-divider {
  height: 1px;
  background: rgba(15, 23, 42, 0.08);
  margin: 26px 0 22px;
}

.left-note {
  line-height: 1.85;
  color: #818895;
  font-size: 15px;
}

.left-action-button {
  margin-top: 16px;
}

.center-content {
  min-width: 0;
  width: min(100%, 780px);
  justify-self: start;
  padding-top: 2px;
  padding-right: 10px;
}

.feed-toolbar,
.sidebar-card {
  margin-bottom: 16px;
}

.post-pagination {
  display: flex;
  justify-content: center;
  padding: 8px 0 18px;
}

.post-pagination :deep(.el-pagination) {
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
}

.right-panel-shell {
  padding: 18px 16px;
}

.side-panel-scroll {
  display: flex;
  flex-direction: column;
}

.side-inner-card {
  padding: 0;
}

.feed-toolbar-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.feed-toolbar {
  padding: 16px 18px;
}

.section-title,
.panel-title {
  color: var(--app-text-color);
  font-weight: 600;
}

.section-title {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.panel-title {
  font-size: 18px;
  margin-bottom: 14px;
}

.topic-chip,
.clear-chip,
.hot-tag-item {
  border: 0;
  background: rgba(0, 0, 0, 0.05);
  color: #666;
  border-radius: 999px;
  padding: 7px 12px;
  cursor: pointer;
}

.clear-chip {
  background: rgba(0, 0, 0, 0.08);
}

.hot-posts-list,
.hot-tags-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.hot-post-link,
.hot-tag-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  text-decoration: none;
  color: #666;
  padding: 12px 14px;
  border-radius: 14px;
}

.hot-post-link {
  background: rgba(0, 0, 0, 0.03);
}

.hot-post-link:hover,
.hot-tag-item:hover,
.topic-chip:hover,
.clear-chip:hover {
  color: var(--app-text-color);
  background: rgba(0, 0, 0, 0.07);
}

.hot-post-title {
  max-width: 180px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-post-link small,
.hot-tag-item em {
  font-style: normal;
  color: #999;
}

.bullet-list {
  margin: 0;
  padding-left: 18px;
  color: #7d7d7d;
  line-height: 1.8;
}

.empty-tip {
  color: #999;
  font-size: 14px;
}

.loading-tip,
.error-tip {
  padding: 20px 0;
  text-align: center;
  color: #888;
}

.error-tip {
  color: #f56c6c;
}

@media screen and (max-width: 1200px) {
  .forum-container {
    --forum-left-width: 296px;
    --forum-gap: 18px;
    --forum-layout-offset: 0px;
    height: auto;
    padding-top: 0;
  }

  .forum-shell {
    grid-template-columns: var(--forum-left-width) minmax(0, 1fr);
  }

  .right-sidebar {
    min-width: unset;
  }

  .left-sidebar,
  .right-sidebar {
    position: static;
  }

  .right-panel-shell {
    position: static;
    height: auto;
    overflow: visible;
  }

  .left-sidebar {
    grid-row: 1 / span 2;
  }

  .right-sidebar {
    grid-column: 2;
    margin-top: 0;
    height: auto;
  }

  .center-content {
    width: 100%;
    justify-self: stretch;
    padding-top: 0;
    padding-right: 0;
  }

  .feed-toolbar {
    margin-top: 16px;
  }

  .left-panel-shell {
    height: auto;
  }
}

@media screen and (max-width: 900px) {
  .forum-shell {
    grid-template-columns: 1fr;
  }

  .forum-container {
    width: calc(100vw - 24px);
  }

  .left-sidebar {
    grid-row: auto;
    margin-bottom: 16px;
  }

  .right-sidebar {
    grid-column: auto;
    margin-top: 16px;
  }

  .left-sidebar,
  .right-sidebar {
    position: static;
  }

  .left-panel-shell,
  .right-panel-shell {
    position: static;
    height: auto;
    overflow: visible;
  }

  .left-panel-shell {
    padding: 22px 18px;
    border-radius: 22px;
  }

  .feed-toolbar-row {
    flex-direction: column;
    align-items: flex-start;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

<template>
  <div class="forum-container">
    <div class="forum-shell">
      <aside class="left-sidebar">
        <div class="panel-card side-panel-shell left-panel-shell">
          <div class="sidebar-title">论坛页面</div>
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
          <div class="left-divider"></div>
          <div class="sidebar-title secondary">参与关注</div>
          <div class="left-note">
            在这里你可以发帖、评论、点赞、分享，后续会接入举报与个人中心论坛管理。
          </div>
        </div>
      </aside>

      <main class="center-content">
        <section class="composer-card panel-card">
          <div class="composer-header">
            <div>
              <div class="channel-kicker">Dream Studio Forum</div>
              <h1>发布帖子</h1>
            </div>
          </div>

          <div class="composer-box">
            <div class="composer-avatar-wrap">
              <el-avatar :size="44" :src="composerAvatar">{{
                composerNickname.slice(0, 1)
              }}</el-avatar>
            </div>
            <div class="composer-main">
              <el-input
                v-model="form.title"
                class="composer-title"
                maxlength="120"
                show-word-limit
                placeholder="发布帖子内容 / 用一句话概括你的主题"
              />
              <el-input
                v-model="form.content"
                class="composer-textarea"
                type="textarea"
                :rows="6"
                maxlength="20000"
                show-word-limit
                placeholder="写下你的问题、经验或想法，支持 Markdown。"
                :disabled="!isLoggedIn"
              />

              <div class="composer-toolbar">
                <div class="toolbar-actions">
                  <button type="button" class="toolbar-button" @click="quickInsert('# ')">
                    标题
                  </button>
                  <button
                    type="button"
                    class="toolbar-button"
                    @click="quickInsert('![图片描述](图片地址)')"
                  >
                    图片
                  </button>
                  <button
                    type="button"
                    class="toolbar-button"
                    @click="quickInsert('[视频标题](视频链接)')"
                  >
                    视频
                  </button>
                  <button
                    type="button"
                    class="toolbar-button"
                    @click="quickInsert('#vue #javascript ')"
                    :disabled="!isLoggedIn"
                  >
                    标签
                  </button>
                </div>
                <template v-if="isLoggedIn">
                  <el-button type="primary" round :loading="submitting" @click="submitPost"
                    >发布</el-button
                  >
                </template>
                <template v-else>
                  <el-button type="primary" round @click="goLogin">登录后发帖</el-button>
                </template>
              </div>
            </div>
          </div>
        </section>

        <section class="feed-toolbar panel-card">
          <div class="section-title">
            帖子列表

            <button
              v-for="tag in hotTags"
              :key="tag.name"
              type="button"
              class="topic-chip"
              @click="applyTagFilter(tag.name)"
            >
              #{{ tag.name }}
            </button>
            <button v-if="activeTag" type="button" class="clear-chip" @click="activeTag = ''">
              清除筛选
            </button>
          </div>
        </section>

        <div v-if="loading" class="loading-tip">正在加载帖子...</div>
        <div v-else-if="error" class="error-tip">{{ error }}</div>
        <div v-else-if="filteredPosts.length" class="post-list">
          <ForumPostCard v-for="post in filteredPosts" :key="post.id" :post="post" />
        </div>
        <el-empty v-else description="还没有帖子，欢迎发布第一篇讨论" />
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

            <section class="sidebar-card side-inner-card">
              <div class="panel-title">本期公告</div>
              <ul class="bullet-list">
                <li>论坛采用用户端集成管理模式，后续管理能力会逐步内嵌到个人中心。</li>
                <li>管理员会在同一界面看到额外操作按钮，不再单独维护一套后台。</li>
                <li>下一阶段将补点赞、举报、分享统计与个人中心论坛管理。</li>
              </ul>
            </section>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import ForumPostCard from '@/components/ForumPostCard.vue'
import defaultAvatar from '@/assets/(5).png'
import { createForumPost, fetchForumPosts } from '@/api/forum'

const router = useRouter()
const userStore = useUserStore()

const sort = ref('latest')
const activeTag = ref('')
const posts = ref([])
const loading = ref(false)
const error = ref('')
const submitting = ref(false)

const form = reactive({
  nickname: '',
  email: '',
  avatar: '',
  title: '',
  summary: '',
  content: '',
})

const currentProfile = computed(() => userStore.profile)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const featuredCount = computed(() => posts.value.filter((item) => item.isFeatured).length)
const composerNickname = computed(() => currentProfile.value.nickname || '未登录用户')
const composerAvatar = computed(() => currentProfile.value.avatar || defaultAvatar)

const extractTags = (post) => {
  const source = `${post?.title || ''} ${post?.summary || ''} ${post?.content || ''}`
  const matches = source.match(/[A-Za-z][A-Za-z0-9+#.-]{1,18}/g) || []
  return [...new Set(matches.map((item) => item.toLowerCase()))].slice(0, 6)
}

const hotPosts = computed(() => {
  return [...posts.value]
    .sort(
      (a, b) =>
        (b.commentCount || 0) + (b.viewCount || 0) - ((a.commentCount || 0) + (a.viewCount || 0)),
    )
    .slice(0, 5)
})

const hotTags = computed(() => {
  const counter = new Map()
  posts.value.forEach((post) => {
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
  { key: 'latest', label: '最新帖子', count: posts.value.length },
  { key: 'hot', label: '热门帖子', count: hotPosts.value.length },
  { key: 'featured', label: '精选文章', count: featuredCount.value },
])

const loadPosts = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await fetchForumPosts({ sort: sort.value, page: 1, size: 1000 })
    posts.value = response?.data || []
  } catch (err) {
    error.value = '获取论坛帖子失败，请稍后重试。'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const changeSort = (nextSort) => {
  sort.value = nextSort
  activeTag.value = ''
  loadPosts()
}

const applyTagFilter = (tagName) => {
  activeTag.value = tagName
}

const quickInsert = (snippet) => {
  if (!isLoggedIn.value) {
    goLogin()
    return
  }
  form.content = `${form.content}${form.content ? '\n' : ''}${snippet}`
}

const syncFormUser = () => {
  form.nickname = currentProfile.value.nickname || ''
  form.email = currentProfile.value.email || ''
  form.avatar = currentProfile.value.avatar || ''
}

const goLogin = () => {
  router.push({ path: '/login', query: { redirect: '/forum' } })
}

const submitPost = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再发帖')
    goLogin()
    return
  }
  if (!form.title.trim()) {
    ElMessage.warning('请先填写标题')
    return
  }
  if (!form.content.trim()) {
    ElMessage.warning('请先填写正文')
    return
  }

  submitting.value = true
  try {
    syncFormUser()
    const createdPost = await createForumPost({ ...form })
    form.title = ''
    form.summary = ''
    form.content = ''

    ElMessage.success('帖子发布成功')
    await router.push(`/forum/${createdPost.id}`)
  } catch (err) {
    console.error(err)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  syncFormUser()
  loadPosts()
})
</script>

<style scoped>
.forum-container {
  width: min(1380px, calc(100vw - 60px));
  margin-top: 60px;
  animation: fadeIn 0.5s ease-out 0.3s forwards;
  opacity: 0;
  --forum-left-width: 220px;
  --forum-right-width: 280px;
  --forum-gap: 18px;
  --forum-top-offset: 60px;
  --forum-primary-height: 300px;
}

.forum-shell {
  display: grid;
  grid-template-columns: var(--forum-left-width) minmax(0, 1fr) var(--forum-right-width);
  gap: var(--forum-gap);
  align-items: start;
}

.panel-card {
  background: var(--card-bg-color, #fff);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 20px;
  box-shadow: none;
}

.left-sidebar,
.right-sidebar {
  min-width: 0;
}

.left-panel-shell,
.right-panel-shell {
  position: sticky;
  top: var(--forum-top-offset);
  height: var(--forum-primary-height);
  overflow: hidden;
}

.left-panel-shell {
  padding: 20px 16px;
}

.left-panel-shell:hover,
.right-panel-shell:hover {
  overflow-y: auto;
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
  font-size: 18px;
  font-weight: 700;
  color: var(--app-text-color);
  margin-bottom: 14px;
}

.sidebar-title.secondary {
  margin-top: 8px;
  font-size: 15px;
}

.left-nav-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
  padding: 12px 14px;
  border: 0;
  border-radius: 14px;
  background: transparent;
  color: #636363;
  cursor: pointer;
  transition: all 0.25s ease;
}

.left-nav-item.active,
.left-nav-item:hover {
  background: rgba(0, 0, 0, 0.06);
  color: var(--app-text-color);
}

.left-nav-item em {
  font-style: normal;
  color: #9c9c9c;
}

.left-divider {
  height: 1px;
  background: rgba(15, 23, 42, 0.08);
  margin: 18px 0;
}

.left-note {
  line-height: 1.8;
  color: #7b7b7b;
  font-size: 14px;
}

.center-content {
  min-width: 0;
  width: 85%;
  justify-self: center;
}

.composer-card {
  min-height: var(--forum-primary-height);
}

.composer-card,
.feed-toolbar,
.sidebar-card {
  margin-bottom: 16px;
}

.side-panel-shell {
  padding: 18px 16px;
}

.side-panel-scroll {
  display: flex;
  flex-direction: column;
}

.side-inner-card {
  padding: 0;
}

.composer-card {
  padding: 20px;
}

.composer-header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 16px;
}

.channel-kicker {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #999;
}

.composer-header h1 {
  margin: 8px 0 12px;
  color: var(--app-text-color);
}

.channel-stats {
  display: flex;
  gap: 12px;
}

.stat-item {
  min-width: 86px;
  padding: 14px 16px;
  background: rgba(0, 0, 0, 0.03);
  border-radius: 14px;
  text-align: center;
}

.stat-item strong {
  display: block;
  font-size: 22px;
  color: var(--app-text-color);
}

.stat-item span {
  color: #999;
  font-size: 13px;
}

.composer-box {
  display: flex;
  gap: 14px;
}

.composer-main {
  flex: 1;
}

.composer-title {
  margin-bottom: 12px;
}

.composer-textarea {
  margin-bottom: 14px;
}

.composer-extra-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.login-user-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(0, 0, 0, 0.03);
}

.login-user-card strong {
  display: block;
  color: var(--app-text-color);
}

.login-user-card p {
  margin: 4px 0 0;
  color: #8a8a8a;
  font-size: 13px;
}

.composer-toolbar,
.feed-toolbar-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.toolbar-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.toolbar-button {
  border: 0;
  background: rgba(0, 0, 0, 0.05);
  color: #666;
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
}

.toolbar-button:hover {
  color: var(--app-text-color);
}

.feed-toolbar {
  padding: 16px 18px;
}

.section-title,
.panel-title {
  color: var(--app-text-color);
  font-weight: 600;
}

.panel-title {
  font-size: 18px;
  margin-bottom: 14px;
}

.hot-tag-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.hot-tag-label {
  color: #8b8b8b;
  align-self: center;
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
  .forum-shell {
    grid-template-columns: 1fr;
  }

  .left-sidebar,
  .right-sidebar {
    min-width: unset;
  }

  .left-panel-shell,
  .right-panel-shell {
    position: static;
    height: auto;
    overflow: visible;
  }

  .left-sidebar {
    margin-bottom: 16px;
  }

  .right-sidebar {
    margin-top: 16px;
  }

  .center-content {
    width: 100%;
  }

  .composer-card {
    min-height: unset;
  }
}

@media screen and (max-width: 900px) {
  .forum-container {
    width: calc(100vw - 24px);
  }

  .composer-header,
  .feed-toolbar-row,
  .composer-toolbar,
  .composer-box {
    flex-direction: column;
    align-items: flex-start;
  }

  .composer-extra-grid {
    grid-template-columns: 1fr;
    width: 100%;
  }

  .channel-stats {
    width: 100%;
    justify-content: space-between;
  }

  .stat-item {
    flex: 1;
  }

  .toolbar-actions {
    width: 100%;
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

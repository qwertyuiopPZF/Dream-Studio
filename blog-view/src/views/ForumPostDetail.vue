<template>
  <div class="forum-detail-container">
    <div v-if="loading" class="loading-tip">帖子加载中...</div>
    <div v-else-if="error" class="error-tip">{{ error }}</div>

    <template v-else-if="post">
      <div class="main-content-area">
        <div class="center">
          <el-card class="forum-content-card">
            <div class="post-header">
              <p class="post-kicker">Forum Thread</p>

              <div class="post-badges">
                <el-tag v-if="post.isPinned" round size="small" effect="plain" class="post-badge pinned-badge"
                  >置顶讨论</el-tag
                >
                <el-tag
                  v-if="post.isFeatured"
                  round
                  size="small"
                  type="success"
                  effect="plain"
                  class="post-badge featured-badge"
                  >精选内容</el-tag
                >
              </div>

              <h1 class="post-title">{{ post.title }}</h1>

              <p class="post-summary">{{ displaySummary }}</p>

              <div class="post-author-row">
                <div class="author-info">
                  <el-avatar :size="60" :src="resolvedAvatar">{{ authorInitial }}</el-avatar>
                  <div class="author-copy">
                    <strong>{{ resolvedAuthorName }}</strong>
                    <p>发起讨论 · {{ formatTime(post.createTime) }}</p>
                  </div>
                </div>

                <div class="post-stats-inline">
                  <span>
                    <el-icon><View /></el-icon>
                    {{ post.viewCount || 0 }} 浏览
                  </span>
                  <span>
                    <el-icon><ChatDotRound /></el-icon>
                    {{ post.commentCount || 0 }} 评论
                  </span>
                  <span>
                    <el-icon><Clock /></el-icon>
                    最近活跃 {{ formatTime(post.lastActivityTime || post.updateTime || post.createTime) }}
                  </span>
                </div>
              </div>

              <div v-if="derivedTags.length" class="post-topic-row">
                <span class="topic-label">讨论主题</span>
                <div class="topic-list">
                  <el-tag v-for="tag in derivedTags" :key="tag" round effect="plain" class="topic-tag">
                    #{{ tag }}
                  </el-tag>
                </div>
              </div>

              <div class="post-action-row">
                <el-button plain round @click="router.push('/forum')">返回论坛</el-button>
                <el-button plain round @click="sharePost">
                  <el-icon><Share /></el-icon>
                  分享帖子
                </el-button>
                <el-button plain round type="warning" @click="reportDialogVisible = true">举报内容</el-button>
              </div>

              <el-divider />
            </div>

            <MdPreview
              :editorId="previewId"
              :modelValue="post.content"
              @onGetCatalog="onGetCatalog"
              :headingId="(index) => `forum-heading-${index}`"
              :markedHeadingId="(index) => `forum-heading-${index}`"
            />
          </el-card>
        </div>

        <div class="sidebar">
          <el-affix :offset="130" target=".main-content-area">
            <div class="sidebar-stack">
              <div class="sidebar-panel info-card">
                <div class="sidebar-title">帖子概览</div>

                <div class="side-info-item">
                  <span>发帖作者</span>
                  <strong>{{ resolvedAuthorName }}</strong>
                </div>
                <div class="side-info-item">
                  <span>发布时间</span>
                  <strong>{{ formatTime(post.createTime) }}</strong>
                </div>
                <div class="side-info-item">
                  <span>最后活跃</span>
                  <strong>{{ formatTime(post.lastActivityTime || post.updateTime || post.createTime) }}</strong>
                </div>
                <div class="side-info-item">
                  <span>评论数</span>
                  <strong>{{ post.commentCount || 0 }}</strong>
                </div>
                <div class="side-info-item">
                  <span>浏览量</span>
                  <strong>{{ post.viewCount || 0 }}</strong>
                </div>

                <div v-if="derivedTags.length" class="sidebar-topics">
                  <span class="sidebar-subtitle">关键词</span>
                  <div class="topic-list compact">
                    <el-tag v-for="tag in derivedTags" :key="`sidebar-${tag}`" round effect="plain" class="topic-tag">
                      #{{ tag }}
                    </el-tag>
                  </div>
                </div>

                <div class="sidebar-actions">
                  <el-button class="back-btn" @click="router.push('/forum')">返回论坛</el-button>
                  <el-button class="back-btn" plain @click="sharePost">复制链接</el-button>
                </div>
              </div>

              <div class="sidebar-panel catalog-card">
                <div class="sidebar-title">目录导航</div>
                <el-anchor :container="scrollContainer" :offset="70" v-if="catalogList.length > 0">
                  <el-anchor-link
                    v-for="item in catalogList"
                    :key="item.uniqueId"
                    :href="`#${item.uniqueId}`"
                    :title="item.text"
                    :class="`indent-level-${item.level}`"
                    @click.prevent
                  />
                </el-anchor>
                <div v-else class="empty-catalog">暂无目录</div>
              </div>
            </div>
          </el-affix>
        </div>
      </div>

      <el-divider />
      <CommentsCard
        :page="`forum-post-${post.id}`"
        :require-login="true"
        class="comments-section"
        @count-changed="handleCommentCountChanged"
      />
    </template>

    <el-dialog v-model="reportDialogVisible" title="举报帖子" width="520px">
      <el-form label-position="top">
        <el-form-item label="举报原因">
          <el-select v-model="reportForm.reason" placeholder="请选择举报原因">
            <el-option label="垃圾内容 / 广告" value="spam" />
            <el-option label="不当言论" value="abuse" />
            <el-option label="侵权 / 搬运" value="copyright" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input
            v-model="reportForm.detail"
            type="textarea"
            :rows="4"
            maxlength="300"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="submitReport">提交举报</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Clock, Share, View } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import CommentsCard from '@/components/CommentsCard.vue'
import defaultAvatar from '@/assets/(5).png'
import { fetchForumPostById } from '@/api/forum'
import { createForumReport } from '@/api/user'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const loading = ref(false)
const error = ref('')
const catalogList = ref([])
const scrollContainer = ref(null)
const previewId = `forum-preview-${route.params.id || 'detail'}`
const reportDialogVisible = ref(false)
const reportForm = reactive({
  reason: '',
  detail: '',
})
const isLoggedIn = computed(() => userStore.isLoggedIn)

const resolvedAuthorName = computed(
  () => post.value?.authorNickname || post.value?.nickname || '匿名用户',
)

const resolvedAvatar = computed(() => post.value?.authorAvatar || post.value?.avatar || defaultAvatar)

const authorInitial = computed(() => resolvedAuthorName.value.slice(0, 1).toUpperCase() || 'D')

const fallbackSummary = computed(() => {
  const content = post.value?.content || ''
  const plainText = content
    .replace(/!\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/\[[^\]]*\]\([^)]*\)/g, ' ')
    .replace(/[#>*_~`-]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()

  return plainText.slice(0, 160)
})

const displaySummary = computed(
  () => post.value?.summary?.trim() || fallbackSummary.value || '这是一篇正在展开讨论的帖子。',
)

const derivedTags = computed(() => {
  const source = `${post.value?.title || ''} ${post.value?.summary || ''} ${post.value?.content || ''}`
  const matches = source.match(/[A-Za-z][A-Za-z0-9+#.-]{1,18}/g) || []
  return [...new Set(matches.map((item) => item.toLowerCase()))].slice(0, 6)
})

const handleCommentCountChanged = (count) => {
  if (!post.value) return
  post.value.commentCount = count
}

const onGetCatalog = (list) => {
  catalogList.value = list.map((item, index) => ({
    ...item,
    uniqueId: `forum-heading-${index}`,
  }))

  nextTick(() => {
    const previewEl = document.querySelector(`#${previewId} .md-editor-preview`)
    if (!previewEl) return

    const headings = previewEl.querySelectorAll('h1, h2, h3, h4, h5, h6')
    headings.forEach((el, index) => {
      el.setAttribute('id', `forum-heading-${index}`)
    })
  })
}

const formatTime = (value) => {
  if (!value) return '--'
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const sharePost = async () => {
  if (!post.value) return
  const shareUrl = window.location.href
  try {
    if (navigator.share) {
      await navigator.share({ title: post.value.title, url: shareUrl })
    } else if (navigator.clipboard) {
      await navigator.clipboard.writeText(shareUrl)
      ElMessage.success('帖子链接已复制')
    } else {
      ElMessage.info('当前环境暂不支持分享，请手动复制地址栏链接')
    }
  } catch (shareError) {
    console.error('分享失败', shareError)
  }
}

const submitReport = async () => {
  if (!post.value) return
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再举报')
    router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }
  if (!reportForm.reason) {
    ElMessage.warning('请先选择举报原因')
    return
  }

  try {
    await createForumReport({
      targetType: 'forum-post',
      targetId: post.value.id,
      reason: reportForm.reason,
      detail: reportForm.detail,
    })
    reportDialogVisible.value = false
    reportForm.reason = ''
    reportForm.detail = ''
    ElMessage.success('举报已提交，管理员会收到通知')
  } catch (submitError) {
    console.error('提交举报失败', submitError)
  }
}

onMounted(async () => {
  const el = document.querySelector('.main-scroll-container')
  scrollContainer.value = el || window

  if (!route.params.id) {
    error.value = '未找到帖子 ID'
    return
  }

  loading.value = true
  try {
    post.value = await fetchForumPostById(route.params.id)
  } catch (err) {
    error.value = '加载帖子失败，请稍后重试。'
    console.error(err)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.forum-detail-container {
  --forum-surface-bg: rgba(255, 255, 255, 0.78);
  --forum-surface-border: rgba(15, 23, 42, 0.08);
  --forum-surface-shadow: 0 18px 40px rgba(148, 163, 184, 0.1);
  --forum-surface-radius: 24px;
  --forum-surface-min-height: 380px;
  --forum-content-width: 900px;
  width: min(1180px, 100%);
  margin: 0 auto;
}

.main-content-area {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, var(--forum-content-width)) 240px;
  gap: 24px;
  align-items: start;
  margin-bottom: 28px;
}

.center,
.sidebar {
  min-width: 0;
}

.forum-content-card {
  width: 100%;
  min-height: var(--forum-surface-min-height);
  --el-card-bg-color: var(--forum-surface-bg);
  --el-card-border-color: var(--forum-surface-border);
  background:
    radial-gradient(circle at top right, rgba(231, 245, 234, 0.95), transparent 34%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(255, 255, 255, 0.78));
  box-shadow: var(--forum-surface-shadow);
  border: 1px solid var(--forum-surface-border);
  border-radius: var(--forum-surface-radius);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  animation: fadeIn 0.5s ease-out 0.2s forwards;
  opacity: 0;
  overflow: hidden;
}

:deep(.forum-content-card .el-card__body) {
  background: transparent;
  padding: 34px 38px;
}

.post-header {
  margin-bottom: 10px;
}

.post-kicker {
  margin: 0 0 12px;
  color: #7d8b7d;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.post-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.post-badge {
  --el-tag-font-size: 12px;
  padding: 0 12px;
  font-weight: 700;
}

.pinned-badge {
  color: #7a4a0f;
  background: rgba(255, 245, 221, 0.8);
  border-color: rgba(219, 161, 69, 0.25);
}

.featured-badge {
  background: rgba(228, 243, 232, 0.8);
  border-color: rgba(88, 158, 104, 0.24);
}

.post-title {
  margin: 0 0 18px;
  color: var(--app-text-color);
  font-size: clamp(2rem, 3vw, 2.7rem);
  font-weight: 800;
  line-height: 1.18;
  letter-spacing: -0.03em;
}

.post-summary {
  margin: 0 0 24px;
  color: #5f6b76;
  font-size: 15px;
  line-height: 1.9;
}

.post-author-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 20px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 14px;
}

.author-copy strong {
  display: block;
  color: var(--app-text-color);
  font-size: 16px;
  font-weight: 700;
}

.author-copy p {
  margin: 4px 0 0;
  color: #8b939b;
  font-size: 13px;
  font-weight: 600;
}

.post-stats-inline {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

.post-stats-inline span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.7);
  color: #53606d;
  font-size: 13px;
  font-weight: 600;
}

.post-topic-row {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  flex-wrap: wrap;
  margin-bottom: 22px;
}

.topic-label,
.sidebar-subtitle {
  color: #6b7280;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.topic-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.topic-list.compact {
  gap: 8px;
}

.topic-tag {
  --el-tag-font-size: 13px;
  padding: 0 12px;
  color: #52606d;
  background: rgba(255, 255, 255, 0.66);
  border-color: rgba(148, 163, 184, 0.2);
  font-weight: 600;
}

.post-action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 6px;
}

.post-action-row :deep(.el-button) {
  min-width: 108px;
}

.sidebar-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-panel {
  padding: 22px 20px;
  border: 1px solid var(--forum-surface-border);
  border-radius: var(--forum-surface-radius);
  background: var(--forum-surface-bg);
  box-shadow: var(--forum-surface-shadow);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.sidebar-title {
  margin-bottom: 16px;
  color: var(--app-text-color);
  font-size: 18px;
  font-weight: 700;
  line-height: 1.3;
  padding-left: 10px;
  border-left: 4px solid #389747;
}

.side-info-item {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  padding: 11px 0;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
}

.side-info-item:last-of-type {
  border-bottom: 0;
}

.side-info-item span {
  color: #7b8794;
  font-size: 13px;
}

.side-info-item strong {
  color: var(--app-text-color);
  font-size: 13px;
  text-align: right;
  line-height: 1.5;
}

.sidebar-topics {
  margin-top: 18px;
}

.sidebar-topics .topic-list {
  margin-top: 10px;
}

.sidebar-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 18px;
}

.back-btn {
  width: 100%;
  margin: 0;
}

.catalog-card {
  max-height: calc(100vh - 180px);
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.catalog-card::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.empty-catalog,
.loading-tip,
.error-tip {
  text-align: center;
  color: #888;
  padding: 24px 0;
}

.error-tip {
  color: #f56c6c;
}

.comments-section {
  width: min(var(--forum-content-width), 100%);
  margin: 0 auto;
}

.comments-section :deep(.comments-card) {
  min-height: var(--forum-surface-min-height);
  background: var(--forum-surface-bg);
  border: 1px solid var(--forum-surface-border);
  border-radius: var(--forum-surface-radius);
  box-shadow: var(--forum-surface-shadow);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 28px 30px;
}

.indent-level-1 {
  padding-left: 0;
}

.indent-level-2 {
  padding-left: 15px;
}

.indent-level-3 {
  padding-left: 30px;
}

.indent-level-4 {
  padding-left: 45px;
}

:deep(.el-anchor__link) {
  font-size: 14px;
  line-height: 2;
}

:deep(.el-main) {
  overflow: visible !important;
}

:deep(.md-editor-preview),
:deep(.md-editor-preview-wrapper) {
  background-color: transparent !important;
}

@media screen and (max-width: 900px) {
  .main-content-area {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .post-author-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .post-stats-inline {
    justify-content: flex-start;
  }

  .comments-section {
    width: 100%;
  }

  .sidebar-panel,
  .forum-content-card,
  .comments-section :deep(.comments-card) {
    min-height: auto;
  }

  :deep(.forum-content-card .el-card__body) {
    padding: 24px 20px;
  }

  .comments-section :deep(.comments-card) {
    padding: 22px 20px;
  }
}

@media screen and (max-width: 640px) {
  .post-title {
    font-size: 1.8rem;
  }

  .post-summary {
    font-size: 14px;
  }

  .post-stats-inline span {
    width: 100%;
    justify-content: flex-start;
  }

  .topic-label,
  .sidebar-subtitle {
    width: 100%;
  }

  .post-action-row {
    width: 100%;
  }

  .post-action-row :deep(.el-button) {
    width: 100%;
    margin-left: 0;
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

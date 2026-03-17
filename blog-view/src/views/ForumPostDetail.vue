<template>
  <div class="forum-detail-container">
    <div v-if="loading" class="loading-tip">帖子加载中...</div>
    <div v-else-if="error" class="error-tip">{{ error }}</div>

    <template v-else-if="post">
      <div class="main-content-area">
        <div class="center">
          <el-card class="forum-content-card">
            <div class="post-header">
              <div class="post-badges">
                <el-tag v-if="post.isPinned" size="small" effect="plain">置顶</el-tag>
                <el-tag v-if="post.isFeatured" size="small" type="success" effect="plain">精华</el-tag>
              </div>
              <h1>{{ post.title }}</h1>
              <div class="post-author-row">
                <div class="author-info">
                  <el-avatar :size="42" :src="post.avatar || defaultAvatar" />
                  <div>
                    <div class="author-name">{{ post.nickname || '匿名用户' }}</div>
                    <div class="author-time">发布于 {{ formatTime(post.createTime) }}</div>
                  </div>
                </div>
                <div class="post-stats-inline">
                  <span><el-icon><View /></el-icon>{{ post.viewCount || 0 }}</span>
                  <span><el-icon><ChatDotRound /></el-icon>{{ post.commentCount || 0 }}</span>
                </div>
              </div>
              <p v-if="post.summary" class="post-summary">{{ post.summary }}</p>
              <div class="post-action-row">
                <el-button plain round @click="sharePost">分享</el-button>
                <el-button plain round type="warning" @click="reportDialogVisible = true">举报</el-button>
              </div>
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
            <el-card class="sidebar-card post-side-info">
              <template #header>
                <div class="sidebar-title">帖子信息</div>
              </template>
              <div class="side-info-item">
                <span>最后活跃</span>
                <strong>{{ formatTime(post.lastActivityTime || post.updateTime) }}</strong>
              </div>
              <div class="side-info-item">
                <span>评论数</span>
                <strong>{{ post.commentCount || 0 }}</strong>
              </div>
              <div class="side-info-item">
                <span>浏览量</span>
                <strong>{{ post.viewCount || 0 }}</strong>
              </div>
              <el-button class="back-btn" @click="router.push('/forum')">返回论坛</el-button>
            </el-card>

            <el-card class="sidebar-card catalog-card">
              <template #header>
                <div class="sidebar-title">目录导航</div>
              </template>
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
            </el-card>
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
          <el-input v-model="reportForm.detail" type="textarea" :rows="4" maxlength="300" show-word-limit />
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
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, View } from '@element-plus/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import CommentsCard from '@/components/CommentsCard.vue'
import defaultAvatar from '@/assets/(5).png'
import { fetchForumPostById } from '@/api/forum'
import { pushCommunityNotification, saveCommunityReport } from '@/utils/community'

const route = useRoute()
const router = useRouter()

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
    }
  } catch (error) {
    console.error('分享失败', error)
  }
}

const submitReport = () => {
  if (!post.value) return
  if (!reportForm.reason) {
    ElMessage.warning('请先选择举报原因')
    return
  }

  const reporterName =
    localStorage.getItem('user_nickname') ||
    localStorage.getItem('forum_post_nickname') ||
    localStorage.getItem('comment_nickname') ||
    '游客'

  const reportRecord = saveCommunityReport({
    targetType: 'forum-post',
    targetId: post.value.id,
    targetTitle: post.value.title,
    reason: reportForm.reason,
    detail: reportForm.detail,
    reporterName,
  })

  pushCommunityNotification({
    type: 'report',
    title: `收到关于帖子《${post.value.title}》的举报`,
    content: `${reporterName} 提交了举报，原因：${reportForm.reason}`,
    targetId: post.value.id,
    targetType: 'forum-post',
    reportId: reportRecord.id,
  })

  reportDialogVisible.value = false
  reportForm.reason = ''
  reportForm.detail = ''
  ElMessage.success('举报已提交，管理员会在消息通知中查看')
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
  width: 100%;
}

.main-content-area {
  width: 90vw;
  display: flex;
}

.center {
  flex: 1;
}

.sidebar {
  width: 220px;
  margin-left: auto;
}

.forum-content-card,
.sidebar-card {
  border: 0;
  box-shadow: none;
}

.forum-content-card {
  width: 900px;
  background-color: var(--card-bg-color);
  padding-left: 245px;
  animation: fadeIn 0.5s ease-out 0.3s forwards;
  opacity: 0;
}

.post-header {
  margin-bottom: 24px;
}

.post-header h1 {
  margin: 12px 0 20px;
  color: var(--app-text-color);
}

.post-badges {
  display: flex;
  gap: 8px;
}

.post-author-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-name {
  font-weight: 600;
  color: var(--app-text-color);
}

.author-time,
.post-summary,
.post-stats-inline,
.side-info-item span {
  color: #7d7d7d;
}

.post-summary {
  line-height: 1.8;
}

.post-action-row {
  display: flex;
  gap: 10px;
  margin-top: 16px;
}

.post-stats-inline {
  display: flex;
  gap: 16px;
}

.post-stats-inline span {
  display: flex;
  align-items: center;
  gap: 6px;
}

.sidebar-title {
  font-weight: bold;
  color: var(--app-text-color);
}

.post-side-info {
  margin-bottom: 16px;
}

.side-info-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}

.side-info-item strong {
  color: var(--app-text-color);
  text-align: right;
}

.back-btn {
  width: 100%;
  margin-top: 10px;
}

.catalog-card {
  max-height: calc(100vh - 180px);
  overflow-y: auto;
}

.empty-catalog,
.loading-tip,
.error-tip {
  text-align: center;
  color: #888;
  padding: 20px 0;
}

.error-tip {
  color: #f56c6c;
}

.comments-section {
  width: 900px;
  margin: 0 auto;
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

:deep(.md-editor-preview),
:deep(.md-editor-preview-wrapper) {
  background-color: transparent !important;
}

@media screen and (max-width: 900px) {
  .main-content-area {
    display: block;
  }

  .forum-content-card,
  .comments-section {
    width: 100%;
    padding-left: 0;
  }

  .sidebar {
    width: 100%;
    margin-top: 20px;
  }

  .post-author-row {
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

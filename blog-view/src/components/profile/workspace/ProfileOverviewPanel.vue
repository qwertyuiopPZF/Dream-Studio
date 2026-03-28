<template>
  <workspace-page-shell
    title="个人总览"
    description="快速查看你的创作进度、互动提醒和账号信息。"
    :loading="loading"
    @refresh="loadOverview"
  >
    <section class="overview-hero">
      <div class="hero-main">
        <el-avatar :size="68" :src="profileSummary.avatar">{{ userInitial }}</el-avatar>
        <div>
          <p class="hero-kicker">Personal Overview</p>
          <h3>{{ profileSummary.nickname || profileSummary.username || 'Dream 用户' }}</h3>
          <p>{{ profileSummary.bio || '在这里查看你的最近创作和互动动态。' }}</p>
          <div class="hero-tags">
            <el-tag effect="plain">{{ profileSummary.role === 'ADMIN' ? '管理员账号' : '普通用户' }}</el-tag>
            <el-tag v-if="profileSummary.githubLogin" effect="plain" type="success">
              GitHub · {{ profileSummary.githubLogin }}
            </el-tag>
            <el-tag :type="profileSummary.passwordInitialized ? 'warning' : 'info'" effect="plain">
              {{ profileSummary.passwordInitialized ? '已设置站内密码' : '仅第三方登录' }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="hero-actions">
        <el-button type="primary" @click="goTo('/profile/writearticle')">写文章</el-button>
        <el-button plain @click="goTo('/profile/writemoment')">发动态</el-button>
        <el-button plain @click="goTo('/profile/forum-publish')">发布帖子</el-button>
        <el-button v-if="canViewAdminDashboard" text @click="goTo('/admin/overview')">进入后台概览</el-button>
      </div>
    </section>

    <div class="stats-grid">
      <overview-stat-card label="我的帖子" :value="overview.postCount || 0" hint="累计在论坛发布的帖子数量。" />
      <overview-stat-card label="我的评论" :value="overview.commentCount || 0" hint="包括文章和论坛中的互动评论。" />
      <overview-stat-card label="未读通知" :value="overview.unreadNotificationCount || 0" hint="新的举报处理结果和站内提醒会集中出现在这里。" />
      <overview-stat-card label="待处理举报" :value="overview.pendingReportCount || 0" hint="你提交的举报或待管理员处理的反馈数量。" />
    </div>

    <div class="overview-grid overview-grid-two">
      <overview-section-card title="最近创作" description="优先展示你最近发布的帖子，可快速返回继续查看。">
        <div v-if="overview.myPosts?.length" class="list-stack">
          <button v-for="post in overview.myPosts.slice(0, 5)" :key="post.id" type="button" class="list-item-button" @click="goToForumPost(post.id)">
            <div>
              <strong>{{ post.title || '未命名帖子' }}</strong>
              <p>{{ post.summary || '进入帖子详情查看完整内容。' }}</p>
            </div>
            <span>{{ formatManagementTime(post.lastActivityTime || post.createTime) }}</span>
          </button>
        </div>
        <el-empty v-else :image-size="72" description="暂时还没有帖子，去发布第一篇讨论吧。" />
      </overview-section-card>

      <overview-section-card title="最近互动" description="查看你最近参与过的评论和互动位置。">
        <div v-if="overview.myComments?.length" class="list-stack">
          <button
            v-for="comment in overview.myComments.slice(0, 5)"
            :key="comment.id"
            type="button"
            class="list-item-button"
            @click="goToCommentTarget(comment)"
          >
            <div>
              <strong>{{ getCommentTargetLabel(comment) }}</strong>
              <p>{{ comment.content || '暂无评论内容' }}</p>
            </div>
            <span>{{ formatManagementTime(comment.createTime) }}</span>
          </button>
        </div>
        <el-empty v-else :image-size="72" description="最近还没有新的评论互动。" />
      </overview-section-card>
    </div>

    <div class="overview-grid overview-grid-two">
      <overview-section-card title="通知中心" description="新的处理结果和站内通知会显示在这里。">
        <template #header-extra>
          <el-button text type="primary" @click="goTo('/profile/notifications')">查看全部</el-button>
        </template>
        <div v-if="overview.notifications?.length" class="list-stack">
          <article v-for="notification in overview.notifications.slice(0, 6)" :key="notification.id" class="notification-item">
            <div>
              <strong>{{ notification.title }}</strong>
              <p>{{ notification.content }}</p>
              <small>{{ formatManagementTime(notification.createTime) }}</small>
            </div>
            <div class="notification-actions">
              <el-tag :type="notification.read ? 'info' : 'warning'" effect="plain">
                {{ notification.read ? '已读' : '未读' }}
              </el-tag>
              <el-button v-if="!notification.read" text type="primary" @click="markNotificationRead(notification)">
                标记已读
              </el-button>
              <el-button text @click="openNotificationTarget(notification)">查看</el-button>
            </div>
          </article>
        </div>
        <el-empty v-else :image-size="72" description="暂无新的通知提醒。" />
      </overview-section-card>

      <overview-section-card title="举报进度" description="这里会同步你提交的举报记录和管理员处理状态。">
        <div v-if="displayedReports.length" class="list-stack">
          <article
            v-for="report in displayedReports"
            :id="`profile-report-${report.id}`"
            :key="report.id"
            class="report-item"
            :class="{ 'is-highlighted': isHighlightedReport(report.id) }"
          >
            <div>
              <strong>{{ report.targetTitle || `内容 #${report.targetId}` }}</strong>
              <p>{{ report.detail || `举报原因：${report.reason || '未填写'}` }}</p>
              <small>{{ formatManagementTime(report.updateTime || report.createTime) }}</small>
            </div>
            <el-tag :type="resolveReportStatusType(report.status)" effect="plain">
              {{ resolveReportStatusLabel(report.status) }}
            </el-tag>
          </article>
        </div>
        <el-empty v-else :image-size="72" description="你还没有提交过举报记录。" />
      </overview-section-card>
    </div>

    <overview-section-card title="账号信息" description="帮助你快速确认当前资料与登录方式。">
      <template #header-extra>
        <el-button text type="primary" @click="goTo('/profile/account')">修改头像 / 密码</el-button>
      </template>
      <div class="account-grid">
        <article class="account-item">
          <span>用户名</span>
          <strong>@{{ profileSummary.username || '--' }}</strong>
        </article>
        <article class="account-item">
          <span>邮箱</span>
          <strong>{{ profileSummary.email || '未填写' }}</strong>
        </article>
        <article class="account-item">
          <span>手机号</span>
          <strong>{{ profileSummary.phone || '未填写' }}</strong>
        </article>
        <article class="account-item">
          <span>登录方式</span>
          <strong>{{ profileSummary.githubLogin ? 'GitHub + 站内资料' : '站内账号' }}</strong>
        </article>
      </div>
    </overview-section-card>
  </workspace-page-shell>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchCurrentUserOverview, markCurrentUserNotificationRead } from '@/api/user'
import OverviewSectionCard from '@/components/profile/workspace/OverviewSectionCard.vue'
import OverviewStatCard from '@/components/profile/workspace/OverviewStatCard.vue'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'
import { useUserStore } from '@/store/user'
import { formatManagementTime, getCommentTargetLabel } from '@/utils/profileManagement'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const overview = ref({
  profile: null,
  postCount: 0,
  commentCount: 0,
  unreadNotificationCount: 0,
  pendingReportCount: 0,
  myPosts: [],
  myComments: [],
  reports: [],
  notifications: [],
})

const canViewAdminDashboard = computed(() =>
  userStore.hasCapability(WORKSPACE_CAPABILITIES.DASHBOARD_VIEW),
)
const highlightedReportId = computed(() => Number(route.query.reportId || 0) || null)
const displayedReports = computed(() => {
  const items = Array.isArray(overview.value.reports) ? [...overview.value.reports] : []

  if (highlightedReportId.value) {
    items.sort((left, right) => {
      if (left.id === highlightedReportId.value) return -1
      if (right.id === highlightedReportId.value) return 1
      return 0
    })
  }

  return items.slice(0, 6)
})
const profileSummary = computed(() => overview.value.profile || userStore.profile || {})
const userInitial = computed(() => profileSummary.value.nickname?.slice(0, 1)?.toUpperCase() || 'D')

const scrollToHighlightedReport = async () => {
  if (!highlightedReportId.value) return

  await nextTick()
  const target = document.getElementById(`profile-report-${highlightedReportId.value}`)
  target?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const loadOverview = async () => {
  loading.value = true

  try {
    const response = await fetchCurrentUserOverview()
    overview.value = {
      profile: response?.profile || null,
      postCount: Number(response?.postCount || 0),
      commentCount: Number(response?.commentCount || 0),
      unreadNotificationCount: Number(response?.unreadNotificationCount || 0),
      pendingReportCount: Number(response?.pendingReportCount || 0),
      myPosts: Array.isArray(response?.myPosts) ? response.myPosts : [],
      myComments: Array.isArray(response?.myComments) ? response.myComments : [],
      reports: Array.isArray(response?.reports) ? response.reports : [],
      notifications: Array.isArray(response?.notifications) ? response.notifications : [],
    }

    if (response?.profile) {
      userStore.hydrateFromServerProfile(response.profile)
    }

    await scrollToHighlightedReport()
  } catch (error) {
    console.error('加载个人总览失败', error)
    ElMessage.error(error.message || '加载个人总览失败，请稍后重试。')
  } finally {
    loading.value = false
  }
}

const goTo = (path) => router.push(path)

const goToForumPost = (id) => {
  if (!id) return
  router.push(`/forum/${id}`)
}

const goToCommentTarget = (comment) => {
  if (!comment) return

  if (comment.blogId) {
    router.push(`/article/${comment.blogId}`)
    return
  }

  if (comment.page?.startsWith('forum-post-')) {
    router.push(`/forum/${comment.page.replace('forum-post-', '')}`)
    return
  }

  if (comment.page === 'friends') {
    router.push('/forum')
  }
}

const openNotificationTarget = (notification) => {
  const notificationType = String(notification?.type || '').toUpperCase()

  if (notificationType === 'REPORT_CREATED' && notification?.relatedReportId) {
    if (userStore.hasCapability(WORKSPACE_CAPABILITIES.REPORT_REVIEW)) {
      router.push({
        path: '/admin/reportmgmt',
        query: { reportId: String(notification.relatedReportId) },
      })
    }
    return
  }

  if (notificationType === 'REPORT_REVIEWED' && notification?.relatedReportId) {
    router.push({
      path: '/profile/overview',
      query: { reportId: String(notification.relatedReportId) },
    })
    return
  }

  if (!notification?.targetId) return

  if (notification.targetType === 'forum-post') {
    router.push(`/forum/${notification.targetId}`)
    return
  }

  if (notification.targetType === 'article') {
    router.push(`/article/${notification.targetId}`)
  }
}

const markNotificationRead = async (notification) => {
  if (!notification?.id || notification.read) return

  try {
    await markCurrentUserNotificationRead(notification.id)
    notification.read = true
    overview.value.unreadNotificationCount = Math.max(0, Number(overview.value.unreadNotificationCount || 0) - 1)
    ElMessage.success('通知已标记为已读')
  } catch (error) {
    console.error('标记通知失败', error)
    ElMessage.error(error.message || '标记通知失败，请稍后重试。')
  }
}

const resolveReportStatusLabel = (status = '') => {
  const normalized = String(status || '').toUpperCase()

  if (normalized === 'RESOLVED') return '已处理'
  if (normalized === 'REJECTED') return '已驳回'
  if (normalized === 'PENDING') return '待处理'

  return status || '未知状态'
}

const resolveReportStatusType = (status = '') => {
  const normalized = String(status || '').toUpperCase()

  if (normalized === 'RESOLVED') return 'success'
  if (normalized === 'REJECTED') return 'info'
  return 'warning'
}

const isHighlightedReport = (reportId) => highlightedReportId.value === reportId

onMounted(() => {
  loadOverview()
})

watch(
  () => route.query.reportId,
  async () => {
    await scrollToHighlightedReport()
  },
)
</script>

<style scoped>
.overview-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 22px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(251, 191, 36, 0.18), transparent 36%),
    linear-gradient(135deg, rgba(255, 247, 237, 0.96), rgba(255, 255, 255, 0.92));
}

.hero-main {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.hero-kicker {
  margin: 0 0 8px;
  color: #9a3412;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.overview-hero h3 {
  margin: 0;
  color: #7c2d12;
  font-size: 30px;
}

.overview-hero p {
  margin: 10px 0 0;
  color: #7c2d12;
  line-height: 1.7;
}

.hero-tags,
.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.hero-tags {
  margin-top: 14px;
}

.hero-actions {
  justify-content: flex-end;
}

.stats-grid,
.overview-grid {
  display: grid;
  gap: 16px;
  margin-top: 18px;
}

.stats-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.overview-grid-two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.list-stack {
  display: grid;
  gap: 12px;
}

.list-item-button,
.notification-item,
.report-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  padding: 14px 16px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 16px;
  background: rgba(248, 250, 252, 0.86);
  text-align: left;
}

.report-item.is-highlighted {
  border-color: rgba(245, 158, 11, 0.55);
  box-shadow: 0 0 0 3px rgba(251, 191, 36, 0.16);
}

.list-item-button {
  cursor: pointer;
}

.list-item-button strong,
.notification-item strong,
.report-item strong {
  display: block;
  color: #0f172a;
}

.list-item-button p,
.notification-item p,
.report-item p {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.list-item-button span,
.notification-item small,
.report-item small {
  flex-shrink: 0;
  color: #64748b;
}

.notification-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.account-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.account-item {
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(248, 250, 252, 0.86);
}

.account-item span {
  display: block;
  color: #64748b;
  font-size: 13px;
}

.account-item strong {
  display: block;
  margin-top: 10px;
  color: #0f172a;
  line-height: 1.5;
}

@media (max-width: 1100px) {
  .stats-grid,
  .account-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .overview-grid-two {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .overview-hero,
  .hero-main,
  .list-item-button,
  .notification-item,
  .report-item {
    flex-direction: column;
  }

  .stats-grid,
  .account-grid {
    grid-template-columns: 1fr;
  }

  .hero-actions,
  .notification-actions {
    width: 100%;
    align-items: stretch;
  }
}
</style>

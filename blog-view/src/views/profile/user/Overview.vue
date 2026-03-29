<template>
  <section class="profile-overview-page">
    <div class="metrics-grid">
      <article v-for="card in statCards" :key="card.label" class="metric-card">
        <div class="metric-copy">
          <span>{{ card.label }}</span>
          <strong>{{ formatMetric(card.value) }}</strong>
          <small>{{ card.hint }}</small>
        </div>

        <div class="metric-icon" :class="`tone-${card.tone}`">
          <el-icon><component :is="card.icon" /></el-icon>
        </div>
      </article>
    </div>

    <section class="contribution-panel">
      <div class="contribution-header">
        <div class="contribution-header-copy">
          <span class="panel-kicker">Activity Graph</span>
          <h3>今年创作与互动贡献度</h3>
          <p>{{ contributionSummary }}</p>
        </div>

        <div class="contribution-legend">
          <span>低</span>
          <i class="level-0"></i>
          <i class="level-1"></i>
          <i class="level-2"></i>
          <i class="level-3"></i>
          <i class="level-4"></i>
          <span>高</span>
        </div>
      </div>

      <div class="contribution-shell">
        <div class="month-row">
          <span v-for="month in monthLabels" :key="month">{{ month }}</span>
        </div>

        <div class="heatmap-layout">
          <div class="weekday-column">
            <span v-for="weekday in weekdayLabels" :key="weekday">{{ weekday }}</span>
          </div>

          <div class="heatmap-grid" :style="heatmapGridStyle">
            <div
              v-for="(week, weekIndex) in heatmapWeeks"
              :key="`week-${weekIndex}`"
              class="heatmap-week"
            >
              <div
                v-for="day in week"
                :key="day.key"
                class="heatmap-day"
                :class="[
                  day.isCurrentYear ? `level-${day.level}` : 'is-outside',
                  { 'is-highlight': day.isToday },
                ]"
                :title="day.tooltip"
              ></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="info-card">
      <div class="info-card-header">
        <div>
          <span class="card-kicker">Recent Notifications</span>
          <h4>通知中心</h4>
        </div>

        <button type="button" class="text-link" @click="goTo('/profile/notifications')">
          查看全部
        </button>
      </div>

      <div v-if="overview.notifications?.length" class="list-stack">
        <article
          v-for="notification in overview.notifications.slice(0, 4)"
          :key="notification.id"
          class="list-item"
        >
          <div class="list-item-copy">
            <strong>{{ notification.title }}</strong>
            <p>{{ notification.content }}</p>
            <small>{{ formatManagementTime(notification.createTime) }}</small>
          </div>

          <div class="list-item-actions">
            <span class="status-pill" :class="{ 'is-muted': notification.read }">
              {{ notification.read ? '已读' : '未读' }}
            </span>
            <button
              v-if="!notification.read"
              type="button"
              class="text-link"
              @click="markNotificationRead(notification)"
            >
              标记已读
            </button>
            <button type="button" class="text-link" @click="openNotificationTarget(notification)">
              查看
            </button>
          </div>
        </article>
      </div>

      <el-empty v-else :image-size="72" description="暂无新的通知提醒。" />
    </section>

    <div class="content-grid">
      <section class="info-card">
        <div class="info-card-header">
          <div>
            <span class="card-kicker">Recent Creation</span>
            <h4>最近创作</h4>
          </div>

          <button type="button" class="text-link" @click="goTo('/profile/forum-manage')">
            管理帖子
          </button>
        </div>

        <div v-if="overview.myPosts?.length" class="list-stack">
          <button
            v-for="post in overview.myPosts.slice(0, 4)"
            :key="post.id"
            type="button"
            class="list-item-button"
            @click="goToForumPost(post.id)"
          >
            <div class="list-item-copy">
              <strong>{{ post.title || '未命名帖子' }}</strong>
              <p>{{ post.summary || '进入帖子详情查看完整内容。' }}</p>
            </div>
            <small>{{ formatManagementTime(post.lastActivityTime || post.createTime) }}</small>
          </button>
        </div>

        <el-empty v-else :image-size="72" description="暂时还没有帖子，去发布第一篇讨论吧。" />
      </section>

      <section class="info-card">
        <div class="info-card-header">
          <div>
            <span class="card-kicker">Report Progress</span>
            <h4>举报进度</h4>
          </div>

          <button
            v-if="canViewAdminDashboard"
            type="button"
            class="text-link"
            @click="goTo('/admin/reportmgmt')"
          >
            后台审核页
          </button>
        </div>

        <div v-if="displayedReports.length" class="list-stack">
          <article
            v-for="report in displayedReports"
            :id="`profile-report-${report.id}`"
            :key="report.id"
            class="list-item report-item"
            :class="{ 'is-highlighted': isHighlightedReport(report.id) }"
          >
            <div class="list-item-copy">
              <strong>{{ report.targetTitle || `内容 #${report.targetId}` }}</strong>
              <p>{{ report.detail || `举报原因：${report.reason || '未填写'}` }}</p>
              <small>{{ formatManagementTime(report.updateTime || report.createTime) }}</small>
            </div>

            <span class="status-pill" :class="`state-${String(report.status || '').toLowerCase()}`">
              {{ resolveReportStatusLabel(report.status) }}
            </span>
          </article>
        </div>

        <el-empty v-else :image-size="72" description="你还没有提交过举报记录。" />
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bell, ChatSquare, Comment, Document } from '@element-plus/icons-vue'
import { fetchCurrentUserOverview, markCurrentUserNotificationRead } from '@/api/user'
import { useUserStore } from '@/store/user'
import { formatManagementTime } from '@/utils/profileManagement'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

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

const monthLabels = [
  'Jan',
  'Feb',
  'Mar',
  'Apr',
  'May',
  'Jun',
  'Jul',
  'Aug',
  'Sep',
  'Oct',
  'Nov',
  'Dec',
]
const weekdayLabels = ['S', 'M', 'T', 'W', 'T', 'F', 'S']
const heatmapWeeks = ref([])

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

  return items.slice(0, 4)
})

const profileSummary = computed(() => overview.value.profile || userStore.profile || {})

const statCards = computed(() => [
  {
    label: '我的帖子',
    value: overview.value.postCount || 0,
    hint: '论坛发布与讨论沉淀',
    icon: Document,
    tone: 'green',
  },
  {
    label: '我的评论',
    value: overview.value.commentCount || 0,
    hint: '文章与论坛互动记录',
    icon: Comment,
    tone: 'blue',
  },
  {
    label: '未读通知',
    value: overview.value.unreadNotificationCount || 0,
    hint: '新的站内提醒与处理结果',
    icon: Bell,
    tone: 'violet',
  },
  {
    label: '待处理举报',
    value: overview.value.pendingReportCount || 0,
    hint: '等待管理员处理的反馈',
    icon: ChatSquare,
    tone: 'cyan',
  },
])

const contributionDayValues = computed(() =>
  heatmapWeeks.value
    .flat()
    .filter((day) => day.isCurrentYear)
    .map((day) => Number(day.value || 0)),
)

const contributionSummary = computed(() => {
  const total = contributionDayValues.value.reduce((sum, value) => sum + value, 0)
  const activeDays = contributionDayValues.value.filter((value) => value > 0).length

  if (!total) {
    return '当前年度还没有足够的创作或互动数据，图表区域已按新布局准备完成。'
  }

  return `本年度累计 ${total} 次创作和互动记录，共有 ${activeDays} 天保持活跃。`
})

const heatmapGridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${heatmapWeeks.value.length || 1}, minmax(0, 1fr))`,
}))

const formatMetric = (value) => Number(value || 0).toLocaleString('zh-CN')

const padNumber = (value) => String(value).padStart(2, '0')

const formatDateKey = (date) =>
  `${date.getFullYear()}-${padNumber(date.getMonth() + 1)}-${padNumber(date.getDate())}`

const parseDateValue = (rawValue) => {
  if (!rawValue) return null

  const parsedDate = new Date(rawValue)
  if (Number.isNaN(parsedDate.getTime())) return null
  return parsedDate
}

const buildHeatmapWeeks = () => {
  const currentYear = new Date().getFullYear()
  const todayKey = formatDateKey(new Date())
  const contributionMap = new Map()

  const pushContribution = (list = [], weight = 1, dateKey = 'createTime') => {
    ;(Array.isArray(list) ? list : []).forEach((item) => {
      const parsedDate = parseDateValue(
        item?.[dateKey] || item?.lastActivityTime || item?.updateTime,
      )
      if (!parsedDate || parsedDate.getFullYear() !== currentYear) return

      const key = formatDateKey(parsedDate)
      contributionMap.set(key, (contributionMap.get(key) || 0) + weight)
    })
  }

  pushContribution(overview.value.myPosts, 3, 'createTime')
  pushContribution(overview.value.myComments, 1, 'createTime')
  pushContribution(overview.value.notifications, 1, 'createTime')

  const maxValue = Math.max(...contributionMap.values(), 0)
  const startOfYear = new Date(currentYear, 0, 1)
  const endOfYear = new Date(currentYear, 11, 31)
  const firstVisibleDate = new Date(startOfYear)
  firstVisibleDate.setDate(startOfYear.getDate() - startOfYear.getDay())
  const lastVisibleDate = new Date(endOfYear)
  lastVisibleDate.setDate(endOfYear.getDate() + (6 - endOfYear.getDay()))

  const weeks = []
  const cursor = new Date(firstVisibleDate)

  while (cursor <= lastVisibleDate) {
    const week = []

    for (let dayIndex = 0; dayIndex < 7; dayIndex += 1) {
      const currentDate = new Date(cursor)
      const key = formatDateKey(currentDate)
      const value = contributionMap.get(key) || 0
      const isCurrentYear = currentDate.getFullYear() === currentYear
      const ratio = maxValue > 0 ? value / maxValue : 0

      let level = 0
      if (value > 0 && ratio <= 0.25) level = 1
      else if (value > 0 && ratio <= 0.5) level = 2
      else if (value > 0 && ratio <= 0.75) level = 3
      else if (value > 0) level = 4

      week.push({
        key,
        value,
        level,
        isCurrentYear,
        isToday: key === todayKey,
        tooltip: `${key} · ${value} 次活跃`,
      })

      cursor.setDate(cursor.getDate() + 1)
    }

    weeks.push(week)
  }

  heatmapWeeks.value = weeks
}

const scrollToHighlightedReport = async () => {
  if (!highlightedReportId.value) return

  await nextTick()
  const target = document.getElementById(`profile-report-${highlightedReportId.value}`)
  target?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const loadOverview = async () => {
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

    buildHeatmapWeeks()
    await scrollToHighlightedReport()
  } catch (error) {
    console.error('加载个人总览失败', error)
    ElMessage.error(error.message || '加载个人总览失败，请稍后重试。')
  }
}

const goTo = (path) => router.push(path)

const goToForumPost = (id) => {
  if (!id) return
  router.push(`/forum/${id}`)
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
    overview.value.unreadNotificationCount = Math.max(
      0,
      Number(overview.value.unreadNotificationCount || 0) - 1,
    )
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

const isHighlightedReport = (reportId) => highlightedReportId.value === reportId

onMounted(() => {
  buildHeatmapWeeks()
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
.profile-overview-page {
  --profile-border: #e8efe6;
  --profile-surface: rgba(255, 255, 255, 0.96);
  --profile-shadow: rgba(98, 127, 101, 0.05);
  --profile-accent: #92b696;
  --profile-accent-strong: #82a686;
  --profile-accent-gradient-start: #d7e8d8;
  --profile-accent-gradient-end: #b5d1b9;
  --profile-accent-soft: #f6faf5;
  --profile-accent-soft-strong: #eef4ed;
  --profile-title: #586e5b;
  --profile-body: #8d9b8e;
  --profile-muted: #a1ada2;
  --profile-subtle: #afb9b0;
  --profile-legend: #a4b0a4;
  --profile-month: #7b897c;
  --profile-icon-soft: rgba(170, 200, 173, 0.14);
  --profile-heat-border: #edf2eb;
  --profile-heat-bg: #fcfefb;
  --profile-heat-0: #f6faf5;
  --profile-heat-1: #edf4ec;
  --profile-heat-2: #dfeade;
  --profile-heat-3: #bfd6c0;
  --profile-heat-4: #93b897;
  --profile-item-border: #edf2eb;
  --profile-item-bg: #fdfefd;
  --profile-highlight-ring: rgba(146, 182, 150, 0.2);
  --profile-highlight-shadow: rgba(146, 182, 150, 0.08);
  display: grid;
  gap: 18px;
}
.metric-card {
  height: 80px;
}
.overview-tabbar,
.overview-heading,
.contribution-panel,
.metric-card,
.info-card {
  border: 1px solid var(--profile-border);
  background: var(--profile-surface);
  box-shadow: 0 16px 34px var(--profile-shadow);
}

.overview-tabbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 14px;
  border-radius: 18px;
}

.overview-tabs,
.overview-meta-chips,
.heading-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.overview-tab,
.heading-button {
  height: 40px;
  padding: 0 18px;
  border: 1px solid var(--profile-border);
  border-radius: 12px;
  background: #ffffff;
  color: var(--profile-accent-strong);
  font-size: 15px;
  font-weight: 600;
}

.heading-button,
.text-link,
.list-item-button {
  cursor: pointer;
}

.heading-button.is-primary {
  border-color: transparent;
  background: linear-gradient(
    135deg,
    var(--profile-accent-gradient-start),
    var(--profile-accent-gradient-end)
  );
  color: #ffffff;
}

.meta-chip,
.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: var(--profile-accent-soft);
  color: var(--profile-muted);
  font-size: 13px;
}

.overview-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 24px;
  border-radius: 26px;
}

.overview-kicker,
.panel-kicker,
.card-kicker {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: var(--profile-accent-soft-strong);
  color: var(--profile-accent-strong);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.overview-heading h2,
.contribution-header h3,
.info-card-header h4 {
  margin: 14px 0 0;
  color: var(--profile-title);
  line-height: 1.12;
}

.overview-heading h2 {
  font-size: clamp(28px, 4vw, 36px);
}

.contribution-header h3 {
  font-size: clamp(30px, 4vw, 38px);
}

.info-card-header h4 {
  font-size: 24px;
}

.overview-heading p,
.contribution-header p,
.list-item-copy p {
  margin: 10px 0 0;
  color: var(--profile-body);
  line-height: 1.7;
}

.metrics-grid,
.content-grid,
.account-grid,
.list-stack {
  display: grid;
  gap: 5px;
}

.metrics-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.content-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.content-grid > *,
.list-stack {
  min-width: 0;
}

.metric-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  min-height: 150px;
  padding: 24px;
  border-radius: 22px;
}

.metric-copy span,
.account-item span,
.list-item-copy small {
  display: block;
  color: var(--profile-muted);
  font-size: 14px;
}

.metric-copy strong {
  display: block;
  margin-top: 12px;
  color: var(--profile-title);
  font-size: clamp(34px, 4.2vw, 44px);
  font-weight: 500;
  line-height: 1;
}

.metric-copy small {
  display: block;
  margin-top: 12px;
  color: var(--profile-subtle);
  line-height: 1.6;
}

.metric-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 66px;
  height: 66px;
  border-radius: 18px;
  font-size: 28px;
}

.metric-icon.tone-green {
  background: var(--profile-icon-soft);
  color: var(--profile-accent);
}

.metric-icon.tone-blue {
  background: rgba(59, 130, 246, 0.14);
  color: #3b82f6;
}

.metric-icon.tone-violet {
  background: rgba(139, 92, 246, 0.14);
  color: #8b5cf6;
}

.metric-icon.tone-cyan {
  background: rgba(34, 211, 238, 0.14);
  color: #06b6d4;
}

.contribution-panel,
.info-card {
  padding: 28px;
  border-radius: 28px;
}

.contribution-header,
.info-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.contribution-legend {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 10px;
  color: var(--profile-legend);
  font-size: 12px;
}

.contribution-legend i {
  width: 24px;
  height: 14px;
  border-radius: 6px;
}

.contribution-shell {
  margin-top: 30px;
}

.month-row {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 8px;
  padding-left: 48px;
  color: var(--profile-month);
  font-size: 15px;
  font-weight: 700;
}

.heatmap-layout {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr);
  gap: 14px;
  margin-top: 14px;
}

.weekday-column {
  display: grid;
  grid-template-rows: repeat(7, 1fr);
  gap: 6px;
  padding-top: 6px;
}

.weekday-column span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--profile-legend);
  font-size: 13px;
  font-weight: 700;
}

.heatmap-grid {
  display: grid;
  gap: 6px;
}

.heatmap-week {
  display: grid;
  grid-template-rows: repeat(7, minmax(0, 1fr));
  gap: 6px;
}

.heatmap-day {
  aspect-ratio: 1 / 1;
  border-radius: 4px;
  border: 1px solid var(--profile-heat-border);
  background: var(--profile-heat-bg);
}

.heatmap-day.is-outside {
  border-color: transparent;
  background: transparent;
}

.heatmap-day.is-highlight {
  box-shadow: 0 0 0 1px var(--profile-highlight-ring);
}

.level-0 {
  background: var(--profile-heat-0);
}

.level-1 {
  background: var(--profile-heat-1);
}

.level-2 {
  background: var(--profile-heat-2);
}

.level-3 {
  background: var(--profile-heat-3);
}

.level-4 {
  background: var(--profile-heat-4);
}

.account-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-top: 20px;
}

.account-item,
.list-item,
.list-item-button {
  box-sizing: border-box;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  width: 100%;
  padding: 16px;
  border: 1px solid var(--profile-item-border);
  border-radius: 18px;
  background: var(--profile-item-bg);
  text-align: left;
}

.list-item-copy {
  min-width: 0;
}

.list-item-copy strong,
.account-item strong {
  display: block;
  color: var(--profile-title);
  line-height: 1.5;
}

.list-item-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.status-pill.is-muted,
.status-pill.state-rejected {
  background: #f3f4f6;
  color: #7b8394;
}

.status-pill.state-resolved {
  background: #eaf8ef;
  color: #15803d;
}

.status-pill.state-pending {
  background: var(--profile-accent-soft-strong);
  color: var(--profile-accent-strong);
}

.report-item.is-highlighted {
  border-color: var(--profile-highlight-ring);
  box-shadow: 0 0 0 3px var(--profile-highlight-shadow);
}

.text-link {
  border: 0;
  background: transparent;
  color: var(--profile-accent-strong);
  font-size: 14px;
}

@media (max-width: 1180px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .overview-heading,
  .contribution-header,
  .info-card-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 820px) {
  .content-grid,
  .account-grid,
  .month-row {
    grid-template-columns: 1fr;
  }

  .month-row {
    padding-left: 0;
  }

  .heatmap-layout {
    grid-template-columns: 1fr;
  }

  .weekday-column {
    grid-template-columns: repeat(7, minmax(0, 1fr));
    grid-template-rows: none;
  }
}

@media (max-width: 720px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }

  .overview-tabbar,
  .overview-heading,
  .list-item,
  .list-item-button,
  .account-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .contribution-panel,
  .info-card,
  .metric-card {
    padding: 20px;
  }

  .heatmap-grid {
    overflow-x: auto;
    min-width: 640px;
  }

  .list-item-actions,
  .heading-actions {
    width: 100%;
    align-items: stretch;
  }
}
</style>

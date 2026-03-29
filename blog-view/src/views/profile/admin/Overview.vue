<template>
  <section class="admin-overview-page">
    <workspace-permission-notice
      v-if="!canViewDashboard"
      message="当前账号没有查看管理概览的权限。"
    />

    <template v-else>
      <div class="metrics-grid">
        <article v-for="card in statCards" :key="card.label" class="metric-card">
          <div class="metric-copy">
            <span>{{ card.label }}</span>
            <strong>{{ formatMetric(card.value) }}</strong>
          </div>

          <div class="metric-icon" :class="`tone-${card.tone}`">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
        </article>
      </div>

      <section v-if="canManageSite" class="announcement-panel">
        <div class="announcement-panel-header">
          <div class="announcement-panel-copy">
            <span class="panel-kicker">Site Broadcast</span>
            <h3>站点公告</h3>
            <p>{{ announcementPanelDescription }}</p>
          </div>

          <div class="announcement-panel-actions">
            <button type="button" class="announcement-action is-primary" @click="goTo('/admin/announcements?composeAnnouncement=1')">
              发布公告
            </button>
            <button type="button" class="announcement-action" @click="goTo('/admin/announcements')">
              管理公告
            </button>
          </div>
        </div>

        <div v-if="recentAnnouncements.length" class="announcement-list">
          <article v-for="item in recentAnnouncements" :key="item.id" class="announcement-item">
            <div class="announcement-item-meta">
              <span>{{ formatAnnouncementTime(item.publishTime || item.createTime) }}</span>
              <span>{{ item.authorNickname || '管理员' }}</span>
            </div>
            <strong>{{ item.title }}</strong>
            <p>{{ item.content }}</p>
          </article>
        </div>

        <div v-else class="announcement-empty">
          暂无站点公告，现在可以直接发布第一条公告。
        </div>
      </section>

      <section class="contribution-panel">
        <div class="contribution-header">
          <div class="contribution-header-copy">
            <span class="panel-kicker">Contribution Graph</span>
            <h3>今年文章贡献度</h3>
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

      <div class="overview-footnote">
        <span>最近更新 {{ lastUpdatedLabel }}</span>
      </div>
    </template>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatSquare, Comment, Document, House } from '@element-plus/icons-vue'
import { fetchAdminContributionStatistics, fetchAdminDashboardStatistics } from '@/api/admin'
import { fetchAdminAnnouncements, fetchAdminSiteInfo } from '@/api/admin/site'
import { fetchCurrentUserOverview } from '@/api/user'
import WorkspacePermissionNotice from '@/components/profile/workspace/WorkspacePermissionNotice.vue'
import { useUserStore } from '@/store/user'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'

const router = useRouter()
const userStore = useUserStore()

const dashboard = reactive({
  articleCount: 0,
  commentCount: 0,
  momentCount: 0,
  forumPostCount: 0,
})

const adminSummary = reactive({
  unreadNotificationCount: 0,
  pendingReportCount: 0,
})

const siteInfo = reactive({
  name: '',
  description: '',
})

const announcements = ref([])
const heatmapWeeks = ref([])
const lastUpdatedAt = ref(new Date())

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

const canViewDashboard = computed(() =>
  userStore.hasCapability(WORKSPACE_CAPABILITIES.DASHBOARD_VIEW),
)
const canManageSite = computed(() =>
  userStore.hasCapability(WORKSPACE_CAPABILITIES.SITE_MANAGE),
)

const statCards = computed(() => [
  {
    label: '文章数',
    value: dashboard.articleCount,
    icon: Document,
    tone: 'blue',
  },
  {
    label: '评论数',
    value: dashboard.commentCount,
    icon: Comment,
    tone: 'violet',
  },
  {
    label: '动态数',
    value: dashboard.momentCount,
    icon: House,
    tone: 'amber',
  },
  {
    label: '论坛帖子',
    value: dashboard.forumPostCount,
    icon: ChatSquare,
    tone: 'cyan',
  },
])

const overviewDescription = computed(() => {
  if (siteInfo.description) {
    return siteInfo.description
  }

  return '总览后台关键数据、审核提醒和年度内容贡献节奏。'
})

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
    return '本年度还没有可展示的贡献数据，图表区域已预留布局。'
  }

  return `本年度累计 ${total} 次内容贡献，共有 ${activeDays} 天产生更新。`
})

const recentAnnouncements = computed(() => announcements.value.slice(0, 3))

const announcementPanelDescription = computed(() => {
  const total = announcements.value.length

  if (!total) {
    return '站点首页和论坛页都会展示公告内容，支持从后台直接维护发布信息。'
  }

  const siteName = siteInfo.name || '当前站点'
  return `${siteName} 目前共维护 ${total} 条公告，可继续发布新内容或进入公告管理页统一管理。`
})

const lastUpdatedLabel = computed(() =>
  new Intl.DateTimeFormat('zh-CN', {
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(lastUpdatedAt.value),
)

const heatmapGridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${heatmapWeeks.value.length || 1}, minmax(0, 1fr))`,
}))

const normalizeMetricValue = (value) => Number(value || 0)

const formatMetric = (value) => Number(value || 0).toLocaleString('zh-CN')

const formatAnnouncementTime = (value) => {
  const parsedDate = parseDateValue(value)

  if (!parsedDate) return '待发布'

  return new Intl.DateTimeFormat('zh-CN', {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(parsedDate)
}

const padNumber = (value) => String(value).padStart(2, '0')

const formatDateKey = (date) =>
  `${date.getFullYear()}-${padNumber(date.getMonth() + 1)}-${padNumber(date.getDate())}`

const parseDateValue = (rawValue) => {
  if (!rawValue) return null

  const parsedDate = new Date(rawValue)
  if (Number.isNaN(parsedDate.getTime())) return null
  return parsedDate
}

const resolveContributionValue = (item) =>
  Number(item?.count ?? item?.articleCount ?? item?.value ?? item?.total ?? 0)

const resolveContributionDate = (item) => item?.date || item?.day || item?.label || item?.name || ''

const buildContributionMap = (list = []) => {
  const currentYear = new Date().getFullYear()
  const startOfYear = new Date(currentYear, 0, 1)
  const items = Array.isArray(list) ? list : []
  const contributionMap = new Map()

  items.forEach((item, index) => {
    const value = resolveContributionValue(item)
    if (value <= 0) return

    const parsedDate = parseDateValue(resolveContributionDate(item))
    const fallbackDate = new Date(startOfYear)
    fallbackDate.setDate(startOfYear.getDate() + index)
    const targetDate = parsedDate || fallbackDate

    if (targetDate.getFullYear() !== currentYear) return

    const key = formatDateKey(targetDate)
    contributionMap.set(key, (contributionMap.get(key) || 0) + value)
  })

  return contributionMap
}

const buildHeatmapWeeks = (list = []) => {
  const currentYear = new Date().getFullYear()
  const todayKey = formatDateKey(new Date())
  const contributionMap = buildContributionMap(list)
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
        tooltip: `${key} · ${value} 次贡献`,
      })

      cursor.setDate(cursor.getDate() + 1)
    }

    weeks.push(week)
  }

  return weeks
}

const loadOverview = async () => {
  if (!canViewDashboard.value) return

  try {
    const [
      dashboardResponse,
      contributionResponse,
      siteResponse,
      announcementResponse,
      userOverview,
    ] = await Promise.all([
      fetchAdminDashboardStatistics(),
      fetchAdminContributionStatistics(),
      fetchAdminSiteInfo(),
      fetchAdminAnnouncements(),
      fetchCurrentUserOverview(),
    ])

    dashboard.articleCount = normalizeMetricValue(dashboardResponse?.articleCount)
    dashboard.commentCount = normalizeMetricValue(dashboardResponse?.commentCount)
    dashboard.momentCount = normalizeMetricValue(dashboardResponse?.momentCount)
    dashboard.forumPostCount = normalizeMetricValue(dashboardResponse?.forumPostCount)

    adminSummary.unreadNotificationCount = normalizeMetricValue(
      userOverview?.unreadNotificationCount,
    )
    adminSummary.pendingReportCount = normalizeMetricValue(userOverview?.pendingReportCount)

    siteInfo.name = siteResponse?.name || ''
    siteInfo.description = siteResponse?.description || ''
    announcements.value = Array.isArray(announcementResponse) ? announcementResponse : []
    heatmapWeeks.value = buildHeatmapWeeks(contributionResponse)
    lastUpdatedAt.value = new Date()

    if (userOverview?.profile) {
      userStore.hydrateFromServerProfile(userOverview.profile)
    }
  } catch (error) {
    console.error('加载管理概览失败', error)
    ElMessage.error(error.message || '加载管理概览失败，请稍后重试。')
  }
}

const goTo = (path) => router.push(path)

onMounted(() => {
  heatmapWeeks.value = buildHeatmapWeeks([])
  loadOverview()
})
</script>

<style scoped>
.admin-overview-page {
  display: grid;
  gap: 18px;
}

.overview-tabbar,
.overview-heading,
.announcement-panel,
.contribution-panel,
.metric-card {
  border: 1px solid #e8edf5;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 16px 34px rgba(22, 34, 72, 0.05);
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
.overview-meta-chips {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.overview-tab {
  height: 40px;
  padding: 0 18px;
  border: 1px solid #dfe7f5;
  border-radius: 12px;
  background: #ffffff;
  color: #5a73ff;
  font-size: 15px;
  font-weight: 600;
}

.meta-chip {
  display: inline-flex;
  align-items: center;
  height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: #f4f7fc;
  color: #7b869d;
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
.panel-kicker {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: #eef2ff;
  color: #6880ff;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.overview-heading h2,
.contribution-header h3 {
  margin: 14px 0 0;
  color: #1c2758;
  font-size: clamp(28px, 4vw, 36px);
  line-height: 1.12;
}

.overview-heading p,
.contribution-header p {
  margin: 10px 0 0;
  color: #7f879b;
  line-height: 1.7;
}

.overview-refresh {
  min-width: 120px;
  height: 42px;
  padding: 0 18px;
  border: 1px solid #dfe6f4;
  border-radius: 12px;
  background: #ffffff;
  color: #506bff;
  font-size: 14px;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.overview-refresh:hover,
.footnote-link:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(92, 124, 255, 0.12);
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
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

.metric-copy span {
  display: block;
  color: #7080a0;
  font-size: 15px;
}

.metric-copy strong {
  display: block;
  margin-top: 12px;
  color: #15285b;
  font-size: clamp(34px, 4.2vw, 44px);
  font-weight: 500;
  line-height: 1;
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

.metric-icon.tone-blue {
  background: rgba(92, 124, 255, 0.12);
  color: #5a74ff;
}

.metric-icon.tone-violet {
  background: rgba(129, 140, 248, 0.12);
  color: #6366f1;
}

.metric-icon.tone-amber {
  background: rgba(251, 191, 36, 0.14);
  color: #f59e0b;
}

.metric-icon.tone-cyan {
  background: rgba(34, 211, 238, 0.14);
  color: #06b6d4;
}

.announcement-panel {
  padding: 28px;
  border-radius: 28px;
}

.announcement-panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.announcement-panel-copy h3 {
  margin: 14px 0 0;
  color: #1c2758;
  font-size: clamp(28px, 4vw, 34px);
  line-height: 1.12;
}

.announcement-panel-copy p {
  margin: 10px 0 0;
  color: #7f879b;
  line-height: 1.7;
}

.announcement-panel-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.announcement-action {
  height: 42px;
  padding: 0 18px;
  border: 1px solid #dfe6f4;
  border-radius: 12px;
  background: #ffffff;
  color: #506bff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
}

.announcement-action.is-primary {
  border-color: #506bff;
  background: linear-gradient(135deg, #5a74ff 0%, #7c91ff 100%);
  color: #ffffff;
}

.announcement-action:hover {
  transform: translateY(-1px);
  box-shadow: 0 12px 24px rgba(92, 124, 255, 0.12);
}

.announcement-list {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 26px;
}

.announcement-item,
.announcement-empty {
  min-height: 168px;
  padding: 20px;
  border: 1px solid #e7ecf5;
  border-radius: 20px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.announcement-item-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #8c97ab;
  font-size: 12px;
}

.announcement-item strong {
  display: block;
  margin-top: 14px;
  color: #1f2b5a;
  font-size: 18px;
}

.announcement-item p,
.announcement-empty {
  margin: 12px 0 0;
  color: #67748a;
  line-height: 1.7;
}

.contribution-panel {
  padding: 28px 28px 26px;
  border-radius: 28px;
}

.contribution-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.contribution-header h3 {
  font-size: clamp(32px, 4vw, 40px);
}

.contribution-legend {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-top: 10px;
  color: #8a93a8;
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
  color: #3c4254;
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
  color: #8b929f;
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
  border: 1px solid #e5e8ef;
  background: #f8fafc;
}

.heatmap-day.is-outside {
  border-color: transparent;
  background: transparent;
}

.heatmap-day.is-highlight {
  box-shadow: 0 0 0 1px rgba(92, 124, 255, 0.22);
}

.level-0 {
  background: #eef2f7;
}

.level-1 {
  background: #d9f2fb;
}

.level-2 {
  background: #98dbf7;
}

.level-3 {
  background: #45bddf;
}

.level-4 {
  background: #0d6f7d;
}

.overview-footnote {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #95a0b5;
  font-size: 13px;
}

.footnote-link {
  height: 38px;
  padding: 0 16px;
  border: 1px solid #dfe6f4;
  border-radius: 12px;
  background: #ffffff;
  color: #546fff;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

@media (max-width: 1180px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .contribution-header,
  .announcement-panel-header,
  .overview-heading {
    flex-direction: column;
    align-items: flex-start;
  }

  .announcement-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 820px) {
  .month-row {
    grid-template-columns: repeat(6, minmax(0, 1fr));
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
  .overview-footnote {
    flex-direction: column;
    align-items: flex-start;
  }

  .contribution-panel,
  .announcement-panel,
  .overview-heading,
  .metric-card {
    padding: 20px;
  }

  .heatmap-grid {
    overflow-x: auto;
  }

  .heatmap-grid {
    min-width: 640px;
  }
}
</style>

<template>
  <workspace-page-shell
    title="管理概览"
    description="快速查看站点状态、待处理事项和近期运营趋势。"
    :loading="loading"
    @refresh="loadOverview"
  >
    <template v-if="!canViewDashboard">
      <workspace-permission-notice message="当前账号没有查看管理概览的权限。" />
    </template>
    <template v-else>
      <section class="overview-hero">
        <div>
          <p class="hero-kicker">Admin Overview</p>
          <h3>{{ siteInfo.name || 'Dream Studio' }}</h3>
          <p>{{ siteInfo.description || '从这里总览站点内容状态、处理待办并快速进入常用管理页。' }}</p>
          <div class="hero-tags">
            <el-tag effect="plain">{{ formatAnnouncementCount(announcements.length) }}</el-tag>
            <el-tag effect="plain" type="warning">待处理举报 {{ adminSummary.pendingReportCount || 0 }}</el-tag>
            <el-tag effect="plain" type="success">未读通知 {{ adminSummary.unreadNotificationCount || 0 }}</el-tag>
          </div>
        </div>

        <div class="hero-actions">
          <el-button type="primary" @click="goTo('/admin/writearticle')">写文章</el-button>
          <el-button plain @click="goTo('/admin/commentmgmt')">评论管理</el-button>
          <el-button plain @click="goTo('/admin/usermgmt')">用户管理</el-button>
          <el-button text @click="goTo('/admin/site')">站点设置</el-button>
        </div>
      </section>

      <div class="stats-grid">
        <overview-stat-card label="文章总数" :value="dashboard.articleCount" hint="当前全站累计文章数量。" />
        <overview-stat-card label="评论总数" :value="dashboard.commentCount" hint="包括文章和论坛中的评论互动。" />
        <overview-stat-card label="动态总数" :value="dashboard.momentCount" hint="用于观察时间轴内容更新频率。" />
        <overview-stat-card label="论坛帖子" :value="dashboard.forumPostCount" hint="集中反映论坛活跃度与讨论沉淀。" />
      </div>

      <div class="overview-grid overview-grid-two">
        <overview-section-card title="近期贡献趋势" description="后端统计接口已经返回发文趋势，这里用简洁柱状条展示。">
          <div v-if="contributionSeries.length" class="bar-grid">
            <div v-for="item in contributionSeries" :key="item.label" class="bar-item">
              <span>{{ item.value }}</span>
              <div class="bar-track">
                <i :style="{ height: `${item.height}%` }"></i>
              </div>
              <small>{{ item.label }}</small>
            </div>
          </div>
          <el-empty v-else :image-size="72" description="暂无趋势数据。" />
        </overview-section-card>

        <overview-section-card title="待处理事项" description="优先关注需要立即处理的管理动作。">
          <div class="task-list">
            <article class="task-item">
              <strong>待处理举报</strong>
              <p>当前还有 {{ adminSummary.pendingReportCount || 0 }} 条举报等待处理。</p>
              <el-button text type="primary" @click="goTo('/admin/reportmgmt')">查看详情</el-button>
            </article>
            <article class="task-item">
              <strong>未读通知</strong>
              <p>你有 {{ adminSummary.unreadNotificationCount || 0 }} 条站内提醒尚未查看。</p>
              <el-button text type="primary" @click="goTo('/profile/notifications')">前往通知中心</el-button>
            </article>
            <article class="task-item">
              <strong>站点公告</strong>
              <p>当前展示 {{ announcements.length }} 条公告，可在站点设置页继续维护。</p>
              <el-button text type="primary" @click="goTo('/admin/site')">管理公告</el-button>
            </article>
          </div>
        </overview-section-card>
      </div>

      <div class="overview-grid overview-grid-two">
        <overview-section-card title="分类分布" description="帮助快速识别内容集中在哪些分类下。">
          <div v-if="categoryDistribution.length" class="distribution-list">
            <div v-for="item in categoryDistribution" :key="item.label" class="distribution-item">
              <div class="distribution-meta">
                <strong>{{ item.label }}</strong>
                <span>{{ item.value }}</span>
              </div>
              <div class="distribution-track">
                <i :style="{ width: `${item.width}%` }"></i>
              </div>
            </div>
          </div>
          <el-empty v-else :image-size="72" description="暂无分类统计数据。" />
        </overview-section-card>

        <overview-section-card title="标签热度" description="帮助判断当前内容热点和标签使用情况。">
          <div v-if="tagDistribution.length" class="distribution-list">
            <div v-for="item in tagDistribution" :key="item.label" class="distribution-item">
              <div class="distribution-meta">
                <strong>#{{ item.label }}</strong>
                <span>{{ item.value }}</span>
              </div>
              <div class="distribution-track">
                <i :style="{ width: `${item.width}%` }"></i>
              </div>
            </div>
          </div>
          <el-empty v-else :image-size="72" description="暂无标签统计数据。" />
        </overview-section-card>
      </div>

      <div class="overview-grid overview-grid-two">
        <overview-section-card title="最近通知" description="管理员账号最近收到的站内提醒。">
          <div v-if="recentNotifications.length" class="list-stack">
            <article v-for="notification in recentNotifications" :key="notification.id" class="list-item">
              <div>
                <strong>{{ notification.title }}</strong>
                <p>{{ notification.content }}</p>
              </div>
              <span>{{ formatManagementTime(notification.createTime) }}</span>
            </article>
          </div>
          <el-empty v-else :image-size="72" description="暂无通知。" />
        </overview-section-card>

        <overview-section-card title="活跃帖子" description="优先关注最近仍有互动的帖子。">
          <div v-if="moderationPosts.length" class="list-stack">
            <button v-for="post in moderationPosts" :key="post.id" type="button" class="list-item-button" @click="goToForumPost(post.id)">
              <div>
                <strong>{{ post.title || '未命名帖子' }}</strong>
                <p>{{ post.summary || '进入详情查看当前讨论内容。' }}</p>
              </div>
              <span>{{ formatManagementTime(post.lastActivityTime || post.createTime) }}</span>
            </button>
          </div>
          <el-empty v-else :image-size="72" description="暂无活跃帖子数据。" />
        </overview-section-card>
      </div>

      <div class="overview-grid overview-grid-two">
        <overview-section-card title="站点摘要" description="概览当前站点基础信息。">
          <div class="site-info-grid">
            <article class="site-info-item">
              <span>网站名称</span>
              <strong>{{ siteInfo.name || 'Dream Studio' }}</strong>
            </article>
            <article class="site-info-item">
              <span>网站作者</span>
              <strong>{{ siteInfo.author || '未填写' }}</strong>
            </article>
            <article class="site-info-item">
              <span>网站关键词</span>
              <strong>{{ siteInfo.keywords || '未填写' }}</strong>
            </article>
            <article class="site-info-item">
              <span>ICP备案号</span>
              <strong>{{ siteInfo.icp || '未填写' }}</strong>
            </article>
          </div>
        </overview-section-card>

        <overview-section-card title="最新公告" description="论坛页当前展示的公告内容。">
          <div v-if="announcements.length" class="list-stack">
            <article v-for="item in announcements.slice(0, 4)" :key="item.id" class="list-item">
              <div>
                <strong>{{ item.title }}</strong>
                <p>{{ item.content }}</p>
              </div>
              <span>{{ formatManagementTime(item.publishTime) }}</span>
            </article>
          </div>
          <el-empty v-else :image-size="72" description="暂无公告。" />
        </overview-section-card>
      </div>
    </template>
  </workspace-page-shell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  fetchAdminCategoryStatistics,
  fetchAdminContributionStatistics,
  fetchAdminDashboardStatistics,
  fetchAdminTagStatistics,
} from '@/api/admin'
import { fetchAdminAnnouncements, fetchAdminSiteInfo } from '@/api/admin/site'
import { fetchCurrentUserOverview } from '@/api/user'
import OverviewSectionCard from '@/components/profile/workspace/OverviewSectionCard.vue'
import OverviewStatCard from '@/components/profile/workspace/OverviewStatCard.vue'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'
import WorkspacePermissionNotice from '@/components/profile/workspace/WorkspacePermissionNotice.vue'
import { useUserStore } from '@/store/user'
import { formatManagementTime } from '@/utils/profileManagement'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const dashboard = reactive({
  articleCount: 0,
  commentCount: 0,
  momentCount: 0,
  forumPostCount: 0,
})
const siteInfo = reactive({
  name: '',
  author: '',
  keywords: '',
  description: '',
  icp: '',
})
const adminSummary = reactive({
  unreadNotificationCount: 0,
  pendingReportCount: 0,
})
const announcements = ref([])
const contributionSeries = ref([])
const categoryDistribution = ref([])
const tagDistribution = ref([])
const recentNotifications = ref([])
const moderationPosts = ref([])

const canViewDashboard = computed(() =>
  userStore.hasCapability(WORKSPACE_CAPABILITIES.DASHBOARD_VIEW),
)

const normalizeMetricValue = (value) => Number(value || 0)

const resolveDistributionLabel = (item, fallback) => {
  return item?.name || item?.categoryName || item?.tagName || item?.label || item?.title || fallback
}

const resolveDistributionValue = (item) => {
  return Number(item?.count ?? item?.articleCount ?? item?.value ?? item?.total ?? 0)
}

const normalizeDistribution = (list = [], fallbackPrefix = '项目') => {
  const items = Array.isArray(list) ? list : []
  const maxValue = Math.max(...items.map((item) => resolveDistributionValue(item)), 0)

  return items.slice(0, 6).map((item, index) => {
    const value = resolveDistributionValue(item)
    return {
      label: resolveDistributionLabel(item, `${fallbackPrefix} ${index + 1}`),
      value,
      width: maxValue > 0 ? Math.max((value / maxValue) * 100, 8) : 0,
    }
  })
}

const normalizeContributionSeries = (list = []) => {
  const items = Array.isArray(list) ? list.slice(-10) : []
  const maxValue = Math.max(...items.map((item) => resolveDistributionValue(item)), 0)

  return items.map((item, index) => {
    const value = resolveDistributionValue(item)
    const rawLabel = String(item?.date || item?.label || item?.name || `#${index + 1}`)

    return {
      label: rawLabel.length > 5 ? rawLabel.slice(-5) : rawLabel,
      value,
      height: maxValue > 0 ? Math.max((value / maxValue) * 100, 12) : 0,
    }
  })
}

const loadOverview = async () => {
  if (!canViewDashboard.value) return

  loading.value = true

  try {
    const [
      dashboardResponse,
      contributionResponse,
      categoryResponse,
      tagResponse,
      siteResponse,
      announcementResponse,
      userOverviewResponse,
    ] = await Promise.all([
      fetchAdminDashboardStatistics(),
      fetchAdminContributionStatistics(),
      fetchAdminCategoryStatistics(),
      fetchAdminTagStatistics(),
      fetchAdminSiteInfo(),
      fetchAdminAnnouncements(),
      fetchCurrentUserOverview(),
    ])

    dashboard.articleCount = normalizeMetricValue(dashboardResponse?.articleCount)
    dashboard.commentCount = normalizeMetricValue(dashboardResponse?.commentCount)
    dashboard.momentCount = normalizeMetricValue(dashboardResponse?.momentCount)
    dashboard.forumPostCount = normalizeMetricValue(dashboardResponse?.forumPostCount)

    siteInfo.name = siteResponse?.name || ''
    siteInfo.author = siteResponse?.author || ''
    siteInfo.keywords = siteResponse?.keywords || ''
    siteInfo.description = siteResponse?.description || ''
    siteInfo.icp = siteResponse?.icp || ''

    adminSummary.unreadNotificationCount = normalizeMetricValue(userOverviewResponse?.unreadNotificationCount)
    adminSummary.pendingReportCount = normalizeMetricValue(userOverviewResponse?.pendingReportCount)
    announcements.value = Array.isArray(announcementResponse) ? announcementResponse : []
    contributionSeries.value = normalizeContributionSeries(contributionResponse)
    categoryDistribution.value = normalizeDistribution(categoryResponse, '分类')
    tagDistribution.value = normalizeDistribution(tagResponse, '标签')
    recentNotifications.value = Array.isArray(userOverviewResponse?.notifications)
      ? userOverviewResponse.notifications.slice(0, 5)
      : []
    moderationPosts.value = Array.isArray(userOverviewResponse?.moderationPosts)
      ? userOverviewResponse.moderationPosts.slice(0, 5)
      : []
  } catch (error) {
    console.error('加载管理概览失败', error)
    ElMessage.error(error.message || '加载管理概览失败，请稍后重试。')
  } finally {
    loading.value = false
  }
}

const formatAnnouncementCount = (count) => `站内公告 ${count} 条`
const goTo = (path) => router.push(path)

const goToForumPost = (id) => {
  if (!id) return
  router.push(`/forum/${id}`)
}

onMounted(() => {
  loadOverview()
})
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
    radial-gradient(circle at top right, rgba(45, 212, 191, 0.18), transparent 34%),
    linear-gradient(135deg, rgba(240, 253, 250, 0.96), rgba(255, 255, 255, 0.92));
}

.hero-kicker {
  margin: 0 0 8px;
  color: #0f766e;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.overview-hero h3 {
  margin: 0;
  color: #134e4a;
  font-size: 30px;
}

.overview-hero p {
  margin: 10px 0 0;
  color: #134e4a;
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

.bar-grid {
  display: grid;
  grid-template-columns: repeat(10, minmax(0, 1fr));
  gap: 10px;
  align-items: end;
}

.bar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.bar-item span,
.bar-item small {
  color: #64748b;
  font-size: 12px;
}

.bar-track {
  display: flex;
  align-items: end;
  width: 100%;
  height: 140px;
  padding: 6px;
  border-radius: 14px;
  background: rgba(15, 118, 110, 0.08);
}

.bar-track i {
  display: block;
  width: 100%;
  border-radius: 10px;
  background: linear-gradient(180deg, #14b8a6, #0f766e);
}

.task-list,
.distribution-list,
.list-stack,
.site-info-grid {
  display: grid;
  gap: 12px;
}

.task-item,
.list-item,
.list-item-button,
.site-info-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(248, 250, 252, 0.86);
}

.list-item-button {
  width: 100%;
  text-align: left;
  cursor: pointer;
}

.task-item strong,
.list-item strong,
.list-item-button strong,
.site-info-item strong,
.distribution-meta strong {
  display: block;
  color: #0f172a;
}

.task-item p,
.list-item p,
.list-item-button p {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.list-item span,
.list-item-button span,
.site-info-item span,
.distribution-meta span {
  flex-shrink: 0;
  color: #64748b;
}

.distribution-item {
  display: grid;
  gap: 8px;
}

.distribution-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.distribution-track {
  width: 100%;
  height: 10px;
  border-radius: 999px;
  background: rgba(15, 118, 110, 0.08);
  overflow: hidden;
}

.distribution-track i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #14b8a6, #0f766e);
}

.site-info-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.site-info-item {
  flex-direction: column;
}

@media (max-width: 1100px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .overview-grid-two,
  .site-info-grid {
    grid-template-columns: 1fr;
  }

  .bar-grid {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .overview-hero,
  .task-item,
  .list-item,
  .list-item-button {
    flex-direction: column;
  }

  .stats-grid,
  .bar-grid {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    width: 100%;
    justify-content: stretch;
  }
}
</style>

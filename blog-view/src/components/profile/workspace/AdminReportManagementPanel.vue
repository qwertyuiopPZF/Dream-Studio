<template>
  <workspace-page-shell
    title="举报审核"
    description="集中处理论坛举报，并记录处理结论与审核备注。"
    :loading="loading"
    @refresh="loadReports"
  >
    <template v-if="!canReviewReports">
      <workspace-permission-notice message="当前账号没有举报审核权限。" />
    </template>
    <template v-else>
      <div class="stats-grid">
        <overview-stat-card label="待处理" :value="pendingCount" hint="优先处理仍处于待审核状态的举报。" />
        <overview-stat-card label="已处理" :value="resolvedCount" hint="已经确认并完成处理的举报数量。" />
        <overview-stat-card label="已驳回" :value="rejectedCount" hint="不成立或无需继续处理的举报数量。" />
      </div>

      <section class="toolbar-row">
        <el-radio-group v-model="activeFilter" size="small">
          <el-radio-button label="ALL">全部</el-radio-button>
          <el-radio-button label="PENDING">待处理</el-radio-button>
          <el-radio-button label="RESOLVED">已处理</el-radio-button>
          <el-radio-button label="REJECTED">已驳回</el-radio-button>
        </el-radio-group>
      </section>

      <div v-if="filteredReports.length" class="report-list">
        <article
          v-for="report in filteredReports"
          :id="`report-card-${report.id}`"
          :key="report.id"
          class="report-card"
          :class="{ 'is-highlighted': isHighlightedReport(report.id) }"
        >
          <div class="report-main">
            <div class="report-title-row">
              <strong>{{ report.targetTitle || `内容 #${report.targetId}` }}</strong>
              <el-tag :type="resolveReportStatusType(report.status)" effect="plain">
                {{ resolveReportStatusLabel(report.status) }}
              </el-tag>
            </div>

            <p class="report-meta">
              举报人：{{ report.reporterName || '匿名用户' }} · 原因：{{ report.reason || '未填写' }} · 更新时间：
              {{ formatManagementTime(report.updateTime || report.createTime) }}
            </p>
            <p class="report-detail">{{ report.detail || '举报人未提供补充说明。' }}</p>
            <p v-if="report.reviewerNote" class="review-note">审核备注：{{ report.reviewerNote }}</p>
          </div>

          <div class="report-actions">
            <el-button text @click="openReportTarget(report)">查看目标</el-button>
            <el-button v-if="isPending(report)" text type="success" @click="openReviewDialog(report, 'RESOLVED')">
              标记已处理
            </el-button>
            <el-button v-if="isPending(report)" text type="warning" @click="openReviewDialog(report, 'REJECTED')">
              驳回举报
            </el-button>
          </div>
        </article>
      </div>

      <el-empty v-else :image-size="84" description="当前筛选条件下暂无举报记录" />

      <el-dialog v-model="reviewDialog.visible" :title="reviewDialog.status === 'RESOLVED' ? '标记为已处理' : '驳回举报'" width="520px">
        <el-form label-position="top">
          <el-form-item label="审核备注">
            <el-input
              v-model="reviewDialog.reviewerNote"
              type="textarea"
              :rows="4"
              maxlength="300"
              show-word-limit
              placeholder="可选，向举报人补充说明处理结果。"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="reviewDialog.visible = false">取消</el-button>
          <el-button type="primary" :loading="reviewDialog.loading" @click="submitReview">确认提交</el-button>
        </template>
      </el-dialog>
    </template>
  </workspace-page-shell>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchCurrentUserOverview, reviewForumReport } from '@/api/user'
import OverviewStatCard from '@/components/profile/workspace/OverviewStatCard.vue'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'
import WorkspacePermissionNotice from '@/components/profile/workspace/WorkspacePermissionNotice.vue'
import { useUserStore } from '@/store/user'
import { formatManagementTime } from '@/utils/profileManagement'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const activeFilter = ref('PENDING')
const reports = ref([])
const reviewDialog = reactive({
  visible: false,
  loading: false,
  reportId: null,
  status: 'RESOLVED',
  reviewerNote: '',
})

const canReviewReports = computed(() => userStore.hasCapability(WORKSPACE_CAPABILITIES.REPORT_REVIEW))
const highlightedReportId = computed(() => Number(route.query.reportId || 0) || null)
const pendingCount = computed(() => reports.value.filter((item) => String(item.status || '').toUpperCase() === 'PENDING').length)
const resolvedCount = computed(() => reports.value.filter((item) => String(item.status || '').toUpperCase() === 'RESOLVED').length)
const rejectedCount = computed(() => reports.value.filter((item) => String(item.status || '').toUpperCase() === 'REJECTED').length)
const filteredReports = computed(() => {
  const baseReports = activeFilter.value === 'ALL'
    ? [...reports.value]
    : reports.value.filter((item) => String(item.status || '').toUpperCase() === activeFilter.value)

  if (!highlightedReportId.value) return baseReports

  return baseReports.sort((left, right) => {
    if (left.id === highlightedReportId.value) return -1
    if (right.id === highlightedReportId.value) return 1
    return 0
  })
})

const scrollToHighlightedReport = async () => {
  if (!highlightedReportId.value) return

  await nextTick()
  const target = document.getElementById(`report-card-${highlightedReportId.value}`)
  target?.scrollIntoView({ behavior: 'smooth', block: 'center' })
}

const loadReports = async () => {
  if (!canReviewReports.value) return

  loading.value = true

  try {
    const response = await fetchCurrentUserOverview()
    reports.value = Array.isArray(response?.reports) ? response.reports : []
    await scrollToHighlightedReport()
  } catch (error) {
    console.error('加载举报审核数据失败', error)
    ElMessage.error(error.message || '加载举报审核数据失败，请稍后重试。')
  } finally {
    loading.value = false
  }
}

const isPending = (report) => String(report?.status || '').toUpperCase() === 'PENDING'
const isHighlightedReport = (reportId) => highlightedReportId.value === reportId

const openReviewDialog = (report, status) => {
  reviewDialog.reportId = report.id
  reviewDialog.status = status
  reviewDialog.reviewerNote = report.reviewerNote || ''
  reviewDialog.visible = true
}

const submitReview = async () => {
  if (!reviewDialog.reportId) return

  reviewDialog.loading = true

  try {
    await reviewForumReport(reviewDialog.reportId, {
      status: reviewDialog.status,
      reviewerNote: reviewDialog.reviewerNote.trim(),
    })
    reviewDialog.visible = false
    reviewDialog.reportId = null
    reviewDialog.reviewerNote = ''
    ElMessage.success('举报处理结果已提交')
    await loadReports()
  } catch (error) {
    console.error('提交举报审核失败', error)
    ElMessage.error(error.message || '提交举报审核失败，请稍后重试。')
  } finally {
    reviewDialog.loading = false
  }
}

const openReportTarget = (report) => {
  if (!report?.targetId) return

  if (report.targetType === 'forum-post') {
    router.push(`/forum/${report.targetId}`)
    return
  }

  if (report.targetType === 'article') {
    router.push(`/article/${report.targetId}`)
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

onMounted(() => {
  if (highlightedReportId.value) {
    activeFilter.value = 'ALL'
  }
  loadReports()
})

watch(
  () => route.query.reportId,
  async (value) => {
    if (!value) return
    activeFilter.value = 'ALL'
    await scrollToHighlightedReport()
  },
)
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 18px;
}

.toolbar-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.report-list {
  display: grid;
  gap: 14px;
  margin-top: 18px;
}

.report-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.9);
}

.report-card.is-highlighted {
  border-color: rgba(245, 158, 11, 0.55);
  box-shadow: 0 0 0 3px rgba(251, 191, 36, 0.16);
}

.report-main {
  min-width: 0;
}

.report-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.report-main strong {
  color: #0f172a;
}

.report-meta,
.report-detail,
.review-note {
  margin: 10px 0 0;
  color: #64748b;
  line-height: 1.7;
}

.report-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .report-card {
    flex-direction: column;
  }

  .toolbar-row {
    justify-content: flex-start;
  }

  .report-actions {
    width: 100%;
    align-items: stretch;
  }
}
</style>

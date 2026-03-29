<template>
  <workspace-page-shell
    :title="pageTitle"
    :description="pageDescription"
    :loading="loading"
    @refresh="loadNotifications"
  >
    <section class="toolbar-row">
      <div class="toolbar-filters">
        <el-radio-group v-model="activeFilter" size="small">
          <el-radio-button label="all">全部通知</el-radio-button>
          <el-radio-button label="unread">仅看未读</el-radio-button>
        </el-radio-group>

        <el-select v-model="typeFilter" size="small" class="type-select">
          <el-option label="全部类型" value="ALL" />
          <el-option v-for="item in notificationTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </div>

      <div class="toolbar-actions">
        <el-tag type="warning" effect="plain">未读 {{ unreadCount }}</el-tag>
        <el-button text type="primary" :disabled="!unreadCount || batchLoading" :loading="batchLoading" @click="markAllNotificationsRead">
          全部标为已读
        </el-button>
      </div>
    </section>

    <div v-if="filteredNotifications.length" class="notification-list">
      <article v-for="notification in filteredNotifications" :key="notification.id" class="notification-card">
        <div class="notification-main">
          <div class="notification-meta">
            <strong>{{ notification.title }}</strong>
            <el-tag effect="plain">{{ resolveNotificationTypeLabel(notification.type) }}</el-tag>
            <el-tag :type="notification.read ? 'info' : 'warning'" effect="plain">
              {{ notification.read ? '已读' : '未读' }}
            </el-tag>
          </div>
          <p>{{ notification.content }}</p>
          <small>{{ formatManagementTime(notification.createTime) }}</small>
        </div>

        <div class="notification-actions">
          <el-button v-if="!notification.read" text type="primary" @click="markNotificationRead(notification)">
            标记已读
          </el-button>
          <el-button text @click="openNotificationTarget(notification)">查看详情</el-button>
        </div>
      </article>
    </div>

    <el-empty v-else :image-size="84" :description="emptyDescription" />
  </workspace-page-shell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchCurrentUserOverview, markCurrentUserNotificationRead } from '@/api/user'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'
import { formatManagementTime } from '@/utils/profileManagement'
import { useUserStore } from '@/store/user'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'

const props = defineProps({
  workspaceMode: {
    type: String,
    default: 'user',
    validator: (value) => ['user', 'admin'].includes(value),
  },
})

const router = useRouter()
const userStore = useUserStore()
const isAdminWorkspace = computed(() => props.workspaceMode === 'admin')

const loading = ref(false)
const activeFilter = ref('all')
const typeFilter = ref('ALL')
const batchLoading = ref(false)
const notifications = ref([])

const unreadCount = computed(() => notifications.value.filter((item) => !item.read).length)
const pageTitle = computed(() => (isAdminWorkspace.value ? '后台通知中心' : '通知中心'))
const pageDescription = computed(() =>
  isAdminWorkspace.value
    ? '集中查看站内通知、审核提醒，并在后台直接处理未读项。'
    : '集中查看站内通知，并对未读提醒执行已读处理。',
)
const notificationTypeOptions = computed(() => {
  const types = [...new Set(notifications.value.map((item) => String(item.type || '').toUpperCase()).filter(Boolean))]
  return types.map((item) => ({
    value: item,
    label: resolveNotificationTypeLabel(item),
  }))
})
const filteredNotifications = computed(() => {
  return notifications.value.filter((item) => {
    const matchesReadState = activeFilter.value !== 'unread' || !item.read
    const matchesType = typeFilter.value === 'ALL' || String(item.type || '').toUpperCase() === typeFilter.value
    return matchesReadState && matchesType
  })
})
const emptyDescription = computed(() => {
  if (activeFilter.value === 'unread' && typeFilter.value === 'ALL') return '暂无未读通知'
  if (filteredNotifications.value.length === 0 && typeFilter.value !== 'ALL') return '当前类型下暂无通知'
  return '暂无通知数据'
})

const resolveNotificationTypeLabel = (type = '') => {
  const normalized = String(type || '').toUpperCase()

  if (normalized === 'REPORT_CREATED') return '新举报提醒'
  if (normalized === 'REPORT_REVIEWED') return '举报处理结果'
  return normalized || '系统通知'
}

const loadNotifications = async () => {
  loading.value = true

  try {
    const response = await fetchCurrentUserOverview()
    notifications.value = Array.isArray(response?.notifications) ? response.notifications : []
  } catch (error) {
    console.error('加载通知中心失败', error)
    ElMessage.error(error.message || '加载通知中心失败，请稍后重试。')
  } finally {
    loading.value = false
  }
}

const markNotificationRead = async (notification) => {
  if (!notification?.id || notification.read) return

  try {
    await markCurrentUserNotificationRead(notification.id)
    notification.read = true
    ElMessage.success('通知已标记为已读')
  } catch (error) {
    console.error('标记通知失败', error)
    ElMessage.error(error.message || '标记通知失败，请稍后重试。')
  }
}

const markAllNotificationsRead = async () => {
  const unreadNotifications = notifications.value.filter((item) => !item.read && item.id)
  if (!unreadNotifications.length) return

  batchLoading.value = true

  try {
    await Promise.all(unreadNotifications.map((item) => markCurrentUserNotificationRead(item.id)))
    unreadNotifications.forEach((item) => {
      item.read = true
    })
    ElMessage.success('全部未读通知已标记为已读')
  } catch (error) {
    console.error('批量标记通知失败', error)
    ElMessage.error(error.message || '批量标记通知失败，请稍后重试。')
  } finally {
    batchLoading.value = false
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
    if (isAdminWorkspace.value && userStore.hasCapability(WORKSPACE_CAPABILITIES.REPORT_REVIEW)) {
      router.push({
        path: '/admin/reportmgmt',
        query: { reportId: String(notification.relatedReportId) },
      })
      return
    }

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

onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-top: 18px;
}

.toolbar-filters,
.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.type-select {
  width: 168px;
}

.notification-list {
  display: grid;
  gap: 14px;
  margin-top: 18px;
}

.notification-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.9);
}

.notification-main {
  min-width: 0;
}

.notification-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.notification-main strong {
  color: #0f172a;
}

.notification-main p {
  margin: 10px 0 0;
  color: #64748b;
  line-height: 1.7;
}

.notification-main small {
  display: inline-block;
  margin-top: 12px;
  color: #64748b;
}

.notification-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

@media (max-width: 720px) {
  .toolbar-row,
  .notification-card {
    flex-direction: column;
  }

  .toolbar-filters,
  .toolbar-actions {
    width: 100%;
  }

  .type-select {
    width: 100%;
  }

  .notification-actions {
    width: 100%;
    align-items: stretch;
  }
}
</style>

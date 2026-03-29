<template>
  <workspace-page-shell
    title="公告管理"
    description="集中维护站点公告，论坛页会同步展示这里发布的内容。"
    :loading="loading"
    @refresh="loadOverview"
  >
    <template v-if="!canManageSite">
      <workspace-permission-notice message="当前账号没有公告管理权限。" />
    </template>
    <template v-else>
      <site-overview-stats
        :site-name="siteInfo.name"
        :description="siteInfo.description"
        :announcement-count="announcements.length"
      />

      <announcement-management-card
        :announcements="announcements"
        @open-dialog="announcementDialog.visible = true"
        @remove="removeAnnouncement"
      />

      <announcement-editor-dialog
        :visible="announcementDialog.visible"
        :loading="announcementDialog.loading"
        :form="announcementDialog.form"
        @update:visible="announcementDialog.visible = $event"
        @update:title="announcementDialog.form.title = $event"
        @update:content="announcementDialog.form.content = $event"
        @submit="submitAnnouncement"
      />
    </template>
  </workspace-page-shell>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminAnnouncement,
  deleteAdminAnnouncement,
  fetchAdminAnnouncements,
  fetchAdminSiteInfo,
} from '@/api/admin/site'
import AnnouncementEditorDialog from '@/components/profile/workspace/AnnouncementEditorDialog.vue'
import AnnouncementManagementCard from '@/components/profile/workspace/AnnouncementManagementCard.vue'
import SiteOverviewStats from '@/components/profile/workspace/SiteOverviewStats.vue'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'
import WorkspacePermissionNotice from '@/components/profile/workspace/WorkspacePermissionNotice.vue'
import { useUserStore } from '@/store/user'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const canManageSite = computed(() => userStore.hasCapability(WORKSPACE_CAPABILITIES.SITE_MANAGE))
const loading = ref(false)
const announcements = ref([])
const siteInfo = reactive({
  name: '',
  description: '',
})
const announcementDialog = reactive({
  visible: false,
  loading: false,
  form: { title: '', content: '' },
})

const resetAnnouncementForm = () => {
  announcementDialog.form = { title: '', content: '' }
}

const openAnnouncementDialog = () => {
  announcementDialog.visible = true
}

const clearComposeAnnouncementQuery = async () => {
  if (route.query.composeAnnouncement === undefined) return

  const nextQuery = { ...route.query }
  delete nextQuery.composeAnnouncement
  await router.replace({ query: nextQuery })
}

const syncAnnouncementDialogFromRoute = async (value) => {
  if (value !== '1') return

  openAnnouncementDialog()
  await clearComposeAnnouncementQuery()
}

const loadSiteInfo = async () => {
  if (!canManageSite.value) return

  const response = await fetchAdminSiteInfo()
  siteInfo.name = response?.name || ''
  siteInfo.description = response?.description || ''
}

const loadAnnouncements = async () => {
  if (!canManageSite.value) return

  announcements.value = (await fetchAdminAnnouncements()) || []
}

const loadOverview = async () => {
  if (!canManageSite.value) return

  loading.value = true

  try {
    await Promise.all([loadSiteInfo(), loadAnnouncements()])
  } finally {
    loading.value = false
  }
}

const submitAnnouncement = async () => {
  if (!announcementDialog.form.title.trim() || !announcementDialog.form.content.trim()) {
    ElMessage.warning('请填写完整公告内容')
    return
  }

  announcementDialog.loading = true

  try {
    await createAdminAnnouncement({
      title: announcementDialog.form.title.trim(),
      content: announcementDialog.form.content.trim(),
      status: 1,
    })

    announcementDialog.visible = false
    resetAnnouncementForm()
    ElMessage.success('公告发布成功')
    await loadAnnouncements()
  } finally {
    announcementDialog.loading = false
  }
}

const removeAnnouncement = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除公告「${row.title}」吗？`, '删除公告', { type: 'warning' })
    await deleteAdminAnnouncement(row.id)
    ElMessage.success('公告已删除')
    await loadAnnouncements()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

watch(
  () => route.query.composeAnnouncement,
  (value) => {
    syncAnnouncementDialogFromRoute(value)
  },
  { immediate: true },
)

onMounted(() => {
  loadOverview()
})
</script>

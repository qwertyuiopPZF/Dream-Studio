<template>
  <workspace-page-shell title="站点概览" description="管理员可以维护站点信息，并管理论坛页展示的公告。" :loading="loading" @refresh="loadOverview">
    <template v-if="!isAdmin">
      <workspace-permission-notice message="当前账号没有站点配置权限。" />
    </template>
    <template v-else>
      <site-overview-stats
        :site-name="siteForm.name"
        :description="siteForm.description"
        :announcement-count="announcements.length"
      />

      <site-info-settings-card
        :form="siteForm"
        :saving="siteSaving"
        @update:field="handleFieldUpdate"
        @submit="submitSiteInfo"
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminAnnouncement,
  deleteAdminAnnouncement,
  fetchAdminAnnouncements,
  fetchAdminSiteInfo,
  updateAdminSiteInfo,
} from '@/api/admin'
import { useUserStore } from '@/store/user'
import AnnouncementEditorDialog from '@/components/profile/workspace/AnnouncementEditorDialog.vue'
import AnnouncementManagementCard from '@/components/profile/workspace/AnnouncementManagementCard.vue'
import SiteInfoSettingsCard from '@/components/profile/workspace/SiteInfoSettingsCard.vue'
import SiteOverviewStats from '@/components/profile/workspace/SiteOverviewStats.vue'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'
import WorkspacePermissionNotice from '@/components/profile/workspace/WorkspacePermissionNotice.vue'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)
const loading = ref(false)
const siteSaving = ref(false)
const announcements = ref([])
const siteForm = reactive({
  name: '',
  description: '',
  keywords: '',
  author: '',
  icp: '',
})
const announcementDialog = reactive({
  visible: false,
  loading: false,
  form: { title: '', content: '' },
})

const loadSiteInfo = async () => {
  if (!isAdmin.value) return

  const response = await fetchAdminSiteInfo()
  siteForm.name = response?.name || ''
  siteForm.description = response?.description || ''
  siteForm.keywords = response?.keywords || ''
  siteForm.author = response?.author || ''
  siteForm.icp = response?.icp || ''
}

const loadAnnouncements = async () => {
  if (!isAdmin.value) return

  announcements.value = (await fetchAdminAnnouncements()) || []
}

const loadOverview = async () => {
  if (!isAdmin.value) return

  loading.value = true

  try {
    await Promise.all([loadSiteInfo(), loadAnnouncements()])
  } finally {
    loading.value = false
  }
}

const handleFieldUpdate = ({ field, value }) => {
  siteForm[field] = value
}

const submitSiteInfo = async () => {
  siteSaving.value = true

  try {
    await updateAdminSiteInfo({ ...siteForm })
    ElMessage.success('站点信息已更新')
  } finally {
    siteSaving.value = false
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
    announcementDialog.form = { title: '', content: '' }
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

onMounted(() => {
  loadOverview()
})
</script>

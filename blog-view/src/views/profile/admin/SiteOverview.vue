<template>
  <workspace-page-shell title="站点设置" description="管理员可以维护站点基础信息和页面配置。" :loading="loading" @refresh="loadOverview">
    <template v-if="!canManageSite">
      <workspace-permission-notice message="当前账号没有站点配置权限。" />
    </template>
    <template v-else>
      <site-info-settings-card
        :form="siteForm"
        :saving="siteSaving"
        @update:field="handleFieldUpdate"
        @submit="submitSiteInfo"
      />
    </template>
  </workspace-page-shell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchAdminSiteInfo, updateAdminSiteInfo } from '@/api/admin/site'
import { useUserStore } from '@/store/user'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'
import SiteInfoSettingsCard from '@/components/profile/workspace/SiteInfoSettingsCard.vue'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'
import WorkspacePermissionNotice from '@/components/profile/workspace/WorkspacePermissionNotice.vue'

const userStore = useUserStore()
const canManageSite = computed(() => userStore.hasCapability(WORKSPACE_CAPABILITIES.SITE_MANAGE))
const loading = ref(false)
const siteSaving = ref(false)
const siteForm = reactive({
  name: '',
  description: '',
  keywords: '',
  author: '',
  icp: '',
})

const loadSiteInfo = async () => {
  if (!canManageSite.value) return

  const response = await fetchAdminSiteInfo()
  siteForm.name = response?.name || ''
  siteForm.description = response?.description || ''
  siteForm.keywords = response?.keywords || ''
  siteForm.author = response?.author || ''
  siteForm.icp = response?.icp || ''
}

const loadOverview = async () => {
  if (!canManageSite.value) return

  loading.value = true

  try {
    await loadSiteInfo()
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

onMounted(() => {
  loadOverview()
})
</script>

<template>
  <workspace-page-shell title="标签管理" description="管理员可以集中维护站点标签。" :loading="loading" @refresh="loadTags">
    <template v-if="!isAdmin">
      <workspace-permission-notice message="当前账号没有标签管理权限。" />
    </template>
    <template v-else>
      <div class="toolbar">
        <el-button type="primary" plain @click="openDialog()">新增标签</el-button>
      </div>

      <el-table :data="tags" table-layout="fixed">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column label="标签名称">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="articleCount" label="关联文章数" width="120" />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatManagementTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button text type="danger" @click="removeTag(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <name-editor-dialog
        :visible="dialog.visible"
        :loading="dialog.loading"
        :form="dialog.form"
        field-label="标签名称"
        create-title="新增标签"
        edit-title="编辑标签"
        @update:visible="dialog.visible = $event"
        @update:name="dialog.form.name = $event"
        @submit="submitTag"
      />
    </template>
  </workspace-page-shell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminTag,
  deleteAdminTag,
  fetchAdminTags,
  updateAdminTag,
} from '@/api/admin'
import { useUserStore } from '@/store/user'
import { formatManagementTime } from '@/utils/profileManagement'
import NameEditorDialog from '@/components/profile/workspace/NameEditorDialog.vue'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'
import WorkspacePermissionNotice from '@/components/profile/workspace/WorkspacePermissionNotice.vue'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)
const loading = ref(false)
const tags = ref([])
const dialog = reactive({
  visible: false,
  loading: false,
  form: { id: null, name: '' },
})

const loadTags = async () => {
  if (!isAdmin.value) return

  loading.value = true

  try {
    tags.value = (await fetchAdminTags()) || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row = null) => {
  dialog.form = row ? { id: row.id, name: row.name } : { id: null, name: '' }
  dialog.visible = true
}

const submitTag = async () => {
  if (!dialog.form.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }

  dialog.loading = true

  try {
    const payload = { ...dialog.form, name: dialog.form.name.trim() }
    if (payload.id) {
      await updateAdminTag(payload)
    } else {
      await createAdminTag(payload)
    }

    dialog.visible = false
    await loadTags()
  } finally {
    dialog.loading = false
  }
}

const removeTag = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除标签「${row.name}」吗？`, '删除标签', { type: 'warning' })
    await deleteAdminTag(row.id)
    await loadTags()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

onMounted(() => {
  loadTags()
})
</script>

<style scoped>
.toolbar {
  margin-top: 18px;
}
</style>

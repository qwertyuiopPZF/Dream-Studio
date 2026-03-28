<template>
  <workspace-page-shell title="分类管理" description="管理员可以集中维护站点分类。" :loading="loading" @refresh="loadCategories">
    <template v-if="!isAdmin">
      <workspace-permission-notice message="当前账号没有分类管理权限。" />
    </template>
    <template v-else>
      <div class="toolbar">
        <el-button type="primary" plain @click="openDialog()">新增分类</el-button>
      </div>

      <el-table :data="categories" table-layout="fixed">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="articleCount" label="关联文章数" width="120" />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatManagementTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button text type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button text type="danger" @click="removeCategory(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <name-editor-dialog
        :visible="dialog.visible"
        :loading="dialog.loading"
        :form="dialog.form"
        field-label="分类名称"
        create-title="新增分类"
        edit-title="编辑分类"
        @update:visible="dialog.visible = $event"
        @update:name="dialog.form.name = $event"
        @submit="submitCategory"
      />
    </template>
  </workspace-page-shell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminCategory,
  deleteAdminCategory,
  fetchAdminCategories,
  updateAdminCategory,
} from '@/api/admin'
import { useUserStore } from '@/store/user'
import { formatManagementTime } from '@/utils/profileManagement'
import NameEditorDialog from '@/components/profile/workspace/NameEditorDialog.vue'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'
import WorkspacePermissionNotice from '@/components/profile/workspace/WorkspacePermissionNotice.vue'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)
const loading = ref(false)
const categories = ref([])
const dialog = reactive({
  visible: false,
  loading: false,
  form: { id: null, name: '' },
})

const loadCategories = async () => {
  if (!isAdmin.value) return

  loading.value = true

  try {
    categories.value = (await fetchAdminCategories()) || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row = null) => {
  dialog.form = row ? { id: row.id, name: row.name } : { id: null, name: '' }
  dialog.visible = true
}

const submitCategory = async () => {
  if (!dialog.form.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }

  dialog.loading = true

  try {
    const payload = { ...dialog.form, name: dialog.form.name.trim() }
    if (payload.id) {
      await updateAdminCategory(payload)
    } else {
      await createAdminCategory(payload)
    }

    dialog.visible = false
    await loadCategories()
  } finally {
    dialog.loading = false
  }
}

const removeCategory = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '删除分类', { type: 'warning' })
    await deleteAdminCategory(row.id)
    await loadCategories()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.toolbar {
  margin-top: 18px;
}
</style>

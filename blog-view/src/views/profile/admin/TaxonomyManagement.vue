<template>
  <workspace-page-shell
    :title="taxonomyConfig.title"
    :description="taxonomyConfig.description"
    :loading="loading"
    @refresh="loadItems"
  >
    <template v-if="!canManageTaxonomy">
      <workspace-permission-notice :message="taxonomyConfig.permissionMessage" />
    </template>
    <template v-else>
      <div class="toolbar">
        <el-button type="primary" plain @click="openDialog()">{{ taxonomyConfig.createButtonText }}</el-button>
      </div>

        <el-table :data="items" table-layout="fixed">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column :label="taxonomyConfig.nameColumnLabel">
          <template #default="{ row }">
            <el-tag v-if="taxonomyConfig.displayAsTag" effect="plain">{{ row.name }}</el-tag>
            <span v-else>{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="articleCount" label="关联文章数" width="120" />
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">{{ formatManagementTime(row.createTime) }}</template>
        </el-table-column>
          <el-table-column label="操作" width="178">
            <template #default="{ row }">
              <div class="table-action-group">
                <el-button class="table-action-button is-neutral" size="small" @click="openDialog(row)">
                  <el-icon><EditPen /></el-icon>
                  <span>编辑</span>
                </el-button>
                <el-button class="table-action-button is-danger" size="small" @click="removeItem(row)">
                  <el-icon><Delete /></el-icon>
                  <span>删除</span>
                </el-button>
              </div>
            </template>
          </el-table-column>
      </el-table>

      <name-editor-dialog
        :visible="dialog.visible"
        :loading="dialog.loading"
        :form="dialog.form"
        :field-label="taxonomyConfig.fieldLabel"
        :create-title="taxonomyConfig.createTitle"
        :edit-title="taxonomyConfig.editTitle"
        @update:visible="dialog.visible = $event"
        @update:name="dialog.form.name = $event"
        @submit="submitItem"
      />
    </template>
  </workspace-page-shell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, EditPen } from '@element-plus/icons-vue'
import {
  createAdminCategory,
  createAdminTag,
  deleteAdminCategory,
  deleteAdminTag,
  fetchAdminCategories,
  fetchAdminTags,
  updateAdminCategory,
  updateAdminTag,
} from '@/api/admin'
import NameEditorDialog from '@/components/profile/workspace/NameEditorDialog.vue'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'
import WorkspacePermissionNotice from '@/components/profile/workspace/WorkspacePermissionNotice.vue'
import { useUserStore } from '@/store/user'
import { formatManagementTime } from '@/utils/profileManagement'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'

const props = defineProps({
  mode: {
    type: String,
    required: true,
    validator: (value) => ['category', 'tag'].includes(value),
  },
})

const TAXONOMY_CONFIGS = {
  category: {
    title: '分类管理',
    description: '管理员可以集中维护站点分类。',
    permissionMessage: '当前账号没有分类管理权限。',
    createButtonText: '新增分类',
    nameColumnLabel: '分类名称',
    fieldLabel: '分类名称',
    createTitle: '新增分类',
    editTitle: '编辑分类',
    deleteTitle: '删除分类',
    deleteConfirmText: (name) => `确定删除分类「${name}」吗？`,
    emptyNameMessage: '请输入分类名称',
    displayAsTag: false,
    fetchItems: fetchAdminCategories,
    createItem: createAdminCategory,
    updateItem: updateAdminCategory,
    deleteItem: deleteAdminCategory,
  },
  tag: {
    title: '标签管理',
    description: '管理员可以集中维护站点标签。',
    permissionMessage: '当前账号没有标签管理权限。',
    createButtonText: '新增标签',
    nameColumnLabel: '标签名称',
    fieldLabel: '标签名称',
    createTitle: '新增标签',
    editTitle: '编辑标签',
    deleteTitle: '删除标签',
    deleteConfirmText: (name) => `确定删除标签「${name}」吗？`,
    emptyNameMessage: '请输入标签名称',
    displayAsTag: true,
    fetchItems: fetchAdminTags,
    createItem: createAdminTag,
    updateItem: updateAdminTag,
    deleteItem: deleteAdminTag,
  },
}

const userStore = useUserStore()
const canManageTaxonomy = computed(() =>
  userStore.hasCapability(WORKSPACE_CAPABILITIES.TAXONOMY_MANAGE),
)
const taxonomyConfig = computed(() => TAXONOMY_CONFIGS[props.mode])
const loading = ref(false)
const items = ref([])
const dialog = reactive({
  visible: false,
  loading: false,
  form: { id: null, name: '' },
})

const loadItems = async () => {
  if (!canManageTaxonomy.value) return

  loading.value = true

  try {
    items.value = (await taxonomyConfig.value.fetchItems()) || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row = null) => {
  dialog.form = row ? { id: row.id, name: row.name } : { id: null, name: '' }
  dialog.visible = true
}

const submitItem = async () => {
  if (!dialog.form.name.trim()) {
    ElMessage.warning(taxonomyConfig.value.emptyNameMessage)
    return
  }

  dialog.loading = true

  try {
    const payload = { ...dialog.form, name: dialog.form.name.trim() }
    if (payload.id) {
      await taxonomyConfig.value.updateItem(payload)
    } else {
      await taxonomyConfig.value.createItem(payload)
    }

    dialog.visible = false
    await loadItems()
  } finally {
    dialog.loading = false
  }
}

const removeItem = async (row) => {
  try {
    await ElMessageBox.confirm(
      taxonomyConfig.value.deleteConfirmText(row.name),
      taxonomyConfig.value.deleteTitle,
      { type: 'warning' },
    )
    await taxonomyConfig.value.deleteItem(row.id)
    await loadItems()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.toolbar {
  margin-top: 18px;
}
</style>

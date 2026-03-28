<template>
  <management-section-shell
    :bordered="bordered"
    :show-header="showHeader"
    :loading="state.loading"
    @refresh="loadArticles"
  >
    <div class="tab-toolbar">
      <el-select
        v-model="state.categoryId"
        clearable
        filterable
        placeholder="按分类筛选"
        class="toolbar-select"
        @change="handleFilterChange"
      >
        <el-option
          v-for="category in categories"
          :key="category.id"
          :label="category.name"
          :value="category.id"
        />
      </el-select>
      <el-button text @click="resetFilter">清空筛选</el-button>
    </div>

    <div v-if="state.error" class="tab-error">{{ state.error }}</div>

    <div v-loading="state.loading" class="tab-table-wrap">
      <el-table v-if="state.items.length" :data="state.items" table-layout="fixed" size="small" class="management-table">
        <el-table-column label="文章信息" min-width="280">
          <template #default="{ row }">
            <div class="article-cell">
              <span class="article-title" :title="row.title">{{ row.title || '--' }}</span>
              <div class="article-meta-inline">
                <el-tag size="small" effect="plain">{{ row.categoryName || '未分类' }}</el-tag>
                <template v-if="getCondensedTags(row.tags).length">
                  <el-tag
                    v-for="tag in getCondensedTags(row.tags)"
                    :key="`${row.id}-${tag}`"
                    size="small"
                    effect="plain"
                  >
                    {{ tag }}
                  </el-tag>
                </template>
                <span v-else class="muted-text">无标签</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="发布人" min-width="140">
          <template #default="{ row }">
            <div class="publisher-cell">
              <el-avatar :size="30" :src="getPublisherAvatar(row)">{{ getPublisherInitial(row) }}</el-avatar>
              <span class="publisher-name">{{ getPublisherName(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="74" />
        <el-table-column label="公开" width="84">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :loading="Boolean(row.statusLoading)"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="时间" width="168">
          <template #default="{ row }">
            <div class="time-cell">
              <span>发: {{ formatManagementTime(row.publishTime) }}</span>
              <span>建: {{ formatManagementTime(row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="108">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button text type="primary" @click="goToEditor(row.id)">编辑</el-button>
              <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else :image-size="84" description="暂无文章数据" />
    </div>

    <div v-if="state.pagination.total > 0" class="tab-pagination">
      <el-pagination
        v-model:current-page="state.pagination.page"
        v-model:page-size="state.pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="state.pagination.total"
        layout="total, prev, pager, next"
        @current-change="loadArticles"
      />
    </div>
  </management-section-shell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import defaultAvatar from '@/assets/(5).png'
import {
  deleteAdminArticle,
  fetchAdminArticles,
  updateAdminArticle,
} from '@/api/admin'
import { fetchCategories } from '@/api/categories'
import ManagementSectionShell from '@/components/profile/management/ManagementSectionShell.vue'
import { useWorkspaceRouteBase } from '@/composables/useWorkspaceRouteBase'
import {
  formatManagementTags,
  formatManagementTime,
} from '@/utils/profileManagement'

defineProps({
  showHeader: {
    type: Boolean,
    default: true,
  },
  bordered: {
    type: Boolean,
    default: true,
  },
})

const router = useRouter()
const categories = ref([])
const { routeBase } = useWorkspaceRouteBase()

const state = reactive({
  items: [],
  loading: false,
  error: '',
  categoryId: null,
  pagination: {
    page: 1,
    size: 10,
    total: 0,
  },
})

const ensureCategories = async () => {
  if (categories.value.length) return

  try {
    categories.value = await fetchCategories()
  } catch (error) {
    console.error('加载文章分类失败', error)
  }
}

const loadArticles = async () => {
  state.loading = true
  state.error = ''

  try {
    await ensureCategories()
    const data = await fetchAdminArticles({
      page: state.pagination.page,
      size: state.pagination.size,
      categoryId: state.categoryId || undefined,
    })

    state.items = (data?.data || []).map((item) => ({
      ...item,
      statusLoading: false,
    }))
    state.pagination.total = Number(data?.pagination?.total || 0)
  } catch (error) {
    console.error('加载文章管理数据失败', error)
    state.error = error.message || '加载文章管理数据失败，请稍后重试。'
  } finally {
    state.loading = false
  }
}

const handleFilterChange = () => {
  state.pagination.page = 1
  loadArticles()
}

const resetFilter = () => {
  if (!state.categoryId) return

  state.categoryId = null
  state.pagination.page = 1
  loadArticles()
}

const getPublisherName = (row) => row.authorNickname || '匿名用户'

const getPublisherAvatar = (row) => row.authorAvatar || defaultAvatar

const getPublisherInitial = (row) => getPublisherName(row).slice(0, 1).toUpperCase()

const getCondensedTags = (tags) => formatManagementTags(tags).slice(0, 2)

const goToEditor = (id) => {
  router.push(`${routeBase.value}/article/edit/${id}`)
}

const handleStatusChange = async (row) => {
  const previousStatus = row.status === 1 ? 0 : 1
  row.statusLoading = true

  try {
    await updateAdminArticle(row.id, { status: row.status })
    ElMessage.success('文章状态已更新')
  } catch (error) {
    row.status = previousStatus
    console.error('更新文章状态失败', error)
  } finally {
    row.statusLoading = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除文章《${row.title}》吗？`, '删除文章', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })

    await deleteAdminArticle(row.id)

    if (state.items.length === 1 && state.pagination.page > 1) {
      state.pagination.page -= 1
    }

    ElMessage.success('文章已删除')
    await loadArticles()
  } catch (error) {
    if (error === 'cancel') return
    console.error('删除文章失败', error)
  }
}

onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.tab-toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.toolbar-select {
  width: 220px;
}

.tab-table-wrap {
  min-height: 160px;
  width: 100%;
}

.tab-pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.tab-error {
  margin-bottom: 14px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(239, 68, 68, 0.08);
  color: #b42318;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

:deep(.management-table .el-table__cell) {
  padding: 10px 0;
}

:deep(.management-table .cell) {
  padding-left: 8px;
  padding-right: 8px;
}

.muted-text {
  color: #6b7280;
}

.article-cell,
.time-cell {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.article-title {
  overflow: hidden;
  color: #0f172a;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-meta-inline {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
  flex-wrap: wrap;
}

.time-cell {
  color: #64748b;
  line-height: 1.35;
}

.publisher-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.publisher-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 720px) {
  .toolbar-select {
    width: 100%;
  }
}
</style>

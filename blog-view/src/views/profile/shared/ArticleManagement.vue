<template>
  <management-page-shell>
    <section class="management-panel">
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
              <div class="article-info-cell">
                <div v-if="row.coverImage" class="article-cover-thumb">
                  <img :src="resolveCoverImage(row.coverImage)" :alt="row.title || '文章封面'" />
                </div>
                <div v-else class="article-cover-thumb is-empty">
                  <span>{{ getArticleInitial(row) }}</span>
                </div>

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
          <el-table-column v-if="canManageFeaturedArticles" label="精选" width="92">
            <template #default="{ row }">
              <el-switch
                v-model="row.isFeatured"
                :loading="Boolean(row.featuredLoading)"
                @change="handleFeaturedChange(row)"
              />
            </template>
          </el-table-column>
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
          <el-table-column label="操作" width="170">
            <template #default="{ row }">
              <div class="table-action-group">
                <el-button class="table-action-button is-neutral" size="small" @click="goToEditor(row.id)">
                  <el-icon><EditPen /></el-icon>
                  <span>编辑</span>
                </el-button>
                <el-button class="table-action-button is-danger" size="small" @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                  <span>删除</span>
                </el-button>
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
    </section>
  </management-page-shell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, EditPen } from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/(5).png'
import {
  deleteManagedArticle,
  fetchManagedArticleById,
  fetchManagedArticles,
  updateManagedArticle,
} from '@/api/article'
import { fetchCategories } from '@/api/categories'
import ManagementPageShell from '@/components/profile/management/ManagementPageShell.vue'
import { useWorkspaceRouteBase } from '@/composables/useWorkspaceRouteBase'
import { useUserStore } from '@/store/user'
import {
  formatManagementTags,
  formatManagementTime,
} from '@/utils/profileManagement'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'

const router = useRouter()
const categories = ref([])
const { routeBase } = useWorkspaceRouteBase()
const userStore = useUserStore()
const uploadBaseUrl = import.meta.env.VITE_APP_UPLOAD_URL || ''

const canManageFeaturedArticles = computed(() =>
  userStore.hasCapability(WORKSPACE_CAPABILITIES.ARTICLE_MANAGE_ALL),
)

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
    const data = await fetchManagedArticles({
      page: state.pagination.page,
      size: state.pagination.size,
      categoryId: state.categoryId || undefined,
    })

    state.items = (data?.data || []).map((item) => ({
      ...item,
      isFeatured: Boolean(item.isFeatured),
      featuredLoading: false,
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

const getArticleInitial = (row) => (row.title || '文').slice(0, 1).toUpperCase()

const resolveCoverImage = (coverImage) => {
  if (!coverImage) return ''
  return coverImage.startsWith('http') ? coverImage : `${uploadBaseUrl}${coverImage}`
}

const getCondensedTags = (tags) => formatManagementTags(tags).slice(0, 2)

const normalizeArticleTags = (tags) => {
  if (Array.isArray(tags)) {
    return tags
      .map((tag) => String(tag).trim())
      .filter(Boolean)
      .join(',')
  }

  return typeof tags === 'string' ? tags.trim() : ''
}

const buildStatusUpdatePayload = async (row) => {
  if (row.status !== 1) {
    return { status: row.status }
  }

  const detail = await fetchManagedArticleById(row.id)
  const tags = normalizeArticleTags(detail?.tags)

  if (!detail?.content?.trim()) {
    ElMessage.warning('请先完善文章内容后再公开')
    throw new Error('missing-article-content')
  }

  if (detail?.categoryId == null) {
    ElMessage.warning('请先选择文章分类后再公开')
    throw new Error('missing-article-category')
  }

  if (!tags) {
    ElMessage.warning('请先选择文章标签后再公开')
    throw new Error('missing-article-tags')
  }

  return {
    title: detail.title || '',
    summary: detail.summary || '',
    content: detail.content || '',
    coverImage: detail.coverImage || '',
    categoryId: detail.categoryId,
    tags,
    isComment: detail.isComment ?? 1,
    status: row.status,
  }
}

const goToEditor = (id) => {
  router.push(`${routeBase.value}/article/edit/${id}`)
}

const handleStatusChange = async (row) => {
  const previousStatus = row.status === 1 ? 0 : 1
  row.statusLoading = true

  try {
    const payload = await buildStatusUpdatePayload(row)
    await updateManagedArticle(row.id, payload)
    ElMessage.success('文章状态已更新')
  } catch (error) {
    row.status = previousStatus
    if (!String(error?.message || '').startsWith('missing-article-')) {
      console.error('更新文章状态失败', error)
    }
  } finally {
    row.statusLoading = false
  }
}

const handleFeaturedChange = async (row) => {
  if (!canManageFeaturedArticles.value) {
    row.isFeatured = !row.isFeatured
    ElMessage.warning('只有管理员可以设置精选文章')
    return
  }

  const previousFeatured = !row.isFeatured

  if (row.isFeatured && row.status !== 1) {
    row.isFeatured = previousFeatured
    ElMessage.warning('请先公开文章，再设为精选文章')
    return
  }

  row.featuredLoading = true

  try {
    await updateManagedArticle(row.id, {
      isFeatured: row.isFeatured,
    })
    ElMessage.success(row.isFeatured ? '已设为精选文章，首页轮播会自动展示' : '已取消精选文章')
  } catch (error) {
    row.isFeatured = previousFeatured
    console.error('更新文章精选状态失败', error)
    ElMessage.error('更新精选状态失败，请稍后重试')
  } finally {
    row.featuredLoading = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除文章《${row.title}》吗？`, '删除文章', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })

    await deleteManagedArticle(row.id)

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
.management-panel {
  display: flex;
  flex-direction: column;
}

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

.article-info-cell {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.article-cover-thumb {
  width: 88px;
  height: 60px;
  border-radius: 14px;
  overflow: hidden;
  background: #e2e8f0;
  flex-shrink: 0;
  box-shadow: inset 0 0 0 1px rgba(148, 163, 184, 0.14);
}

.article-cover-thumb img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-cover-thumb.is-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e2ecf7 0%, #f8e4d6 100%);
  color: #475569;
  font-size: 1.15rem;
  font-weight: 700;
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

  .article-info-cell {
    gap: 10px;
  }

  .article-cover-thumb {
    width: 72px;
    height: 52px;
    border-radius: 12px;
  }
}
</style>

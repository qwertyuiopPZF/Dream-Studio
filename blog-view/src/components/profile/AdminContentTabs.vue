<template>
  <div class="admin-tabs-shell" :class="{ plain: !props.bordered }">
    <div v-if="props.showHeader" class="admin-tabs-head">
      <div>
        <div class="admin-tabs-title">具体管理页</div>
        <p>文章、评论、动态管理都直接收进个人中心标签页里处理。</p>
      </div>
      <el-button round :loading="currentLoading" @click="refreshCurrentTab">刷新当前标签</el-button>
    </div>

    <el-tabs v-model="activeTab" class="admin-tabs">
      <el-tab-pane label="文章管理" name="articles">
        <div class="tab-toolbar">
          <el-select
            v-model="articleState.categoryId"
            clearable
            filterable
            placeholder="按分类筛选"
            class="toolbar-select"
            @change="handleArticleFilterChange"
          >
            <el-option v-for="category in categories" :key="category.id" :label="category.name" :value="category.id" />
          </el-select>
          <el-button text @click="resetArticleFilter">清空筛选</el-button>
        </div>

        <div v-if="articleState.error" class="tab-error">{{ articleState.error }}</div>

        <div v-loading="articleState.loading" class="tab-table-wrap">
          <el-table v-if="articleState.items.length" :data="articleState.items" table-layout="fixed">
            <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
            <el-table-column prop="categoryName" label="分类" width="120">
              <template #default="{ row }">
                <el-tag effect="plain">{{ row.categoryName || '--' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="标签" min-width="180">
              <template #default="{ row }">
                <div v-if="formatTags(row.tags).length" class="tag-list">
                  <el-tag v-for="tag in formatTags(row.tags)" :key="`${row.id}-${tag}`" size="small" effect="plain">
                    {{ tag }}
                  </el-tag>
                </div>
                <span v-else class="muted-text">--</span>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="浏览量" width="90" />
            <el-table-column label="公开性" width="110">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  :active-value="1"
                  :inactive-value="0"
                  :loading="Boolean(row.statusLoading)"
                  @change="handleArticleStatusChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="150">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="发布时间" width="150">
              <template #default="{ row }">
                {{ formatTime(row.publishTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="190" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button text type="primary" @click="goToArticleEditor(row.id)">编辑</el-button>
                  <el-button text @click="goToArticle(row.id)">预览</el-button>
                  <el-button text type="danger" @click="handleDeleteArticle(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else :image-size="84" description="暂无文章数据" />
        </div>

        <div v-if="articleState.pagination.total > 0" class="tab-pagination">
          <el-pagination
            v-model:current-page="articleState.pagination.page"
            v-model:page-size="articleState.pagination.size"
            :page-sizes="[10, 20, 50]"
            :total="articleState.pagination.total"
            layout="total, prev, pager, next"
            @current-change="loadArticles"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="评论管理" name="comments">
        <div v-if="commentState.error" class="tab-error">{{ commentState.error }}</div>

        <div v-loading="commentState.loading" class="tab-table-wrap">
          <el-table v-if="commentState.items.length" :data="commentState.items" table-layout="fixed">
            <el-table-column prop="nickname" label="用户" width="110" />
            <el-table-column label="来源" min-width="170">
              <template #default="{ row }">
                <el-tag size="small" effect="plain">{{ commentTargetLabel(row) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="父评论" width="120">
              <template #default="{ row }">
                {{ row.parentNickname || row.parentCommentNickname || '--' }}
              </template>
            </el-table-column>
            <el-table-column prop="content" label="评论内容" min-width="260" show-overflow-tooltip />
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-switch v-model="row.status" :loading="Boolean(row.statusLoading)" @change="handleCommentStatusChange(row)" />
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="150">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="170" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button text type="primary" @click="goToCommentTarget(row)">定位</el-button>
                  <el-button text type="danger" @click="handleDeleteComment(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else :image-size="84" description="暂无评论数据" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="动态管理" name="moments">
        <div v-if="momentState.error" class="tab-error">{{ momentState.error }}</div>

        <div v-loading="momentState.loading" class="tab-table-wrap">
          <el-table v-if="momentState.items.length" :data="momentState.items" table-layout="fixed">
            <el-table-column prop="content" label="内容" min-width="280" show-overflow-tooltip />
            <el-table-column label="图片" width="140">
              <template #default="{ row }">
                <div v-if="parseMomentImages(row.image).length" class="moment-preview">
                  <el-image
                    :src="parseMomentImages(row.image)[0]"
                    :preview-src-list="parseMomentImages(row.image)"
                    fit="cover"
                    preview-teleported
                    class="moment-thumb"
                  />
                  <span class="moment-count">{{ parseMomentImages(row.image).length }} 张</span>
                </div>
                <span v-else class="muted-text">无图片</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="110">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">
                  {{ row.status === 1 ? '已发布' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="150">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button text type="primary" @click="goToMomentPage">查看</el-button>
                  <el-button text type="danger" @click="handleDeleteMoment(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else :image-size="84" description="暂无动态数据" />
        </div>

        <div v-if="momentState.pagination.total > 0" class="tab-pagination">
          <el-pagination
            v-model:current-page="momentState.pagination.page"
            v-model:page-size="momentState.pagination.size"
            :page-sizes="[10, 20, 50]"
            :total="momentState.pagination.total"
            layout="total, prev, pager, next"
            @current-change="loadMoments"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteAdminArticle,
  deleteAdminComment,
  deleteAdminMoment,
  fetchAdminArticles,
  fetchAdminComments,
  fetchAdminMoments,
  updateAdminArticle,
  updateAdminCommentStatus,
} from '@/api/admin'
import { fetchCategories } from '@/api/categories'

const props = defineProps({
  showHeader: {
    type: Boolean,
    default: true,
  },
  bordered: {
    type: Boolean,
    default: true,
  },
  initialTab: {
    type: String,
    default: 'articles',
  },
})

const router = useRouter()
const activeTab = ref(props.initialTab || 'articles')
const categories = ref([])
const uploadBaseUrl = import.meta.env.VITE_APP_UPLOAD_URL || ''

const articleState = reactive({
  items: [],
  loading: false,
  error: '',
  loaded: false,
  categoryId: null,
  pagination: {
    page: 1,
    size: 10,
    total: 0,
  },
})

const commentState = reactive({
  items: [],
  loading: false,
  error: '',
  loaded: false,
})

const momentState = reactive({
  items: [],
  loading: false,
  error: '',
  loaded: false,
  pagination: {
    page: 1,
    size: 10,
    total: 0,
  },
})

const currentLoading = computed(() => {
  if (activeTab.value === 'articles') return articleState.loading
  if (activeTab.value === 'comments') return commentState.loading
  if (activeTab.value === 'moments') return momentState.loading
  return false
})

const ensureCategories = async () => {
  if (categories.value.length) return

  try {
    categories.value = await fetchCategories()
  } catch (error) {
    console.error('加载文章分类失败', error)
  }
}

const formatTime = (value) => {
  if (!value) return '--'
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const formatTags = (tags) => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags.filter(Boolean)
  if (typeof tags === 'string') {
    return tags
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
  }
  return []
}

const commentTargetLabel = (comment) => {
  if (comment.blogId) {
    return comment.title ? `文章 · ${comment.title}` : `文章 #${comment.blogId}`
  }
  if (comment.page === 'friends') return '论坛页'
  if (comment.page?.startsWith('forum-post-')) {
    return `论坛帖子 #${comment.page.replace('forum-post-', '')}`
  }
  return comment.page || '站内页面'
}

const parseMomentImages = (imageValue) => {
  if (!imageValue) return []
  return String(imageValue)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .map((item) => (item.startsWith('http') ? item : `${uploadBaseUrl}${item}`))
}

const loadArticles = async () => {
  articleState.loading = true
  articleState.error = ''

  try {
    await ensureCategories()

    const data = await fetchAdminArticles({
      page: articleState.pagination.page,
      size: articleState.pagination.size,
      categoryId: articleState.categoryId || undefined,
    })

    articleState.items = (data?.data || []).map((item) => ({
      ...item,
      statusLoading: false,
    }))
    articleState.pagination.total = Number(data?.pagination?.total || 0)
    articleState.loaded = true
  } catch (error) {
    console.error('加载文章管理数据失败', error)
    articleState.error = error.message || '加载文章管理数据失败，请稍后重试。'
  } finally {
    articleState.loading = false
  }
}

const loadComments = async () => {
  commentState.loading = true
  commentState.error = ''

  try {
    const data = await fetchAdminComments()
    commentState.items = (data || []).map((item) => ({
      ...item,
      statusLoading: false,
    }))
    commentState.loaded = true
  } catch (error) {
    console.error('加载评论管理数据失败', error)
    commentState.error = error.message || '加载评论管理数据失败，请稍后重试。'
  } finally {
    commentState.loading = false
  }
}

const loadMoments = async () => {
  momentState.loading = true
  momentState.error = ''

  try {
    const data = await fetchAdminMoments({
      page: momentState.pagination.page,
      size: momentState.pagination.size,
    })
    momentState.items = data?.list || []
    momentState.pagination.total = Number(data?.total || 0)
    momentState.loaded = true
  } catch (error) {
    console.error('加载动态管理数据失败', error)
    momentState.error = error.message || '加载动态管理数据失败，请稍后重试。'
  } finally {
    momentState.loading = false
  }
}

const loadActiveTab = async (force = false) => {
  if (activeTab.value === 'articles') {
    if (force || !articleState.loaded) {
      await loadArticles()
    }
    return
  }

  if (activeTab.value === 'comments') {
    if (force || !commentState.loaded) {
      await loadComments()
    }
    return
  }

  if (force || !momentState.loaded) {
    await loadMoments()
  }
}

const refreshCurrentTab = async () => {
  await loadActiveTab(true)
}

watch(
  () => activeTab.value,
  () => {
    loadActiveTab(false)
  },
  { immediate: true },
)

watch(
  () => props.initialTab,
  (value) => {
    if (value && value !== activeTab.value) {
      activeTab.value = value
    }
  },
)

const handleArticleFilterChange = () => {
  articleState.pagination.page = 1
  loadArticles()
}

const resetArticleFilter = () => {
  if (!articleState.categoryId) return
  articleState.categoryId = null
  articleState.pagination.page = 1
  loadArticles()
}

const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

const goToArticleEditor = (id) => {
  router.push(`/admin/article/edit/${id}`)
}

const goToCommentTarget = (comment) => {
  if (comment.blogId) {
    router.push(`/article/${comment.blogId}`)
    return
  }

  if (comment.page === 'friends') {
    router.push('/forum')
    return
  }

  if (comment.page?.startsWith('forum-post-')) {
    router.push(`/forum/${comment.page.replace('forum-post-', '')}`)
    return
  }

  ElMessage.info('当前评论所在页面没有单独跳转入口')
}

const goToMomentPage = () => {
  router.push('/moment')
}

const handleArticleStatusChange = async (row) => {
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

const handleCommentStatusChange = async (row) => {
  const previousStatus = !row.status
  row.statusLoading = true

  try {
    await updateAdminCommentStatus(row.id, row.status)
    ElMessage.success('评论状态已更新')
  } catch (error) {
    row.status = previousStatus
    console.error('更新评论状态失败', error)
  } finally {
    row.statusLoading = false
  }
}

const handleDeleteArticle = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除文章《${row.title}》吗？`, '删除文章', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })

    await deleteAdminArticle(row.id)

    if (articleState.items.length === 1 && articleState.pagination.page > 1) {
      articleState.pagination.page -= 1
    }

    ElMessage.success('文章已删除')
    await loadArticles()
  } catch (error) {
    if (error === 'cancel') return
    console.error('删除文章失败', error)
  }
}

const handleDeleteComment = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除这条评论及其子评论吗？', '删除评论', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })

    await deleteAdminComment(row.id)
    ElMessage.success('评论已删除')
    await loadComments()
  } catch (error) {
    if (error === 'cancel') return
    console.error('删除评论失败', error)
  }
}

const handleDeleteMoment = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除这条动态吗？', '删除动态', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })

    await deleteAdminMoment(row.id)

    if (momentState.items.length === 1 && momentState.pagination.page > 1) {
      momentState.pagination.page -= 1
    }

    ElMessage.success('动态已删除')
    await loadMoments()
  } catch (error) {
    if (error === 'cancel') return
    console.error('删除动态失败', error)
  }
}

defineExpose({
  refreshCurrentTab,
})
</script>

<style scoped>
.admin-tabs-shell {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px dashed rgba(15, 23, 42, 0.12);
}

.admin-tabs-shell.plain {
  margin-top: 0;
  padding-top: 0;
  border-top: 0;
}

.admin-tabs-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 10px;
}

.admin-tabs-title {
  font-size: 16px;
  font-weight: 700;
}

.admin-tabs-head p {
  margin: 6px 0 0;
  color: #6b7280;
  line-height: 1.7;
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

.tag-list,
.action-buttons,
.moment-preview {
  display: flex;
  align-items: center;
}

.tag-list,
.action-buttons {
  gap: 6px;
  flex-wrap: wrap;
}

.muted-text,
.moment-count {
  color: #6b7280;
}

.moment-preview {
  gap: 10px;
}

.moment-thumb {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
}

@media (max-width: 720px) {
  .admin-tabs-head {
    flex-direction: column;
  }

  .toolbar-select {
    width: 100%;
  }
}
</style>

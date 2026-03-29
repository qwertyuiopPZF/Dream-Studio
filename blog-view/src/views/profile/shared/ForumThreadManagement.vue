<template>
  <workspace-page-shell
    :title="title"
    :description="description"
    :loading="loading"
    @refresh="loadForumPosts"
  >
    <el-table :data="forumPosts" table-layout="fixed" size="small" class="management-table">
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column label="分类" min-width="120">
        <template #default="{ row }">
          <el-tag v-if="row.categoryName" size="small" effect="plain">{{
            row.categoryName
          }}</el-tag>
          <span v-else class="meta-placeholder">未设置</span>
        </template>
      </el-table-column>
      <el-table-column label="标签" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <div v-if="normalizeTags(row.tags).length" class="tag-cell">
            <el-tag
              v-for="tag in normalizeTags(row.tags).slice(0, 3)"
              :key="`${row.id}-${tag}`"
              size="small"
              effect="plain"
              type="success"
            >
              {{ tag }}
            </el-tag>
          </div>
          <span v-else class="meta-placeholder">未设置</span>
        </template>
      </el-table-column>

      <el-table-column prop="commentCount" label="评论" width="76" />
      <el-table-column prop="viewCount" label="浏览" width="76" />
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
      <el-table-column v-if="canModerateForumPosts" label="置顶" width="82">
        <template #default="{ row }">
          <el-switch
            v-model="row.isPinned"
            :loading="Boolean(row.metaLoading)"
            @change="updateForumMeta(row)"
          />
        </template>
      </el-table-column>
      <el-table-column v-if="canModerateForumPosts" label="加精" width="82">
        <template #default="{ row }">
          <el-switch
            v-model="row.isFeatured"
            :loading="Boolean(row.metaLoading)"
            @change="updateForumMeta(row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="140">
        <template #default="{ row }">{{ formatManagementTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="168">
        <template #default="{ row }">
          <div class="table-action-group">
            <el-button class="table-action-button" size="small" @click="goToForumPost(row.id)">
              <el-icon><View /></el-icon>
              <span>查看</span>
            </el-button>
            <el-button class="table-action-button is-danger" size="small" @click="removeForumPost(row)">
              <el-icon><Delete /></el-icon>
              <span>删除</span>
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div v-if="pagination.total > 0" class="tab-pagination">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="loadForumPosts"
      />
    </div>

  </workspace-page-shell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, View } from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/(5).png'
import {
  deleteManagedForumPost,
  fetchManagedForumPosts,
  updateManagedForumPostMeta,
  updateManagedForumPostStatus,
} from '@/api/forum'
import { useUserStore } from '@/store/user'
import { formatManagementTime } from '@/utils/profileManagement'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'

const router = useRouter()
const userStore = useUserStore()

const canModerateForumPosts = computed(() =>
  userStore.hasCapability(WORKSPACE_CAPABILITIES.FORUM_POST_MODERATE),
)
const loading = ref(false)
const forumPosts = ref([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const title = '帖子管理'
const description = computed(() => {
  return canModerateForumPosts.value
    ? '管理员可以管理全部帖子，普通用户只管理自己的帖子。'
    : '这里集中展示你发布过的帖子，方便查看与删除。'
})

const getPublisherName = (row) => row.authorNickname || row.nickname || '匿名用户'

const getPublisherAvatar = (row) => row.authorAvatar || row.avatar || defaultAvatar

const getPublisherInitial = (row) => getPublisherName(row).slice(0, 1).toUpperCase()

const normalizeTags = (tags) => {
  if (Array.isArray(tags)) return tags.map((item) => String(item).trim()).filter(Boolean)
  if (typeof tags !== 'string') return []
  return tags
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

const loadForumPosts = async () => {
  loading.value = true

  try {
    const response = await fetchManagedForumPosts({
      page: pagination.page,
      size: pagination.size,
    })

    forumPosts.value = (response?.data || []).map((item) => ({
      ...item,
      metaLoading: false,
      statusLoading: false,
    }))
    pagination.total = Number(response?.pagination?.total || 0)
  } finally {
    loading.value = false
  }
}

const handleStatusChange = async (row) => {
  const previousStatus = row.status === 1 ? 0 : 1
  row.statusLoading = true

  try {
    await updateManagedForumPostStatus(row.id, row.status)
    ElMessage.success('帖子状态已更新')
  } catch (error) {
    row.status = previousStatus
    console.error('更新帖子状态失败', error)
  } finally {
    row.statusLoading = false
  }
}

const updateForumMeta = async (row) => {
  if (!canModerateForumPosts.value) return

  row.metaLoading = true

  try {
    await updateManagedForumPostMeta(row.id, {
      isPinned: row.isPinned,
      isFeatured: row.isFeatured,
      categoryId: row.categoryId ?? null,
      tags: row.tagIds || '',
    })
    ElMessage.success('帖子状态已更新')
  } catch (error) {
    console.error('更新帖子状态失败', error)
    await loadForumPosts()
  } finally {
    row.metaLoading = false
  }
}

const removeForumPost = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除帖子「${row.title}」吗？`, '删除帖子', { type: 'warning' })
    await deleteManagedForumPost(row.id)
    ElMessage.success('帖子已删除')
    await loadForumPosts()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const goToForumPost = (id) => router.push(`/forum/${id}`)

onMounted(() => {
  loadForumPosts()
})
</script>

<style scoped>
.forum-manage-panel {
  margin-top: 18px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(248, 250, 252, 0.85);
}

.section-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.section-row h3 {
  margin: 0 0 8px;
}

.section-description {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.7;
}

.forum-manage-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tab-pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

:deep(.management-table .el-table__cell) {
  padding: 10px 0;
}

:deep(.management-table .cell) {
  padding-left: 8px;
  padding-right: 8px;
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

.tag-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.meta-placeholder {
  color: #94a3b8;
}

@media (max-width: 720px) {
  .section-row {
    flex-direction: column;
  }
}
</style>

<template>
  <workspace-page-shell :title="title" :description="description" :loading="loading" @refresh="loadForumPosts">
    <section class="forum-manage-panel">
      <el-table :data="forumPosts" table-layout="fixed" size="small" class="management-table">
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="发布人" min-width="150">
          <template #default="{ row }">
            <div class="publisher-cell">
              <el-avatar :size="30" :src="getPublisherAvatar(row)">{{ getPublisherInitial(row) }}</el-avatar>
              <span class="publisher-name">{{ getPublisherName(row) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="commentCount" label="评论" width="76" />
        <el-table-column prop="viewCount" label="浏览" width="76" />
        <el-table-column v-if="isAdmin" label="置顶" width="82">
          <template #default="{ row }">
            <el-switch v-model="row.isPinned" :loading="Boolean(row.metaLoading)" @change="updateForumMeta(row)" />
          </template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="加精" width="82">
          <template #default="{ row }">
            <el-switch v-model="row.isFeatured" :loading="Boolean(row.metaLoading)" @change="updateForumMeta(row)" />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="140">
          <template #default="{ row }">{{ formatManagementTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="128">
          <template #default="{ row }">
            <el-button text type="primary" @click="goToForumPost(row.id)">查看</el-button>
            <el-button text type="danger" @click="removeForumPost(row)">删除</el-button>
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
    </section>
  </workspace-page-shell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import defaultAvatar from '@/assets/(5).png'
import {
  deleteManagedForumPost,
  fetchAdminForumPosts,
  updateAdminForumPostMeta,
} from '@/api/admin'
import { useWorkspaceRouteBase } from '@/composables/useWorkspaceRouteBase'
import { useUserStore } from '@/store/user'
import { formatManagementTime } from '@/utils/profileManagement'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'

const router = useRouter()
const userStore = useUserStore()
const { routeBase } = useWorkspaceRouteBase()

const isAdmin = computed(() => userStore.isAdmin)
const loading = ref(false)
const forumPosts = ref([])
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0,
})

const title = '帖子管理'
const heading = computed(() => (isAdmin.value ? '论坛帖子管理' : '我的帖子'))
const description = computed(() => {
  return isAdmin.value
    ? '管理员可以管理全部帖子，普通用户只管理自己的帖子。'
    : '这里集中展示你发布过的帖子，方便查看与删除。'
})

const getPublisherName = (row) => row.authorNickname || row.nickname || '匿名用户'

const getPublisherAvatar = (row) => row.authorAvatar || row.avatar || defaultAvatar

const getPublisherInitial = (row) => getPublisherName(row).slice(0, 1).toUpperCase()

const loadForumPosts = async () => {
  loading.value = true

  try {
    const response = await fetchAdminForumPosts({
      page: pagination.page,
      size: pagination.size,
    })

    forumPosts.value = (response?.data || []).map((item) => ({ ...item, metaLoading: false }))
    pagination.total = Number(response?.pagination?.total || 0)
  } finally {
    loading.value = false
  }
}

const updateForumMeta = async (row) => {
  if (!isAdmin.value) return

  row.metaLoading = true

  try {
    await updateAdminForumPostMeta(row.id, {
      isPinned: row.isPinned,
      isFeatured: row.isFeatured,
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

const goToForum = () => router.push('/forum')
const goToForumPublish = () => router.push(`${routeBase.value}/forum-publish`)
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

@media (max-width: 720px) {
  .section-row {
    flex-direction: column;
  }
}
</style>

<template>
  <management-section-shell
    :bordered="bordered"
    :show-header="showHeader"
    :loading="state.loading"
    @refresh="loadComments"
  >
    <div v-if="state.error" class="tab-error">{{ state.error }}</div>

    <div v-loading="state.loading" class="tab-table-wrap">
      <el-table v-if="state.items.length" :data="state.items" table-layout="fixed">
        <el-table-column prop="nickname" label="用户" width="110" />
        <el-table-column label="来源" min-width="170">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ getCommentTargetLabel(row) }}</el-tag>
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
            <el-switch
              v-model="row.status"
              :loading="Boolean(row.statusLoading)"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="150">
          <template #default="{ row }">
            {{ formatManagementTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button text type="primary" @click="goToTarget(row)">定位</el-button>
              <el-button text type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else :image-size="84" description="暂无评论数据" />
    </div>
  </management-section-shell>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteAdminComment,
  fetchAdminComments,
  updateAdminCommentStatus,
} from '@/api/admin/comments'
import ManagementSectionShell from '@/components/profile/management/ManagementSectionShell.vue'
import {
  formatManagementTime,
  getCommentTargetLabel,
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
const state = reactive({
  items: [],
  loading: false,
  error: '',
})

const loadComments = async () => {
  state.loading = true
  state.error = ''

  try {
    const data = await fetchAdminComments()
    state.items = (data || []).map((item) => ({
      ...item,
      statusLoading: false,
    }))
  } catch (error) {
    console.error('加载评论管理数据失败', error)
    state.error = error.message || '加载评论管理数据失败，请稍后重试。'
  } finally {
    state.loading = false
  }
}

const goToTarget = (comment) => {
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

const handleStatusChange = async (row) => {
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

const handleDelete = async (row) => {
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

onMounted(() => {
  loadComments()
})
</script>

<style scoped>
.tab-table-wrap {
  min-height: 160px;
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
  gap: 6px;
  flex-wrap: wrap;
}
</style>

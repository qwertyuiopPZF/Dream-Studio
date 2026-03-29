<template>
  <management-page-shell>
    <section class="management-panel">
      <div v-if="state.error" class="tab-error">{{ state.error }}</div>

      <div v-loading="state.loading" class="tab-table-wrap">
        <el-table v-if="state.items.length" :data="state.items" table-layout="fixed" size="small" class="management-table">
          <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
          <el-table-column label="发布人" min-width="150">
            <template #default="{ row }">
              <div class="publisher-cell">
                <el-avatar :size="30" :src="getPublisherAvatar(row)">{{ getPublisherInitial(row) }}</el-avatar>
                <span class="publisher-name">{{ getPublisherName(row) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="图片" width="122">
            <template #default="{ row }">
              <div v-if="getMomentImages(row.image).length" class="moment-preview">
                <el-image
                  :src="getMomentImages(row.image)[0]"
                  :preview-src-list="getMomentImages(row.image)"
                  fit="cover"
                  preview-teleported
                  class="moment-thumb"
                />
                <span class="moment-count">{{ getMomentImages(row.image).length }} 张</span>
              </div>
              <span v-else class="muted-text">无图片</span>
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
          <el-table-column label="创建时间" width="132">
            <template #default="{ row }">
              {{ formatManagementTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="170">
            <template #default="{ row }">
              <div class="table-action-group">
                <el-button class="table-action-button" size="small" @click="goToMomentPage">
                  <el-icon><View /></el-icon>
                  <span>查看</span>
                </el-button>
                <el-button class="table-action-button is-danger" size="small" @click="handleDelete(row)">
                  <el-icon><Delete /></el-icon>
                  <span>删除</span>
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else :image-size="84" description="暂无动态数据" />
      </div>

      <div v-if="state.pagination.total > 0" class="tab-pagination">
        <el-pagination
          v-model:current-page="state.pagination.page"
          v-model:page-size="state.pagination.size"
          :page-sizes="[10, 20, 50]"
          :total="state.pagination.total"
          layout="total, prev, pager, next"
          @current-change="loadMoments"
        />
      </div>
    </section>
  </management-page-shell>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, View } from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/(5).png'
import { deleteManagedMoment, fetchManagedMoments, updateManagedMoment } from '@/api/moment'
import ManagementPageShell from '@/components/profile/management/ManagementPageShell.vue'
import {
  formatManagementTime,
  parseManagementMomentImages,
} from '@/utils/profileManagement'

const router = useRouter()
const uploadBaseUrl = import.meta.env.VITE_APP_UPLOAD_URL || ''
const state = reactive({
  items: [],
  loading: false,
  error: '',
  pagination: {
    page: 1,
    size: 10,
    total: 0,
  },
})

const getMomentImages = (imageValue) => parseManagementMomentImages(imageValue, uploadBaseUrl)
const getPublisherName = (row) => row.authorNickname || '匿名用户'
const getPublisherAvatar = (row) => row.authorAvatar || defaultAvatar
const getPublisherInitial = (row) => getPublisherName(row).slice(0, 1).toUpperCase()

const loadMoments = async () => {
  state.loading = true
  state.error = ''

  try {
    const data = await fetchManagedMoments({
      page: state.pagination.page,
      size: state.pagination.size,
    })

    state.items = (data?.list || []).map((item) => ({
      ...item,
      statusLoading: false,
    }))
    state.pagination.total = Number(data?.total || 0)
  } catch (error) {
    console.error('加载动态管理数据失败', error)
    state.error = error.message || '加载动态管理数据失败，请稍后重试。'
  } finally {
    state.loading = false
  }
}

const goToMomentPage = () => {
  router.push('/moment')
}

const handleStatusChange = async (row) => {
  const previousStatus = row.status === 1 ? 0 : 1
  row.statusLoading = true

  try {
    await updateManagedMoment(row.id, { status: row.status })
    ElMessage.success('动态状态已更新')
  } catch (error) {
    row.status = previousStatus
    console.error('更新动态状态失败', error)
  } finally {
    row.statusLoading = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除这条动态吗？', '删除动态', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })

    await deleteManagedMoment(row.id)

    if (state.items.length === 1 && state.pagination.page > 1) {
      state.pagination.page -= 1
    }

    ElMessage.success('动态已删除')
    await loadMoments()
  } catch (error) {
    if (error === 'cancel') return
    console.error('删除动态失败', error)
  }
}

onMounted(() => {
  loadMoments()
})
</script>

<style scoped>
.management-panel {
  display: flex;
  flex-direction: column;
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

.moment-preview {
  display: flex;
  align-items: center;
}

:deep(.management-table .el-table__cell) {
  padding: 10px 0;
}

:deep(.management-table .cell) {
  padding-left: 8px;
  padding-right: 8px;
}

.moment-preview {
  gap: 10px;
}

.muted-text,
.moment-count {
  color: #6b7280;
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

.moment-thumb {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  overflow: hidden;
  flex-shrink: 0;
}
</style>

<template>
  <workspace-page-shell title="用户管理" description="管理员可以查看当前账号，并执行删号和管理员权限调整。" :loading="loading" @refresh="loadUsers">
    <template v-if="!isAdmin">
      <workspace-permission-notice message="当前账号没有用户管理权限。" />
    </template>
    <template v-else>
      <div class="overview-grid">
        <article class="overview-card">
          <span>有效账号</span>
          <strong>{{ users.length }}</strong>
          <p>当前仍可登录或被后台管理的用户数量。</p>
        </article>
        <article class="overview-card">
          <span>管理员</span>
          <strong>{{ adminCount }}</strong>
          <p>至少保留一名管理员，避免后台权限丢失。</p>
        </article>
        <article class="overview-card">
          <span>已设密码</span>
          <strong>{{ passwordReadyCount }}</strong>
          <p>方便区分仅 GitHub 登录与已补充站内密码的账号。</p>
        </article>
      </div>

      <el-table :data="users" table-layout="fixed">
        <el-table-column label="用户" min-width="240">
          <template #default="{ row }">
            <div class="user-meta">
              <el-avatar :size="42" :src="row.avatar">{{ row.nickname?.slice(0, 1) || 'D' }}</el-avatar>
              <div>
                <div class="user-name-row">
                  <strong>{{ row.nickname || row.username }}</strong>
                  <el-tag v-if="isCurrentUser(row)" size="small" effect="plain">当前账号</el-tag>
                </div>
                <p>@{{ row.username }}</p>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="权限" width="140">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'success' : 'info'" effect="light">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="登录方式" width="140">
          <template #default="{ row }">
            <el-tag :type="row.passwordInitialized ? 'warning' : 'info'" effect="plain">
              {{ row.passwordInitialized ? '已设密码' : '仅 GitHub' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="GitHub" min-width="160">
          <template #default="{ row }">{{ row.githubLogin || '--' }}</template>
        </el-table-column>

        <el-table-column label="联系方式" min-width="200">
          <template #default="{ row }">
            <div class="contact-cell">
              <span>{{ row.email || '--' }}</span>
              <span>{{ row.phone || '--' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="加入时间" width="180">
          <template #default="{ row }">{{ formatManagementTime(row.createTime) }}</template>
        </el-table-column>

        <el-table-column label="最近登录" width="180">
          <template #default="{ row }">{{ formatManagementTime(row.lastLoginTime) }}</template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button
              text
              :type="row.role === 'ADMIN' ? 'warning' : 'primary'"
              :disabled="isCurrentUser(row) || actionLoadingId === row.id"
              @click="toggleAdminStatus(row)"
            >
              {{ row.role === 'ADMIN' ? '取消管理员' : '设为管理员' }}
            </el-button>
            <el-button
              text
              type="danger"
              :disabled="isCurrentUser(row) || actionLoadingId === row.id"
              @click="removeUser(row)"
            >
              删除用户
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </workspace-page-shell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteAdminUser,
  fetchAdminUsers,
  updateAdminUserAdminStatus,
} from '@/api/admin'
import { useUserStore } from '@/store/user'
import { formatManagementTime } from '@/utils/profileManagement'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'
import WorkspacePermissionNotice from '@/components/profile/workspace/WorkspacePermissionNotice.vue'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.isAdmin)
const currentProfile = computed(() => userStore.profile || {})
const loading = ref(false)
const actionLoadingId = ref(null)
const users = ref([])

const adminCount = computed(() => users.value.filter((item) => item.role === 'ADMIN').length)
const passwordReadyCount = computed(() => users.value.filter((item) => item.passwordInitialized).length)

const isCurrentUser = (row) => {
  if (!row) return false
  if (currentProfile.value.id && row.id) {
    return currentProfile.value.id === row.id
  }
  return row.username === currentProfile.value.username
}

const loadUsers = async () => {
  if (!isAdmin.value) return

  loading.value = true

  try {
    users.value = (await fetchAdminUsers()) || []
  } finally {
    loading.value = false
  }
}

const toggleAdminStatus = async (row) => {
  const nextAdminState = row.role !== 'ADMIN'
  const actionLabel = nextAdminState ? '设为管理员' : '取消管理员'

  try {
    await ElMessageBox.confirm(
      `确定要为「${row.nickname || row.username}」${actionLabel}吗？`,
      '更新用户权限',
      { type: 'warning' },
    )

    actionLoadingId.value = row.id
    await updateAdminUserAdminStatus(row.id, nextAdminState)
    ElMessage.success(nextAdminState ? '已授予管理员权限' : '已移除管理员权限')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  } finally {
    actionLoadingId.value = null
  }
}

const removeUser = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定删除用户「${row.nickname || row.username}」吗？删除后该账号将无法继续登录。`,
      '删除用户',
      { type: 'warning' },
    )

    actionLoadingId.value = row.id
    await deleteAdminUser(row.id)
    ElMessage.success('用户已删除')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  } finally {
    actionLoadingId.value = null
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 18px;
}

.overview-card {
  padding: 18px;
  border-radius: 20px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: linear-gradient(180deg, rgba(240, 253, 250, 0.72), rgba(255, 255, 255, 0.96));
}

.overview-card span {
  display: block;
  color: #64748b;
  font-size: 13px;
}

.overview-card strong {
  display: block;
  margin-top: 10px;
  color: #134e4a;
  font-size: 32px;
  line-height: 1;
}

.overview-card p {
  margin: 12px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-meta p,
.contact-cell {
  margin: 0;
  color: #64748b;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.contact-cell {
  display: grid;
  gap: 4px;
}

@media (max-width: 1080px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>

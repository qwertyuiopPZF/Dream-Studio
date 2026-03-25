<template>
  <div class="profile-center-container">
    <section class="hero-card">
      <div class="hero-left">
        <button
          type="button"
          class="profile-avatar-trigger"
          :class="{ clickable: isAdmin }"
          :title="isAdmin ? '进入后台管理' : '当前账号暂无后台权限'"
          @click="goToAdminApp"
        >
          <el-avatar :size="84" :src="profile.avatar">{{ userInitial }}</el-avatar>
        </button>
        <div>
          <div class="hero-kicker">Personal Center</div>
          <h1>{{ profile.nickname }}</h1>
          <p>
            {{
              isLoggedIn
                ? isAdmin
                  ? '这里集中处理账号安全、论坛记录、通知，以及后台管理。'
                  : '这里集中处理头像、密码、论坛记录、举报与通知。'
                : '登录后查看真实的帖子、评论、举报和通知数据。'
            }}
          </p>
        </div>
      </div>

      <div class="hero-right">
        <div class="hero-stat">
          <strong>{{ overview.postCount }}</strong>
          <span>已发布帖子</span>
        </div>
        <div class="hero-stat">
          <strong>{{ overview.commentCount }}</strong>
          <span>评论足迹</span>
        </div>
        <div class="hero-stat">
          <strong>{{ overview.unreadNotificationCount }}</strong>
          <span>未读通知</span>
        </div>
        <div v-if="isAdmin" class="hero-stat admin active">
          <strong>{{ adminTaskCount }}</strong>
          <span>后台待办</span>
        </div>
      </div>
    </section>

    <div v-if="loadError" class="error-banner">{{ loadError }}</div>

    <div class="profile-grid">
      <section class="profile-card primary-card">
        <div class="section-head">
          <div>
            <div class="card-title">内容与社区</div>
            <p>帖子、评论、举报都直接来自后端接口，管理员能力也并入这里。</p>
          </div>
          <el-button round :loading="loadingOverview || loadingAdminDashboard" @click="handleRefreshCenter"
            >刷新</el-button
          >
        </div>

        <template v-if="isAdmin">
          <div class="admin-workbench">
            <div class="admin-workbench-head">
              <div>
                <div class="subsection-title admin-title">后台工作台</div>
                <p>把后台总览和常用处理动作收进个人中心，进来就能直接管理。</p>
              </div>
              <el-tag size="small" type="success">已合并</el-tag>
            </div>

            <div v-if="adminDashboardError" class="admin-inline-error">{{ adminDashboardError }}</div>

            <div class="admin-stat-grid">
              <article v-for="card in adminStatCards" :key="card.label" class="admin-stat-card">
                <span>{{ card.label }}</span>
                <strong>{{ card.value }}</strong>
                <small>{{ card.hint }}</small>
              </article>
            </div>

            <div class="admin-focus-grid">
              <article class="admin-focus-item">
                <span>待处理举报</span>
                <strong>{{ overview.pendingReportCount }}</strong>
                <small>优先处理站内风险内容</small>
              </article>
              <article class="admin-focus-item">
                <span>可处理帖子</span>
                <strong>{{ moderationPosts.length }}</strong>
                <small>置顶、精华和删除统一在此处理</small>
              </article>
              <article class="admin-focus-item">
                <span>未读通知</span>
                <strong>{{ overview.unreadNotificationCount }}</strong>
                <small>合并后仍然保留站内提醒流</small>
              </article>
              <article class="admin-focus-item">
                <span>我的内容</span>
                <strong>{{ overview.postCount }}</strong>
                <small>个人发帖记录和后台处理共用同一入口</small>
              </article>
            </div>

            <admin-content-tabs ref="adminContentTabsRef" />
          </div>
        </template>

        <div class="subsection-title">我的帖子</div>
        <div v-if="myPosts.length" class="manage-list">
          <article v-for="post in myPosts" :key="post.id" class="manage-item">
            <button type="button" class="item-main" @click="goToPost(post.id)">
              <strong>{{ post.title }}</strong>
              <p>{{ post.summary || '暂无摘要' }}</p>
              <div class="meta-line">
                <span>{{ formatTime(post.createTime) }}</span>
                <span>{{ post.commentCount || 0 }} 评论 · {{ post.viewCount || 0 }} 浏览</span>
              </div>
            </button>
          </article>
        </div>
        <el-empty
          v-else
          :description="isLoggedIn ? '你还没有发布过帖子' : '登录后查看你的帖子'"
          :image-size="90"
        />

        <div class="subsection-title">我的评论</div>
        <div v-if="myComments.length" class="manage-list">
          <article v-for="comment in myComments" :key="comment.id" class="manage-item">
            <button type="button" class="item-main" @click="goToCommentTarget(comment)">
              <strong>{{ commentTargetLabel(comment) }}</strong>
              <p>{{ comment.content }}</p>
              <div class="meta-line">
                <span>{{ formatTime(comment.createTime) }}</span>
                <span>{{ comment.parentCommentId ? '回复评论' : '主评论' }}</span>
              </div>
            </button>
          </article>
        </div>
        <el-empty
          v-else
          :description="isLoggedIn ? '你还没有留下评论' : '登录后查看你的评论'"
          :image-size="90"
        />

        <div class="subsection-title">{{ isAdmin ? '举报后台处理' : '举报记录' }}</div>
        <div v-if="reports.length" class="manage-list">
          <article v-for="report in reports" :key="report.id" class="manage-item report-item">
            <button type="button" class="item-main" @click="goToReportedTarget(report)">
              <div class="item-head">
                <strong>{{ report.targetTitle }}</strong>
                <el-tag size="small" :type="reportStatusTagType(report.status)">{{
                  reportStatusText(report.status)
                }}</el-tag>
              </div>
              <p>
                举报原因：{{ reportReasonText(report.reason)
                }}<span v-if="report.detail"> · {{ report.detail }}</span>
              </p>
              <div class="meta-line">
                <span>{{ formatTime(report.createTime) }}</span>
                <span v-if="report.reviewerName">处理人：{{ report.reviewerName }}</span>
              </div>
            </button>
            <div v-if="isAdmin && report.status === 'PENDING'" class="item-actions">
              <el-button
                size="small"
                type="success"
                plain
                @click="handleReviewReport(report, 'RESOLVED')"
                >标记已处理</el-button
              >
              <el-button
                size="small"
                type="danger"
                plain
                @click="handleReviewReport(report, 'REJECTED')"
                >驳回</el-button
              >
            </div>
          </article>
        </div>
        <el-empty
          v-else
          :description="
            isLoggedIn
              ? isAdmin
                ? '当前没有举报记录'
                : '你还没有提交过举报'
              : '登录后查看举报记录'
          "
          :image-size="90"
        />

        <template v-if="isAdmin">
          <div class="subsection-title">帖子后台管理</div>
          <div v-if="moderationPosts.length" class="manage-list">
            <article
              v-for="post in moderationPosts"
              :key="`moderation-${post.id}`"
              class="manage-item report-item"
            >
              <button type="button" class="item-main" @click="goToPost(post.id)">
                <div class="item-head">
                  <strong>{{ post.title }}</strong>
                  <div class="post-flags">
                    <el-tag v-if="post.isPinned" size="small">置顶</el-tag>
                    <el-tag v-if="post.isFeatured" size="small" type="success">精华</el-tag>
                  </div>
                </div>
                <p>{{ post.summary || '暂无摘要' }}</p>
                <div class="meta-line">
                  <span>{{ formatTime(post.lastActivityTime || post.createTime) }}</span>
                  <span>{{ post.commentCount || 0 }} 评论 · {{ post.viewCount || 0 }} 浏览</span>
                </div>
              </button>
              <div class="item-actions multi-actions">
                <el-button size="small" plain @click="togglePostFlag(post, 'isPinned')">{{
                  post.isPinned ? '取消置顶' : '置顶'
                }}</el-button>
                <el-button
                  size="small"
                  type="success"
                  plain
                  @click="togglePostFlag(post, 'isFeatured')"
                  >{{ post.isFeatured ? '取消精华' : '设为精华' }}</el-button
                >
                <el-button size="small" type="danger" plain @click="deleteModerationPost(post)"
                  >删除帖子</el-button
                >
              </div>
            </article>
          </div>
          <el-empty v-else description="暂无可处理的帖子" :image-size="90" />
        </template>
      </section>

      <section class="profile-card side-card">
        <div class="card-title">账号安全</div>

        <div class="avatar-panel">
          <button
            type="button"
            class="profile-avatar-trigger compact"
            :class="{ clickable: isAdmin }"
            :title="isAdmin ? '进入后台管理' : '当前账号暂无后台权限'"
            @click="goToAdminApp"
          >
            <el-avatar :size="72" :src="profile.avatar">{{ userInitial }}</el-avatar>
          </button>
          <div>
            <strong>{{ profile.nickname }}</strong>
            <p>{{ isAdmin ? '管理员账号' : isLoggedIn ? '站点用户' : '游客身份' }}</p>
          </div>
        </div>

        <div class="info-pairs">
          <div>
            <span>用户名</span><strong>{{ profile.username || '--' }}</strong>
          </div>
          <div>
            <span>GitHub</span><strong>{{ profile.githubLogin || '--' }}</strong>
          </div>
          <div>
            <span>邮箱</span><strong>{{ profile.email || '未填写' }}</strong>
          </div>
          <div>
            <span>手机号</span><strong>{{ profile.phone || '未填写' }}</strong>
          </div>
          <div>
            <span>密码状态</span
            ><strong>{{ profile.passwordInitialized ? '已设置' : '待设置' }}</strong>
          </div>
          <div>
            <span>待处理举报</span><strong>{{ overview.pendingReportCount }}</strong>
          </div>
        </div>

        <div class="account-actions">
          <el-button :disabled="!isLoggedIn" round @click="openAvatarDialog">修改头像</el-button>
          <el-button :disabled="!isLoggedIn" type="primary" round @click="openPasswordDialog">{{
            profile.passwordInitialized ? '修改密码' : '设置密码'
          }}</el-button>
        </div>

        <div class="notification-block">
          <div class="subsection-title">消息通知</div>
          <div v-if="notifications.length" class="notification-list">
            <button
              v-for="item in notifications"
              :key="item.id"
              type="button"
              class="notification-item"
              :class="{ unread: !item.read }"
              @click="openNotification(item)"
            >
              <div class="item-head">
                <strong>{{ item.title }}</strong>
                <el-tag size="small" :type="item.read ? 'info' : 'warning'">{{
                  item.read ? '已读' : '未读'
                }}</el-tag>
              </div>
              <p>{{ item.content }}</p>
              <span>{{ formatTime(item.createTime) }}</span>
            </button>
          </div>
          <el-empty
            v-else
            :description="isLoggedIn ? '暂时没有消息通知' : '登录后查看消息通知'"
            :image-size="90"
          />
        </div>
      </section>
    </div>

    <el-dialog v-model="avatarDialogVisible" title="修改头像" width="460px">
      <el-form label-position="top">
        <el-form-item label="头像地址">
          <el-input v-model="avatarForm.avatar" placeholder="请输入 http(s) 或站内相对路径" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="avatarDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingAvatar" @click="submitAvatarUpdate"
          >保存头像</el-button
        >
      </template>
    </el-dialog>

    <el-dialog
      v-model="passwordDialogVisible"
      :title="profile.passwordInitialized ? '修改密码' : '设置密码'"
      width="460px"
    >
      <el-form label-position="top">
        <el-form-item label="新密码">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="至少 6 位"
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submittingPassword" @click="submitPasswordUpdate"
          >保存密码</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchAdminDashboardStatistics } from '@/api/admin'
import AdminContentTabs from '@/components/profile/AdminContentTabs.vue'
import { useAuthStore } from '@/store/auth'
import { useUserStore } from '@/store/user'
import { openAdminApp } from '@/utils/adminBridge'
import {
  deleteAdminForumPost,
  fetchCurrentUserOverview,
  markCurrentUserNotificationRead,
  reviewForumReport,
  updateAdminForumPost,
  updateCurrentUserAvatar,
  updateCurrentUserPassword,
} from '@/api/user'

const createEmptyOverview = () => ({
  profile: null,
  postCount: 0,
  commentCount: 0,
  unreadNotificationCount: 0,
  pendingReportCount: 0,
  myPosts: [],
  myComments: [],
  reports: [],
  notifications: [],
  moderationPosts: [],
})

const createEmptyAdminDashboard = () => ({
  articleCount: 0,
  commentCount: 0,
  momentCount: 0,
  forumPostCount: 0,
})

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

const loadingOverview = ref(false)
const loadingAdminDashboard = ref(false)
const loadError = ref('')
const adminDashboardError = ref('')
const centerOverview = ref(createEmptyOverview())
const adminDashboard = ref(createEmptyAdminDashboard())
const adminContentTabsRef = ref(null)

const avatarDialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const submittingAvatar = ref(false)
const submittingPassword = ref(false)

const avatarForm = reactive({ avatar: '' })
const passwordForm = reactive({ newPassword: '', confirmPassword: '' })

const profile = computed(() => userStore.profile)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isAdmin = computed(() => userStore.isAdmin)
const userInitial = computed(() => profile.value.nickname?.slice(0, 1)?.toUpperCase() || 'D')

const overview = computed(() => centerOverview.value)
const myPosts = computed(() => overview.value.myPosts || [])
const myComments = computed(() => overview.value.myComments || [])
const reports = computed(() => overview.value.reports || [])
const notifications = computed(() => overview.value.notifications || [])
const moderationPosts = computed(() => overview.value.moderationPosts || [])
const adminTaskCount = computed(
  () =>
    Number(overview.value.pendingReportCount || 0) +
    Number(moderationPosts.value.length || 0) +
    Number(overview.value.unreadNotificationCount || 0),
)
const adminStatCards = computed(() => [
  {
    label: '文章总数',
    value: adminDashboard.value.articleCount,
    hint: '后台内容库规模',
  },
  {
    label: '评论总数',
    value: adminDashboard.value.commentCount,
    hint: '用于观察社区活跃度',
  },
  {
    label: '动态总数',
    value: adminDashboard.value.momentCount,
    hint: '同步查看动态内容沉淀',
  },
  {
    label: '论坛帖子数',
    value: adminDashboard.value.forumPostCount,
    hint: '社区讨论内容规模',
  },
])

const loadOverview = async () => {
  if (!isLoggedIn.value) {
    centerOverview.value = createEmptyOverview()
    loadError.value = ''
    return
  }

  loadingOverview.value = true
  loadError.value = ''
  try {
    const data = await fetchCurrentUserOverview()
    centerOverview.value = {
      ...createEmptyOverview(),
      ...data,
    }

    if (data?.profile) {
      userStore.hydrateFromServerProfile(data.profile)
    }
  } catch (error) {
    console.error('加载个人中心失败', error)
    loadError.value = error.message || '加载个人中心失败，请稍后重试。'
  } finally {
    loadingOverview.value = false
  }
}

const loadAdminDashboard = async () => {
  if (!isLoggedIn.value || !isAdmin.value) {
    adminDashboard.value = createEmptyAdminDashboard()
    adminDashboardError.value = ''
    return
  }

  loadingAdminDashboard.value = true
  adminDashboardError.value = ''

  try {
    const data = await fetchAdminDashboardStatistics()
    adminDashboard.value = {
      ...createEmptyAdminDashboard(),
      ...data,
    }
  } catch (error) {
    console.error('加载后台总览失败', error)
    adminDashboardError.value = error.message || '后台总览加载失败，请稍后重试。'
  } finally {
    loadingAdminDashboard.value = false
  }
}

const handleRefreshCenter = async () => {
  const tasks = [loadOverview(), loadAdminDashboard()]

  if (isAdmin.value && adminContentTabsRef.value?.refreshCurrentTab) {
    tasks.push(adminContentTabsRef.value.refreshCurrentTab())
  }

  await Promise.allSettled(tasks)
}

watch(
  [() => isLoggedIn.value, () => isAdmin.value],
  () => {
    handleRefreshCenter()
  },
  { immediate: true },
)

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

const reportReasonText = (reason) => {
  const normalizedReason = String(reason || '').toUpperCase()
  const map = {
    SPAM: '垃圾内容 / 广告',
    ABUSE: '不当言论',
    COPYRIGHT: '侵权 / 搬运',
    OTHER: '其他',
  }
  return map[normalizedReason] || reason || '--'
}

const reportStatusText = (status) => {
  const normalizedStatus = String(status || '').toUpperCase()
  const map = {
    PENDING: '待处理',
    RESOLVED: '已处理',
    REJECTED: '已驳回',
  }
  return map[normalizedStatus] || status || '--'
}

const reportStatusTagType = (status) => {
  const normalizedStatus = String(status || '').toUpperCase()
  if (normalizedStatus === 'RESOLVED') return 'success'
  if (normalizedStatus === 'REJECTED') return 'danger'
  return 'warning'
}

const goToAdminApp = () => {
  openAdminApp({
    isAdmin: isAdmin.value,
    accessToken: authStore.accessToken,
    router,
  })
}

const goLogin = () => {
  router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
}

const goToPost = (postId) => {
  router.push(`/forum/${postId}`)
}

const commentTargetLabel = (comment) => {
  if (comment.page?.startsWith('forum-post-')) {
    return `论坛帖子 #${comment.page.replace('forum-post-', '')}`
  }
  if (comment.blogId) {
    return comment.title ? `博客文章：${comment.title}` : `博客文章 #${comment.blogId}`
  }
  return '站内页面评论'
}

const goToCommentTarget = (comment) => {
  if (comment.page?.startsWith('forum-post-')) {
    router.push(`/forum/${comment.page.replace('forum-post-', '')}`)
    return
  }
  if (comment.blogId) {
    router.push(`/article/${comment.blogId}`)
  }
}

const goToReportedTarget = (report) => {
  if (report.targetType === 'forum-post') {
    router.push(`/forum/${report.targetId}`)
  }
}

const openNotification = async (item) => {
  if (!item.read) {
    try {
      await markCurrentUserNotificationRead(item.id)
      item.read = true
      centerOverview.value.unreadNotificationCount = Math.max(
        0,
        (centerOverview.value.unreadNotificationCount || 0) - 1,
      )
    } catch (error) {
      console.error('标记通知已读失败', error)
    }
  }

  if (item.targetType === 'forum-post') {
    router.push(`/forum/${item.targetId}`)
  }
}

const openAvatarDialog = () => {
  avatarForm.avatar = profile.value.avatar || ''
  avatarDialogVisible.value = true
}

const submitAvatarUpdate = async () => {
  if (!avatarForm.avatar.trim()) {
    ElMessage.warning('请输入头像地址')
    return
  }

  submittingAvatar.value = true
  try {
    const updatedProfile = await updateCurrentUserAvatar({ avatar: avatarForm.avatar.trim() })
    userStore.hydrateFromServerProfile(updatedProfile)
    centerOverview.value.profile = updatedProfile
    avatarDialogVisible.value = false
    ElMessage.success('头像已更新')
  } catch (error) {
    console.error('更新头像失败', error)
  } finally {
    submittingAvatar.value = false
  }
}

const openPasswordDialog = () => {
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

const submitPasswordUpdate = async () => {
  if (passwordForm.newPassword.trim().length < 6) {
    ElMessage.warning('密码长度不能少于 6 位')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  submittingPassword.value = true
  try {
    await updateCurrentUserPassword({ newPassword: passwordForm.newPassword.trim() })
    userStore.hydrateFromServerProfile({ ...profile.value, passwordInitialized: true })
    centerOverview.value.profile = { ...profile.value, passwordInitialized: true }
    passwordDialogVisible.value = false
    ElMessage.success('密码已更新')
  } catch (error) {
    console.error('更新密码失败', error)
  } finally {
    submittingPassword.value = false
  }
}

const handleReviewReport = async (report, status) => {
  const actionText = status === 'RESOLVED' ? '标记为已处理' : '驳回'
  try {
    await ElMessageBox.confirm(`确定要${actionText}这条举报吗？`, '处理举报', {
      type: 'warning',
      confirmButtonText: actionText,
      cancelButtonText: '取消',
    })
    await reviewForumReport(report.id, { status })
    ElMessage.success('举报状态已更新')
    loadOverview()
  } catch (error) {
    if (error === 'cancel') return
    console.error('处理举报失败', error)
  }
}

const togglePostFlag = async (post, key) => {
  try {
    await updateAdminForumPost(post.id, {
      isPinned: key === 'isPinned' ? !post.isPinned : post.isPinned,
      isFeatured: key === 'isFeatured' ? !post.isFeatured : post.isFeatured,
    })
    ElMessage.success('帖子状态已更新')
    loadOverview()
  } catch (error) {
    console.error('更新帖子状态失败', error)
  }
}

const deleteModerationPost = async (post) => {
  try {
    await ElMessageBox.confirm(`确定删除帖子《${post.title}》吗？`, '删除帖子', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteAdminForumPost(post.id)
    ElMessage.success('帖子已删除')
    loadOverview()
  } catch (error) {
    if (error === 'cancel') return
    console.error('删除帖子失败', error)
  }
}

const handleLogout = async () => {
  authStore.logout()
  centerOverview.value = createEmptyOverview()
}
</script>

<style scoped>
.profile-center-container {
  max-width: 1160px;
  margin-top: 60px;
  color: var(--app-text-color);
}

.hero-card,
.profile-card {
  background: var(--card-bg-color, #fff);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 24px;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px;
  margin-bottom: 20px;
}

.hero-left {
  display: flex;
  gap: 18px;
  align-items: center;
}

.profile-avatar-trigger {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  line-height: 0;
  cursor: default;
  border-radius: 999px;
}

.profile-avatar-trigger.clickable {
  cursor: pointer;
}

.profile-avatar-trigger.clickable :deep(.el-avatar) {
  box-shadow: 0 0 0 0 rgba(22, 163, 74, 0.22);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.profile-avatar-trigger.clickable:hover :deep(.el-avatar) {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(22, 163, 74, 0.2);
}

.profile-avatar-trigger.compact {
  flex-shrink: 0;
}

.hero-kicker {
  color: #8a8a8a;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.hero-left h1 {
  margin: 8px 0 10px;
}

.hero-left p,
.section-head p,
.avatar-panel p {
  margin: 0;
  color: #777;
  line-height: 1.7;
}

.hero-right {
  display: grid;
  grid-template-columns: repeat(2, minmax(110px, 1fr));
  gap: 12px;
}

.hero-stat {
  min-width: 120px;
  padding: 18px 16px;
  border-radius: 18px;
  background: rgba(248, 250, 252, 0.92);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.hero-stat strong {
  font-size: 24px;
}

.hero-stat span {
  color: #6b7280;
  font-size: 13px;
}

.hero-stat.admin.active {
  background: rgba(22, 163, 74, 0.12);
}

.error-banner {
  margin-bottom: 20px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(239, 68, 68, 0.08);
  color: #b42318;
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(320px, 0.9fr);
  gap: 20px;
  align-items: start;
}

.profile-card {
  padding: 24px;
}

.section-head,
.item-head,
.avatar-panel,
.action-row,
.account-actions,
.meta-line,
.item-actions,
.post-flags {
  display: flex;
  align-items: center;
}

.section-head,
.item-head,
.action-row,
.meta-line,
.item-actions {
  justify-content: space-between;
}

.section-head {
  gap: 16px;
  margin-bottom: 20px;
}

.card-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 6px;
}

.subsection-title {
  margin: 26px 0 14px;
  font-size: 16px;
  font-weight: 700;
}

.manage-list,
.notification-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.manage-item,
.notification-item {
  width: 100%;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(250, 250, 250, 0.7);
}

.item-main,
.notification-item {
  width: 100%;
  text-align: left;
  background: transparent;
  border: 0;
  color: inherit;
  cursor: pointer;
}

.item-main strong,
.notification-item strong {
  font-size: 15px;
}

.item-main p,
.notification-item p {
  margin: 10px 0;
  color: #666;
  line-height: 1.7;
}

.meta-line {
  color: #8a8a8a;
  font-size: 13px;
  gap: 12px;
  flex-wrap: wrap;
}

.report-item {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.admin-workbench {
  margin-bottom: 28px;
  padding: 18px;
  border-radius: 20px;
  border: 1px solid rgba(22, 163, 74, 0.14);
  background: var(--app-page-bg);
}

.admin-workbench-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 14px;
}

.admin-workbench-head p {
  margin: 6px 0 0;
  color: #4b5563;
  line-height: 1.7;
}

.admin-title {
  margin: 0;
}

.admin-inline-error {
  margin-bottom: 14px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(239, 68, 68, 0.08);
  color: #b42318;
}

.admin-stat-grid,
.admin-focus-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.admin-focus-grid {
  margin-top: 12px;
}

.admin-stat-card,
.admin-focus-item {
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.8);
}

.admin-stat-card span,
.admin-focus-item span {
  display: block;
  color: #6b7280;
  font-size: 13px;
}

.admin-stat-card strong,
.admin-focus-item strong {
  display: block;
  margin: 8px 0 6px;
  font-size: 22px;
}

.admin-stat-card small,
.admin-focus-item small {
  color: #6b7280;
  line-height: 1.6;
}

.multi-actions {
  justify-content: flex-start;
  gap: 10px;
  flex-wrap: wrap;
}

.side-card {
  position: sticky;
  top: 80px;
}

.avatar-panel {
  gap: 16px;
  margin-bottom: 20px;
}

.avatar-panel strong {
  display: block;
  margin-bottom: 4px;
}

.info-pairs {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

.info-pairs div {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 10px;
  border-bottom: 1px dashed rgba(15, 23, 42, 0.12);
}

.info-pairs span {
  color: #6b7280;
}

.info-pairs strong {
  text-align: right;
}

.account-actions,
.action-row {
  gap: 10px;
  margin-top: 18px;
  flex-wrap: wrap;
}

.notification-block {
  margin-top: 26px;
}

.notification-item.unread {
  border-color: rgba(245, 158, 11, 0.3);
  background: rgba(255, 251, 235, 0.9);
}

@media (max-width: 1024px) {
  .hero-card,
  .profile-grid {
    grid-template-columns: 1fr;
    display: grid;
  }

  .hero-right {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .side-card {
    position: static;
  }
}

@media (max-width: 720px) {
  .profile-center-container {
    margin-top: 24px;
  }

  .hero-card,
  .profile-card {
    padding: 18px;
  }

  .hero-card {
    display: block;
  }

  .hero-left {
    margin-bottom: 18px;
    align-items: flex-start;
  }

  .hero-right {
    grid-template-columns: 1fr 1fr;
  }

  .admin-stat-grid,
  .admin-focus-grid {
    grid-template-columns: 1fr;
  }
}
</style>

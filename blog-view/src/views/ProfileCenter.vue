<template>
  <div class="profile-center-container">
    <section class="hero-card">
      <div class="hero-left">
        <el-avatar :size="78" :src="profile.avatar">{{ profile.nickname.slice(0, 1) }}</el-avatar>
        <div>
          <div class="hero-kicker">Personal Center</div>
          <h1>{{ profile.nickname }}</h1>
          <p>
            {{ isLoggedIn ? '这里会逐步承接论坛管理、消息通知和个性化设置。' : '当前为游客模式，可先在论坛参与发帖，后续登录后接入完整权限。' }}
          </p>
        </div>
      </div>
      <div class="hero-right">
        <div class="hero-stat">
          <strong>{{ forumDraftStats.posts }}</strong>
          <span>已发布帖子</span>
        </div>
        <div class="hero-stat">
          <strong>{{ forumDraftStats.comments }}</strong>
          <span>评论足迹</span>
        </div>
        <div class="hero-stat admin" :class="{ active: isAdmin }">
          <strong>{{ isAdmin ? 'ON' : 'OFF' }}</strong>
          <span>管理权限</span>
        </div>
      </div>
    </section>

    <div class="profile-grid">
      <section class="profile-card">
        <div class="card-title">论坛管理</div>
        <div class="subsection-title">我的帖子</div>
        <div v-if="myPosts.length" class="manage-list real-list">
          <button v-for="post in myPosts" :key="post.id" type="button" class="manage-item interactive" @click="goToPost(post.id)">
            <strong>{{ post.title }}</strong>
            <p>{{ post.summary || '暂无摘要' }}</p>
            <div class="meta-line">
              <span>{{ formatTime(post.createTime) }}</span>
              <span>{{ post.commentCount || 0 }} 评论 · {{ post.viewCount || 0 }} 浏览</span>
            </div>
          </button>
        </div>
        <el-empty v-else description="你还没有发布过帖子" :image-size="90" />

        <div class="subsection-title">我的评论</div>
        <div v-if="myComments.length" class="manage-list real-list">
          <button
            v-for="comment in myComments"
            :key="comment.id"
            type="button"
            class="manage-item interactive"
            @click="goToCommentTarget(comment)"
          >
            <strong>{{ commentTargetLabel(comment) }}</strong>
            <p>{{ comment.content }}</p>
            <div class="meta-line">
              <span>{{ formatTime(comment.createTime) }}</span>
              <span>{{ comment.parentCommentId ? '回复评论' : '主评论' }}</span>
            </div>
          </button>
        </div>
        <el-empty v-else description="你还没有留下评论" :image-size="90" />

        <div class="subsection-title">举报与审核</div>
        <div class="manage-list real-list">
          <div v-if="visibleReports.length" class="report-section">
            <button
              v-for="report in visibleReports"
              :key="report.id"
              type="button"
              class="manage-item interactive"
              @click="goToReportedTarget(report)"
            >
              <strong>{{ report.targetTitle }}</strong>
              <p>举报原因：{{ reportReasonText(report.reason) }}<span v-if="report.detail"> · {{ report.detail }}</span></p>
              <div class="meta-line">
                <span>{{ formatTime(report.createTime) }}</span>
                <span>{{ report.status === 'pending' ? '待处理' : report.status }}</span>
              </div>
            </button>
          </div>
          <el-empty
            v-else
            :description="isAdmin ? '当前没有待查看的举报记录' : '你还没有提交过举报'"
            :image-size="90"
          />
        </div>
      </section>

      <section class="profile-card">
        <div class="card-title">当前账号状态</div>
        <div class="info-pairs">
          <div><span>身份</span><strong>{{ isAdmin ? '管理员' : isLoggedIn ? '普通用户' : '游客' }}</strong></div>
          <div><span>昵称来源</span><strong>{{ nicknameSource }}</strong></div>
          <div><span>邮箱</span><strong>{{ profile.email || '未填写' }}</strong></div>
          <div><span>后续方向</span><strong>用户端集成管理权限</strong></div>
        </div>
        <div class="action-row">
          <el-button round @click="router.push('/forum')">进入论坛</el-button>
          <el-button v-if="!isLoggedIn" type="primary" round @click="router.push('/login')">前往登录</el-button>
        </div>

        <div class="notification-block">
          <div class="subsection-title">消息通知</div>
          <div v-if="visibleNotifications.length" class="notification-list">
            <button
              v-for="item in visibleNotifications"
              :key="item.id"
              type="button"
              class="notification-item"
              :class="{ unread: !item.read }"
              @click="openNotification(item)"
            >
              <strong>{{ item.title }}</strong>
              <p>{{ item.content }}</p>
              <span>{{ formatTime(item.createTime) }}</span>
            </button>
          </div>
          <el-empty v-else description="暂时没有消息通知" :image-size="90" />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { fetchForumPosts } from '@/api/forum'
import {
  getCommunityComments,
  getCommunityForumPosts,
  getCommunityNotifications,
  getCommunityReports,
  markNotificationAsRead,
} from '@/utils/community'

const router = useRouter()
const userStore = useUserStore()
const allForumPosts = ref([])

const profile = computed(() => userStore.profile)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isAdmin = computed(() => userStore.isAdmin)

const nicknameSource = computed(() => {
  if (localStorage.getItem('user_nickname')) return '登录资料'
  if (localStorage.getItem('forum_post_nickname')) return '论坛发帖信息'
  return '默认游客资料'
})

const myPostRecords = computed(() => getCommunityForumPosts())
const myCommentRecords = computed(() => getCommunityComments())
const allReports = computed(() => getCommunityReports())
const allNotifications = computed(() => getCommunityNotifications())

const forumDraftStats = computed(() => ({
  posts: myPosts.value.length,
  comments: myComments.value.length,
}))

const myPosts = computed(() => {
  const myIds = new Set(myPostRecords.value.map((item) => item.id))
  const fetched = allForumPosts.value.filter((item) => myIds.has(item.id))
  if (fetched.length) return fetched
  return myPostRecords.value
})

const myComments = computed(() => {
  const nickname = profile.value.nickname
  const email = profile.value.email
  return myCommentRecords.value.filter((item) => {
    if (email && item.email) {
      return item.email === email
    }
    return item.nickname === nickname
  })
})

const visibleReports = computed(() => {
  if (isAdmin.value) return allReports.value
  return allReports.value.filter((item) => item.reporterName === profile.value.nickname)
})

const visibleNotifications = computed(() => {
  if (isAdmin.value) return allNotifications.value
  return allNotifications.value.filter((item) => item.type !== 'report' || item.content.includes(profile.value.nickname))
})

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
  const map = {
    spam: '垃圾内容 / 广告',
    abuse: '不当言论',
    copyright: '侵权 / 搬运',
    other: '其他',
  }
  return map[reason] || reason
}

const goToPost = (postId) => {
  router.push(`/forum/${postId}`)
}

const commentTargetLabel = (comment) => {
  if (comment.page?.startsWith('forum-post-')) {
    return `论坛帖子 #${comment.page.replace('forum-post-', '')}`
  }
  if (comment.blogId) {
    return `博客文章 #${comment.blogId}`
  }
  return '站内页面评论'
}

const goToCommentTarget = (comment) => {
  if (comment.page?.startsWith('forum-post-')) {
    const postId = comment.page.replace('forum-post-', '')
    router.push(`/forum/${postId}`)
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

const openNotification = (item) => {
  markNotificationAsRead(item.id)
  if (item.targetType === 'forum-post') {
    router.push(`/forum/${item.targetId}`)
  }
}

onMounted(async () => {
  try {
    allForumPosts.value = (await fetchForumPosts({ sort: 'latest' })) || []
  } catch (error) {
    console.error('加载个人中心帖子失败', error)
  }
})
</script>

<style scoped>
.profile-center-container {
  max-width: 1120px;
  margin-top: 60px;
  color: var(--app-text-color);
}

.hero-card,
.profile-card {
  background: var(--card-bg-color, #fff);
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 24px;
  box-shadow: none;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 28px;
  margin-bottom: 20px;
}

.hero-left {
  display: flex;
  gap: 18px;
  align-items: center;
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

.hero-left p {
  margin: 0;
  color: #777;
  line-height: 1.8;
}

.hero-right {
  display: flex;
  gap: 12px;
}

.hero-stat {
  min-width: 110px;
  padding: 16px;
  border-radius: 18px;
  background: rgba(0, 0, 0, 0.04);
  text-align: center;
}

.hero-stat strong {
  display: block;
  font-size: 24px;
}

.hero-stat span {
  font-size: 13px;
  color: #808080;
}

.hero-stat.admin.active {
  background: rgba(34, 197, 94, 0.12);
}

.profile-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 18px;
}

.profile-card {
  padding: 24px;
}

.card-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 18px;
}

.subsection-title {
  font-size: 16px;
  font-weight: 700;
  margin: 8px 0 14px;
}

.manage-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.real-list {
  margin-bottom: 18px;
}

.manage-item {
  width: 100%;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(0, 0, 0, 0.03);
  border: 0;
  text-align: left;
}

.manage-item.interactive,
.notification-item {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.manage-item.interactive:hover,
.notification-item:hover {
  background: rgba(0, 0, 0, 0.06);
}

.manage-item strong {
  display: block;
  margin-bottom: 8px;
}

.manage-item p {
  margin: 0;
  color: #777;
  line-height: 1.8;
}

.meta-line {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  margin-top: 10px;
  color: #8a8a8a;
  font-size: 13px;
}

.info-pairs {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-pairs div {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 12px;
  border-bottom: 1px dashed rgba(15, 23, 42, 0.12);
}

.info-pairs span {
  color: #7d7d7d;
}

.action-row {
  display: flex;
  gap: 12px;
  margin-top: 22px;
}

.notification-block {
  margin-top: 28px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-item {
  border: 0;
  border-radius: 16px;
  background: rgba(0, 0, 0, 0.03);
  padding: 14px 16px;
  text-align: left;
}

.notification-item.unread {
  outline: 1px solid rgba(64, 158, 255, 0.25);
}

.notification-item strong {
  display: block;
  margin-bottom: 6px;
}

.notification-item p {
  margin: 0 0 8px;
  color: #777;
  line-height: 1.7;
}

.notification-item span {
  color: #999;
  font-size: 12px;
}

@media screen and (max-width: 900px) {
  .hero-card,
  .hero-left,
  .hero-right {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>

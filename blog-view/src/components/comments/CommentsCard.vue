<template>
  <div class="comments-card">
    <h3 class="title">评论</h3>

    <!-- 主评论表单 -->
    <div class="comment-form-wrapper">
      <div v-if="requireLogin" class="login-comment-box">
        <template v-if="isLoggedIn">
          <div class="current-user-bar">
            <el-avatar :size="42" :src="currentProfile.avatar">{{ currentProfile.nickname.slice(0, 1) }}</el-avatar>
            <div>
              <strong>{{ currentProfile.nickname }}</strong>
              <p>{{ currentProfile.email || '已登录用户评论' }}</p>
            </div>
          </div>
          <el-input
            v-model="commentForm.content"
            placeholder="请输入评论..."
            :rows="5"
            type="textarea"
          />
        </template>
        <div v-else class="login-required-panel">
          <p>登录后才能发表评论或回复，并自动使用你的头像与昵称。</p>
          <el-button type="primary" @click="router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })">去登录</el-button>
        </div>
      </div>

      <div v-else class="user-info-inputs">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-input v-model="commentForm.nickname" placeholder="昵称 (必填)" />
          </el-col>
          <el-col :span="12">
            <el-input v-model="commentForm.email" type="email" placeholder="邮箱 (必填，不公开)" />
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-input v-model="commentForm.website" type="url" placeholder="网站 (可选)" />
          </el-col>
          <el-col :span="12">
            <el-input v-model="commentForm.avatar" type="url" placeholder="头像URL (可选)" />
          </el-col>
        </el-row>
        <el-input
          v-model="commentForm.content"
          placeholder="请输入评论..."
          :rows="5"
          type="textarea"
        ></el-input>
      </div>

      <div class="form-footer">
        <el-button @click="submitRootComment" :disabled="requireLogin && !isLoggedIn"> 发表评论</el-button>
      </div>
    </div>

    <el-divider />
    <h3>共有{{ totalComments }}条评论</h3>
    <div v-if="loading && comments.length === 0" class="loading-tip">评论加载中...</div>
    <div v-if="error" class="error-tip">{{ error }}</div>

    <!-- 评论列表 -->
    <div class="comments-list" v-if="comments.length > 0">
      <CommentNode
        v-for="comment in comments"
        :key="comment.id"
        :comment="comment"
        :replyingTo="replyingToId"
        :defaultAvatar="defaultAvatar"
        :requireLogin="requireLogin"
        :isLoggedIn="isLoggedIn"
        :isAdmin="isAdmin"
        @show-reply="handleShowReply"
        :commentForm="commentForm"
        @submit-reply="handleSubmitReply"
        @cancel-reply="handleCancelReply"
        @delete-comment="handleDeleteComment"
      />
    </div>
    <div v-else-if="!loading" class="no-comments-tip">暂无评论，快来抢沙发吧！</div>

    <div v-if="noMore && comments.length > 0" class="no-more-tip">已加载所有评论</div>
  </div>
</template>

<script setup>
import { ref, onMounted, defineProps, reactive, onBeforeMount, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchComments, createComment, deleteComment } from '@/api/comment.js'
import { useUserStore } from '@/store/user'
import CommentNode from './CommentNode.vue'
import defaultAvatar from '@/assets/(5).png'
const props = defineProps({
  blogId: { type: [Number, String], default: null },
  page: { type: String, default: null },
  requireLogin: { type: Boolean, default: false },
})
const emit = defineEmits(['count-changed'])

const router = useRouter()
const userStore = useUserStore()
const currentProfile = computed(() => userStore.profile)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const isAdmin = computed(() => userStore.isAdmin)

const totalComments = ref(0)

const comments = ref([])
const commentForm = reactive({
  content: '',
  nickname: '',
  email: '',
  website: '',
  avatar: '',
  page: '',
  blogId: 0,
})

const fillLoginProfile = () => {
  if (!props.requireLogin || !isLoggedIn.value) return
  commentForm.nickname = currentProfile.value.nickname || ''
  commentForm.email = currentProfile.value.email || ''
  commentForm.avatar = currentProfile.value.avatar || defaultAvatar
  commentForm.website = ''
}

watch(
  () => [props.requireLogin, currentProfile.value.nickname, currentProfile.value.email, currentProfile.value.avatar, isLoggedIn.value],
  () => fillLoginProfile(),
  { immediate: true },
)

const loading = ref(false)
const error = ref(null)
const noMore = ref(false)
const pagination = reactive({ pageNum: 1, pageSize: 1000 }) // Load all comments at once for tree building

const replyingToId = ref(null)

const buildTree = (commentsList) => {
  const commentMap = {}
  const rootComments = []

  commentsList.forEach((comment) => {
    comment.children = []
    commentMap[comment.id] = comment
  })

  commentsList.forEach((comment) => {
    if (comment.parentCommentId && commentMap[comment.parentCommentId]) {
      const parent = commentMap[comment.parentCommentId]
      comment.parentCommentNickname = parent.nickname
      commentMap[comment.parentCommentId].children.push(comment)
    } else {
      rootComments.push(comment)
    }
  })

  Object.values(commentMap).forEach((comment) => {
    if (comment.children.length > 1) {
      comment.children.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
    }
  })

  rootComments.sort((a, b) => new Date(a.createTime) - new Date(b.createTime))

  return rootComments
}

const getComments = async () => {
  if (loading.value) return
  loading.value = true
  error.value = null
  try {
    const params = { blogId: props.blogId, page: props.page, ...pagination }
    const res = await fetchComments(params)
    const flatList = res || [] // The interceptor returns the data directly
    totalComments.value = flatList.length // 在这里更新总数
    emit('count-changed', flatList.length)
    comments.value = buildTree(flatList)
    if (flatList.length < pagination.pageSize) noMore.value = true
  } catch (err) {
    error.value = '评论加载失败，请稍后重试。'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const submitComment = async (commentData) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再评论')
    router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }
  if (!commentForm.nickname.trim()) return alert('昵称不能为空！')
  if (!commentForm.content.trim()) return alert('内容不能为空！')

  try {
    await createComment(commentData)

    ElMessage.success('评论成功')
    resetAndReload()
  } catch (err) {
    ElMessage.error(err.message || '评论失败，请稍后重试。')
    console.error(err)
  }
}
const submitRootComment = () => {
  if (!commentForm.content.trim()) return alert('评论内容不能为空！')
  const data = { ...commentForm, blogId: props.blogId, page: props.page, parentCommentId: null }
  submitComment(data)
}

const handleShowReply = (comment) => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录后再回复')
    router.push({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }
  replyingToId.value = comment.id
}

const handleSubmitReply = (parentComment) => {
  const data = {
    ...commentForm,
    blogId: props.blogId,
    page: props.page,
    parentCommentId: parentComment.id,
  }
  submitComment(data)
}

const handleCancelReply = () => {
  replyingToId.value = null
}

const handleDeleteComment = async (comment) => {
  try {
    await ElMessageBox.confirm('确定删除这条评论吗？删除后其子评论也会一并删除。', '删除评论', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await deleteComment(comment.id)
    ElMessage.success('评论已删除')
    resetAndReload()
  } catch (error) {
    if (error === 'cancel') return
    ElMessage.error(error?.message || '删除评论失败')
  }
}

const clearForm = () => {
  commentForm.content = ''
  if (props.requireLogin && isLoggedIn.value) {
    fillLoginProfile()
    return
  }
  commentForm.nickname = ''
  commentForm.email = ''
  commentForm.website = ''
  commentForm.avatar = ''
}

const resetAndReload = () => {
  replyingToId.value = null
  clearForm() // 调用清空函数
  comments.value = []
  pagination.pageNum = 1
  noMore.value = false
  getComments()
}
onBeforeMount(clearForm)
onMounted(getComments)
</script>

<style scoped>
.comments-card {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
}
.title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 20px;
}
.comment-form-wrapper {
  margin-bottom: 30px;
}

.login-comment-box {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.current-user-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(0, 0, 0, 0.03);
}

.current-user-bar strong {
  display: block;
  color: var(--app-text-color);
}

.current-user-bar p,
.login-required-panel p {
  margin: 4px 0 0;
  color: #8a8a8a;
}

.login-required-panel {
  padding: 18px;
  border-radius: 14px;
  background: rgba(0, 0, 0, 0.03);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.input-field {
  flex: 1 1 200px;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}
.comment-textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  resize: vertical;
}
.el-row {
  margin-bottom: 20px;
}
.el-row:last-child {
  margin-bottom: 0;
}
.el-col {
  border-radius: 4px;
}

.grid-content {
  border-radius: 4px;
  min-height: 36px;
}
.form-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
.submit-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  background-color: #333;
  color: white;
}
.loading-tip,
.error-tip,
.no-comments-tip,
.no-more-tip {
  text-align: center;
  color: #888;
  padding: 20px 0;
}
.error-tip {
  color: #f56c6c;
}
.comments-list {
  margin-top: 20px;
}
.no-more-tip {
  color: #999;
}
</style>

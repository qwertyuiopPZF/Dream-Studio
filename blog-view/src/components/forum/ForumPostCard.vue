<template>
  <el-card class="forum-post-card">
    <div class="card-content">
      <div class="author-row">
        <div class="author-info">
          <el-avatar :size="34" :src="post.authorAvatar || post.avatar || defaultAvatar" />
          <div>
            <strong>{{ post.authorNickname || post.nickname }}</strong>
            <div class="author-time">{{ formatTime(post.createTime) }}</div>
          </div>
        </div>
        <div class="card-badges">
          <el-tag v-if="post.isPinned" size="small" effect="plain">置顶</el-tag>
          <el-tag v-if="post.isFeatured" size="small" type="success" effect="plain">精华</el-tag>
        </div>
      </div>

      <h3 class="post-title">
        <router-link :to="`/forum/${post.id}`">{{ post.title }}</router-link>
      </h3>

      <p class="post-summary">{{ post.summary || fallbackSummary }}</p>

      <div v-if="derivedTags.length" class="tag-list">
        <el-tag v-for="tag in derivedTags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
      </div>

      <div class="post-meta">
        <button class="meta-action" type="button" @click="goDetail">
          <el-icon><ChatDotRound /></el-icon>{{ post.commentCount || 0 }} 评论
        </button>
        <button class="meta-action" type="button" @click="toggleLike">
          <el-icon><Pointer /></el-icon>{{ likeCount }} 点赞
        </button>
        <button class="meta-action" type="button" @click="sharePost">
          <el-icon><Share /></el-icon>分享
        </button>
        <router-link class="detail-link" :to="`/forum/${post.id}`">进入讨论</router-link>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Pointer, Share } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import defaultAvatar from '@/assets/(5).png'

const props = defineProps({
  post: {
    type: Object,
    required: true,
  },
})

const router = useRouter()
const likedStorageKey = computed(() => `forum-post-liked-${props.post.id}`)
const likeCount = ref(Number(localStorage.getItem(`forum-post-like-count-${props.post.id}`)) || 0)
const liked = ref(localStorage.getItem(likedStorageKey.value) === '1')

watch(
  () => props.post.id,
  (id) => {
    likeCount.value = Number(localStorage.getItem(`forum-post-like-count-${id}`)) || 0
    liked.value = localStorage.getItem(`forum-post-liked-${id}`) === '1'
  },
  { immediate: true },
)

const fallbackSummary = computed(() => {
  const content = props.post?.content || ''
  const plainText = content
    .replace(/[#>*_~`-]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
  return plainText.slice(0, 120) || '暂无摘要'
})

const derivedTags = computed(() => {
  const source = `${props.post?.title || ''} ${props.post?.summary || ''} ${props.post?.content || ''}`
  const matches = source.match(/[A-Za-z][A-Za-z0-9+#.-]{1,18}/g) || []
  return [...new Set(matches.map((item) => item.toLowerCase()))].slice(0, 4)
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

const goDetail = () => {
  router.push(`/forum/${props.post.id}`)
}

const toggleLike = () => {
  liked.value = !liked.value
  if (liked.value) {
    likeCount.value += 1
  } else {
    likeCount.value = Math.max(likeCount.value - 1, 0)
  }
  localStorage.setItem(likedStorageKey.value, liked.value ? '1' : '0')
  localStorage.setItem(`forum-post-like-count-${props.post.id}`, String(likeCount.value))
}

const sharePost = async () => {
  const link = `${window.location.origin}/forum/${props.post.id}`
  try {
    if (navigator.share) {
      await navigator.share({ title: props.post.title, url: link })
    } else if (navigator.clipboard) {
      await navigator.clipboard.writeText(link)
      ElMessage.success('帖子链接已复制')
    }
  } catch (error) {
    console.error('分享失败', error)
  }
}
</script>

<style scoped>
.forum-post-card {
  margin-bottom: 14px;
  box-shadow: none;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 18px;
}

.author-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.card-badges {
  display: flex;
  gap: 8px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
  color: #6b7280;
  font-size: 14px;
}

.author-info strong {
  display: block;
  color: var(--app-text-color);
}

.author-time {
  margin-top: 3px;
  color: #999;
  font-size: 12px;
}

.post-title a {
  position: relative;
  color: var(--app-text-color);
  text-decoration: none;
  font-size: 1.32rem;
  transition: color 0.3s;
}

.post-title a::after,
.detail-link::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -2px;
  width: 0%;
  height: 2px;
  background-color: currentColor;
  transition: width 0.3s ease-in-out;
}

.post-title a:hover,
.detail-link:hover {
  color: #333;
}

.post-title a:hover::after,
.detail-link:hover::after {
  width: 100%;
}

.post-summary {
  color: #7d7d7d;
  line-height: 1.7;
  margin: 12px 0 14px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.post-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  align-items: center;
  font-size: 0.9em;
  color: #999;
}

.meta-action {
  display: flex;
  align-items: center;
  gap: 6px;
  border: 0;
  background: transparent;
  padding: 0;
  color: #7c7c7c;
  cursor: pointer;
}

.meta-action:hover,
.detail-link:hover {
  color: var(--app-text-color);
}

.detail-link {
  position: relative;
  margin-left: auto;
  color: #7c7c7c;
  text-decoration: none;
}

@media screen and (max-width: 768px) {
  .author-row {
    flex-direction: column;
  }

  .detail-link {
    margin-left: 0;
  }
}
</style>

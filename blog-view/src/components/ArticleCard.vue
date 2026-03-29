<template>
  <div>
    <el-card class="article-card">
      <div class="article-card__layout">
        <router-link v-if="coverImageUrl" class="article-cover-link" :to="`/article/${article.id}`">
          <img :src="coverImageUrl" :alt="article.title" class="article-cover-image" loading="lazy" />
        </router-link>

        <div class="card-content">
          <h6 class="article-title">
            <router-link :to="`/article/${article.id}`">{{ article.title }}</router-link>
          </h6>
          <p class="article-summary">{{ article.summary }}</p>

          <div class="article-meta">
            <span v-if="article.authorNickname || article.authorAvatar">
              <el-avatar :size="28" :src="article.authorAvatar || article.avatar">{{
                authorInitial
              }}</el-avatar>
              <span>{{ article.authorNickname || 'null' }}</span>
            </span>
            <span v-if="article.tags"
              ><el-icon><PriceTag /></el-icon> {{ article.tags }}</span
            >
            <span v-if="formattedDate"
              ><el-icon><Clock /></el-icon> {{ formattedDate }}</span
            >
            <span v-if="article.categoryName"
              ><el-icon><FolderOpened /></el-icon> {{ article.categoryName }}</span
            >
            <span v-if="article.viewCount"
              ><el-icon><View /></el-icon>{{ article.viewCount }}</span
            >
          </div>

          <div class="more">
            <router-link :to="`/article/${article.id}`">more</router-link>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { Clock, FolderOpened, PriceTag, View } from '@element-plus/icons-vue'
import { defineProps, computed } from 'vue'

const props = defineProps({
  article: {
    type: Object,
    required: true,
  },
})

const uploadBaseUrl = import.meta.env.VITE_APP_UPLOAD_URL || ''

const formattedDate = computed(() => {
  if (!props.article.publishTime) return ''
  const date = new Date(props.article.publishTime)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
})

const authorInitial = computed(() => {
  return (props.article.authorNickname || 'A').slice(0, 1).toUpperCase()
})

const coverImageUrl = computed(() => {
  const coverImage = props.article.coverImage
  if (!coverImage) return ''
  return coverImage.startsWith('http') ? coverImage : `${uploadBaseUrl}${coverImage}`
})
</script>

<style scoped>
.article-card {
  margin-bottom: 10px;
  box-shadow: none;
  border: 0;
}

.article-card__layout {
  display: flex;
  gap: 18px;
  align-items: stretch;
}

.article-cover-link {
  display: block;
  width: 220px;
  min-width: 220px;
  border-radius: 18px;
  overflow: hidden;
}

.article-cover-image {
  display: block;
  width: 100%;
  height: 100%;
  min-height: 156px;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-cover-link:hover .article-cover-image {
  transform: scale(1.04);
}

.card-content {
  flex: 1;
  min-width: 0;
}

.article-title a {
  position: relative;
  color: #5b5b5b;
  text-decoration: none;
  font-size: 1.4rem;
  transition: color 0.3s;
}

/* 下划线动画 */
.article-title a::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -2px; /* 可以根据需求调整位置 */
  width: 0%;
  height: 2px;
  background-color: currentColor; /* 使用当前文字颜色 */
  transition: width 0.3s ease-in-out;
}

.article-title a:hover {
  color: #333;
}

.article-title a:hover::after {
  width: 100%;
}

.article-summary {
  margin: 12px 0 0;
  color: #7d7d7d;
  line-height: 1.6;
}

.article-author {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 14px;
  color: #666;
  font-size: 14px;
}

.article-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  font-size: 0.9em;
  color: #999999;
  margin-top: 15px;
}

.article-meta span {
  display: flex;
  align-items: center;
  gap: 5px;
}

.more a {
  color: #7c7c7c;
  transition: color 0.3s;
  position: relative;
  float: inline-end;
  text-decoration: none;
  font-size: 1rem;
}

.more a:hover {
  color: #333;
}

/* 下划线动画 */
.more a::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: -2px;
  width: 0%;
  height: 2px;
  background-color: currentColor;
  transition: width 0.3s ease-in-out;
}

.more a:hover::after {
  width: 100%;
}

@media (max-width: 767px) {
  .article-card__layout {
    flex-direction: column;
    gap: 14px;
  }

  .article-cover-link {
    width: 100%;
    min-width: 0;
  }

  .article-cover-image {
    min-height: 188px;
  }

  .article-meta {
    gap: 12px;
  }
}
</style>

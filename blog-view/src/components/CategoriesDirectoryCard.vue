<template>
  <div class="directory-card">
    <el-text class="mx-1"> <h3 class="directory-title">目录</h3></el-text>
    <div v-if="loading">正在加载文章...</div>
    <div v-if="error">{{ error }}</div>

    <div v-if="articleslist.length" class="dir-list">
      <el-row :gutter="100">
        <el-col v-for="(column, columnIndex) in articleColumns" :key="columnIndex" :xs="24" :sm="12">
          <div class="grid-content" :class="columnIndex === 0 ? 'ep-bg-purple' : 'ep-bg-purple-light'">
            <div v-for="article in column" :key="article.id" class="directory-item">
              <router-link :to="`/article/${article.id}`" class="articlestitle">
                {{ article.title }}
              </router-link>

              <span class="article-author">
                <el-avatar class="article-author-avatar" :size="22" :src="getAuthorAvatar(article)">
                  {{ getAuthorInitial(article) }}
                </el-avatar>
                <span class="article-author-name">{{ getAuthorName(article) }}</span>
              </span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    <div v-else-if="!loading" style="display: flex; justify-content: center">
      该分类下暂无文章。
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchArticlesByCategoryId } from '@/api/categories'
import defaultAvatar from '@/assets/(5).png'

const route = useRoute()
const articleslist = ref([])
const loading = ref(false)
const error = ref(null)
const categoryId = ref(route.params.id)

const articleColumns = computed(() => {
  const middleIndex = Math.ceil(articleslist.value.length / 2)
  return [articleslist.value.slice(0, middleIndex), articleslist.value.slice(middleIndex)]
})

const getAuthorName = (article) => article?.authorNickname || '匿名用户'

const getAuthorAvatar = (article) => article?.authorAvatar || article?.avatar || defaultAvatar

const getAuthorInitial = (article) => getAuthorName(article).slice(0, 1).toUpperCase()

const getArticlesByCategoryId = async (id) => {
  loading.value = true
  error.value = null
  try {
    const response = await fetchArticlesByCategoryId(id)
    articleslist.value = response.data || response || []
  } catch (err) {
    error.value = '获取分类列表失败'
    console.error(err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getArticlesByCategoryId(categoryId.value)
})

watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      categoryId.value = newId
      getArticlesByCategoryId(newId)
    }
  },
)
</script>

<style scoped>
.directory-card {
  padding: 20px 0;
  border-top: 1px solid var(--card-border-color, #3a3a3a);
  border-bottom: 1px solid var(--card-border-color, #3a3a3a);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.directory-title {
  font-size: 1.6rem;
  color: var(--app-secondary-text-color, #a2a2a2);
}

.dir-list {
  counter-reset: directory-counter;
  animation: fadeIn 0.5s ease-out 0.3s forwards;
  opacity: 0;
}

.grid-content {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.directory-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: start;
  column-gap: 16px;
  line-height: 1.6;
}

.articlestitle {
  display: block;
  min-width: 0;
  color: #555555;
  text-decoration: none;
  text-shadow: #666;
  transition: color 0.3s;
}

.articlestitle:hover {
  color: #000000;
}

.article-author {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: var(--app-secondary-text-color, #9b9b9b);
  font-size: 0.84rem;
  font-weight: 400;
  letter-spacing: 0.01em;
  white-space: nowrap;
  justify-self: end;
  opacity: 0.88;
}

.article-author-avatar {
  flex-shrink: 0;
  border: 1px solid rgba(0, 0, 0, 0.05);
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.42);
}

.article-author-name {
  line-height: 1;
  color: inherit;
}

@media (max-width: 768px) {
  .directory-item {
    grid-template-columns: minmax(0, 1fr);
    row-gap: 8px;
  }

  .article-author {
    justify-self: start;
  }
}
</style>

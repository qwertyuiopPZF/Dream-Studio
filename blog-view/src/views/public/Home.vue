<template>
  <div class="home-container">
    <el-row :gutter="20" class="main-content">
      <el-col :xs="20" :sm="10" :md="18">
        <div
          v-if="!route.query.keyword && (featuredLoading || featuredArticles.length)"
          v-loading="featuredLoading"
          class="featured-carousel-shell"
        >
          <el-carousel
            v-if="featuredArticles.length"
            height="clamp(280px, 26vw, 320px)"
            :interval="5000"
            trigger="click"
            :autoplay="featuredArticles.length > 1"
            indicator-position="outside"
            class="featured-carousel"
          >
            <el-carousel-item v-for="article in featuredArticles" :key="article.id">
              <article
                class="featured-slide"
                :style="getFeaturedSlideStyle(article)"
                @click="openFeaturedArticle(article.id)"
              >
                <div class="featured-slide__content">
                  <span class="featured-slide__eyebrow">精华文章</span>
                  <h2>{{ article.title }}</h2>
                  <p>{{ article.summary }}</p>
                  <div class="featured-slide__meta">
                    <span v-if="article.categoryName">{{ article.categoryName }}</span>
                    <span v-if="formatPublishDate(article.publishTime)">
                      {{ formatPublishDate(article.publishTime) }}
                    </span>
                    <span v-if="article.viewCount">{{ article.viewCount }} 阅读</span>
                  </div>
                </div>
              </article>
            </el-carousel-item>
          </el-carousel>
        </div>

        <div class="article-list">
          <ArticleCard v-for="(article, index) in articles" :key="index" :article="article" />
        </div>
        <div v-if="loading" :data="getArticles" style="width: 100%" class="loading-tip">
          正在加载文章...
        </div>
        <div v-if="error" class="error-tip">{{ error }}</div>

        <div class="pagination-container" v-if="!loading && !error && articles.length > 0">
          <el-pagination
            layout="prev, pager, next"
            :total="pagination.total"
            :page-size="pagination.size"
            :current-page="pagination.currentPage"
            @current-change="handlePageChange"
          />
        </div>
      </el-col>
      <!-- Sidebar -->
      <el-col :xs="24" :sm="24" :md="6" class="sidebar-column">
        <div class="sidebar">
          <InfoCard class="sidebar-card" />

          <div class="sidebar-affix-shell">
            <TagsCard class="sidebar-card" />
            <CategoryCard class="sidebar-card" />
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import InfoCard from '@/components/InfoCard.vue'
import TagsCard from '@/components/TagsCard.vue'
import CategoryCard from '@/components/CategoryCard.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import { fetchArticles } from '@/api/article.js'

const route = useRoute()
const router = useRouter()
const uploadBaseUrl = import.meta.env.VITE_APP_UPLOAD_URL || ''

// 文章列表
const articles = ref([])
const featuredArticles = ref([])

// 分页信息
const pagination = ref({
  currentPage: 1,
  total: 0,
  size: 10,
})

// 加载和错误状态
const loading = ref(true)
const featuredLoading = ref(false)
const error = ref(null)

const featuredFallbacks = [
  'radial-gradient(circle at 16% 20%, rgba(255, 238, 203, 0.95) 0%, rgba(255, 238, 203, 0) 32%), linear-gradient(135deg, #10315c 0%, #1d5b8f 45%, #f28d35 100%)',
  'radial-gradient(circle at 82% 18%, rgba(255, 224, 198, 0.92) 0%, rgba(255, 224, 198, 0) 28%), linear-gradient(135deg, #31473a 0%, #6b8f71 44%, #d7a86e 100%)',
  'radial-gradient(circle at 24% 18%, rgba(255, 235, 211, 0.92) 0%, rgba(255, 235, 211, 0) 34%), linear-gradient(135deg, #2f2548 0%, #4b4e9b 48%, #e78267 100%)',
  'radial-gradient(circle at 78% 24%, rgba(255, 255, 255, 0.7) 0%, rgba(255, 255, 255, 0) 26%), linear-gradient(135deg, #3d2c2e 0%, #a05c5c 48%, #f5c26b 100%)',
]

const resolveCoverImage = (coverImage) => {
  if (!coverImage) return ''
  return coverImage.startsWith('http') ? coverImage : `${uploadBaseUrl}${coverImage}`
}

const pickFeaturedFallback = (article) => {
  const seed = String(article?.id || article?.title || '')
  const hash = Array.from(seed).reduce((total, char) => total + char.charCodeAt(0), 0)
  return featuredFallbacks[hash % featuredFallbacks.length]
}

const getFeaturedSlideStyle = (article) => {
  const coverImage = resolveCoverImage(article.coverImage)
  return {
    backgroundImage: coverImage
      ? `linear-gradient(90deg, rgba(6, 14, 30, 0.82) 0%, rgba(6, 14, 30, 0.48) 48%, rgba(6, 14, 30, 0.18) 100%), url("${coverImage}")`
      : pickFeaturedFallback(article),
  }
}

const formatPublishDate = (publishTime) => {
  if (!publishTime) return ''
  return new Date(publishTime).toLocaleDateString('zh-CN', {
    month: 'long',
    day: 'numeric',
  })
}

const openFeaturedArticle = (id) => {
  router.push(`/article/${id}`)
}

// 获取文章列表
const getArticles = async (page = 1) => {
  loading.value = true
  error.value = null
  try {
    const keyword = route.query.keyword || ''
    const response = await fetchArticles({
      page,
      size: pagination.value.size,
      keyword: keyword || undefined,
    })
    articles.value = response.data
    pagination.value.total = response.pagination.total
    pagination.value.currentPage = response.pagination.currentPage
  } catch (err) {
    error.value = '获取文章列表失败，请稍后再试。'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const getFeaturedArticles = async () => {
  featuredLoading.value = true

  try {
    const response = await fetchArticles({
      page: 1,
      size: 5,
      featuredOnly: true,
    })
    featuredArticles.value = response.data || []
  } catch (err) {
    featuredArticles.value = []
    console.error('获取精华文章轮播失败', err)
  } finally {
    featuredLoading.value = false
  }
}

// 分页
const handlePageChange = (page) => {
  getArticles(page)
  const container = document.querySelector('.main-scroll-container')
  container?.scrollTo({ top: 0, behavior: 'smooth' })
}

// 组件挂载时获取第一页数据
onMounted(() => {
  getArticles(1)
  getFeaturedArticles()
})

// 监听路由上的 keyword 变化，实时更新搜索结果
watch(
  () => route.query.keyword,
  (keyword) => {
    // 每次搜索关键字变化时，从第一页重新请求
    getArticles(1)

    if (!keyword && !featuredArticles.value.length) {
      getFeaturedArticles()
    }
  },
)
</script>

<style scoped>
.home-container {
  max-width: 1200px;
  margin-top: 60px;
  animation: fadeIn 0.5s ease-out 0.3s forwards;
  opacity: 0;
}

.featured-carousel-shell {
  margin-bottom: 22px;
}

.featured-carousel {
  border-radius: 8px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(226, 234, 244, 0.95), rgba(247, 236, 220, 0.9));
  box-shadow: 0 24px 54px rgba(18, 32, 61, 0.14);
}

.featured-slide {
  position: relative;
  display: flex;
  align-items: flex-end;
  width: 100%;
  height: 100%;
  padding: clamp(28px, 4vw, 42px);
  border-radius: 28px;
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  cursor: pointer;
}

.featured-slide::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(6, 14, 30, 0.04) 0%, rgba(6, 14, 30, 0.5) 100%);
}

.featured-slide__content {
  position: relative;
  z-index: 1;
  max-width: min(76%, 640px);
  color: #fff;
}

.featured-slide__eyebrow {
  display: inline-flex;
  align-items: center;
  margin-bottom: 14px;
  padding: 7px 14px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(12px);
  font-size: 0.82rem;
  letter-spacing: 0.16em;
}

.featured-slide__content h2 {
  margin: 0;
  font-size: clamp(1.9rem, 3vw, 3.15rem);
  line-height: 1.16;
  text-wrap: balance;
  text-shadow: 0 10px 24px rgba(0, 0, 0, 0.26);
}

.featured-slide__content p {
  display: -webkit-box;
  margin: 14px 0 0;
  overflow: hidden;
  color: rgba(255, 255, 255, 0.88);
  line-height: 1.7;
  text-shadow: 0 4px 14px rgba(0, 0, 0, 0.18);
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.featured-slide__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.featured-slide__meta span {
  padding: 6px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  color: rgba(255, 255, 255, 0.92);
  font-size: 0.88rem;
}

.featured-slide__cta {
  margin-top: 26px;
  padding: 12px 22px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, #ffffff 0%, #dff0ff 100%);
  color: #20446d;
  font-size: 0.96rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 12px 26px rgba(19, 47, 85, 0.22);
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease;
}

.featured-slide:hover .featured-slide__cta {
  transform: translateY(-1px);
  box-shadow: 0 16px 30px rgba(19, 47, 85, 0.28);
}

.featured-carousel-shell :deep(.el-carousel__container) {
  border-radius: 28px;
}

.featured-carousel-shell :deep(.el-carousel__arrow) {
  width: 42px;
  height: 42px;
  background: rgba(14, 23, 43, 0.34);
  backdrop-filter: blur(12px);
}

.featured-carousel-shell :deep(.el-carousel__indicators--outside) {
  margin-top: -8px;
}

.featured-carousel-shell :deep(.el-carousel__button) {
  width: 34px;
  height: 4px;
  border-radius: 999px;
  background: rgba(28, 68, 109, 0.24);
}

.featured-carousel-shell :deep(.is-active .el-carousel__button) {
  background: linear-gradient(90deg, #376ca4, #f59f63);
}

.article-card {
  margin-bottom: 20px;
}

.article-meta {
  font-size: 0.9em;
  color: #aaa;
}

.article-meta span {
  margin-right: 15px;
}

.sidebar-card {
  margin-bottom: 20px;
}

.main-content {
  align-items: stretch;
}

.sidebar-column {
  display: flex;
}

.sidebar,
.sidebar-affix-shell {
  width: 100%;
}

.sidebar {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.sidebar-affix-shell {
  position: sticky;
  top: 0px;
  display: flex;
  flex-direction: column;
}

.sidebar-affix-shell .sidebar-card:last-child {
  margin-bottom: 0;
}

@media (max-width: 991px) {
  .sidebar-affix-shell {
    position: static;
    top: auto;
  }
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.pagination-container ::v-deep(.el-pager li.is-active) {
  color: var(--pagination-active-color);
  font-weight: bold;
}

.pagination-container ::v-deep(.el-pager li.is-active:hover) {
  color: var(--pagination-active-color);
}

.pagination-container ::v-deep(.el-pager li:not(.is-active):hover) {
  color: var(--pagination-hover-color) !important;
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

@media (max-width: 991px) {
  .featured-slide__content {
    max-width: 100%;
  }
}

@media (max-width: 767px) {
  .home-container {
    margin-top: 34px;
  }

  .featured-carousel-shell {
    margin-bottom: 18px;
  }

  .featured-slide {
    align-items: flex-end;
    padding: 24px 20px;
  }

  .featured-slide__content h2 {
    font-size: 1.65rem;
  }

  .featured-slide__content p {
    font-size: 0.94rem;
    line-height: 1.6;
  }

  .featured-slide__meta {
    gap: 8px;
    margin-top: 14px;
  }

  .featured-slide__meta span {
    font-size: 0.8rem;
  }

  .featured-slide__cta {
    margin-top: 20px;
    padding: 10px 18px;
  }
}
</style>

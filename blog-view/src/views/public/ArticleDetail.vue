<template>
  <div class="article-detail-container">
    <div v-if="loading" class="loading-tip">文章加载中...</div>
    <div v-if="error" class="error-tip">{{ error }}</div>

    <div class="main-content-area" :class="{ 'catalog-open': catalogDrawerVisible }">
      <div class="center">
        <el-card v-if="article" class="article-content-card">
          <div class="article-header-block">
            <p class="article-kicker">Article</p>

            <div class="article-author-row">
              <div class="article-author-info">
                <el-avatar :size="64" :src="article.authorAvatar || defaultAvatar">{{
                  authorInitial
                }}</el-avatar>
                <div>
                  <strong>{{ article.authorNickname || 'admin' }}</strong>
                  <p>作者</p>
                </div>
              </div>
              <div class="article-meta">
                <span>发布于：{{ formatTime(article.publishTime) }}</span>
              </div>
            </div>

            <div v-if="article.categoryName || formattedTags.length" class="article-taxonomy">
              <div v-if="article.categoryName" class="taxonomy-group">
                <span class="taxonomy-label">分类</span>
                <el-tag
                  round
                  effect="plain"
                  class="taxonomy-tag category-tag interactive-tag"
                  @click="goToCategory"
                >
                  {{ article.categoryName }}
                </el-tag>
              </div>

              <div v-if="formattedTags.length" class="taxonomy-group taxonomy-group-tags">
                <span class="taxonomy-label">标签</span>
                <div class="taxonomy-tags">
                  <el-tag
                    v-for="tag in formattedTags"
                    :key="tag.key"
                    round
                    effect="plain"
                    class="taxonomy-tag interactive-tag"
                    @click="goToTag(tag)"
                  >
                    #{{ tag.name }}
                  </el-tag>
                </div>
              </div>
            </div>
            <el-divider />
          </div>

          <MdPreview
            editorId="preview-only"
            :modelValue="article.content"
            @onGetCatalog="onGetCatalog"
            :headingId="(index) => `heading-${index}`"
            :markedHeadingId="(index) => `heading-${index}`"
          />
        </el-card>
      </div>

      <aside class="catalog-side-panel" :class="{ 'is-open': catalogDrawerVisible }">
        <el-affix class="catalog-affix" :offset="6">
          <div
            ref="catalogPanelRef"
            class="catalog-affix-shell"
            :class="{ 'is-open': catalogDrawerVisible }"
          >
            <el-button
              class="catalog-handle"
              :class="{ 'is-open': catalogDrawerVisible }"
              type="success"
              @click="toggleCatalogDrawer"
            >
              <el-icon class="catalog-handle-main"><Collection /></el-icon>
              <el-icon class="catalog-handle-arrow" :class="{ 'is-open': catalogDrawerVisible }">
                <ArrowLeftBold />
              </el-icon>
            </el-button>

            <div class="catalog-panel-body">
              <div class="catalog-card catalog-drawer-card">
                <div class="catalog-title">
                  <span>目录导航</span>
                </div>
                <el-anchor :container="scrollContainer" :offset="70" v-if="catalogList.length > 0">
                  <el-anchor-link
                    v-for="item in catalogList"
                    :key="item.uniqueId"
                    :href="`#${item.uniqueId}`"
                    :title="item.text"
                    :class="`indent-level-${item.level}`"
                    @click.prevent="handleCatalogNavigate(item.uniqueId)"
                  />
                </el-anchor>
                <div v-else class="empty-catalog">暂无目录</div>
              </div>
            </div>
          </div>
        </el-affix>
      </aside>
    </div>
    <el-divider></el-divider>
    <!-- 评论区 -->
    <CommentsCard
      v-if="article && article.id"
      :blog-id="article.id"
      :require-login="true"
      class="comments-section"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed, watch, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeftBold, Collection, PictureRounded } from '@element-plus/icons-vue'
import { fetchArticleById } from '@/api/article.js'
import { fetchCategories } from '@/api/categories'
import { fetchTags } from '@/api/tags'
import CommentsCard from '@/components/CommentsCard.vue'
import defaultAvatar from '@/assets/(5).png'

import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'

const route = useRoute()
const router = useRouter()
const article = ref(null)
const loading = ref(false)
const error = ref(null)
const catalogList = ref([])
const catalogDrawerVisible = ref(false)
const catalogPanelRef = ref(null)
const scrollContainer = ref(null)
const tagOptions = ref([])
const categoryOptions = ref([])

const authorInitial = computed(() => {
  return (article.value?.authorNickname || 'A').slice(0, 1).toUpperCase()
})

const normalizeTags = (tags) => {
  if (!tags) return []
  if (Array.isArray(tags)) {
    return tags
      .map((item) => {
        if (typeof item === 'string' || typeof item === 'number') {
          const value = String(item).trim()
          return value
            ? { raw: value, id: /^\d+$/.test(value) ? Number(value) : null, name: value }
            : null
        }
        if (item && typeof item === 'object') {
          const id = Number(item.id ?? item.tagId)
          const name = String(item.name || item.tagName || item.label || item.id || '').trim()
          if (!name) return null
          return {
            raw: name,
            id: Number.isNaN(id) ? null : id,
            name,
          }
        }
        return null
      })
      .filter(Boolean)
  }
  if (typeof tags === 'string') {
    return tags
      .split(',')
      .map((item) => item.trim())
      .map((item) => {
        if (!item) return null
        return {
          raw: item,
          id: /^\d+$/.test(item) ? Number(item) : null,
          name: item,
        }
      })
      .filter(Boolean)
  }
  return []
}

const tagLookup = computed(() => {
  const lookup = new Map()
  tagOptions.value.forEach((item) => {
    if (item?.id != null) {
      lookup.set(Number(item.id), item)
    }
  })
  return lookup
})

const categoryLookup = computed(() => {
  const lookup = new Map()
  categoryOptions.value.forEach((item) => {
    if (item?.id != null) {
      lookup.set(Number(item.id), item)
    }
  })
  return lookup
})

const formattedTags = computed(() =>
  normalizeTags(article.value?.tags).map((tag, index) => {
    const matchedTag =
      (tag.id != null ? tagLookup.value.get(Number(tag.id)) : null) ||
      tagOptions.value.find((item) => item?.name && item.name === tag.name)
    const displayName = matchedTag?.name || tag.name
    const resolvedId = matchedTag?.id ?? tag.id
    return {
      key: `${resolvedId ?? tag.raw}-${index}`,
      id: resolvedId,
      name: displayName,
    }
  }),
)

const resolvedCategoryId = computed(() => {
  const articleCategoryId = Number(article.value?.categoryId)
  if (!Number.isNaN(articleCategoryId) && articleCategoryId > 0) {
    return articleCategoryId
  }

  const matchedCategory = categoryOptions.value.find(
    (item) => item?.name && item.name === article.value?.categoryName,
  )
  return matchedCategory?.id ?? null
})

const goToCategory = () => {
  if (!resolvedCategoryId.value) return
  router.push(`/category/${resolvedCategoryId.value}`)
}

const goToTag = (tag) => {
  if (!tag?.id) return
  router.push(`/tag/${tag.id}`)
}

const loadTaxonomyOptions = async () => {
  const [tagRes, categoryRes] = await Promise.allSettled([fetchTags(), fetchCategories()])

  if (tagRes.status === 'fulfilled') {
    tagOptions.value = Array.isArray(tagRes.value) ? tagRes.value : []
  }

  if (categoryRes.status === 'fulfilled') {
    categoryOptions.value = Array.isArray(categoryRes.value) ? categoryRes.value : []
  }
}

// list 里的 text 就是现在正文里的实际 ID
const onGetCatalog = (list) => {
  catalogList.value = list.map((item, index) => ({
    ...item,
    // 这里绝对不要用 item.text，只用 index
    uniqueId: `heading-${index}`,
  }))

  nextTick(() => {
    // 找到预览区域内所有的标题标签
    const previewEl = document.querySelector('#preview-only .md-editor-preview')
    if (previewEl) {
      const headings = previewEl.querySelectorAll('h1, h2, h3, h4, h5, h6')
      headings.forEach((el, index) => {
        // 强制覆盖原来的 ID，改为 heading-0, heading-1...
        el.setAttribute('id', `heading-${index}`)
      })
    }
  })
}

const handleCatalogNavigate = (headingId) => {
  const target = document.getElementById(headingId)
  if (!target) return

  if (scrollContainer.value && scrollContainer.value !== window) {
    const containerRect = scrollContainer.value.getBoundingClientRect()
    const targetRect = target.getBoundingClientRect()
    const top = targetRect.top - containerRect.top + scrollContainer.value.scrollTop - 70
    scrollContainer.value.scrollTo({ top, behavior: 'smooth' })
  } else {
    const top = target.getBoundingClientRect().top + window.scrollY - 70
    window.scrollTo({ top, behavior: 'smooth' })
  }

  catalogDrawerVisible.value = false
}

const toggleCatalogDrawer = () => {
  catalogDrawerVisible.value = !catalogDrawerVisible.value
}

const handleDocumentClick = (event) => {
  if (!catalogDrawerVisible.value) return

  const target = event.target
  const clickedPanel = catalogPanelRef.value?.contains(target)

  if (clickedPanel) return
  catalogDrawerVisible.value = false
}

const formatTime = (datetime) => {
  if (!datetime) return '--'
  const date = new Date(datetime)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

const loadArticle = async (articleId) => {
  error.value = null
  article.value = null
  catalogList.value = []
  catalogDrawerVisible.value = true

  if (!articleId) {
    error.value = '未找到文章ID'
    return
  }

  loading.value = true
  try {
    const [data] = await Promise.all([fetchArticleById(articleId), loadTaxonomyOptions()])
    article.value = data || null
  } catch (err) {
    error.value = '加载文章失败，请稍后再试。'
    console.error(err)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  const el = document.querySelector('.main-scroll-container')
  scrollContainer.value = el ? el : window
  document.addEventListener('click', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick)
})

watch(
  () => route.params.id,
  (articleId) => {
    loadArticle(Array.isArray(articleId) ? articleId[0] : articleId)
  },
  { immediate: true },
)
</script>

<style scoped>
.article-detail-container {
  --article-surface-bg: rgba(255, 255, 255, 0.74);
  --article-surface-border: rgba(15, 23, 42, 0.08);
  --article-surface-shadow: 0 18px 40px rgba(148, 163, 184, 0.08);
  --article-surface-radius: 24px;
  --article-surface-min-height: 380px;
  --article-content-width: 900px;
  --catalog-panel-width: 280px;
  --catalog-panel-gap: 0px;
  width: min(1180px, 100%);
  margin: 0 auto;
}

.main-content-area {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  margin-bottom: 28px;
}

.main-content-area.catalog-open {
  gap: var(--catalog-panel-gap);
}

.center {
  width: min(var(--article-content-width), 100%);
  min-width: 0;
  transition: width 0.32s ease;
}

.main-content-area.catalog-open .center {
  width: min(var(--article-content-width), 100%);
}

.catalog-side-panel {
  flex: 0 0 0;
  width: 0;
  min-width: 0;
  overflow: visible;
}

.catalog-affix,
.catalog-affix-shell {
  width: var(--catalog-panel-width);
  overflow: visible;
}

.catalog-affix-shell {
  position: fixed;
  top: 96px;
  right: 0;
  z-index: 1000;
  transform: translateX(100%);
  transition: transform 0.32s ease;
}

.catalog-affix-shell.is-open {
  transform: translateX(0);
}

.catalog-panel-body {
  width: 100%;
  overflow: hidden;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.24s ease;
}

.catalog-affix-shell.is-open .catalog-panel-body {
  opacity: 1;
  pointer-events: auto;
}

.catalog-card {
  max-height: calc(100vh - 200px);
  min-height: var(--article-surface-min-height);
  overflow-y: auto;

  border-radius: var(--article-surface-radius);
  background: var(--article-surface-bg);
  box-shadow: var(--article-surface-shadow);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  scrollbar-width: 0;
  -ms-overflow-style: none;
  &::-webkit-scrollbar {
    width: 0;
    height: 0;
  }
}

.catalog-drawer-card {
  width: var(--catalog-panel-width);
  height: 100%;
  max-height: 100%;
  min-height: auto;
}

.catalog-handle {
  position: absolute;
  top: 2px;
  left: -58px;
  z-index: 1;
  width: 30px;
  height: 25px;
  min-width: 62px;
  padding: 0;
  border: none;
  background: linear-gradient(135deg, #3fa655 0%, #2f8f47 100%);
  box-shadow: 0 16px 34px rgba(56, 151, 71, 0.24);
  display: inline-flex;
  align-items: center;
  justify-content: center;

  transition:
    transform 0.28s ease,
    box-shadow 0.28s ease;
}

:deep(.catalog-affix .el-affix--fixed) {
  width: var(--catalog-panel-width) !important;
  right: 0 !important;
  left: auto !important;
}

.catalog-handle:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 38px rgba(56, 151, 71, 0.3);
}

.catalog-handle.is-open {
  background: linear-gradient(135deg, #358e49 0%, #26713a 100%);
}

.catalog-handle :deep(.el-icon) {
  color: #fff;
}

.catalog-handle-main {
  font-size: 18px;
}

.catalog-handle-arrow {
  font-size: 14px;
  transition: transform 0.28s ease;
}

.catalog-handle-arrow.is-open {
  transform: rotate(180deg);
}

.article-content-card {
  width: 100%;
  min-height: var(--article-surface-min-height);
  --el-card-bg-color: var(--article-surface-bg);
  --el-card-border-color: var(--article-surface-border);
  background: var(--article-surface-bg);
  box-shadow: var(--article-surface-shadow);
  border: 1px solid var(--article-surface-border);
  border-radius: var(--article-surface-radius);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  transition:
    background-color 0.3s,
    border-color 0.3s;
  scroll-behavior: smooth;
  animation: fadeIn 0.5s ease-out 0.3s forwards;
  opacity: 0; /* 初始状态为透明 */
  overflow: hidden;
}

:deep(.article-content-card .el-card__body) {
  background: transparent;
  padding: 32px 36px;
}

.article-header-block {
  margin-bottom: 8px;
}

.article-kicker {
  margin: 0 0 12px;
  color: #7d8b7d;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.article-detail-title {
  margin: 0 0 24px;
  color: var(--app-text-color);
  font-size: clamp(2rem, 2.8vw, 2.8rem);
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: -0.03em;
}

.article-author-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.article-author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.article-author-info strong {
  display: block;
  color: var(--app-text-color);
  font-size: 16px;
  font-weight: 700;
}

.article-author-info p {
  margin: 4px 0 0;
  color: #8d8d8d;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.article-meta {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  color: var(--app-text-color);
  margin-left: 0;
  box-shadow: none;
  border: 0;
  transition: color 0.3s;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
}

.article-taxonomy {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 10px;
}

.taxonomy-group {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.taxonomy-group-tags {
  align-items: flex-start;
}

.taxonomy-label {
  min-width: 42px;
  color: #6b7280;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.taxonomy-tags {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.taxonomy-tag {
  --el-tag-font-size: 13px;
  --el-tag-border-color: rgba(148, 163, 184, 0.22);
  --el-tag-hover-color: transparent;
  padding: 0 12px;
  color: #52606d;
  background: rgba(255, 255, 255, 0.62);
  font-weight: 600;
}

.category-tag {
  color: #3d6c49;
  background: rgba(226, 239, 230, 0.72);
}

.interactive-tag {
  cursor: pointer;
  transition:
    transform 0.2s ease,
    background-color 0.2s ease,
    border-color 0.2s ease,
    color 0.2s ease;
}

.interactive-tag:hover {
  transform: translateY(-1px);
  border-color: rgba(109, 160, 81, 0.35);
}

.catalog-title {
  margin-bottom: 16px;
  color: var(--app-text-color);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.3;
  padding-left: 10px;
  border-left: 4px solid #389747;
}

.catalog-title-icon {
  color: #389747;
  font-size: 18px;
}
/* 目录层级缩进样式 */
:deep(.el-anchor__link) {
  font-size: 14px;
  line-height: 2;
}
.indent-level-1 {
  padding-left: 0px;
}
.indent-level-2 {
  padding-left: 15px;
}
.indent-level-3 {
  padding-left: 30px;
}
.indent-level-4 {
  padding-left: 45px;
}
.comments-section {
  width: min(var(--article-content-width), 100%);
  margin: 0 auto;
}

.comments-section :deep(.comments-card) {
  min-height: var(--article-surface-min-height);
  background: var(--article-surface-bg);
  border: 1px solid var(--article-surface-border);
  border-radius: var(--article-surface-radius);
  box-shadow: var(--article-surface-shadow);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  padding: 28px 30px;
}

:deep(#preview-only .md-editor-preview),
:deep(#preview-only .md-editor-preview-wrapper) {
  background-color: transparent !important;
}
:deep(.el-main) {
  overflow: visible !important;
}
@media screen and (max-width: 900px) {
  .main-content-area {
    display: block;
  }

  .main-content-area.catalog-open {
    gap: 0;
  }

  .main-content-area.catalog-open .center {
    width: 100%;
  }

  .catalog-side-panel {
    width: 0;
    margin-top: 0;
  }

  .catalog-side-panel.is-open {
    width: 100%;
    margin-top: 16px;
  }

  .catalog-affix,
  .catalog-affix-shell {
    width: 100%;
  }

  .catalog-affix-shell {
    top: 84px;
  }

  .catalog-handle {
    width: 56px;
    min-width: 56px;
    height: 48px;
  }

  .article-detail-title {
    margin-bottom: 18px;
  }

  .article-author-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .article-meta {
    justify-content: flex-start;
    white-space: normal;
  }

  .comments-section {
    width: 100%;
  }

  .catalog-card,
  .article-content-card,
  .comments-section :deep(.comments-card) {
    min-height: auto;
  }

  :deep(.article-content-card .el-card__body) {
    padding: 24px 20px;
  }

  .comments-section :deep(.comments-card) {
    padding: 22px 20px;
  }
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(0px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

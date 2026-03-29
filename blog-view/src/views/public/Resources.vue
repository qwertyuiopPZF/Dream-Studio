<template>
  <div class="resources-page">
    <div class="resources-shell">
      <section class="hero-panel">
        <div class="hero-copy">
          <h2>上传资源</h2>
        </div>

        <div class="hero-tools">
          <label class="hero-search-shell">
            <el-icon class="hero-search-icon"><Search /></el-icon>
            <input
              v-model.trim="searchKeyword"
              type="text"
              class="hero-search-input"
              placeholder="搜索资源名称、扩展名、上传人..."
            />
          </label>

          <button type="button" class="upload-button" :disabled="uploading" @click="openFilePicker">
            <el-icon><UploadFilled /></el-icon>
            <span>{{ uploading ? '上传中...' : canUpload ? '上传资源' : '登录后上传' }}</span>
          </button>

          <input
            ref="fileInputRef"
            class="native-file-input"
            type="file"
            :accept="acceptTypes"
            multiple
            @change="handleFileSelection"
          />

          <div
            class="drop-zone"
            :class="{ active: isDragActive, disabled: uploading || !canUpload }"
            @dragenter.prevent="handleDragEnter"
            @dragover.prevent="handleDragOver"
            @dragleave.prevent="handleDragLeave"
            @drop.prevent="handleDrop"
          >
            <el-icon class="drop-zone-icon"><UploadFilled /></el-icon>
            <div class="drop-zone-copy">
              <strong>{{ uploading ? '资源上传中...' : '拖拽资源到这里即可上传' }}</strong>
              <span>
                支持 {{ supportedFormatLabelsText }}，单文件不超过 {{ maxUploadSizeText }}。
                {{ canUpload ? '普通用户上传后将进入待审核列表。' : '登录后即可上传资源。' }}
              </span>
            </div>
          </div>

          <div v-if="uploading || uploadStatusText" class="upload-progress-card">
            <div class="upload-progress-head">
              <span>{{ uploadStatusText || '正在上传资源...' }}</span>
              <strong>{{ uploadProgress }}%</strong>
            </div>
            <div class="upload-progress-bar">
              <span class="upload-progress-fill" :style="{ width: `${uploadProgress}%` }"></span>
            </div>
          </div>
        </div>

        <div class="hero-stats">
          <div class="hero-stat-card">
            <strong>{{ publicResources.length }}</strong>
            <span>已展示资源</span>
          </div>
          <div class="hero-stat-card">
            <strong>{{ totalDownloads }}</strong>
            <span>累计下载</span>
          </div>
          <div class="hero-stat-card">
            <strong>{{ totalResourceSize }}</strong>
            <span>资源体量</span>
          </div>
          <div class="hero-stat-card" v-if="canUpload">
            <strong>{{ myPendingCount }}</strong>
            <span>我的待审核</span>
          </div>
          <div class="hero-stat-card" v-if="isAdmin">
            <strong>{{ adminPendingCount }}</strong>
            <span>待管理员处理</span>
          </div>
        </div>
      </section>

      <section v-if="canUpload" class="resource-board status-board">
        <div class="board-header">
          <div>
            <h2>我的上传</h2>
            <p>这里可以查看我上传的资源审核状态和处理备注。</p>
          </div>
          <button
            type="button"
            class="text-button"
            :disabled="mineLoading"
            @click="loadMyResources"
          >
            刷新
          </button>
        </div>

        <div class="board-summary">
          <div class="summary-card">
            <span>待审核</span>
            <strong>{{ myPendingCount }}</strong>
          </div>
          <div class="summary-card">
            <span>已通过</span>
            <strong>{{ myApprovedCount }}</strong>
          </div>
          <div class="summary-card">
            <span>未通过</span>
            <strong>{{ myRejectedCount }}</strong>
          </div>
        </div>

        <div v-if="myResources.length" class="resource-grid compact-grid">
          <article
            v-for="resource in myResources"
            :key="`mine-${resource.id}`"
            class="resource-card compact-card"
          >
            <div class="resource-card-top">
              <div
                class="resource-preview-frame compact-preview"
                :class="`is-${resource.categoryKey}`"
              >
                <img
                  v-if="resource.isPreviewable"
                  class="resource-preview"
                  :src="resource.fileUrl"
                  :alt="resource.originalName"
                  loading="lazy"
                />
                <div
                  v-else
                  class="resource-icon compact-icon"
                  :class="`is-${resource.categoryKey}`"
                >
                  <el-icon><component :is="resolveFileIcon(resource.extension)" /></el-icon>
                </div>
              </div>
            </div>

            <div class="resource-card-body compact-body">
              <div class="card-head-row">
                <h3 class="resource-name left-align">{{ resource.originalName }}</h3>
                <span class="resource-status" :class="`is-${resource.statusKey}`">{{
                  resource.statusLabel
                }}</span>
              </div>
              <div class="resource-tags left-align-tags">
                <span class="resource-badge secondary">{{ resource.extensionLabel }}</span>
              </div>
              <p class="review-note-block">
                {{ resource.reviewNote || resolveDefaultReviewNote(resource.statusKey) }}
              </p>
            </div>

            <div class="resource-card-footer compact-footer">
              <div class="resource-owner">
                <el-avatar :size="30" :src="resource.uploaderAvatar">{{
                  resource.uploaderInitial
                }}</el-avatar>
                <span>{{ resource.uploadedDateLabel }}</span>
              </div>
              <div class="resource-actions">
                <button type="button" class="card-action ghost" @click="copyResourceLink(resource)">
                  <el-icon><Link /></el-icon>
                </button>
                <button
                  type="button"
                  class="card-action ghost danger"
                  :disabled="isRemoving(resource.id)"
                  @click="removeResource(resource)"
                >
                  <el-icon><Delete /></el-icon>
                </button>
                <button
                  v-if="resource.statusKey === 'approved'"
                  type="button"
                  class="card-action primary"
                  @click="openResource(resource)"
                >
                  查看
                </button>
              </div>
            </div>
          </article>
        </div>

        <el-empty v-else :image-size="72" description="你还没有上传过资源" />
      </section>

      <section v-if="isAdmin" class="resource-board moderation-board">
        <div class="board-header">
          <div>
            <h2>资源审核</h2>
          </div>
          <div class="toolbar-group sort-group">
            <button
              v-for="item in reviewFilterOptions"
              :key="item.value"
              type="button"
              class="toolbar-chip sort-chip"
              :class="{ active: reviewStatusFilter === item.value }"
              @click="reviewStatusFilter = item.value"
            >
              {{ item.label }}
            </button>
          </div>
        </div>

        <div v-if="adminResources.length" class="review-list">
          <article
            v-for="resource in adminResources"
            :key="`review-${resource.id}`"
            class="review-card"
          >
            <div class="review-card-main">
              <div class="card-head-row review-title-row">
                <strong>{{ resource.originalName }}</strong>
                <span class="resource-status" :class="`is-${resource.statusKey}`">{{
                  resource.statusLabel
                }}</span>
              </div>
              <p class="review-meta">
                上传人：{{ resource.uploaderName }} · 分类：{{ resource.categoryLabel }} · 大小：{{
                  resource.fileSizeFormatted
                }}
              </p>
              <p class="review-meta">
                上传时间：{{ resource.uploadedAt }}
                <span v-if="resource.reviewerName"> · 审核人：{{ resource.reviewerName }}</span>
              </p>
              <p class="review-note-block admin-note">
                {{ resource.reviewNote || resolveDefaultReviewNote(resource.statusKey) }}
              </p>
            </div>

            <div class="review-card-actions">
              <button type="button" class="card-action ghost" @click="copyResourceLink(resource)">
                <el-icon><Link /></el-icon>
              </button>
              <button
                type="button"
                class="card-action ghost"
                @click="openResourcePreview(resource)"
              >
                预览
              </button>
              <button
                v-if="resource.statusKey !== 'approved'"
                type="button"
                class="card-action approve"
                :disabled="reviewingIds.includes(resource.id)"
                @click="approveResource(resource)"
              >
                通过
              </button>
              <button
                v-if="resource.statusKey !== 'rejected'"
                type="button"
                class="card-action reject"
                :disabled="reviewingIds.includes(resource.id)"
                @click="rejectResource(resource)"
              >
                驳回
              </button>
              <button
                type="button"
                class="card-action ghost danger"
                :disabled="isRemoving(resource.id)"
                @click="removeResource(resource)"
              >
                <el-icon><Delete /></el-icon>
              </button>
            </div>
          </article>
        </div>

        <el-empty v-else :image-size="72" description="当前筛选条件下没有待处理资源" />
      </section>

      <section class="resource-board public-board">
        <div class="board-header">
          <div>
            <h2>公开资源</h2>
            <p>仅展示已通过管理员审核的资源。</p>
          </div>
          <button
            type="button"
            class="text-button"
            :disabled="loading"
            @click="loadPublicResources"
          >
            刷新
          </button>
        </div>

        <div v-if="publicSections.length" class="resource-sections">
          <section v-for="section in publicSections" :key="section.key" class="resource-section">
            <div class="section-heading">
              <div class="section-title-pill">
                <span class="section-title-icon">{{ section.emoji }}</span>
                <span>{{ section.label }}</span>
              </div>
            </div>

            <div class="resource-grid">
              <article v-for="resource in section.items" :key="resource.id" class="resource-card">
                <div class="resource-card-top">
                  <div class="resource-preview-frame" :class="`is-${resource.categoryKey}`">
                    <img
                      v-if="resource.isPreviewable"
                      class="resource-preview"
                      :src="resource.fileUrl"
                      :alt="resource.originalName"
                      loading="lazy"
                    />
                    <div v-else class="resource-icon" :class="`is-${resource.categoryKey}`">
                      <el-icon><component :is="resolveFileIcon(resource.extension)" /></el-icon>
                    </div>
                  </div>
                </div>

                <div class="resource-card-body">
                  <h3 class="resource-name">{{ resource.originalName }}</h3>

                  <div class="resource-tags">
                    <span class="resource-badge secondary">{{ resource.extensionLabel }}</span>
                  </div>
                </div>

                <div class="resource-card-footer info-footer">
                  <div class="resource-meta-line">
                    <span>
                      <el-icon><Calendar /></el-icon>
                      {{ resource.uploadedDateLabel }}
                    </span>
                    <span>
                      <el-icon><Download /></el-icon>
                      {{ resource.downloadCount }}
                    </span>
                    <span>
                      <el-icon><View /></el-icon>
                      {{ resource.fileSizeFormatted }}
                    </span>
                  </div>
                  <div class="resource-owner-row">
                    <div class="resource-owner compact-owner">
                      <el-avatar :size="28" :src="resource.uploaderAvatar">{{
                        resource.uploaderInitial
                      }}</el-avatar>
                      <span>{{ resource.uploaderName }}</span>
                    </div>
                    <div class="resource-actions">
                      <button
                        type="button"
                        class="card-action ghost"
                        @click="copyResourceLink(resource)"
                      >
                        <el-icon><Link /></el-icon>
                      </button>
                      <button
                        type="button"
                        class="card-action ghost"
                        @click="copyMarkdownLink(resource)"
                      >
                        <el-icon><Document /></el-icon>
                      </button>
                      <button
                        v-if="canRemoveResource(resource)"
                        type="button"
                        class="card-action ghost danger"
                        :disabled="isRemoving(resource.id)"
                        @click="removeResource(resource)"
                      >
                        <el-icon><Delete /></el-icon>
                      </button>
                      <button
                        type="button"
                        class="card-action primary"
                        @click="openResource(resource)"
                      >
                        查看
                      </button>
                    </div>
                  </div>
                </div>
              </article>
            </div>
          </section>
        </div>

        <el-empty v-else :image-size="84" :description="emptyDescription" />
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar,
  Delete,
  Document,
  Download,
  Files,
  FolderOpened,
  Link,
  Picture,
  Search,
  UploadFilled,
  View,
} from '@element-plus/icons-vue'
import {
  deleteResourceFile,
  fetchAdminResources,
  fetchMyResources,
  fetchPublicResources,
  recordResourceDownload,
  reviewResourceFile,
  uploadResourceFile,
} from '@/api/resource'
import { useUserStore } from '@/store/user'

const MAX_RESOURCE_SIZE = 200 * 1024 * 1024
const SUPPORTED_EXTENSIONS = [
  'jpg',
  'jpeg',
  'png',
  'gif',
  'webp',
  'bmp',
  'svg',
  'zip',
  'rar',
  '7z',
  'tar',
  'gz',
  'exe',
  'msi',
  'dmg',
  'pkg',
  'pdf',
  'doc',
  'docx',
  'xls',
  'xlsx',
  'ppt',
  'pptx',
  'txt',
  'md',
]
const PREVIEWABLE_EXTENSIONS = new Set(['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp', 'svg'])
const categoryMetaMap = {
  package: { label: '软件安装包', emoji: '🧰' },
  pdf: { label: 'PDF电子书', emoji: '📕' },
  document: { label: '文档资料', emoji: '📘' },
  image: { label: '图片素材', emoji: '🖼' },
  installer: { label: '安装程序', emoji: '💿' },
  generic: { label: '其他资源', emoji: '📁' },
}
const extensionCategoryMap = {
  jpg: 'image',
  jpeg: 'image',
  png: 'image',
  gif: 'image',
  webp: 'image',
  bmp: 'image',
  svg: 'image',
  zip: 'package',
  rar: 'package',
  '7z': 'package',
  tar: 'package',
  gz: 'package',
  exe: 'installer',
  msi: 'installer',
  dmg: 'installer',
  pkg: 'installer',
  pdf: 'pdf',
  doc: 'document',
  docx: 'document',
  xls: 'document',
  xlsx: 'document',
  ppt: 'document',
  pptx: 'document',
  txt: 'document',
  md: 'document',
}
const categoryOrder = ['package', 'pdf', 'document', 'image', 'installer', 'generic']

const userStore = useUserStore()

const fileInputRef = ref(null)
const searchKeyword = ref('')
const activeCategory = ref('all')
const sortMode = ref('latest')
const reviewStatusFilter = ref('PENDING')
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadStatusText = ref('')
const isDragActive = ref(false)
const loading = ref(false)
const mineLoading = ref(false)
const reviewLoading = ref(false)
const publicResources = ref([])
const myResources = ref([])
const adminResources = ref([])
const removingIds = ref([])
const reviewingIds = ref([])

const sortOptions = [
  { value: 'latest', label: '最新上传' },
  { value: 'downloads', label: '下载最多' },
  { value: 'name', label: '名称排序' },
]

const reviewFilterOptions = [
  { value: 'PENDING', label: '待审核' },
  { value: 'APPROVED', label: '已通过' },
  { value: 'REJECTED', label: '已驳回' },
  { value: 'ALL', label: '全部' },
]

const canUpload = computed(() => userStore.isLoggedIn)
const isAdmin = computed(() => userStore.isAdmin)
const currentUserId = computed(() => Number(userStore.profile?.id || 0) || null)
const acceptTypes = computed(() => SUPPORTED_EXTENSIONS.map((item) => `.${item}`).join(','))
const supportedFormatLabelsText = computed(() =>
  SUPPORTED_EXTENSIONS.map((item) => item.toUpperCase()).join(' / '),
)
const maxUploadSizeText = computed(() => formatFileSize(MAX_RESOURCE_SIZE))
const totalDownloads = computed(() =>
  publicResources.value.reduce((total, item) => total + Number(item.downloadCount || 0), 0),
)
const totalResourceSize = computed(() =>
  formatFileSize(
    publicResources.value.reduce((total, item) => total + Number(item.fileSize || 0), 0),
  ),
)
const myPendingCount = computed(
  () => myResources.value.filter((item) => item.statusKey === 'pending').length,
)
const myApprovedCount = computed(
  () => myResources.value.filter((item) => item.statusKey === 'approved').length,
)
const myRejectedCount = computed(
  () => myResources.value.filter((item) => item.statusKey === 'rejected').length,
)
const adminPendingCount = computed(
  () => adminResources.value.filter((item) => item.statusKey === 'pending').length,
)

const categoryOptions = computed(() => {
  const counts = publicResources.value.reduce((accumulator, item) => {
    accumulator[item.categoryKey] = (accumulator[item.categoryKey] || 0) + 1
    return accumulator
  }, {})

  return [
    { key: 'all', label: '全部资源', count: publicResources.value.length },
    ...categoryOrder
      .filter((key) => counts[key])
      .map((key) => ({
        key,
        label: getCategoryLabel(key),
        count: counts[key],
      })),
  ]
})

const filteredPublicResources = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  return publicResources.value.filter((item) => {
    const matchCategory =
      activeCategory.value === 'all' || item.categoryKey === activeCategory.value
    const searchableText = [
      item.originalName,
      item.extension,
      item.uploaderName,
      item.categoryLabel,
    ]
      .join(' ')
      .toLowerCase()

    return matchCategory && (!keyword || searchableText.includes(keyword))
  })
})

const sortedPublicResources = computed(() => {
  const list = [...filteredPublicResources.value]

  switch (sortMode.value) {
    case 'downloads':
      return list.sort(
        (left, right) =>
          right.downloadCount - left.downloadCount || right.uploadedAtValue - left.uploadedAtValue,
      )
    case 'name':
      return list.sort((left, right) =>
        left.originalName.localeCompare(right.originalName, 'zh-CN'),
      )
    case 'latest':
    default:
      return list.sort((left, right) => right.uploadedAtValue - left.uploadedAtValue)
  }
})

const publicSections = computed(() => {
  const groups = new Map()

  sortedPublicResources.value.forEach((item) => {
    if (!groups.has(item.categoryKey)) {
      groups.set(item.categoryKey, [])
    }
    groups.get(item.categoryKey).push(item)
  })

  return [...groups.entries()]
    .sort((left, right) => categoryRank(left[0]) - categoryRank(right[0]))
    .map(([key, items]) => ({
      key,
      label: getCategoryLabel(key),
      emoji: getCategoryEmoji(key),
      items,
    }))
})

const activeSectionLabel = computed(() => {
  const target = categoryOptions.value.find((item) => item.key === activeCategory.value)
  return target?.label || '资源中心'
})

const activeSectionEmoji = computed(() =>
  activeCategory.value === 'all' ? '📦' : getCategoryEmoji(activeCategory.value),
)

const emptyDescription = computed(() => {
  if (loading.value) return '资源加载中...'
  if (searchKeyword.value.trim() || activeCategory.value !== 'all')
    return '当前筛选条件下没有找到资源'
  return '当前还没有通过审核的资源'
})

const extractExtension = (filename = '') => {
  const dotIndex = filename.lastIndexOf('.')
  if (dotIndex < 0 || dotIndex === filename.length - 1) return 'file'
  return filename.slice(dotIndex + 1).toLowerCase()
}

const categoryRank = (key) => {
  const index = categoryOrder.indexOf(key)
  return index >= 0 ? index : categoryOrder.length
}

const getCategoryKey = (extension) => extensionCategoryMap[extension] || 'generic'
const getCategoryLabel = (categoryKey) =>
  categoryMetaMap[categoryKey]?.label || categoryMetaMap.generic.label
const getCategoryEmoji = (categoryKey) =>
  categoryMetaMap[categoryKey]?.emoji || categoryMetaMap.generic.emoji

const formatFileSize = (size = 0) => {
  if (!Number.isFinite(size) || size <= 0) return '0 B'

  const units = ['B', 'KB', 'MB', 'GB']
  let value = size
  let unitIndex = 0

  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024
    unitIndex += 1
  }

  return `${value >= 10 || unitIndex === 0 ? value.toFixed(0) : value.toFixed(1)} ${units[unitIndex]}`
}

const formatDateTime = (value) => {
  if (!value) return '--'
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

const formatDateOnly = (value) => {
  if (!value) return '--'
  return new Date(value).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

const resolveStatusLabel = (status) => {
  const normalized = String(status || '').toUpperCase()
  if (normalized === 'APPROVED') return '已通过'
  if (normalized === 'REJECTED') return '未通过'
  return '待审核'
}

const resolveStatusKey = (status) => {
  const normalized = String(status || '').toUpperCase()
  if (normalized === 'APPROVED') return 'approved'
  if (normalized === 'REJECTED') return 'rejected'
  return 'pending'
}

const normalizeResource = (item = {}) => {
  const originalName = item.originalName || '未命名资源'
  const extension = (item.extension || extractExtension(originalName)).toLowerCase()
  const categoryKey = item.categoryKey || getCategoryKey(extension)
  const uploadedAtValue = new Date(
    item.createTime || item.reviewTime || item.updateTime || Date.now(),
  ).getTime()
  const uploaderName = item.uploaderName || 'Dream 用户'

  return {
    id: Number(item.id || 0) || 0,
    uploaderId: Number(item.uploaderId || 0) || null,
    uploaderName,
    uploaderAvatar: item.uploaderAvatar || '',
    uploaderInitial: uploaderName.slice(0, 1)?.toUpperCase() || 'D',
    originalName,
    fileUrl: item.fileUrl || '',
    displayUrl: String(item.fileUrl || '').replace(/^https?:\/\//, ''),
    fileSize: Number(item.fileSize) || 0,
    fileSizeFormatted: formatFileSize(Number(item.fileSize) || 0),
    extension,
    extensionLabel: extension.toUpperCase(),
    categoryKey,
    categoryLabel: getCategoryLabel(categoryKey),
    categoryEmoji: getCategoryEmoji(categoryKey),
    status: String(item.status || 'PENDING').toUpperCase(),
    statusLabel: resolveStatusLabel(item.status),
    statusKey: resolveStatusKey(item.status),
    reviewNote: item.reviewNote || '',
    reviewerId: Number(item.reviewerId || 0) || null,
    reviewerName: item.reviewerName || '',
    downloadCount: Number(item.downloadCount) || 0,
    uploadedAtValue,
    uploadedAt: formatDateTime(item.createTime || item.reviewTime || item.updateTime),
    uploadedDateLabel: formatDateOnly(item.createTime || item.reviewTime || item.updateTime),
    isPreviewable: PREVIEWABLE_EXTENSIONS.has(extension),
  }
}

const resolveFileIcon = (extension) => {
  if (PREVIEWABLE_EXTENSIONS.has(extension)) return Picture
  if (['zip', 'rar', '7z', 'tar', 'gz', 'exe', 'msi', 'dmg', 'pkg'].includes(extension))
    return FolderOpened
  if (['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'md'].includes(extension))
    return Document
  return Files
}

const resolveDefaultReviewNote = (statusKey) => {
  if (statusKey === 'approved') return '资源已通过审核，已在公开资源中展示。'
  if (statusKey === 'rejected') return '该资源未通过审核，请根据内容规范调整后重新上传。'
  return '资源已上传成功，正在等待管理员审核。'
}

const patchResourceInList = (listRef, resourceId, updater) => {
  listRef.value = listRef.value.map((item) => {
    if (item.id !== resourceId) return item
    return normalizeResource(updater(item))
  })
}

const patchResourceEverywhere = (resourceId, updater) => {
  patchResourceInList(publicResources, resourceId, updater)
  patchResourceInList(myResources, resourceId, updater)
  patchResourceInList(adminResources, resourceId, updater)
}

const loadPublicResources = async () => {
  loading.value = true
  try {
    const response = await fetchPublicResources()
    publicResources.value = Array.isArray(response)
      ? response.map((item) => normalizeResource(item))
      : []
  } catch (error) {
    console.error('加载公开资源失败', error)
    ElMessage.error(error.message || '加载公开资源失败，请稍后重试。')
  } finally {
    loading.value = false
  }
}

const loadMyResources = async () => {
  if (!canUpload.value) {
    myResources.value = []
    return
  }

  mineLoading.value = true
  try {
    const response = await fetchMyResources()
    myResources.value = Array.isArray(response)
      ? response.map((item) => normalizeResource(item))
      : []
  } catch (error) {
    console.error('加载我的资源失败', error)
    ElMessage.error(error.message || '加载我的资源失败，请稍后重试。')
  } finally {
    mineLoading.value = false
  }
}

const loadAdminResources = async () => {
  if (!isAdmin.value) {
    adminResources.value = []
    return
  }

  reviewLoading.value = true
  try {
    const response = await fetchAdminResources(
      reviewStatusFilter.value === 'ALL' ? undefined : reviewStatusFilter.value,
    )
    adminResources.value = Array.isArray(response)
      ? response.map((item) => normalizeResource(item))
      : []
  } catch (error) {
    console.error('加载资源审核列表失败', error)
    ElMessage.error(error.message || '加载资源审核列表失败，请稍后重试。')
  } finally {
    reviewLoading.value = false
  }
}

const refreshAll = async () => {
  const tasks = [loadPublicResources()]
  if (canUpload.value) tasks.push(loadMyResources())
  if (isAdmin.value) tasks.push(loadAdminResources())
  await Promise.all(tasks)
}

const isSupportedFile = (file) => {
  const extension = extractExtension(file?.name || '')
  return SUPPORTED_EXTENSIONS.includes(extension)
}

const validateSelectedFile = (file) => {
  if (!isSupportedFile(file)) {
    ElMessage.error(`${file.name} 暂不支持上传`)
    return false
  }

  if (file.size > MAX_RESOURCE_SIZE) {
    ElMessage.error(`${file.name} 超过 ${maxUploadSizeText.value}，无法上传`)
    return false
  }

  return true
}

const openFilePicker = () => {
  if (!canUpload.value) {
    ElMessage.warning('请先登录后再上传资源')
    return
  }
  if (uploading.value) return
  fileInputRef.value?.click()
}

const handleDragEnter = () => {
  if (!canUpload.value || uploading.value) return
  isDragActive.value = true
}

const handleDragOver = () => {
  if (!canUpload.value || uploading.value) return
  isDragActive.value = true
}

const handleDragLeave = () => {
  isDragActive.value = false
}

const uploadSingleFile = async (file, index, total) => {
  const formData = new FormData()
  formData.append('file', file)

  uploadProgress.value = 0
  uploadStatusText.value = `正在上传 ${index}/${total} · ${file.name}`

  return uploadResourceFile(formData, {
    onUploadProgress: ({ loaded, total: totalSize }) => {
      if (!totalSize) return
      uploadProgress.value = Math.min(100, Math.round((loaded / totalSize) * 100))
    },
  })
}

const uploadFiles = async (selectedFiles = []) => {
  if (!canUpload.value) {
    ElMessage.warning('请先登录后再上传资源')
    return
  }

  const files = Array.from(selectedFiles).filter((file) => validateSelectedFile(file))
  if (!files.length || uploading.value) return

  uploading.value = true
  let successCount = 0
  let pendingCount = 0
  let approvedCount = 0

  try {
    for (const [index, file] of files.entries()) {
      try {
        const response = await uploadSingleFile(file, index + 1, files.length)
        const normalized = normalizeResource(response)
        successCount += 1
        if (normalized.statusKey === 'approved') {
          approvedCount += 1
        } else {
          pendingCount += 1
        }
      } catch (error) {
        console.error('资源上传失败', error)
      }
    }

    uploadProgress.value = successCount ? 100 : 0
    uploadStatusText.value = successCount
      ? `上传完成，成功 ${successCount}/${files.length} 个文件`
      : '本次上传未成功写入资源'

    if (successCount) {
      await refreshAll()
      if (pendingCount && !approvedCount) {
        ElMessage.success(`上传成功 ${successCount} 个文件，已进入待审核列表`)
      } else if (approvedCount && !pendingCount) {
        ElMessage.success(`上传成功 ${successCount} 个文件，已直接展示到页面`)
      } else {
        ElMessage.success(`上传成功 ${successCount} 个文件，其中 ${pendingCount} 个待审核`)
      }
    }
  } finally {
    isDragActive.value = false

    window.setTimeout(() => {
      uploading.value = false
      uploadProgress.value = 0
      uploadStatusText.value = ''
    }, 600)
  }
}

const handleFileSelection = async (event) => {
  try {
    await uploadFiles(event.target?.files || [])
  } finally {
    if (event.target) event.target.value = ''
  }
}

const handleDrop = async (event) => {
  isDragActive.value = false
  await uploadFiles(event.dataTransfer?.files || [])
}

const isRemoving = (resourceId) => removingIds.value.includes(resourceId)
const canRemoveResource = (resource) =>
  isAdmin.value || (currentUserId.value && currentUserId.value === resource.uploaderId)

const copyResourceLink = async (resource) => {
  try {
    await navigator.clipboard.writeText(resource.fileUrl)
    ElMessage.success('资源链接已复制')
  } catch (error) {
    console.error('复制资源链接失败', error)
    window.prompt('当前环境不支持自动复制，请手动复制资源链接', resource.fileUrl)
  }
}

const copyMarkdownLink = async (resource) => {
  const markdown = resource.isPreviewable
    ? `![${resource.originalName}](${resource.fileUrl})`
    : `[${resource.originalName}](${resource.fileUrl})`

  try {
    await navigator.clipboard.writeText(markdown)
    ElMessage.success('引用链接已复制')
  } catch (error) {
    console.error('复制 Markdown 引用失败', error)
    window.prompt('当前环境不支持自动复制，请手动复制引用链接', markdown)
  }
}

const openResourcePreview = (resource) => {
  window.open(resource.fileUrl, '_blank', 'noopener')
}

const openResource = async (resource) => {
  window.open(resource.fileUrl, '_blank', 'noopener')

  if (resource.statusKey !== 'approved') return

  patchResourceEverywhere(resource.id, (item) => ({
    ...item,
    downloadCount: Number(item.downloadCount || 0) + 1,
  }))

  try {
    await recordResourceDownload(resource.id)
  } catch (error) {
    console.error('记录资源下载次数失败', error)
  }
}

const removeResource = async (resource) => {
  if (!canRemoveResource(resource) || isRemoving(resource.id)) return

  try {
    await ElMessageBox.confirm(
      `确定删除资源「${resource.originalName}」吗？删除后无法恢复。`,
      '删除资源',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    console.error('删除确认失败', error)
    ElMessage.error('删除确认失败，请稍后重试。')
    return
  }

  removingIds.value = [...removingIds.value, resource.id]

  try {
    await deleteResourceFile(resource.id)
    await refreshAll()
    ElMessage.success('资源已删除')
  } catch (error) {
    console.error('删除资源失败', error)
    ElMessage.error(error.message || '删除资源失败，请稍后重试。')
  } finally {
    removingIds.value = removingIds.value.filter((item) => item !== resource.id)
  }
}

const submitReview = async (resource, status, reviewNote = '') => {
  if (reviewingIds.value.includes(resource.id)) return

  reviewingIds.value = [...reviewingIds.value, resource.id]

  try {
    await reviewResourceFile(resource.id, { status, reviewNote })
    await refreshAll()
    ElMessage.success(status === 'APPROVED' ? '资源已审核通过' : '资源已驳回')
  } catch (error) {
    console.error('审核资源失败', error)
    ElMessage.error(error.message || '审核资源失败，请稍后重试。')
  } finally {
    reviewingIds.value = reviewingIds.value.filter((item) => item !== resource.id)
  }
}

const approveResource = async (resource) => {
  try {
    await ElMessageBox.confirm(
      `确认通过资源「${resource.originalName}」吗？通过后会展示在公开资源中。`,
      '审核通过',
      {
        type: 'success',
        confirmButtonText: '确认通过',
        cancelButtonText: '取消',
      },
    )
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    console.error('审核确认失败', error)
    ElMessage.error('审核确认失败，请稍后重试。')
    return
  }

  await submitReview(resource, 'APPROVED')
}

const rejectResource = async (resource) => {
  try {
    const result = await ElMessageBox.prompt(
      '可选填写驳回原因，用户会在“我的上传”里看到这条备注。',
      '驳回资源',
      {
        inputPlaceholder: '例如：资源内容与分类不符',
        confirmButtonText: '确认驳回',
        cancelButtonText: '取消',
      },
    )

    await submitReview(resource, 'REJECTED', result.value?.trim() || '')
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    console.error('驳回资源失败', error)
    ElMessage.error('驳回资源失败，请稍后重试。')
  }
}

watch(reviewStatusFilter, () => {
  if (isAdmin.value) {
    loadAdminResources()
  }
})

watch(
  () => `${userStore.isLoggedIn}-${userStore.isAdmin}-${userStore.profile?.id || ''}`,
  () => {
    refreshAll()
  },
)

onMounted(() => {
  refreshAll()
})
</script>

<style scoped>
.resources-page {
  width: 700px;
  padding: 28px 0 56px;
  box-sizing: border-box;
  display: contents;
}

.resources-shell {
  width: min(1420px, calc(70vw - 40px));
  margin: 0 auto;
  display: grid;
  gap: 22px;
  --resource-accent: #69ba78;

  --resource-accent-soft: #edf8ee;
  --resource-border: rgba(113, 142, 118, 0.18);
  --resource-card-border: rgba(196, 205, 210, 0.7);
  --resource-text: #2f3f58;
  --resource-muted: #6b7485;
  animation: pageFadeIn 0.45s ease-out forwards;
}

.hero-panel,
.resource-board {
  border: 1px solid rgba(226, 228, 236, 0.92);
  border-radius: 26px;

  box-shadow: 0 18px 38px rgba(169, 170, 197, 0.12);
  margin: 2px;
}

.hero-panel {
  padding: 30px;
}

.hero-copy {
  display: grid;
  gap: 12px;
}

.hero-copy h1,
.board-header h2 {
  margin: 0;
  color: var(--resource-text);
  font-size: clamp(28px, 3vw, 40px);
  line-height: 1.12;
}

.hero-copy p,
.board-header p,
.drop-zone-copy span,
.review-meta,
.review-note-block {
  margin: 0;
  color: var(--resource-muted);
  font-size: 14px;
  line-height: 1.8;
}

.hero-tools {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 14px;
  margin-top: 24px;
}

.hero-search-shell {
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 60px;
  padding: 0 18px;
  border: 1px solid rgba(198, 205, 225, 0.9);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.86);
}

.hero-search-icon {
  color: #9ca4bb;
  font-size: 18px;
}

.hero-search-input {
  width: 100%;
  border: none;
  outline: none;
  background: transparent;
  color: var(--resource-text);
  font-size: 15px;
}

.hero-search-input::placeholder {
  color: #9ea8ba;
}

.upload-button,
.toolbar-chip,
.card-action,
.text-button {
  border: none;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    color 0.2s ease,
    border-color 0.2s ease;
}

.upload-button:hover,
.toolbar-chip:hover,
.card-action:hover,
.text-button:hover {
  transform: translateY(-1px);
}

.upload-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-width: 166px;
  min-height: 60px;
  padding: 0 20px;
  border-radius: 18px;
  background: linear-gradient(135deg, #7bbe87 0%, #57a868 100%);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  box-shadow: 0 14px 26px rgba(89, 157, 101, 0.22);
}

.upload-button:disabled,
.card-action:disabled,
.text-button:disabled {
  cursor: not-allowed;
  opacity: 0.72;
  transform: none;
}

.native-file-input {
  display: none;
}

.category-group,
.drop-zone,
.upload-progress-card {
  grid-column: 1 / -1;
}

.toolbar-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.toolbar-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 40px;
  padding: 0 16px;
  border-radius: 14px;
  background: linear-gradient(180deg, #f9fafc 0%, #f1f4fa 100%);
  color: #65718a;
  font-size: 14px;
  font-weight: 600;
  box-shadow: inset 0 0 0 1px rgba(202, 208, 220, 0.75);
}

.toolbar-chip em {
  font-style: normal;
  color: #93a0b6;
}

.toolbar-chip.active {
  background: linear-gradient(180deg, #ffffff 0%, #eef5ff 100%);
  color: #4c86f7;
  box-shadow: inset 0 0 0 1px rgba(87, 150, 255, 0.68);
}

.sort-chip.active {
  color: #2f3f58;
}

.drop-zone {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  border: 1.5px dashed rgba(111, 166, 120, 0.42);
  border-radius: 20px;
  background: linear-gradient(180deg, rgba(246, 252, 246, 0.96), rgba(240, 248, 241, 0.96));
}

.drop-zone.active {
  border-color: #5aa66a;
  background: linear-gradient(180deg, rgba(236, 249, 236, 0.98), rgba(228, 245, 230, 0.98));
}

.drop-zone.disabled {
  opacity: 0.72;
}

.drop-zone-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(123, 190, 135, 0.24), rgba(92, 168, 107, 0.18));
  color: var(--resource-accent-strong);
  font-size: 24px;
}

.drop-zone-copy {
  display: grid;
  gap: 4px;
}

.drop-zone-copy strong {
  color: #34513e;
  font-size: 15px;
}

.upload-progress-card {
  padding: 16px 18px;
  border: 1px solid rgba(180, 195, 185, 0.55);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
}

.upload-progress-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
  color: #4b576a;
  font-size: 14px;
}

.upload-progress-bar {
  height: 8px;
  border-radius: 999px;
  background: rgba(123, 190, 135, 0.15);
  overflow: hidden;
}

.upload-progress-fill {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #8bd391 0%, #53a764 100%);
}

.hero-stats,
.board-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 14px;
  margin-top: 22px;
}

.hero-stat-card,
.summary-card {
  min-height: 86px;
  padding: 16px 18px;
  border: 1px solid rgba(210, 216, 228, 0.92);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.86);
}

.hero-stat-card strong,
.summary-card strong {
  display: block;
  color: var(--resource-text);
  font-size: 24px;
}

.hero-stat-card span,
.summary-card span {
  display: block;
  margin-top: 8px;
  color: #79849a;
  font-size: 13px;
}

.resource-board {
  padding: 28px 30px;
}

.board-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.text-button {
  padding: 0;
  background: transparent;
  color: #5c87e9;
  font-size: 14px;
  font-weight: 700;
}

.resource-sections {
  display: grid;
  gap: 28px;
  margin-top: 24px;
}

.resource-section {
  display: grid;
  gap: 18px;
}

.section-heading {
  display: flex;
  align-items: center;
}

.section-title-pill {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-height: 60px;
  padding: 0 22px;
  border: 1px solid rgba(208, 216, 234, 0.9);
  border-radius: 18px;
  background: linear-gradient(180deg, #edf2ff 0%, #f6f8ff 100%);
  color: #334461;
  font-size: 16px;
  font-weight: 700;
}

.hero-pill {
  width: fit-content;
}

.section-title-icon {
  font-size: 21px;
}

.resource-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 2fr));
}

.compact-grid {
  margin-top: 22px;
  gap: 15px;
}

.resource-card {
  display: flex;
  flex-direction: column;
  min-height: 314px;
  border: 1px solid var(--resource-card-border);
  border-radius: 16px;
  background: #fff;
  overflow: hidden;
}

.compact-card {
  min-height: 252px;
}

.resource-card-top {
  padding: 18px 18px 0;
}

.resource-card-body {
  padding: 18px 18px 0;
}

.compact-body {
  padding-top: 16px;
}

.resource-preview-frame {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 126px;
  border-radius: 14px;
  overflow: hidden;
  background: linear-gradient(180deg, #f2f6ff 0%, #edf1fb 100%);
}

.compact-preview {
  height: 96px;
}

.resource-preview-frame.is-package,
.resource-preview-frame.is-installer {
  background: linear-gradient(180deg, #f2f6ff 0%, #eef2fb 100%);
}

.resource-preview-frame.is-pdf,
.resource-preview-frame.is-document {
  background: linear-gradient(180deg, #fff4f5 0%, #fff8f7 100%);
}

.resource-preview-frame.is-image {
  background: linear-gradient(180deg, #eef8ed 0%, #f7fcf5 100%);
}

.resource-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.resource-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 72px;
  height: 72px;
  border-radius: 18px;
  font-size: 38px;
}

.compact-icon {
  width: 54px;
  height: 54px;
  font-size: 28px;
}

.resource-icon.is-image {
  background: linear-gradient(135deg, #dff0da 0%, #edf8e9 100%);
  color: #4d8e59;
}

.resource-icon.is-package,
.resource-icon.is-installer {
  background: linear-gradient(135deg, #e4ecff 0%, #f1f5ff 100%);
  color: #4c86f7;
}

.resource-icon.is-pdf,
.resource-icon.is-document,
.resource-icon.is-generic {
  background: linear-gradient(135deg, #ffe7e7 0%, #fff3f0 100%);
  color: #e35f5f;
}

.resource-name {
  margin: 0;
  color: var(--resource-text);
  font-size: 16px;
  line-height: 1.6;
  text-align: center;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.left-align {
  text-align: left;
}

.card-head-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.review-title-row {
  align-items: center;
}

.resource-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 12px;
}

.left-align-tags {
  justify-content: flex-start;
}

.resource-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
}

.resource-badge.free {
  border: 1px solid #bce593;
  color: #70a83d;
  background: #f8fff1;
}

.resource-badge.secondary {
  background: #f0f4ed;
  color: #768176;
}

.resource-status {
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.resource-status.is-pending {
  background: #fff7e5;
  color: #c08a1f;
}

.resource-status.is-approved {
  background: #edf9ef;
  color: #38914a;
}

.resource-status.is-rejected {
  background: #fff0f0;
  color: #d45a5a;
}

.resource-card-footer {
  margin-top: auto;
  padding: 14px 18px 16px;
  border-top: 1px solid rgba(228, 230, 238, 0.92);
}

.info-footer {
  display: grid;
  gap: 12px;
}

.compact-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.resource-meta-line {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  color: #6f7788;
  font-size: 13px;
}

.resource-meta-line span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.resource-owner-row,
.resource-owner,
.resource-actions,
.review-card-actions {
  display: flex;
  align-items: center;
}

.resource-owner-row {
  justify-content: space-between;
  gap: 12px;
}

.resource-owner,
.compact-owner {
  gap: 10px;
  color: #535d73;
  font-size: 14px;
  font-weight: 600;
}

.resource-actions,
.review-card-actions {
  gap: 8px;
}

.card-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 38px;
  height: 38px;
  padding: 0 12px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 700;
}

.card-action.ghost {
  background: #f2f5fb;
  color: #63738f;
}

.card-action.ghost.danger {
  background: #fff1f1;
  color: #d86558;
}

.card-action.primary,
.card-action.approve {
  min-width: 66px;
  background: linear-gradient(135deg, #79bf88 0%, #57a868 100%);
  color: #fff;
}

.card-action.reject {
  min-width: 66px;
  background: #fff3f3;
  color: #d45a5a;
}

.review-list {
  display: grid;
  gap: 14px;
  margin-top: 22px;
}

.review-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 20px;
  border: 1px solid rgba(220, 225, 235, 0.9);
  border-radius: 18px;
  background: linear-gradient(180deg, #fff 0%, #fafbff 100%);
}

.review-card-main {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.review-card-main strong {
  color: var(--resource-text);
  font-size: 16px;
}

.review-note-block {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f7f9fc;
}

.admin-note {
  margin-top: 6px;
}

@media (max-width: 1320px) {
  .resource-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 1100px) {
  .hero-tools {
    grid-template-columns: 1fr;
  }

  .upload-button {
    width: 100%;
  }

  .resource-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .review-card {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 720px) {
  .resources-shell {
    width: calc(100vw - 20px);
  }

  .hero-panel,
  .resource-board {
    padding: 20px 16px;
    border-radius: 22px;
  }

  .hero-copy h1,
  .board-header h2 {
    font-size: 28px;
  }

  .drop-zone {
    flex-direction: column;
    align-items: flex-start;
  }

  .resource-grid {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .board-header,
  .resource-owner-row,
  .compact-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .resource-actions,
  .review-card-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .card-action.primary,
  .card-action.approve,
  .card-action.reject {
    flex: 1 1 96px;
  }
}

@keyframes pageFadeIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>

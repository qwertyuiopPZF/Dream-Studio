<template>
  <div>
    <el-card shadow="never">
      <el-form
        :model="articleForm"
        :rules="rules"
        ref="articleFormRef"
        label-position="top"
      >
        <el-form-item label="文章标题" prop="title">
          <el-input v-model="articleForm.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="文章内容" prop="content">
          <!-- 
          MdEditor 配置说明：
          1. v-model: 双向绑定内容
          2. @onUploadImg: 监听图片上传（包括点击上传按钮 和 截图粘贴）
          3. preview-theme: 预览主题 (推荐 'github' 或 'smart-blue')
          4. code-theme: 代码高亮主题 (推荐 'atom' 或 'github')
          5. toolbars:以此配置显示的工具栏
        -->
          <MdEditor
            v-model="articleForm.content"
            placeholder="支持 Markdown 语法，可直接截图粘贴图片..."
            preview-theme="default"
            code-theme="atom"
            @onUploadImg="handleUploadImage"
            style="height: calc(100vh - 100px)"
          />
        </el-form-item>

        <!-- Sidebar Area -->

        <template #header>
          <span>发布设置</span>
        </template>

        <el-form-item label="文章封面">
          <div class="cover-field">
            <el-upload
              class="cover-uploader"
              accept="image/*"
              :show-file-list="false"
              :http-request="handleCoverUpload"
              :before-upload="beforeCoverUpload"
            >
              <div v-if="coverPreviewUrl" class="cover-preview-card">
                <img :src="coverPreviewUrl" alt="文章封面" class="cover-preview-image" />
                <div class="cover-preview-mask">
                  <span>{{ coverUploading ? '上传中...' : '重新上传' }}</span>
                </div>
              </div>
              <div v-else class="cover-upload-placeholder">
                <el-icon class="cover-upload-icon"><Plus /></el-icon>
                <span>{{ coverUploading ? '封面上传中...' : '上传本地封面图' }}</span>
              </div>
            </el-upload>

            <div class="cover-field__actions">
              <span class="cover-field__hint">支持 JPG / PNG / WEBP / GIF，大小不超过 10MB</span>
              <el-button v-if="articleForm.coverImage" text type="danger" @click="removeCoverImage">
                移除封面
              </el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="文章摘要" prop="summary">
          <el-input
            type="textarea"
            v-model="articleForm.summary"
            :autosize="{ minRows: 3, maxRows: 10 }"
            placeholder="请输入文章摘要"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select
                v-model="articleForm.categoryId"
                placeholder="请选择分类"
              >
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签" prop="tags">
              <el-select
                v-model="articleForm.tags"
                multiple
                placeholder="请选择标签"
                tag="success"
                :popper-append-to-body="false"
              >
                <el-option
                  v-for="tag in tagsList"
                  :key="tag.id"
                  :label="tag.name"
                  :value="tag.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="允许评论">
          <el-switch
            v-model="articleForm.isComment"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>

        <el-form-item>
          <el-button @click="saveDraft" :loading="loading || coverUploading">保存草稿</el-button>
          <el-button type="primary" @click="publishArticle" :loading="loading || coverUploading"
            >发布文章</el-button
          >
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch, defineProps } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { fetchCategories } from '@/api/categories'
import { fetchTags } from '@/api/tags'
import {
  createManagedArticle,
  fetchManagedArticleById,
  updateManagedArticle,
} from '@/api/article'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { uploadImage } from '@/api/upload'
import { useWorkspaceRouteBase } from '@/composables/useWorkspaceRouteBase'

const router = useRouter()
const articleFormRef = ref(null)
const loading = ref(false)
const coverUploading = ref(false)
const route = useRoute()
const { routeBase } = useWorkspaceRouteBase()
const uploadBaseUrl = import.meta.env.VITE_APP_UPLOAD_URL || ''

const articleForm = ref({
  id: null,
  title: '',
  content: '',
  summary: '',
  categoryId: null,
  tags: [],
  coverImage: '',
  isComment: 1,
  status: 0,
  publishTime: null,
})

const categories = ref([])
const tagsList = ref([])

const resolveCoverImage = (coverImage) => {
  if (!coverImage) return ''
  return coverImage.startsWith('http') ? coverImage : `${uploadBaseUrl}${coverImage}`
}

const coverPreviewUrl = computed(() => resolveCoverImage(articleForm.value.coverImage))

const props = defineProps({
  initData: {
    type: Object,
    default: () => null,
  },
})

watch(
  () => props.initData,
  (newData) => {
    if (newData) {
      articleForm.value.title = newData.title
      articleForm.value.summary = newData.summary
      articleForm.value.content = newData.content
      articleForm.value.coverImage = newData.coverImage || articleForm.value.coverImage
    }
  },

  {
    immediate: true,
    deep: true,
  }
)

const rules = ref({
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章内容', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
})

const submitArticle = async (status) => {
  if (coverUploading.value) {
    ElMessage.warning('封面上传中，请稍候再提交文章')
    return
  }

  loading.value = true
  const articleData = {
    ...articleForm.value,
    tags: articleForm.value.tags.join(','),
    status,
  }

  try {
    if (articleForm.value.id) {
      await updateManagedArticle(articleData.id, articleData)
      ElMessage.success(status === 1 ? '文章更新成功' : '草稿已更新')
    } else {
      await createManagedArticle(articleData)
      ElMessage.success(status === 1 ? '文章发布成功' : '草稿保存成功')
    }

    router.push(`${routeBase.value}/articlemgmt`)
  } catch (error) {
    console.error('Failed to submit article:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    categories.value = await fetchCategories()
    tagsList.value = await fetchTags()
  } catch (error) {
    console.error('Failed to load categories or tags:', error)
    ElMessage.error('加载分类或标签失败')
  }

  const articleId = route.params.id
  if (articleId) {
    await loadArticleDetail(articleId)
  }
})

const saveDraft = () => {
  submitArticle(0)
}

const publishArticle = () => {
  articleFormRef.value.validate((valid) => {
    if (valid) {
      submitArticle(1)
    } else {
      ElMessage.error('请填写所有必填项')
      return false
    }
  })
}

const loadArticleDetail = async (id) => {
  try {
    const data = await fetchManagedArticleById(id)

    articleForm.value = {
      id: data.id,
      title: data.title,
      content: data.content,
      summary: data.summary,
      coverImage: data.coverImage,
      categoryId: data.categoryId, // 确保类型一致(Number/String)
      isComment: data.isComment,
      status: data.status,
      tags: Array.isArray(data.tags)
        ? data.tags
        : String(data.tags || '')
            .split(',')
            .map((item) => item.trim())
            .filter(Boolean)
            .map((item) => Number(item) || item),
    }
  } catch (error) {
    console.error('获取详情失败', error)
  }
}

const beforeCoverUpload = (rawFile) => {
  const isImage = rawFile.type?.startsWith('image/')
  const isWithinLimit = rawFile.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('封面文件必须是图片格式')
    return false
  }

  if (!isWithinLimit) {
    ElMessage.error('封面图片大小不能超过 10MB')
    return false
  }

  return true
}

const handleCoverUpload = async (options) => {
  coverUploading.value = true

  try {
    const formData = new FormData()
    formData.append('file', options.file)

    const response = await uploadImage(formData)
    const relativePath = typeof response === 'string' ? response : response?.data || response?.url

    if (!relativePath) {
      throw new Error('上传接口未返回封面地址')
    }

    articleForm.value.coverImage = relativePath
    options.onSuccess?.(response)
    ElMessage.success('封面上传成功')
  } catch (error) {
    console.error('封面上传失败', error)
    options.onError?.(error)
    ElMessage.error('封面上传失败，请重试')
  } finally {
    coverUploading.value = false
  }
}

const removeCoverImage = () => {
  articleForm.value.coverImage = ''
}

/**
 * @param {Array<File>} files
 * @param {Function} callback
 */
const handleUploadImage = async (files, callback) => {
  try {
    const res = await Promise.all(
      files.map((file) => {
        return new Promise((resolve, reject) => {
          const formData = new FormData()
          formData.append('file', file)

          uploadImage(formData)
            .then((response) => {
              const relativePath = typeof response === 'string' ? response : response?.data || response?.url

              if (relativePath) {
                resolve(relativePath.startsWith('http') ? relativePath : `${uploadBaseUrl}${relativePath}`)
              } else {
                reject(new Error('返回结果中没有URL'))
              }
            })
            .catch((err) => {
              console.error('单个图片上传失败', err)
              reject(err)
            })
        })
      })
    )

    callback(res)
  } catch (error) {
    console.error('图片上传过程中出现错误:', error)
    ElMessage.error('图片上传失败，请重试')
  }
}
</script>

<style scoped>
.cover-field {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.cover-uploader {
  width: 100%;
}

.cover-uploader :deep(.el-upload) {
  display: block;
  width: 100%;
  cursor: pointer;
}

.cover-preview-card,
.cover-upload-placeholder {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 208px;
  border: 1px dashed #cbd5e1;
  border-radius: 20px;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(241, 245, 249, 0.92), rgba(255, 247, 237, 0.95));
  transition:
    border-color 0.2s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.cover-preview-card:hover,
.cover-upload-placeholder:hover {
  border-color: #60a5fa;
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(148, 163, 184, 0.16);
}

.cover-preview-image {
  display: block;
  width: 100%;
  height: 208px;
  object-fit: cover;
}

.cover-preview-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.38);
  color: #fff;
  font-size: 0.96rem;
  font-weight: 600;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.cover-preview-card:hover .cover-preview-mask {
  opacity: 1;
}

.cover-upload-placeholder {
  flex-direction: column;
  gap: 12px;
  color: #475569;
}

.cover-upload-icon {
  font-size: 30px;
  color: #3b82f6;
}

.cover-field__actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.cover-field__hint {
  color: #64748b;
  font-size: 0.88rem;
}

.markdown-editor {
  width: 100%;
  height: 100%;
}

/* 修复全屏编辑时的层级问题 */
.md-editor {
  z-index: 999 !important;
}

@media (max-width: 767px) {
  .cover-preview-card,
  .cover-upload-placeholder {
    min-height: 180px;
  }

  .cover-preview-image {
    height: 180px;
  }
}
</style>

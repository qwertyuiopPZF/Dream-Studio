<template>
  <el-input
    v-model="form.title"
    class="composer-title"
    maxlength="80"
    placeholder="输入一个清晰的帖子标题"
    show-word-limit
  />

  <el-input
    v-model="form.summary"
    class="composer-summary"
    maxlength="160"
    placeholder="用一句话概括你的分享内容，便于在列表中快速浏览"
    show-word-limit
  />

  <div class="taxonomy-row">
    <el-select
      v-model="form.categoryId"
      class="taxonomy-select"
      placeholder="选择分类"
      clearable
      filterable
      :loading="taxonomyLoading"
      :disabled="taxonomyLoading || !categories.length"
      no-data-text="暂无可用分类，请联系管理员添加"
    >
      <el-option
        v-for="category in categories"
        :key="category.id"
        :label="category.name"
        :value="category.id"
      />
    </el-select>

    <el-select
      v-model="form.tags"
      class="taxonomy-select"
      multiple
      clearable
      filterable
      collapse-tags
      collapse-tags-tooltip
      placeholder="选择标签"
      :loading="taxonomyLoading"
      :disabled="taxonomyLoading || !tagsList.length"
      no-data-text="暂无可用标签，请联系管理员添加"
    >
      <el-option v-for="tag in tagsList" :key="tag.id" :label="tag.name" :value="tag.id" />
    </el-select>
  </div>

  <el-input
    v-model="form.content"
    class="composer-content"
    type="textarea"
    :rows="10"
    maxlength="4000"
    resize="vertical"
    placeholder="写下你的想法、问题或经验，也可以在内容里自然加入标签关键词。"
    show-word-limit
  />

  <div class="action-row">
    <el-button :loading="submitting" type="primary" round @click="submitPost">发布帖子</el-button>
    <el-button round @click="resetDraft">清空内容</el-button>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchCategories } from '@/api/categories'
import { createForumPost } from '@/api/forum'
import { fetchTags } from '@/api/tags'
import defaultAvatar from '@/assets/(5).png'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const submitting = ref(false)
const form = reactive({
  nickname: '',
  email: '',
  avatar: '',
  title: '',
  summary: '',
  categoryId: null,
  tags: [],
  content: '',
})
const categories = ref([])
const tagsList = ref([])
const taxonomyLoading = ref(false)

const currentProfile = computed(() => userStore.profile || {})
const composerNickname = computed(
  () => currentProfile.value.nickname || currentProfile.value.username || 'Dream 用户',
)

const syncFormUser = () => {
  form.nickname = currentProfile.value.nickname || currentProfile.value.username || ''
  form.email = currentProfile.value.email || ''
  form.avatar = currentProfile.value.avatar || ''
}

watch(currentProfile, syncFormUser, { immediate: true, deep: true })

const resetDraft = () => {
  form.title = ''
  form.summary = ''
  form.categoryId = null
  form.tags = []
  form.content = ''
}

const submitPost = async () => {
  if (!form.title.trim()) {
    ElMessage.warning('请先填写标题')
    return
  }

  if (!form.content.trim()) {
    ElMessage.warning('请先填写正文')
    return
  }

  if (!form.categoryId) {
    ElMessage.warning('请先选择分类')
    return
  }

  submitting.value = true
  try {
    syncFormUser()
    const createdPost = await createForumPost({
      ...form,
      tags: form.tags.join(','),
    })
    const createdPostId = createdPost?.id || createdPost?.data?.id
    resetDraft()
    ElMessage.success('帖子发布成功')
    await router.push(createdPostId ? `/forum/${createdPostId}` : '/forum')
  } catch (error) {
    console.error('发布帖子失败', error)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  taxonomyLoading.value = true
  try {
    const [categoryData, tagData] = await Promise.all([fetchCategories(), fetchTags()])
    categories.value = Array.isArray(categoryData) ? categoryData : []
    tagsList.value = Array.isArray(tagData) ? tagData : []
  } catch (error) {
    console.error('加载分类或标签失败', error)
    ElMessage.error('加载分类或标签失败，请稍后重试')
  } finally {
    taxonomyLoading.value = false
  }
})
</script>

<style scoped>
.forum-manager-card {
  padding: 28px;
  border-radius: 20px;
  background:
    radial-gradient(circle at top right, rgba(14, 165, 233, 0.12), transparent 34%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 252, 0.98));
  border: 1px solid rgba(148, 163, 184, 0.2);
}

.manager-copy {
  margin-bottom: 24px;
}

.manager-eyebrow {
  margin: 0 0 10px;
  font-size: 12px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #0f766e;
}

.manager-title-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.manager-title-row h2 {
  margin: 0 0 10px;
  color: #0f172a;
}

.manager-title-row p {
  margin: 0;
  max-width: 640px;
  line-height: 1.75;
  color: #475569;
}

.manager-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.55fr) minmax(260px, 0.95fr);
  gap: 18px;
}

.composer-panel,
.tips-card {
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(226, 232, 240, 0.95);
}

.composer-panel {
  padding: 22px;
}

.author-card {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 18px;
  background: #f8fafc;
}

.author-card strong {
  display: block;
  color: #0f172a;
}

.author-card p {
  margin: 6px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.composer-title,
.composer-summary,
.composer-content {
  margin-bottom: 14px;
}

.taxonomy-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.taxonomy-select {
  width: 100%;
}

.taxonomy-help {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: -4px 0 14px;
  color: #64748b;
  font-size: 13px;
}

.quick-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.quick-toolbar span {
  color: #64748b;
  font-size: 14px;
}

.quick-chip {
  border: 0;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(15, 118, 110, 0.1);
  color: #0f766e;
  cursor: pointer;
  transition:
    transform 0.2s ease,
    background 0.2s ease;
}

.quick-chip:hover {
  transform: translateY(-1px);
  background: rgba(15, 118, 110, 0.16);
}

.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tips-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.tips-card {
  padding: 20px;
}

.tips-card h3 {
  margin: 0 0 12px;
  color: #0f172a;
}

.tips-card ul {
  margin: 0;
  padding-left: 18px;
  color: #475569;
  line-height: 1.8;
}

.tips-card p {
  margin: 0;
  color: #475569;
  line-height: 1.8;
}

.accent-card {
  background: linear-gradient(180deg, rgba(240, 253, 250, 0.96), rgba(236, 252, 203, 0.92));
}

@media (max-width: 900px) {
  .forum-manager-card {
    padding: 22px;
  }

  .manager-grid {
    grid-template-columns: 1fr;
  }

  .taxonomy-row {
    grid-template-columns: 1fr;
  }

  .manager-title-row {
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .forum-manager-card {
    padding: 18px;
    border-radius: 24px;
  }

  .composer-panel,
  .tips-card {
    padding: 16px;
  }

  .author-card {
    align-items: flex-start;
  }
}
</style>

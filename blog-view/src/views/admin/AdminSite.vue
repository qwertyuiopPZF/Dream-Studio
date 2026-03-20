<template>
  <section class="site-shell">
    <div class="site-head">
      <div>
        <h2>站点配置</h2>
        <p>把 `blog-cms` 里的分类、标签管理与 Forum 入口收进同一个站点配置页。</p>
      </div>
      <el-button round :loading="loading" @click="loadAll">刷新配置</el-button>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="分类管理" name="categories">
        <div class="toolbar">
          <el-button type="primary" plain @click="openCategoryDialog()">新增分类</el-button>
        </div>
        <el-table :data="categories" table-layout="fixed">
          <el-table-column type="index" label="#" width="60" />
          <el-table-column prop="name" label="分类名称" />
          <el-table-column prop="articleCount" label="关联文章数" width="120" />
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button text type="primary" @click="openCategoryDialog(row)">编辑</el-button>
              <el-button text type="danger" @click="removeCategory(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="标签管理" name="tags">
        <div class="toolbar">
          <el-button type="primary" plain @click="openTagDialog()">新增标签</el-button>
        </div>
        <el-table :data="tags" table-layout="fixed">
          <el-table-column type="index" label="#" width="60" />
          <el-table-column label="标签名称">
            <template #default="{ row }">
              <el-tag effect="plain">{{ row.name }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="articleCount" label="关联文章数" width="120" />
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="{ row }">
              <el-button text type="primary" @click="openTagDialog(row)">编辑</el-button>
              <el-button text type="danger" @click="removeTag(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="Forum 入口" name="forum">
        <div class="forum-panel">
          <article class="forum-card">
            <h3>前台 Forum</h3>
            <p>直接进入论坛首页，查看帖子流、热门标签和当前发帖体验。</p>
            <el-button type="primary" plain @click="goToForum">进入 Forum</el-button>
          </article>

          <article class="forum-card">
            <h3>个人中心审核</h3>
            <p>论坛帖子处理、举报审核和用户通知已经集中到个人中心与管理页。</p>
            <el-button plain @click="goToProfile">前往个人中心</el-button>
          </article>
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="categoryDialog.visible" :title="categoryDialog.form.id ? '编辑分类' : '新增分类'" width="420px">
      <el-form label-position="top">
        <el-form-item label="分类名称">
          <el-input v-model="categoryDialog.form.name" maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="categoryDialog.loading" @click="submitCategory">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="tagDialog.visible" :title="tagDialog.form.id ? '编辑标签' : '新增标签'" width="420px">
      <el-form label-position="top">
        <el-form-item label="标签名称">
          <el-input v-model="tagDialog.form.name" maxlength="20" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="tagDialog.loading" @click="submitTag">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createAdminCategory,
  createAdminTag,
  deleteAdminCategory,
  deleteAdminTag,
  fetchAdminCategories,
  fetchAdminTags,
  updateAdminCategory,
  updateAdminTag,
} from '@/api/admin'

const router = useRouter()
const route = useRoute()
const activeTab = ref('categories')
const loading = ref(false)
const categories = ref([])
const tags = ref([])

const categoryDialog = reactive({
  visible: false,
  loading: false,
  form: { id: null, name: '' },
})

const tagDialog = reactive({
  visible: false,
  loading: false,
  form: { id: null, name: '' },
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

const loadAll = async () => {
  loading.value = true
  try {
    const [categoryRes, tagRes] = await Promise.all([
      fetchAdminCategories(),
      fetchAdminTags(),
    ])
    categories.value = categoryRes || []
    tags.value = tagRes || []
  } finally {
    loading.value = false
  }
}

loadAll()

watch(
  () => route.path,
  (path) => {
    if (path.startsWith('/admin/tagsmgmt')) {
      activeTab.value = 'tags'
      return
    }

    if (path.startsWith('/admin/forum-entry')) {
      activeTab.value = 'forum'
      return
    }

    activeTab.value = 'categories'
  },
  { immediate: true },
)

const openCategoryDialog = (row = null) => {
  categoryDialog.form = row ? { id: row.id, name: row.name } : { id: null, name: '' }
  categoryDialog.visible = true
}

const submitCategory = async () => {
  if (!categoryDialog.form.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  categoryDialog.loading = true
  try {
    const payload = { ...categoryDialog.form, name: categoryDialog.form.name.trim() }
    if (payload.id) {
      await updateAdminCategory(payload)
    } else {
      await createAdminCategory(payload)
    }
    categoryDialog.visible = false
    await loadAll()
  } finally {
    categoryDialog.loading = false
  }
}

const removeCategory = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除分类「${row.name}」吗？`, '删除分类', { type: 'warning' })
    await deleteAdminCategory(row.id)
    await loadAll()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const openTagDialog = (row = null) => {
  tagDialog.form = row ? { id: row.id, name: row.name } : { id: null, name: '' }
  tagDialog.visible = true
}

const submitTag = async () => {
  if (!tagDialog.form.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  tagDialog.loading = true
  try {
    const payload = { ...tagDialog.form, name: tagDialog.form.name.trim() }
    if (payload.id) {
      await updateAdminTag(payload)
    } else {
      await createAdminTag(payload)
    }
    tagDialog.visible = false
    await loadAll()
  } finally {
    tagDialog.loading = false
  }
}

const removeTag = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除标签「${row.name}」吗？`, '删除标签', { type: 'warning' })
    await deleteAdminTag(row.id)
    await loadAll()
  } catch (error) {
    if (error !== 'cancel') console.error(error)
  }
}

const goToForum = () => {
  router.push('/forum')
}

const goToProfile = () => {
  router.push('/profile')
}
</script>

<style scoped>
.site-shell {
  padding: 22px;
  border-radius: 26px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.88);
}

.site-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.site-head h2 {
  margin: 0 0 8px;
}

.site-head p {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.toolbar {
  margin-bottom: 14px;
}

.forum-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.forum-card {
  padding: 20px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(248, 250, 252, 0.85);
}

.forum-card h3 {
  margin: 0 0 8px;
}

.forum-card p {
  margin: 0 0 18px;
  color: #64748b;
  line-height: 1.7;
}

@media (max-width: 720px) {
  .site-head {
    flex-direction: column;
  }

  .forum-panel {
    grid-template-columns: 1fr;
  }
}
</style>

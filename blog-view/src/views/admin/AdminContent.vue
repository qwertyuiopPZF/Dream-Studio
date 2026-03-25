<template>
  <section class="content-shell">
    <div class="content-head">
      <div>
        <h2>内容管理面板</h2>
        <p>文章、评论、动态沿用后台管理能力，但已经统一进入 `blog-view`。</p>
      </div>
      <div class="content-actions">
        <el-button plain @click="router.push('/admin/writearticle')">写文章</el-button>
        <el-button type="primary" @click="router.push('/admin/writemoment')">发布动态</el-button>
      </div>
    </div>

    <admin-content-tabs :bordered="false" :show-header="false" :initial-tab="activeTab" />
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useRoute } from 'vue-router'
import AdminContentTabs from '@/components/profile/AdminContentTabs.vue'

const router = useRouter()
const route = useRoute()

const activeTab = computed(() => {
  if (route.path.startsWith('/admin/commentmgmt')) return 'comments'
  if (route.path.startsWith('/admin/momentsmgmt')) return 'moments'
  return 'articles'
})
</script>

<style scoped>
.content-shell {
  padding: 22px;
  border-radius: 26px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.86);
}

.content-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.content-head h2 {
  margin: 0 0 8px;
}

.content-head p {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.content-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

@media (max-width: 720px) {
  .content-head {
    flex-direction: column;
  }
}
</style>

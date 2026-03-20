<template>
  <section class="editor-shell">
    <div class="editor-head">
      <el-button round @click="router.push('/admin/content')">返回内容管理</el-button>
    </div>

    <article-form-card />
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ArticleFormCard from '@/components/ArticleFormCard.vue'

const route = useRoute()
const router = useRouter()
const isEditing = computed(() => Boolean(route.params.id))
</script>

<style scoped>
.editor-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.editor-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.editor-head h2 {
  margin: 0 0 8px;
}

.editor-head p {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

@media (max-width: 720px) {
  .editor-head {
    flex-direction: column;
  }
}
</style>

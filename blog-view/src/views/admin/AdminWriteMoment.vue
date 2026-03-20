<template>
  <section class="editor-shell">
    <div class="editor-head">
      <div>
        <h2>发布动态</h2>
        <p>保留后台发布动态能力，统一进入站内后台工作流。</p>
      </div>
      <el-button round @click="router.push('/admin/content')">返回内容管理</el-button>
    </div>

    <admin-moment-form />
  </section>
</template>

<script setup>
import { useRouter } from 'vue-router'
import AdminMomentForm from '@/components/admin/AdminMomentForm.vue'

const router = useRouter()
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

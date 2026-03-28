<template>
  <section class="workspace-shell">
    <div class="workspace-head">
      <div>
        <h2>{{ title }}</h2>
        <p>{{ description }}</p>
      </div>
      <el-button round :loading="loading" @click="$emit('refresh')">刷新内容</el-button>
    </div>

    <slot />
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    required: true,
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

defineEmits(['refresh'])
</script>

<style scoped>
.workspace-shell {
  padding: 18px;
  border-radius: 26px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.88);
}

.workspace-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 18px;
}

.workspace-head h2 {
  margin: 0 0 8px;
}

.workspace-head p {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

@media (max-width: 720px) {
  .workspace-head {
    flex-direction: column;
  }
}
</style>

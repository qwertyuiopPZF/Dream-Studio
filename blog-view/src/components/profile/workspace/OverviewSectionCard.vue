<template>
  <section class="overview-section-card">
    <header class="section-header">
      <div>
        <h3>{{ title }}</h3>
        <p v-if="description">{{ description }}</p>
      </div>
      <slot name="header-extra" />
    </header>

    <div class="section-body">
      <slot />
    </div>
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
    default: '',
  },
})
</script>

<style scoped>
.overview-section-card {
  padding: 20px;
  border-radius: 22px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.9);
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  color: var(--workspace-title, #0f172a);
  font-size: 18px;
}

.section-header p {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.65;
}

.section-body {
  min-width: 0;
}

@media (max-width: 720px) {
  .section-header {
    flex-direction: column;
  }
}
</style>

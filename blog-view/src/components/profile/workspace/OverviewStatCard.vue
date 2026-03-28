<template>
  <article class="stat-card">
    <span class="stat-label">{{ label }}</span>
    <strong class="stat-value">{{ value }}</strong>
    <p class="stat-hint">{{ hint }}</p>
  </article>
</template>

<script setup>
defineProps({
  label: {
    type: String,
    required: true,
  },
  value: {
    type: [String, Number],
    required: true,
  },
  hint: {
    type: String,
    default: '',
  },
})
</script>

<style scoped>
.stat-card {
  padding: 20px;
  border-radius: 20px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.94), rgba(248, 250, 252, 0.92));
}

.stat-label {
  display: block;
  color: #64748b;
  font-size: 13px;
}

.stat-value {
  display: block;
  margin-top: 10px;
  color: var(--workspace-title, #0f172a);
  font-size: clamp(30px, 4vw, 38px);
  line-height: 1;
}

.stat-hint {
  margin: 12px 0 0;
  color: #64748b;
  line-height: 1.6;
}
</style>

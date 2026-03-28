<template>
  <div class="permission-empty">{{ message }}</div>
</template>

<script setup>
defineProps({
  message: {
    type: String,
    required: true,
  },
})
</script>

<style scoped>
.permission-empty {
  padding: 20px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(248, 250, 252, 0.85);
  color: #64748b;
}
</style>

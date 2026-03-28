<template>
  <section class="settings-panel">
    <div class="section-row">
      <h3>站点信息</h3>
      <el-button type="primary" :loading="saving" @click="$emit('submit')">保存站点信息</el-button>
    </div>
    <el-form label-position="top" class="site-form-grid">
      <el-form-item label="网站名称">
        <el-input :model-value="form.name" maxlength="60" @update:model-value="updateField('name', $event)" />
      </el-form-item>
      <el-form-item label="网站作者">
        <el-input :model-value="form.author" maxlength="60" @update:model-value="updateField('author', $event)" />
      </el-form-item>
      <el-form-item label="网站关键词">
        <el-input :model-value="form.keywords" maxlength="160" @update:model-value="updateField('keywords', $event)" />
      </el-form-item>
      <el-form-item label="ICP备案号">
        <el-input :model-value="form.icp" maxlength="80" @update:model-value="updateField('icp', $event)" />
      </el-form-item>
      <el-form-item label="网站描述" class="site-form-wide">
        <el-input
          :model-value="form.description"
          type="textarea"
          :rows="4"
          maxlength="300"
          show-word-limit
          @update:model-value="updateField('description', $event)"
        />
      </el-form-item>
    </el-form>
  </section>
</template>

<script setup>
defineProps({
  form: {
    type: Object,
    required: true,
  },
  saving: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['submit', 'update:field'])

const updateField = (field, value) => {
  emit('update:field', { field, value })
}
</script>

<style scoped>
.settings-panel {
  margin-top: 18px;
  padding: 20px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(248, 250, 252, 0.85);
}

.section-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.section-row h3 {
  margin: 0 0 8px;
}

.site-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
  margin-top: 12px;
}

.site-form-wide {
  grid-column: 1 / -1;
}

@media (max-width: 900px) {
  .site-form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .section-row {
    flex-direction: column;
  }
}
</style>

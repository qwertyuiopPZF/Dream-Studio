<template>
  <el-dialog :model-value="visible" title="发布公告" width="520px" @update:model-value="emit('update:visible', $event)">
    <el-form label-position="top">
      <el-form-item label="公告标题">
        <el-input :model-value="form.title" maxlength="80" @update:model-value="updateTitle" />
      </el-form-item>
      <el-form-item label="公告内容">
        <el-input
          :model-value="form.content"
          type="textarea"
          :rows="5"
          maxlength="300"
          show-word-limit
          @update:model-value="updateContent"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="emit('submit')">发布</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  form: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits([
  'submit',
  'update:visible',
  'update:title',
  'update:content',
])

const updateTitle = (value) => {
  emit('update:title', value)
}

const updateContent = (value) => {
  emit('update:content', value)
}
</script>

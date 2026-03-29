<template>
  <el-dialog
    :model-value="visible"
    :title="form.id ? editTitle : createTitle"
    width="420px"
    @update:model-value="emit('update:visible', $event)"
  >
    <el-form label-position="top">
      <el-form-item :label="fieldLabel">
        <el-input :model-value="form.name" maxlength="20" @update:model-value="updateName" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="loading" @click="emit('submit')">保存</el-button>
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
  fieldLabel: {
    type: String,
    required: true,
  },
  createTitle: {
    type: String,
    required: true,
  },
  editTitle: {
    type: String,
    required: true,
  },
})

const emit = defineEmits(['submit', 'update:visible', 'update:name'])

const updateName = (value) => {
  emit('update:name', value)
}
</script>

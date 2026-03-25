<template>
  <el-card shadow="never" class="moment-form-card">
    <el-form ref="momentFormRef" :model="momentForm" :rules="rules" label-position="top">
      <el-form-item label="动态内容" prop="content">
        <el-input
          v-model="momentForm.content"
          type="textarea"
          :rows="6"
          maxlength="500"
          show-word-limit
          placeholder="分享你的新鲜事..."
        />
      </el-form-item>

      <el-form-item label="配图（最多 9 张）">
        <el-upload
          v-model:file-list="fileList"
          list-type="picture-card"
          :limit="9"
          :http-request="handleHttpRequest"
          :on-exceed="handleExceed"
          :on-preview="handlePictureCardPreview"
          :on-success="handleUploadSuccess"
          :before-upload="beforeImageUpload"
          name="file"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>

      <el-form-item>
        <el-button :loading="loading" @click="saveDraft">保存草稿</el-button>
        <el-button type="primary" :loading="loading" @click="publishMoment">发布动态</el-button>
      </el-form-item>
    </el-form>

    <el-dialog v-model="dialogVisible" width="560px">
      <img :src="dialogImageUrl" alt="Preview" class="preview-image" />
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createAdminMoment } from '@/api/admin'
import { uploadImage } from '@/api/upload'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const momentFormRef = ref(null)
const dialogImageUrl = ref('')
const dialogVisible = ref(false)
const fileList = ref([])
const uploadBaseUrl = import.meta.env.VITE_APP_UPLOAD_URL || ''

const momentForm = ref({
  content: '',
  image: '',
})

const rules = {
  content: [{ required: true, message: '请输入动态内容', trigger: 'blur' }],
}

const beforeImageUpload = (rawFile) => {
  const isImage = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png'
  const isLt5M = rawFile.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('上传图片只能是 JPG/PNG 格式')
    return false
  }

  if (!isLt5M) {
    ElMessage.error('上传图片大小不能超过 5MB')
    return false
  }

  return true
}

const handleHttpRequest = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)
  return uploadImage(formData)
}

const handleUploadSuccess = (response, uploadFile) => {
  const relativePath = typeof response === 'string' ? response : response?.data || response?.url

  if (!relativePath) return

  uploadFile.actualPath = relativePath
  uploadFile.url = relativePath.startsWith('http') ? relativePath : `${uploadBaseUrl}${relativePath}`
  ElMessage.success('上传成功')
}

const handlePictureCardPreview = (uploadFile) => {
  dialogImageUrl.value = uploadFile.url
  dialogVisible.value = true
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传 9 张图片')
}

const processImages = () => {
  return fileList.value
    .map((item) => item.actualPath || (typeof item.response === 'string' ? item.response : item.response?.data))
    .filter(Boolean)
    .join(',')
}

const submitMoment = async (status) => {
  loading.value = true
  try {
    await createAdminMoment({
      content: momentForm.value.content,
      image: processImages(),
      status,
    })

    ElMessage.success(status === 1 ? '动态发布成功' : '草稿保存成功')
    router.push('/admin/content')
  } catch (error) {
    console.error('保存动态失败', error)
  } finally {
    loading.value = false
  }
}

const saveDraft = async () => {
  if (!momentForm.value.content.trim()) {
    ElMessage.warning('请输入动态内容')
    return
  }
  await submitMoment(0)
}

const publishMoment = async () => {
  if (!momentFormRef.value) return

  await momentFormRef.value.validate(async (valid) => {
    if (!valid) return
    await submitMoment(1)
  })
}
</script>

<style scoped>
.moment-form-card {
  border-radius: 24px;
}

.preview-image {
  width: 100%;
  display: block;
  border-radius: 16px;
}

:deep(.el-upload--picture-card) {
  border-style: dashed;
}
</style>

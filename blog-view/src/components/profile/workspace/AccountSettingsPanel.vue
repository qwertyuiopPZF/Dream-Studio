<template>
  <workspace-page-shell
    title="账号设置"
    description="更新头像、设置站内密码，并确认当前账号资料。"
    :loading="loading"
    @refresh="loadProfile"
  >
    <div class="settings-grid">
      <overview-section-card title="头像设置" description="支持直接填写头像地址，或先上传图片再保存到个人资料。">
        <div class="avatar-section">
          <el-avatar :size="88" :src="avatarForm.avatar || currentProfile.avatar">{{ avatarInitial }}</el-avatar>

          <div class="avatar-form">
            <el-input v-model="avatarForm.avatar" placeholder="请输入头像 URL 或站内相对路径" clearable />
            <div class="avatar-actions">
              <el-upload :show-file-list="false" :http-request="handleAvatarUpload">
                <el-button plain>上传图片</el-button>
              </el-upload>
              <el-button type="primary" :loading="avatarSaving" @click="submitAvatar">保存头像</el-button>
            </div>
            <p class="helper-text">后端支持 `http(s)` 或以 `/` 开头的站内相对路径。</p>
          </div>
        </div>
      </overview-section-card>

      <overview-section-card title="密码设置" description="首次使用 GitHub 登录的账号，可以在这里补充站内密码。">
        <el-form label-position="top" class="password-form">
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="至少 6 位" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
          </el-form-item>
          <div class="password-actions">
            <el-button type="primary" :loading="passwordSaving" @click="submitPassword">更新密码</el-button>
          </div>
        </el-form>
      </overview-section-card>
    </div>

    <overview-section-card title="当前资料" description="这些信息来自当前登录账号，可帮助确认资料同步是否正确。">
      <div class="profile-summary-grid">
        <article class="summary-item">
          <span>用户名</span>
          <strong>@{{ currentProfile.username || '--' }}</strong>
        </article>
        <article class="summary-item">
          <span>昵称</span>
          <strong>{{ currentProfile.nickname || '--' }}</strong>
        </article>
        <article class="summary-item">
          <span>邮箱</span>
          <strong>{{ currentProfile.email || '未填写' }}</strong>
        </article>
        <article class="summary-item">
          <span>手机号</span>
          <strong>{{ currentProfile.phone || '未填写' }}</strong>
        </article>
        <article class="summary-item">
          <span>角色</span>
          <strong>{{ currentProfile.role === 'ADMIN' ? '管理员' : '普通用户' }}</strong>
        </article>
        <article class="summary-item">
          <span>登录方式</span>
          <strong>{{ currentProfile.githubLogin ? 'GitHub + 站内资料' : '站内账号' }}</strong>
        </article>
      </div>
    </overview-section-card>
  </workspace-page-shell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchCurrentUserProfile, updateCurrentUserAvatar, updateCurrentUserPassword } from '@/api/user'
import { uploadImage } from '@/api/upload'
import OverviewSectionCard from '@/components/profile/workspace/OverviewSectionCard.vue'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const loading = ref(false)
const avatarSaving = ref(false)
const passwordSaving = ref(false)
const avatarForm = reactive({
  avatar: '',
})
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: '',
})

const currentProfile = computed(() => userStore.profile || {})
const avatarInitial = computed(() => currentProfile.value.nickname?.slice(0, 1)?.toUpperCase() || 'D')
const uploadBaseUrl = import.meta.env.VITE_APP_UPLOAD_URL || ''

const loadProfile = async () => {
  loading.value = true

  try {
    const response = await fetchCurrentUserProfile()
    userStore.hydrateFromServerProfile(response)
    avatarForm.avatar = response?.avatar || ''
  } catch (error) {
    console.error('加载账号资料失败', error)
    ElMessage.error(error.message || '加载账号资料失败，请稍后重试。')
  } finally {
    loading.value = false
  }
}

const submitAvatar = async () => {
  if (!avatarForm.avatar.trim()) {
    ElMessage.warning('请输入头像地址或先上传图片')
    return
  }

  avatarSaving.value = true

  try {
    const profile = await updateCurrentUserAvatar({ avatar: avatarForm.avatar.trim() })
    userStore.hydrateFromServerProfile(profile)
    avatarForm.avatar = profile?.avatar || avatarForm.avatar
    ElMessage.success('头像已更新')
  } catch (error) {
    console.error('更新头像失败', error)
    ElMessage.error(error.message || '更新头像失败，请稍后重试。')
  } finally {
    avatarSaving.value = false
  }
}

const handleAvatarUpload = async (options) => {
  const formData = new FormData()
  formData.append('file', options.file)

  try {
    const response = await uploadImage(formData)
    const relativePath = typeof response === 'string' ? response : response?.data || response?.url

    if (!relativePath) {
      throw new Error('上传结果中没有可用的图片地址')
    }

    avatarForm.avatar = relativePath.startsWith('http') ? relativePath : `${uploadBaseUrl}${relativePath}`
    options.onSuccess?.(response)
    ElMessage.success('图片上传成功，请点击保存头像完成更新')
  } catch (error) {
    options.onError?.(error)
    console.error('上传头像失败', error)
    ElMessage.error(error.message || '上传头像失败，请稍后重试。')
  }
}

const submitPassword = async () => {
  if (!passwordForm.newPassword.trim()) {
    ElMessage.warning('请输入新密码')
    return
  }

  if (passwordForm.newPassword.trim().length < 6) {
    ElMessage.warning('密码长度不能少于 6 位')
    return
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  passwordSaving.value = true

  try {
    await updateCurrentUserPassword({ newPassword: passwordForm.newPassword.trim() })
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    await loadProfile()
    ElMessage.success('站内密码已更新')
  } catch (error) {
    console.error('更新密码失败', error)
    ElMessage.error(error.message || '更新密码失败，请稍后重试。')
  } finally {
    passwordSaving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  margin-top: 18px;
}

.avatar-section {
  display: flex;
  align-items: flex-start;
  gap: 18px;
}

.avatar-form,
.password-form,
.profile-summary-grid {
  width: 100%;
}

.avatar-actions,
.password-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.helper-text {
  margin: 10px 0 0;
  color: #64748b;
  line-height: 1.6;
}

.profile-summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.summary-item {
  padding: 16px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(248, 250, 252, 0.86);
}

.summary-item span {
  display: block;
  color: #64748b;
  font-size: 13px;
}

.summary-item strong {
  display: block;
  margin-top: 10px;
  color: #0f172a;
  line-height: 1.6;
}

@media (max-width: 960px) {
  .settings-grid,
  .profile-summary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .avatar-section {
    flex-direction: column;
  }

  .avatar-actions,
  .password-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>

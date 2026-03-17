<template>
  <div class="login-box">
    <h2 class="text">登录 Dream Studio</h2>
    <p class="desc">支持用户名密码登录，也支持使用 GitHub 首次注册并设置站内密码。</p>

    <div v-if="!showPasswordSetup" class="login-panel">
      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="账号密码登录" name="password">
          <div class="form-grid">
            <el-input v-model="passwordForm.username" placeholder="请输入用户名" />
            <el-input v-model="passwordForm.password" type="password" show-password placeholder="请输入密码" />
            <el-button type="primary" :loading="loading" @click="handlePasswordLogin">登录</el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="GitHub 登录" name="github">
          <div class="github-login-panel">
            <p>使用 GitHub 账号授权后：</p>
            <ul>
              <li>首次登录：自动注册站内账号</li>
              <li>自动关联用户名</li>
              <li>首次进入后需要设置站内密码，后续即可用用户名密码登录</li>
            </ul>
            <el-button type="primary" :loading="loading" @click="handleGithubLogin">使用 GitHub 登录</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div v-else class="password-setup-panel">
      <h3>设置站内密码</h3>
      <p class="setup-tip">GitHub 登录成功，当前用户名：<strong>{{ pendingUsername }}</strong></p>
      <el-input v-model="setupPasswordForm.newPassword" type="password" show-password placeholder="请输入新密码（至少 6 位）" />
      <el-input v-model="setupPasswordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
      <el-button type="primary" :loading="loading" @click="handleSetupPassword">保存密码并进入个人中心</el-button>
    </div>

    <p v-if="statusMsg" class="status-text">{{ statusMsg }}</p>
    <p v-if="errorMsg" class="error-text">{{ errorMsg }}</p>
  </div>
</template>
<script>
export default {
  name: "MyLogin",
};
</script>
<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useUserStore } from '@/store/user'
import { fetchGithubAuthorizeUrl, githubCodeLogin, usernamePasswordLogin } from '@/api/auth'
import { fetchCurrentUserProfile, updateCurrentUserPassword } from '@/api/user'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const userStore = useUserStore()

const loading = ref(false)
const errorMsg = ref('')
const statusMsg = ref('')
const activeTab = ref('password')
const showPasswordSetup = ref(false)
const pendingUsername = ref('')

const passwordForm = reactive({
  username: '',
  password: '',
})

const setupPasswordForm = reactive({
  newPassword: '',
  confirmPassword: '',
})

const finishLogin = async (tokenPayload, options = {}) => {
  authStore.setTokens(tokenPayload.accessToken, tokenPayload.refreshToken)
  userStore.hydrateFromToken(tokenPayload.accessToken)
  const profile = await fetchCurrentUserProfile()
  userStore.hydrateFromServerProfile(profile)

  if (options.skipRedirect) {
    return profile
  }

  const redirectTarget = typeof route.query.redirect === 'string' ? route.query.redirect : '/profile'
  await router.replace(redirectTarget)
}

const handlePasswordLogin = async () => {
  if (!passwordForm.username.trim() || !passwordForm.password.trim()) {
    errorMsg.value = '请输入用户名和密码'
    return
  }

  loading.value = true
  errorMsg.value = ''
  statusMsg.value = '正在登录...'
  try {
    const tokenPayload = await usernamePasswordLogin(passwordForm)
    await finishLogin(tokenPayload)
  } catch (error) {
    console.error(error)
    errorMsg.value = '账号密码登录失败：' + (error.message || '未知错误')
  } finally {
    loading.value = false
    statusMsg.value = ''
  }
}

const handleGithubLogin = async () => {
  try {
    loading.value = true
    errorMsg.value = ''
    const data = await fetchGithubAuthorizeUrl()
    window.location.href = data.url
  } catch (error) {
    console.error(error)
    errorMsg.value = '获取 GitHub 登录地址失败：' + (error.message || '未知错误')
  } finally {
    loading.value = false
  }
}

const handleGithubCallback = async () => {
  const { code, state } = route.query
  if (!code) return

  loading.value = true
  statusMsg.value = '正在完成 GitHub 登录，请稍候...'
  errorMsg.value = ''
  try {
    const tokenPayload = await githubCodeLogin({ code, state })
    pendingUsername.value = tokenPayload.username || ''
    if (tokenPayload.needsPasswordSetup) {
      await finishLogin(tokenPayload, { skipRedirect: true })
      showPasswordSetup.value = true
      statusMsg.value = '请为当前站内账号设置密码'
      return
    }
    await finishLogin(tokenPayload)
  } catch (error) {
    console.error(error)
    errorMsg.value = 'GitHub 登录失败：' + (error.message || '未知错误')
  } finally {
    loading.value = false
    statusMsg.value = ''
  }
}

const handleSetupPassword = async () => {
  if (!setupPasswordForm.newPassword.trim()) {
    errorMsg.value = '请输入新密码'
    return
  }
  if (setupPasswordForm.newPassword.length < 6) {
    errorMsg.value = '密码长度不能少于 6 位'
    return
  }
  if (setupPasswordForm.newPassword !== setupPasswordForm.confirmPassword) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }

  loading.value = true
  errorMsg.value = ''
  statusMsg.value = '正在保存密码...'
  try {
    await updateCurrentUserPassword({ newPassword: setupPasswordForm.newPassword })
    showPasswordSetup.value = false
    setupPasswordForm.newPassword = ''
    setupPasswordForm.confirmPassword = ''
    const redirectTarget = typeof route.query.redirect === 'string' ? route.query.redirect : '/profile'
    await router.replace(redirectTarget)
  } catch (error) {
    console.error(error)
    errorMsg.value = '设置密码失败：' + (error.message || '未知错误')
  } finally {
    loading.value = false
    statusMsg.value = ''
  }
}

onMounted(handleGithubCallback)
</script>

<style scoped>
.login-box {
  width: 420px;
  margin: 120px auto;
  text-align: center;
  padding: 36px 30px;
  border-radius: 24px;
  background: var(--card-bg-color, #fff);
  border: 1px solid rgba(15, 23, 42, 0.08);
}

.login-panel,
.password-setup-panel {
  text-align: left;
}

.text {
  font-size: 1.8rem;
  font-weight: 600;
  color: var(--app-text-color, #545252);
  margin-top: 0;
  margin-bottom: 16px;
}

.desc {
  color: #7a7a7a;
  line-height: 1.8;
  margin-bottom: 24px;
}

.form-grid,
.password-setup-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.github-login-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.github-login-panel p,
.github-login-panel ul,
.setup-tip {
  margin: 0;
  color: #7a7a7a;
  line-height: 1.8;
}

.github-login-panel ul {
  padding-left: 18px;
}

.password-setup-panel h3 {
  margin: 0 0 4px;
  color: var(--app-text-color, #545252);
}

.status-text {
  margin-top: 18px;
  color: #409eff;
}

.error-text {
  margin-top: 18px;
  color: #f56c6c;
}
</style>

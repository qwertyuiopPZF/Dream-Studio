<template>
  <el-card class="login-page">
    <div class="login-box">
      <div class="header-block">
        <p class="eyebrow">Dream Studio Account</p>
        <h2 class="text">登录或完成 GitHub 注册</h2>
        <p class="desc">
          支持用户名/密码直接登录；首次注册通过 GitHub 授权拉取公开资料，再补充手机号与站内密码。
        </p>
      </div>

      <!-- 注册表单（GitHub授权后） -->
      <div v-if="showRegistrationForm" class="register-panel">
        <div class="github-profile-card">
          <el-avatar shape="square" :size="64" :src="pendingGithubProfile.avatar">
            {{
              (pendingGithubProfile.nickname || pendingGithubProfile.githubLogin || 'G').slice(0, 1)
            }}
          </el-avatar>
          <div class="github-profile-meta">
            <strong>{{
              pendingGithubProfile.nickname || pendingGithubProfile.githubLogin || 'GitHub 用户'
            }}</strong>
            <span>GitHub 账号：{{ pendingGithubProfile.githubLogin || '未获取到' }}</span>
            <span>邮箱：{{ pendingGithubProfile.email || 'GitHub 未公开邮箱' }}</span>
          </div>
        </div>
        <p class="setup-tip">
          GitHub 已完成授权。请设置站内用户名和密码；用户名不可重复，手机号需手动填写。
        </p>
        <div class="form-grid">
          <el-input
            v-model="registrationForm.username"
            placeholder="请输入站内用户名（3-30 位，字母/数字/_/-）"
          />
          <el-input v-model="registrationForm.phone" placeholder="请输入手机号" />
          <el-input
            v-model="registrationForm.password"
            type="password"
            show-password
            placeholder="请输入站内密码（至少 6 位）"
          />
          <el-input
            v-model="registrationForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入站内密码"
          />
          <el-button type="primary" :loading="loading" @click="handleCompleteGithubRegistration"
            >完成注册并登录</el-button
          >
          <el-button text @click="resetRegistrationState">重新选择登录方式</el-button>
        </div>
      </div>

      <!-- 登录表单 -->
      <div v-else class="login-panel">
        <el-tabs v-model="activeTab" stretch>
          <el-tab-pane label="账号密码登录" name="password">
            <div class="form-grid">
              <el-input v-model="passwordForm.username" placeholder="请输入用户名" />
              <el-input
                v-model="passwordForm.password"
                type="password"
                show-password
                placeholder="请输入密码"
              />
              <el-button type="primary" :loading="loading" @click="handlePasswordLogin"
                >登录</el-button
              >
            </div>
          </el-tab-pane>
          <el-tab-pane label="GitHub 注册" name="github">
            <div class="github-login-panel">
              <p>首次注册会通过 GitHub 拉取以下公开资料：</p>
              <ul>
                <li>GitHub 登录名</li>
                <li>昵称、头像、公开邮箱</li>
                <li>站内注册建议用户名</li>
              </ul>
              <p>GitHub 默认不会返回手机号，因此会在授权成功后由你手动补充。</p>
              <el-button type="primary" :loading="loading" @click="handleGithubLogin"
                >使用 GitHub 注册 / 登录</el-button
              >
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <p v-if="statusMsg" class="status-text">{{ statusMsg }}</p>
      <p v-if="errorMsg" class="error-text">{{ errorMsg }}</p>
    </div>
  </el-card>
</template>

<script setup>
// 1. 引入必要依赖（确保项目已安装对应包）
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
// 若项目未用Pinia/Vuex，可注释Store相关代码，后续补全
import { useAuthStore } from '@/store/auth'
import { useUserStore } from '@/store/user'
// API请求（确保@/api/auth和@/api/user路径下有对应函数）
import {
  completeGithubRegistration,
  fetchGithubAuthorizeUrl,
  githubCodeLogin,
  usernamePasswordLogin,
} from '@/api/auth'
import { fetchCurrentUserProfile } from '@/api/user'
// Element Plus组件（若项目已全局注册可省略，否则需单独引入）
import { ElAvatar, ElButton, ElInput, ElTabs, ElTabPane } from 'element-plus'

// 2. 初始化基础变量
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore() // 若未用Store，需替换为localStorage存储token
const userStore = useUserStore() // 同理，可替换为localStorage存储用户信息

const loading = ref(false)
const errorMsg = ref('')
const statusMsg = ref('')
const activeTab = ref('password')

// 登录表单数据
const passwordForm = reactive({
  username: '',
  password: '',
})

// 注册表单数据
const registrationForm = reactive({
  registrationToken: '',
  username: '',
  phone: '',
  password: '',
  confirmPassword: '',
})

// GitHub授权后的临时用户信息
const pendingGithubProfile = reactive({
  githubLogin: '',
  nickname: '',
  avatar: '',
  email: '',
})

// 判断是否显示注册表单
const showRegistrationForm = computed(() => Boolean(registrationForm.registrationToken))

// 3. 核心逻辑函数
// 刷新用户信息（若未用Store，可改为直接存储到localStorage）
const refreshCurrentUserProfile = async () => {
  try {
    const profile = await fetchCurrentUserProfile()
    if (userStore?.hydrateFromServerProfile) {
      userStore.hydrateFromServerProfile(profile)
    } else {
      localStorage.setItem('userProfile', JSON.stringify(profile))
    }
    return profile
  } catch (error) {
    console.warn('获取用户资料失败，保留登录态', error)
    return null
  }
}

// 解析跳转目标
const resolveRedirectTarget = () =>
  typeof route.query.redirect === 'string' ? route.query.redirect : '/home'

// 登录完成后的处理（若未用Store，用localStorage存token）
const finishLogin = async (tokenPayload) => {
  if (authStore?.setTokens) {
    authStore.setTokens(tokenPayload.accessToken, tokenPayload.refreshToken)
  } else {
    localStorage.setItem('accessToken', tokenPayload.accessToken)
    localStorage.setItem('refreshToken', tokenPayload.refreshToken)
  }

  if (userStore?.hydrateFromToken) {
    userStore.hydrateFromToken(tokenPayload.accessToken, tokenPayload.username || '')
  } else {
    localStorage.setItem('username', tokenPayload.username || '')
  }

  await refreshCurrentUserProfile()
  await router.replace(resolveRedirectTarget())
  return userStore?.profile || JSON.parse(localStorage.getItem('userProfile'))
}

// 重置注册状态
const resetRegistrationState = () => {
  Object.assign(registrationForm, {
    registrationToken: '',
    username: '',
    phone: '',
    password: '',
    confirmPassword: '',
  })
  Object.assign(pendingGithubProfile, {
    githubLogin: '',
    nickname: '',
    avatar: '',
    email: '',
  })
  statusMsg.value = ''
  errorMsg.value = ''
}

// 清除GitHub回调参数
const clearGithubCallbackQuery = async () => {
  if (!route.query.code && !route.query.state) return
  const nextQuery = {}
  if (typeof route.query.redirect === 'string') {
    nextQuery.redirect = route.query.redirect
  }
  await router.replace({ path: '/login', query: nextQuery })
}

// 应用GitHub注册信息
const applyGithubRegistrationPayload = async (payload) => {
  registrationForm.registrationToken = payload.registrationToken || ''
  registrationForm.username = payload.username || ''
  registrationForm.phone = payload.phone || ''
  pendingGithubProfile.githubLogin = payload.githubLogin || ''
  pendingGithubProfile.nickname = payload.nickname || ''
  pendingGithubProfile.avatar = payload.avatar || ''
  pendingGithubProfile.email = payload.email || ''
  await clearGithubCallbackQuery()
}

// 账号密码登录
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

// GitHub登录/注册
const handleGithubLogin = async () => {
  try {
    loading.value = true
    errorMsg.value = ''
    statusMsg.value = '正在跳转 GitHub 授权页...'
    const data = await fetchGithubAuthorizeUrl()
    window.location.href = data.url
  } catch (error) {
    console.error(error)
    errorMsg.value = '获取 GitHub 授权地址失败：' + (error.message || '未知错误')
  } finally {
    loading.value = false
    statusMsg.value = ''
  }
}

// 处理GitHub授权回调
const handleGithubCallback = async () => {
  const { code, state } = route.query
  if (!code) return
  loading.value = true
  statusMsg.value = '正在完成 GitHub 授权，请稍候...'
  errorMsg.value = ''
  try {
    const tokenPayload = await githubCodeLogin({ code, state })
    if (tokenPayload.needsRegistration) {
      await applyGithubRegistrationPayload(tokenPayload)
      statusMsg.value = '请补充站内注册信息'
      activeTab.value = 'github'
      return
    }
    await finishLogin(tokenPayload)
  } catch (error) {
    console.error(error)
    errorMsg.value = 'GitHub 授权失败：' + (error.message || '未知错误')
  } finally {
    loading.value = false
    if (!showRegistrationForm.value) statusMsg.value = ''
  }
}

// 完成GitHub注册
const handleCompleteGithubRegistration = async () => {
  if (!registrationForm.registrationToken) {
    errorMsg.value = 'GitHub 注册会话已失效，请重新授权'
    return
  }
  if (!registrationForm.username.trim()) {
    errorMsg.value = '请输入站内用户名'
    return
  }
  if (!registrationForm.phone.trim()) {
    errorMsg.value = '请输入手机号'
    return
  }
  if (!registrationForm.password.trim() || registrationForm.password.length < 6) {
    errorMsg.value = '密码长度不能少于 6 位'
    return
  }
  if (registrationForm.password !== registrationForm.confirmPassword) {
    errorMsg.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  errorMsg.value = ''
  statusMsg.value = '正在完成注册...'
  try {
    const tokenPayload = await completeGithubRegistration({
      registrationToken: registrationForm.registrationToken,
      username: registrationForm.username.trim(),
      phone: registrationForm.phone.trim(),
      password: registrationForm.password,
    })
    resetRegistrationState()
    await finishLogin(tokenPayload)
  } catch (error) {
    console.error(error)
    errorMsg.value = '完成注册失败：' + (error.message || '未知错误')
  } finally {
    loading.value = false
    if (!showRegistrationForm.value) statusMsg.value = ''
  }
}

// 页面挂载时处理GitHub回调
onMounted(handleGithubCallback)
</script>

<style scoped>
/* 替换项目未定义的CSS变量，改为实际可用的颜色 */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  background: #f5f7fa; /* 替换var(--app-page-bg) */
}

.login-box {
  width: min(460px, calc(100vw - 32px));
  margin: 0 auto;
  padding: 34px 30px;
  border-radius: 28px;
  background: #ffffff; /* 替换var(--card-bg-color) */
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 20px 50px rgba(15, 23, 42, 0.08);
}

.header-block {
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 10px;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  color: #9a7f54;
}

.login-panel,
.register-panel {
  text-align: left;
}

.text {
  margin: 0 0 10px;
  font-size: 1.9rem;
  font-weight: 700;
  color: #545252; /* 替换var(--app-text-color) */
}

.desc {
  margin: 0;
  color: #6f6f6f;
  line-height: 1.8;
}

.form-grid {
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
  color: #707070;
  line-height: 1.8;
}

.github-login-panel ul {
  padding-left: 18px;
}

.github-profile-card {
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 16px;
  margin-bottom: 16px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(250, 244, 232, 0.95), rgba(244, 247, 251, 0.92));
  border: 1px solid rgba(154, 127, 84, 0.16);
}

.github-profile-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.github-profile-meta strong {
  display: block;
  color: #545252; /* 替换var(--app-text-color) */
  font-size: 1rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.github-profile-meta span {
  color: #6f6f6f;
  font-size: 13px;
  word-break: break-all;
}

.setup-tip {
  margin-bottom: 16px;
}

.status-text {
  margin-top: 18px;
  color: #409eff;
}

.error-text {
  margin-top: 18px;
  color: #f56c6c;
}
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  background-image: url('/背景样式.jpg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  position: relative;
}

.login-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(255, 255, 255, 0.6);
  z-index: 1;
}

.login-box {
  position: relative;
  z-index: 2;
}

/* 响应式适配 */
@media screen and (max-width: 640px) {
  .login-page {
    padding: 20px 12px;
  }
  .login-box {
    padding: 26px 20px;
  }
  .github-profile-card {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/auth'
import { useUserStore } from '@/store/user'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 10000, // 请求超时时间
})

let refreshRequest = null

const resolveMessage = (payload, fallback = '请求失败') => {
  if (!payload) return fallback
  if (typeof payload === 'string') return payload
  return payload.msg || payload.message || fallback
}

const shouldSkipAuthRefresh = (config = {}) => {
  const url = String(config.url || '')
  return Boolean(config.skipAuthRefresh) || /\/auth\/(login|refresh|github)/.test(url)
}

const requestTokenRefresh = async () => {
  const authStore = useAuthStore()
  const userStore = useUserStore()

  if (!authStore.refreshToken) {
    throw new Error('登录已过期，请重新登录')
  }

  if (!refreshRequest) {
    refreshRequest = service
      .post(
        '/auth/refresh',
        { refreshToken: authStore.refreshToken },
        { skipAuthRefresh: true, showErrorMessage: false },
      )
      .then((tokenPayload) => {
        authStore.setTokens(tokenPayload.accessToken, tokenPayload.refreshToken, { markLogin: false })
        userStore.hydrateFromToken(tokenPayload.accessToken, tokenPayload.username || '')
        return tokenPayload
      })
      .finally(() => {
        refreshRequest = null
      })
  }

  return refreshRequest
}

service.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.accessToken) {
      config.headers.Authorization = `Bearer ${authStore.accessToken}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 检查响应是否是我们期望的 Result DTO 格式
    // 如果 code 字段不存在，我们假定这是一个成功的请求，且响应体就是原始数据
    if (res.code === undefined) {
      return res
    }

    // 如果是 Result DTO 格式，我们根据 code 的值来判断
    if (res.code === 1) {
      return res.data // code 为 1 表示成功，直接返回 data 部分
    } else {
      // 处理已知的错误情况 (code 不为 1)
      const message = resolveMessage(res)
      ElMessage({
        message,
        type: 'error',
        duration: 5 * 1000,
      })
      return Promise.reject(new Error(message))
    }
  },
  async (error) => {
    console.error('Network Error:', error) // for debug

    const authStore = useAuthStore()
    const originalRequest = error.config || {}

    if (error.response?.status === 401 && !originalRequest._retry && !shouldSkipAuthRefresh(originalRequest) && authStore.refreshToken) {
      originalRequest._retry = true
      try {
        const tokenPayload = await requestTokenRefresh()
        originalRequest.headers = originalRequest.headers || {}
        originalRequest.headers.Authorization = `Bearer ${tokenPayload.accessToken}`
        return service(originalRequest)
      } catch (refreshError) {
        authStore.logout()
        const refreshMessage = resolveMessage(refreshError.response?.data, refreshError.message || '登录已过期，请重新登录')
        if (originalRequest.showErrorMessage !== false) {
          ElMessage({
            message: refreshMessage,
            type: 'error',
            duration: 5 * 1000,
          })
        }
        return Promise.reject(new Error(refreshMessage))
      }
    }

    if (error.response?.status === 401) {
      authStore.logout()
    }

    const message = resolveMessage(error.response?.data, error.message || '网络错误，请检查您的连接')

    if (originalRequest.showErrorMessage !== false) {
      ElMessage({
        message,
        type: 'error',
        duration: 5 * 1000,
      })
    }

    return Promise.reject(new Error(message))
  },
)

export default service

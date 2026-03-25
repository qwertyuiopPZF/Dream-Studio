import defaultAvatar from '@/assets/(5).png'

const USER_PROFILE_KEYS = [
  'user_id',
  'user_nickname',
  'user_avatar',
  'user_email',
  'user_phone',
  'user_role',
  'user_bio',
  'user_username',
  'user_github_login',
  'user_password_initialized',
]

const ACCOUNT_PRESETS = {
  admin: {
    nickname: '管理员',
    bio: '站点管理员 · 可在用户端处理内容与举报',
  },
}

const safeBase64Decode = (value) => {
  try {
    const base64 = value.replace(/-/g, '+').replace(/_/g, '/')
    const padded = `${base64}${'='.repeat((4 - (base64.length % 4)) % 4)}`
    return decodeURIComponent(
      atob(padded)
        .split('')
        .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join(''),
    )
  } catch (error) {
    return ''
  }
}

export const parseJwtPayload = (token) => {
  if (!token || typeof token !== 'string') return null
  const parts = token.split('.')
  if (parts.length < 2) return null

  const decoded = safeBase64Decode(parts[1])
  if (!decoded) return null

  try {
    return JSON.parse(decoded)
  } catch (error) {
    return null
  }
}

const normalizeRole = (roles = []) => {
  if (!Array.isArray(roles) || !roles.length) return ''
  const firstRole = String(roles[0] || '')
  return firstRole.replace(/^ROLE_/, '').toUpperCase()
}

export const resolveCurrentUserProfile = (accessToken = '') => {
  const token = accessToken || localStorage.getItem('accessToken') || ''
  const payload = parseJwtPayload(token)
  const usernameFromToken = payload?.sub || ''
  const roleFromToken = normalizeRole(payload?.roles)
  const preset = ACCOUNT_PRESETS[usernameFromToken.toLowerCase()] || {}
  const isLoggedIn = Boolean(token && usernameFromToken)

  if (isLoggedIn) {
    const username = localStorage.getItem('user_username') || usernameFromToken
    const nickname = localStorage.getItem('user_nickname') || preset.nickname || username
    const avatar = localStorage.getItem('user_avatar') || defaultAvatar
    const email = localStorage.getItem('user_email') || ''
    const phone = localStorage.getItem('user_phone') || ''
    const role = (localStorage.getItem('user_role') || roleFromToken || '').toUpperCase()
    const githubLogin = localStorage.getItem('user_github_login') || ''
    const passwordInitialized = localStorage.getItem('user_password_initialized') === 'true'
    const bio =
      localStorage.getItem('user_bio') ||
      preset.bio ||
      (role === 'ADMIN' ? '管理员账户 · 可在用户端执行管理操作' : `${nickname} 的个人主页`)

    return {
      id: Number(localStorage.getItem('user_id') || 0) || null,
      username,
      nickname,
      avatar,
      email,
      phone,
      role,
      bio,
      githubLogin,
      passwordInitialized,
      isLoggedIn: true,
    }
  }

  return {
    id: null,
    username: '',
    nickname: '游客',
    avatar: defaultAvatar,
    email: '',
    phone: '',
    role: '',
    bio: '游客模式 · 登录后可同步个人资料',
    githubLogin: '',
    passwordInitialized: false,
    isLoggedIn: false,
  }
}

export const syncUserProfileFromToken = (accessToken, fallbackUsername = '') => {
  const payload = parseJwtPayload(accessToken)
  const username = payload?.sub || fallbackUsername || ''
  const role = normalizeRole(payload?.roles)
  const preset = ACCOUNT_PRESETS[username.toLowerCase()] || {}
  const nickname = preset.nickname || username || 'Dream 用户'
  const bio = preset.bio || (role === 'ADMIN' ? '管理员账户 · 可在用户端执行管理操作' : `${nickname} 的个人主页`)

  localStorage.setItem('user_username', username)
  localStorage.setItem('user_nickname', nickname)
  localStorage.setItem('user_role', role)
  localStorage.setItem('user_bio', bio)
  localStorage.setItem('user_github_login', '')
  localStorage.setItem('user_password_initialized', 'false')
  if (!localStorage.getItem('user_phone')) {
    localStorage.setItem('user_phone', '')
  }

  if (!localStorage.getItem('user_avatar')) {
    localStorage.setItem('user_avatar', defaultAvatar)
  }

  return resolveCurrentUserProfile(accessToken)
}

export const syncUserProfileFromServerProfile = (profile = {}) => {
  if (profile.id !== undefined && profile.id !== null) localStorage.setItem('user_id', String(profile.id))
  if (profile.username !== undefined) localStorage.setItem('user_username', profile.username || '')
  if (profile.nickname !== undefined) localStorage.setItem('user_nickname', profile.nickname || '')
  if (profile.avatar !== undefined) localStorage.setItem('user_avatar', profile.avatar || defaultAvatar)
  if (profile.email !== undefined) localStorage.setItem('user_email', profile.email || '')
  if (profile.phone !== undefined) localStorage.setItem('user_phone', profile.phone || '')
  if (profile.role !== undefined) localStorage.setItem('user_role', profile.role || '')
  if (profile.bio !== undefined) localStorage.setItem('user_bio', profile.bio || '')
  if (profile.githubLogin !== undefined) localStorage.setItem('user_github_login', profile.githubLogin || '')
  if (profile.passwordInitialized !== undefined) {
    localStorage.setItem('user_password_initialized', profile.passwordInitialized ? 'true' : 'false')
  }
  return resolveCurrentUserProfile(localStorage.getItem('accessToken') || '')
}

export const clearUserProfileStorage = () => {
  USER_PROFILE_KEYS.forEach((key) => localStorage.removeItem(key))
}

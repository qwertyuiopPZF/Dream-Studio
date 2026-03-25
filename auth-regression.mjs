#!/usr/bin/env node

const config = {
  backendBaseUrl: process.env.BACKEND_BASE_URL || 'http://localhost:8081',
  userFrontendUrl: process.env.USER_FRONTEND_URL || 'http://localhost:3000',
  cmsFrontendUrl: process.env.CMS_FRONTEND_URL || 'http://localhost:8080',
  testUserUsername: process.env.TEST_USER_USERNAME || 'admin',
  testUserPassword: process.env.TEST_USER_PASSWORD || '123456',
  testAdminUsername: process.env.TEST_ADMIN_USERNAME || 'admin',
  testAdminPassword: process.env.TEST_ADMIN_PASSWORD || '123456',
  requireGithub: process.env.REQUIRE_GITHUB === 'true',
  requestTimeoutMs: Number(process.env.REQUEST_TIMEOUT_MS || 8000),
}

const failures = []
const warnings = []

const color = {
  reset: '\x1b[0m',
  green: '\x1b[32m',
  yellow: '\x1b[33m',
  red: '\x1b[31m',
  cyan: '\x1b[36m',
}

function printUsage() {
  console.log(`Usage: node auth-regression.mjs

Environment variables:
  BACKEND_BASE_URL    Backend base URL, default http://localhost:8081
  USER_FRONTEND_URL   User frontend URL, default http://localhost:3000
  CMS_FRONTEND_URL    CMS frontend URL, default http://localhost:8080
  TEST_USER_USERNAME  User login username, default admin
  TEST_USER_PASSWORD  User login password, default 123456
  TEST_ADMIN_USERNAME Admin login username, default admin
  TEST_ADMIN_PASSWORD Admin login password, default 123456
  REQUIRE_GITHUB      Set true to fail when GitHub OAuth is not configured
  REQUEST_TIMEOUT_MS  Single request timeout, default 8000

Examples:
  node auth-regression.mjs
  TEST_USER_USERNAME=tester TEST_USER_PASSWORD=secret node auth-regression.mjs
  REQUIRE_GITHUB=true BACKEND_BASE_URL=http://127.0.0.1:8081 node auth-regression.mjs
`)
}

if (process.argv.includes('--help') || process.argv.includes('-h')) {
  printUsage()
  process.exit(0)
}

function logInfo(message) {
  console.log(`${color.cyan}INFO${color.reset} ${message}`)
}

function logPass(message) {
  console.log(`${color.green}PASS${color.reset} ${message}`)
}

function logWarn(message) {
  warnings.push(message)
  console.log(`${color.yellow}WARN${color.reset} ${message}`)
}

function logFail(message) {
  failures.push(message)
  console.log(`${color.red}FAIL${color.reset} ${message}`)
}

function ensure(condition, message) {
  if (!condition) {
    throw new Error(message)
  }
}

async function requestJson(method, url, options = {}) {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), options.timeoutMs || config.requestTimeoutMs)
  const headers = {
    Accept: 'application/json',
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
    ...(options.headers || {}),
  }

  try {
    const response = await fetch(url, {
      method,
      headers,
      body: options.body ? JSON.stringify(options.body) : undefined,
      signal: controller.signal,
    })

    const rawText = await response.text()
    let payload = null

    if (rawText) {
      try {
        payload = JSON.parse(rawText)
      } catch (error) {
        payload = rawText
      }
    }

    return {
      status: response.status,
      ok: response.ok,
      payload,
      rawText,
      headers: response.headers,
    }
  } catch (error) {
    if (error.name === 'AbortError') {
      throw new Error(`请求超时: ${method} ${url}`)
    }
    throw error
  } finally {
    clearTimeout(timeout)
  }
}

function unwrapResult(response, expectedStatus = 200) {
  ensure(response.status === expectedStatus, `HTTP ${response.status}, body: ${response.rawText || '<empty>'}`)

  if (response.payload && typeof response.payload === 'object' && 'code' in response.payload) {
    ensure(response.payload.code === 1, `业务响应失败: ${response.payload.msg || response.payload.message || response.rawText}`)
    return response.payload.data
  }

  return response.payload
}

async function runStep(title, fn, options = {}) {
  try {
    await fn()
    logPass(title)
  } catch (error) {
    if (options.warning) {
      logWarn(`${title} - ${error.message}`)
      return
    }
    logFail(`${title} - ${error.message}`)
  }
}

async function probeFrontend(name, url) {
  await runStep(`${name} 可访问`, async () => {
    const response = await requestJson('GET', url)
    ensure(response.status >= 200 && response.status < 500, `页面状态异常: ${response.status}`)
  }, { warning: true })
}

async function testUserLoginChain() {
  const loginUrl = `${config.backendBaseUrl}/api/auth/login`
  const meUrl = `${config.backendBaseUrl}/api/user/me`
  const refreshUrl = `${config.backendBaseUrl}/api/auth/refresh`

  let accessToken = ''
  let refreshToken = ''

  await runStep('用户端账号密码登录成功', async () => {
    const response = await requestJson('POST', loginUrl, {
      body: {
        username: config.testUserUsername,
        password: config.testUserPassword,
      },
    })
    const data = unwrapResult(response)
    ensure(data?.accessToken, '缺少 accessToken')
    ensure(data?.refreshToken, '缺少 refreshToken')
    ensure(data?.username === config.testUserUsername, `返回用户名异常: ${data?.username || '<empty>'}`)
    accessToken = data.accessToken
    refreshToken = data.refreshToken
  })

  await runStep('用户端当前用户资料接口可用', async () => {
    ensure(accessToken, '未拿到 accessToken，跳过资料校验')
    const response = await requestJson('GET', meUrl, { token: accessToken })
    const data = unwrapResult(response)
    ensure(data?.username === config.testUserUsername, `资料用户名异常: ${data?.username || '<empty>'}`)
    ensure(data?.role, '资料缺少 role')
  })

  await runStep('用户端 refresh token 续签成功', async () => {
    ensure(refreshToken, '未拿到 refreshToken，无法测试续签')
    const response = await requestJson('POST', refreshUrl, {
      body: { refreshToken },
    })
    const data = unwrapResult(response)
    ensure(data?.accessToken, '续签后缺少 accessToken')
    ensure(data?.refreshToken, '续签后缺少 refreshToken')

    const profileResponse = await requestJson('GET', meUrl, { token: data.accessToken })
    const profile = unwrapResult(profileResponse)
    ensure(profile?.username === config.testUserUsername, '续签后的 accessToken 访问 /api/user/me 失败')
  })

  await runStep('用户端错误密码返回稳定错误信息', async () => {
    const response = await requestJson('POST', loginUrl, {
      body: {
        username: config.testUserUsername,
        password: `${config.testUserPassword}_wrong`,
      },
    })
    ensure(response.status === 401, `期望 401，实际 ${response.status}`)
    ensure(response.payload?.code === 401, `期望 code=401，实际 ${JSON.stringify(response.payload)}`)
    ensure(Boolean(response.payload?.msg), '错误响应缺少 msg')
  })
}

async function testAdminLoginChain() {
  const loginUrl = `${config.backendBaseUrl}/admin/auth/login`
  const refreshUrl = `${config.backendBaseUrl}/admin/auth/refresh`
  const dashboardUrl = `${config.backendBaseUrl}/admin/dashboard/statistics`

  let accessToken = ''
  let refreshToken = ''

  await runStep('后台账号密码登录成功', async () => {
    const response = await requestJson('POST', loginUrl, {
      body: {
        username: config.testAdminUsername,
        password: config.testAdminPassword,
      },
    })
    const data = unwrapResult(response)
    ensure(data?.accessToken, '缺少 accessToken')
    ensure(data?.refreshToken, '缺少 refreshToken')
    accessToken = data.accessToken
    refreshToken = data.refreshToken
  })

  await runStep('后台受保护接口可访问', async () => {
    ensure(accessToken, '未拿到后台 accessToken')
    const response = await requestJson('GET', dashboardUrl, { token: accessToken })
    const data = unwrapResult(response)
    ensure(data && typeof data === 'object', '后台统计接口返回为空')
  })

  await runStep('后台 refresh token 续签成功', async () => {
    ensure(refreshToken, '未拿到后台 refreshToken')
    const response = await requestJson('POST', refreshUrl, {
      body: { refreshToken },
    })
    const data = unwrapResult(response)
    ensure(data?.accessToken, '后台续签后缺少 accessToken')
    ensure(data?.refreshToken, '后台续签后缺少 refreshToken')

    const dashboardResponse = await requestJson('GET', dashboardUrl, { token: data.accessToken })
    unwrapResult(dashboardResponse)
  })

  await runStep('后台错误密码返回稳定错误信息', async () => {
    const response = await requestJson('POST', loginUrl, {
      body: {
        username: config.testAdminUsername,
        password: `${config.testAdminPassword}_wrong`,
      },
    })
    ensure(response.status === 401, `期望 401，实际 ${response.status}`)
    ensure(response.payload?.code === 401, `期望 code=401，实际 ${JSON.stringify(response.payload)}`)
    ensure(Boolean(response.payload?.msg), '后台错误响应缺少 msg')
  })
}

async function testGithubChain() {
  const githubUrl = `${config.backendBaseUrl}/api/auth/github/url`

  await runStep('GitHub 登录地址接口可用', async () => {
    const response = await requestJson('GET', githubUrl)

    if (response.status >= 400) {
      const message = response.payload?.msg || response.payload?.message || response.rawText || '未知错误'
      if (message.includes('GitHub OAuth 未配置')) {
        throw new Error('GitHub OAuth 未配置，无法继续自动验证授权地址')
      }
      throw new Error(`HTTP ${response.status}: ${message}`)
    }

    const data = unwrapResult(response)
    ensure(data?.url, '缺少 GitHub authorize url')
    ensure(data?.state, '缺少 GitHub state')

    const parsed = new URL(data.url)
    ensure(parsed.hostname === 'github.com', `GitHub host 异常: ${parsed.hostname}`)
    ensure(parsed.pathname === '/login/oauth/authorize', `GitHub path 异常: ${parsed.pathname}`)
    ensure(parsed.searchParams.get('state') === data.state, 'state 与返回 payload 不一致')
    ensure(Boolean(parsed.searchParams.get('client_id')), 'GitHub authorize url 缺少 client_id')
    ensure(Boolean(parsed.searchParams.get('redirect_uri')), 'GitHub authorize url 缺少 redirect_uri')
  }, { warning: !config.requireGithub })
}

async function main() {
  logInfo(`Backend: ${config.backendBaseUrl}`)
  logInfo(`User frontend: ${config.userFrontendUrl}`)
  logInfo(`CMS frontend: ${config.cmsFrontendUrl}`)

  await probeFrontend('用户端前台', config.userFrontendUrl)
  await probeFrontend('后台前台', config.cmsFrontendUrl)

  await testUserLoginChain()
  await testAdminLoginChain()
  await testGithubChain()

  console.log('')
  console.log('Regression summary:')
  console.log(`- failures: ${failures.length}`)
  console.log(`- warnings: ${warnings.length}`)

  if (warnings.length) {
    console.log('- warning details:')
    warnings.forEach((item) => console.log(`  - ${item}`))
  }

  if (failures.length) {
    console.log('- failure details:')
    failures.forEach((item) => console.log(`  - ${item}`))
    process.exit(1)
  }
}

main().catch((error) => {
  logFail(`脚本异常退出 - ${error.message}`)
  process.exit(1)
})

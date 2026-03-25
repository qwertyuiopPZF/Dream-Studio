# Auth Regression Checklist

这份清单配合 `auth-regression.mjs` 使用，覆盖四条核心链路：

- 用户端账号密码登录
- 后台账号密码登录
- GitHub OAuth 登录
- Refresh Token 续签

## 0. 前置准备

- 启动后端：确认 `http://localhost:8081` 可访问。
- 启动用户端：确认 `http://localhost:3000` 可访问。
- 启动后台端：确认 `http://localhost:8080` 可访问。
- 准备测试账号：默认可使用 `admin / 123456`。
- 如果需要验证 GitHub OAuth，全量配置以下环境变量后再启动后端：
  - `GITHUB_OAUTH_CLIENT_ID`
  - `GITHUB_OAUTH_CLIENT_SECRET`
  - `GITHUB_OAUTH_REDIRECT_URI`

## 1. 自动回归脚本

### 1.1 直接执行

```bash
node auth-regression.mjs
```

### 1.2 自定义测试账号

```bash
TEST_USER_USERNAME=tester TEST_USER_PASSWORD=secret TEST_ADMIN_USERNAME=admin TEST_ADMIN_PASSWORD=123456 node auth-regression.mjs
```

### 1.3 把 GitHub 视为必过项

```bash
REQUIRE_GITHUB=true node auth-regression.mjs
```

### 1.4 自动脚本预期覆盖点

- 用户端登录接口 `POST /api/auth/login`
- 用户端资料接口 `GET /api/user/me`
- 用户端续签接口 `POST /api/auth/refresh`
- 后台登录接口 `POST /admin/auth/login`
- 后台受保护接口 `GET /admin/dashboard/statistics`
- 后台续签接口 `POST /admin/auth/refresh`
- GitHub 授权地址接口 `GET /api/auth/github/url`
- 登录失败时的统一错误结构 `code + msg`

## 2. 用户端登录手工验证

### 2.1 正向登录

- 打开 `http://localhost:3000/login`
- 输入正确用户名和密码
- 点击“登录”
- 预期结果：
  - 跳转到 `redirect` 指定页面，未指定时跳到 `/profile`
  - 页面进入已登录态
  - 刷新页面后仍保持登录
  - `localStorage` 中存在 `accessToken`、`refreshToken`

### 2.2 公开浏览 + 按需登录

- 未登录直接访问 `http://localhost:3000/home`
- 未登录直接访问文章详情、论坛、关于页
- 预期结果：
  - 页面允许直接访问，不会被统一重定向到登录页
- 在评论区点击“去登录”或在需登录动作上触发拦截
- 预期结果：
  - 跳到 `/login?redirect=<当前页面>`
  - 登录后回到刚才页面

### 2.3 错误密码

- 在登录页输入错误密码
- 预期结果：
  - 页面提示“用户名或密码错误”或后端明确错误信息
  - 不会产生残留登录态

### 2.4 资料接口弱依赖验证

- 登录成功后，观察是否立刻进入已登录态
- 即使 `/api/user/me` 出现短暂失败，也不应把整次登录判为失败
- 预期结果：
  - 先进入已登录态
  - 资料稍后补齐，不出现“已登录但又被踢出”的抖动

## 3. 后台登录手工验证

### 3.1 正向登录

- 打开 `http://localhost:8080/login`
- 使用正确账号密码登录
- 预期结果：
  - 登录后进入后台首页
  - 页面可以正常请求仪表盘、文章管理等后台接口
  - 刷新页面后仍保留登录态

### 3.2 错误密码

- 输入错误密码登录
- 预期结果：
  - 页面出现稳定错误提示
  - 不会卡住不返回
  - 不会写入错误 token

## 4. GitHub OAuth 手工验证

### 4.1 授权地址阶段

- 打开 `http://localhost:3000/login`
- 切到 “GitHub 登录” 页签
- 点击“使用 GitHub 登录”
- 预期结果：
  - 成功跳转到 GitHub 授权页
  - 地址中带有 `client_id`、`redirect_uri`、`state`

### 4.2 首次登录

- 使用一个从未绑定过的 GitHub 账号授权
- 预期结果：
  - 成功回到站点
  - 自动创建站内账号
  - 页面出现“设置站内密码”流程
  - 设置密码成功后跳到个人中心或 redirect 页面

### 4.3 已绑定账号登录

- 再次使用同一个 GitHub 账号登录
- 预期结果：
  - 不再进入设置密码页
  - 直接完成登录并进入目标页面

### 4.4 异常场景

- 修改回调参数中的 `state` 或让其过期
- 预期结果：
  - 页面出现清晰错误提示
  - 不产生脏 token

## 5. Refresh Token 手工验证

### 5.1 用户端自动续签

- 先登录用户端
- 人工将 `accessToken` 改成无效值，但保留有效 `refreshToken`
- 在已登录页触发一个需要鉴权的接口，例如刷新个人中心或评论删除/提交等
- 预期结果：
  - 前端先请求 `/api/auth/refresh`
  - 续签成功后自动重放原请求
  - 页面不需要手动重新登录

### 5.2 后台自动续签

- 先登录后台
- 人工将 `accessToken` 改成无效值，但保留有效 `refreshToken`
- 刷新后台首页或进入需要鉴权的数据页
- 预期结果：
  - 前端先请求 `/admin/auth/refresh`
  - 续签成功后页面继续正常加载

### 5.3 续签失败

- 同时把 `accessToken` 和 `refreshToken` 都改成无效值
- 触发一个受保护请求
- 预期结果：
  - 前端清理登录态
  - 回到未登录状态
  - 页面提示“登录已过期，请重新登录”或等价信息

## 6. 回归结论记录模板

建议每次回归后记录以下结果：

- 测试时间：
- 后端分支 / 提交：
- 用户端分支 / 提交：
- 后台端分支 / 提交：
- 用户端登录：通过 / 失败
- 后台登录：通过 / 失败
- GitHub 登录：通过 / 跳过 / 失败
- Refresh 续签：通过 / 失败
- 备注：

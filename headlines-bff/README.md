## Headlines BFF

不改动原后端源码的前提下，为“新闻头条”专栏提供聚合/增强接口，支持：
- 头条列表（按发布时间倒序，含作者信息）
- 文章详情（可选携带评论）
- 点赞/取消点赞（本地持久到 `data/likes.json`）
- 发表评论、查询评论（透传现有后端 `/api/comments`）

### 运行
```bash
cd headlines-bff
npm install
npm run dev   # 默认端口 3001，可用 PORT 指定
```

环境变量：
- `BACKEND_BASE`：后端基地址，默认 `http://localhost:8080`
- `PORT`：BFF 端口，默认 `3001`

### 路由
- `GET /api/headlines?page=1&pageSize=10`  
  返回 `list`（含作者与点赞态）+ `pagination`。
- `GET /api/headlines/:id?includeComments=true`  
  返回文章详情；`includeComments=true` 时附带评论并补齐 `stats.comments`。
- `POST /api/headlines/:id/like`  
  Body: `{ "action": "like" | "unlike" }`，需 `Authorization: Bearer <token>`；返回 `{ likes, liked }`。
- `GET /api/headlines/:id/comments`  
  透传评论列表，附统一响应包装。
- `POST /api/headlines/:id/comments`  
  Body: `{ "content": "..." }`，透传到后端 `/api/comments/comment`，需要登录。

### 说明
- 点赞数据存储在 `data/likes.json`，文件自动创建，适合开发/演示场景；生产请替换为持久化存储。
- 统一响应格式：`{ code, message, data, timestamp, requestId }`，成功 `code=200`，其余遵循业务含义（401/422/502等）。
- 需要后端已有的 JWT 登录接口提供 `Authorization` 头；用户 ID 通过 token 载荷解析（不校验签名，仅作聚合用途）。  

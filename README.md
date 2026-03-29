<h1 align="center">𝒟𝓇𝑒𝒶𝓂 𝒮𝓉𝓊𝒹𝒾𝑜</h1>

<p align="center"><strong>Dream Studio</strong></p>

![Vue 3](https://img.shields.io/badge/Vue-3-42b883?logo=vue.js&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6db33f?logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ed?logo=docker&logoColor=white)

Dream Studio 是一个面向个人内容创作与轻社区运营的全栈博客系统，围绕「内容发布 + 用户互动 + 后台治理」三条主线构建，适合作为个人博客、作品展示站、创作者主页，或小型社区项目的基础模板。

它提供博客、动态、论坛、资源展示、通知中心、个人工作台与管理后台等完整能力，同时保留了前后端分离、权限控制和容器化部署的工程化基础，方便二次开发和落地部署。

## 导航

- [项目亮点](#项目亮点)
- [界面预览](#界面预览)
- [功能矩阵](#功能矩阵)
- [技术栈](#技术栈)
- [仓库结构](#仓库结构)
- [快速开始](#快速开始)
- [本地开发](#本地开发)
- [关键配置](#关键配置)
- [默认账号与安全提示](#默认账号与安全提示)
- [数据说明](#数据说明)
- [部署建议](#部署建议)

## 项目亮点

- 一套系统覆盖博客发布、动态分享、论坛交流、资源展示与通知触达，适合个人创作者或小型内容社区快速起步。
- 提供前台站点、用户工作台与管理后台三类使用视角，既能支撑内容消费，也能支持内容生产和运营治理。
- 前端基于 Vue 3 + Vite，后端基于 Spring Boot 3 + Spring Security，兼顾开发效率与后续扩展性。
- 内置角色与 capability 权限模型，前端按能力展示页面，后端按职责划分普通用户端与管理端接口。
- 支持 Docker Compose 一键启动，同时兼容本地开发、传统部署与 S3 兼容对象存储方案。

## 界面预览
<img width="1919" height="916" alt="屏幕截图 2026-03-29 010937" src="https://github.com/user-attachments/assets/f50f5fa8-8dac-4d5e-942a-b1ef842e590b" />

<img width="1919" height="895" alt="屏幕截图 2026-03-29 010959" src="https://github.com/user-attachments/assets/468305b8-86dc-40ed-815a-4d0494e6fea3" />

<img width="1919" height="891" alt="屏幕截图 2026-03-29 011022" src="https://github.com/user-attachments/assets/87a86200-1f25-4e66-bef7-5f6c549d22b0" />

<img width="1914" height="889" alt="屏幕截图 2026-03-29 011305" src="https://github.com/user-attachments/assets/c1430e5f-5307-4414-92bd-bf98f284af18" />

<img width="19<img width="1919" height="902" alt="屏幕截图 2026-03-29 012140" src="https://github.com/user-attachments/assets/b9067ae7-6ee6-4269-a576-a696cc1f332c" />


<img width="1903" height="799" alt="屏幕截图 2026-03-29 021151" src="https://github.com/user-attachments/assets/81f21b4b-bf44-4b02-b2af-f3bae0d9723a" />


## 功能矩阵

| 模块 | 主要能力 |
| --- | --- |
| 博客前台 | 首页文章流、精华文章轮播、文章详情、分类筛选、标签页、归档页、关于页 |
| 轻社区互动 | 动态（Moment）、论坛帖子、评论互动、热门侧栏、举报处理 |
| 创作者工作台 | 文章管理、动态管理、论坛帖子管理、通知中心、账号资料维护 |
| 管理后台 | 统计面板、文章管理、评论管理、标签管理、分类管理、用户管理、公告管理、站点信息管理、论坛举报处理 |
| 权限控制 | 基于角色与 capability 的页面/接口权限控制 |
| 文件与资源 | 本地上传目录、资源中心、S3 兼容对象存储接口 |

## 技术栈

| 分层 | 技术选型 |
| --- | --- |
| 前端 | Vue 3、Vite、Vue Router、Pinia、Element Plus、Axios |
| 编辑与展示 | `md-editor-v3`、`highlight.js`、`prismjs` |
| 后端 | Java 21、Spring Boot 3、Spring Security、MyBatis-Plus、PageHelper、JWT |
| 服务能力 | Redis、Spring Boot Actuator、SpringDoc OpenAPI |
| 数据与存储 | MySQL 8、Redis、本地上传目录、S3 兼容对象存储 |
| 部署 | Docker、Docker Compose、Nginx |

## 仓库结构

```text
Dream-Studio/
|- blog-view/      # Vue 3 前端，包含前台页面、用户中心、管理后台
|- blog-backend/   # Spring Boot 多模块后端
|  |- blog-common/
|  |- blog-pojo/
|  `- blog-server/ # 主服务模块
|- sql/            # 数据库初始化与补充脚本
|- upload_data/    # 本地上传文件目录
`- docker-compose.yml
```

## 快速开始

### 方式一：Docker Compose

这是最快的体验方式，适合本地联调、演示和快速预览项目。

```bash
docker compose up --build -d
```

启动后默认服务如下：

| 服务 | 地址 |
| --- | --- |
| 前端 | `http://localhost:3000` |
| 后端 | `http://localhost:8080` |
| MySQL | `localhost:3306` |
| Redis | `localhost:6379` |

停止服务：

```bash
docker compose down
```

同时清理数据卷：

```bash
docker compose down -v
```

## 本地开发

### 环境要求

- Node.js 20+
- npm 10+
- Java 21
- Maven 3.9+
- MySQL 8
- Redis 6+

### 1. 初始化数据库

创建数据库并导入初始化脚本：

```bash
mysql -u root -p < sql/init.sql
```

默认脚本会创建并使用数据库：`eleven_blog`。

### 2. 启动后端

在项目根目录执行：

```bash
mvn -f blog-backend/pom.xml -pl blog-server -am spring-boot:run
```

默认后端地址：`http://localhost:8080`

接口文档地址：

```text
http://localhost:8080/swagger-ui/index.html
```

### 3. 启动前端

进入前端目录并启动开发服务：

```bash
cd blog-view
npm install
npm run dev
```

默认前端地址：`http://localhost:3000`

前端默认环境变量见 `blog-view/.env`：

```env
VITE_APP_API_URL=http://localhost:8080/api
VITE_APP_UPLOAD_URL=http://localhost:8080
```

## 关键配置

后端主配置文件位于 `blog-backend/blog-server/src/main/resources/application.yml`，支持通过环境变量覆盖。

### 数据库与 Redis

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATA_REDIS_HOST`
- `SPRING_DATA_REDIS_PORT`
- `SPRING_DATA_REDIS_PASSWORD`
- `SPRING_DATA_REDIS_DATABASE`

### 文件上传

- `FILE_UPLOAD_DIR`：本地上传目录

### 对象存储

- `OBJECT_STORAGE_ENDPOINT`
- `OBJECT_STORAGE_REGION`
- `OBJECT_STORAGE_ACCESS_KEY`
- `OBJECT_STORAGE_SECRET_KEY`
- `OBJECT_STORAGE_BUCKET`
- `OBJECT_STORAGE_PUBLIC_URL`
- `OBJECT_STORAGE_KEY_PREFIX`
- `OBJECT_STORAGE_PATH_STYLE_ACCESS`

### 站点信息

- `BLOG_OWNER_EMAIL`
- `BLOG_OWNER_AVATAR`
- `BLOG_OWNER_NICKNAME`

### GitHub OAuth

- `GITHUB_OAUTH_CLIENT_ID`
- `GITHUB_OAUTH_CLIENT_SECRET`
- `GITHUB_OAUTH_REDIRECT_URI`

## 默认账号与安全提示

> 后端启动时会自动确保默认管理员账号存在：`admin / 123456`。
>
> 首次启动后请立即修改默认管理员密码，不要在公开环境直接使用默认凭据。
>
> 当前仓库中的 `docker-compose.yml` 与 `application.yml` 含有本地开发用途的默认密码、示例 OAuth 配置和对象存储配置，部署前请全部替换。
>
> 建议将敏感信息迁移到环境变量或外部密钥管理系统，不要把生产密钥直接提交到仓库。

## 数据说明

`sql/init.sql` 提供了基础表结构与部分演示数据，覆盖的核心数据对象包括：

- 文章 `article`
- 分类 `category`
- 标签 `tags`
- 评论 `comment`
- 动态 `moment`
- 系统配置 `system_config`

应用启动时还会自动确保以下业务表存在：

- 用户账号 `user_account`
- 论坛帖子 `forum_post`
- 论坛举报 `forum_report`
- 用户通知 `user_notification`
- 站点公告 `announcement`
- 资源中心 `site_resource`

`sql/` 目录中还提供了一些补充脚本，便于按需初始化或扩展相关业务能力。

## 部署建议

- 开发环境可直接使用仓库内的 Docker 配置快速启动。
- 生产环境建议将 MySQL、Redis、对象存储拆分为独立服务，并调整默认端口暴露策略。
- 部署前请统一替换默认账号、密码、示例 OAuth 配置与对象存储配置。
- 若启用 GitHub OAuth，请确保 `GITHUB_OAUTH_REDIRECT_URI` 与前端登录页地址保持一致。

如果你准备基于该项目继续扩展，可以优先从主题定制、权限细化、审核流、消息体系或对象存储接入这几个方向继续演进。

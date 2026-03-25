# Local MySQL Setup

## 现状

- 旧的 Windows 服务名为 `MySQL80`
- 旧数据目录位于 `C:\ProgramData\MySQL\MySQL Server 8.0`
- 当前服务和数据目录都无法用普通权限清理，需要管理员权限
- 新安装包已下载到 `D:\Downloads\MySQL\MySQL_8.4.8_Machine_X64_wix_en-US.msi`

## 1. 先清理旧残留（管理员 PowerShell）

在管理员 PowerShell 中执行：

```powershell
sc delete MySQL80
Remove-Item "C:\ProgramData\MySQL\MySQL Server 8.0" -Recurse -Force
```

如果只想先保留旧数据备份，也可以改成：

```powershell
Rename-Item "C:\ProgramData\MySQL\MySQL Server 8.0" "MySQL Server 8.0.backup"
```

## 2. 安装本地 MySQL

- 双击运行 `D:\Downloads\MySQL\MySQL_8.4.8_Machine_X64_wix_en-US.msi`
- 建议保持以下选项：
  - 端口：`3306`
  - 用户：`root`
  - 密码：`123456`
  - 字符集：`utf8mb4`

这样可以直接匹配当前后端默认配置，不需要额外改环境变量。

## 3. 初始化数据库

安装完成后，在 MySQL 命令行中执行：

```sql
SOURCE C:/Users/33313/Desktop/Dream-Studio/sql/init.sql;
```

或者手动执行：

```sql
CREATE DATABASE IF NOT EXISTS eleven_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE eleven_blog;
```

然后再导入 `sql/init.sql`。

## 4. 后端本地连接配置

项目默认配置位于 `blog-backend/blog-server/src/main/resources/application.yml`，默认值如下：

```text
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/eleven_blog?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=123456
```

如果你安装 MySQL 时使用的密码不是 `123456`，启动后端前请覆盖环境变量。

PowerShell 示例：

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/eleven_blog?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="你的密码"
```

## 5. 启动前验证

先确认本地 MySQL 正常监听：

```powershell
Get-NetTCPConnection -LocalPort 3306 -State Listen
```

再验证数据库存在：

```sql
SHOW DATABASES;
USE eleven_blog;
SHOW TABLES;
```

## 6. 启动后端

在 `blog-backend` 目录执行：

```bash
mvn -pl blog-server spring-boot:run
```

## 7. 注意事项

- 当前项目还依赖 Redis，本地 `6379` 也需要可用，否则部分登录链路和 GitHub OAuth state 校验仍可能异常
- `sql/init.sql` 中原先有冲突标记，已经清理，可以直接导入

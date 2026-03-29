create table article
(
    id           bigint auto_increment comment '文章ID'
        primary key,
    author_id    bigint                             null comment '作者用户ID',
    title        varchar(200)                       not null comment '文章标题',
    summary      varchar(500)                       null comment '文章摘要',
    content      longtext                           null comment '文章内容（Markdown格式）',
    category_id  bigint                             null comment '分类ID',
    view_count   int      default 0                 null comment '浏览次数',
    is_comment   tinyint  default 1                 null comment '是否允许评论：0-否，1-是',
    status       tinyint  default 1                 null comment '状态：0-草稿，1-已发布，2-已删除',
    publish_time datetime                           null comment '发布时间',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    cover_image  varchar(1000)                      null,
    tags         varchar(256)                       null
)
    comment '文章表';

create index idx_category_id
    on article (category_id);

create index idx_publish_time
    on article (publish_time);

create index idx_status
    on article (status);

create table category
(
    id            bigint auto_increment comment '分类ID'
        primary key,
    name          varchar(50)                        not null comment '分类名称',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    article_count int                                null,
    constraint uk_name
        unique (name)
)
    comment '分类表';

create table comment
(
    id              bigint auto_increment comment '评论ID'
        primary key,
    user_id         bigint                               null comment '评论用户ID',
    nickname        varchar(100)                         not null comment '昵称',
    email           varchar(200)                         null comment '邮箱',
    website         varchar(500)                         null comment '网站',
    avatar          varchar(500)                         null comment '头像地址',
    ip              varchar(50)                          null comment 'IP地址',
    content         text                                 not null comment '评论内容',
    blog_id         bigint                               null comment '文章ID',
    status          tinyint    default 1                 null comment '是否公开：0-否，1-是',
    notice          tinyint(1) default 0                 null comment '邮件提醒：0-否，1-是',
    create_time     datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    Title           varchar(388)                         null,
    adminComment    tinyint(1)                           null,
    parentCommentId mediumtext                           null,
    page            varchar(200)                         null comment '所在页面：0普通文章，1友链页面'
)
    comment '评论表';

create index idx_article_id
    on comment (blog_id);

create index idx_create_time
    on comment (create_time);

create table forum_post
(
    id                 bigint auto_increment comment '帖子ID'
        primary key,
    author_id          bigint                               null comment '作者用户ID',
    title              varchar(200)                         not null comment '帖子标题',
    summary            varchar(500)                         null comment '帖子摘要',
    content            longtext                             not null comment '帖子正文（Markdown）',
    nickname           varchar(100)                         not null comment '发帖昵称',
    email              varchar(200)                         null comment '邮箱',
    avatar             varchar(500)                         null comment '头像地址',
    view_count         int        default 0                 not null comment '浏览次数',
    is_pinned          tinyint(1) default 0                 not null comment '是否置顶',
    is_featured        tinyint(1) default 0                 not null comment '是否加精',
    create_time        datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time        datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    last_activity_time datetime   default CURRENT_TIMESTAMP not null comment '最后活跃时间'
)
    comment '论坛帖子表';

create index idx_forum_post_activity
    on forum_post (last_activity_time);

create index idx_forum_post_create_time
    on forum_post (create_time);

create index idx_forum_post_featured
    on forum_post (is_featured, is_pinned);

create table forum_report
(
    id            bigint auto_increment comment '举报ID'
        primary key,
    reporter_id   bigint                                not null comment '举报人用户ID',
    target_type   varchar(50)                           not null comment '举报目标类型',
    target_id     bigint                                not null comment '举报目标ID',
    target_title  varchar(255)                          not null comment '举报目标标题',
    reason        varchar(50)                           not null comment '举报原因',
    detail        varchar(500)                          null comment '补充说明',
    status        varchar(30) default 'PENDING'         not null comment '处理状态',
    reviewer_id   bigint                                null comment '处理人用户ID',
    reviewer_note varchar(500)                          null comment '处理备注',
    create_time   datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '论坛举报表';

create index idx_forum_report_reporter
    on forum_report (reporter_id);

create index idx_forum_report_status
    on forum_report (status);

create index idx_forum_report_target
    on forum_report (target_type, target_id);

create table moment
(
    id           bigint auto_increment comment '动态ID'
        primary key,
    content      text                               not null comment '动态内容',
    image        varchar(1000)                      null comment '动态图片（多个图片用逗号分隔）',
    is_public    tinyint  default 1                 null comment '是否公开：0-私密，1-公开',
    status       tinyint  default 1                 null comment '动态状态：0-草稿，1-已发布',
    view_count   int      default 0                 null comment '浏览次数',
    publish_time datetime                           null comment '发布时间',
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '动态表';

create index idx_create_time
    on moment (create_time);

create index idx_is_public
    on moment (is_public);

create index idx_publish_time
    on moment (publish_time);

create index idx_status
    on moment (status);

create table operation_log
(
    id             bigint auto_increment comment '日志ID'
        primary key,
    module         varchar(50)                        null comment '操作模块',
    type           varchar(20)                        null comment '操作类型：CREATE-新增，UPDATE-修改，DELETE-删除，QUERY-查询',
    description    varchar(500)                       null comment '操作描述',
    request_method varchar(10)                        null comment '请求方法：GET/POST/PUT/DELETE',
    request_url    varchar(255)                       null comment '请求URL',
    request_params text                               null comment '请求参数（JSON）',
    response_data  text                               null comment '响应数据（JSON）',
    user_id        bigint                             null comment '操作用户ID',
    username       varchar(50)                        null comment '操作用户名',
    ip             varchar(50)                        null comment 'IP地址',
    user_agent     varchar(500)                       null comment '用户代理',
    execution_time int                                null comment '执行时间（毫秒）',
    status         tinyint  default 1                 null comment '状态：0-失败，1-成功',
    error_message  text                               null comment '错误信息',
    create_time    datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '操作日志表';

create index idx_create_time
    on operation_log (create_time);

create index idx_user_id
    on operation_log (user_id);

create table system_config
(
    id           bigint auto_increment comment '配置ID'
        primary key,
    config_key   varchar(100)                          not null comment '配置键',
    config_value text                                  null comment '配置值',
    config_type  varchar(20) default 'STRING'          null comment '配置类型：STRING/NUMBER/BOOLEAN/JSON',
    description  varchar(200)                          null comment '配置描述',
    create_time  datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_config_key
        unique (config_key)
)
    comment '系统配置表';

create table tags
(
    id            bigint auto_increment comment '标签ID'
        primary key,
    name          varchar(50)                        not null comment '标签名称',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    article_count int                                null,
    constraint uk_name
        unique (name)
)
    comment '标签表';

create table user
(
    id              bigint auto_increment comment '用户ID'
        primary key,
    username        varchar(50)                           not null comment '用户名',
    password        varchar(255)                          not null comment '密码（加密）',
    nickname        varchar(50)                           null comment '昵称',
    email           varchar(100)                          null comment '邮箱',
    avatar          varchar(255)                          null comment '头像URL',
    role            varchar(20) default 'USER'            null comment '角色：ADMIN-管理员，USER-普通用户',
    status          tinyint     default 1                 null comment '状态：0-禁用，1-启用',
    last_login_time datetime                              null comment '最后登录时间',
    last_login_ip   varchar(50)                           null comment '最后登录IP',
    create_time     datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_username
        unique (username)
)
    comment '用户表';

create table user_account
(
    id              bigint auto_increment comment '用户ID'
        primary key,
    username        varchar(100)                          not null comment '系统用户名',
    github_id       bigint                                null comment 'GitHub 用户ID',
    github_login    varchar(100)                          null comment 'GitHub 登录名',
    nickname        varchar(100)                          not null comment '昵称',
    avatar          varchar(500)                          null comment '头像地址',
    email           varchar(255)                          null comment '邮箱',
    phone           varchar(30)                           null comment '手机号',
    password_hash   varchar(255)                          null comment '站内密码哈希',
    bio             varchar(500)                          null comment '简介',
    role            varchar(30) default 'USER'            not null comment '角色',
    status          tinyint(1)  default 1                 not null comment '状态',
    last_login_time datetime                              null comment '最后登录时间',
    create_time     datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time     datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_user_account_github_id
        unique (github_id),
    constraint uk_user_account_github_login
        unique (github_login),
    constraint uk_user_account_username
        unique (username)
)
    comment '站点用户账号表（GitHub OAuth）';

create table user_notification
(
    id                bigint auto_increment comment '通知ID'
        primary key,
    user_id           bigint                               not null comment '接收人用户ID',
    type              varchar(50)                          not null comment '通知类型',
    title             varchar(255)                         not null comment '通知标题',
    content           varchar(500)                         not null comment '通知内容',
    target_type       varchar(50)                          null comment '关联目标类型',
    target_id         bigint                               null comment '关联目标ID',
    related_report_id bigint                               null comment '关联举报ID',
    is_read           tinyint(1) default 0                 not null comment '是否已读',
    create_time       datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    read_time         datetime                             null comment '已读时间'
)
    comment '用户通知表';

create index idx_user_notification_read
    on user_notification (user_id, is_read);

create index idx_user_notification_user
    on user_notification (user_id);



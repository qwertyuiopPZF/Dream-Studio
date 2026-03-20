<template>
  <el-card shadow="never" class="forum-entry-card">
    <div class="forum-entry-head">
      <div>
        <h2>Forum 入口</h2>
        <p>论坛发帖、查看和审核现在统一走主站的 Forum 与个人中心。</p>
      </div>
      <el-button round @click="openForumHome">打开 Forum</el-button>
    </div>

    <div class="forum-entry-grid">
      <article class="entry-block">
        <h3>前台 Forum</h3>
        <p>查看帖子流、热门话题和真实用户发帖体验。</p>
        <el-button type="primary" plain @click="openForumHome">进入帖子页</el-button>
      </article>

      <article class="entry-block">
        <h3>个人中心管理</h3>
        <p>论坛内容审核、举报处理和通知联动已经整合到主站个人中心。</p>
        <el-button plain @click="openProfileCenter">前往个人中心</el-button>
      </article>
    </div>
  </el-card>
</template>

<script setup>
const frontAppBaseUrl = (import.meta.env.VITE_BLOG_VIEW_URL || "http://localhost:5173").replace(/\/$/, "")

const openForumHome = () => {
  window.open(`${frontAppBaseUrl}/forum`, "_blank")
}

const openProfileCenter = () => {
  window.open(`${frontAppBaseUrl}/profile`, "_blank")
}
</script>

<style scoped>
.forum-entry-card {
  border-radius: 16px;
}

.forum-entry-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.forum-entry-head h2 {
  margin: 0 0 8px;
}

.forum-entry-head p,
.entry-block p {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

.forum-entry-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.entry-block {
  padding: 20px;
  border-radius: 14px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #f8fafc;
}

.entry-block h3 {
  margin: 0 0 10px;
}

.entry-block .el-button {
  margin-top: 18px;
}

@media (max-width: 720px) {
  .forum-entry-head {
    flex-direction: column;
  }

  .forum-entry-grid {
    grid-template-columns: 1fr;
  }
}
</style>

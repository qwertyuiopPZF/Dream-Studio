<template>
  <section class="dashboard-shell">
    <div class="hero-card">
      <div>
        <p class="eyebrow">Admin Dashboard</p>
        <h2>后台首页已就绪</h2>
        <p class="hero-copy">
          这里是后台工作台入口。你可以从这里快速进入文章管理、动态发布和站点配置模块。
        </p>
      </div>

      <div class="hero-actions">
        <el-button type="primary" @click="router.push('/admin/articlemgmt')">进入内容管理</el-button>
        <el-button plain @click="router.push('/admin/site')">进入站点配置</el-button>
      </div>
    </div>

    <div class="stat-grid">
      <article class="stat-card">
        <span class="stat-label">内容面板</span>
        <strong>Articles</strong>
        <p>管理文章、评论和动态内容。</p>
      </article>

      <article class="stat-card">
        <span class="stat-label">创作入口</span>
        <strong>Write</strong>
        <p>快速发布文章或动态，继续完善内容。</p>
      </article>

      <article class="stat-card">
        <span class="stat-label">站点维护</span>
        <strong>Site</strong>
        <p>统一维护分类、标签和论坛入口。</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.dashboard-shell {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero-card,
.stat-card {
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.88);
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: center;
  padding: 24px;
}

.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #64748b;
}

.hero-card h2 {
  margin: 0 0 10px;
}

.hero-copy {
  margin: 0;
  max-width: 560px;
  line-height: 1.75;
  color: #64748b;
}

.hero-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  padding: 20px;
}

.stat-label {
  display: inline-block;
  margin-bottom: 10px;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #94a3b8;
}

.stat-card strong {
  display: block;
  margin-bottom: 8px;
  font-size: 24px;
  color: #0f172a;
}

.stat-card p {
  margin: 0;
  line-height: 1.7;
  color: #64748b;
}

@media (max-width: 900px) {
  .hero-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>

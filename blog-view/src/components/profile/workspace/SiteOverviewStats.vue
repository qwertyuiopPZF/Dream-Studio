<template>
  <div class="overview-grid">
    <article class="overview-card">
      <span>网站名称</span>
      <strong>{{ siteName || 'Dream Studio' }}</strong>
      <p>{{ description || '暂无站点描述' }}</p>
    </article>
    <article class="overview-card">
      <span>已发布公告</span>
      <strong>{{ announcementCount }}</strong>
      <p>公告会显示在论坛页的“本期公告”区域。</p>
    </article>
    <article class="overview-card">
      <span>管理能力</span>
      <strong>公告发布 / 删除</strong>
      <p>公告管理已独立为单独入口，方便集中维护论坛页展示内容。</p>
    </article>
  </div>
</template>

<script setup>
defineProps({
  siteName: {
    type: String,
    default: '',
  },
  description: {
    type: String,
    default: '',
  },
  announcementCount: {
    type: Number,
    default: 0,
  },
})
</script>

<style scoped>
.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 18px;
}

.overview-card {
  padding: 20px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(248, 250, 252, 0.85);
}

.overview-card span {
  display: block;
  margin-bottom: 10px;
  color: #64748b;
  font-size: 13px;
}

.overview-card strong {
  display: block;
  margin-bottom: 10px;
  font-size: 28px;
  color: #0f172a;
}

.overview-card p {
  margin: 0;
  color: #64748b;
  line-height: 1.7;
}

@media (max-width: 900px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>

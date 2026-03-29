<template>
  <section class="settings-panel">
    <div class="section-row">
      <h3>公告管理</h3>
      <el-button type="primary" plain @click="$emit('open-dialog')">发布公告</el-button>
    </div>

    <el-table :data="announcements" table-layout="fixed">
      <el-table-column prop="title" label="标题" min-width="180" />
      <el-table-column prop="content" label="内容" min-width="260" show-overflow-tooltip />
      <el-table-column label="发布时间" width="180">
        <template #default="{ row }">{{ formatManagementTime(row.publishTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="122">
        <template #default="{ row }">
          <el-button class="table-action-button is-danger" size="small" @click="$emit('remove', row)">
            <el-icon><Delete /></el-icon>
            <span>删除</span>
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup>
import { Delete } from '@element-plus/icons-vue'
import { formatManagementTime } from '@/utils/profileManagement'

defineProps({
  announcements: {
    type: Array,
    default: () => [],
  },
})

defineEmits(['open-dialog', 'remove'])
</script>

<style scoped>
.settings-panel {
  margin-top: 18px;
  padding: 20px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(248, 250, 252, 0.85);
}

.section-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.section-row h3 {
  margin: 0 0 8px;
}

@media (max-width: 720px) {
  .section-row {
    flex-direction: column;
  }
}
</style>

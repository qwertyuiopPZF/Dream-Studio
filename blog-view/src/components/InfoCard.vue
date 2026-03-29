<template>
  <div>
    <el-text class="mx-1">
      <el-card class="info-card">
        <div class="avatar-section">
          <el-avatar shape="square" :size="100" :src="profile.avatar">{{
            profileInitial
          }}</el-avatar>
        </div>
        <div class="info-section">
          <h3>{{ profile.nickname }}</h3>
          <p>{{ profile.bio }}</p>
        </div>
      </el-card></el-text
    >
  </div>
</template>

<script setup>
import { ChatLineRound, Message, Share } from '@element-plus/icons-vue'
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const profile = computed(() => userStore.profile)
const profileInitial = computed(() => profile.value.nickname?.slice(0, 1)?.toUpperCase() || 'D')

const copyProfileLabel = async () => {
  try {
    if (navigator.clipboard) {
      await navigator.clipboard.writeText(`${profile.value.nickname} · ${profile.value.bio}`)
      ElMessage.success('个人信息已复制')
    }
  } catch (error) {
    console.error('复制个人信息失败', error)
  }
}
</script>

<style scoped>
.info-card {
  text-align: center;
  background-color: var(--card-bg-color);
  border: 1px solid var(--card-border-color);
  box-shadow: var(--card-box-shadow);
  transition:
    background-color 0.3s,
    border-color 0.3s;
  box-shadow: none;
  border: 0;
}

.avatar-section {
  margin-bottom: 15px;
}

.info-section h3 {
  margin: 10px 0;
  font-size: 1rem;
  color: var(--app-text-color);
  transition: color 0.3s;
}

.info-section p {
  font-size: 0.9rem;
  color: var(--app-text-color);
  margin-bottom: 20px;
  transition: color 0.3s;
}

.social-links a {
  margin: 0 15px;
  font-size: 24px;
  color: var(--app-text-color);
  text-decoration: none;
  transition: color 0.3s;
}

.social-links a:hover {
  color: var(--link-hover-color);
}
</style>

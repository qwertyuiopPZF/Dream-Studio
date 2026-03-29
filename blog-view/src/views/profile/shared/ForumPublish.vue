<template>
  <workspace-page-shell :title="title" :description="description" @refresh="refreshContent">


    <profile-forum-composer :key="composerKey" />
  </workspace-page-shell>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useWorkspaceRouteBase } from '@/composables/useWorkspaceRouteBase'
import { WORKSPACE_CAPABILITIES } from '@/utils/workspaceCapabilities'
import ProfileForumComposer from '@/components/profile/ProfileForumComposer.vue'
import WorkspacePageShell from '@/components/profile/workspace/WorkspacePageShell.vue'

const router = useRouter()
const userStore = useUserStore()
const { routeBase } = useWorkspaceRouteBase()

const canModerateForumPosts = computed(() =>
  userStore.hasCapability(WORKSPACE_CAPABILITIES.FORUM_POST_MODERATE),
)
const composerKey = ref(0)
const title = '发布帖子'
const description = computed(() => {
  return canModerateForumPosts.value
    ? '管理员可以直接发布论坛帖子，并在独立的帖子管理页继续维护内容状态。'
    : '在这里专注发布新帖子，发帖后的整理和查看移到了独立的帖子管理页。'
})
const helperText = computed(() => {
  return canModerateForumPosts.value
    ? '你可以直接从后台发布帖子，然后切换到帖子管理页维护置顶、加精和删除操作。'
    : '在这里专注写作内容，发布后可以到帖子管理页继续查看和整理自己的讨论。'
})
const refreshContent = () => {
  composerKey.value += 1
}
</script>

<style scoped>
.forum-helper-panel {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-top: 18px;
  padding: 20px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(248, 250, 252, 0.85);
}

.forum-helper-panel h3 {
  margin: 0 0 8px;
}

.forum-helper-panel p {
  margin: 10px 0 0;
  color: #64748b;
  line-height: 1.7;
}

.forum-helper-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

@media (max-width: 720px) {
  .forum-helper-panel {
    flex-direction: column;
  }
}
</style>

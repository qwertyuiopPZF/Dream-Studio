<template>
  <management-page-shell>
    <admin-comment-management-panel :bordered="false" :show-header="false" />
  </management-page-shell>
</template>

<script setup>
import AdminCommentManagementPanel from '@/components/profile/management/AdminCommentManagementPanel.vue'
import ManagementPageShell from '@/components/profile/management/ManagementPageShell.vue'
</script>

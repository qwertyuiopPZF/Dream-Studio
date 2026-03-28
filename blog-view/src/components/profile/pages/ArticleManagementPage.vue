<template>
  <management-page-shell>
    <article-management-panel :bordered="false" :show-header="false" />
  </management-page-shell>
</template>

<script setup>
import ArticleManagementPanel from '@/components/profile/management/ArticleManagementPanel.vue'
import ManagementPageShell from '@/components/profile/management/ManagementPageShell.vue'
</script>

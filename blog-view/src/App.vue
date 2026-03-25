<template>
  <router-view v-slot="{ Component, route }">
    <transition :name="transitionName" mode="out-in">
      <component :is="Component" :key="topLevelRouteKey(route)" />
    </transition>
  </router-view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const transitionName = ref('app-fade')

const topLevelRouteKey = (currentRoute) => currentRoute.matched?.[0]?.path || currentRoute.path

watch(
  () => route.fullPath,
  (to, from) => {
    const enteringAdmin = to.startsWith('/admin')
    const leavingAdmin = from?.startsWith('/admin')

    if (enteringAdmin && !leavingAdmin) {
      transitionName.value = 'app-slide-left'
      return
    }

    if (!enteringAdmin && leavingAdmin) {
      transitionName.value = 'app-slide-right'
      return
    }

    transitionName.value = 'app-fade'
  },
  { immediate: true },
)
</script>

<style>
html,
body,
#app {
  min-height: 100%;
  background: var(--app-page-bg);
}

body {
  margin: 0;
}

html[data-theme='dark']::before {
  content: '';
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(16, 16, 27, 0.38);
  pointer-events: none;
  z-index: 9999;
  transition: background-color 0.3s;
}
.el-header {
  top: 0;
  z-index: 2000;
  background-color: transparent;
}

.app-fade-enter-active,
.app-fade-leave-active,
.app-slide-left-enter-active,
.app-slide-left-leave-active,
.app-slide-right-enter-active,
.app-slide-right-leave-active {
  transition:
    opacity 0.38s ease,
    transform 0.38s ease;
}

.app-fade-enter-from,
.app-fade-leave-to {
  opacity: 0;
}

.app-slide-left-enter-from {
  opacity: 0.88;
  transform: translateX(88px);
}

.app-slide-left-leave-to {
  opacity: 0.4;
  transform: translateX(-88px);
}

.app-slide-right-enter-from {
  opacity: 0.88;
  transform: translateX(-88px);
}

.app-slide-right-leave-to {
  opacity: 0.4;
  transform: translateX(88px);
}
</style>

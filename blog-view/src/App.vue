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
<<<<<<< HEAD
=======
const isWorkspaceRoute = (path = '') => path.startsWith('/admin') || path.startsWith('/profile')
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8

watch(
  () => route.fullPath,
  (to, from) => {
<<<<<<< HEAD
    const enteringAdmin = to.startsWith('/admin')
    const leavingAdmin = from?.startsWith('/admin')

    if (enteringAdmin && !leavingAdmin) {
=======
    const enteringWorkspace = isWorkspaceRoute(to)
    const leavingWorkspace = isWorkspaceRoute(from)

    if (to.startsWith('/admin') && from?.startsWith('/profile')) {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
      transitionName.value = 'app-slide-left'
      return
    }

<<<<<<< HEAD
    if (!enteringAdmin && leavingAdmin) {
=======
    if (to.startsWith('/profile') && from?.startsWith('/admin')) {
      transitionName.value = 'app-slide-right'
      return
    }

    if (enteringWorkspace && !leavingWorkspace) {
      transitionName.value = 'app-slide-left'
      return
    }

    if (!enteringWorkspace && leavingWorkspace) {
>>>>>>> df87942a53c2717282b884e9e8b7a7f8444e1cc8
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

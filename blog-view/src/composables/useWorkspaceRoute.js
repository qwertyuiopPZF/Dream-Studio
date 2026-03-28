import { computed } from 'vue'
import { useRoute } from 'vue-router'

export function useWorkspaceRoute() {
  const route = useRoute()
  const isAdminWorkspace = computed(() => route.path.startsWith('/admin'))
  const workspaceMode = computed(() => (isAdminWorkspace.value ? 'admin' : 'user'))
  const routeBase = computed(() => (isAdminWorkspace.value ? '/admin' : '/profile'))

  return {
    route,
    isAdminWorkspace,
    workspaceMode,
    routeBase,
  }
}

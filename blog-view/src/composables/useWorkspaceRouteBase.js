import { computed } from 'vue'
import { useRoute } from 'vue-router'

export function useWorkspaceRouteBase() {
  const route = useRoute()
  const isAdminRoute = computed(() => route.path.startsWith('/admin'))
  const isUserRoute = computed(() => route.path.startsWith('/profile'))
  const routeBase = computed(() => (isAdminRoute.value ? '/admin' : '/profile'))

  return {
    route,
    isAdminRoute,
    isUserRoute,
    routeBase,
  }
}

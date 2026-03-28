import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { resolveCurrentUserProfile, syncUserProfileFromServerProfile, syncUserProfileFromToken } from '@/utils/userProfile'
import { hasRequiredCapability, resolveCapabilitiesByRole } from '@/utils/workspaceCapabilities'

export const useUserStore = defineStore('user', () => {
  const profile = ref(resolveCurrentUserProfile(localStorage.getItem('accessToken') || ''))

  const isLoggedIn = computed(() => profile.value.isLoggedIn)
  const isAdmin = computed(() => profile.value.role === 'ADMIN')
  const capabilities = computed(() => resolveCapabilitiesByRole(profile.value.role))

  const hydrateProfile = (accessToken = '') => {
    profile.value = resolveCurrentUserProfile(accessToken || localStorage.getItem('accessToken') || '')
    return profile.value
  }

  const hydrateFromToken = (accessToken, fallbackUsername = '') => {
    profile.value = syncUserProfileFromToken(accessToken, fallbackUsername)
    return profile.value
  }

  const hydrateFromServerProfile = (serverProfile = {}) => {
    profile.value = syncUserProfileFromServerProfile(serverProfile)
    return profile.value
  }

  const clearProfile = () => {
    profile.value = resolveCurrentUserProfile('')
  }

  const hasCapability = (requiredCapability) => hasRequiredCapability(capabilities.value, requiredCapability)

  return {
    profile,
    isLoggedIn,
    isAdmin,
    capabilities,
    hydrateProfile,
    hydrateFromToken,
    hydrateFromServerProfile,
    clearProfile,
    hasCapability,
  }
})

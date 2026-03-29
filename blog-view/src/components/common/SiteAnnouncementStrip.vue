<template>
  <section
    v-show="!isDismissed"
    class="announcement-strip"
    :class="{ 'is-empty': !announcements.length }"
    aria-label="站点公告"
  >
    <div class="announcement-badge">站点公告</div>
    <div ref="trackRef" class="announcement-track">
      <span ref="contentRef" class="announcement-content" :style="marqueeStyle">{{
        announcementText
      }}</span>
    </div>
    <button type="button" class="announcement-dismiss" aria-label="关闭公告" @click="dismissAnnouncement">
      x
    </button>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { fetchSiteAnnouncements } from '@/api/site'
import { useAuthStore } from '@/store/auth'

const DISMISSED_LOGIN_SESSION_KEY = 'announcementDismissedLoginSessionId'
const DISMISSED_GUEST_SESSION_KEY = 'announcementDismissedGuestSession'

const announcements = ref([])
const trackRef = ref(null)
const contentRef = ref(null)
const marqueeStart = ref(0)
const marqueeDistance = ref(0)
const marqueeDuration = ref(20)
const authStore = useAuthStore()

let resizeObserver = null

const resolveDismissedState = () => {
  if (typeof window === 'undefined') {
    return false
  }

  if (authStore.loginSessionId) {
    return localStorage.getItem(DISMISSED_LOGIN_SESSION_KEY) === authStore.loginSessionId
  }

  return sessionStorage.getItem(DISMISSED_GUEST_SESSION_KEY) === '1'
}

const isDismissed = ref(resolveDismissedState())

const announcementText = computed(() => {
  if (!announcements.value.length) {
    return '暂无最新公告，欢迎浏览首页、论坛和资源中心的新内容。'
  }

  return announcements.value
    .slice(0, 4)
    .map((item) => `${item.title}：${item.content}`)
    .join('    ·    ')
})

const marqueeStyle = computed(() => ({
  '--marquee-start': `${marqueeStart.value}px`,
  '--marquee-distance': `${marqueeDistance.value}px`,
  '--marquee-duration': `${marqueeDuration.value}s`,
}))

const updateMarqueeMetrics = () => {
  if (isDismissed.value || !trackRef.value || !contentRef.value) {
    return
  }

  const trackWidth = trackRef.value.clientWidth
  const contentWidth = contentRef.value.scrollWidth
  const totalDistance = trackWidth + contentWidth

  marqueeStart.value = trackWidth
  marqueeDistance.value = totalDistance
  marqueeDuration.value = Math.max(totalDistance / 90, 12)
}

const loadAnnouncements = async () => {
  try {
    const response = await fetchSiteAnnouncements()
    announcements.value = Array.isArray(response) ? response : []
  } catch (error) {
    console.error('获取站点公告失败', error)
    announcements.value = []
  }
}

const dismissAnnouncement = () => {
  if (typeof window === 'undefined') {
    return
  }

  if (authStore.loginSessionId) {
    localStorage.setItem(DISMISSED_LOGIN_SESSION_KEY, authStore.loginSessionId)
  } else {
    sessionStorage.setItem(DISMISSED_GUEST_SESSION_KEY, '1')
  }

  isDismissed.value = true
}

watch(announcementText, async () => {
  await nextTick()
  updateMarqueeMetrics()
})

watch(
  () => authStore.loginSessionId,
  async () => {
    isDismissed.value = resolveDismissedState()
    await nextTick()
    updateMarqueeMetrics()
  },
)

onMounted(async () => {
  isDismissed.value = resolveDismissedState()
  await loadAnnouncements()
  await nextTick()
  updateMarqueeMetrics()

  resizeObserver = new ResizeObserver(() => {
    updateMarqueeMetrics()
  })

  if (trackRef.value) {
    resizeObserver.observe(trackRef.value)
  }

  if (contentRef.value) {
    resizeObserver.observe(contentRef.value)
  }
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
})
</script>

<style scoped>
.announcement-strip {
  width: 97%;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 30px;
  padding-right: 6px;

  border: 1px solid rgba(182, 136, 96, 0.22);
  border-radius: 8px;
  background:
    radial-gradient(circle at 12% 50%, rgba(250, 226, 197, 0.36), transparent 28%),
    linear-gradient(90deg, rgba(255, 249, 241, 0.96), rgba(250, 243, 232, 0.98));
  color: #7b4d26;
  box-shadow: 0 14px 32px rgba(122, 92, 53, 0.08);
  overflow: hidden;
}

.announcement-strip.is-empty {
  border-color: rgba(148, 163, 184, 0.18);
  background: linear-gradient(90deg, rgba(249, 250, 251, 0.96), rgba(243, 244, 246, 0.98));
  color: #526173;
}

.announcement-badge {
  flex: none;
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.announcement-track {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  overflow: hidden;
  white-space: nowrap;
}

.announcement-content {
  display: inline-block;
  padding-right: 2rem;
  line-height: 1;
  will-change: transform;
  animation: announcement-marquee var(--marquee-duration, 20s) linear infinite;
}

.announcement-track:hover .announcement-content,
.announcement-track:focus-within .announcement-content {
  animation-play-state: paused;
}

.announcement-dismiss {
  flex: none;
  width: 22px;
  height: 22px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  color: #8b5e34;
  font-size: 12px;
  font-weight: 700;
  line-height: 1;
  cursor: pointer;
  transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.announcement-dismiss:hover,
.announcement-dismiss:focus-visible {
  background: rgba(255, 255, 255, 0.96);
  color: #6f451f;
  transform: scale(1.05);
}

@keyframes announcement-marquee {
  0% {
    transform: translate3d(var(--marquee-start, 0px), 0, 0);
  }

  100% {
    transform: translate3d(calc(var(--marquee-start, 0px) - var(--marquee-distance, 0px)), 0, 0);
  }
}

@media (prefers-reduced-motion: reduce) {
  .announcement-content {
    animation: none;
    transform: none;
  }
}

@media (max-width: 720px) {
  .announcement-strip {
    width: 100%;
    min-height: 48px;
    gap: 12px;
    margin-top: 0;
    padding: 0 14px;
    border-radius: 14px;
  }

  .announcement-badge {
    padding: 6px 10px;
    font-size: 11px;
  }

  .announcement-track {
    align-self: stretch;
  }

  .announcement-content {
    padding-right: 1.5rem;
  }

  .announcement-dismiss {
    width: 24px;
    height: 24px;
  }
}
</style>

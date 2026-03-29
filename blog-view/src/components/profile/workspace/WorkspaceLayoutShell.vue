<template>
  <div class="workspace-shell" :class="`variant-${variant}`">
    <aside class="workspace-sidebar">
      <button type="button" class="workspace-brand" @click="$emit('brand-click')">
        <el-avatar :size="52" :src="brandAvatarSrc" class="workspace-brand-avatar">
          {{ brandAvatarFallback }}
        </el-avatar>

        <div class="workspace-brand-copy">
          <strong>{{ brandTitle }}</strong>
          <span>{{ brandSubtitle }}</span>
        </div>
      </button>

      <el-scrollbar class="workspace-menu-scroll">
        <div class="workspace-menu-stack">
          <section
            v-for="section in groupedMenuSections"
            :key="section.key"
            class="workspace-menu-group"
          >
            <span class="workspace-menu-group-title">{{ section.label }}</span>

            <div class="workspace-menu-list">
              <button
                v-for="item in section.items"
                :key="item.index"
                type="button"
                class="workspace-menu-item"
                :class="[{ 'is-active': isMenuItemActive(item) }, `tone-${item.tone}`]"
                @click="$emit('menu-click', item.index)"
              >
                <span class="workspace-menu-item-main">
                  <span class="workspace-menu-icon">
                    <el-icon><component :is="item.icon" /></el-icon>
                  </span>
                  <span class="workspace-menu-label">{{ item.label }}</span>
                </span>

                <span v-if="item.badge" class="workspace-menu-badge">{{ item.badge }}</span>
                <el-icon class="workspace-menu-arrow"><ArrowDown /></el-icon>
              </button>
            </div>
          </section>
        </div>
      </el-scrollbar>
    </aside>

    <div class="workspace-main">
      <header class="workspace-topbar">
        <div class="workspace-topbar-leading">
          <div class="workspace-title-block">
            <strong> · {{ currentPageTitle }}</strong>
          </div>
        </div>

        <div class="workspace-topbar-actions">
          <label class="workspace-search-box">
            <el-icon><Search /></el-icon>
            <input :placeholder="searchPlaceholder" type="text" />
            <span class="search-shortcut">Ctrl K</span>
          </label>

          <slot name="actions" />

          <button type="button" class="workspace-logout-button" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出登录</span>
          </button>
        </div>
      </header>

      <main class="workspace-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useAuthStore } from '@/store/auth'
import { ArrowDown, Search, SwitchButton } from '@element-plus/icons-vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'user',
  },
  brandTitle: {
    type: String,
    default: '',
  },
  brandSubtitle: {
    type: String,
    default: '',
  },
  brandAvatarSrc: {
    type: String,
    default: '',
  },
  brandAvatarFallback: {
    type: String,
    default: '',
  },
  groupedMenuSections: {
    type: Array,
    default: () => [],
  },
  activeSectionLabel: {
    type: String,
    default: '',
  },
  currentPageTitle: {
    type: String,
    default: '',
  },
  activeMenuPath: {
    type: String,
    default: '',
  },
})

defineEmits(['brand-click', 'menu-click', 'overview-click', 'refresh-click'])

const authStore = useAuthStore()

const searchPlaceholder = computed(() => `搜索 ${props.currentPageTitle || '当前页面'}`)

const isMenuItemActive = (item) => props.activeMenuPath === item.index

const handleLogout = () => {
  authStore.logout()
}
</script>

<style scoped>
.workspace-shell {
  --workspace-accent: #5c7cff;
  --workspace-accent-soft: rgba(92, 124, 255, 0.12);
  --workspace-title: #1c2758;
  --workspace-shell-bg: linear-gradient(180deg, #f7f8fc 0%, #f4f7fb 100%);
  --workspace-text: #24304d;
  --workspace-sidebar-border: #e5e9f2;
  --workspace-sidebar-bg: rgba(255, 255, 255, 0.94);
  --workspace-brand-hover-bg: #f6f8fd;
  --workspace-brand-hover-ring: rgba(92, 124, 255, 0.08);
  --workspace-brand-title: #1c2758;
  --workspace-brand-subtitle: #8b95ab;
  --workspace-brand-avatar-border: rgba(92, 124, 255, 0.12);
  --workspace-brand-avatar-shadow: rgba(92, 124, 255, 0.12);
  --workspace-group-title: #9aa3b8;
  --workspace-menu-text: #30415f;
  --workspace-menu-hover-bg: #f4f7fc;
  --workspace-menu-active-bg: #eef2ff;
  --workspace-menu-active-ring: rgba(92, 124, 255, 0.08);
  --workspace-menu-icon-bg: #f4f7fc;
  --workspace-menu-badge-bg: #ff5d5d;
  --workspace-menu-arrow: #a4acc2;
  --workspace-topbar-border: #e8edf5;
  --workspace-topbar-bg: rgba(255, 255, 255, 0.82);
  --workspace-topbar-icon-border: #e5e9f2;
  --workspace-topbar-icon-color: #6b7390;
  --workspace-topbar-icon-hover-border: rgba(92, 124, 255, 0.24);
  --workspace-topbar-icon-hover-color: #4f6cff;
  --workspace-topbar-icon-hover-shadow: rgba(92, 124, 255, 0.12);
  --workspace-icon-badge-bg: #4f6cff;
  --workspace-breadcrumb: #8e97ad;
  --workspace-search-border: #e5e9f2;
  --workspace-search-text: #8d95aa;
  --workspace-search-input: #44506c;
  --workspace-shortcut-border: #e6ebf3;
  --workspace-shortcut-text: #9ba4b8;
  --workspace-logout-border: #f0d8d8;
  --workspace-logout-bg: rgba(255, 255, 255, 0.96);
  --workspace-logout-text: #b25555;
  --workspace-logout-hover-border: rgba(209, 110, 110, 0.36);
  --workspace-logout-hover-bg: #fff6f6;
  --workspace-logout-hover-shadow: rgba(209, 110, 110, 0.14);
  display: grid;
  grid-template-columns: 304px minmax(0, 1fr);
  min-height: 100vh;
  background: var(--workspace-shell-bg);
  color: var(--workspace-text);
}

.workspace-shell.variant-user {
  --workspace-accent: #aac8ad;
  --workspace-accent-soft: rgba(170, 200, 173, 0.12);
  --workspace-title: #5a715d;
  --workspace-shell-bg: linear-gradient(180deg, #fbfdfb 0%, #f6faf5 52%, #f1f6f0 100%);
  --workspace-text: #657b67;
  --workspace-sidebar-border: #e8efe6;
  --workspace-sidebar-bg: rgba(255, 255, 255, 0.94);
  --workspace-brand-hover-bg: #f8fbf7;
  --workspace-brand-hover-ring: rgba(170, 200, 173, 0.08);
  --workspace-brand-title: #5a715d;
  --workspace-brand-subtitle: #a4b3a5;
  --workspace-brand-avatar-border: rgba(170, 200, 173, 0.14);
  --workspace-brand-avatar-shadow: rgba(170, 200, 173, 0.1);
  --workspace-group-title: #acb9ac;
  --workspace-menu-text: #6b7f6d;
  --workspace-menu-hover-bg: #f7faf6;
  --workspace-menu-active-bg: #f1f6f0;
  --workspace-menu-active-ring: rgba(142, 178, 147, 0.08);
  --workspace-menu-icon-bg: #f6faf5;
  --workspace-menu-badge-bg: #9dbda1;
  --workspace-menu-arrow: #b5bfb4;
  --workspace-topbar-border: #e8efe6;
  --workspace-topbar-bg: rgba(255, 255, 255, 0.82);
  --workspace-topbar-icon-border: #e8eee6;
  --workspace-topbar-icon-color: #95a696;
  --workspace-topbar-icon-hover-border: rgba(142, 178, 147, 0.16);
  --workspace-topbar-icon-hover-color: #7f9e84;
  --workspace-topbar-icon-hover-shadow: rgba(142, 178, 147, 0.1);
  --workspace-icon-badge-bg: #94b69a;
  --workspace-breadcrumb: #a8b5a8;
  --workspace-search-border: #e8eee6;
  --workspace-search-text: #adb8ae;
  --workspace-search-input: #647865;
  --workspace-shortcut-border: #edf2eb;
  --workspace-shortcut-text: #b4beb4;
  --workspace-logout-border: #e8d7d2;
  --workspace-logout-bg: rgba(255, 255, 255, 0.96);
  --workspace-logout-text: #b07162;
  --workspace-logout-hover-border: rgba(176, 113, 98, 0.24);
  --workspace-logout-hover-bg: #fff8f5;
  --workspace-logout-hover-shadow: rgba(176, 113, 98, 0.12);
}

.workspace-sidebar {
  position: sticky;
  top: 0;
  align-self: start;
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 10px;
  border-right: 1px solid var(--workspace-sidebar-border);
  background: var(--workspace-sidebar-bg);
  box-sizing: border-box;
}

.workspace-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 18px 20px 14px;
  border: 0;
  border-radius: 18px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    box-shadow 0.2s ease;
}

.workspace-brand:hover {
  background: var(--workspace-brand-hover-bg);
  box-shadow: inset 0 0 0 1px var(--workspace-brand-hover-ring);
}

.workspace-brand-avatar {
  flex-shrink: 0;
  border: 2px solid var(--workspace-brand-avatar-border);
  box-shadow: 0 10px 24px var(--workspace-brand-avatar-shadow);
}

.workspace-brand-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.workspace-brand-copy strong {
  color: var(--workspace-brand-title);
  font-size: 18px;
  font-weight: 700;
}

.workspace-brand-copy span {
  color: var(--workspace-brand-subtitle);
  font-size: 13px;
}

.workspace-menu-scroll {
  flex: 1;
  min-height: 0;
}

.workspace-menu-scroll :deep(.el-scrollbar__wrap) {
  scrollbar-width: none;
}

.workspace-menu-scroll :deep(.el-scrollbar__wrap::-webkit-scrollbar) {
  width: 0;
  height: 0;
}

.workspace-menu-scroll :deep(.el-scrollbar__bar) {
  display: none;
}

.workspace-menu-stack {
  display: grid;
  gap: 18px;
  padding: 8px 0 20px;
}

.workspace-menu-group {
  display: grid;
  gap: 10px;
}

.workspace-menu-group-title {
  padding: 0 14px;
  color: var(--workspace-group-title);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.workspace-menu-list {
  display: grid;
  gap: 8px;
}

.workspace-menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  min-height: 54px;
  padding: 12px 14px;
  border: 0;
  border-radius: 14px;
  background: transparent;
  color: var(--workspace-menu-text);
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.workspace-menu-item:hover {
  background: var(--workspace-menu-hover-bg);
  transform: translateX(2px);
}

.workspace-menu-item.is-active {
  background: var(--workspace-menu-active-bg);
  box-shadow: inset 0 0 0 1px var(--workspace-menu-active-ring);
}

.workspace-menu-item-main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.workspace-menu-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: var(--workspace-menu-icon-bg);
  font-size: 17px;
}

.workspace-menu-label {
  min-width: 0;
  font-size: 15px;
  font-weight: 500;
}

.workspace-menu-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  background: var(--workspace-menu-badge-bg);
  color: #ffffff;
  font-size: 12px;
  font-weight: 700;
}

.workspace-menu-arrow {
  color: var(--workspace-menu-arrow);
  font-size: 13px;
}

.workspace-menu-item.tone-amber .workspace-menu-icon {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.12);
}

.workspace-menu-item.tone-blue .workspace-menu-icon {
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.12);
}

.workspace-menu-item.tone-violet .workspace-menu-icon {
  color: #8b5cf6;
  background: rgba(139, 92, 246, 0.12);
}

.workspace-menu-item.tone-green .workspace-menu-icon {
  color: #22c55e;
  background: rgba(34, 197, 94, 0.12);
}

.workspace-menu-item.tone-rose .workspace-menu-icon {
  color: #f43f5e;
  background: rgba(244, 63, 94, 0.12);
}

.workspace-menu-item.tone-cyan .workspace-menu-icon {
  color: #06b6d4;
  background: rgba(6, 182, 212, 0.12);
}

.workspace-menu-item.tone-emerald .workspace-menu-icon {
  color: #10b981;
  background: rgba(16, 185, 129, 0.12);
}

.workspace-menu-item.tone-indigo .workspace-menu-icon {
  color: #6366f1;
  background: rgba(99, 102, 241, 0.12);
}

.workspace-menu-item.tone-slate .workspace-menu-icon {
  color: #64748b;
  background: rgba(100, 116, 139, 0.12);
}

.workspace-menu-item.tone-teal .workspace-menu-icon {
  color: #14b8a6;
  background: rgba(20, 184, 166, 0.12);
}

.workspace-menu-item.tone-orange .workspace-menu-icon {
  color: #f97316;
  background: rgba(249, 115, 22, 0.12);
}

.workspace-menu-item.tone-red .workspace-menu-icon {
  color: #ef4444;
  background: rgba(239, 68, 68, 0.12);
}

.workspace-menu-item.tone-sky .workspace-menu-icon {
  color: #0ea5e9;
  background: rgba(14, 165, 233, 0.12);
}

.workspace-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.workspace-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 24px 14px;
  border-bottom: 1px solid var(--workspace-topbar-border);
  background: var(--workspace-topbar-bg);
  backdrop-filter: blur(12px);
}

.workspace-topbar-leading,
.workspace-topbar-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.workspace-topbar-leading {
  min-width: 0;
}

.workspace-topbar :deep(.topbar-icon-button) {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border: 1px solid var(--workspace-topbar-icon-border);
  border-radius: 12px;
  background: #ffffff;
  color: var(--workspace-topbar-icon-color);
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    color 0.2s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.workspace-topbar :deep(.topbar-icon-button:hover) {
  transform: translateY(-1px);
  border-color: var(--workspace-topbar-icon-hover-border);
  color: var(--workspace-topbar-icon-hover-color);
  box-shadow: 0 14px 24px var(--workspace-topbar-icon-hover-shadow);
}

.workspace-topbar :deep(.icon-badge) {
  position: absolute;
  top: -5px;
  right: -4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: var(--workspace-icon-badge-bg);
  color: #ffffff;
  font-size: 11px;
  font-weight: 700;
}

.workspace-topbar :deep(.icon-badge.is-danger) {
  background: #ff5d5d;
}

.workspace-title-block {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.workspace-breadcrumb {
  color: var(--workspace-breadcrumb);
  font-size: 13px;
}

.workspace-title-block strong {
  color: var(--workspace-title);
  font-size: 22px;
  font-weight: 700;
}

.workspace-search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  width: min(360px, 40vw);
  height: 42px;
  padding: 0 14px;
  border: 1px solid var(--workspace-search-border);
  border-radius: 12px;
  background: #ffffff;
  color: var(--workspace-search-text);
}

.workspace-search-box input {
  flex: 1;
  min-width: 0;
  border: 0;
  outline: none;
  background: transparent;
  color: var(--workspace-search-input);
  font-size: 14px;
}

.search-shortcut {
  display: inline-flex;
  align-items: center;
  height: 26px;
  padding: 0 8px;
  border: 1px solid var(--workspace-shortcut-border);
  border-radius: 8px;
  color: var(--workspace-shortcut-text);
  font-size: 12px;
}

.workspace-logout-button {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 35px;
  padding: 0 16px;
  border: 1px solid var(--workspace-logout-border);
  border-radius: 12px;
  background: var(--workspace-logout-bg);
  color: var(--workspace-logout-text);
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.workspace-logout-button:hover {
  transform: translateY(-1px);
  border-color: var(--workspace-logout-hover-border);
  background: var(--workspace-logout-hover-bg);
  box-shadow: 0 14px 24px var(--workspace-logout-hover-shadow);
}

.workspace-logout-button span {
  line-height: 1;
}

.workspace-content {
  min-width: 0;
  padding: 24px;
}

@media (max-width: 1180px) {
  .workspace-shell {
    grid-template-columns: 250px minmax(0, 1fr);
  }

  .workspace-search-box {
    width: 240px;
  }
}

@media (max-width: 960px) {
  .workspace-shell {
    grid-template-columns: 1fr;
  }

  .workspace-sidebar {
    position: static;
    height: auto;
    border-right: 0;
    border-bottom: 1px solid var(--workspace-sidebar-border);
  }

  .workspace-topbar {
    flex-direction: column;
    align-items: stretch;
  }

  .workspace-topbar-actions,
  .workspace-topbar-leading {
    flex-wrap: wrap;
  }

  .workspace-search-box {
    width: 100%;
  }
}

@media (max-width: 720px) {
  .workspace-content,
  .workspace-topbar {
    padding-left: 16px;
    padding-right: 16px;
  }

  .workspace-brand {
    padding-left: 12px;
    padding-right: 12px;
  }

  .workspace-menu-item {
    min-height: 50px;
  }

  .workspace-topbar-actions {
    width: 100%;
  }

  .search-shortcut {
    display: none;
  }

  .workspace-logout-button {
    justify-content: center;
    width: 100%;
  }
}
</style>

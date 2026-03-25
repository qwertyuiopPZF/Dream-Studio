<template>
  <el-container class="common-layout">
    <el-header class="layout-header">
      <Header />
    </el-header>

    <el-main class="main-scroll-container layout-main">
      <div class="layout-view">
        <router-view v-slot="{ Component }">
          <transition name="app-fade" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </div>
      <Footer class="layout-footer" />
    </el-main>
  </el-container>
</template>

<script setup>
import { useRoute } from 'vue-router'
import Header from '@/components/common/Header.vue'
import Footer from '@/components/common/Footer.vue'

const route = useRoute()
</script>

<style scoped>
.common-layout {
  height: 97vh;
}

.layout-header {
  background: transparent;
}

.layout-main {
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  min-height: 0;
  padding: 0;
  background: var(--app-page-bg);
  font-family: Georgia, 'Times New Roman', Times, serif;
  scrollbar-width: none;
  -ms-overflow-style: none;
  transition: background 0.25s ease;
}

.layout-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  padding: 20px 20px 0;
  box-sizing: border-box;
}

.layout-footer {
  margin-top: auto;
}

.layout-main::-webkit-scrollbar {
  width: 0;
  height: 0;
}
</style>

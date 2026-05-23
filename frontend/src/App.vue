<template>
  <n-config-provider :theme="darkTheme" :theme-overrides="themeOverrides">
    <n-message-provider>
      <div class="app-wrapper">
        <img class="bg-image" src="/background.jpeg" alt="" />
        <n-layout class="app-layout">
          <n-layout-header class="app-header" bordered>
            <div class="header-inner">
              <router-link to="/" class="logo">
                <span class="logo-text">Vain Space</span>
              </router-link>
              <n-menu mode="horizontal" :value="activeKey" :options="menuOptions" @update:value="handleMenuSelect" class="nav-menu" />
              <div class="header-right">
                <router-link to="/profile" class="header-user-btn" v-if="authStore.isLoggedIn">
                  <span class="header-user-avatar">{{ authStore.username.charAt(0).toUpperCase() }}</span>
                  <span class="header-user-name">{{ authStore.username }}</span>
                </router-link>
                <router-link v-if="authStore.isAdmin" to="/admin" class="header-admin-link">管理</router-link>
                <a v-if="authStore.isLoggedIn" class="header-logout" @click="handleLogout">退出</a>
                <router-link v-else to="/login" class="header-login-btn">登录</router-link>
              </div>
            </div>
          </n-layout-header>
          <n-layout-content class="app-content">
            <router-view />
          </n-layout-content>
        </n-layout>
      </div>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NIcon, darkTheme } from 'naive-ui'
import {
  HomeOutline, FlagOutline, BookOutline, FitnessOutline,
  TimeOutline, BookmarkOutline, CompassOutline, ChatboxOutline, SearchOutline
} from '@vicons/ionicons5'
import { useAuthStore } from './store/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const activeKey = computed(() => route.name)

const themeOverrides = {
  common: {
    bodyColor: 'transparent',
    cardColor: 'rgba(0, 0, 0, 0.45)',
    modalColor: 'rgba(0, 0, 0, 0.85)',
    primaryColor: 'rgba(255, 255, 255, 0.7)',
    primaryColorHover: 'rgba(255, 255, 255, 0.9)',
    borderRadius: '10%',
    fontSize: '14px',
    fontFamily: '"Urbanist", "Mgen", sans-serif',
    fontWeight: '300',
    textColor1: 'rgba(255, 255, 255, 0.8)',
    textColor2: 'rgba(255, 255, 255, 0.6)',
    textColor3: 'rgba(255, 255, 255, 0.3)'
  }
}

function renderIcon(icon) {
  return () => h(NIcon, null, { default: () => h(icon) })
}

const menuOptions = [
  { label: '首页', key: 'Home', icon: renderIcon(HomeOutline) },
  { label: '目标', key: 'Goals', icon: renderIcon(FlagOutline) },
  { label: '文章', key: 'Articles', icon: renderIcon(BookOutline) },
  { label: '健身', key: 'Workout', icon: renderIcon(FitnessOutline) },
  { label: '番茄钟', key: 'Pomodoro', icon: renderIcon(TimeOutline) },
  { label: '书签', key: 'Bookmarks', icon: renderIcon(BookmarkOutline) },
  { label: '发现', key: 'Discover', icon: renderIcon(CompassOutline) },
  { label: '消息', key: 'Messages', icon: renderIcon(ChatboxOutline) }
]

function handleMenuSelect(key) {
  router.push({ name: key })
}

function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style>
@font-face {
  font-family: "Nothing";
  src: url("/fonts/nothing.ttf") format("truetype");
  font-weight: normal;
  font-style: normal;
}
@font-face {
  font-family: "NothingExtended";
  src: url("/fonts/Ndot77JPExtended.ttf") format("truetype");
  font-weight: normal;
  font-style: normal;
}
@font-face {
  font-family: "Simplon";
  src: url("/fonts/simplon-m.ttf") format("truetype");
  font-weight: 500;
  font-style: normal;
}
@font-face {
  font-family: "Simplon";
  src: url("/fonts/simplon-r.ttf") format("truetype");
  font-weight: 400;
  font-style: normal;
}
@font-face {
  font-family: "Mgen";
  src: url("/fonts/mgenplus-b.ttf") format("truetype");
  font-weight: 500;
  font-style: normal;
}
@font-face {
  font-family: "Mgen";
  src: url("/fonts/mgenplus-l.ttf") format("truetype");
  font-weight: 300;
  font-style: normal;
}
@font-face {
  font-family: "Urbanist";
  src: url("/fonts/urbanist-b.ttf") format("truetype");
  font-weight: 500;
  font-style: normal;
}
@font-face {
  font-family: "Urbanist";
  src: url("/fonts/urbanist-r.ttf") format("truetype");
  font-weight: 400;
  font-style: normal;
}
@font-face {
  font-family: "Urbanist";
  src: url("/fonts/urbanist-l.ttf") format("truetype");
  font-weight: 300;
  font-style: normal;
}

body {
  margin: 0;
  padding: 0;
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.8);
  background: #03030d;
}
a { color: inherit; text-decoration: none; }

.app-wrapper {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
}

.bg-image {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 0;
  opacity: 0.5;
}

.app-layout {
  position: relative;
  z-index: 1;
  background: transparent !important;
}

.app-header {
  background: rgba(0, 0, 0, 0.3) !important;
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08) !important;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
}

.logo-text {
  font-size: 17px;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 0.02em;
}

.nav-menu {
  background: transparent !important;
  flex: none;
}

.nav-menu .n-menu-item {
  color: rgba(255, 255, 255, 0.4) !important;
  font-weight: 300 !important;
  font-size: 13px;
}

.nav-menu .n-menu-item:hover {
  color: rgba(255, 255, 255, 0.7) !important;
}

.nav-menu .n-menu-item.n-menu-item--active {
  color: rgba(255, 255, 255, 0.8) !important;
}

.nav-menu .n-menu-item .n-menu-item-icon {
  color: inherit !important;
}

.nav-menu .n-menu-item.n-menu-item--active::after {
  background: rgba(255, 255, 255, 0.3) !important;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 300;
  flex-shrink: 0;
}

.header-user-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  text-decoration: none;
  transition: all 0.2s;
  cursor: pointer;
}

.header-user-btn:hover {
  border-color: rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.04);
}

.header-user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 400;
}

.header-user-name {
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}

.header-user-btn:hover .header-user-name {
  color: rgba(255, 255, 255, 0.7);
}

.header-admin-link {
  padding: 4px 10px;
  border-radius: 8px;
  border: 1px solid rgba(100, 160, 255, 0.2);
  color: rgba(100, 160, 255, 0.5);
  text-decoration: none;
  font-size: 12px;
  transition: all 0.2s;
}

.header-admin-link:hover {
  border-color: rgba(100, 160, 255, 0.4);
  color: rgba(100, 160, 255, 0.8);
}

.header-logout {
  color: rgba(255, 255, 255, 0.2);
  cursor: pointer;
  text-decoration: none;
  font-size: 12px;
  transition: color 0.2s;
}

.header-logout:hover {
  color: rgba(255, 255, 255, 0.4);
}

.header-login-btn {
  padding: 6px 16px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.4);
  text-decoration: none;
  font-size: 13px;
  transition: all 0.2s;
}

.header-login-btn:hover {
  border-color: rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.7);
}

.app-content {
  background: transparent !important;
  max-width: 1400px;
  margin: 0 auto;
  padding: 16px 24px;
  min-height: calc(100vh - 56px);
}

.n-card {
  background: rgba(0, 0, 0, 0.45) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  border-radius: 10% !important;
  color: rgba(255, 255, 255, 0.8) !important;
  font-weight: 300;
}

.n-card .n-card-header {
  color: rgba(255, 255, 255, 0.6) !important;
}

.n-data-table {
  background: transparent !important;
}

.n-data-table .n-data-table-th {
  background: rgba(0, 0, 0, 0.3) !important;
  color: rgba(255, 255, 255, 0.4) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08) !important;
  font-weight: 300;
}

.n-data-table .n-data-table-td {
  background: transparent !important;
  color: rgba(255, 255, 255, 0.6) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04) !important;
  font-weight: 300;
}

.n-data-table .n-data-table-tr:hover .n-data-table-td {
  background: rgba(0, 0, 0, 0.2) !important;
}

.n-h2 {
  color: rgba(255, 255, 255, 0.6) !important;
  font-weight: 300 !important;
}

.n-h2.n-h2--prefix-bar::before {
  background: rgba(255, 255, 255, 0.3) !important;
}

.n-empty {
  color: rgba(255, 255, 255, 0.15) !important;
}

.n-button.n-button--primary {
  background: rgba(255, 255, 255, 0.1) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.7) !important;
  font-weight: 300;
}

.n-button.n-button--primary:hover {
  background: rgba(255, 255, 255, 0.15) !important;
  border-color: rgba(255, 255, 255, 0.35) !important;
}

.n-modal {
  background: rgba(0, 0, 0, 0.85) !important;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
  color: rgba(255, 255, 255, 0.8) !important;
  border-radius: 10% !important;
}

.n-modal .n-modal-header {
  color: rgba(255, 255, 255, 0.6) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06) !important;
  font-weight: 300;
}

.n-input, .n-input-number, .n-select {
  background: rgba(255, 255, 255, 0.06) !important;
  border-color: rgba(255, 255, 255, 0.12) !important;
  color: rgba(255, 255, 255, 0.7) !important;
  font-weight: 300;
  border-radius: 8px !important;
}

.n-input:hover, .n-input-number:hover, .n-select:hover {
  border-color: rgba(255, 255, 255, 0.25) !important;
}

.n-input .n-input__placeholder, .n-input-number .n-input-number__placeholder {
  color: rgba(255, 255, 255, 0.2) !important;
}

.n-date-picker {
  background: rgba(255, 255, 255, 0.06) !important;
  border-color: rgba(255, 255, 255, 0.12) !important;
}

.n-tag {
  backdrop-filter: blur(4px);
  font-weight: 300;
}

.n-progress .n-progress__text {
  color: rgba(255, 255, 255, 0.6) !important;
}

.n-divider {
  background: rgba(255, 255, 255, 0.08) !important;
}

.n-tabs .n-tabs-tab {
  color: rgba(255, 255, 255, 0.35) !important;
  font-weight: 300;
}

.n-tabs .n-tabs-tab.n-tabs-tab--active {
  color: rgba(255, 255, 255, 0.7) !important;
  font-weight: 400;
}

.n-tabs .n-tabs-bar {
  background: rgba(255, 255, 255, 0.3) !important;
}

.n-spin .n-spin-content {
  color: rgba(255, 255, 255, 0.5) !important;
}

.n-form-item .n-form-item-label {
  color: rgba(255, 255, 255, 0.5) !important;
  font-weight: 300;
}

.n-menu-item .n-menu-item-content {
  font-weight: 300 !important;
}

.n-select .n-base-selection {
  background: rgba(255, 255, 255, 0.06) !important;
}
</style>

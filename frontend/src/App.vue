<template>
  <n-config-provider :theme="darkTheme" :theme-overrides="themeOverrides">
    <n-message-provider>
      <div class="app-wrapper">
        <img class="bg-image" src="/background.jpeg" alt="" />
        <n-layout class="app-layout">
          <n-layout-header class="app-header" bordered>
            <div class="header-inner">
              <div class="header-left">
                <a class="nav-toggle" @click="navOpen = !navOpen">
                  <span :class="['nav-toggle-bar', { open: navOpen }]"></span>
                </a>
                <router-link to="/" class="logo">
                  <span class="logo-text">Vain Space</span>
                </router-link>
              </div>
              <n-menu mode="horizontal" :value="activeKey" :options="menuOptions" @update:value="handleMenuSelect" class="nav-menu" />
              <div class="header-right">
                <router-link to="/profile" class="header-user-btn" v-if="authStore.isLoggedIn">
                  <span class="header-user-avatar">{{ authStore.username.charAt(0).toUpperCase() }}</span>
                </router-link>
              </div>
            </div>
          </n-layout-header>
          <div :class="['nav-drawer-bg', { open: navOpen }]" @click="navOpen = false"></div>
          <div :class="['nav-drawer', { open: navOpen }]">
            <div class="nav-drawer-header">
              <span class="nav-drawer-title">Vain Space</span>
              <a class="nav-drawer-close" @click="navOpen = false">✕</a>
            </div>
            <div class="nav-drawer-user" v-if="authStore.isLoggedIn">
              <span class="nav-drawer-avatar">{{ authStore.username.charAt(0).toUpperCase() }}</span>
              <div class="nav-drawer-user-info">
                <span class="nav-drawer-username">{{ authStore.username }}</span>
                <router-link to="/profile" class="nav-drawer-profile-link" @click="navOpen = false">个人中心</router-link>
              </div>
            </div>
            <div class="nav-drawer-links">
              <router-link v-for="item in menuOptions" :key="item.key" :to="{ name: item.key }"
                :class="['nav-drawer-link', { active: activeKey === item.key }]" @click="navOpen = false">
                <component :is="item.icon?.()" class="nav-drawer-icon" />
                <span>{{ item.label }}</span>
              </router-link>
            </div>
            <div class="nav-drawer-footer">
              <router-link v-if="authStore.isAdmin" to="/admin" class="nav-drawer-footer-link" @click="navOpen = false">管理后台</router-link>
              <a v-if="authStore.isLoggedIn" class="nav-drawer-footer-link" @click="handleLogout; navOpen = false">退出登录</a>
              <router-link v-else to="/login" class="nav-drawer-footer-link" @click="navOpen = false">登录</router-link>
            </div>
          </div>
          <n-layout-content class="app-content">
            <router-view />
          </n-layout-content>
        </n-layout>
      </div>
    </n-message-provider>
  </n-config-provider>
</template>

<script setup>
import { ref, computed, h } from 'vue'
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

const navOpen = ref(false)

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
  navOpen.value = false
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

.nav-toggle {
  display: none;
  width: 28px;
  height: 28px;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-toggle-bar {
  display: block;
  width: 18px;
  height: 2px;
  background: rgba(255, 255, 255, 0.5);
  position: relative;
  transition: background 0.2s;
}

.nav-toggle-bar::before,
.nav-toggle-bar::after {
  content: '';
  position: absolute;
  left: 0;
  width: 100%;
  height: 2px;
  background: rgba(255, 255, 255, 0.5);
  transition: all 0.25s;
}

.nav-toggle-bar::before { top: -6px; }
.nav-toggle-bar::after { top: 6px; }
.nav-toggle-bar.open { background: transparent; }
.nav-toggle-bar.open::before { top: 0; transform: rotate(45deg); }
.nav-toggle-bar.open::after { top: 0; transform: rotate(-45deg); }

.nav-drawer-bg {
  display: none;
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 200;
}

.nav-drawer-bg.open { display: block; }

.nav-drawer {
  position: fixed;
  top: 0; left: 0;
  width: 280px;
  height: 100vh;
  background: rgba(0, 0, 0, 0.92);
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  z-index: 210;
  transform: translateX(-100%);
  transition: transform 0.3s ease;
  display: flex;
  flex-direction: column;
}

.nav-drawer.open { transform: translateX(0); }

.nav-drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.nav-drawer-title {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 300;
  letter-spacing: 0.02em;
}

.nav-drawer-close {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  padding: 4px;
}

.nav-drawer-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.nav-drawer-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.5);
  flex-shrink: 0;
}

.nav-drawer-user-info {
  display: flex;
  flex-direction: column;
}

.nav-drawer-username {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 300;
}

.nav-drawer-profile-link {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.25);
  text-decoration: none;
  margin-top: 2px;
}

.nav-drawer-links {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.nav-drawer-link {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 20px;
  color: rgba(255, 255, 255, 0.45);
  text-decoration: none;
  font-size: 15px;
  font-weight: 300;
  transition: all 0.15s;
  border-left: 3px solid transparent;
}

.nav-drawer-link:hover {
  color: rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.04);
}

.nav-drawer-link.active {
  color: rgba(255, 255, 255, 0.8);
  border-left-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.04);
}

.nav-drawer-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: currentColor;
  opacity: 0.7;
}

.nav-drawer-footer {
  padding: 12px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-drawer-footer-link {
  color: rgba(255, 255, 255, 0.25);
  text-decoration: none;
  font-size: 13px;
  font-weight: 300;
  cursor: pointer;
}

.nav-drawer-footer-link:hover {
  color: rgba(255, 255, 255, 0.5);
}

.nav-menu {
  background: transparent !important;
  flex: none;
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

@media (max-width: 768px) {
  .header-inner {
    padding: 0 16px;
  }

  .nav-toggle {
    display: flex;
  }

  .nav-menu {
    display: none;
  }

  .header-right {
    gap: 4px;
  }

  .header-user-btn {
    padding: 4px;
    border: none;
  }

  .header-user-btn:hover {
    background: transparent;
  }

  .app-content {
    padding: 12px 16px;
  }
}
</style>

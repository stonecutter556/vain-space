<template>
  <div class="user-page">
    <div class="user-card">
      <div class="user-header">
        <div class="user-avatar">{{ userData.username?.charAt(0)?.toUpperCase() }}</div>
        <div class="user-info">
          <div class="user-name">{{ userData.username }}</div>
          <div v-if="userData.nickname" class="user-nick">{{ userData.nickname }}</div>
        </div>
      </div>
      <div v-if="userData.bio" class="user-bio">{{ userData.bio }}</div>
    </div>

    <div class="user-section">
      <div class="section-header">公开文章</div>
      <div v-if="articles.length" class="section-list">
        <div v-for="item in articles" :key="item.id" class="section-item" @click="goArticle(item.id)">
          <div class="section-item-top">
            <span class="section-item-title">{{ item.title }}</span>
            <span class="section-item-tag">{{ item.articleType === 'diary' ? '日记' : '博客' }}</span>
          </div>
          <div class="section-item-time">{{ formatTime(item.createdAt) }}</div>
        </div>
      </div>
      <div v-else class="section-empty">暂无公开文章</div>
    </div>

    <div class="user-section">
      <div class="section-header">公开目标</div>
      <div v-if="goals.length" class="section-list">
        <div v-for="item in goals" :key="item.id" class="section-item">
          <div class="section-item-title">{{ item.title }}</div>
          <div class="section-item-meta">
            <span class="section-item-tag">{{ item.status }}</span>
            <span v-if="item.targetDate" class="section-item-time">{{ formatDate(item.targetDate) }}</span>
          </div>
        </div>
      </div>
      <div v-else class="section-empty">暂无公开目标</div>
    </div>

    <div class="message-fab" @click="goMessage">
      发消息
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const BACKEND_URL = import.meta.env.VITE_API_BASE_URL || ''

const userData = ref({})
const articles = ref([])
const goals = ref([])

function getToken() {
  return localStorage.getItem('vs_token')
}

async function fetchUser() {
  const id = route.params.id
  try {
    const res = await axios.get(BACKEND_URL + '/api/user/public/' + id, {
      headers: { Authorization: 'Bearer ' + getToken() }
    })
    userData.value = res.data.user || {}
    articles.value = res.data.articles || []
    goals.value = res.data.goals || []
  } catch (e) {
    userData.value = {}
    articles.value = []
    goals.value = []
  }
}

function formatTime(t) {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN')
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

function goArticle(id) {
  router.push('/articles/' + id)
}

function goMessage() {
  router.push('/messages/' + route.params.id)
}

onMounted(fetchUser)
</script>

<style scoped>
.user-page {
  max-width: 640px;
  margin: 0 auto;
  padding-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: relative;
  padding-bottom: 80px;
}

.user-card {
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 28px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 300;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 300;
}

.user-nick {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 300;
}

.user-bio {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  font-size: 13px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 300;
  line-height: 1.5;
}

.user-section {
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 24px;
}

.section-header {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 300;
  letter-spacing: 0.03em;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.section-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.section-item {
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  cursor: pointer;
  transition: opacity 0.2s;
}

.section-item:last-child {
  border-bottom: none;
}

.section-item:hover {
  opacity: 0.7;
}

.section-item-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}

.section-item-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 300;
}

.section-item-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  font-weight: 300;
  letter-spacing: 0.02em;
}

.section-item-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 4px;
}

.section-item-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.25);
  font-weight: 300;
}

.section-empty {
  text-align: center;
  padding: 24px 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.2);
  font-weight: 300;
}

.message-fab {
  position: fixed;
  bottom: 32px;
  right: 32px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.25);
  color: rgba(255, 255, 255, 0.7);
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  backdrop-filter: blur(8px);
  z-index: 100;
}

.message-fab:hover {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(0, 0, 0, 0.75);
}

@media (max-width: 768px) {
  .user-public-page { padding: 0 10px; }
}
</style>

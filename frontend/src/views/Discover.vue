<template>
  <div class="discover-page">
    <div class="discover-card">
      <div class="discover-card-header">
        <span class="discover-card-title">搜索用户</span>
      </div>
      <div class="search-bar">
        <input v-model="searchQuery" class="search-input" placeholder="输入用户名或昵称..." @keyup.enter="searchUsers" />
        <button class="search-btn" @click="searchUsers">搜索</button>
      </div>
      <div v-if="searchResults.length" class="search-results">
        <div v-for="user in searchResults" :key="user.id" class="search-item" @click="goUser(user.id)">
          <div class="search-item-avatar">{{ user.username?.charAt(0)?.toUpperCase() }}</div>
          <div class="search-item-info">
            <div class="search-item-name">{{ user.username }}</div>
            <div v-if="user.nickname" class="search-item-nick">{{ user.nickname }}</div>
          </div>
        </div>
      </div>
      <div v-if="searched && !searchResults.length" class="search-empty">未找到用户</div>
    </div>

    <div class="discover-card">
      <div class="discover-card-header">
        <span class="discover-card-title">公开文章</span>
      </div>
      <div v-if="articles.length" class="articles-list">
        <div v-for="item in articles" :key="item.id" class="article-item" @click="goArticle(item.id)">
          <div class="article-item-top">
            <span class="article-item-title">{{ item.title }}</span>
            <span class="article-item-tag">{{ item.articleType === 'diary' ? '日记' : '博客' }}</span>
          </div>
          <div class="article-item-preview">{{ item.content?.substring(0, 100) }}{{ item.content?.length > 100 ? '...' : '' }}</div>
        </div>
      </div>
      <div v-else class="search-empty">暂无公开文章</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const BACKEND_URL = import.meta.env.VITE_API_BASE_URL || ''

const searchQuery = ref('')
const searchResults = ref([])
const searched = ref(false)
const articles = ref([])

function getToken() {
  return localStorage.getItem('vs_token')
}

async function searchUsers() {
  const q = searchQuery.value.trim()
  if (!q) return
  searched.value = true
  try {
    const res = await axios.get(BACKEND_URL + '/api/user/search', {
      params: { keyword: q },
      headers: { Authorization: 'Bearer ' + getToken() }
    })
    searchResults.value = res.data || []
  } catch (e) {
    searchResults.value = []
  }
}

async function fetchArticles() {
  try {
    const res = await axios.get(BACKEND_URL + '/api/articles/public', {
      headers: { Authorization: 'Bearer ' + getToken() }
    })
    articles.value = res.data || []
  } catch (e) {
    articles.value = []
  }
}

function goUser(id) {
  router.push('/users/' + id)
}

function goArticle(id) {
  router.push('/articles/' + id)
}

onMounted(fetchArticles)
</script>

<style scoped>
.discover-page {
  max-width: 640px;
  margin: 0 auto;
  padding-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.discover-card {
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 24px;
}

.discover-card-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.discover-card-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 300;
  letter-spacing: 0.03em;
}

.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.search-input {
  flex: 1;
  padding: 10px 12px;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.7);
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  font-size: 14px;
  outline: none;
}

.search-input:focus {
  border-color: rgba(255, 255, 255, 0.3);
}

.search-btn {
  padding: 10px 20px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: rgba(255, 255, 255, 0.7);
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.search-btn:hover {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.05);
}

.search-results {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  cursor: pointer;
  transition: opacity 0.2s;
}

.search-item:last-child {
  border-bottom: none;
}

.search-item:hover {
  opacity: 0.7;
}

.search-item-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 300;
  flex-shrink: 0;
}

.search-item-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.search-item-name {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 300;
}

.search-item-nick {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
  font-weight: 300;
}

.search-empty {
  text-align: center;
  padding: 24px 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.2);
  font-weight: 300;
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.article-item {
  padding: 14px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  cursor: pointer;
  transition: opacity 0.2s;
}

.article-item:last-child {
  border-bottom: none;
}

.article-item:hover {
  opacity: 0.7;
}

.article-item-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.article-item-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 300;
}

.article-item-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 10px;
  font-weight: 300;
  letter-spacing: 0.02em;
  flex-shrink: 0;
}

.article-item-preview {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.3);
  font-weight: 300;
  line-height: 1.4;
}
</style>

<template>
  <div class="messages-page">
    <div class="messages-card">
      <div class="messages-card-header">
        <span class="messages-card-title">消息</span>
      </div>
      <div v-if="conversations.length" class="conversations-list">
        <div v-for="conv in conversations" :key="conv.userId" class="conversation-item" @click="goChat(conv.userId)">
          <div class="conv-avatar-wrap">
            <div class="conv-avatar">{{ conv.username?.charAt(0)?.toUpperCase() }}</div>
            <div v-if="conv.unread" class="conv-unread-dot"></div>
          </div>
          <div class="conv-content">
            <div class="conv-top">
              <span class="conv-username">{{ conv.username }}</span>
              <span class="conv-time">{{ formatTime(conv.lastMessageTime) }}</span>
            </div>
            <div class="conv-preview">{{ conv.lastMessage }}</div>
          </div>
        </div>
      </div>
      <div v-else class="messages-empty">暂无消息</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const BACKEND_URL = import.meta.env.VITE_API_BASE_URL || ''

const conversations = ref([])

function getToken() {
  return localStorage.getItem('vs_token')
}

async function fetchConversations() {
  try {
    const res = await axios.get(BACKEND_URL + '/api/messages/conversations', {
      headers: { Authorization: 'Bearer ' + getToken() }
    })
    conversations.value = res.data || []
  } catch (e) {
    conversations.value = []
  }
}

function formatTime(t) {
  if (!t) return ''
  const date = new Date(t)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

function goChat(userId) {
  router.push('/messages/' + userId)
}

onMounted(fetchConversations)
</script>

<style scoped>
.messages-page {
  max-width: 640px;
  margin: 0 auto;
  padding-top: 20px;
}

.messages-card {
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 24px;
}

.messages-card-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.messages-card-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 300;
  letter-spacing: 0.03em;
}

.conversations-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  cursor: pointer;
  transition: opacity 0.2s;
}

.conversation-item:last-child {
  border-bottom: none;
}

.conversation-item:hover {
  opacity: 0.7;
}

.conv-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.conv-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 300;
}

.conv-unread-dot {
  position: absolute;
  top: 0;
  right: 0;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.7);
  border: 2px solid rgba(0, 0, 0, 0.45);
}

.conv-content {
  flex: 1;
  min-width: 0;
}

.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conv-username {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 300;
}

.conv-time {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.25);
  font-weight: 300;
  flex-shrink: 0;
  margin-left: 8px;
}

.conv-preview {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.35);
  font-weight: 300;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.messages-empty {
  text-align: center;
  padding: 48px 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.2);
  font-weight: 300;
}
</style>

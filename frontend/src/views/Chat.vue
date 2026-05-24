<template>
  <div class="chat-page">
    <div class="chat-card">
      <div class="chat-header">
        <span class="chat-back" @click="goBack">←</span>
        <span class="chat-username">{{ targetUsername }}</span>
      </div>
      <div class="chat-messages" ref="messagesRef">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="chat-msg"
          :class="{ 'chat-msg-self': msg.isSelf }"
        >
          <div class="chat-bubble">
            <div class="chat-text">{{ msg.content }}</div>
            <div class="chat-time">{{ formatTime(msg.createdAt) }}</div>
          </div>
        </div>
        <div v-if="!messages.length" class="chat-empty">暂无消息</div>
      </div>
      <div class="chat-input-bar">
        <input
          v-model="inputText"
          class="chat-input"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
        />
        <button class="chat-send-btn" @click="sendMessage" :disabled="!inputText.trim()">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const BACKEND_URL = import.meta.env.VITE_API_BASE_URL || ''

const messages = ref([])
const inputText = ref('')
const targetUsername = ref('')
const messagesRef = ref(null)
const userId = route.params.userId

function getToken() {
  return localStorage.getItem('vs_token')
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

async function fetchMessages() {
  try {
    const res = await axios.get(BACKEND_URL + '/api/messages/' + userId, {
      headers: { Authorization: 'Bearer ' + getToken() }
    })
    const data = res.data || []
    const currentUser = localStorage.getItem('vs_username') || ''
    messages.value = data.map(msg => ({
      ...msg,
      isSelf: msg.senderUsername === currentUser || msg.senderId?.toString() === localStorage.getItem('vs_userId')
    }))
    if (data.length && data[0].senderUsername) {
      const first = data[0]
      targetUsername.value = first.senderUsername === currentUser ? first.receiverUsername : first.senderUsername
    }
    scrollToBottom()
  } catch (e) {
    messages.value = []
  }
}

async function sendMessage() {
  const content = inputText.value.trim()
  if (!content) return
  inputText.value = ''
  try {
    await axios.post(BACKEND_URL + '/api/messages', {
      content,
      receiverId: userId
    }, {
      headers: { Authorization: 'Bearer ' + getToken() }
    })
    await fetchMessages()
  } catch (e) {
    inputText.value = content
  }
}

function formatTime(t) {
  if (!t) return ''
  const date = new Date(t)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function goBack() {
  router.push('/messages')
}

onMounted(fetchMessages)
</script>

<style scoped>
.chat-page {
  max-width: 640px;
  margin: 0 auto;
  padding-top: 20px;
}

.chat-card {
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
}

.chat-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}

.chat-back {
  font-size: 20px;
  color: rgba(255, 255, 255, 0.4);
  cursor: pointer;
  padding: 4px;
  transition: color 0.2s;
  line-height: 1;
}

.chat-back:hover {
  color: rgba(255, 255, 255, 0.7);
}

.chat-username {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.6);
  font-weight: 300;
  letter-spacing: 0.03em;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-messages::-webkit-scrollbar {
  width: 4px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.chat-msg {
  display: flex;
  justify-content: flex-start;
}

.chat-msg-self {
  justify-content: flex-end;
}

.chat-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.chat-msg-self .chat-bubble {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.18);
}

.chat-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  font-weight: 300;
  line-height: 1.4;
  word-break: break-word;
}

.chat-time {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.2);
  font-weight: 300;
  margin-top: 4px;
  text-align: right;
}

.chat-empty {
  text-align: center;
  padding: 48px 0;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.2);
  font-weight: 300;
}

.chat-input-bar {
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  flex-shrink: 0;
}

.chat-input {
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

.chat-input:focus {
  border-color: rgba(255, 255, 255, 0.3);
}

.chat-send-btn {
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

.chat-send-btn:hover {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.05);
}

.chat-send-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .chat-page { padding: 0; }
  .chat-messages { padding: 10px; }
}
</style>

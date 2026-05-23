<template>
  <div class="admin-page">
    <div class="admin-header">
      <div class="admin-title">管理后台</div>
      <div class="admin-stats">
        <div class="admin-stat">总用户：{{ users.length }}</div>
        <div class="admin-stat">管理员：{{ users.filter(u => u.role === 'admin').length }}</div>
      </div>
    </div>

    <div class="admin-table">
      <div class="admin-table-row admin-table-head">
        <span class="admin-col-id">ID</span>
        <span class="admin-col-name">用户名</span>
        <span class="admin-col-nick">昵称</span>
        <span class="admin-col-role">角色</span>
        <span class="admin-col-email">邮箱</span>
        <span class="admin-col-time">注册时间</span>
        <span class="admin-col-action">操作</span>
      </div>
      <div v-for="u in users" :key="u.id" class="admin-table-row">
        <span class="admin-col-id">{{ u.id }}</span>
        <span class="admin-col-name">{{ u.username }}</span>
        <span class="admin-col-nick">{{ u.nickname || '-' }}</span>
        <span class="admin-col-role">
          <span :class="['admin-tag', u.role === 'admin' ? 'admin-tag-admin' : 'admin-tag-user']">{{ u.role }}</span>
        </span>
        <span class="admin-col-email">{{ u.email || '-' }}</span>
        <span class="admin-col-time">{{ formatTime(u.createdAt) }}</span>
        <span class="admin-col-action">
          <button v-if="u.role !== 'admin'" class="admin-btn" @click="setAdmin(u.id)">设为管理员</button>
          <button v-if="u.role === 'admin' && u.id !== currentUserId" class="admin-btn admin-btn-outline" @click="setUser(u.id)">取消管理</button>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import { useAuthStore } from '../store/auth'

const message = useMessage()
const authStore = useAuthStore()
const currentUserId = ref(null)
const users = ref([])
const BACKEND_URL = import.meta.env.VITE_API_BASE_URL || ''

function getToken() { return localStorage.getItem('vs_token') }

async function fetchUsers() {
  try {
    const res = await axios.get(BACKEND_URL + '/api/admin/users', {
      headers: { Authorization: 'Bearer ' + getToken() }
    })
    users.value = res.data || []
  } catch (e) {
    message.error('加载用户列表失败')
  }
}

async function setAdmin(id) {
  try {
    await axios.put(BACKEND_URL + '/api/admin/users/' + id + '/role', { role: 'admin' }, {
      headers: { Authorization: 'Bearer ' + getToken() }
    })
    message.success('已设为管理员')
    await fetchUsers()
  } catch (e) {
    message.error('操作失败')
  }
}

async function setUser(id) {
  try {
    await axios.put(BACKEND_URL + '/api/admin/users/' + id + '/role', { role: 'user' }, {
      headers: { Authorization: 'Bearer ' + getToken() }
    })
    message.success('已取消管理员')
    await fetchUsers()
  } catch (e) {
    message.error('操作失败')
  }
}

function formatTime(t) {
  if (!t) return '-'
  return new Date(t).toLocaleDateString('zh-CN')
}

onMounted(fetchUsers)
</script>

<style scoped>
.admin-page {
  max-width: 960px;
  margin: 0 auto;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.admin-title {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 300;
  letter-spacing: 0.05em;
}

.admin-stats {
  display: flex;
  gap: 16px;
}

.admin-stat {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.3);
  font-weight: 300;
}

.admin-table {
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  overflow: hidden;
}

.admin-table-row {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
  font-size: 13px;
  font-weight: 300;
}

.admin-table-row:last-child {
  border-bottom: none;
}

.admin-table-head {
  background: rgba(255, 255, 255, 0.03);
  color: rgba(255, 255, 255, 0.3);
  font-size: 12px;
}

.admin-col-id { width: 50px; color: rgba(255,255,255,0.3); }
.admin-col-name { width: 100px; color: rgba(255,255,255,0.6); }
.admin-col-nick { width: 100px; color: rgba(255,255,255,0.4); }
.admin-col-role { width: 80px; }
.admin-col-email { flex: 1; color: rgba(255,255,255,0.3); min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.admin-col-time { width: 100px; color: rgba(255,255,255,0.3); }
.admin-col-action { width: 130px; text-align: right; }

.admin-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 11px;
}

.admin-tag-admin {
  background: rgba(100, 160, 255, 0.12);
  color: rgba(100, 160, 255, 0.6);
  border: 1px solid rgba(100, 160, 255, 0.2);
}

.admin-tag-user {
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.35);
}

.admin-btn {
  padding: 4px 12px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  background: transparent;
  color: rgba(255, 255, 255, 0.4);
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.admin-btn:hover {
  border-color: rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.7);
}

.admin-btn-outline {
  border-color: rgba(255, 150, 100, 0.2);
  color: rgba(255, 150, 100, 0.4);
}

.admin-btn-outline:hover {
  border-color: rgba(255, 150, 100, 0.4);
  color: rgba(255, 150, 100, 0.7);
}
</style>

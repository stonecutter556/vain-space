<template>
  <div class="profile-page">
    <div class="profile-card">
      <div class="profile-card-header">
        <span class="profile-card-title">个人中心</span>
        <router-link to="/" class="profile-close">✕</router-link>
      </div>
      <div class="profile-avatar-wrap">
        <div class="profile-avatar" @click="triggerUpload">
          <img v-if="profile.avatar" :src="avatarUrl" class="profile-avatar-img" />
          <span v-else class="profile-avatar-placeholder">{{ profile.username?.charAt(0)?.toUpperCase() }}</span>
        </div>
        <input ref="fileInput" type="file" accept="image/*" style="display:none" @change="uploadAvatar" />
        <div class="profile-username">{{ profile.username }}</div>
      </div>

      <div class="profile-form">
        <div class="profile-field">
          <label>昵称</label>
          <input v-model="form.nickname" class="pf-input" placeholder="设置昵称" />
        </div>
        <div class="profile-field">
          <label>个人简介</label>
          <textarea v-model="form.bio" class="pf-input pf-textarea" placeholder="介绍一下自己..." rows="3"></textarea>
        </div>
        <div class="profile-field">
          <label>邮箱</label>
          <input v-model="form.email" class="pf-input" placeholder="邮箱（可选）" />
        </div>
        <button class="profile-save-btn" @click="saveProfile" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'

const message = useMessage()
const fileInput = ref(null)
const saving = ref(false)

const BACKEND_URL = import.meta.env.VITE_API_BASE_URL || ''

const profile = reactive({
  username: '',
  nickname: '',
  avatar: '',
  bio: '',
  email: ''
})

const form = reactive({
  nickname: '',
  bio: '',
  email: ''
})

const avatarUrl = computed(() => {
  if (!profile.avatar) return ''
  return BACKEND_URL + profile.avatar
})

function getToken() {
  return localStorage.getItem('vs_token')
}

async function fetchProfile() {
  try {
    const res = await axios.get(BACKEND_URL + '/api/user/profile', {
      headers: { Authorization: 'Bearer ' + getToken() }
    })
    Object.assign(profile, res.data)
    form.nickname = res.data.nickname || ''
    form.bio = res.data.bio || ''
    form.email = res.data.email || ''
  } catch (e) {
    message.error('加载个人信息失败')
  }
}

async function saveProfile() {
  saving.value = true
  try {
    await axios.put(BACKEND_URL + '/api/user/profile', form, {
      headers: { Authorization: 'Bearer ' + getToken() }
    })
    profile.nickname = form.nickname
    profile.bio = form.bio
    profile.email = form.email
    message.success('保存成功')
  } catch (e) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

function triggerUpload() {
  fileInput.value?.click()
}

async function uploadAvatar(e) {
  const file = e.target.files[0]
  if (!file) return
  const fd = new FormData()
  fd.append('file', file)
  try {
    const res = await axios.post(BACKEND_URL + '/api/user/avatar', fd, {
      headers: { Authorization: 'Bearer ' + getToken() }
    })
    profile.avatar = res.data.avatar
    message.success('头像已更新')
  } catch (e) {
    message.error('上传失败')
  }
}

onMounted(fetchProfile)
</script>

<style scoped>
.profile-page {
  max-width: 480px;
  margin: 0 auto;
  padding-top: 20px;
}

.profile-card {
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 32px;
  position: relative;
}

.profile-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.profile-card-title {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 300;
  letter-spacing: 0.05em;
}

.profile-close {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.3);
  text-decoration: none;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 6px;
  transition: all 0.2s;
}

.profile-close:hover {
  color: rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.06);
}

.profile-avatar-wrap {
  text-align: center;
  margin-bottom: 28px;
}

.profile-avatar {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.2);
  overflow: hidden;
  margin: 0 auto 12px;
  cursor: pointer;
  transition: border-color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-avatar:hover {
  border-color: rgba(255, 255, 255, 0.4);
}

.profile-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-avatar-placeholder {
  font-size: 36px;
  color: rgba(255, 255, 255, 0.4);
  font-weight: 300;
}

.profile-username {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.5);
  font-weight: 300;
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profile-field label {
  display: block;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.35);
  font-weight: 300;
  margin-bottom: 6px;
}

.pf-input {
  width: 100%;
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

.pf-input:focus {
  border-color: rgba(255, 255, 255, 0.3);
}

.pf-textarea {
  resize: vertical;
  min-height: 60px;
}

.profile-save-btn {
  width: 100%;
  padding: 12px;
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  background: transparent;
  color: rgba(255, 255, 255, 0.7);
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 4px;
}

.profile-save-btn:hover {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.05);
}

.profile-save-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
</style>

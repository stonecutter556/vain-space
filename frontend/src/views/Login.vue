<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-title">{{ isLogin ? '登录' : '注册' }}</div>
      <div class="auth-form">
        <input v-model="username" type="text" class="auth-input" placeholder="用户名" @keyup.enter="submit" />
        <input v-model="password" type="password" class="auth-input" placeholder="密码" @keyup.enter="submit" />
        <input v-if="!isLogin" v-model="confirmPassword" type="password" class="auth-input" placeholder="确认密码" @keyup.enter="submit" />
        <div class="auth-error" v-if="error">{{ error }}</div>
        <button class="auth-btn" @click="submit" :disabled="loading">{{ loading ? '处理中...' : isLogin ? '登录' : '注册' }}</button>
      </div>
      <div class="auth-switch" @click="isLogin = !isLogin">
        {{ isLogin ? '没有账号？去注册' : '已有账号？去登录' }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { useAuthStore } from '../store/auth'
import axios from 'axios'

const router = useRouter()
const message = useMessage()
const authStore = useAuthStore()

const isLogin = ref(true)
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')
const loading = ref(false)

const BACKEND_URL = import.meta.env.VITE_API_BASE_URL || ''

async function submit() {
  error.value = ''
  if (!username.value || !password.value) {
    error.value = '请输入用户名和密码'
    return
  }
  if (!isLogin.value && password.value !== confirmPassword.value) {
    error.value = '两次密码不一致'
    return
  }

  loading.value = true
  try {
    const url = isLogin.value ? '/api/auth/login' : '/api/auth/register'
    const res = await axios.post(BACKEND_URL + url, {
      username: username.value,
      password: password.value
    })
    authStore.setAuth(res.data.token, res.data.username, res.data.role)
    message.success(isLogin.value ? '登录成功' : '注册成功')
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.error || '操作失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 100px);
}

.auth-card {
  background: rgba(0, 0, 0, 0.45);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  padding: 40px 32px;
  width: 340px;
}

.auth-title {
  font-size: 24px;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.7);
  text-align: center;
  margin-bottom: 24px;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.auth-input {
  width: 100%;
  padding: 12px 14px;
  box-sizing: border-box;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  color: rgba(255, 255, 255, 0.7);
  font-family: "Urbanist", "Mgen", sans-serif;
  font-weight: 300;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
}

.auth-input:focus {
  border-color: rgba(255, 255, 255, 0.3);
}

.auth-btn {
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
}

.auth-btn:hover {
  border-color: rgba(255, 255, 255, 0.4);
  background: rgba(255, 255, 255, 0.05);
}

.auth-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.auth-error {
  color: rgba(255, 150, 100, 0.7);
  font-size: 13px;
  font-weight: 300;
  text-align: center;
}

.auth-switch {
  text-align: center;
  margin-top: 16px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.3);
  cursor: pointer;
  font-weight: 300;
}

.auth-switch:hover {
  color: rgba(255, 255, 255, 0.5);
}
</style>
